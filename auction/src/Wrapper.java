import logist.task.Task;
import logist.topology.Topology.City;

/*
 * Wrapper class for a task. We generate 2 wrappers for a single task, one for the pickup and one for the delivery.
 */
public class Wrapper {
	private Task task;
	private boolean pickup;
	
	public Wrapper(Task tsk, boolean pckp) {
		this.task = tsk;
		this.pickup = pckp;
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
