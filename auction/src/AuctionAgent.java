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
	private final double RATIO_UPPER = 1.0;
	private final double RATIO_LOWER = 0.7;
	private double ratio = 0.7;
	private int iter = 0;
	private double opponent_min_bid = Double.MAX_VALUE;
	private long timeout_setup;
	private long timeout_plan;
	private long timeout_bid;
	
	public double totalBid = 0;

	@Override
	public void setup(Topology topology, TaskDistribution distribution,
			Agent agent) {

		this.topology = topology;
		this.distribution = distribution;
		this.agent = agent;
		this.vehicle = agent.vehicles().get(0);
		this.currentCity = vehicle.homeCity();
		this.ourVehicles = agent.vehicles();

		long seed = -9019554669489983951L * currentCity.hashCode() * agent.id();
		this.random = new Random(seed);
		// --------------------------------------------------------------------------------------FIX TIMEOUT
		
        // this code is used to get the timeouts
        LogistSettings ls = null;
        try {
            ls = Parsers.parseSettings("config" + File.separator + "settings_auction.xml");
        }
        catch (Exception exc) {
            System.out.println("There was a problem loading the configuration file.");
        }
        
        // the setup method cannot last more than timeout_setup milliseconds
        timeout_setup = ls.get(LogistSettings.TimeoutKey.SETUP);
        timeout_plan = ls.get(LogistSettings.TimeoutKey.PLAN);
        timeout_bid = ls.get(LogistSettings.TimeoutKey.BID);
		
		this.centralizedAgent = new CentralizedAgent(topology, distribution, agent, timeout_bid/2);
		
		this.ourSolution = new Solution(agent.vehicles(), topology);
		this.opponentSolution = new Solution(agent.vehicles(), topology);
		
		this.opponentVehicles = new ArrayList<Vehicle>();
		HashSet<City> ourCities = new HashSet<City>();
		
		for (Vehicle v: ourVehicles) {
			ourCities.add(v.homeCity());
		}
		ArrayList<City> remainingCities = new ArrayList<City>();
		for (City c: topology.cities()) {
			if (!ourCities.contains(c)) {
				remainingCities.add(c);
			}
		}
		
		for (Vehicle v: ourVehicles) {
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
					return 0;
				}
				
				@Override
				public Color color() {
					return null;
				}
				
				@Override
				public int capacity() {
					return 0;
				}
			};
			remainingCities.remove(randInt);
			opponentVehicles.add(newV);
		}
		
	}

	@Override
	public void auctionResult(Task previous, int winner, Long[] bids) {
		if (winner == agent.id()) {
			this.totalBid += bids[agent.id()];
			for (int i = 0; i < bids.length; i++) {
				System.out.println("Agent " + i + ": " + bids[i]);
			}
			this.ourSolution = this.theSolutionWeBidFor;
			// We win so we will bid more less aggressivaly
			this.ratio = Math.max(Math.min(this.ratio*1.01, this.RATIO_UPPER), this.RATIO_LOWER);
			System.out.println("Agent " + agent.id() + " has this much profit: " + (this.totalBid - this.ourSolution.getCost()));
			System.out.println(this.totalBid);
		}
		else {
			this.opponentSolution = this.theSolutionOpponentBidsFor;
			// We lose so we will bid more aggressivaly
			this.ratio = Math.max(Math.min(this.ratio*0.99, this.RATIO_UPPER), this.RATIO_LOWER);
		}
		this.opponent_min_bid = Math.min(this.opponent_min_bid, bids[(int) (1 - agent.id())]);
	}
	
	@Override
	public Long askPrice(Task task) {
		// Our company
		this.theSolutionWeBidFor = this.centralizedAgent.getSolution(task, this.ourSolution);
		if (this.theSolutionWeBidFor == null) {
			return null;
		}
		
		// Opponent company
		this.theSolutionOpponentBidsFor = this.centralizedAgent.getSolution(task, this.opponentSolution);

		// Marginal costs
		double ourMarginalCost = this.theSolutionWeBidFor.getCost() - this.ourSolution.getCost();
		double opponentMarginalCost = opponentSolution.getCost() - this.opponentSolution.getCost();
		
		double bid = opponentMarginalCost * ratio;
		if (bid < ourMarginalCost * 0.8) {
			bid = ourMarginalCost * 0.8;
		}
		if (bid < this.opponent_min_bid) {
			bid = this.opponent_min_bid - 1;
		}
		if (this.iter < 3) {
			bid = ourMarginalCost * 0.5;
		}
		this.iter++;
		
		// Some random agent
		bid = this.random.nextDouble() * 5000;
		
		// Some cheap ass agent
		bid = ourMarginalCost;
		
		// Baller agent
		bid = ourMarginalCost * 1.1;
		return (long) bid;
	}

	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
		this.centralizedAgent.setTimeout(timeout_plan);
		List<Plan> plans = this.centralizedAgent.plan(vehicles, tasks);
		System.out.println(plans);
		System.out.println("Agent " + this.agent.id() + " : " + (this.totalBid - this.ourSolution.getCost()));
        return plans;
	}

}
