package edu.umd.MatchSnapshots.ExpandedObjects;

public class BoolObject extends ExpandedObject {
	public boolean val;
	public BoolObject(boolean val) {
		this.val = val;
	}
	@Override
	public String toString() {
		return Boolean.toString(val);
	}
	@Override
	public boolean equals(Object other) {
		if(other instanceof BoolObject) {
			BoolObject otherObj = (BoolObject) other;
			return val == otherObj.val;
		}
		else return false;
	}
	@Override
	public int hashCode() {
		return (val ? 1 : 0) | (11 << 30);			
	}
	@Override
	public String getType() { return "boolean"; }
}