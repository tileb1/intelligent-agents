import java.util.HashMap;
import java.util.PriorityQueue;

import logist.plan.Plan;

public class ASTAR {

	HashMap<State, State> exploredStates; // Note that we could use a hashset if we are sure the heuristic function is good

	public Plan computePlan(State initState) {
		// Holds the states sorted according to our heuristic function
		PriorityQueue<State> Q = new PriorityQueue<State>();
		this.exploredStates = new HashMap<State, State>();
		Q.add(initState);
		while (!Q.isEmpty()) {
			State n = Q.poll();

			if (n.getIsGoal()) {
				return n.getPlan();
			}

			// Add children and update set of explored states if never seen before
			if (!this.exploredStates.containsKey(n)) {
				this.exploredStates.put(n, n);
				Q.addAll(n.getChildren());
			}
			// ------------- This if statement won't get executed if the heuristic is well chosen -------------
			else {
				if (this.exploredStates.get(n).getCost() > n.getCost()) {
					Q.addAll(n.getChildren());
				}
			}
			// ------------------------------------------------------------------------------------------------
		}
		return null;
	}
}
