import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.Collections;

import logist.plan.Plan;
import logist.task.Task;

public class ASTAR {

	public ASTAR() {

	}
	
	HashMap<State, State> exploredStates = new HashMap<State, State>();

	public Plan computePlan(State initState) {
		final long startTime = System.nanoTime();

		LinkedList<State> Q = new LinkedList<State>();

		Q.add(initState);

		while (!Q.isEmpty()) {
			State n = Q.poll();
			
			if (n.getIsGoal()) {
				final long timeBegin = System.nanoTime();
				System.out.println(TimeUnit.MILLISECONDS.convert(timeBegin - startTime, TimeUnit.NANOSECONDS));
				System.out.println(TimeUnit.MILLISECONDS.convert(System.nanoTime() - timeBegin, TimeUnit.NANOSECONDS));
				System.out.println("-----------------------------------------------------------------");
				State someState = n;
				
				for (State s: Q) {
					System.out.println(s.getHeuristic());
				}
				
				while (someState != null) {
					System.out.println(someState.getCity() + " " + someState);
					someState = someState.getParent();
				}
				return n.getPlan();
			}
		
//			if (this.exploredStates.containsKey(n.getParent())) {
//				n.setParent(this.exploredStates.get(n.getParent()));
//			}

			if (!this.exploredStates.containsKey(n)) {
				this.exploredStates.put(n, n);
				Q.addAll(n.getChildren());
				Collections.sort(Q);
			} else {
				if (this.exploredStates.get(n).getCost() > n.getCost()) {
					this.exploredStates.put(n, n);
					Q.addAll(n.getChildren());
					Collections.sort(Q);
				}
			}
		}
		return null;
	}
}
