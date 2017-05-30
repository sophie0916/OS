//package lab2;

public class Process implements Comparable<Process>{
	int arrival;
	int maxCPUBurst;
	int totalCPUTime;
	int remainingCPU;
	int maxIOTime;

	int finishingTime;
	int turnAroundTime;
	int IOTime = 0;;
	int waitingTime = 0;

	int blockedTime = 0;

	int status = 0;
	int priority = 0;

	int runningTime = 0;

	boolean preempt = false;
	int preemptRemainingTime = 0;

	public static final int UNSTARTED = 0;
	public static final int READY = 1;
	public static final int RUNNING = 2;
	public static final int BLOCKED = 3;
	public static final int TERMINATED = -1;

	public Process(){}

	public Process(int arrival, int maxCPUBurst, int totalCPUTime, int maxIOTime){
		this.arrival = arrival;
		this.maxCPUBurst = maxCPUBurst;
		this.totalCPUTime = totalCPUTime;
		this.remainingCPU = totalCPUTime;
		this.maxIOTime = maxIOTime;
	}

	public int compareTo(Process process) {
		if (this.arrival < process.arrival) {
			return -1;
		}
		else if (this.arrival > process.arrival) {
			return 1;
		}
		else{
			if (this.priority < process.priority) {
				return -1;
			}
			else{
				return 1;
			}
		}
	}



	public int run(int randNum) {

		this.remainingCPU--;
		randNum--;
		this.preemptRemainingTime = (this.preempt)? this.preemptRemainingTime-1 : 0;

		if (this.remainingCPU == 0 ) {
			this.status = TERMINATED;
		}

		else if (randNum == 0) {
			this.status = BLOCKED;
		}
		if (this.preemptRemainingTime == 0){
			this.preempt = false;
		}
		this.runningTime = randNum;
		return randNum;
	}

	public void block(int blockedTime) {
		this.blockedTime = blockedTime;
	}

	public void block() {
		this.blockedTime --;
		this.IOTime++;
	}

	public void ready() {
		this.status = READY;
		this.blockedTime = -1;
	}

	public void waitProcess() {
		this.waitingTime++;
	}

	public void done(int finishingTime){
		this.finishingTime = finishingTime;
		this.turnAroundTime = finishingTime - this.arrival;
	}

	public String getStatus() {
		switch(this.status){
			case(UNSTARTED) : return "unstarted\t";
			case(READY) : return "ready\t";
			case(RUNNING) : return "running\t";
			case(BLOCKED) : return "blocked\t";
			case(TERMINATED) : return "terminated\t";
			default: return "invalid state";
		}
	}


	public int getStatusNum() {

		switch(this.status){
			case(RUNNING) : return this.runningTime;
			case(BLOCKED) : return this.blockedTime;
			default:
				if (this.preempt) return this.runningTime;
				else return 0;
		}
	}


	public String toString(){
		String result = Integer.toString(this.arrival) + " " +Integer.toString(this.maxCPUBurst) + " " +Integer.toString(this.totalCPUTime) + " " +Integer.toString(this.maxIOTime);
		return(result);
	}

	public void printFinal() {
		System.out.printf("\t(A,B,C,IO) = (%d,%d,%d,%d)\n", this.arrival, this.maxCPUBurst, this.totalCPUTime, this.maxIOTime);
		System.out.printf("\tFinishing Time: %d\n", this.finishingTime);
		System.out.printf("\tTurnaround Time: %d\n", this.turnAroundTime);
		System.out.printf("\tI/O Time: %d\n", this.IOTime);
		System.out.printf("\tWaiting Time: %d\n", this.waitingTime);
	}


}
