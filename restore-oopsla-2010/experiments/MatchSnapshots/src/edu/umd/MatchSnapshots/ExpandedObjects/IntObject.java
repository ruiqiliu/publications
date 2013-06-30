package edu.umd.MatchSnapshots.ExpandedObjects;

import java.util.LinkedList;
import java.util.List;

import edu.umd.MatchSnapshots.FieldPath;

public class IntObject extends ExpandedObject {
	public int val;
	public IntObject(int val) {
		this.val = val;
	}
	@Override
	public String toString() {
		return Integer.toString(val);
	}
	@Override
	public boolean equals(Object other) {
		if(other instanceof IntObject) {
			IntObject otherObj = (IntObject) other;
			return val == otherObj.val;
		}
		else return false;
	}
	@Override
	public int hashCode() {
		return (val & HASH_MASK) | (10 << 30);
	}
	public String getType() { return "int"; }
}