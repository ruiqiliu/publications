package edu.umd.MatchSnapshots;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import edu.umd.MatchSnapshots.ExpandedObjects.ExpandedObject;
import edu.umd.MatchSnapshots.Synthesis.Example;
import edu.umd.MatchSnapshots.Synthesis.SynthMatching;
import edu.umd.MatchSnapshots.Synthesis.SynthFuncs.SynthSwitch;

public class Matcher {
    private LinkedList<HeapSnapshot> oldSnaps1;
    private LinkedList<HeapSnapshot> oldSnaps2;
    private LinkedList<HeapSnapshot> newSnaps;
    private boolean use_static;

    public static final int NtoN = 1;
    public static final int NtoM = 2;
    public static final int Synthesis = 3;
	
	public Matcher(LinkedList<HeapSnapshot> oldSnaps1, LinkedList<HeapSnapshot> oldSnaps2,
		       LinkedList<HeapSnapshot> newSnaps, boolean use_static) {
		this.oldSnaps1 = oldSnaps1;
		this.oldSnaps2 = oldSnaps2;
		this.newSnaps = newSnaps;
		this.use_static = use_static;
	}

    /* May modify "matching" argument */
    public static Matching doMatch(Matching matching, String targetClass, FieldPath targetField, int match_type) {
	switch (match_type) {
	case NtoN:
		System.out.println("========= Searching for Key Fields =========");
		System.out.println("-------- Performing N-to-N Matching ---------");

		// Copy this so that we can undo the "filter different sized" operation that the N-to-N
		// match does.
		KeyFields kf = new KeyFields(matching, true);
		if(!kf.getKeyFields().isEmpty()) {
			System.out.println("Key fields found:");
			for(FieldPath field : kf.getKeyFields())
				System.out.println("  " + field);
		}
		matching = kf.getMatching();
		System.out.println("Largest EQ class: " + kf.largestEQClass());
		System.out.println("Number of EQ classes: " + matching.numClasses());
		break;

	case NtoM:
		System.out.println("-------- Performing N-to-M Matching ---------");
		kf = new KeyFields(matching, false);
		if(!kf.getKeyFields().isEmpty()) {
			System.out.println("Key fields found:");
			for(FieldPath field : kf.getKeyFields())
				System.out.println("  " + kf);
		}
		matching = kf.getMatching();
		System.out.println("Largest EQ class: " + kf.largestEQClass());
		System.out.println("Number of EQ classes: " + matching.numClasses());
		break;

	case Synthesis:
		System.out.println("-------- Performing Synthesis-based Matching ---------");
		SynthMatching smatcher = new SynthMatching(matching, targetClass, targetField);
		matching = smatcher.getMatching();
		break;
	}
	return matching;
    }

	public static Matching tryAllMatches(Matching initialMatching, String targetClass, FieldPath targetField) {
		Matching unmodifiedMatching = initialMatching.clone();
		Matching matching;
		long time = System.currentTimeMillis();
		matching = doMatch(initialMatching, targetClass, targetField, NtoN);
		time = System.currentTimeMillis() - time;
		System.out.println("NtoN matching time: " + ((double)time)/1000);
		if(matching.allSingleton())
		    return matching;
/*		matching = doMatch(unmodifiedMatching, targetClass, targetField,NtoM);
		System.out.println("finished NtoM matching");
		if(matching.allSingleton())
		return matching;*/
		time = System.currentTimeMillis();
		doMatch(matching, targetClass, targetField, Synthesis);
		time = System.currentTimeMillis() - time;
		System.out.println("Synthesis matching time: " + ((double)time)/1000);
		return matching;
	}
	
	public static List<Example> match(InstanceSet oldObjs1, InstanceSet oldObjs2, InstanceSet newObjs) {
		LinkedList<InstanceSet> oldList1, oldList2, newList;
		oldList1 = new LinkedList<InstanceSet>();
		oldList2 = new LinkedList<InstanceSet>();
		newList = new LinkedList<InstanceSet>();
		oldList1.add(oldObjs1);
		oldList2.add(oldObjs2);
		newList.add(newObjs);
		Matching matching = new Matching(oldList1, oldList2, newList);
		System.out.println("DEBUG: matching.numSnapshots = " + matching.numSnapshots());
		System.out.println("DEBUG: matching.numClasses = " + matching.numClasses());
		String targetClass = null;
		if(oldObjs1.size() > 0) {
			targetClass = newObjs.instances.get(0).getType();
			System.out.println("DEBUG: targetClass = " + targetClass);
		}
		return tryAllMatches(matching, targetClass, null).getExamples();
	}
	
	/**
	 * Matches instances of class targetClass and returns a list of example pairs.
	 * @param targetClass
	 * @param filterEmpty - If true, then only snapshots where old1, old2, and new all contain
	 * instances of targetClass are considered.
	 * @param targetField - The field for which synthesis is being performed.  If non-null, then
	 *   synthesis-based matching will be used in cases where other matching methods fail.
	 * @return
	 */
	public List<Example> match(String targetClass, boolean filterEmpty, FieldPath targetField)
	{
		System.out.println("========= Finding instances of target class =========");
		System.out.println("targetClass = " + targetClass);
		long time = System.currentTimeMillis();
		Matching matching = searchSnapshots(targetClass, filterEmpty);
		time = System.currentTimeMillis() - time;
		System.out.println("filter snapshot time: " + ((double)time)/1000);
		return tryAllMatches(matching, targetClass, targetField).getExamples();
	}
	
	private Matching searchSnapshots(String targetClass, boolean filterEmpty) {
		LinkedList<InstanceSet> oldSets1 = new LinkedList<InstanceSet>();
		LinkedList<InstanceSet> oldSets2 = new LinkedList<InstanceSet>();
		LinkedList<InstanceSet> newSets = new LinkedList<InstanceSet>();
		Iterator<HeapSnapshot> oldIter1 = oldSnaps1.iterator();
		Iterator<HeapSnapshot> oldIter2 = oldSnaps2.iterator();
		Iterator<HeapSnapshot> newIter = newSnaps.iterator();
		InstanceSet oldObjects1 = null, oldObjects2 = null, newObjects = null;
		while(oldIter1.hasNext()) {
		    oldObjects1 = oldIter1.next().getObjectsOfClass(targetClass, use_static);
		    oldObjects2 = oldIter2.next().getObjectsOfClass(targetClass, use_static);
		    newObjects = newIter.next().getObjectsOfClass(targetClass, use_static);
		    if(filterEmpty && (newObjects.isEmpty() || oldObjects1.isEmpty() || oldObjects2.isEmpty())) {
			System.out.println("WARNING: One or more snapshots did not contain instances of the target class.");				
			System.out.println("Snapshot dropped.");				
		    }
		    else {
			oldSets1.add(oldObjects1);
			oldSets2.add(oldObjects2);
			newSets.add(newObjects);
		    }
		}
		
		if(oldSets1.isEmpty() && oldSets2.isEmpty() && newSets.isEmpty()) {
			System.out.println("ERROR: No snapshots contain class " + targetClass);
			System.exit(-1);
		}
		else {
			System.out.println("Snapshots containing instances of target class: " + oldSets1.size());
			System.out.println("First non-empty snapshot contains " + oldSets1.getFirst().size() + " instances.");
		}
		
		return new Matching(oldSets1, oldSets2, newSets);
	}
}
