package edu.umd.MatchSnapshots.ExpandedObjects;

public class CharObject extends ExpandedObject {
	public char val;
	public CharObject(char val) {
		this.val = val;
	}
	@Override
	public String toString() {
		return Character.toString(val);
	}
	@Override
	public boolean equals(Object other) {
		if(other instanceof CharObject) {
			CharObject otherObj = (CharObject) other;
			return val == otherObj.val;
		}
		else return false;
	}
	public String getType() { return "char"; }
}