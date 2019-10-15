import java.util.ArrayList;

import logist.plan.Plan;
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
	private int capacityLeft;
	
//	private Vehicle vehicle;

	public State(City city, TaskSet availableTasks, TaskSet loadedTasks, State parent, int capacityLeft) {
		this.city = city;
		this.availableTasks = availableTasks;
		this.loadedTasks = loadedTasks;
		this.parent = parent;
//		this.vehicle = vehicle;

//		if (parent != null) {
//			this.cost = this.parent.getCost() + this.parent.getCity().distanceTo(this.city) * vehicle.costPerKm();
//		} else {
//			this.cost = 0;
//		}
		this.cost = 0;
		
		// We could precompute this to make it faster but this is more user friendly
		this.capacityLeft = capacityLeft;//vehicle.capacity();
//		for (Task task : this.loadedTasks) {
//			this.capacityLeft -= task.weight;
//		}
	}
	
	@Override
	public String toString() {
		return Integer.toString(this.availableTasks.size());
		
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
		return this.loadedTasks.size() == 0 & this.availableTasks.size() == 0;
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
	 * Gets the children node of the current state
	 */
	public ArrayList<State> getChildren() {
		ArrayList<State> children = new ArrayList<State>();
		TaskSet newAvailableTasks;
		TaskSet newLoadedTasks;
		
		// Go to city of available task
		for (Task task : this.availableTasks) {
			
			// Check that we can take the task in the truck
			if (this.capacityLeft >= task.weight) {
				newAvailableTasks = this.availableTasks.clone();
				newAvailableTasks.remove(task);
				newLoadedTasks = this.loadedTasks.clone();
				newLoadedTasks.add(task);
				children.add(new State(task.pickupCity, newAvailableTasks, newLoadedTasks, this, this.capacityLeft - task.weight));
			}
		}
		
		// Deliver a task in the loaded tasks
		for (Task task : this.loadedTasks) {
			newLoadedTasks = this.loadedTasks.clone();
			newLoadedTasks.remove(task);
			children.add(new State(task.deliveryCity, this.availableTasks, newLoadedTasks, this, this.capacityLeft + task.weight));
		}
		return children;
	}
	
	/*
	 * Returns the plan from the initial state to the current state
	 */
	public Plan getPlan() {
//		long timeBegin = System.nanoTime();
		if (this.parent == null) {
			return new Plan(this.city);
		}
		// Recursive call to parent getPlan() method
		Plan currentPlan = this.parent.getPlan();
		
		TaskSet oneElemMaxLoaded;
		TaskSet oneElemMaxAvailable = this.parent.getAvailableTasks().clone();
		oneElemMaxAvailable.removeAll(this.availableTasks);
		
		// The current city is a deliver city for the parent node
		if (oneElemMaxAvailable.size() == 0) {
			for (City city : this.parent.getCity().pathTo(this.city)) {
				currentPlan.appendMove(city);
			}
			// If the current city is a deliver city, then, the parent node has 1 extra loaded task than the current node
			oneElemMaxLoaded = this.parent.getLoadedTasks().clone();
			oneElemMaxLoaded.removeAll(this.loadedTasks);
			for (Task onlyTask : oneElemMaxLoaded) {
				currentPlan.appendDelivery(onlyTask);
			}
		}
		
		// The current city is a pickup city for the parent node
		else {
			for (City city : this.parent.getCity().pathTo(this.city)) {
				currentPlan.appendMove(city);
			}
			// If the current city is a pickup city, then, the current node has 1 extra loaded task than the parent
			oneElemMaxLoaded = this.loadedTasks.clone();
			oneElemMaxLoaded.removeAll(this.parent.getLoadedTasks());
			for (Task onlyTask : oneElemMaxLoaded) {
				currentPlan.appendPickup(onlyTask);
			}
		}
//		System.out.println(System.nanoTime() - timeBegin);
		return currentPlan;
	}
}