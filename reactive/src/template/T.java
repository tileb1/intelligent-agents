package template;

import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;

import java.util.ArrayList;
import java.util.HashMap;

public class T {
	
	private HashMap<State, HashMap<City, HashMap<State, Double>>> mat;
	private HashMap<City, HashMap<State, Double>> probabilities;
	
	public T(HashMap<City, ArrayList<State>> cityStates, Topology topology, TaskDistribution td) {
		this.mat = new HashMap<State, HashMap<City, HashMap<State, Double>>>();
		probabilities = new HashMap<City, HashMap<State, Double>>();
		
		// First extract the probabilities data for all states into a HashMap
		// that is organised by current city
		for (City city : cityStates.keySet()) {
			HashMap<State, Double> cityProbs = new HashMap<State, Double>();
			for (State state : cityStates.get(city)) {
				cityProbs.put(state, td.probability(state.fromCity, state.toCity));
			}
			probabilities.put(city, cityProbs);
		}
		
		// Iterate over all states in batches of states that share the same current city
		for (ArrayList<State> statesBatch : cityStates.values()) {
			
			// Identify current city and its neighbours
			City currentCity = statesBatch.get(0).fromCity; 
			HashMap<City, HashMap<State, Double>> cityToStatesMap = new HashMap<City, HashMap<State, Double>>();
			ArrayList<City> neighbours = (ArrayList<City>) currentCity.neighbors();
			
			// Gather all neighbours' probability data for all the relevant states
			for (City neighbour : neighbours) {
				cityToStatesMap.put(neighbour, probabilities.get(neighbour));
			}
			
			// For every state that shares the same current city, populate the
			// HashMap representing the transition matrix with the transition 
			// probabilities of accessible states i.e. all neighbouring states
			// and additionally the states that have as current city the delivery 
			// city of the current task (if it isn't a neighbour)
			for (State currentState : statesBatch) {
				if (!(neighbours.contains(currentState.toCity))) {
					HashMap<City, HashMap<State, Double>> accessibleStatesMap = new HashMap<City, HashMap<State, Double>>();
					accessibleStatesMap.putAll(cityToStatesMap);
					accessibleStatesMap.put(currentState.toCity, probabilities.get(currentState.toCity));
					mat.put(currentState, accessibleStatesMap);
				} else {
					mat.put(currentState, cityToStatesMap);
				}
			}
		}	
	}
	
	public double get(State s1, City a, State s2) {
		return this.mat.get(s1).get(a).get(s2);
	}

}
