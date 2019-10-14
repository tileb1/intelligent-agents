import java.util.ArrayList;

import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;

public class State {

	private City city;
	private TaskSet availableTasks;
	private TaskSet loadedTasks;
	private State parent;
	private double cost;
	private double capacityLeft;
	
	private boolean isGoal = false;
	private Vehicle vehicle;

	public State(City city, TaskSet availableTasks, TaskSet loadedTasks, Vehicle vehicle, State parent) {
		this.city = city;
		this.availableTasks = availableTasks;
		this.loadedTasks = loadedTasks;
		this.parent = parent;
		this.vehicle = vehicle;

		if (parent != null) {
			this.cost = this.parent.getCost() + this.parent.getCity().distanceTo(this.city) * vehicle.costPerKm();
		} else {
			this.cost = 0;
		}
		
		if (this.loadedTasks.size() == 0 & this.availableTasks.size() == 0) {
			this.isGoal = true;
		}
		
		// We could precompute this to make it faster but this is more user friendly
		this.capacityLeft = vehicle.capacity();
		for (Task task : this.loadedTasks) {
			this.capacityLeft -= task.weight;
		}
	}

	// -------------------- GETTERS -------------------------
	public double getCost() {
		return this.cost;
	}

	public TaskSet getAvailableTasks() {
		return this.availableTasks;
	}

	public TaskSet getLoadedTasks() {
		return this.loadedTasks;
	}

	public City getCity() {
		return this.city;
	}

	public boolean getIsGoal() {
		return this.isGoal;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		State other = (State) o;
		return other.getLoadedTasks().equals(this.loadedTasks) & other.getAvailableTasks().equals(this.availableTasks)
				& this.city.equals(other.getCity());
	}

	/*
	 * Get children of the state
	 */
	public ArrayList<State> getChildren() {
		ArrayList<State> children = new ArrayList<State>();
		
		// Go to city of available task
		for (Task task : this.availableTasks) {
			
			// Check that we can take the task in the truck
			if (this.capacityLeft >= task.weight) {
				TaskSet newAvailableTasks = this.availableTasks.clone();
				newAvailableTasks.remove(task);
				children.add(new State(task.deliveryCity, newAvailableTasks, this.loadedTasks, this.vehicle, this));
			}
		}
		
		// Deliver a task in the loaded tasks
		for (Task task : this.loadedTasks) {
			
		}
		return null;
	}
}