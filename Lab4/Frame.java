/**
 * Operating Systems CSCI-UA 202 Spring 2017
 * @author: Sophie YeonSoo Kim
 * @version 1.0
 * @since May 3rd, 2017
 */

public class Frame {

	public int ID = -1;
	public int currentProcess = -1;
	public int currentPage = -1;
	public boolean isFree = true;
	public int lastUsedTime = -1;
	public int loadTime = -1;

    //Default constructor
	public Frame(){}

    //Constructor: initialize with ID
	public Frame(int frameID) {
		this.ID = frameID;
	}

    //Load current frame with given page ID and process ID
	public void setCurrent(int pageID, int processID) {
		this.currentPage = pageID;
		this.currentProcess = processID;
		this.isFree = false;
	}

}
