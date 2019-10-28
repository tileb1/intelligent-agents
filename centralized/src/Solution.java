import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Random;
import java.util.Set;

import logist.simulation.Vehicle;

public class Solution implements Comparable<Solution>, Cloneable {
	private double cost;
	private HashMap<Vehicle, LinkedList<Wrapper>> nextTaskV;
	private Set<Vehicle> vehicles = this.nextTaskV.keySet(); // init in constructor ---------------------------------
	public static Random random = new Random();

	public Solution() {
		// TODO
		// Generate a feasible solution in wrappers and vehicle assos in nextTaskV
	}
	

	// TODO
	public ArrayList<Solution> getNeighbors() throws CloneNotSupportedException {
		ArrayList<Solution> neighbors = new ArrayList<Solution>();
		neighbors.addAll(this.getNeighborsChangeVehicle());
		return null;
	}
	
	private Vehicle getRandomVehicle() {
		int item = Solution.random.nextInt(vehicles.size());
		int i = 0;
		for (Vehicle vehicle : vehicles) {
			if (i == item)
				return vehicle;
			i++;
		}
		return null;
	}

	private ArrayList<Solution> getNeighborsChangeVehicle() throws CloneNotSupportedException {
		ArrayList<Solution> neighbors = new ArrayList<Solution>();
		Vehicle randomVehicle = null;
		for (int i = 0; i < 10; i++) {
			randomVehicle = this.getRandomVehicle();
			if (this.nextTaskV.get(randomVehicle).size() >= 2) {
				break;
			}
		}
		if (randomVehicle != null) {
			Vehicle otherVehicle = this.getRandomVehicle();
			Solution current = this.clone(randomVehicle, otherVehicle);
			LinkedList<Wrapper> wrappers = current.nextTaskV.get(otherVehicle);
			Wrapper pickup;
			Wrapper delivery;
			
			// Delete the first task from the random vehicle
			ListIterator<Wrapper> iterator = current.nextTaskV.get(randomVehicle).listIterator();
			pickup = iterator.next();
			iterator.remove();
			while (iterator.hasNext()) {
				delivery = iterator.next();
				if (delivery.task.equals(pickup.task)) {
					iterator.remove();
					break;
				}
			}
			
			// Try all possible positions for pickup and delivery
			ListIterator<Wrapper> iteratorForward = wrappers.listIterator();
			ListIterator<Wrapper> iteratorBackward = wrappers.listIterator(wrappers.size());
			while (iteratorBackward.hasPrevious()) {
				while (iteratorForward.hasNext() && iteratorForward.nextIndex() < iteratorBackward.previousIndex()) {
//					iteratorForward.set();
				}
			}
		}
		return neighbors;
	}

	public void updateCost() {

	}
	

	@SuppressWarnings("unchecked")
	public Solution clone(Vehicle random, Vehicle other) {
		try {
			Solution clone = (Solution) super.clone();
			clone.nextTaskV.put(random, (LinkedList<Wrapper>) this.nextTaskV.get(random).clone());
			clone.nextTaskV.put(other, (LinkedList<Wrapper>) this.nextTaskV.get(other).clone());
			return clone;
		} catch (CloneNotSupportedException e) {
			// Let it be
			return null;
		}
	}

	@Override
	public int compareTo(Solution o) {
		// TODO Auto-generated method stub
		return 0;
	}
}
