package template;

import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;

import java.util.ArrayList;
import java.util.HashMap;

public class T {
	
	private HashMap<State, HashMap<City, HashMap<State, Double>>> mat;
	
	public T(HashMap<City, ArrayList<State>> cityStates, Topology topology, TaskDistribution td) {
		mat = new HashMap<State, HashMap<City, HashMap<State, Double>>>();
		for (ArrayList<State> statesBatch : cityStates.values()) {
			
			City currentCity = statesBatch.get(0).fromCity; 
			HashMap<City, HashMap<State, Double>> cityToStatesMap = new HashMap<City, HashMap<State, Double>>();
			ArrayList<City> neighbours = (ArrayList<City>) currentCity.neighbors();
			
			for (City neighbour : neighbours) {
				for (State neighbourStates : cityStates.get(neighbour)) {
					
				}
			}
			
			for (State currentState : statesBatch) {
				if (!(neighbours.contains(currentState.toCity))) {
					//cityToStatesMap.put(currentCity, remappingFunction);
				}
			}
		}	
	}
	
	public double get(State s1, City a, State s2) {
		return this.mat.get(s1).get(a).get(s2);
	}

}
