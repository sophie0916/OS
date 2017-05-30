/**
 * Operating Systems CSCI-UA 202 Spring 2017
 * @author: Sophie YeonSoo Kim
 * @version 1.0
 * @since May 3rd, 2017
 */

import java.io.*;
import java.util.*;

public class Pager {

	public static int machineSize;
	public static int pageSize;
	public static int processSize;
	public static int jobMix;
	public static int numRefs;
	public static int numProcesses;
	public static int numFrames;
	public static String algorithm;

	public static int debuggingLevel = 0;
	public static int runtime = 1;

	public static final int QUANTUM = 3;

	public static Frame currentFrame = new Frame();

	public static ArrayList<Process> processes = new ArrayList<Process>();
	public static ArrayList<Frame> frameTable = new ArrayList<Frame>();
	public static ArrayList<Process> done = new ArrayList<Process>();

	public static void echoInput() {
		System.out.println();
		System.out.println("The machie size is " + machineSize);
		System.out.println("The page size is " + pageSize);
		System.out.println("The process size is " + processSize);
		System.out.println("The job mix number is " + jobMix);
		System.out.println("The number of references per process is " + numRefs);
		System.out.println("The replacement algorithm is " + algorithm);
		System.out.println("The level of debugging output is " + debuggingLevel);
		System.out.println();
	}


	@SuppressWarnings("static-access")
	public static void main(String[] args) throws FileNotFoundException {
		//Create ArrayList of random numbers from given random-number file
		Scanner random = new Scanner(new FileReader("random-numbers.txt"));

        //Check for correct input format
		if (args.length < 6) {
			System.err.println("Error: invalid input format");
			System.exit(1);
		}

        //Parse input
		machineSize = Integer.parseInt(args[0]);
		pageSize = Integer.parseInt(args[1]);
		processSize = Integer.parseInt(args[2]);
		jobMix = Integer.parseInt(args[3]);
		numRefs = Integer.parseInt(args[4]);
		algorithm = args[5];
		
	//if debugging mode is specified
		if (args.length == 7) {
		    debuggingLevel = Integer.parseInt(args[6]);
		}

		numFrames = machineSize/pageSize;

		//create frame table
		for (int i = 0; i < numFrames; i++) {
			Frame f = new Frame(i);
			frameTable.add(f);
		}
        
        //load processes
		loadProcesses(jobMix);

		echoInput();
		
		//set mode and run algorithm accordingly
		switch(algorithm.toLowerCase()){
			case("lru"):
				LRU lruRunner = new LRU(frameTable);
				lruRunner.run(random);
				printResult();
				break;
			case("fifo"):
				FIFO fifoRunner = new FIFO(frameTable);
				fifoRunner.run(random);
				printResult();
				break;
			case("random"):
				Random randomRunner = new Random(frameTable);
				randomRunner.run(random);
				printResult();
				break;
			default:
				System.err.println("Error: invalid algorithm type");
				System.exit(1);
		}


	}


    //Check if given process is referenced given number of reference times
	public static boolean isDone(Process p) {
		if (p.referenced() == numRefs){
			p.isDone = true;
			done.add(p);
			return true;
		}
		return false;
	}


    //Method to create and load processes to the ArrayList based on given jobMix
	public static void loadProcesses(int jobMix) {
		Process p = new Process();
		switch(jobMix){
			case(1):
				numProcesses = 1;
				p = new Process(1, processSize, 1, 0, 0, numFrames);
				processes.add(p);
				break;
			case(2):
				numProcesses = 4;
				for (int i = 0; i < numProcesses; i++) {
					p = new Process(i+1, processSize, 1, 0, 0, numFrames);
					processes.add(p);
				}
				break;
			case(3):
				numProcesses = 4;
				for (int i = 0; i < numProcesses; i++) {
					p = new Process(i+1, processSize, 0, 0, 0, numFrames);
					processes.add(p);
				}
				break;

			case(4):
				numProcesses = 4;
				double[][] prob = {
						{0.75, 0.25, 0},
						{0.75, 0, 0.25},
						{0.75, 0.125, 0.125},
						{0.5, 0.125, 0.125}
						};
				for (int i = 0; i < numProcesses; i++) {
					p = new Process(i+1, processSize, prob[i][0], prob[i][1], prob[i][2], numFrames);
					processes.add(p);
				}
				break;
		}//switch
	}


    //Check for page fault
	public static boolean hasPageFault(int processID, int pageNumber, Collection<Frame> frames){
		for (Frame f : frames){
			if (f.currentProcess == processID && f.currentPage == pageNumber){
				currentFrame = f;
				return false;
			}
		}
		return true;
	}
    
    
    //Check if there are any free frames left
	public static boolean freeFrameLeft(Collection<Frame> frames) {
		for (Frame f : frames){
			if (f.isFree) return true;
		}
		return false;
	}

    
    //Get the next highest free frame
	public static Frame nextHighestFrame() {
		Frame curr = new Frame();
		for (int i = frameTable.size()-1; i >= 0; i--) {
			curr = frameTable.get(i);
			if (curr.isFree) return curr;
		}
		return curr;
	}

    
    //Pring result after running algorithm
	public static void printResult() {
		int totalFaults = 0;
		int totalResidency = 0;
		int totalEviction = 0;
		System.out.println();
		for (Process p : processes) {
			totalFaults += p.numFaults;
			totalResidency += p.residency;
			totalEviction += p.eviction;
			if (p.eviction == 0){
				System.out.printf("Process %d had %d faults.\n", p.ID, p.numFaults);
				System.out.println("\tWith no evictions, the average residence is undefined.\n");
			}
			else{
			System.out.printf("Process %d had %d faults and %.13f average residency\n", p.ID, p.numFaults, ((double)p.residency/p.eviction));
			}
		}
		System.out.println();
		if (totalEviction == 0){
			System.out.printf("The total number of faults is %d.\n", totalFaults);
			System.out.println("\tWith no evictions, the average residence is undefined.\n");
		}
		else{
			System.out.printf("The total number of faluts is %d and the overall average residency is %.13f.\n\n", totalFaults, ((double)totalResidency/totalEviction));
		}
	}
}
