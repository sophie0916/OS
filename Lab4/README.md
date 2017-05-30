## Operating Systems CSCI-UA 202
### Spring 2017
### Lab4: Demand Paging-Page Replace Algorithms
### Author: Sophie YeonSoo Kim


* Implementation of FIFO, LRU, and Random algorithms for page replacement in Demand Paging.
* The program will run the algorithm specified in the command line input.
* Driver uses round robin scheduling with quantum q = 3

* Input: takes 6 (+1 optional) arguments from the command line
	- M: machine size in words
	- P: page size in words
	- S: size of process (i.e. reference are to virtual addresses 0…S-1)
	- J: “job mix” that specifies the number of processes and determines probabilities A, B, and C
	- N: number of references for each process
	- R: replacement algorithm (FIFO, RANDOM, or LRU)
	- debugging mode(optional): if 0, print only simple output, if 1, print details of each runtime, if 11 print random numbers used

* Output: when run is completed, the following will be displayed on console
	- for each process, the number of page faults and average residency time (time page was evicted - time loaded, in memory references, divided by # of evictions)
	- total number of faults and the overall average residency time
	- debugging mode: if 0, print only simple output, if 1, print details of each runtime, if 11 print random numbers used




FIFO (First In First Out):
	- The first one loaded is the first one evicted.

LRU (Least Recently Used):
	- When a page fault occurs, choose as victim that page that has been unused for the longest time, i.e. the one that has been least recently used.


Random:
	- Choose victim frame by random (using random numbers from random-numbers.txt



Compile and run the program with following commands:

	javac *.java
	java Pager M P S J N R <debug mode(optional)>

	ex) java Pager	800 40 400 4 5000 lru 0
	    java Pager	800 40 400 4 5000 lru 1

	Here is a list of inputs that you can try:
		- 10 10 20 1 10 lru 0
		- 10 10 10 1 100 lru 0
		- 10 10 10 2 10 lru 0
		- 20 10 10 2 10 lru 0
		- 20 10 10 2 10 random 0
		- 20 10 10 2 10 fifo 0
		- 20 10 10 3 10 lru 0
		- 20 10 10 3 10 fifo 0
		- 20 10 10 4 10 lru 0
		- 20 10 10 4 10 random 0
		- 90 10 40 4 100 lru 0
		- 40 10 90 1 100 lru 0
		- 40 10 90 1 100 fifo 0
		- 800 40 400 4 5000 lru 0
		- 10 5 30 4 3 random 0
		- 1000 40 400 4 5000 fifo 0
