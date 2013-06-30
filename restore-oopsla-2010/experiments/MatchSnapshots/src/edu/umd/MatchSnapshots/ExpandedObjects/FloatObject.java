package edu.umd.MatchSnapshots.ExpandedObjects;

public class FloatObject extends ExpandedObject {
	public float val;
	public FloatObject(float val) {
		this.val = val;
	}
	@Override
	public String toString() {
		return Float.toString(val);
	}
	@Override
	public boolean equals(Object other) {
		if(other instanceof FloatObject) {
			FloatObject otherObj = (FloatObject) other;
			return val == otherObj.val;
		}
		else return false;
	}
	public String getType() { return "float"; }
}