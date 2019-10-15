import java.util.ArrayList;
import logist.plan.Plan;

public class BFS {
	public BFS() {
		
	}
	
	public Plan computePlan(State initState) {
		
		ArrayList<State> Q = new ArrayList<State>();
		ArrayList<State> exploredStates = new ArrayList<State>();
		State bestGoalState = null;
		
		Q.add(initState);
		
		while (!Q.isEmpty()) {
			State n = Q.remove(0);
			if (n.getIsGoal()) {
				if ((bestGoalState == null) || (n.getCost() < bestGoalState.getCost())) {
					bestGoalState = n;
				}
			}
			else {
				ArrayList<State> childNodes = n.getChildren();
				boolean newState = true;
				State sameState = null;
				for (State s: exploredStates) {
					if ((s.equals(n)) & (s.getCost() > n.getCost())) {
//						System.out.println("IF");
						newState = false;
						sameState = s;
						break;
					}
				}
				if (!newState) {
					exploredStates.remove(sameState);
					exploredStates.add(n);
				}
				else {
					
					exploredStates.add(n);
				}
				Q.addAll(childNodes);
			}		
		}
		return bestGoalState.getPlan();	
	}
}
