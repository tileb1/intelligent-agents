import java.util.ArrayList;
import java.util.Objects;

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
	private double costParent;
	private double costDistance;
	private int capacityLeft;
	
	private Vehicle vehicle;

	public State(City city, TaskSet availableTasks, TaskSet loadedTasks, State parent, Vehicle vehicle, int capacityLeft) {
		this.city = city;
		this.availableTasks = availableTasks;
		this.loadedTasks = loadedTasks;
		this.parent = parent;
		this.vehicle = vehicle;

		if (parent != null) {
			this.costParent = this.parent.getCost();
			this.costDistance = this.parent.getCity().distanceTo(this.city) * vehicle.costPerKm();
		} else {
			this.costDistance = 0;
			this.costParent = 0;
		}
//		this.cost = 0;
		
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
		if (this.getParent() == null) {
			return 0;
		}
		return this.costDistance + this.parent.getCost();
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
	
	public State getParent() {
		return this.parent;
	}

	public boolean getIsGoal() {
		return this.loadedTasks.size() == 0 & this.availableTasks.size() == 0;
	}

//	@Override
//	public boolean equals(Object o) {
//		if (this == o) {
//			return true;
//		}
//		if (o == null || getClass() != o.getClass()) {
//			return false;
//		}
//		State other = (State) o;
//		return other.getLoadedTasks().equals(this.loadedTasks) & other.getAvailableTasks().equals(this.availableTasks)
//				& this.city.equals(other.getCity());
//	}
	
//	@Override
//	public int hashCode() {
//		return Objects.hash(this.availableTasks, this.loadedTasks, this.city);
//	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((availableTasks == null) ? 0 : availableTasks.hashCode());
		result = prime * result + capacityLeft;
		result = prime * result + ((city == null) ? 0 : city.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		State other = (State) obj;
		if (availableTasks == null) {
			if (other.availableTasks != null)
				return false;
		} else if (!availableTasks.equals(other.availableTasks))
			return false;
		if (capacityLeft != other.capacityLeft)
			return false;
		if (city == null) {
			if (other.city != null)
				return false;
		} else if (!city.equals(other.city))
			return false;
		return true;
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
				children.add(new State(task.pickupCity, newAvailableTasks, newLoadedTasks, this, this.vehicle, this.capacityLeft - task.weight));
			}
		}
		
		// Deliver a task in the loaded tasks
		for (Task task : this.loadedTasks) {
			newLoadedTasks = this.loadedTasks.clone();
			newLoadedTasks.remove(task);
			children.add(new State(task.deliveryCity, this.availableTasks, newLoadedTasks, this, this.vehicle, this.capacityLeft + task.weight));
		}
		return children;
	}
	
//	public void setParentCost(double cost) {
//		this.costParent = cost;
//	}
	
	public void setParent(State state) {
		this.parent = state;
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
				System.out.println(city);
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
				System.out.println(city);
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