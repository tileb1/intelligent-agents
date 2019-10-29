import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.Set;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

public class Solution implements Comparable<Solution>, Cloneable {
	private double cost;
	private HashMap<Vehicle, LinkedList<Wrapper>> nextTaskV;
	public static Random random = new Random();
	private Set<Vehicle> vehicles;

	public Solution(List<Vehicle> vehicles2, Topology topology, TaskSet taskset) {
		// TODO
		// Generate a feasible solution in wrappers and vehicle assos in nextTaskV
		this.nextTaskV = new HashMap<Vehicle, LinkedList<Wrapper>>();
		Vehicle biggestVehicle = null;
		
		for (Vehicle v: vehicles2) {
			if (biggestVehicle == null || v.capacity() > biggestVehicle.capacity()) {
				biggestVehicle = v;
			}
			this.nextTaskV.put(v, new LinkedList<Wrapper>());
		}
		
		
		
		LinkedList<Wrapper> biggestVehiclePlan = new LinkedList<Wrapper>();
		
		for (Task task: taskset) {
			biggestVehiclePlan.addLast(new Wrapper(task, true, biggestVehicle.capacity() - task.weight));
			biggestVehiclePlan.addLast(new Wrapper(task, false, biggestVehicle.capacity()));
		}
		
		this.nextTaskV.put(biggestVehicle, biggestVehiclePlan);
		updateCost();
		this.vehicles = this.nextTaskV.keySet();
	}
	

	// TODO
	public ArrayList<Solution> getNeighbors() throws CloneNotSupportedException {
		ArrayList<Solution> neighbors = new ArrayList<Solution>();
		ArrayList<Solution> changeVehicleNeighbors = this.getNeighborsChangeVehicle();
		ArrayList<Solution> swapTaskseNeighbors = this.getNeighborsSwapTasks();
		if (changeVehicleNeighbors.size() > 0) {
			neighbors.addAll(changeVehicleNeighbors);
		}
		if (swapTaskseNeighbors.size() > 0) {
			neighbors.addAll(swapTaskseNeighbors);
		}
		for (Solution n : neighbors) {
			n.updateCost();
			if(!n.isFeasible()) {
				neighbors.remove(n);
			}
		}
		return neighbors;
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
	
	private ArrayList<Solution> getNeighborsSwapTasks() {
		ArrayList<Solution> neighbors = new ArrayList<Solution>();
		Vehicle vehicle = null;
		for (int i = 0; i < 10; i++) {
			vehicle = this.getRandomVehicle();
			if (this.nextTaskV.get(vehicle).size() >= 4) {
				break;
			}
		}
		LinkedList<Wrapper> wrappers = this.nextTaskV.get(vehicle);
		if (wrappers.size() >= 4) {
			// Delete the task from the vehicle
			Solution current = this.clone(vehicle, wrappers);
			wrappers = current.nextTaskV.get(vehicle);
			int toRemove = Solution.random.nextInt(wrappers.size());
			ListIterator<Wrapper> iterator = wrappers.listIterator();
			Wrapper pickup = null;
			Wrapper delivery = null;
			Wrapper removed = null;
			while (iterator.hasNext()) {
				if (iterator.nextIndex() == toRemove) {
					removed = iterator.next();
					iterator.remove();
					if (removed.pickup == false) {
						delivery = removed;
						break;
					}
					else {
						pickup = removed;
					}
				}
				Wrapper next = iterator.next();
				if (removed != null && next.task.equals(removed.task)) {
					delivery = next;
					iterator.remove();
					break;
				}
			}
			if (pickup == null) {
				if (delivery == null) {
					throw new IllegalStateException();
				}
				iterator = wrappers.listIterator();
				while (iterator.hasNext()) {
					Wrapper next = iterator.next();
					if (next.task.equals(delivery.task)) {
						pickup = next;
						iterator.remove();
						break;
					}
				}
			}
//			System.out.println(pickup);
//			System.out.println(delivery);
//			System.out.println(removed);
//			System.out.println(this);
//			System.out.println(current);
//			System.out.println("---------------------------------------------------");
			this.addToNeighbors(neighbors, current, pickup, delivery, vehicle);
		}
		return neighbors;
	}

	private ArrayList<Solution> getNeighborsChangeVehicle() throws CloneNotSupportedException {
		ArrayList<Solution> neighbors = new ArrayList<Solution>();
		Vehicle removeVehicle = null;
		for (int i = 0; i < 10; i++) {
			removeVehicle = this.getRandomVehicle();
			if (this.nextTaskV.get(removeVehicle).size() >= 2) {
				break;
			}
		}
		if (removeVehicle != null) {
			if (this.nextTaskV.get(removeVehicle).size() < 2) {
				return neighbors;
			}
			Vehicle addVehicle = this.getRandomVehicle();
			if (removeVehicle.equals(addVehicle)) {
				return neighbors;
			}
			Solution current = this.clone(removeVehicle, addVehicle);
			Wrapper pickup = null;
			Wrapper delivery = null;
			
			// Delete the first task from the random vehicle
			ListIterator<Wrapper> iterator = current.nextTaskV.get(removeVehicle).listIterator();
			pickup = iterator.next();
			iterator.remove();
			while (iterator.hasNext()) {
				delivery = iterator.next();
				if (delivery.task.equals(pickup.task)) {
					iterator.remove();
					break;
				}
			}
			this.addToNeighbors(neighbors, current, pickup, delivery, addVehicle);
		}
		return neighbors;
	}
	
	
	@SuppressWarnings("unchecked")
	private void addToNeighbors(ArrayList<Solution> neighbors, Solution current, Wrapper pickup, Wrapper delivery, Vehicle addVehicle) {
		LinkedList<Wrapper> wrappers = current.nextTaskV.get(addVehicle);
		if (wrappers.size() == 0) {
			wrappers.add(pickup);
			wrappers.add(delivery);
			neighbors.add(current.clone(addVehicle, wrappers));
		}
		else {
			// Try all possible positions for pickup and delivery
			ListIterator<Wrapper> iteratorBackward = wrappers.listIterator(wrappers.size());
			ListIterator<Wrapper> iteratorForward;
			while (iteratorBackward.hasPrevious()) {
				iteratorBackward.add(delivery);
				iteratorBackward.previous();
				LinkedList<Wrapper> wrappers2 = (LinkedList<Wrapper>) wrappers.clone();
				iteratorForward = wrappers2.listIterator();
				while (iteratorForward.hasNext() && iteratorForward.nextIndex() < iteratorBackward.previousIndex()) {
					iteratorForward.add(pickup);
					
					// Add neighbours to list
					neighbors.add(current.clone(addVehicle, wrappers2));
					
					// Reset forward iterator
					iteratorForward.next();
					iteratorForward.remove();
					
					// Take iterator step forward
					iteratorForward.next();
				}
				// Reset backward iterator and take step
				iteratorBackward.next();
				
				iteratorBackward.remove();
				iteratorBackward.previous();
			}
		}
	}

	public void updateCost() {
		this.cost = 0; // ----------------------------------------------------------------------RIGHT???
		for (Vehicle v: this.nextTaskV.keySet()) {
			City prevCity = v.homeCity();
			for (Wrapper w: this.nextTaskV.get(v)) {
				try {
					this.cost += prevCity.distanceTo(w.getCity()) * v.costPerKm();
				}
				catch (NullPointerException e) {
					System.out.println(prevCity);
					System.out.println(v);
					System.out.println(w);
					System.out.println(this.nextTaskV.get(v));
				}
				prevCity = w.getCity();
			}
		}
	}
	
	public boolean isFeasible() {
		for (Vehicle v: this.nextTaskV.keySet()) {
			double load = 0;
			for (Wrapper w: this.nextTaskV.get(v)) {
				if (w.isPickup()) {
					load += w.getTask().weight;
				}
				else {
					load -= w.getTask().weight;
				}
				if (load > v.capacity()) {
					return false;
				}
			}
		}
		return true;
	}
	
	public HashMap<Vehicle, LinkedList<Wrapper>> getPlans () {
		return this.nextTaskV;
	}

	public Solution clone(Vehicle random, Vehicle other) {
		try {
			Solution clone = (Solution) super.clone();
			clone.nextTaskV.put(random, new LinkedList<Wrapper>(this.nextTaskV.get(random)));
			clone.nextTaskV.put(other, new LinkedList<Wrapper>(this.nextTaskV.get(other)));
			return clone;
		} catch (CloneNotSupportedException e) {
			// Let it be
			return null;
		}
	}
	
	public Solution clone(Vehicle vehicle, LinkedList<Wrapper> wrappers) {
		try {
			Solution clone = (Solution) super.clone();
			clone.nextTaskV.put(vehicle, new LinkedList<Wrapper>(wrappers));
			return clone;
		} catch (CloneNotSupportedException e) {
			// Let it be
			return null;
		}
	}

	@Override
	public int compareTo(Solution o) {
		return (int) -(this.cost - o.cost);
	}
	
	public double getCost() {
		return this.cost;
	}
	
	public String toString() {
		String sString = new String();
		for (Vehicle v: this.nextTaskV.keySet()) {
			String vString = "Vehicle" + v.id() + ", origin: " + v.homeCity().toString() + ", capacity: " + v.capacity() + ", costPerKm: " + v.costPerKm() + ", tasks: " + this.nextTaskV.get(v).size() + "\n";
			for (Wrapper w: this.nextTaskV.get(v)) {
				vString = vString + w.toString();
			}
		sString = sString + vString;
		}
		return sString;
	}
}
