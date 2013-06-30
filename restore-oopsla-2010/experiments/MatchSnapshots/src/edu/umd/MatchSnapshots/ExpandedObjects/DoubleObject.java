package edu.umd.MatchSnapshots.ExpandedObjects;

public class DoubleObject extends ExpandedObject {
	public double val;
	public DoubleObject(double val) {
		this.val = val;
	}
	@Override
	public String toString() {
		return Double.toString(val);
	}
	@Override
	public boolean equals(Object other) {
		if(other instanceof DoubleObject) {
			DoubleObject otherObj = (DoubleObject) other;
			return val == otherObj.val;
		}
		else return false;
	}
	public String getType() { return "double"; }
}