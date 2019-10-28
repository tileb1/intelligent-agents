import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
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
	private Set<Vehicle> vehicles = this.nextTaskV.keySet(); // init in constructor ---------------------------------
	public static Random random = new Random();

	public Solution(ArrayList<Vehicle> vehicles, Topology topology, TaskSet taskset) {
		// TODO
		// Generate a feasible solution in wrappers and vehicle assos in nextTaskV
		this.nextTaskV = new HashMap<Vehicle, LinkedList<Wrapper>>();
		Vehicle biggestVehicle = null;
		
		for (Vehicle v: vehicles) {
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
		Vehicle removeVehicle = null;
		for (int i = 0; i < 10; i++) {
			removeVehicle = this.getRandomVehicle();
			if (this.nextTaskV.get(removeVehicle).size() >= 2) {
				break;
			}
		}
		if (removeVehicle != null) {
			Vehicle addVehicle = this.getRandomVehicle();
			Solution current = this.clone(removeVehicle, addVehicle);
			LinkedList<Wrapper> wrappers = current.nextTaskV.get(addVehicle);
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
			
			// Try all possible positions for pickup and delivery
			ListIterator<Wrapper> iteratorForward = wrappers.listIterator();
			ListIterator<Wrapper> iteratorBackward = wrappers.listIterator(wrappers.size());
			while (iteratorBackward.hasPrevious()) {
				while (iteratorForward.hasNext() && iteratorForward.nextIndex() < iteratorBackward.previousIndex()) {
					iteratorForward.add(pickup);
					iteratorBackward.add(delivery);
					
					// add neighbours to list
					neighbors.add(current.clone(addVehicle, wrappers));
					
					// Reset forward iterator
					iteratorForward.next();
					iteratorForward.remove();
					
					// Reset backward iterator
					iteratorBackward.next();
					iteratorBackward.remove();
					
					// Take iterator step
					iteratorBackward.previous();
					iteratorForward.next();
					
				}
			}
		}
		return neighbors;
	}

	public void updateCost() {
		for (Vehicle v: this.nextTaskV.keySet()) {
			City prevCity = v.homeCity();
			for (Wrapper w: this.nextTaskV.get(v)) {
				this.cost += prevCity.distanceTo(w.getCity()) * v.costPerKm();
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
	
	@SuppressWarnings("unchecked")
	public Solution clone(Vehicle vehicle, LinkedList<Wrapper> wrappers) {
		try {
			Solution clone = (Solution) super.clone();
			clone.nextTaskV.put(vehicle, (LinkedList<Wrapper>) wrappers.clone());
			return clone;
		} catch (CloneNotSupportedException e) {
			// Let it be
			return null;
		}
	}

	@Override
	public int compareTo(Solution o) {
		return (int) (this.cost - o.cost);
	}
}
