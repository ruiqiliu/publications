package edu.umd.MatchSnapshots.Synthesis.SynthFuncs;

import edu.umd.MatchSnapshots.FieldPath;
import edu.umd.MatchSnapshots.ExpandedObjects.ExpandedObject;
import edu.umd.MatchSnapshots.ExpandedObjects.GenericObject;
import edu.umd.MatchSnapshots.ExpandedObjects.StringObject;
import edu.umd.MatchSnapshots.Synthesis.Example;

/** Deprecated.  Subsumed by Constant.
 * 
 * @author smagill
 *
 */
public class StringConstant extends SynthFun {
	private String constant;
	private StringConstant(String className, FieldPath field, String constant) {
		super(className, field);
		this.constant = constant;
	}
	
	/** Returns an instance of this transformer function if there is an instance that would be
	 *  consistent with the given example. */
	public static StringConstant getSynthFun(String className, FieldPath field, Example ex) {
		ExpandedObject newObj = ex.getNew().getField(field);
		ExpandedObject oldObj = ex.getOld().getField(field);
		
		if(newObj instanceof StringObject)
				return new StringConstant(className, field, ((StringObject)newObj).val);
		else
			return null;
	}
	
	public String toString() {
		return make_new(field) + " = \"" + constant + "\";\n";
	}
	
	@Override
	public int size() { return 1; }
	
	public boolean consistentWith(Example ex) {
		ExpandedObject newObj = ex.getNew().getField(field);
		ExpandedObject oldObj = ex.getOld().getField(field);
		
		if(newObj instanceof StringObject &&
			 ((StringObject)newObj).val.equals(constant))
			return true;
		else
			return false;
	}
}