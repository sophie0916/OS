Operating Systems CSCI-UA 202
Spring 2017
Lab3-Banker’s Algorithm
Author: Sophie YeonSoo Kim


* Implementation of resource allocation using both optimistic resource manager & banker’s algorithm of Dijkstra.
* From the input file, processes T tasks and R resources types.
* The program will run both Optimistic and Banker’s algorithm during a single run.
* Input: input-01.txt to input-13.txt are available in the src folder
* Output: when run is completed, the time taken, waiting time, and percentage of time spent waiting will be displayed for each task and as a total.



Optimistic manager:
	- satisfy a request if possible
	- if not make the task wait
	- when a release occurs, try to satisfy pending requests in a FIFO manner.



Banker’s Algorithm:
	- detects a deadlock when all non-terminated tasks have outstanding requests that the manager cannot satisfy.



Error Checks: resource manager prints informative error messages and aborts task for the following cases
	      (note: this does not apply to the optimistic allocator)	- task’s initial claim > resources present
	- task’s request > remaining claim




Compile and run the program with following commands:

	javac *.java
	java Manager inputs/input-01.txt
