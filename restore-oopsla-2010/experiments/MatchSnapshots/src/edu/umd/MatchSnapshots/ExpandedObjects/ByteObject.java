package edu.umd.MatchSnapshots.ExpandedObjects;

import java.util.LinkedList;
import java.util.List;

import edu.umd.MatchSnapshots.FieldPath;

public class ByteObject extends ExpandedObject {
	public byte val;
	public ByteObject(byte val) {
		this.val = val;
	}
	@Override
	public String toString() {
		return Byte.toString(val);
	}
	@Override
	public boolean equals(Object other) {
		if(other instanceof ByteObject) {
			ByteObject otherObj = (ByteObject) other;
			return val == otherObj.val;
		}
		else return false;
	}
	@Override
	public int hashCode() {
		return (val & HASH_MASK) | (10 << 30);
	}
	public String getType() { return "byte"; }
}