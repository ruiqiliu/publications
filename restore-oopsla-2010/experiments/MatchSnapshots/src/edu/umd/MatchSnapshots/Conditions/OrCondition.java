package edu.umd.MatchSnapshots.Conditions;

import edu.umd.MatchSnapshots.ExpandedObjects.ExpandedObject;

public class OrCondition extends Condition {
	private Condition cond1, cond2;
	
	/** Constructs a basic AndCondition. */
	public OrCondition(Condition cond1, Condition cond2) {
		this.cond1 = cond1;
		this.cond2 = cond2;
	}

	/** Constructs an AndCondition but additionally tries to simplify the condition. */
	public static Condition makeAndSimplify(Condition cond1, Condition cond2) {
		if(cond1 instanceof TrueCondition)
			return cond1;
		if(cond2 instanceof TrueCondition)
			return cond2;
		if(cond1 instanceof FalseCondition)
			return cond2;
		if(cond2 instanceof FalseCondition)
			return cond1;
		return new OrCondition(cond1,cond2);
	}

	public boolean satisfies(ExpandedObject obj) {
		return cond1.satisfies(obj) || cond2.satisfies(obj);
	}

	@Override
	public String toString() {
		return cond1.toString() + " || " + cond2.toString();
	}
}
