//package lab2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Scheduler {

	public static File file;
	public static boolean VERBOSE = false;
	public static final int FCFSCODE = 0;
	public static final int RRQ2CODE = 1;
	public static final int LCFSCODE = 2;
	public static final int PSJFCODE = 3;
	public static ArrayList<Process> processes= new ArrayList<Process>();
	public static ArrayList<Integer> randNum = new ArrayList<Integer>();
	public static ArrayList<Process> unstartedProcesses = new ArrayList<Process>();
	public static ArrayList<Process> blockedProcesses = new ArrayList<Process>();
	public static ArrayList<Process> terminatedProcesses = new ArrayList<Process>();
	public static int randomCounter = 0;	//reset after each simulation
	public static int finishingTime;		//reset after each simulation
	public static int CPUTime = 0;			//reset after each simulation
	public static int blockTime = 0;		//reset after each simulation

	public static int UNSTARTED = 0;
	public static int READY = 1;
	public static int RUNNING = 2;
	public static int BLOCKED = 3;
	public static int TERMINATED = -1;

	public static int cycle = 0;

	public static int randomOS(int U) {
		int X = randNum.get(randomCounter++);
		return (1 + (X % U));
	}

	public static int randomOSPeak(int U) {
		int X = randNum.get(randomCounter);
		return (1 + (X % U));
	}

	/**
	 *
	 * @param processes, which is the ArrayList of input processes
	 * @return sorted input for the Round Robin model to the main program
	 */
	public static void initiate(ArrayList<Process> processes){
		for (Process process : processes){
			unstartedProcesses.add(process);
		}
	}

	public static void assignPriority() {
		for (int i = 0; i < processes.size(); i++) {
			processes.get(i).priority = i;
		}
	}

	public static void printStatus(){
		System.out.printf("Before cycle: %4d" , cycle);
		Process curr;
		for (int i = 0; i < processes.size(); i++) {
			curr = processes.get(i);
			System.out.printf("%12s%2d", curr.getStatus(), curr.getStatusNum());
		}
		System.out.println("");
	}


	public static void printSummary(int algoCode){
		String algorithm;
		int totalTurnaroundTime = 0;
		int totalWaitingTime = 0;
		for (Process p : processes){
			totalTurnaroundTime += p.turnAroundTime;
			totalWaitingTime += p.waitingTime;
		}
		switch(algoCode) {
			case FCFSCODE:
				algorithm = "First Come First Serve";
				break;
			case RRQ2CODE:
				algorithm = "Round Robin";
				break;
			case LCFSCODE:
				algorithm = "Last Come First Serve";
				break;
			case PSJFCODE:
				algorithm = "Preemptive Shortest Job First";
				break;
			default:
				algorithm = "";
		}
		System.out.printf("The scheduling algorithm used was %s.\n\n", algorithm);
		for (int i = 0; i < processes.size(); i++){
			System.out.printf("Process %d:\n", i);
			processes.get(i).printFinal();
			System.out.println();
		}
		System.out.println("Summary Data:");
		System.out.printf("\tFinishing time: %d\n", cycle);
		System.out.printf("\tCPU Utilization: %f\n", (float)CPUTime/cycle);
		System.out.printf("\tI/O Utilization: %f\n", (float)blockTime/cycle);
		System.out.printf("\tThroughput: %f processes per hundred cycles\n", (float)terminatedProcesses.size()*100/cycle);
		System.out.printf("\tAverage turnaround time: %f\n", (float)totalTurnaroundTime/terminatedProcesses.size());
		System.out.printf("\tAverage waiting time: %f\n", (float)totalWaitingTime/terminatedProcesses.size());

	}
	public static void main(String[] args) throws FileNotFoundException {

		//Create ArrayList of random numbers from given random-number file
		Scanner in = new Scanner(new File("src/random-numbers.txt"));
		int counter=0;
		while(in.hasNextInt()){
			   randNum.add(in.nextInt());
		}

		//Check for command line arguments
		String fileName = "";
		if (args.length < 1){
			System.err.println("Filename missing");
			System.exit(1);
		}
		else if (args.length == 1){
			fileName += args[0];
		}

		else if (args.length == 2 && args[0].equals("--verbose")){
			VERBOSE = true;
			fileName += args[1];
			System.out.println("Success recognizing file name");
		}
		else {
			System.err.println("Incorrect file name format");
		}
		//input file
		file = new File(fileName);

		BufferedReader br = null;
		String input = "";

		//read file
		try {
			br = new BufferedReader(new FileReader(file));
			for (String str = br.readLine(); str != null;str = br.readLine()) {
				input += str;
				input += "\n";
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		//parse input file into array
		String[] tokens = input.split("\\s+");
		int index = 0;
		while (((tokens[index]).equals(""))){
			index++;
		}


		//store information in input file into ArrayList of Process objects
		final int numProcess = Integer.parseInt(tokens[index++]);
		for (int i = 0; i < numProcess; i++) {
			Process curr = new Process(Integer.parseInt(tokens[index++]), Integer.parseInt(tokens[index++]), Integer.parseInt(tokens[index++]), Integer.parseInt(tokens[index++]));
			processes.add(curr);
		}


		boolean isValid = false;
		String answer;
		Scanner userInput = new Scanner(System.in);
		while (!isValid) {
			System.out.println("Choose the scheduling algorithm you would like to run: ");
			System.out.println("1. FCFS");
			System.out.println("2. RR (q=2)");
			System.out.println("3. LCFS");
			System.out.println("4. PSJF");
			answer = userInput.nextLine();
			switch  (answer.toUpperCase()){
				case "FCFS":
					FCFS.simulate(processes);
					isValid = true;
					break;
				case "RR":
					RR.simulate(processes);
					isValid = true;
					break;
				case "LCFS":
					LCFS.simulate(processes);
					isValid = true;
					break;
				case "PSJF":
					PSJF.simulate(processes);
					isValid = true;
					break;
				default: isValid = false;
			}
		}
		in.close();
		userInput.close();

		//FCFS.simulate(processes);
		//RR.simulate(processes);
		//LCFS.simulate(processes);
		//PSJF.simulate(processes);
	}

}
