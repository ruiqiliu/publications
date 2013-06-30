package edu.umd.MatchSnapshots.Conditions;

import edu.umd.MatchSnapshots.ExpandedObjects.ExpandedObject;

public class TrueCondition extends Condition {
	public TrueCondition() { }
	
	public boolean satisfies(ExpandedObject obj) {
		return true;
	}

	@Override
	public String toString() {
		return "true";
	}
}
