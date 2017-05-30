/**
 * Operating Systems CSCI-UA 202 Spring 2017
 * @author: Sophie YeonSoo Kim
 * @version 1.0
 * @since Apr. 17th, 2017
 */

import java.util.*;

public class Banker extends Manager{

	public static int cycle = 0;

	public static Queue<Task> ready = new LinkedList<Task>();											//list of currently ready tasks
	public static ArrayList<Task> tasks = new ArrayList<Task>();									//stores tasks ready for next cycle
	public static Queue<Task> blocked = new LinkedList<Task>();										//list of blocked tasks
	public static ArrayList<Task> terminated = new ArrayList<Task>();							//list of terminated tasks
	public static ArrayList<Task> aborted = new ArrayList<Task>();								//list of aborted tasks
	public static ArrayList<Task> toRemoveFromBlocked = new ArrayList<Task>();		//temporarily stores tasks being unblocked to move to ready
	public static boolean updateRec = false;

	public static String errMsg = "";																							//stores Error Messages

	/**
	 * Before every new cycle, add resources released from previous cycle to list of available resources
	 */
	public static void updatePendingResources() {
		for (Integer ID : resources.keySet()){
			resources.get(ID).updatePending();
		}
	}


	/**
	 * Method to allocate requested units of resource (info from activity)
	 * given that it is SAFE to grant allocation
	 * @param task: current task
	 */
	public static void allocate(Task task){
		//since it has been verified that it is safe to allocate resource to this current task, poll requesting activity from task's activity list
		String activity = task.activities.poll();
		String[] tokens = activity.split("\\s+");
		int resourceID = Integer.parseInt(tokens[3]);
		int request = Integer.parseInt(tokens[4]);

		//execute allocation and process task as completed
		resources.get(resourceID).allocate(request);
		task.grantRequest(resourceID, request);
		task.done.add(activity);
	}


	/**
	 * Method to release task's specified number of resources of specified resource type according to activity
	 * then adds that released resource to pending resource list (to become available during next cycle)
	 *
	 * @param task: current task
	 */
	public static void release(Task task) {

		String activity = task.activities.poll();
		String[] tokens = activity.split("\\s+");
		int resourceID = Integer.parseInt(tokens[3]);
		int release = Integer.parseInt(tokens[4]);

		//execute release and process task as completed
		resources.get(resourceID).addReleasedResource(task.release(resourceID, release));
		task.done.add(activity);
	}


	/**
	 * Method to check whether allocation of certain type of resource to current task is safe, let alone possible
	 *
	 * @param task: current task
	 * @return code UNSAFE if there aren't enough resources potentially, and therefore task should be blocked
	 * 				ABORT if task requests more than its remaining claims
	 * 				SAFE else, it is considered safe
	 */
	public static int isSafe(Task task){

		String activity = task.activities.peek();
		String[] tokens = activity.split("\\s+");
		int resourceID = Integer.parseInt(tokens[3]);
		int request = Integer.parseInt(tokens[4]);

		//ERROR CHECK for banker's algorithm: if request is greater than claim, abort
		if (request > task.maxAllocation[resourceID] - task.has[resourceID]) {
			errMsg += "\nDuring cycle " + cycle + "-" + (cycle+1) + " of Banker's algorithms\n\tTask " + task.getID() + "'s request exceeds its claim; aborted; " + task.has[resourceID] + " units available next cycle";
			return ABORT;
		}
		//remaining = max allocation -  currently allocated
		//if remaining > available resource, then it’s not safe and do not grant request
		else {
			for (Integer ID : resources.keySet()){
				if (task.maxAllocation[ID] - task.has[ID] > resources.get(ID).available()) {
					return UNSAFE;
				}
			}
		}
		return SAFE;
	}


	/**
	 * Blocks unsafe task and adds to blocked list
	 * @param t: current task
	 */
	public static void blockTask(Task t) {
		t.block();
		blocked.add(t);
	}


	/**
	 * Method to iterate through all the blocked tasks first, to check if any requests can be satisfied
	 * first, check by calling the isSafe() method, then act accordingly
	 */
	public static void handleBlocked(boolean updateRec){
		if (updateRec) updatePendingResources();
		for (Task task : blocked){
			//if next activity is safe to execute, unblock and resume
			if (isSafe(task) == SAFE) {
				allocate(task);
				task.pending = "";
				toRemoveFromBlocked.add(task);
				tasks.add(task);
			}
			//if still unsafe, continue blocking
			else if (isSafe(task) == UNSAFE) {
				task.block();
			}
		}
	}


	/**
	 * Update blocked list after iterating through and processing blocked tasks first
	 */
	public static void removeFromBlocked() {
		for (Task t : toRemoveFromBlocked){
			blocked.remove(t);
		}
		toRemoveFromBlocked.clear();
	}


	/**
	 * Method to release all the resources current aborting task t has, then actually aborting task
	 * @param t: current task t
	 */
	public static void abortTask(Task task) {

		//if task is not initiated yet, no resource is allocated to task, so simply abort
		if (!task.isInitiated){
			task.abort();
		}
		//else, release all resource units allocated to task before aborting
		else{
			for (int i = 0; i < task.has.length; i++){
				if (resources.containsKey(i)) {
					int release = task.releaseAll(i);
					resources.get(i).addReleasedResource(release);
				}
			}
		}
		//after releasing, abort task
		task.abort();
		aborted.add(task);
		tasks.remove(task);
	}


	/**
	 * Method to terminate given task and release all the resources that task currently has
	 * @param task: current task to terminate
	 */
	public static void terminate(Task task) {
		for(int i = 1; i < numResources; i++) {
			resources.get(i).addReleasedResource(task.releaseAll(i));
		}
		task.terminate();
		terminated.add(task);
		tasks.remove(task);
		task.done.add(task.activities.poll());
	}


	/**
	 * Main backbone for running Banker's algorithm
	 * 1. Check all blocked tasks
	 * 2. For all non-terminated, non-aborted, non-blocked resources, execute activity
	 */
	public static void runBanker(){

		//load tasks from originalTasks
		tasks.addAll(originalTasks);

		//continue cycle until all tasks are either terminated or aborted
		while(terminated.size() + aborted.size() < numTasks){

			//load ready tasks
			ready.addAll(tasks);
			tasks.clear();

			//update available resources to take into account all the resources released during last cycle
			updatePendingResources();

			//check blocked tasks first
			if (blocked.size() != 0){
				handleBlocked(updateRec);
				removeFromBlocked();
				updateRec = false;
			}

			//Loop through all ready processes
			while (!ready.isEmpty()){

				//load next activity of current task
				Task task = ready.poll();
				String act = task.activities.peek();
				String[] tokens = act.split("\\s+");
				int delay = Integer.parseInt(tokens[2]);
				int resourceID = Integer.parseInt(tokens[3]);
				int request = Integer.parseInt(tokens[4]);

				//Check and handle any delays
				if (!task.delayCompleted && delay > 0){
					//if task is already in the process of delaying remaining computes, delay 1 of remaining
					if (task.isDelayed){
						task.delay();
					}
					//if task is not delayed yet, initiate delay
					else{
						task.delay(delay);
					}
					tasks.add(task);
					continue;
				}
				//reset delay status of task after delay is complete
				task.delayCompleted = false;

				//initiate task with initial resource claim
				if (act.contains("initiate")){
					//ERROR CHECK: if task’s initial claim > resource’s maximum units available, abort
					if(request > resources.get(resourceID).getMax()) {
						abortTask(task);
						errMsg += "\nBanker aborts task " + task.getID() + " before run begins:\n\tclaim for resourse " + resourceID + " (" + request + ") exceeds number of units present (" + resources.get(resourceID).getMax() + ")";
						continue;
					}
					//if task is not initiated yet, initiate with maximum number of resource types
					if (!task.isInitiated) {
						task.initiate(numResources);
					}
					//initiate with resource type and initial claim
					task.initiate(resourceID, request);
					task.done.add(task.activities.poll());
					tasks.add(task);
				}

				//handle resource allocation requests
				else if (act.contains("request")){
					//check state of task's next activity
					int isItSafe = isSafe(task);

					//if safe to proceed, grant resource allocation request
					if (isItSafe == SAFE) {
						allocate(task);
						tasks.add(task);
					}
					//if remaining claim > resource’s currently available units
					//unsafe to proceed. Block task.
					else if (isItSafe == UNSAFE) {
						blockTask(task);
						task.pending = act;
					}
					//if request > task’s remaining claim, abort task
					else if (isItSafe == ABORT){
						abortTask(task);
					}
				}

				//handle resource releases
				else if (act.contains("release")){
					release(task);
					tasks.add(task);
				}

				//terminate task
				else if (act.contains("terminate")){
					terminate(task);
				}

				//if next activity is termination and there is no delay, terminate during this cycle
				if(task.shouldTerminate) terminate(task);
			}
			cycle++;
		}
		//print error message
		System.out.println(errMsg);
		printResult(BANKER);
	}
}
