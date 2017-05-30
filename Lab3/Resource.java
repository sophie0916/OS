/**
 * Operating Systems CSCI-UA 202 Spring 2017
 * @author: Sophie YeonSoo Kim
 * @version 1.0
 * @since Apr. 17th, 2017
 */

public class Resource {

	private int ID;																							//specifies resource type
	private int maxResource = 0;																//Max number of resources available (initial amount)
	private int available = 0;																	//number of currently available resources
	private int pending = 0;																		//resources just released but not yet available
																															//		(will become available during next cycle)


	//Default constructor
	public Resource(){}


	/**
	 * Constructor for Resource
	 * @param ID: resource type
	 * @param maxResource: number of units initially present for this resource type
	 */
	public Resource (int ID, int maxResource) {
		this.ID = ID;
		this.maxResource = maxResource;
		this.available = maxResource;		//initially no units are allocated so all are available
	}


	/**
	 * Method that temporarily stores resources that are just released
	 * These are not yet available, but will become available in the next cycle
	 * @param released
	 */
	public void addReleasedResource(int released) {
		this.pending += released;
	}


	/**
	 * Method to make recently released resources available to allocate again
	 */
	public void updatePending() {
		this.available += this.pending;
		this.pending = 0;
	}


	/**
	 * Method to allocate requested amount of units
	 * Keeps track of how many units are left as available
	 * @param request
	 */
	public void allocate(int request) {
		this.available -= request;
	}


	/**
	 * Method to reset resource to initial state, when initialized from input file info
	 * No units are allocated and none are pending
	 */
	public void resetResource() {
		this.available = this.maxResource;
		this.pending = 0;
	}


	/////////////////////////// Following are getter methods /////////////////////////

	public int getID() {
		return this.ID;
	}

	public int getMax() {
		return this.maxResource;
	}

	public int getPending() {
		return this.pending;
	}

	public int available() {
		return this.available;
	}
	/////////////////////////////////////////////////////////////////////////////////

}
