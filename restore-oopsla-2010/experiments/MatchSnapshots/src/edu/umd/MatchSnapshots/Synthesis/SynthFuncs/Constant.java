package edu.umd.MatchSnapshots.Synthesis.SynthFuncs;

import edu.umd.MatchSnapshots.FieldPath;
import edu.umd.MatchSnapshots.ExpandedObjects.ExpandedObject;
import edu.umd.MatchSnapshots.ExpandedObjects.GenericObject;
import edu.umd.MatchSnapshots.Synthesis.Example;

/**
 * A generic "constant initializer" function.  We only create constant initializers
 * for non-GenericObject values.  GenericObjects are handled by the SynthFields
 * synthesis class.
 * @author smagill
 *
 */
public class Constant extends SynthFun {
	private ExpandedObject value;
	private Constant(String className, FieldPath field, ExpandedObject value)
	{
		super(className, field);
		this.value = value;
	}
	
	/** Returns an instance of this transformer function if there is an instance that would be
	 *  consistent with the given example. */
	public static Constant getSynthFun(String className, FieldPath field, Example ex) {
		ExpandedObject val = ex.getNew().getField(field);
		if(val instanceof GenericObject && !((GenericObject)val).isNull())
			return null;
		return new Constant(className, field, val);
	}
	
	public String toString() {
		return make_new(field) + " = " + value.toString() + ";\n";
	}
	
	@Override
	public int size() { return 1; }
	
	public boolean consistentWith(Example ex) {
		if(ex.getNew().getField(field).equals(this.value))
			return true;
		else
			return false;
	}	
}