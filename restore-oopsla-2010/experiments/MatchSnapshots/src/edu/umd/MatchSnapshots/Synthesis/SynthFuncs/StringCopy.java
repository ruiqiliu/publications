package edu.umd.MatchSnapshots.Synthesis.SynthFuncs;

import edu.umd.MatchSnapshots.FieldPath;
import edu.umd.MatchSnapshots.ExpandedObjects.*;
import edu.umd.MatchSnapshots.Synthesis.Example;

public class StringCopy extends SynthFun {
	private StringCopy(String className, FieldPath field) { super(className, field); }

	public static StringCopy getSynthFun(String className, FieldPath field, Example ex) {
		ExpandedObject eo1 = ex.getOld().getField(field);
		ExpandedObject eo2 = ex.getNew().getField(field);
		if(eo1 instanceof StringObject && eo2 instanceof StringObject) {
			StringObject o1 = (StringObject) eo1;
			StringObject o2 = (StringObject) eo2;
			if(o1.val.equals(o2.val))
				return new StringCopy(className, field);
		}
		if(eo1 == null && eo2 == null)
			return new StringCopy(className, field);
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
		if(eo1 instanceof StringObject && eo2 instanceof StringObject) {
			StringObject o1 = (StringObject) eo1;
			StringObject o2 = (StringObject) eo2;
			if(o1.val.equals(o2.val))
				return true;
		}
		if(eo1 == null && eo2 == null)
			return true;
		return false;
	}
}
