import java.util.ArrayList;

/**
 * @author Sophie YeonSoo Kim
 * @version Feb. 12th, 2017
 * 
 */

public class Module {
	protected int modNum;
	protected ArrayList<Symbol> symbols;
	protected ArrayList<String> uses;
	protected ArrayList<String> inst;
	private int absAdd;


	/*
	 * Default constructor
	 */
	public Module() {}

	/**
	 * Constructor for each module of input
	 * @param num represents the index number of this module object
	 */
	public Module(int num){
		this.modNum = num;
		symbols = new ArrayList<Symbol>();
		uses = new ArrayList<String>();
		inst = new ArrayList<String>();
	}

	public void setAdd(int address) {
		this.absAdd = address;
	}

	public int getAddress() {
		return this.absAdd;
	}

	public int getLen() {
		return this.inst.size();
	}

}
