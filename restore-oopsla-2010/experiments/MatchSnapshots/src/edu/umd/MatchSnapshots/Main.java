package edu.umd.MatchSnapshots;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import edu.umd.MatchSnapshots.ExpandedObjects.ExpandedObject;
import edu.umd.MatchSnapshots.ExpandedObjects.GenericObject;
import edu.umd.MatchSnapshots.Synthesis.Example;
import edu.umd.MatchSnapshots.Synthesis.SynthFuncs.SynthPartition;
import edu.umd.MatchSnapshots.Synthesis.SynthFuncs.SynthSwitch;

public class Main {
	public static void main(String[] args) {
		String newSnapFile, oldSnapFile, targetClass;
		boolean use_statics = false;
		
		if(args.length < 5) {
			System.out.println("Usage: main old_snapshot_dir1 old_snapshot_dir2 new_snapshot_dir classname fieldname");
			System.exit(-1);
		}

		File old_dir1 = new File(args[0]);
		File old_dir2 = new File(args[1]);
		File new_dir = new File(args[2]);
		FieldPath synthField = new FieldPath(args[4]);
		if(args.length == 6)
		    if(args[5].equals("-static")) {
			use_statics = true;
			System.out.println("Using static fields.");
		    }
		System.out.println("synthField = " + synthField);
		targetClass = args[3];
		File[] old_snapshots1 = old_dir1.listFiles();
		File[] old_snapshots2 = old_dir2.listFiles();
		File[] new_snapshots = new_dir.listFiles();
		if(old_snapshots1.length != old_snapshots2.length
				|| old_snapshots1.length != new_snapshots.length)
		{
			System.out.println("Directories contain unequal numbers of snapshot files.");
			System.exit(-1);
		}
		int num_snapshots = old_snapshots1.length;
		assert(num_snapshots == new_snapshots.length);
		
		System.out.println("Loading heap snapshots...");
		LinkedList<HeapSnapshot> oldSnaps1 = new LinkedList<HeapSnapshot>();
		LinkedList<HeapSnapshot> oldSnaps2 = new LinkedList<HeapSnapshot>();
		LinkedList<HeapSnapshot> newSnaps = new LinkedList<HeapSnapshot>();
		for(int i = 0 ; i < num_snapshots ; i++) {
			System.out.println("Loading old snapshot 1...");
			HeapSnapshot snap = new HeapSnapshot(old_snapshots1[i]);
			oldSnaps1.add(snap);
			System.out.println("Loading old snapshot 2...");
			snap = new HeapSnapshot(old_snapshots2[i]);
			oldSnaps2.add(snap);
			System.out.println("Loading new snapshot...");
			snap = new HeapSnapshot(new_snapshots[i]);
			newSnaps.add(snap);
		}

		Stats stats = new Stats(oldSnaps1, oldSnaps2, newSnaps, targetClass);
		stats.printStats();
		
		/* TODO: resurrect this code at some point
		if(newSets.getFirst().getFields().contains(new FieldPath(synthField))) {
			System.out.println("Field is present in examples.");
		}
		else {
			System.out.println("Field is not present in examples.");
			System.out.println("Field list:");
			System.out.println(newSets.getFirst().getFields().toString());
			System.exit(-1);
		}
		*/
		
		long start = System.currentTimeMillis();
		Matcher matcher = new Matcher(oldSnaps1, oldSnaps2, newSnaps, use_statics);
		List<Example> exList = matcher.match(targetClass, true, synthField);
		long match_time = System.currentTimeMillis() - start;

		System.out.println("Synthesizing update for field: " + synthField);
		SynthSwitch part = null;
		System.out.println("Number of example pairs: " + exList.size());

		Example.examplesToHTML(exList, "examples.html");
		
		// Remove example pairs not containing the field.
		System.out.println("Removing examples not containing field " + synthField);
		Iterator<Example> iter = exList.iterator();
		while(iter.hasNext()) {
			Example ex = iter.next();
			if(ex.getOld() == null ||
			   ex.getNew() == null ||
			   /* !ex.getOld().containsField(synthField) ||*/
			   !ex.getNew().containsField(synthField))
				iter.remove();
		}
		System.out.println("Number of example pairs: " + exList.size());

		start = System.currentTimeMillis();
		int exNum = 0;
		for(Example ex : exList) {
			System.out.println("Example " + exNum++ + "\n");
			if(part == null)
				part = SynthSwitch.getSynthFun(targetClass, synthField, ex);
			else
				part.expand(ex);
		}
		System.out.print("Number of partitions: ");
		if(part != null) {
			System.out.println(part.numPartitions());
			System.out.println("Synthesized function:");
			System.out.println("type = " + part.getClass());
			System.out.println(part);
		}
		else
			System.out.println("Synthesis failed.");
		long synth_time = System.currentTimeMillis() - start;
		System.out.println("Match time: " + ((double)match_time)/1000);
		System.out.println("Synth time: " + ((double)synth_time)/1000);
	}

	static ExpandedObject findMinDiff(ExpandedObject o1, List<ExpandedObject> other) {
		ExpandedObject minObj = null;
		int minDiff = Integer.MAX_VALUE;
		
		for(ExpandedObject o2 : other) {
			int diff = o1.diff(o2);
			if(diff < minDiff) {
				minDiff = diff;
				minObj = o2;
			}
		}
		
		return minObj;
	}

    static class Stats {
	private int numSnapshots = 0;
	private int numClasses = 0;
	private int numObjects = 0;
	private int sameNumObjects = 0;
	private int sameNumClasses = 0;
	private double avgFieldsPerSameNumObject = 0.0;
	private String targetClass = "";
	private int fieldsInTargetClass = 0;
	private int objectsOfTargetClass = 0;
	Stats(LinkedList<HeapSnapshot> oldsnaps1,
	      LinkedList<HeapSnapshot> oldsnaps2,
	      LinkedList<HeapSnapshot> newsnaps,
	      String targetClass) {
	    int sameNumFields = 0;
	    for(int i = 0; i < oldsnaps1.size(); i++) {
		numSnapshots++;
		System.out.println("i = " + i);
		this.targetClass = targetClass;
		LinkedList<String> classes = oldsnaps1.get(i).getClasses();
		if( classes.size() > numClasses )
		    numClasses = classes.size();
		for(String cl : classes) {
		    if(cl.equals("java.lang.Object"))
			continue;
		    System.out.println("cl = " + cl);
		    int oldObjCount = oldsnaps1.get(i).countObjectsOfClass(cl);
		    int newObjCount = newsnaps.get(i).countObjectsOfClass(cl);
		    ExpandedObject obj = oldsnaps1.get(i).getFirstObjectOfClass(cl);
		    numObjects += oldObjCount;
		    if(oldObjCount != 0 && obj != null && oldObjCount == newObjCount) {
			int fields = obj.getFields().size();
			if(fields > 0) {
			    sameNumClasses++;
			    sameNumObjects += oldObjCount;
			    sameNumFields += fields;
			}
		    }
		    
		    if(cl.equals(targetClass)) {
			if(fieldsInTargetClass == 0 && obj != null)
			    fieldsInTargetClass = obj.getFields().size();
			objectsOfTargetClass += oldObjCount;
		    }
		}
	    }
	    avgFieldsPerSameNumObject = ((double)sameNumFields) / ((double)sameNumClasses);
	    sameNumClasses /= numSnapshots;
	}

	public void printStats() {
	    System.out.println("---- Stats ----");
	    System.out.println("numSnapshots = " + numSnapshots);
	    System.out.println("numClasses = " + numClasses);
	    System.out.println("numObjects = " + numObjects);
	    System.out.println("sameNumClasses = " + sameNumClasses);
	    System.out.println("sameNumObjects = " + sameNumObjects);
	    System.out.println("avgFieldsPerSameNumObject = " + avgFieldsPerSameNumObject);
	    System.out.println("targetClass = " + targetClass);
	    System.out.println("fieldsInTargetClass = " + fieldsInTargetClass);
	    System.out.println("objectsOfTargetClass = " + objectsOfTargetClass);
	}
    }
}
