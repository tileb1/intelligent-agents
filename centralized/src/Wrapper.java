import logist.task.Task;
import logist.topology.Topology.City;

public class Wrapper {
//	private Wrapper prev;
//	private Wrapper next;
//	private int time; // removed because we won't generate wrong solutions
	public Task task;
	public boolean pickup;
	public double capacity;
	
	public Wrapper(Task tsk, boolean pckp, double cpct) {
		this.task = tsk;
		this.pickup = pckp;
		this.capacity = cpct;
	}
	
	public Task getTask() {
		return this.task;
	}
	
	public City getCity() {
		if (pickup) {
			return this.task.pickupCity;
		}
		else {
			return this.task.deliveryCity;
		}
	}
	
	public boolean isPickup() {
		return this.pickup;
	}
	
	public String toString() {
		if (pickup) {
			return "- Pickup Task " + this.task.id + " at " + this.task.pickupCity.toString() + ", to be delivred to " + this.task.deliveryCity.toString() + " \n";
		}
		else {
			return "- Deliver Task " + this.task.id + " to " + this.task.deliveryCity.toString() + " \n";
		}
		
	}
}
