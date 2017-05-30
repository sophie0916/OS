/**
 * Operating Systems CSCI-UA 202 Spring 2017
 * @author: Sophie YeonSoo Kim
 * @version 1.0
 * @since May 3rd, 2017
 */

import java.util.ArrayList;
import java.util.Scanner;

public class Random extends Pager{

	public static ArrayList<Frame> frames = new ArrayList<Frame>();

    //Constructor: load all frames
	public Random(ArrayList<Frame> frameTable) {
		frames.addAll(frameTable);
	}

    //Replace Algorithm: choose victim frame by random
	public static Frame replaceRandom(Scanner random) {
		Frame curr = new Frame();
		int frameID = random.nextInt() % numFrames;
		for (Frame f : frames) {
			if (f.ID == frameID){
				curr = f;
			}
		}
		return curr;
	}


    //Main
	public static void run(Scanner random){
		do {
			for (Process process : processes){

				for (int counter = 0; counter < QUANTUM; counter++){
					if (process.isDone) continue;
					int pageNumber = process.word/pageSize;
					if (debuggingLevel != 0) System.out.printf("%d references word %d (page %d) at time %d: ", process.ID, process.word, pageNumber, runtime);

					//page fault!
					if (hasPageFault(process.ID, pageNumber, frames)){
						//replace algorithm
						//1. find free frame
						if (freeFrameLeft(frames)){
							currentFrame = nextHighestFrame();
							if (debuggingLevel != 0) System.out.printf("Fault, using free frame %d.\n", currentFrame.ID);
						}
						//2. if no free frame, evict
						else{
							currentFrame = replaceRandom(random);
							if (debuggingLevel != 0) System.out.printf("Fault, evicting page %d of %d from frame %d.\n", currentFrame.currentPage, currentFrame.currentProcess, currentFrame.ID);
							processes.get(currentFrame.currentProcess-1).evict(runtime, currentFrame.ID);
						}
						process.numFaults++;
						currentFrame.setCurrent(pageNumber, process.ID);
						process.load(runtime, currentFrame.ID);
					}
					else{
						if (debuggingLevel != 0) System.out.printf("Hit in frame %d.\n", currentFrame.ID);
					}
                    //Check if process is done and update next word
					isDone(process);
					process.getNextWord(random);
					//Update the last used time of current frame
					currentFrame.lastUsedTime = runtime;
					runtime++;
				}
			}
		} while (done.size() < numProcesses);
	}

}
