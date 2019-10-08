

import java.util.ArrayList;
import java.util.HashMap;

import logist.agent.Agent;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;

public class R {

	private HashMap<State, HashMap<City, Double>> rewards = new HashMap<State, HashMap<City,Double>>();
	
	public R(HashMap<City, ArrayList<State>> cityStates, Agent agent, Topology topology, TaskDistribution td) {
		// Iterate over every state
		for(ArrayList<State> states : cityStates.values()) {
			for(State initState: states) {
				HashMap<City, Double> initStateRewards = new HashMap<City, Double>();
				for (City nextCity : cityStates.keySet()) {
					// Set reward to expected reward minus cost of the action, if it corresponds to going to toCity of the state
					// Otherwise set reward to cost of the action (only neighbors)
					if(initState.toCity == nextCity) {
						initStateRewards.put(nextCity, td.reward(initState.fromCity, nextCity) - initState.fromCity.distanceTo(nextCity) * agent.vehicles().get(0).costPerKm());
					} else if(initState.fromCity.hasNeighbor(nextCity)) {
						initStateRewards.put(nextCity, - initState.fromCity.distanceTo(nextCity) * agent.vehicles().get(0).costPerKm());
					}
				}
				this.rewards.put(initState, initStateRewards);
			}
		}
	}
	public double get(State s, City a) {
		return rewards.get(s).get(a);
	}
}
