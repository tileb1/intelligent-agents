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
public class CentralizedAgent implements CentralizedBehavior {

    private Topology topology;
    private TaskDistribution distribution;
    private Agent agent;
    private long timeout_setup;
    private long timeout_plan;
    
    @Override
    public void setup(Topology topology, TaskDistribution distribution,
            Agent agent) {
        
        // this code is used to get the timeouts
        LogistSettings ls = null;
        try {
            ls = Parsers.parseSettings("config" + File.separator + "settings_default.xml");
        }
        catch (Exception exc) {
            System.out.println("There was a problem loading the configuration file.");
        }
        
        // the setup method cannot last more than timeout_setup milliseconds
        // Not needed as our setup is trivial
        timeout_setup = ls.get(LogistSettings.TimeoutKey.SETUP);
        // the plan method cannot execute more than timeout_plan milliseconds
        timeout_plan = ls.get(LogistSettings.TimeoutKey.PLAN);
        
        this.topology = topology;
        this.distribution = distribution;
        this.agent = agent;
    }

    @Override
    public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
        long time_start = System.currentTimeMillis();
        Solution currentSol = new Solution(vehicles, topology, tasks);
        
        int iter = 0;
        Solution bestSolEver = currentSol;
        
        // We add a second safety delay
    	while (System.currentTimeMillis() - time_start + 1000 < this.timeout_plan) {
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
			if (iter % 1000 == 0) {
				System.out.println("Iteration number: " + iter + " with cost " + currentSol.getCost() + " and best cost " + bestSolEver.getCost());
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
        
    	// Print computation time
        long time_end = System.currentTimeMillis();
        long duration = time_end - time_start;
        System.out.println("The plan was generated in " + duration + " milliseconds.");
        System.out.print(currentSol.toString());
        
        return plans;
    }

    private Plan naivePlan(Vehicle vehicle, TaskSet tasks) {
        City current = vehicle.getCurrentCity();
        Plan plan = new Plan(current);

        for (Task task : tasks) {
            // move: current city => pickup location
            for (City city : current.pathTo(task.pickupCity)) {
                plan.appendMove(city);
            }

            plan.appendPickup(task);

            // move: pickup location => delivery location
            for (City city : task.path()) {
                plan.appendMove(city);
            }

            plan.appendDelivery(task);

            // set current city
            current = task.deliveryCity;
        }
        return plan;
    }
}
