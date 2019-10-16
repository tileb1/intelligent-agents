import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import logist.plan.Plan;

public class BFS {
	public BFS() {
		
	}
	
	public Plan computePlan(State initState) {
		final long startTime = System.nanoTime();
		
		LinkedList<State> Q = new LinkedList<State>();
		HashMap<State, State> exploredStates = new HashMap<State, State>();
		State bestGoalState = null;
		
		Q.add(initState);
		
		while (!Q.isEmpty()) {
			State n = Q.poll();
			if (n.getIsGoal()) {
				if ((bestGoalState == null) || (n.getCost() < bestGoalState.getCost())) {
					bestGoalState = n;
				}
			}
			else if (exploredStates.containsKey(n)) {
				
				if (exploredStates.get(n).getCost() > n.getCost()) {

				}
				
			}
			else {
				ArrayList<State> childNodes = n.getChildren();
//				boolean newState = true;
//				State sameState = null;
//				for (State s: exploredStates) {
//					if ((s.equals(n)) & (s.getCost() > n.getCost())) {
////						System.out.println("IF");
//						newState = false;
//						sameState = s;
//						break;
//					}
//				}
//				if (!newState) {
//					exploredStates.remove(sameState);
//					exploredStates.add(n);
//				}
//				else {
//					
//					exploredStates.add(n);
//				}
				Q.addAll(childNodes);
			}		
		}
		final long timeBegin = System.nanoTime();
		Plan plan = bestGoalState.getPlan();
		System.out.println(TimeUnit.MILLISECONDS.convert(timeBegin - startTime, TimeUnit.NANOSECONDS));
		System.out.println(TimeUnit.MILLISECONDS.convert(System.nanoTime() - timeBegin, TimeUnit.NANOSECONDS));
		return plan;	
	}
}
