
/* import table */
import logist.simulation.Vehicle;

import java.util.concurrent.TimeUnit;

import logist.agent.Agent;
import logist.behavior.DeliberativeBehavior;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

/**
 * An optimal planner for one vehicle.
 */
@SuppressWarnings("unused")
public class DeliberativeAgent implements DeliberativeBehavior {

	enum Algorithm {
		BFS, ASTAR
	}

	/* Environment */
	Topology topology;
	TaskDistribution td;

	/* the properties of the agent */
	Agent agent;
	int capacity;
	BFS bfs = new BFS();
	ASTAR astar = new ASTAR();
	TaskSet loadedTasks;

	/* the planning class */
	Algorithm algorithm;

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {
		this.topology = topology;
		this.td = td;
		this.agent = agent;

		// initialize the planner
		int capacity = agent.vehicles().get(0).capacity();
		String algorithmName = agent.readProperty("algorithm", String.class, "ASTAR");

		// Throws IllegalArgumentException if algorithm is unknown
		algorithm = Algorithm.valueOf(algorithmName.toUpperCase());
	}

	@Override
	public Plan plan(Vehicle vehicle, TaskSet tasks) {
		Plan plan;
		// Compute the plan with the selected algorithm.
		StringBuilder out = new StringBuilder();
		final long timeBegin = System.nanoTime();
		switch (algorithm) {
		case ASTAR:
			out.append("ASTAR ");
			plan = this.astar.computePlan(new State(vehicle.getCurrentCity(), tasks, vehicle.getCurrentTasks(), null,
					vehicle, vehicle.capacity(), true));
			break;
		case BFS:
			out.append("BFS ");
			plan = this.bfs.computePlan(new State(vehicle.getCurrentCity(), tasks, vehicle.getCurrentTasks(), null,
					vehicle, vehicle.capacity(), false));
			break;
		default:
			throw new AssertionError("Should not happen.");
		}
		final long durationTime = System.nanoTime() - timeBegin;
		out.append("ran in ");
		out.append(Double.toString(TimeUnit.MILLISECONDS.convert(durationTime, TimeUnit.NANOSECONDS)/1000.0));
		out.append(" seconds with ");
		out.append(tasks.size());
		out.append(" tasks and a total distance of ");
		out.append(plan.totalDistance());
		System.out.println(out.toString());
		return plan;
	}

	@Override
	public void planCancelled(TaskSet carriedTasks) {
		// We get the carried tasks from the vehicle right away...
	}
}
