import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import logist.simulation.Vehicle;

public class Solution implements Comparable<Solution>, Cloneable {
	private double cost;
	private HashMap<Vehicle, LinkedList<Wrapper>> nextTaskV;
	
	public Solution() {
		// TODO
		// Generate a feasible solution in wrappers and vehicle assos in nextTaskV
	}
	
	// TODO
	public ArrayList<Solution> getNeighbors() {
		ArrayList<Solution> neighbors = new ArrayList<Solution>();
		return null;
	}
	
	private ArrayList<Solution> changeVehicle() {
		
		ArrayList<Solution> neighbors = new ArrayList<Solution>();
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
