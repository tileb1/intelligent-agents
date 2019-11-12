//the list of imports
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import logist.Measures;
import logist.behavior.AuctionBehavior;
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
	private List<Vehicle> ourVehicles;
	private List<Vehicle> opponentVehicles;

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
		this.centralizedAgent = new CentralizedAgent(topology, distribution, agent, 5000);
		
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
					return remainingCities.get(randInt);
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
		System.out.println("Winner " + winner);
		for (int i = 0; i < bids.length; i++) {
			System.out.println(bids[i]);
		}
		if (winner == agent.id()) {
			currentCity = previous.deliveryCity;
		}
	}
	
	@Override
	public Long askPrice(Task task) {
		// Our company
		double cost = this.ourSolution.getCost();
		Solution solution = this.centralizedAgent.getSolution(task, this.ourSolution);
		if (solution == null) {
			return null;
		}
		
		// Oponent company
		double opponentCost = this.opponentSolution.getCost();
		Solution opponentSolution = this.centralizedAgent.getSolution(task, this.opponentSolution);
//		if (opponentSolution == null) {
//			return Long.MAX_VALUE;
//		}
		double marginalCost = solution.getCost() - cost;
		double opponentMarginalCost = opponentSolution.getCost() - opponentCost;
		return (long) marginalCost;
	}

	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
		
//		System.out.println("Agent " + agent.id() + " has tasks " + tasks);

		Plan planVehicle1 = naivePlan(vehicle, tasks);

		List<Plan> plans = new ArrayList<Plan>();
		plans.add(planVehicle1);
		while (plans.size() < vehicles.size())
			plans.add(Plan.EMPTY);

		return plans;
	}

	private Plan naivePlan(Vehicle vehicle, TaskSet tasks) {
		City current = vehicle.getCurrentCity();
		Plan plan = new Plan(current);

		for (Task task : tasks) {
			// move: current city => pickup location
			for (City city : current.pathTo(task.pickupCity))
				plan.appendMove(city);

			plan.appendPickup(task);

			// move: pickup location => delivery location
			for (City city : task.path())
				plan.appendMove(city);

			plan.appendDelivery(task);

			// set current city
			current = task.deliveryCity;
		}
		return plan;
	}
}
