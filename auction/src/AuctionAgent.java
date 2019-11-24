
//the list of imports
import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import logist.LogistSettings;
import logist.Measures;
import logist.behavior.AuctionBehavior;
import logist.config.Parsers;
import logist.agent.Agent;
import logist.simulation.Vehicle;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

/**
 * A very simple auction agent that assigns all tasks to its first vehicle and
 * handles them sequentially.
 * 
 */
@SuppressWarnings("unused")
public class AuctionAgent implements AuctionBehavior {

	private Topology topology;
	private TaskDistribution distribution;
	private Agent agent;
	private Random random;
	private Vehicle vehicle;
	private City currentCity;
	private CentralizedAgent centralizedAgent;
	private Solution ourSolution;
	private Solution opponentSolution;
	private Solution theSolutionWeBidFor;
	private Solution theSolutionOpponentBidsFor;
	private List<Vehicle> ourVehicles;
	private List<Vehicle> opponentVehicles;
	private final double RATIO_UPPER = 1.2;
	private final double RATIO_LOWER = 0.8;
	private double ratio = 1.0;
	private int iter = 0;
	private double opponent_min_bid = Double.MAX_VALUE;
	private long timeout_setup;
	private long timeout_plan;
	private long timeout_bid;
	private ArrayList<Task> firstSpeculatedTasks;
	private ArrayList<Task> wonTasks = new ArrayList<Task>();
	private ArrayList<Task> oppenentWonTask = new ArrayList<Task>();
	private boolean iterTransientToSteady = false;

	public double totalBid = 0;

	@Override
	public void setup(Topology topology, TaskDistribution distribution, Agent agent) {

		this.topology = topology;
		this.distribution = distribution;
		this.agent = agent;
		this.vehicle = agent.vehicles().get(0);
		this.currentCity = vehicle.homeCity();
		this.ourVehicles = agent.vehicles();

		long seed = -9019554669489983951L * currentCity.hashCode() * agent.id();
		this.random = new Random(seed);

		// this code is used to get the timeouts
		LogistSettings ls = null;
		try {
			ls = Parsers.parseSettings("config" + File.separator + "settings_auction.xml");
		} catch (Exception exc) {
			System.out.println("There was a problem loading the configuration file.");
		}
		timeout_setup = ls.get(LogistSettings.TimeoutKey.SETUP);
		timeout_plan = ls.get(LogistSettings.TimeoutKey.PLAN);
		timeout_bid = ls.get(LogistSettings.TimeoutKey.BID);

		this.centralizedAgent = new CentralizedAgent(topology, distribution, agent, timeout_bid / 2);

		this.ourSolution = new Solution(agent.vehicles(), topology);
		this.firstSpeculatedTasks = this.initTasks(distribution, topology, 3);

		this.opponentVehicles = new ArrayList<Vehicle>();
		HashSet<City> ourCities = new HashSet<City>();

		for (Vehicle v : ourVehicles) {
			ourCities.add(v.homeCity());
		}
		ArrayList<City> remainingCities = new ArrayList<City>();
		for (City c : topology.cities()) {
			if (!ourCities.contains(c)) {
				remainingCities.add(c);
			}
		}

		for (final Vehicle v : ourVehicles) {
			Random rand = new Random();
			int randInt = rand.nextInt(remainingCities.size());
			final City homeCity = remainingCities.get(randInt);
			Vehicle newV = new Vehicle() {

				@Override
				public double speed() {
					return 0;
				}

				@Override
				public String name() {
					return null;
				}

				@Override
				public int id() {
					return 0;
				}

				@Override
				public City homeCity() {
					return homeCity;
				}

				@Override
				public long getReward() {
					return 0;
				}

				@Override
				public long getDistanceUnits() {
					return 0;
				}

				@Override
				public double getDistance() {
					return 0;
				}

				@Override
				public TaskSet getCurrentTasks() {
					return null;
				}

				@Override
				public City getCurrentCity() {
					return null;
				}

				@Override
				public int costPerKm() {
					return v.costPerKm();
				}

				@Override
				public Color color() {
					return null;
				}

				@Override
				public int capacity() {
					return v.capacity();
				}
			};
			remainingCities.remove(randInt);
			opponentVehicles.add(newV);
		}
		this.opponentSolution = new Solution(this.opponentVehicles, topology);
		this.ourSolution = this.centralizedAgent.getSolution(this.getTasksForSolution(null), this.ourVehicles);

	}

	@Override
	public void auctionResult(Task previous, int winner, Long[] bids) {
		if (winner == agent.id()) {
			this.totalBid += bids[agent.id()];
			this.updateMyState(previous);
			this.ourSolution = this.theSolutionWeBidFor;
			
			// We win so we will bid more less aggressivaly
			this.ratio = Math.max(Math.min(this.ratio * 1.05, this.RATIO_UPPER), this.RATIO_LOWER);
		} else {
			this.updateOpponentState(previous);
			this.opponentSolution = this.theSolutionOpponentBidsFor;
			
			// We lose so we will bid more aggressivaly
			this.ratio = Math.max(Math.min(this.ratio * 0.99, this.RATIO_UPPER), this.RATIO_LOWER);
		}
		this.opponent_min_bid = Math.min(this.opponent_min_bid, bids[(int) (1 - agent.id())]);
	}

	@Override
	public Long askPrice(Task task) {
		double bid;

		// In steady state
		if (this.firstSpeculatedTasks.size() == 0) {
			double ourMarginalCost;
			double opponentMarginalCost;
			if (this.iterTransientToSteady) {
				// Our company
				this.theSolutionWeBidFor = this.centralizedAgent.getSolution(this.getTasksForSolution(task),
						this.ourVehicles);
				if (this.theSolutionWeBidFor == null) {
					return null;
				}

				// Opponent company
				Task[] array = new Task[this.oppenentWonTask.size()];
				for (int i = 0; i < this.oppenentWonTask.size(); i++) {
					Task tmp = this.oppenentWonTask.get(i);
					array[i] = new Task(i, tmp.pickupCity, tmp.deliveryCity, tmp.reward, tmp.weight);
				}
				this.theSolutionOpponentBidsFor = this.centralizedAgent
						.getSolution(TaskSet.create(array), this.ourVehicles);
				this.opponentSolution = this.theSolutionOpponentBidsFor;
				
				// Marginal costs
				ourMarginalCost = this.theSolutionWeBidFor.getCost() - this.ourSolution.getCost();
			    opponentMarginalCost = 1000;

			} else {
				// Our company
				this.theSolutionWeBidFor = this.centralizedAgent.getSolution(task, this.ourSolution);
				if (this.theSolutionWeBidFor == null) {
					return null;
				}

				// Opponent company
				this.theSolutionOpponentBidsFor = this.centralizedAgent.getSolution(task, this.opponentSolution);
				this.opponentSolution = this.theSolutionOpponentBidsFor;
				
				// Marginal costs
				ourMarginalCost = this.theSolutionWeBidFor.getCost() - this.ourSolution.getCost();
				opponentMarginalCost = this.theSolutionOpponentBidsFor.getCost() - this.opponentSolution.getCost();
			}
			
			bid = opponentMarginalCost * ratio;
			if (bid < ourMarginalCost * 1) {
				bid = ourMarginalCost * 1;
			}
			if (bid <= 0) {
				bid = 250 - this.iter;
			}
			if (bid < this.opponent_min_bid) {
				bid = this.opponent_min_bid - 1;
			}
			
		}
		// Beginning of game
		else {
			this.theSolutionWeBidFor = this.centralizedAgent.getSolution(this.getTasksForSolution(task), this.ourVehicles);
			bid = this.theSolutionWeBidFor.getCost() - this.ourSolution.getCost();
			if (bid <= 0) {
				bid = 250 - this.iter;
			}
		}
		
		this.iter++;
		System.out.println(this.iter);
		return (long) bid;
	}
	
	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
		System.out.println(this.theSolutionOpponentBidsFor);
		this.centralizedAgent.setTimeout(timeout_plan);
		Solution solution = null;
		List<Plan> plan = null;

		// We don't win the last task
		if (tasks.size() == this.ourSolution.size()) {
			for (Task task : tasks) {
				this.ourSolution.setTask(task);
			}
			plan = this.centralizedAgent.plan(vehicles, this.ourSolution);
		}
		
		// We win the last task
		if (tasks.size() == this.ourSolution.size() + 1) {
			for (Task task : tasks) {
				this.theSolutionWeBidFor.setTask(task);
			}
			plan = this.centralizedAgent.plan(vehicles, this.theSolutionWeBidFor);
		}
		
		// The auction agent has not even entered steady state...
		if (tasks.size() > this.ourSolution.size() + 1) {
			plan = this.centralizedAgent.plan(vehicles, tasks);
		}

		return plan;
	}

	private void updateMyState(Task task) {
		this.iterTransientToSteady = false;
		if (this.firstSpeculatedTasks.size() > 0) {
			if (this.firstSpeculatedTasks.size() == 1) {
				this.iterTransientToSteady = true;
			}
			this.firstSpeculatedTasks.remove(0);
		}
		this.wonTasks.add(task);
	}

	private void updateOpponentState(Task task) {
		this.oppenentWonTask.add(task);
	}

	private TaskSet getTasksForSolution(Task offer) {
		Task[] array;
		if (offer != null) {
			array = new Task[this.wonTasks.size() + this.firstSpeculatedTasks.size()+1];
		}
		else {
			array = new Task[this.wonTasks.size() + this.firstSpeculatedTasks.size()];
		}
		
		for (int i = 0; i < this.wonTasks.size(); i++) {
			Task task = this.wonTasks.get(i);
			array[i] = new Task(i, task.pickupCity, task.deliveryCity, task.reward, task.weight);
		}
		for (int i = 0; i < this.firstSpeculatedTasks.size(); i++) {
			Task task = this.firstSpeculatedTasks.get(i);
			array[i + this.wonTasks.size()] = new Task(i+this.wonTasks.size(), task.pickupCity, task.deliveryCity, task.reward, task.weight);
		}
		if (offer != null) {
			array[this.wonTasks.size() + this.firstSpeculatedTasks.size()] = new Task(this.wonTasks.size() + this.firstSpeculatedTasks.size(), offer.pickupCity, offer.deliveryCity, offer.reward, offer.weight);
		}
		
		return TaskSet.create(array);
	}

	public ArrayList<Task> initTasks(TaskDistribution td, Topology tp, int num) {
		ArrayList<City> froms = new ArrayList<City>();
		ArrayList<City> tos = new ArrayList<City>();
		ArrayList<Double> probabilities = new ArrayList<Double>();

		for (int i = 0; i < num; i++) {
			probabilities.add((double) 0);
			froms.add(tp.cities().get(0));
			tos.add(tp.cities().get(0));
		}

		for (City c_f : tp.cities()) {
			for (City c_t : tp.cities()) {
				Double prob = td.probability(c_f, c_t);
				int iter = -1;
				for (Double observed_p : probabilities) {
					if (prob > observed_p) {
						iter++;
						continue;
					} else {
						break;
					}
				}
				if (iter >= 0) {
					probabilities.remove(iter);
					probabilities.add(iter, prob);
					froms.remove(iter);
					froms.add(iter, c_f);
					tos.remove(iter);
					tos.add(iter, c_t);
				}
			}
		}

		ArrayList<Task> initialTasks = new ArrayList<Task>();
		int dummy_id = 0;
		for (int i = 0; i < num; i++) {
			City from_city = froms.get(i);
			City to_city = tos.get(i);
			Task t = new Task(dummy_id + i, from_city, to_city, td.reward(from_city, to_city),
					td.weight(from_city, to_city));
			initialTasks.add(t);
		}

		return initialTasks;
	}

}
