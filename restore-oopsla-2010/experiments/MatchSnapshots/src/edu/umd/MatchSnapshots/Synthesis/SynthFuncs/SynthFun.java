package edu.umd.MatchSnapshots.Synthesis.SynthFuncs;

import java.util.StringTokenizer;

import edu.umd.MatchSnapshots.FieldPath;
import edu.umd.MatchSnapshots.Synthesis.Example;

/** Represents a SET of transformer functions. */
/** Currently does not implement Sumit's prioritization of merging based on size of the function set. */
public abstract class SynthFun {
	String className;
	FieldPath field;
	public static String old_obj = "old_obj";
	public static String new_obj = "new_obj";
	
	SynthFun(String className, FieldPath field) {
		this.className = className;
		this.field = field;
	}
	
	/**
	 * Gives the expression representing the given field for the old-version object.
	 * @param field
	 */
	public static String make_old(FieldPath field) {
		if(field.length() == 0)
			return old_obj;
		else
			return old_obj + "." + field.toString();
	}
	
	/**
	 * Gives the expression representing the given field for the new-version object.
	 * @param field
	 */
	public static String make_new(FieldPath field) {
		if(field.length() == 0)
			return new_obj;
		else
			return new_obj + "." + field.toString();
	}
	
	protected static String indent(String s) {
		StringBuffer result = new StringBuffer();
		StringTokenizer tok = new StringTokenizer(s,"\n");
		while(tok.hasMoreTokens()) {
			String t = tok.nextToken();
			result.append("  ");
			result.append(t);
			result.append("\n");
		}
		return result.toString();
	}
	
	public abstract int size();
	
	/** Returns true if and only if there is some transformer in this set that is consistent with the
	 *  example. */
	public abstract boolean consistentWith(Example ex);
	
	/** Removes those elements of the set that are inconsistent with ex.
	 *  Returns either the updated SynFunc object or null if ex is inconsistent with this set.
	 *  The default implementation returns null unless the current function is already consistent
	 *  with ex. */
	public SynthFun refine(Example ex) {
		if(consistentWith(ex))
			return this;
		else
			return null;
	}
	
	/** Expands the function to handle the given example.  This is not possible with all function
	 *  types, but is for "disjunctive" types such as SynthSet and SynthSwitch, it is.  Returns
	 *  null if the function cannot be expanded.  The default implementation always returns null. 
	 */
	public SynthFun expand(Example ex) {
		return null;
	}
	
	/** Each subclass also implements a static getSynthFun method.  This returns null if a
	 *  function of the given class cannot be synthesized or a SynthFun instance if it can be.
	 *  This method can have a variety of signatures depending on the type of function being
	 *  synthesized. */
}