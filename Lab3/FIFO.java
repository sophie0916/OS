/**
 * Operating Systems CSCI-UA 202 Spring 2017
 * @author: Sophie YeonSoo Kim
 * @version 1.0
 * @since Apr. 17th, 2017
 */

import java.util.*;

public class FIFO extends Manager{

	public static int cycle = 0;

	public static Queue<Task> ready = new LinkedList<Task>();											//list of currently ready tasks
	public static ArrayList<Task> tasks = new ArrayList<Task>();									//stores tasks ready for next cycle
	public static Queue<Task> blocked = new LinkedList<Task>();										//list of blocked tasks
	public static ArrayList<Task> terminated = new ArrayList<Task>();							//list of terminated tasks
	public static ArrayList<Task> aborted = new ArrayList<Task>();								//list of aborted tasks
	public static ArrayList<Task> toRemoveFromBlocked = new ArrayList<Task>();		//temporarily stores tasks being unblocked to move to ready
	public static boolean updateRec = false;


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
	 * @return code SAFE if there are enough resources to allow allocation,
	 * 			    UNSAFE if there aren't enough resources and therefore task should be blocked
	 */
	public static int isSafe(Task task){

		String activity = task.activities.peek();
		String[] tokens = activity.split("\\s+");
		int resourceID = Integer.parseInt(tokens[3]);
		int request = Integer.parseInt(tokens[4]);

		if (request <= resources.get(resourceID).available()){
			return SAFE;
		}
		return UNSAFE;
	}


	/**
	 * For checking blocked tasks.
	 * Checked when all non-terminated, non-aborted tasks are blocked
	 * If resources available now + resources available next cycle < request,
	 * then signal to abort lowest
	 * @param task
	 * @return
	 */
	public static int shouldAbort(Task task){

		String activity = task.activities.peek();
		String[] tokens = activity.split("\\s+");
		int resourceID = Integer.parseInt(tokens[3]);
		int request = Integer.parseInt(tokens[4]);

		if (request <= resources.get(resourceID).available() + resources.get(resourceID).getPending())
			return SAFE;

		return ABORT;
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

		//for each resource type current aborting task has released to available resources list
		for (int i = 0; i < task.has.length; i++){
//			System.out.println("ID: " + i);
			if (resources.containsKey(i)) {

				int release = task.releaseAll(i);
				resources.get(i).addReleasedResource(release);
			}
		}
		//after releasing, abort task
		task.abort();
		aborted.add(task);
		tasks.remove(task);
	}


	/**
	 * Find among blocked tasks one with lowest ID to abort
	 * Then call abortTask method with that specific task
	 */
	public static void abortLowest(){
		Task lowest = blocked.peek();
		for (Task t : blocked) {
			if (lowest.ID > t.ID){
				lowest = t;
			}
		}
		blocked.remove(lowest);
		abortTask(lowest);
		updateRec = true;
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
	public static void runFIFO(){

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
				Boolean flag = true;

				//if all non-aborted, non-terminated tasks are blocked and there are no tasks to grant resource allocation, abort lowest task
				while (blocked.size() == numTasks - (aborted.size() + terminated.size()) && flag){
					flag = true;
					//if at least one of the tasks is safe to proceed, do not abort
					for (Task task : blocked){
						if (shouldAbort(task) == SAFE) {
							flag = false;
						}
					}
					//if all tasks are unsafe, abort task with lowest ID
					if (flag) abortLowest();
				}
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
					//if request > currently available resource units
					//unsafe to proceed. Block task.
					else if (isItSafe == UNSAFE) {
						blockTask(task);
						task.pending = act;
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
		printResult(OPTIMISTIC);
	}
}
