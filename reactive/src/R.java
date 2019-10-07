

import java.util.ArrayList;
import java.util.HashMap;

import logist.agent.Agent;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;

public class R {

	private HashMap<State, HashMap<City, Double>> rewards = new HashMap<State, HashMap<City,Double>>();
	private ArrayList<State> states = new ArrayList<State>();
	
	public R(HashMap<City, ArrayList<State>> cityStates, Agent agent, Topology topology, TaskDistribution td) {
		for(ArrayList<State> batchStates : cityStates.values()) {
			states.addAll(batchStates);
		}
		for(State initState : states) {
			HashMap<City, Double> fixedOriginStateRewards = new HashMap<City, Double>();
			for (City nextCity : cityStates.keySet()) {
				if(initState.toCity == nextCity) {
					fixedOriginStateRewards.put(nextCity, td.reward(initState.fromCity, nextCity) - initState.fromCity.distanceTo(nextCity) * agent.vehicles().get(0).costPerKm());
				} else if(initState.fromCity.hasNeighbor(nextCity)) {
					fixedOriginStateRewards.put(nextCity, - initState.fromCity.distanceTo(nextCity) * agent.vehicles().get(0).costPerKm());
				}
			}
			this.rewards.put(initState, fixedOriginStateRewards);
		}
	}
	public double get(State s, City a) {
		return rewards.get(s).get(a);
	}
}
