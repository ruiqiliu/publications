package edu.umd.MatchSnapshots.Synthesis.SynthFuncs;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.umd.MatchSnapshots.FieldPath;
import edu.umd.MatchSnapshots.InstanceSet;
import edu.umd.MatchSnapshots.Pair;
import edu.umd.MatchSnapshots.Conditions.Condition;
import edu.umd.MatchSnapshots.ExpandedObjects.ExpandedObject;
import edu.umd.MatchSnapshots.ExpandedObjects.GenericObject;
import edu.umd.MatchSnapshots.Synthesis.Example;

/**
 * Synthesizes updates for all fields of an object.  Each field update is given by a switch statement.
 * TODO: Make this work for String, Integer, etc. objects.
 * @author smagill
 *
 */
public class SynthFields extends SynthFun {
	private List<FieldPath> fields;
	private Map<FieldPath,SynthFun> fun;
	
	private SynthFields(String className, List<FieldPath> fields) {
	  // Using null here to indicate that no specific field update is being synthesized (all of them are)
		// Better would be to expand the SynthFun class heirarchy to have FieldSynthFun and ObjectSynthFun.
		super(className, null);
		this.fields = fields;
		fun = new HashMap<FieldPath,SynthFun>((int) (fields.size() * 1.2));
	}
	
	private static List<FieldPath> getFields(Example ex) {
		List<FieldPath> fields = ex.getNew().getFields();
		// Remove the empty field (representing this object itself) if the object is non-null and has
		// non-empty fields (things like strings and integers only have the empty field)
		if(fields.size() > 1) {
			fields.remove(0);
		}
		return fields;
	}
	
	public static SynthFields getSynthFun(String className, Example ex) {
		List<FieldPath> fields = getFields(ex);
		SynthFields result = new SynthFields(className, fields);
		for(FieldPath field : fields) {
			System.out.println("getting synth fun for field: " + field);
			String fieldClassName = ex.getNew().getType();
			result.fun.put(field, SynthSwitch.getSynthFun(fieldClassName, field, ex));
		}
		return result;
	}
	
	public String toString() {
		StringBuffer result = new StringBuffer();
		if(fun.size() == 0) {
			return "Null field transformer";
		}
		for(Map.Entry<FieldPath, SynthFun> entry : fun.entrySet()) {
			if(entry.getValue() != null)
				result.append(entry.getValue().toString());
			else
				result.append(make_new(entry.getKey()) + " = " + make_old(entry.getKey()) + "\n");
		}
		return result.toString();
	}
	
	public SynthFields refine(Example ex) {
		int i = 0;
		for(FieldPath field : fields) {
			System.out.println("refining synth fun for field: " + field);
			if(fun.get(field) == null) {
				System.out.println("Couldn't synthesize for field " + field + ", skipping.");
				continue;
			}
			fun.put(field, fun.get(field).refine(ex));
		}
		return this;
	}
	
	public SynthFields expand(Example ex) {
		int i = 0;
		for(FieldPath field : fields) {
			System.out.println("refining synth fun for field: " + field);
			if(fun.get(field) == null) {
				System.out.println("Couldn't synthesize for field " + field + ", skipping.");
				continue;
			}
			fun.put(field, fun.get(field).expand(ex));
		}
		return this;		
	}
	
	@Override
	public int size() {
		int i = 1;
		for(FieldPath field : fields)
			i *= fun.get(field).size();
		return i;
	}
	
	@Override
	public boolean consistentWith(Example ex) {
		for(Map.Entry<FieldPath, SynthFun> entry : fun.entrySet()) {
			if(entry.getValue() == null)
				// We gave up on this field earlier.  Don't count it against this example.
				continue;
			if(!entry.getValue().consistentWith(ex))
				return false;
		}
		return true;
	}
}
