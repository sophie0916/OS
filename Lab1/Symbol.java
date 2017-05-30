/**
 * @author Sophie YeonSoo Kim
 * @version Feb. 12th, 2017
 *
 */

public class Symbol {
	private String name = "";
	private int rel = 0;
	private int abs = 0;
	private int modNum = -1;
	private boolean isUsed = false;
	String error = " ";

	/*
	 * Default Constructor
	 */
	public Symbol(){}

	/**
	 * Constructor for each new symbol
	 * @param name represents the name of symbol
	 */
	public Symbol(String name) {
		this.name = name;
		this.error += "Error: " + this.name + " is not defined; zero used.";
	}

	public Symbol(String name, int rel){
		this.name = name;
		this.rel = rel;
	}

	public String toString() {
		return (this.name + " = " + this.abs + this.error);
	}

	public String getName() {
		return this.name;
	}

	public int getRel() {
		return this.rel;
	}

	public void setAbs(int baseAddress){
		this.abs = this.rel + baseAddress;
	}

	public int getAbs() {
		return this.abs;
	}

	public void setUsed() {
		this.isUsed = true;
	}

	public boolean getUsed() {
		return this.isUsed;
	}

	public int getModNum() {
		return this.modNum;
	}

	public void setModNum(int modNum) {
		this.modNum = modNum;
	}

}
