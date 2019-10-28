import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import logist.simulation.Vehicle;

public class Solution implements Comparable<Solution>, Cloneable {
	private double cost;
	private HashSet<Wrapper> wrappers = new HashSet<Wrapper>();
	private HashMap<Vehicle, Wrapper> nextTaskV = new HashMap<Vehicle, Wrapper>();
	
	public Solution() {
		// TODO
		// Generate a feasible solution in wrappers and vehicle assos in nextTaskV
	}
	
	// TODO
	public ArrayList<Solution> getNeighbors() {
		return null;
	}
	
	public void updateCost() {
		
	}

	@Override
	public int compareTo(Solution o) {
		// TODO Auto-generated method stub
		return 0;
	}
}
