// package lab2;

import java.util.*;

public class FCFS extends Scheduler{

	public static Queue<Process> readyProcesses = new LinkedList<Process>();
	public static Process current;
	public static int runTimes;
	public static boolean switchProcess = true;


	public static void checkUnstarted() {
		if (unstartedProcesses.isEmpty()) return;
		for (Process process : unstartedProcesses){
			if (process.arrival == cycle){
				process.ready();
				readyProcesses.add(process);
			}
		}
		for (Process p : readyProcesses){
			for (Process q : unstartedProcesses){
				if (p.equals(q)){
					unstartedProcesses.remove(q);
					break;
				}
			}
		}
	}

	public static void updateBlocked(){
		if (blockedProcesses.isEmpty()) return;
		//check number of processes to unblock: for the purpose of tiebreaking
		blockTime++;
		ArrayList<Process> temp = new ArrayList<Process>();
		for (Process process : blockedProcesses){
			process.block();

			if (process.blockedTime == 0){
				temp.add(process);
			}
		}
		//sort according to arrival
		Collections.sort(temp);
		//add sorted processes to ready list
		for (Process process : temp) {
			process.ready();
			readyProcesses.add(process);
		}

		//remove ready processes from blocked processes
		for (Process p : readyProcesses){
			for (Process q : blockedProcesses){
				if (p.equals(q)){
					blockedProcesses.remove(q);
					break;
				}
			}
		}
	}

	public static void updateCurrent() {
		if (readyProcesses.isEmpty()) return;
		current = readyProcesses.poll();
		current.status = RUNNING;
		runTimes = randomOS(current.maxCPUBurst);
		current.runningTime = runTimes;
	}
	public static void updateWaiting() {
		for (Process p : readyProcesses){
			p.waitProcess();
		}
	}

	public static void assignPriority() {
		for (int i = 0; i < processes.size(); i++) {
			processes.get(i).priority = i;
		}
	}


	public static void simulate(ArrayList<Process> processes){

		for (Process p : processes){
			System.out.print(p.toString() + "\t");
		}
		System.out.println();
		Collections.sort(processes);
		initiate(processes);
		assignPriority();

		System.out.println("Sorted list");
		for (Process p : processes){
			System.out.print(p.toString() + "\t");
		}
		System.out.println();
		//run until all the processes are taken
		while (terminatedProcesses.size() < processes.size()){
			if (VERBOSE) printStatus();
			updateBlocked();
			checkUnstarted();

			if (readyProcesses.isEmpty() && runTimes == 0){
				cycle++;
				continue;
			}

			else if (runTimes == 0) {
				updateCurrent();
			}


			else {
				if (current.status == RUNNING && runTimes != 0) {
					runTimes = current.run(runTimes);
					CPUTime++;
				}
				if (current.status == BLOCKED){
					current.block(randomOS(current.maxIOTime));
					blockedProcesses.add(current);
					updateCurrent();
				}
				else if (current.status == TERMINATED){
					current.done(cycle);
					terminatedProcesses.add(current);
					updateCurrent();
					if (terminatedProcesses.size() == processes.size()) break; //??????
				}
			}
			cycle++;
			updateWaiting();
		}
		printSummary(FCFSCODE);
	}

}
