//package lab2;

import java.util.*;

public class RR extends Scheduler{
	public static Queue<Process> readyProcesses = new LinkedList<Process>();

	public static ArrayList<Process> temp = new ArrayList<Process>();

	public static Process current;
	public static int runTimes;
	public static int QUANTUM = 2;

	public static boolean tempUpdated = false;


	public static void checkUnstarted() {
		if (unstartedProcesses.isEmpty()) return;

		for (Process process : unstartedProcesses){
			if (process.arrival == cycle){
				temp.add(process);
			}
		}
		Collections.sort(temp);
	}


	public static void updateBlocked(){
		if (blockedProcesses.isEmpty()) return;
		blockTime++;
		for (Process process : blockedProcesses){
			process.block();

			if (process.blockedTime == 0){
				temp.add(process);
			}
		}
		//sort according to arrival
		Collections.sort(temp);
	}

	public static void updateTemp() {
		Collections.sort(temp);
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
			for (Process q : unstartedProcesses){
				if (p.equals(q)){
					unstartedProcesses.remove(q);
					break;
				}
			}
		}
		tempUpdated = true;
		temp.clear();
	}

	public static void updateCurrent() {
		if (readyProcesses.isEmpty()) return;
		current = readyProcesses.poll();
		current.status = RUNNING;

		if(current.preempt) {
			runTimes = current.preemptRemainingTime;
		}
		else{
			runTimes = randomOS(current.maxCPUBurst);
			current.preemptRemainingTime = runTimes;
		}

		if(runTimes > QUANTUM){
			runTimes = QUANTUM;
			current.preempt = true;
		}

		else{
			current.preempt = false;
		}

		current.runningTime = runTimes;
	}

	public static void assignPriority() {
		for (int i = 0; i < processes.size(); i++) {
			processes.get(i).priority = i;
		}
	}
	public static void updateWaiting() {
		for (Process p : readyProcesses){
			p.waitProcess();
		}
	}

	public static void simulate(ArrayList<Process> processes){

		System.out.printf("%-23s%-5d", "The original input was:", processes.size());
		for (Process p : processes){
			System.out.print(p.toString() + "\t");
		}
		System.out.println();
		Collections.sort(processes);
		initiate(processes);
		assignPriority();

		System.out.printf("%-23s%-5d", "The (sorted) input is:", processes.size());
		for (Process p : processes){
			System.out.print(p.toString() + "\t");
		}
		System.out.println();

		if (VERBOSE) printStatus();
		checkUnstarted();
		updateTemp();
		tempUpdated = false;

		//run until all the processes are taken
		while (terminatedProcesses.size() < processes.size() ){
			updateBlocked();
			checkUnstarted();

			if (runTimes == 0) {
				updateTemp();
				updateCurrent();
			}

			else {
				if (current.status == RUNNING && runTimes != 0) {
					runTimes = current.run(runTimes);
					CPUTime++;
				}
				if (current.status == BLOCKED){
					if (current.preempt) {
						current.status = READY;
						temp.add(current);
						updateTemp();
					}
					else{
						updateTemp();
						current.block(randomOS(current.maxIOTime));
						blockedProcesses.add(current);
						current.preempt = false;
					}
					updateCurrent();
				}
				else if (current.status == TERMINATED){
					updateTemp();
					current.done(cycle);
					terminatedProcesses.add(current);
					current.preempt = false;
					updateCurrent();
					if (terminatedProcesses.size() == processes.size()) break;
				}
			}
			if (!tempUpdated) updateTemp();
			tempUpdated = false;
			cycle++;
			if (VERBOSE)printStatus();
			updateWaiting();
		}
		printSummary(RRQ2CODE);
	}

}
