package template;

import logist.topology.Topology.City;

public class State {
	public City fromCity;
	public City toCity;
	
	public State(City f, City t) {
		fromCity = f;
		toCity = t;
	}

}
