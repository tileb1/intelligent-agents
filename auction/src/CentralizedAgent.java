import java.io.File;
//the list of imports
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import logist.LogistSettings;

import logist.Measures;
import logist.behavior.AuctionBehavior;
import logist.behavior.CentralizedBehavior;
import logist.agent.Agent;
import logist.config.Parsers;
import logist.simulation.Vehicle;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

/**
 * A very simple auction agent that assigns all tasks to its first vehicle and
 * handles them sequentially.
 *
 */
@SuppressWarnings("unused")
public class CentralizedAgent {

    private Topology topology;
    private TaskDistribution distribution;
    private Agent agent;
    private long timeout_plan_per_round;
    
    public CentralizedAgent(Topology topology, TaskDistribution distribution, Agent agent, long timeout_plan_per_round) {
        this.topology = topology;
        this.distribution = distribution;
        this.agent = agent;
        this.timeout_plan_per_round = timeout_plan_per_round;
    }
    
    
    public Solution getSolution(Task task, Solution currentSol) {
        long time_start = System.currentTimeMillis();
        int iter = 0;
        
        ArrayList<Solution> sols = currentSol.getNeighbors(task);
        if (sols.size() == 0) {
        	return null;
        }
        currentSol = Collections.min(sols);
        Solution bestSolEver = currentSol;
        
        // We add a second safety delay
    	while (System.currentTimeMillis() - time_start + 500 < this.timeout_plan_per_round) {
    		sols = currentSol.getNeighbors();
			if (sols.size() > 0) {
				Solution minSol = Collections.min(sols);
				if (minSol.getCost() < currentSol.getCost()) {
					currentSol = minSol;
					if (bestSolEver.getCost() > minSol.getCost()) {
						bestSolEver = minSol;
					}
				}
				// Do some exploration
				if (Math.random() < 0.03) {
					currentSol = sols.get(Solution.random.nextInt(sols.size()));
				}
				// Reset to last local minimum very rarely
				// This can be usefull when the exploration leads us nowhere...
				if (Math.random() < 0.0005) {
					currentSol = bestSolEver;
				}
			}
//			if (iter % 1000 == 0) {
//				System.out.println("Iteration number: " + iter + " with cost " + currentSol.getCost() + " and best cost " + bestSolEver.getCost());
//			}
    		iter++;
    	}
    	
        
    	// Print computation time
        long time_end = System.currentTimeMillis();
        long duration = time_end - time_start;
//        System.out.println("The solution was generated in " + duration + " milliseconds.");
//        System.out.print(currentSol.toString());
        
        return bestSolEver;
    }
    
    
    public Solution getSolution(TaskSet tasks, List<Vehicle> veh) {
        long time_start = System.currentTimeMillis();
        int iter = 0;
        
        Solution currentSol = new Solution(veh, topology, tasks);
        ArrayList<Solution> sols = currentSol.getNeighbors();
        if (sols.size() == 0) {
        	return currentSol;
        }
        currentSol = Collections.min(sols);
        Solution bestSolEver = currentSol;
        
        // We add a second safety delay
    	while (System.currentTimeMillis() - time_start + 500 < this.timeout_plan_per_round) {
    		sols = currentSol.getNeighbors();
			if (sols.size() > 0) {
				Solution minSol = Collections.min(sols);
				if (minSol.getCost() < currentSol.getCost()) {
					currentSol = minSol;
					if (bestSolEver.getCost() > minSol.getCost()) {
						bestSolEver = minSol;
					}
				}
				// Do some exploration
				if (Math.random() < 0.03) {
					currentSol = sols.get(Solution.random.nextInt(sols.size()));
				}
				// Reset to last local minimum very rarely
				// This can be usefull when the exploration leads us nowhere...
				if (Math.random() < 0.0005) {
					currentSol = bestSolEver;
				}
			}
//			if (iter % 1000 == 0) {
//				System.out.println("Iteration number: " + iter + " with cost " + currentSol.getCost() + " and best cost " + bestSolEver.getCost());
//			}
    		iter++;
    	}
    	
        
    	// Print computation time
        long time_end = System.currentTimeMillis();
        long duration = time_end - time_start;
//        System.out.println("The solution was generated in " + duration + " milliseconds.");
//        System.out.print(currentSol.toString());
        
        return bestSolEver;
    }
    
    
    
    public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
        long time_start = System.currentTimeMillis();
        Solution currentSol = new Solution(vehicles, topology, tasks);
        
        int iter = 0;
        Solution bestSolEver = currentSol;
        
        // We add a second safety delay
    	while (System.currentTimeMillis() - time_start + 1000 < 1200) {
    		ArrayList<Solution> sols = currentSol.getNeighbors();
			if (sols.size() > 0) {
				Solution minSol = Collections.min(sols);
				if (minSol.getCost() < currentSol.getCost()) {
					currentSol = minSol;
					if (bestSolEver.getCost() > minSol.getCost()) {
						bestSolEver = minSol;
					}
				}
				// Do some exploration
				if (Math.random() < 0.03) {
					currentSol = sols.get(Solution.random.nextInt(sols.size()));
				}
				// Reset to last local minimum very rarely
				// This can be usefull when the exploration leads us nowhere...
				if (Math.random() < 0.0005) {
					currentSol = bestSolEver;
				}
			}

    		iter++;
    	}
    	
    	// Generate plan
    	List<Plan> plans = new ArrayList<Plan>();
    	for (Vehicle v: vehicles) {
    		Plan plan = new Plan(v.homeCity());
    		City fromCity = v.homeCity();
    		for (Wrapper w: bestSolEver.getPlans().get(v)) {
    			for (City c: fromCity.pathTo(w.getCity())) {
					plan.appendMove(c);
				}
    			fromCity = w.getCity();
    			if (w.isPickup()) {
    				plan.appendPickup(w.getTask());
    			}
    			else {
    				plan.appendDelivery(w.getTask());
    			}
    		}
    		plans.add(plan);
    	}
        
        return plans;
    }
    
    public void setTimeout(long time) {
    	this.timeout_plan_per_round = time;
    }

}
