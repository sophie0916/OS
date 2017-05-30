/**
 * Resource Allocation Manager
 *
 * Operating Systems CSCI-UA 202 Spring 2017
 * @author: Sophie YeonSoo Kim
 * @version 1.0
 * @since Apr. 17th, 2017
 */

import java.io.*;
import java.util.*;

public class Manager {

	public static File file;
	public static ArrayList<Task> originalTasks = new ArrayList<Task>();											//list of tasks
	public static HashMap<Integer, Resource> resources = new HashMap<Integer, Resource>();		//list of resources
	public static int numTasks = 0;																														//number of tasks
	public static int numResources = 0;																												//number of types of resources

	//signals from checking if safe to execute
	public static final int ABORT = -1;
	public static final int SAFE = 0;
	public static final int UNSAFE = 1;

	//Algorithm code
	public static final int OPTIMISTIC = 0;
	public static final int BANKER = 1;


	/**
	 * Method to read file and process information into task and resource lists
	 * @param file
	 */
	public static void loadTasks(File file) {

		BufferedReader br = null;
		int counter = 0;

		//read file
		try {
			br = new BufferedReader(new FileReader(file));
			ArrayList<String> totalActivity = new ArrayList<String>();
			for (String str = br.readLine(); str != null;str = br.readLine()) {

				//For the first line of the input, store information about the number of processes and resources
				if (counter == 0) {
					String [] firstLine = str.split(" ");
					numTasks = Integer.parseInt(firstLine[0]);
					numResources = Integer.parseInt(firstLine[1]);
					//store the number of each resource in an array, with the index as its resource ID
					for (int j = 2; j < firstLine.length; j++){
						Resource r = new Resource(j-1, Integer.parseInt(firstLine[j]));
						resources.put(r.getID(), r);
					}
				}
				//following lines are added to the activity list read from file
				else{
					totalActivity.add(str);
				}
				counter++;
			}

			//populate empty originalTasks array with distinct ID for each task
			for (int i = 0; i < numTasks; i++){
				Task temp = new Task(i+1);
				originalTasks.add(temp);
			}

			//each activity is added to the activity list of corresponding task number
			for (String activity : totalActivity) {
				if (activity.equals("")) continue;
				String[]tokens = activity.split("\\s+");
				originalTasks.get(Integer.parseInt(tokens[1])-1).addActivity(activity);
			}
		}

		catch (IOException e) {
			e.printStackTrace();
		}

		/*
 		//Check if Tasks have been successfully added
		for (Task t : originalTasks) {
			System.out.println(t.toString());
		}
		System.out.println("length of tasks list: " + originalTasks.size());
		*/
	}

	public static void main(String[] args) throws FileNotFoundException{

		//Check for command line arguments
		String fileName = "";
		if (args.length < 1){
			System.err.println("Filename missing");
			System.exit(1);
		}
		else if (args.length == 1){
			fileName += args[0];
		}

		else {
			System.err.println("Incorrect file name format");
		}

		//create input file
		file = new File(fileName);

		//Load task with information retrieved from file
		loadTasks(file);


		//Run the FIFO algorithm for the Optimistic Resource Manager
		FIFO.runFIFO();
		//reset all the tasks and resources as read from input
		reset();
		//Run Banker's algorithm of Dijkstra
		Banker.runBanker();

	}


	/**
	 * Method runs through the entire list of tasks and resources
	 * to reset the fields to values as initialized when read from input file
	 */
	public static void reset(){
		for (Task task : originalTasks) {
			if (!task.activities.isEmpty()) {
				System.out.println("Error! should be empty after completing all activities.");
			}
			//remove each activity from completed list to original activities list
			task.activities.addAll(task.done);
			task.reset();
		}
		//reset resources to initial max availability and zero allocation
		for (Integer ID : resources.keySet()){
			resources.get(ID).resetResource();
		}
	}



	/**
	 * Method to print out the results
	 * @param ALGOCODE: specifies which algorithm is run
	 */
	public static void printResult(int ALGOCODE) {
		String algorithm = (ALGOCODE == OPTIMISTIC)? "FIFO" : "BANKER'S";
		System.out.printf("\n%15s\n", algorithm);
		int totalCycle = 0;
		int totalWaiting = 0;
		for (Task t : originalTasks){
			System.out.printf("Task %-2d" , t.getID());
			if (t.isAborted) System.out.printf("%13s", "ABORTED");
			else{
				totalCycle += t.cycle;
				totalWaiting += t.waitingTime;
				System.out.printf("%7d" , t.cycle);
				System.out.printf("%5d" , t.waitingTime);
				System.out.printf("%5.0f%s" , (float)(t.waitingTime)*100/t.cycle, "%");
			}
			System.out.println();
		}
		System.out.printf("%-7s" , "total");
		System.out.printf("%7d" , totalCycle);
		System.out.printf("%5d" , totalWaiting);
		System.out.printf("%5.0f%s\n\n" , (float)(totalWaiting)*100/totalCycle, "%");
	}
}
