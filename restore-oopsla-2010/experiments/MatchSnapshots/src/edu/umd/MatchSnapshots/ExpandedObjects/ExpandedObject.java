package edu.umd.MatchSnapshots.ExpandedObjects;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import edu.umd.MatchSnapshots.FieldPath;

/** This class is used to represent both recursively expanded objects and field values.
 *  Expanded objects are, at the top-level, instances of GenericObject.  The ordering
 *  among these and their hash codes are based on internal VM addresses.
 *  Equality is structural equality.
 *  Value representations are instances of one of the "non-generic" classes.
 *  Currently these are BoolObject, IntObject, and StringObject.
 *  These classes have equals(), hashCode(), and compareTo() methods that take into
 *  account the actual values represented by instances of the class. */
public abstract class ExpandedObject {
	int HASH_MASK = ~(11 << 30);
	
	public ExpandedObject getField(FieldPath path) {
		ExpandedObject result = this;
		for(String field : path.getPath()) {
			if(result == null)
				return null;
			if(!(result instanceof GenericObject) || ((GenericObject)result).fields == null)
				return null;
			result = ((GenericObject)result).fields.get(field);
		}
		return result;
	}
	
	public abstract String getType();

	public boolean containsField(FieldPath path) {
		return getField(path) != null;
	}
	
	public final int TYPE_MISMATCH_DIFF = 2;
	public final int FIELD_MISMATCH_DIFF = 1;
	public final int NULL_MISMATCH_DIFF = 1;
	
	public int diff(ExpandedObject o2) {
		ExpandedObject o1 = this;
		
		if(o2 == null)
			return NULL_MISMATCH_DIFF;
		
		/* If field name matches but type differs return 2. */
		if(o1 instanceof StringObject) {
			if(!(o2 instanceof StringObject))
				return TYPE_MISMATCH_DIFF;
			if(o2 == null)
				return NULL_MISMATCH_DIFF;
			
			/* If strings are equal return 0, else return 1. */
			if(((StringObject)o1).val.equals(((StringObject)o2).val))
				return 0;
			return 1;
		}
		
		if(o1 instanceof IntObject) {
			if(!(o2 instanceof IntObject))
				return TYPE_MISMATCH_DIFF;
			if(((IntObject)o1).val == ((IntObject)o2).val)
				return 0;
			return 1;
		}
		
		if(o1 instanceof GenericObject) {
			if(!(o2 instanceof GenericObject))
				return TYPE_MISMATCH_DIFF;
			
			GenericObject obj1 = (GenericObject) o1;
			GenericObject obj2 = (GenericObject) o2;
			if(!obj1.type.equals(obj2.type))
				return TYPE_MISMATCH_DIFF;
			
			Set<String> keyset1 = obj1.fields.keySet();
			Set<String> keyset2 = obj2.fields.keySet();
			int difference = 0;
			int common_elements = 0;
			for(String s : keyset1) {
				if(keyset2.contains(s)) {
					common_elements++;
					// compute recursive difference;
					ExpandedObject obj1mapping = obj1.fields.get(s);
					ExpandedObject obj2mapping = obj2.fields.get(s);
					if(obj1mapping != null)
						// this also takes care of the case where obj1mapping != null but obj2mapping == null
						difference += obj1mapping.diff(obj2mapping);
					else if(obj2mapping != null)
						// obj1mapping == null and obj2mapping != null
						difference += NULL_MISMATCH_DIFF;
					// else both null -- diff is 0.
				}
			}
			// add difference that occurs at this level
			difference += (keyset1.size() - common_elements) * FIELD_MISMATCH_DIFF;
			difference += (keyset2.size() - common_elements) * FIELD_MISMATCH_DIFF;
			
			return difference;
		}
		
		if(o1 instanceof BoolObject) {
			if(!(o2 instanceof BoolObject))
				return TYPE_MISMATCH_DIFF;
			
			BoolObject obj1 = (BoolObject) o1;
			BoolObject obj2 = (BoolObject) o2;
			if(obj1.val == obj2.val)
				return 0;
			return 1;
		}
		
		return Integer.MAX_VALUE; // unreachable
	}
	
	protected void prefixFields(List<FieldPath> fields, String prefix) {
		for(FieldPath field : fields)
			field.prependMutate(prefix);
	}

	/** Subclasses that are composite objects should override this to list their fields. */
	public List<FieldPath> getFields() {
		LinkedList<FieldPath> result = new LinkedList<FieldPath>();
		result.add(new FieldPath());
		return result;
	}
	
	public String indent(String str) {
		return "  " + str.replace("\n", "\n  ");
	}
	
	/** Does this object represent a null value?  Defaults to 'false'. */
	public boolean isNull() {
		return false;
	}
}
