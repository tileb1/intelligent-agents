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

public class ReactiveAgent implements ReactiveBehavior {

	private int numActions;
	private Agent myAgent;
	private HashMap<City, ArrayList<State>> statesMap;
	private T T;
	private R R;
	private HashMap<State, Double> V = new HashMap<State, Double>();
	private HashMap<State, City> PI = new HashMap<State, Topology.City>();

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {

		// Reads the discount factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		Double discount = agent.readProperty("discount-factor", Double.class, 0.95);

		this.numActions = 0;
		this.myAgent = agent;

		// Data structure for fast iteration over relevant states
		this.statesMap = new HashMap<City, ArrayList<State>>();
		for (City fromCity : topology.cities()) {
			ArrayList<State> states = new ArrayList<State>();
			for (City toCity : topology.cities()) {
				State newState;
				if (toCity.equals(fromCity)) {
					newState = new State(fromCity, null);
				} else {
					newState = new State(fromCity, toCity);
				}
				states.add(newState);
			}
			this.statesMap.put(fromCity, states);
		}

		// Initialize needed "tensors"
		this.T = new T(statesMap, topology, td);
		this.R = new R(statesMap, this.myAgent, topology, td);
		for (ArrayList<State> stateList : this.statesMap.values()) {
			for (State s : stateList) {
				// Random initialization
				this.V.put(s, 0.0);
			}
		}

		this.doValueIteration(discount);
	}

	/*
	 * Learns V offline to compute the best policy.
	 */
	private void doValueIteration(double gamma) {
		// Loop until good enough (change later)
		// -------------------------------------------------------------------
		for (int index = 0; index < 10000; index++) {

			// The 2 following for loops loop over all the possible states
			for (ArrayList<State> stateList : this.statesMap.values()) {
				for (State s : stateList) {

					// Loop over most actions (going to neighbor)
					double bestQ = Double.NEGATIVE_INFINITY;
					City bestAction = null;
					for (City a : s.fromCity.neighbors()) {
						System.out.print(s);
						System.out.println(" + " + a.name);
						double QNew = this.R.get(s, a) + gamma * this.getExpectedV(s, a);
						if (QNew > bestQ) {
							bestQ = QNew;
							bestAction = a;
						}
					}
					
					// Loop over last action (going to task if possible)
					if (s.toCity != null) {
						double QNew = this.R.get(s, s.toCity) + gamma * this.getExpectedV(s, s.toCity);
						if (QNew > bestQ) {
							bestQ = QNew;
							bestAction = s.toCity;
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

		// We don't care about the weight of the task as described by the assistants
		State currentState;
		Action action;
		// Move to best neighbor if no task available.
		if (availableTask == null) {
			currentState = new State(vehicle.getCurrentCity(), null);
			action = new Move(this.getPolicy(currentState));
		}
		// Move to best neighbor and pickup task if deliver city corresponds.
		else {
			currentState = new State(vehicle.getCurrentCity(), availableTask.deliveryCity);
			City a = this.getPolicy(currentState);
			if (availableTask.deliveryCity.equals(a)) {
				action = new Pickup(availableTask);
			} else {
				action = new Move(a);
			}
		}

		if (numActions >= 1) {
			System.out.println("The total profit after " + numActions + " actions is " + myAgent.getTotalProfit()
					+ " (average profit: " + (myAgent.getTotalProfit() / (double) numActions) + ")");
		}
		numActions++;
		return action;
	}
}
