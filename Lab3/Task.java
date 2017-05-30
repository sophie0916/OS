/**
 * Operating Systems CSCI-UA 202 Spring 2017
 * @author: Sophie YeonSoo Kim
 * @version 1.0
 * @since Apr. 17th, 2017
 */

import java.util.*;

public class Task {

	Queue<String> activities = new LinkedList<String>();		//list of activities yet to be completed
	Queue<String> done = new LinkedList<String>();					//list of completed activities

	int ID;																									//Task number
	int[] maxAllocation;																		//stores initial claim of each resource type
	int[] claims;																						//stores remaining claims for each resourct type
	int[] has;																							//stores how many units of each resource type is currently allocated

	String pending = "";																		//pending activity when Task is blocked

	//boolean variables that represent the state of the task
	boolean isInitiated = false;
	boolean isTerminated = false;
	boolean isAborted = false;
	boolean isDelayed = false;
	boolean delayCompleted = false;
	boolean shouldTerminate = false;

	int remainingDelay = -1;																//keeps track of how many cycles the task computes before next activity
	int cycle = 0;																					//total number of cycles it took for task to complete
	int waitingTime = 0;																		//total number of cycles the task spent blocked



	//default constructor
	public Task() {}


	/**
	 * Constructor for Task object
	 * @param ID: stores task number
	 */
	public Task(int ID) {
		this.ID = ID;
	}


	/**
	 * Adds activity to list of future activities of this Task
	 * @param activity read from input
	 */
	public void addActivity(String activity) {
		this.activities.add(activity);
	}


	/**
	 * Initializes sizes of resource arrays
	 * @param numResources: number of resource types (information from input)
	 */
	public void initiate(int numResources) {
		this.claims = new int[numResources+1];
		this.maxAllocation = new int[numResources+1];
		this.has = new int[numResources+1];
		this.isInitiated = true;
	}


	/**
	 * Fills out resource arrays: for convenience, index = resource ID; value = units of resources
	 * @param resourceType
	 * @param initialClaim
	 */
	public void initiate(int resourceType, int initialClaim) {
		this.claims[resourceType] = initialClaim;
		this.maxAllocation[resourceType] = initialClaim;
		this.has[resourceType] = 0;
		this.addCycle();
	}


	/**
	 * Increase time taken for task (in cycles)
	 */
	public void addCycle() {
		this.cycle++;
	}


	/**
	 * Method to initiate delay: change delay-related states and # of remaining delay time
	 * But does not execute the delay itself
	 *
	 * @param delay: number of delays specified by activity
	 * @return number of remaining delays (returned from overloading delay method)
	 */
	public int delay(int delay) {
		this.remainingDelay = delay;
		this.delayCompleted = false;
		this.isDelayed = true;
		return this.delay();
	}


	/**
	 * Method that actually  executes delay
	 * @return number of remaining delays
	 */
	public int delay() {
		this.remainingDelay--;
		this.addCycle();
		//when done with delay, change status back
		if (this.remainingDelay == 0) {
			this.delayCompleted = true;
			this.isDelayed = false;
		}
		return this.remainingDelay;
	}


	/**
	 * Blocking current task
	 */
	public void block(){
		this.waitingTime++;
		this.addCycle();
	}


	/**
	 * Abort current task: 1. move all activities to completed list
	 * 					   2. clear activities list
	 */
	public void abort() {
		for (String s : this.activities) {
			this.done.add(s);
		}
		activities.clear();
		//indicate this task is aborted
		this.isAborted = true;
		this.cycle = 0;
		this.waitingTime = 0;
	}


	/**
	 * Method that accounts for resource Allocation that is granted
	 * @param resourceID
	 * @param request
	 */
	public void grantRequest(int resourceID, int request) {
		//subtract from remaining claim, and add to has list
		this.claims[resourceID] -= request;
		this.has[resourceID] += request;
		this.addCycle();
	}


	/**
	 * Called when blocked: current activity will not be executed (because is UNSAFE) so is saved as pending activity
	 * @param activity
	 */
	public void pendingRequest(String activity){
		this.pending = activity;
	}


	/**
	 * Method that releases specified amount and type of resource allocated to this task
	 * @param resourceID: specifies which resource to release
	 * @param release: units of resources to release
	 * @return # of released resources
	 */
	public int release(int resourceID, int release) {
		if (this.has[resourceID] < release)
			release = this.has[resourceID];
		this.has[resourceID] -= release;
		this.addCycle();
		if (this.shouldTerminate()) this.shouldTerminate = true;
		return release;
	}


	/**
	 * Method that releases all the resource of given type allocated to this task
	 * @param resourceID: specifies which resource to release
	 * @return # of released resources
	 */
	public int releaseAll(int resourceID) {
		int release = this.has[resourceID];
		this.has[resourceID] = 0;
		return release;
	}


	/**
	 * Method that determines whether task is ready to terminate in the current cycle.
	 * Terminates when both of following conditions are satisfied:
	 * 		- if next activity is terminate
	 * 		- if there is no delay in termination
	 * @return whether task should terminate or not
	 */
	public boolean shouldTerminate() {
		String[] peek = this.activities.peek().split("\\s+");
		if (peek[0].equals("terminate") && Integer.parseInt(peek[2]) == 0) this.shouldTerminate = true;
		return this.shouldTerminate;
	}


	/////////////////////////// Following are getter methods /////////////////////////

	public int getID() {
		return this.ID;
	}

	public boolean isTerminated() {
		return this.isTerminated;
	}

	public void terminate() {
		this.isTerminated = true;
	}

	public boolean isAborted() {
		return this.isAborted;
	}

	/////////////////////////////////////////////////////////////////////////////////


	/**
	 * Method to reset Task's properties to when it was initialized
	 * Arrays storing information about resources will be initialized when algorithm is run (at initiate)
	 */
	public void reset() {
		this.isInitiated = false;
		this.isTerminated = false;
		this.isAborted = false;
		this.isDelayed = false;
		this.delayCompleted = false;
		this.shouldTerminate = false;
		this.remainingDelay = -1;
		this.cycle = 0;
		this.waitingTime = 0;
		this.done.clear();
	}


	/**
	 * ToString method that shows all remaining activities
	 */
	public String toString() {
		String str = "";
		for (String s : activities){
			str += s + "\n";
		}
		return str;
	}
}
