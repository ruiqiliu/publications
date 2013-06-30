package edu.umd.MatchSnapshots.Synthesis.SynthFuncs;

import java.util.LinkedList;
import java.util.List;

import edu.umd.MatchSnapshots.FieldPath;
import edu.umd.MatchSnapshots.ExpandedObjects.ExpandedObject;
import edu.umd.MatchSnapshots.ExpandedObjects.GenericObject;
import edu.umd.MatchSnapshots.Synthesis.Example;

/** This class partitions the examples into separate classes that can each be covered by a
 *  transformer function.  It does not try to find an optimal covering however. */
public class SynthPartition extends SynthFun {
	private List<SynthFun> partitions;
	
	private SynthPartition(String className, FieldPath field, List<SynthFun> partitions) {
		super(className, field);
		this.partitions = partitions;
	}
	
	public List<SynthFun> getPartitions() {
		return partitions;
	}
	
	public static SynthPartition getSynthFun(String className, FieldPath field, Example ex) {
		SynthFun f = SynthSet.getSynthFun(className, field, ex);
		if(f == null)  return null;
		List<SynthFun> partitions = new LinkedList<SynthFun>();
		partitions.add(f);
		return new SynthPartition(className, field, partitions);
	}
	
	public int numPartitions() {
		return partitions.size();
	}
	
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("--------\n");
		for(SynthFun f : partitions) {
			result.append(f.toString());
			result.append("\n--------\n");
		}
		return result.toString();
	}
	
	public SynthPartition expand(Example ex) {
		for(SynthFun f : partitions) {
			if(f.consistentWith(ex)) {
				f.refine(ex);
				return this;
			}
		}
		// Create a new partition
		SynthFun f = SynthSet.getSynthFun(className, field, ex);
		if(f != null)
			partitions.add(f);
		else {
			System.out.println("Could not synthesize an update for field " + field);
			System.out.println("Field value = " + ex.getNew().getField(field));
		}
		return this;
	}
	
	@Override
	public int size() {
		int i = 1;
		for(SynthFun f : partitions)
			i *= f.size();
		return i;		
	}
	
	@Override
	public boolean consistentWith(Example ex) {
		for(SynthFun f : partitions) {
			if(f.consistentWith(ex))
				return true;
		}
		return false;
	}
}
