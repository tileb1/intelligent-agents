import java.util.ArrayList;
import java.util.Objects;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;

public class State implements Comparable<State> {

	private City city;
	private TaskSet availableTasks;
	private TaskSet loadedTasks;
	private State parent;
	private double costDistance;
	private int capacityLeft;
	private double heuristic;

	private boolean calcH = false;
	private Vehicle vehicle;

	public State(City city, TaskSet availableTasks, TaskSet loadedTasks, State parent, Vehicle vehicle,
			int capacityLeft, boolean calcH) {
		this.setupState(city, availableTasks, loadedTasks, parent, vehicle, capacityLeft);
		this.setHeuristic(calcH);
	}

	@Override
	public String toString() {
		return "Some state at " + this.city.toString();

	}

	public void setupState(City city, TaskSet availableTasks, TaskSet loadedTasks, State parent, Vehicle vehicle,
			int capacityLeft) {
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
		this.capacityLeft = capacityLeft;
	}

	// -------------------- GETTERS and SETTERS -------------------------
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

	public double getHeuristic() {
		return this.heuristic;
	}

	public void setParent(State state) {
		this.parent = state;
	}

	/*
	 * Gets the children node of the current state if any
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
				children.add(new State(task.pickupCity, newAvailableTasks, newLoadedTasks, this, this.vehicle,
						this.capacityLeft - task.weight, this.calcH));
			}
		}

		// Deliver a task in the loaded tasks
		for (Task task : this.loadedTasks) {
			newLoadedTasks = this.loadedTasks.clone();
			newLoadedTasks.remove(task);
			children.add(new State(task.deliveryCity, this.availableTasks, newLoadedTasks, this, this.vehicle,
					this.capacityLeft + task.weight, this.calcH));
		}
		return children;
	}

	private void setHeuristic(boolean calcH) {
		this.calcH = calcH;
		double tmp = 0;

		// Get max cost of picking-up task and delivering it
		for (Task task : availableTasks) {
			int distance = (int) (this.city.distanceTo(task.pickupCity)
					+ task.pickupCity.distanceTo(task.deliveryCity));
			tmp = Math.max(distance, tmp);
		}

		// Get max cost of delivering task in vehicle
		for (Task task : loadedTasks) {
			int distance = (int) (this.city.distanceTo(task.deliveryCity));
			tmp = Math.max(distance, tmp);
		}

		this.heuristic = tmp * this.vehicle.costPerKm() + this.getCost();
	}

	/*
	 * Returns the plan from the initial state to the current state
	 */
	public Plan getPlan() {
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
			// If the current city is a deliver city, then, the parent node has 1 extra
			// loaded task than the current node
			oneElemMaxLoaded = this.parent.getLoadedTasks().clone();
			oneElemMaxLoaded.removeAll(this.loadedTasks);
			for (Task onlyTask : oneElemMaxLoaded) {
				currentPlan.appendDelivery(onlyTask);
			}
		}

		// The current city is a pickup city for the parent node
		else {
			assert oneElemMaxAvailable.size() == 1;
			for (City city : this.parent.getCity().pathTo(this.city)) {
				currentPlan.appendMove(city);
			}
			// If the current city is a pickup city, then, the current node has 1 extra
			// loaded task than the parent
			oneElemMaxLoaded = this.loadedTasks.clone();
			oneElemMaxLoaded.removeAll(this.parent.getLoadedTasks());
			for (Task onlyTask : oneElemMaxLoaded) {
				currentPlan.appendPickup(onlyTask);
			}
		}
		return currentPlan;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.availableTasks, this.loadedTasks, this.city);
	}

	@Override // Auto-generated by eclipse
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

	@Override
	public int compareTo(State otherState) {
		return (int) (this.heuristic - otherState.heuristic);
	}
}