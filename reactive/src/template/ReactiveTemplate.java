package template;

import java.util.Random;
import java.util.HashMap;
import java.util.ArrayList;

import logist.simulation.Vehicle;
import logist.agent.Agent;
import logist.behavior.ReactiveBehavior;
import logist.plan.Action;
import logist.plan.Action.Move;
import logist.plan.Action.Pickup;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;

public class ReactiveTemplate implements ReactiveBehavior {

	private Random random;
	private double pPickup;
	private int numActions;
	private Agent myAgent;
	private HashMap<City, ArrayList<State>> statesMap;
	private T T;
	private R R;
	private HashMap<State, Double> V;
	private HashMap<State, City> PI;

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {

		// Reads the discount factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		Double discount = agent.readProperty("discount-factor", Double.class,
				0.95);

		this.random = new Random();
		this.pPickup = discount;
		this.numActions = 0;
		this.myAgent = agent;
		
		// Data structure for fast iteration over relevant states
		this.statesMap = new HashMap<City, ArrayList<State>>();
		for (City fromCity: topology.cities()) {
			ArrayList<State> states = new ArrayList<State>();
			for (City toCity: topology.cities()) {
				State newState;
				if (toCity == fromCity) {
					newState = new State(fromCity, toCity);
				}
				else {
					newState = new State(fromCity, null);
				}
				states.add(newState);
			}
			this.statesMap.put(fromCity, states);
		}
		
		// Initialize needed "tensors"
		this.T = new T(statesMap, topology, td);
		this.R = new R();
		for (ArrayList<State> stateList : this.statesMap.values()) {
			for (State s : stateList) {
				// Random initialization
				this.V.put(s, 0.0);
			}
		}
	}
	
	/*
	 * Learns V offline to compute the best policy.
	 */
	public void doValueIteration(double gamma) {
		// Loop until good enough (change later) -------------------------------------------------------------------
		for (int index = 0; index < 10; index++) {
			
			// The 2 following for loops loop over all the possible states
			for (ArrayList<State> stateList : this.statesMap.values()) {
				for (State s : stateList) {
					
					// Loop over all actions
					double bestQ = Double.NEGATIVE_INFINITY;
					City bestAction = null;
					for (City a : this.statesMap.keySet()) {
						double QNew = this.R.get(s, a) + gamma * this.getExpectedV(s, a);
						if (QNew > bestQ) {
							bestQ = QNew;
							bestAction = a;
						}
					}
					
					// Set V(s) to max of Q over all possible actions
					this.V.put(s, bestQ);
					this.PI.put(s, bestAction);
				}
			}
		}
		
	}
	
	/*
	 * Returns the optimal learned policy given a state
	 */
	private City getPolicy(State s) {
		return this.PI.get(s);
	}
	
	/*
	 * Compute expected value of V given a current state and an action.
	 */
	private double getExpectedV(State s, City a) {
		double cumul = 0.0;
		for (State sPrime : this.statesMap.get(a)) {
			cumul += this.T.get(s, a, sPrime) * this.V.get(sPrime);
		}
		return cumul;
	}

	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
		Action action;

		if (availableTask == null || random.nextDouble() > pPickup) {
			City currentCity = vehicle.getCurrentCity();
			action = new Move(currentCity.randomNeighbor(random));
		} else {
			action = new Pickup(availableTask);
		}
		
		if (numActions >= 1) {
			System.out.println("The total profit after "+numActions+" actions is "+myAgent.getTotalProfit()+" (average profit: "+(myAgent.getTotalProfit() / (double)numActions)+")");
		}
		numActions++;
		
		return action;
	}
}
