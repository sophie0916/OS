import java.io.*;
import java.util.ArrayList;

/**
 * Main class for linker
 * 
 * @author Sophie YeonSoo Kim
 * @version Feb. 16th, 2017
 *
 */

public class Linker {
	public static File file;
	public static int baseAdd = 0;
	public static ArrayList<Module> MODULES = new ArrayList<Module>();
	public static ArrayList<Symbol> SYMBOL_TABLE = new ArrayList<Symbol>();
	public static ArrayList<String> MEMORY_MAP = new ArrayList<String>();
	public static ArrayList<String> ERROR_LIST = new ArrayList<String>();
	final static int MACHINE_SIZE = 200;

	/**
	 * Method correctly stores definitions, uses, and instructions into corresponding arrayLists
	 * of the current Module object
	 * 
	 * @param tokens: list of tokens from input file
	 * @param index: continues where the previous module has ended
	 * @param currMod: current Module object
	 * @return index: of the tokens[] where the current module left off (so that next module can continue)
	 */
	public static int createModule(String[] tokens, int index, Module currMod){
		//definitions
		int numDefs = Integer.parseInt(tokens[index++]);
		//System.out.println("numDefs " + numDefs);
		for (int j = 0; j < numDefs; j++){
			Symbol currSym = new Symbol(tokens[index++], Integer.parseInt(tokens[index++]));
			currMod.symbols.add(currSym);
			//System.out.println(currSym.toString());
		}

		int numUses = Integer.parseInt(tokens[index++]);

		//store information about uses
		for (int j = 0; j < numUses; j++) {
			String use = tokens[index++];
			while(Integer.parseInt(tokens[index]) != -1){
				use += " " + tokens[index++];  
			}

			currMod.uses.add(use);
			//check sentinel is correct
			if (!tokens[index++].equals("-1")) {
				System.err.println("Incorrect sentinel");
			}
		}

		int numInst = Integer.parseInt(tokens[index++]);

		//store information about instructions
		for (int j = 0; j < numInst; j++){
			String instruction = tokens[index++] + " " + tokens[index++];
			currMod.inst.add(instruction);
		}
		return index;
	}
	
	/**
	 * Method that sets the correct absolute addresses for each base address of the module
	 * and gathers information from  symbols ArrayList defined in each module, if any,
	 * to generate the symbol table for all the modules.
	 * 
	 * @param currMod: current module object
	 */
	public static void fillSymbolTable(Module currMod){
		//create base addresses for each module
		currMod.setAdd(baseAdd);
		baseAdd += currMod.getLen();
		
		//create symbol table
		if (currMod.symbols.size() != 0){
			for(Symbol symbol : currMod.symbols){
				for (Symbol a : SYMBOL_TABLE){
					//Check for multiple definition of variable; use first value
					if (symbol.getName().equals(a.getName())){
						a.error += "Error: This variable is multiply defined; first value used.";
						return;
					}
				}
				//Check for error for when definition exceeds module size; use first word in module
				if (symbol.getRel() >= currMod.getLen()){
					symbol.error += "Error: Definition exceeds module size; first word in module used.";
					String toAdd = "" + currMod.inst.get(0).charAt(3);
					symbol.setAbs(Integer.parseInt(toAdd)-symbol.getRel());
				}
				else{
					symbol.setAbs(currMod.getAddress());
				}
				symbol.setModNum(currMod.modNum);
				SYMBOL_TABLE.add(symbol);
			}
		}
	}
	
	
	/**
	 * Method that searches for given symbol in the symbol table where all the symbols are stored 
	 * If the symbol is found, it is used to relocate address according to instruction, during second pass
	 * 
	 * @param name String literal of given symbol
	 * @return result: Symbol object with matching symbol name, if exists, default symbol object otherwise
	 */
	public static Symbol findSymbol(String name){
		Symbol result = new Symbol(name);
		for (Symbol symbol : SYMBOL_TABLE){
			if (name.equals(symbol.getName())){
				symbol.setUsed();
				result = symbol;
				symbol.error = "";
				return result;
			}
		}
		return result;
	}

	
	/**
	 * Finds symbol with the use that corresponds to the given index of instructions
	 * 
	 * @param module specifies module object to access its uses ArrayList
	 * @param use, the counter variable for iterating instructions that represents the index of the
	 * @return symbol with use corresponding to instruction index, later used for relocation
	 */
	public static Symbol findUse(Module module, int use){
		Symbol result = new Symbol();
		for (int i = 0; i < module.uses.size(); i++){
			String[] ss = module.uses.get(i).split("\\s");
			for (int j = 1; j < ss.length; j++){
				if (ss[j].equals(Integer.toString(use))){
					result = findSymbol(ss[0]);
					for ( int k = i+1; k < module.uses.size(); k++){
						String[] ss2 = module.uses.get(k).split("\\s");
						
						//Check whether multiple variable points to single instruction
						for (int l = 1; l < ss2.length; l++){
							if (ss[j].equals(ss2[l])){
								result.error += " Error: Multiple variables used in instruction; all but first ignored";
								for(Symbol q : SYMBOL_TABLE){
									if (ss2[0].equals(q.getName())){
										q.setUsed();
										break;
									}
								}
								return result;
							}
						}
					}
				}
			}
		}
		return result;
	}

	
	/**
	 * Main method reads files in from the command line argument
	 * Processes the input information by appropriate method calls
	 * 1st pass: creates module and symbol objects, from which the symbol table is computed
	 * 2nd pass: using information from 1st pass, relocates relative addresses and resolves external references
	 * Ultimately prints out symbol table, computed memory map, and errors, if any, to console
	 * 
	 * @param args : name of input file
	 */
	public static void main(String[] args) {  

		if (args.length < 1){
			System.err.println("Filename missing");
			System.exit(1);
		}
		else {
			file = new File(args[0]);
		}
		BufferedReader br = null;
		String text = "";

		try {
			br = new BufferedReader(new FileReader(file));
			for (String str = br.readLine(); str != null;str = br.readLine()) {
				text += str;
				text += "\n";
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		String[] tokens = text.split("\\s+");
		int index = 0;
		
		while (((tokens[index]).equals(""))){
			index++;
		}

		final int numMods = Integer.parseInt(tokens[index++]); 

//---------------------------------------First pass-------------------------------------- 
//while populating list of modules with module objects,
//compute base addresses and generate symbol table
		
		for (int i = 1; i < numMods+1; i++){
			Module currMod = new Module(i);
			MODULES.add(currMod);
			index = createModule(tokens, index, currMod);
			fillSymbolTable(currMod);
		}
		
		System.out.println("Symbol Table");
		for (Symbol sym : SYMBOL_TABLE){
			System.out.println(sym.toString());
		}
		System.out.println("");
		
//--------------------------------------Second pass-------------------------------------- 
//using the base addresses and symbol table generated from the first pass,
//generate the actual output by relocating relative addresses and resolving external references
		
		for (Module module : MODULES){
			//checking that each module's instruction lists are correct
			int counter = 0;
			for (String instruction : module.inst){
				String[] inst = instruction.split("\\s");
				String letter = inst[0];
				int number = Integer.parseInt(inst[1]);
				String finalInst = inst[1];

				switch(letter) {
					case "I":
						MEMORY_MAP.add(Integer.toString(number));
						break;

					case "A":
						//Check whether absolute address exceeds machine size
						if (Integer.parseInt(finalInst.substring(1)) > MACHINE_SIZE){
							finalInst = finalInst.substring(0, 1) + "000";
							finalInst += " Error: Absolute address exceeds machine size; zero used.";
						}
						MEMORY_MAP.add(finalInst);
						break;

					case "R":
						//Check whether relative address exceeds module size
						if (Integer.parseInt(finalInst.substring(1)) > module.getAddress()+module.getLen()){
							finalInst = finalInst.substring(0, 1) + "000";
							finalInst += " Error: Relative address exceeds module size; zero used.";
						}
						else{
							number += module.getAddress();	
							finalInst = Integer.toString(number);
						}
						MEMORY_MAP.add(finalInst);
						break;

					case "E":
						Symbol sym = findUse(module, counter);
						if (sym.getModNum() > 0) {
							int temp = sym.getAbs();
							int temp2 = Integer.parseInt(inst[1].substring(0, 1) + "000") + temp;
							finalInst = Integer.toString(temp2);
						}	
						else{
							finalInst = finalInst.substring(0, 1) + "000";
							
						}
						finalInst += sym.error;
						MEMORY_MAP.add(finalInst);
						break;
						
					default:
						System.err.println("Wrong Instruction code");
						break;
				}
				counter++;
			}
		}
		
		
//---------------------------------------Print out Memory Map-------------------------------------- 
		System.out.println("Memory Map");
		for (int i = 0; i < MEMORY_MAP.size(); i++){
			System.out.println(i + ": " + MEMORY_MAP.get(i).toString());
		}
		
//---------------------------------------Print out Symbol Table-------------------------------------- 		
		System.out.println("");
		for (Symbol sym : SYMBOL_TABLE){
			if (!sym.getUsed()){
				ERROR_LIST.add("Warning: " + sym.getName() + " was defined in module " + sym.getModNum() + " but never used.");
			}
		}
		
		//Check for any errors regarding use exceeding module size
		for (Module mod : MODULES){
			for (String s : mod.uses){
				String[] ss = s.split("\\s");
				for (int i = 1; i < ss.length; i++){
					if (Integer.parseInt(ss[i]) > mod.getLen()){
						ERROR_LIST.add("Error: Use of " + ss[0] + " in module " + mod.modNum + " exceeds module size; use ignored.");
					}
				}
			}
		}
		 
		//Final print of Errors list
		for (String warning : ERROR_LIST) {
			System.out.println(warning);
		}
	}
}
