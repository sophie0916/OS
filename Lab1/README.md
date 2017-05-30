## Operating Systems CSCI-UA 202
### Spring 2017
### Lab1-Linker
### Author: Sophie YeonSoo Kim


Implementation of a two-pass linker in Java .
The target machine is word addressable, with a memory of 200 words, each consisting of 4 decimal digits.

This linker program accepts the input file from the command line argument, processes the input twice 
- 1st pass builds a library of modules and symbols to compute the base addresses and the symbol table,
- 2nd pass utilizes the symbol table to relocate relative addresses and resolve external references.

The output consisting of the symbol table and the memory map is then printed on the console.

Following errors are handled when encountered:
- Symbols that are defined multiple times: only first defined value is used
- Symbols that are undefined but used: value of zero is used
- Symbols that are defined but never used (shown as warning message)
- Multiple symbols listed as use in same instruction: all but first are ignored
- Address appearing in use list that exceeds size of module is ignored
- Absolute address that exceeds size of machine: value of zero is used
- Relative address that exceeds size of module: value of zero is used


In order to compile and run the Linker program, make sure you are in the src folder, not lab1.
Compile with following code: 
javac *.java

Run with following code:
java Linker inputs/input1.txt
(input files from input1.txt to  input9.txt are available in the inputs folder)
