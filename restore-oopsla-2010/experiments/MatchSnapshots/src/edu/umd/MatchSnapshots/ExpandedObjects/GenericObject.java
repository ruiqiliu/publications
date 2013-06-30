package edu.umd.MatchSnapshots.ExpandedObjects;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.umd.MatchSnapshots.FieldPath;

public class GenericObject extends ExpandedObject {
	/** The fields for this object.  The null value is represented by a GenericObject with fields == null. */
	public HashMap<String,ExpandedObject> fields;
	public String type;
	
	public GenericObject(String type) {
		fields = null;
		this.type = type;
	}
	
	public GenericObject(HashMap<String,ExpandedObject> fields, String type) {
		this.fields = fields;
		this.type = type;
	}
	
	public String getType() {
		return type;
	}
	
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		if(fields != null) {
			for(String str : fields.keySet()) {
				buf.append(str);
				if(fields.get(str) != null) {
					buf.append(" :\n");
					buf.append(indent(fields.get(str).toString()));
				}
				else
					buf.append(" : null");
				buf.append("\n");
			}
		}
		else buf.append("null");
		return buf.toString();
	}

	@Override
	public List<FieldPath> getFields() {
		LinkedList<FieldPath> result = new LinkedList<FieldPath>();
		// Add one empty field path for this object itself.
		result.add(new FieldPath());
		// Add field paths for this object's fields.
		if(fields != null) {
			for(Map.Entry<String,ExpandedObject> entry : fields.entrySet()) {
			    if(entry.getValue() != null) {
				List<FieldPath> subfields = entry.getValue().getFields();
				prefixFields(subfields,entry.getKey());
				result.addAll(subfields);
			    }
			}
		}
		return result;
	}

	public int numMatchingFields(GenericObject o2) {
		int result = 0;
		if(fields != null) {
			for(Map.Entry<String,ExpandedObject> entry : fields.entrySet()) {
			    ExpandedObject o1val = entry.getValue();
			    ExpandedObject o2val = o2.getField(new FieldPath(entry.getKey()));
			    if(o1val == null && o2val == null)
				result++;
			    else if(o1val != null && o2val != null
				    && o1val.equals(o2val))
				result++;
			}
		}
		return result;
	}

	@Override
	public boolean equals(Object other) {
		if(other instanceof GenericObject) {
			GenericObject otherObj = (GenericObject) other;
			if(fields == null)
				return otherObj.fields == null;			
			if(type == null)
				return otherObj.type == null;			
			return (fields.equals(otherObj.fields) && type.equals(otherObj.type));
		}
		else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		if(fields == null)
			return 0;
		else
			return super.hashCode();
	}
	
	@Override
	public boolean isNull() {
		return fields == null;
	}
}