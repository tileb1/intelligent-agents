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
		this.nextTaskV = new HashMap<Vehicle, LinkedList<Wrapper>>();
		Vehicle biggestVehicle = null;

		// Find vehicle with largest capacity
		for (Vehicle v : vehicles2) {
			if (biggestVehicle == null || v.capacity() > biggestVehicle.capacity()) {
				biggestVehicle = v;
			}
			this.nextTaskV.put(v, new LinkedList<Wrapper>());
		}

		LinkedList<Wrapper> biggestVehiclePlan = new LinkedList<Wrapper>();
		for (Task task : taskset) {
			biggestVehiclePlan.addLast(new Wrapper(task, true, biggestVehicle.capacity() - task.weight));
			biggestVehiclePlan.addLast(new Wrapper(task, false, biggestVehicle.capacity()));
		}

		this.nextTaskV.put(biggestVehicle, biggestVehiclePlan);
		this.updateCost();
		this.vehicles = this.nextTaskV.keySet();
	}

	public Solution(Set<Vehicle> vehicles, HashMap<Vehicle, LinkedList<Wrapper>> nextTaskV) {
		this.nextTaskV = nextTaskV;
		this.vehicles = vehicles;
	}

	/*
	 * Returns the neighboring solution. This is the most important method of the
	 * class and should be called by the CentralizedAgent.
	 */
	public ArrayList<Solution> getNeighbors() {
		ArrayList<Solution> neighbors = new ArrayList<Solution>();
		this.addNeighborsSwapTasks(neighbors);
		this.addNeighborsChangeVehicle(neighbors);
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

	private void addNeighborsSwapTasks(ArrayList<Solution> neighbors) {
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

			// Remove random task
			while (iterator.hasNext()) {
				if (iterator.nextIndex() == toRemove) {
					removed = iterator.next();
					iterator.remove();
					break;
				} else {
					iterator.next();
				}
			}

			// Remove corresponding task
			iterator = wrappers.listIterator();
			while (iterator.hasNext()) {
				Wrapper next = iterator.next();
				if (next.task.equals(removed.task)) {
					if (next.isPickup()) {
						pickup = next;
						delivery = removed;
					} else {
						pickup = removed;
						delivery = next;
					}
					iterator.remove();
					break;
				}
			}

			this.addToNeighbors(neighbors, current, pickup, delivery, vehicle);
		}
	}

	/*
	 * Returns neighboring solutions by selecting a task and moving it to another
	 * vehicle.
	 */
	private void addNeighborsChangeVehicle(ArrayList<Solution> neighbors) {
		Vehicle removeVehicle = null;

		// Find random vehicle with enough tasks
		for (int i = 0; i < 10; i++) {
			removeVehicle = this.getRandomVehicle();
			if (this.nextTaskV.get(removeVehicle).size() >= 2) {
				Vehicle addVehicle = this.getRandomVehicle();
				if (removeVehicle.equals(addVehicle)) {
					return;
				}
				Solution current = this.clone(removeVehicle, addVehicle);
				Wrapper pickup = null;
				Wrapper delivery = null;

				// Delete the first wrapper from the random vehicle
				ListIterator<Wrapper> iterator = current.nextTaskV.get(removeVehicle).listIterator();
				pickup = iterator.next();
				iterator.remove();

				// Delete the corresponding delivery Wrapper
				while (iterator.hasNext()) {
					delivery = iterator.next();
					if (delivery.task.equals(pickup.task)) {
						iterator.remove();
						break;
					}
				}
				this.addToNeighbors(neighbors, current, pickup, delivery, addVehicle);
				break;
			}
		}
	}

	private void addToNeighbors(ArrayList<Solution> neighbors, Solution current, Wrapper pickup, Wrapper delivery,
			Vehicle addVehicle) {
		ArrayList<Wrapper> wrappers = new ArrayList<Wrapper>(current.nextTaskV.get(addVehicle));
		if (wrappers.size() == 0) {
			wrappers.add(pickup);
			wrappers.add(delivery);
			Solution sol = current.clone(addVehicle, wrappers);
			sol.updateCost();
			neighbors.add(sol);
		} else {
			// When we encounter a non feasible solution, we can break out of the inner loop
			// as all the other solutions won't be feasible either.
			for (int iB = wrappers.size() - 1; iB >= 0; iB--) {
				for (int iF = iB - 1; iF > 0; iF--) {
					wrappers.add(iF, pickup);
					wrappers.add(iB + 1, delivery);
					Solution newSolution = current.clone(addVehicle, wrappers);

					// Reset list
					wrappers.remove(iB + 1);
					wrappers.remove(iF);
					newSolution.updateCost();
					if (newSolution.isFeasible()) {
						neighbors.add(newSolution);
					} else {
						break;
					}
				}
			}
		}
	}

	public void updateCost() {
		this.cost = 0;
		for (Vehicle v : this.nextTaskV.keySet()) {
			City prevCity = v.homeCity();
			for (Wrapper w : this.nextTaskV.get(v)) {
				this.cost += prevCity.distanceTo(w.getCity()) * v.costPerKm();
				prevCity = w.getCity();
			}
		}
	}

	public boolean isFeasible() {
		for (Vehicle v : this.nextTaskV.keySet()) {
			double load = 0;
			for (Wrapper w : this.nextTaskV.get(v)) {
				if (w.isPickup()) {
					load += w.getTask().weight;
				} else {
					load -= w.getTask().weight;
				}
				if (load > v.capacity()) {
					return false;
				}
			}
		}
		return true;
	}

	public HashMap<Vehicle, LinkedList<Wrapper>> getPlans() {
		return this.nextTaskV;
	}

	public Solution clone(Vehicle random, Vehicle other) {
		HashMap<Vehicle, LinkedList<Wrapper>> nextTaskV = new HashMap<Vehicle, LinkedList<Wrapper>>(this.nextTaskV);
		nextTaskV.put(random, new LinkedList<Wrapper>(this.nextTaskV.get(random)));
		nextTaskV.put(other, new LinkedList<Wrapper>(this.nextTaskV.get(other)));
		return new Solution(this.vehicles, nextTaskV);
	}

	public Solution clone(Vehicle vehicle, LinkedList<Wrapper> wrappers) {
		HashMap<Vehicle, LinkedList<Wrapper>> nextTaskV = new HashMap<Vehicle, LinkedList<Wrapper>>(this.nextTaskV);
		nextTaskV.put(vehicle, new LinkedList<Wrapper>(wrappers));
		return new Solution(this.vehicles, nextTaskV);
	}

	public Solution clone(Vehicle vehicle, ArrayList<Wrapper> wrappers) {
		HashMap<Vehicle, LinkedList<Wrapper>> nextTaskV = new HashMap<Vehicle, LinkedList<Wrapper>>(this.nextTaskV);
		nextTaskV.put(vehicle, new LinkedList<Wrapper>(wrappers));
		return new Solution(this.vehicles, nextTaskV);
	}

	@Override
	public int compareTo(Solution o) {
		return (int) (this.cost - o.cost);
	}

	public double getCost() {
		return this.cost;
	}

	public String toString() {
		String sString = new String();
		for (Vehicle v : this.nextTaskV.keySet()) {
			String vString = "Vehicle" + v.id() + ", origin: " + v.homeCity().toString() + ", capacity: " + v.capacity()
					+ ", costPerKm: " + v.costPerKm() + ", tasks: " + this.nextTaskV.get(v).size() + "\n";
			for (Wrapper w : this.nextTaskV.get(v)) {
				vString = vString + w.toString();
			}
			sString = sString + vString;
		}
		return sString;
	}
}
