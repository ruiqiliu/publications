package edu.umd.MatchSnapshots.Conditions;

import edu.umd.MatchSnapshots.ExpandedObjects.ExpandedObject;

public class FalseCondition extends Condition {
	public FalseCondition() { }
	
	public boolean satisfies(ExpandedObject obj) {
		return false;
	}

	@Override
	public String toString() {
		return "false";
	}
}
