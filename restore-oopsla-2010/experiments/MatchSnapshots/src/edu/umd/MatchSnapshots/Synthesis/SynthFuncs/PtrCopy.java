package edu.umd.MatchSnapshots.Synthesis.SynthFuncs;

import edu.umd.MatchSnapshots.FieldPath;
import edu.umd.MatchSnapshots.ExpandedObjects.*;
import edu.umd.MatchSnapshots.Synthesis.Example;

public class PtrCopy extends SynthFun {
	private PtrCopy(String className, FieldPath field) { super(className, field); }

	// TODO: Make this check that the key fields for the objects match
	public static PtrCopy getSynthFun(String className, FieldPath field, Example ex) {
		ExpandedObject eo1 = ex.getOld().getField(field);
		ExpandedObject eo2 = ex.getNew().getField(field);
		if(eo1 instanceof GenericObject && eo2 instanceof GenericObject) {
			GenericObject o1 = (GenericObject) eo1;
			GenericObject o2 = (GenericObject) eo2;
			if(o1.equals(o2))
				return new PtrCopy(className, field);
			/* Stand-in for computing the matching field */
			else if(o1.numMatchingFields(o2) >= (o1.getFields().size()/2))
			    return new PtrCopy(className, field);
		}
		if(eo1 == null && eo2 == null)
			return new PtrCopy(className, field);
		return null;
	}
	
	public String toString() {
		return make_new(field) + " = " + make_old(field) + ";\n";
	}
	
	@Override
	public int size() { return 1; }
	
	@Override
	public boolean consistentWith(Example ex) {
		ExpandedObject eo1 = ex.getOld().getField(field);
		ExpandedObject eo2 = ex.getNew().getField(field);
		if(eo1 instanceof GenericObject && eo2 instanceof GenericObject) {
			GenericObject o1 = (GenericObject) eo1;
			GenericObject o2 = (GenericObject) eo2;
			if(o1.equals(o2))
				return true;
			/* Stand-in for computing the matching field */
			else if(o1.numMatchingFields(o2) >= (o1.getFields().size()/2))
			    return true;
		}
		if(eo1 == null && eo2 == null)
			return true;
		return false;
	}
}
