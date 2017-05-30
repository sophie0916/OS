//package lab2;

import java.util.*;

public class LCFS extends Scheduler{
	public static Stack<Process> readyProcesses = new Stack<Process>();
	public static HashMap<Integer, Stack<Process>> hashMap = new HashMap<Integer, Stack<Process>>();
	public static Stack<Process> temp = new Stack<Process>();

	public static Process current;
	public static int runTimes;

	public static boolean tempUpdated = false;


	public static void checkUnstarted() {
		if (unstartedProcesses.isEmpty()) return;

		for (Process process : unstartedProcesses){
			if (process.arrival == cycle){
				if (!hashMap.containsKey(cycle)){
					Stack<Process> a = new Stack<Process>();
					a.push(process);
					hashMap.put(cycle, a);
				}
				else{
					hashMap.get(cycle).push(process);
					Collections.sort(hashMap.get(cycle));
				}
			}
		}
	}

	public static void updateBlocked(){
		if (blockedProcesses.isEmpty()) return;
		blockTime++;
		for (Process process : blockedProcesses){
			process.block();
			if (process.blockedTime == 0){
				if (!hashMap.containsKey(cycle)){
					Stack<Process> a = new Stack<Process>();
					a.push(process);
					hashMap.put(cycle, a);
				}

				else{
					hashMap.get(cycle).push(process);
					Collections.sort(hashMap.get(cycle));
				}
			}
		}
	}

	public static void addToReady() {

		Iterator<Integer> keySetIterator = hashMap.keySet().iterator();
		while(keySetIterator.hasNext()){
			Integer key = keySetIterator.next();
			for (Process p : hashMap.get(key)){
				temp.push(p);

			}
			hashMap.remove(key);
		}

		int tempSize = temp.size();
		for (int i = 0; i < tempSize; i++) {
			Process process = temp.pop();
			process.ready();
			readyProcesses.push(process);
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
		temp.clear();
	}

	public static void updateCurrent() {
		if (readyProcesses.isEmpty()) return;
		current = readyProcesses.pop();
		current.status = RUNNING;
		runTimes = randomOS(current.maxCPUBurst);
		current.runningTime = runTimes;
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

		//run until all the processes are taken
		while (terminatedProcesses.size() < processes.size()){
			updateBlocked();
			checkUnstarted();
			addToReady();


			if (runTimes == 0) {
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
					if (terminatedProcesses.size() == processes.size()) break;
				}
			}
			cycle++;
			if (VERBOSE)printStatus();
			updateWaiting();

		}

		printSummary(LCFSCODE);
	}
}
