package edu.umd.MatchSnapshots.Conditions;

import java.util.HashSet;
import java.util.Set;

import edu.umd.MatchSnapshots.FieldPath;
import edu.umd.MatchSnapshots.InstanceSet;
import edu.umd.MatchSnapshots.ExpandedObjects.ExpandedObject;
import edu.umd.MatchSnapshots.ExpandedObjects.GenericObject;

public class EqCondition extends Condition {
	private FieldPath field;
	private ExpandedObject value;
	boolean negated;
	
	private EqCondition(FieldPath field, ExpandedObject value, boolean negated) {
		this.field = field;
		this.value = value;
		this.negated = negated;
	}
	
	static Set<ExpandedObject> union(Set<ExpandedObject> set1, Set<ExpandedObject> set2) {
		Set<ExpandedObject> oldVals = set1;
		Set<ExpandedObject> newVals = set2;
		Set<ExpandedObject> unioned = new HashSet<ExpandedObject>(oldVals.size() * newVals.size());
		for(ExpandedObject val : oldVals)
			unioned.add(val);
		for(ExpandedObject val : newVals)
			unioned.add(val);
		return unioned;
	}

	public static Condition getCondition(InstanceSet in, InstanceSet out) {
		int bestRank = 0;
		EqCondition bestCondition = null;
		Set<FieldPath> fields = in.getFields();
		boolean baseType = fields.size() > 1;
		for(FieldPath field : fields) {
		    System.out.println("eq considering field " + field);
			if(field.length() == 0 && !baseType)
				// Don't try to find a value to match for the null field if this is not a basic object
				// (that is either a base type or the constant null)
				continue;
			Set<ExpandedObject> vals = union(in.getValues(field), out.getValues(field));
			for(ExpandedObject val : vals) {
			    if(val.getFields().size() > 1)
				// this is a compound object, we will match
				// on sub-components later (better would be to
				// just filter out fieldds that are prefixes
				// of other fields.
				continue;
			    // Check 'field = val'
			    int num_in = in.instancesMatching(field, val);
			    int num_out = out.size() - out.instancesMatching(field, val);
				int rank = getRank(num_in, num_out);
				if(rank > bestRank ||
					(bestCondition != null && rank == bestRank && field.length() < bestCondition.field.length())) {
					bestRank = rank;
					bestCondition = new EqCondition(field,val,false);
				}
				// Check 'field <> val'
				num_in = in.size() - in.instancesMatching(field, val);
				num_out = out.instancesMatching(field, val);
				rank = getRank(num_in, num_out);
				if(rank > bestRank ||
					(bestCondition != null && rank == bestRank && field.length() < bestCondition.field.length())) {
					bestRank = rank;
					bestCondition = new EqCondition(field,val,true);
				}
			}
		}
		System.out.println("Best condition: " + bestCondition);
		return bestCondition;
	}
	
	public int getRank(InstanceSet in, InstanceSet out) {
		int num_in, num_out;
		
		if(!negated) {
			num_in = in.instancesMatching(field, value);
			num_out = out.size() - out.instancesMatching(field, value);
		}
		else {
			num_in = in.size() - in.instancesMatching(field, value);
			num_out = out.instancesMatching(field, value);			
		}
		return super.getRank(num_in, num_out);
	}
	
	@Override
	public String toString() {
		if(!negated) {
			return field.toString() + " == " + value.toString();
		}
		else {
			return field.toString() + " != " + value.toString();
		}
	}
	
	@Override
	public boolean satisfies(ExpandedObject obj) {
		if(!negated) {
			if(value != null)
				return value.equals(obj.getField(field));
			else
				return obj.getField(field) == null;
		}
		else {
			if(value != null)
				return !value.equals(obj.getField(field));
			else
				return !(obj.getField(field) == null);
		}
	}
}
