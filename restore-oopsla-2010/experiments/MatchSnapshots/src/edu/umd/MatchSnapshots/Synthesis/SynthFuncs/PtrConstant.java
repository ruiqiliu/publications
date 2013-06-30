package edu.umd.MatchSnapshots.Synthesis.SynthFuncs;

import edu.umd.MatchSnapshots.FieldPath;
import edu.umd.MatchSnapshots.ExpandedObjects.GenericObject;
import edu.umd.MatchSnapshots.Synthesis.Example;

/** Deprecated.  Subsumed by Constant.
 * 
 * @author smagill
 *
 */
public class PtrConstant extends SynthFun {
	/* The only pointer constant is null, so no need to store the constant. */
	private PtrConstant(String className, FieldPath field) { super(className, field); }
	
	/** Returns an instance of this transformer function if there is an instance that would be
	 *  consistent with the given example. */
	public static PtrConstant getSynthFun(String className, FieldPath field, Example ex) {
		if(ex.getNew().getField(field).isNull())
			return new PtrConstant(className, field);
		else
			return null;
	}
	
	public String toString() {
		return make_new(field) + " = null;\n";
	}
	
	@Override
	public int size() { return 1; }
	
	public boolean consistentWith(Example ex) {
		if(ex.getNew().getField(field).isNull())
			return true;
		else
			return false;
	}
}