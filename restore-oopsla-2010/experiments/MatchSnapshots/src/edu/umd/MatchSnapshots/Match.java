package edu.umd.MatchSnapshots;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import edu.umd.MatchSnapshots.ExpandedObjects.ExpandedObject;
import edu.umd.MatchSnapshots.Synthesis.Example;
import edu.umd.MatchSnapshots.Synthesis.SynthFuncs.SynthSwitch;

public class Match {
    public static void match(LinkedList<HeapSnapshot> oldSnaps1, LinkedList<HeapSnapshot> oldSnaps2,
			     LinkedList<HeapSnapshot> newSnaps, String targetClass, boolean use_static)
	{
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
		    if(!newObjects.isEmpty()) {
			oldSets1.add(oldObjects1);
			oldSets2.add(oldObjects2);
			newSets.add(newObjects);
		    }
		    else {
			System.out.println("WARNING: One or more snapshots did not contain instances of the target class.");				
		    }
		}
		
		if(oldSets1.isEmpty() || oldSets2.isEmpty() || newSets.isEmpty()) {
			System.out.println("ERROR: No snapshots contain class " + targetClass);
			System.exit(-1);
		}
		else {
			System.out.println("Snapshots containing instances of target class: " + oldSets1.size());
			System.out.println("First non-empty snapshot contains " + oldSets1.getFirst().size() + " instances.");
		}

		System.out.println("========= Searching for Key Fields =========");
		System.out.println("-------- Old-Old Matching ---------");

		/* Commented out since I believe this class is no longer necessary.
		Matching m = new Matching(oldSets1,oldSets2,newSets);
		KeyFields kf = new KeyFields(m,true);
		Set<FieldPath> keyfields = kf.getKeyFields();
		System.out.println("Old-Old Key Fields:");
		for(FieldPath field : keyfields)
			System.out.println("  " + field);
		System.out.println(kf.getN() + "-to-" + kf.getN() + " mapping found.");
		
		if(keyfields.isEmpty() || kf.getN() > 2) {
			System.out.println("Key-field-based mapping was inadequate. Trying synthesis-based mapping.");
		}
		
		FieldPath f = newSets.getFirst().instances.get(0).getFields().get(0);
		SynthMatching(m, targetClass, f)*/
	}
}
