import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;

public class BFS {
	public BFS() {

	}

	HashMap<State, State> exploredStates = new HashMap<State, State>();

	public Plan computePlan(State initState) {
		final long startTime = System.nanoTime();

		LinkedList<State> Q = new LinkedList<State>();
		State bestGoalState = null;

		Q.add(initState);

		while (!Q.isEmpty()) {
			State n = Q.poll();
			
			if (this.exploredStates.containsKey(n.getParent())) {
				n.setParent(this.exploredStates.get(n.getParent()));
			}

			if (!this.exploredStates.containsKey(n)) {
				this.exploredStates.put(n, n);
				Q.addAll(n.getChildren());

			} else {
				if (this.exploredStates.get(n).getCost() > n.getCost()) {
					this.exploredStates.put(n, n);
				}
			}
			if (n.getIsGoal()) {
				if ((bestGoalState == null) || (n.getCost() < bestGoalState.getCost())) {
					bestGoalState = n;
				}
			}
		}

		final long timeBegin = System.nanoTime();
		Plan plan = bestGoalState.getPlan();
		System.out.println(TimeUnit.MILLISECONDS.convert(timeBegin - startTime, TimeUnit.NANOSECONDS));
		System.out.println(TimeUnit.MILLISECONDS.convert(System.nanoTime() - timeBegin, TimeUnit.NANOSECONDS));
		
		
		
		System.out.println("-----------------------------------------------------------------");
		State someState = bestGoalState;
		while (someState != null) {
			System.out.println(someState.getCity() + " " + someState);
			someState = someState.getParent();
		}
		return plan;
	}
}
