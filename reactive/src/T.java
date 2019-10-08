

import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;

import java.util.ArrayList;
import java.util.HashMap;

public class T {
	
	private HashMap<State, HashMap<City, HashMap<State, Double>>> transitionMatrix;
	private HashMap<City, HashMap<State, Double>> probabilities;
	
	public T(HashMap<City, ArrayList<State>> statesPerCity, Topology topology, TaskDistribution td) {
		this.transitionMatrix = new HashMap<State, HashMap<City, HashMap<State, Double>>>();
		this.probabilities = new HashMap<City, HashMap<State, Double>>();
		
		// First extract the probabilities data for all states into a HashMap organized by fromCity
		for (City city : statesPerCity.keySet()) {
			HashMap<State, Double> cityProbs = new HashMap<State, Double>();
			for (State state : statesPerCity.get(city)) {
				cityProbs.put(state, td.probability(state.fromCity, state.toCity));
			}
			this.probabilities.put(city, cityProbs);
		}
		
		// Iterate over all states in batches of states that share the same fromCity
		for (ArrayList<State> cityStates : statesPerCity.values()) {
			
			// Identify current city
			City currentCity = cityStates.get(0).fromCity; 
			HashMap<City, HashMap<State, Double>> neighboringStates = new HashMap<City, HashMap<State, Double>>();
			
			// Gather all probability data for all neighboring cities' states
			for (City neighbor : currentCity.neighbors()) {
				neighboringStates.put(neighbor, probabilities.get(neighbor));
			}
			
			// For each state populate the transition matrix with the probabilities of accessible states 
			// i.e. states of neighboring cities and states of the toCity (if it isn't a neighbor)
			for (State currentState : cityStates) {
				if (!(currentCity.neighbors().contains(currentState.toCity))) {
					HashMap<City, HashMap<State, Double>> accessibleStates = new HashMap<City, HashMap<State, Double>>();
					accessibleStates.putAll(neighboringStates);
					accessibleStates.put(currentState.toCity, probabilities.get(currentState.toCity));
					transitionMatrix.put(currentState, accessibleStates);
				} else {
					transitionMatrix.put(currentState, neighboringStates);
				}
			}
		}	
	}
	
	public double get(State s1, City a, State s2) {
		return this.transitionMatrix.get(s1).get(a).get(s2);
	}

}
