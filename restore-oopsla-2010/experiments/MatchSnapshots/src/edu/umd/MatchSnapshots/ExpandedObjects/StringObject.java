package edu.umd.MatchSnapshots.ExpandedObjects;

public class StringObject extends ExpandedObject {
	public String val;
	public StringObject(String val) {
		this.val = val;
	}
	@Override
	public String toString() {
		return val;
	}
	@Override
	public boolean equals(Object other) {
		if(other instanceof StringObject) {
			StringObject otherObj = (StringObject) other;
			if(val == null)
				return otherObj.val == null;
			else
				return val.equals(otherObj.val);
		}
		else return false;
	}
	@Override
	public int hashCode() {
		return (val.hashCode() & HASH_MASK) | (01 << 30);
	}
	public String getType() { return "String"; }
}