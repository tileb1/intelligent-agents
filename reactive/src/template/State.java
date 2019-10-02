package template;

import java.util.Objects;

import logist.topology.Topology.City;

public class State {
	public City fromCity;
	public City toCity;

	public State(City f, City t) {
		fromCity = f;
		toCity = t;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		State s = (State) o;
		return s.fromCity == this.fromCity && s.toCity == this.toCity;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(fromCity, toCity);
	}

}
