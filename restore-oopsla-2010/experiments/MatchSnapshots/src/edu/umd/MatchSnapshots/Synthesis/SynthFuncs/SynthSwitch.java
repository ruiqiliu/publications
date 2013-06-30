package edu.umd.MatchSnapshots.Synthesis.SynthFuncs;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.umd.MatchSnapshots.FieldPath;
import edu.umd.MatchSnapshots.InstanceSet;
import edu.umd.MatchSnapshots.Pair;
import edu.umd.MatchSnapshots.Conditions.Condition;
import edu.umd.MatchSnapshots.ExpandedObjects.GenericObject;
import edu.umd.MatchSnapshots.Synthesis.Example;

/**
 * The top-level synthesis routine for individual fields.  Synthesizes a switch statement.
 * @author smagill
 *
 */
public class SynthSwitch extends SynthFun {
	private List<SynthFun> branches;
	private List<InstanceSet> instances;
	private List<Condition> conditions;
	
	private SynthSwitch(String className, FieldPath field) {
		super(className, field);
		branches = new LinkedList<SynthFun>();
		instances = new LinkedList<InstanceSet>();
		conditions = new LinkedList<Condition>();
	}
	
	private void addPartition(SynthFun f, Example ex) {
		branches.add(f);
		InstanceSet set = new InstanceSet();
		set.addInstance(ex.getOld());
		instances.add(set);		
	}
	
	public static SynthSwitch getSynthFun(String className, FieldPath field, Example ex) {
		SynthFun f = SynthSet.getSynthFun(className, field, ex);
		if(f == null)  return null;
		SynthSwitch result = new SynthSwitch(className, field);
		result.addPartition(f, ex);
		return result;
	}
	
	public int numPartitions() {
		return branches.size();
	}
	
	public String toString() {
		if(conditions.size() == 0)
			inferConditions();
		StringBuffer result = new StringBuffer();
		Iterator<SynthFun> fIter = branches.iterator();
		Iterator<Condition> cIter = conditions.iterator();
		if(branches.size() > 1) {
			while(fIter.hasNext()) {
				SynthFun f = fIter.next();
				Condition c = cIter.next();
				result.append("if(" + c.toString() + ") {\n");
				result.append("  " + indent(f.toString()) + "\n");
				result.append("}\n");
			}
		}
		else {
			SynthFun f = fIter.next();
			result.append(f.toString());
		}
		return result.toString();
	}
	
	public SynthSwitch refine(Example ex) {
		int i = 0;
		for(SynthFun f : branches) {
			if(f.consistentWith(ex)) {
				f.refine(ex);
				InstanceSet set = instances.get(i);
				set.addInstance(ex.getOld());
				return this;
			}
			i++;
		}
		// No branches were consistent with this example.
		return null;
	}
	
	public SynthSwitch expand(Example ex) {
		int i = 0;
		for(SynthFun f : branches) {
			if(f.consistentWith(ex)) {
				f.refine(ex);
				InstanceSet set = instances.get(i);
				set.addInstance(ex.getOld());
				System.out.println("Consistent with existing.");
				return this;
			}
			i++;
		}
		// No branches were consistent with this example.
		// Create a new partition
		SynthFun f = SynthSet.getSynthFun(className, field, ex);
		if(f != null) {
		    System.out.println("Adding new branch.");
		    addPartition(f,ex);
		}
		else {
			System.out.println("Could not synthesize an update for field " + field);
			System.out.println("Field value = " + ex.getNew().getField(field));
			return null;
		}
		return this;
	}
	
	@Override
	public int size() {
		int i = 1;
		for(SynthFun f : branches)
			i *= f.size();
		return i;
	}
	
	public void inferConditions() {
		InstanceSet in = new InstanceSet();
		InstanceSet out = new InstanceSet();
		
		// Add everything to 'out'
		for(InstanceSet set : instances)
			out.addAll(set);
		
		// Compute conditions for each partition
		for(InstanceSet set : instances) {
			in.addAll(set);
			out.removeAll(set);
			conditions.add(Condition.getCondition(in, out));
			in.removeAll(set);
			out.addAll(set);
		}
	}
	
	@Override
	public boolean consistentWith(Example ex) {
		for(SynthFun f : branches)
			if(f.consistentWith(ex))
				return true;
		return false;
	}
}
