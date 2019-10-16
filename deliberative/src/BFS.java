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
		return plan;
	}

//	public Plan getPlan(State state) {
//		State realState = this.exploredStates.get(state);
//		if (realState.getParent() == null) {
//			return new Plan(realState.getCity());
//		}
//		Plan currentPlan = this.getPlan(realState.getParent());
//
//		TaskSet oneElemMaxLoaded;
//		TaskSet oneElemMaxAvailable = realState.getParent().getAvailableTasks().clone();
//		oneElemMaxAvailable.removeAll(realState.getAvailableTasks());
//
//		// The current city is a deliver city for the parent node
//		if (oneElemMaxAvailable.size() == 0) {
//			for (City city : realState.getParent().getCity().pathTo(realState.getCity())) {
//				currentPlan.appendMove(city);
//			}
//			// If the current city is a deliver city, then, the parent node has 1 extra
//			// loaded task than the current node
//			oneElemMaxLoaded = realState.getParent().getLoadedTasks().clone();
//			oneElemMaxLoaded.removeAll(realState.getLoadedTasks());
//			for (Task onlyTask : oneElemMaxLoaded) {
//				currentPlan.appendDelivery(onlyTask);
//			}
//		}
//
//		// The current city is a pickup city for the parent node
//		else {
//			for (City city : realState.getParent().getCity().pathTo(realState.getCity())) {
//				currentPlan.appendMove(city);
//			}
//			// If the current city is a pickup city, then, the current node has 1 extra
//			// loaded task than the parent
//			oneElemMaxLoaded = realState.getLoadedTasks().clone();
//			oneElemMaxLoaded.removeAll(realState.getParent().getLoadedTasks());
//			for (Task onlyTask : oneElemMaxLoaded) {
//				currentPlan.appendPickup(onlyTask);
//			}
//		}
////		System.out.println(System.nanoTime() - timeBegin);
//		return currentPlan;
//	}
}
