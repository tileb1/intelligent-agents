import java.util.ArrayList;
import java.util.HashSet;
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
	private double costDistance;
	private int capacityLeft;
	
	private Vehicle vehicle;
	private HashSet<Integer> hashSetAvailableID = new HashSet<Integer>();
	private HashSet<Integer> hashSetLoadedID = new HashSet<Integer>();

	public State(City city, TaskSet availableTasks, TaskSet loadedTasks, State parent, Vehicle vehicle, int capacityLeft) {
		this.city = city;
		this.availableTasks = availableTasks;
		this.loadedTasks = loadedTasks;
		this.parent = parent;
		this.vehicle = vehicle;

		if (parent != null) {
			this.costDistance = this.parent.getCity().distanceTo(this.city) * vehicle.costPerKm();
		} else {
			this.costDistance = 0;
		}
		
		for (Task task : this.availableTasks) {
			hashSetAvailableID.add(task.id);
		}
		
		for (Task task : this.loadedTasks) {
			hashSetLoadedID.add(task.id);
		}
		
		// We could precompute this to make it faster but this is more user friendly
		this.capacityLeft = capacityLeft;//vehicle.capacity();
//		for (Task task : this.loadedTasks) {
//			this.capacityLeft -= task.weight;
//		}
	}
	
	@Override
	public String toString() {
		return "available: " + this.hashSetAvailableID.toString() + ", loaded: " + this.hashSetLoadedID.toString();
		
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
//				assert task.pickupCity != this.city;
				children.add(new State(task.pickupCity, newAvailableTasks, newLoadedTasks, this, this.vehicle, this.capacityLeft - task.weight));
			}
		}
		
		// Deliver a task in the loaded tasks
		for (Task task : this.loadedTasks) {
			newLoadedTasks = this.loadedTasks.clone();
			newLoadedTasks.remove(task);
//			assert task.deliveryCity != this.city;
			children.add(new State(task.deliveryCity, this.availableTasks, newLoadedTasks, this, this.vehicle, this.capacityLeft + task.weight));
		}
		return children;
	}
	
	
	public void setParent(State state) {
		this.parent = state;
	}
	
	
	@Override
	public int hashCode() {
		return Objects.hash(this.availableTasks, this.loadedTasks, this.city);
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
		if (city == null) {
			if (other.city != null)
				return false;
		} else if (!city.equals(other.city))
			return false;
		if (loadedTasks == null) {
			if (other.loadedTasks != null)
				return false;
		} else if (!loadedTasks.equals(other.loadedTasks))
			return false;
		return true;
	}

	/*
	 * Returns the plan from the initial state to the current state
	 */
	public Plan getPlan() {
//		long timeBegin = System.nanoTime();
		if (this.parent == null) {
			return new Plan(this.city);
		}
//		System.out.println(this.city.toString() + " - " + this.parent.getCity().toString());
		// Recursive call to parent getPlan() method
		Plan currentPlan = this.parent.getPlan();
		
		TaskSet oneElemMaxLoaded;
		TaskSet oneElemMaxAvailable = this.parent.getAvailableTasks().clone();
		oneElemMaxAvailable.removeAll(this.availableTasks);
		
		// The current city is a deliver city for the parent node
		if (oneElemMaxAvailable.size() == 0) {
			for (City city : this.parent.getCity().pathTo(this.city)) {
				currentPlan.appendMove(city);
//				System.out.println(city);
			}
//			int index = 0;
			// If the current city is a deliver city, then, the parent node has 1 extra loaded task than the current node
			oneElemMaxLoaded = this.parent.getLoadedTasks().clone();
			oneElemMaxLoaded.removeAll(this.loadedTasks);
			for (Task onlyTask : oneElemMaxLoaded) {
//				System.out.println("Deliver: " + onlyTask);
				currentPlan.appendDelivery(onlyTask);
//				System.out.println(index);
//				index++;
			}
		}
		
		// The current city is a pickup city for the parent node
		else {
			assert oneElemMaxAvailable.size() == 1;
			for (City city : this.parent.getCity().pathTo(this.city)) {
				currentPlan.appendMove(city);
//				System.out.println(city);
			}
			// If the current city is a pickup city, then, the current node has 1 extra loaded task than the parent
			oneElemMaxLoaded = this.loadedTasks.clone();
			oneElemMaxLoaded.removeAll(this.parent.getLoadedTasks());
//			int index = 0;
			for (Task onlyTask : oneElemMaxLoaded) {
//				System.out.println("Pickup: " + onlyTask);
				currentPlan.appendPickup(onlyTask);
//				System.out.println(index);
//				index++;
			}
		}
//		System.out.println(System.nanoTime() - timeBegin);
		return currentPlan;
	}
}