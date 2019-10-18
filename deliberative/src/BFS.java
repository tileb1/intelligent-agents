import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import logist.plan.Plan;

public class BFS {
	HashMap<State, State> exploredStates;

	public Plan computePlan(State initState) {
		this.exploredStates = new HashMap<State, State>();
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
		return bestGoalState.getPlan();
	}
}
