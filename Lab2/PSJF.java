//package lab2;

import java.util.*;

public class PSJF extends Scheduler{
	public static Queue<Process> readyProcesses = new LinkedList<Process>();
	public static HashMap<Integer, ArrayList<Process>> hashMap = new HashMap<Integer, ArrayList<Process>>();
	public static Process current = new Process();
	public static int runTimes;



	public static void checkUnstarted() {
		if (unstartedProcesses.isEmpty()) return;

		for (Process process : unstartedProcesses){
			if (process.arrival == cycle){
				//place Process into hashMap with its remainingCPU as key
				hashMapAdd(process);
			}
		}
	}

	public static void updateBlocked(){
		if (blockedProcesses.isEmpty()) return;
		blockTime++;
		for (Process process : blockedProcesses){
			process.block();
			if (process.blockedTime == 0){
				hashMapAdd(process);
			}
		}
	}

	public static void hashMapAdd(Process process) {
		if (!hashMap.containsKey(process.remainingCPU)){
			ArrayList<Process> a = new ArrayList<Process>();
			a.add(process);
			hashMap.put(process.remainingCPU, a);
		}

		else{
			hashMap.get(process.remainingCPU).add(process);
			Collections.sort(hashMap.get(process.remainingCPU));
		}
	}


	public static void addToReady() {
		//pull everything from readyProcesses and then add to hashmap,
		//then add to ready again (so that it's sorted w/ respect to remainingCPU)
		for (Process p : readyProcesses){
			hashMapAdd(p);
		}
		readyProcesses.clear();

		SortedSet<Integer> keys = new TreeSet<Integer>(hashMap.keySet());
			for (Integer key : keys) {
				 for (Process process : hashMap.get(key)){
	 				process.ready();
	 				readyProcesses.add(process);
	 			}
			}

		//remove ready processes from blocked processes
		for (Process p : readyProcesses){
			hashMap.remove(p.remainingCPU);
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
	}


	public static void updateCurrent() {
		if (readyProcesses.isEmpty()) return;
		current = readyProcesses.poll();
		current.status = RUNNING;

		//if preempted, resume
		if (current.preempt){
			runTimes = current.runningTime;
		}
		//if not preempted assign new runningTime from randomOS
		else {
			runTimes = randomOS(current.maxCPUBurst);
			current.preemptRemainingTime = runTimes;
		}
		current.runningTime = runTimes;
	}


	public static void handleCurrent() {
		current.preempt = true;
		current.status = READY;
		hashMapAdd(current);
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

		while (terminatedProcesses.size() < processes.size() ){
			if (VERBOSE) printStatus();
			updateBlocked();

			checkUnstarted();
			addToReady();

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
					current.preempt = false;
				}
				else if (current.status == TERMINATED){
					current.done(cycle);
					terminatedProcesses.add(current);
					current.preempt = false;
					if (terminatedProcesses.size() == processes.size()) break; //???????
				}
				else handleCurrent();
				addToReady();
				updateCurrent();

			}
			cycle++;
			updateWaiting();
		}
		printSummary(PSJFCODE);
	}

}
