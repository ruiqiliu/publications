package edu.umd.MatchSnapshots.ExpandedObjects;

import java.util.LinkedList;
import java.util.List;

import edu.umd.MatchSnapshots.FieldPath;

public class LongObject extends ExpandedObject {
	public long val;
	public LongObject(long val) {
		this.val = val;
	}
	@Override
	public String toString() {
		return Long.toString(val);
	}
	@Override
	public boolean equals(Object other) {
		if(other instanceof LongObject) {
			LongObject otherObj = (LongObject) other;
			return val == otherObj.val;
		}
		else return false;
	}
	@Override
	public int hashCode() {
		return ((int)val & HASH_MASK) | (10 << 30);
	}
	public String getType() { return "long"; }
}