package edu.umd.MatchSnapshots;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import edu.umd.MatchSnapshots.ExpandedObjects.ExpandedObject;
import edu.umd.MatchSnapshots.Synthesis.Example;

/**
 * Stores a matching between old and new snapshot lists.  Conceptually, this looks like the following:
 *   ============
 *    Snapshot 1
 *   ============
 *    old    new _
 *     O1     O2  \___ Equivalence class 1
 *     O3     O4 _/
 *    old    new
 *     O5     O6 - Equivalence class 2
 *    old    new  _
 *     O8     O9   \
 *     O10    O11   |--- Equivalence class 3
 *     O12        _/
 *   ============
 *    Snapshot 2
 *   ============
 *    ...
 *    
 *  We store the old and new columns separately.  Each is a list of lists of sets.
 *    Outer list - Snapshots
 *    Inner list - Equivalence classes
 *    Set - Elements of equivalence class
 *  The two list-of-lists match up, such that objects in oldSeries[i][j] are matched with
 *  objects in newSeries[i][j].  The old and new equivalence classes need not contain the same
 *  number of objects.  The actual implementation keeps two "old" columns in order to facilitate
 *  old-old matching.
 * @author smagill
 *
 */
public class Matching {
	LinkedList<LinkedList<InstanceSet>> oldSeries1;
	LinkedList<LinkedList<InstanceSet>> oldSeries2;
	LinkedList<LinkedList<InstanceSet>> newSeries;

	/**
	 * Similar to an iterator, but it allows the current element to be accessed multiple times (versus
	 * iterators' "next()" method, which requires the output to be saved.  Starts at position 0, so
	 * getOld1(), getOld2(), and getNew() can all be called immediately after construction.  advance()
	 * then advances the cursor to position 1.
	 * @author smagill
	 *
	 */
	public class Cursor {
		private ListIterator<LinkedList<InstanceSet>> oldSeriesIter1;
		private ListIterator<LinkedList<InstanceSet>> oldSeriesIter2;
		private ListIterator<LinkedList<InstanceSet>> newSeriesIter;
		
		private LinkedList<InstanceSet> currOldSeries1;
		private LinkedList<InstanceSet> currOldSeries2;
		private LinkedList<InstanceSet> currNewSeries;
		
		private ListIterator<InstanceSet> oldIter1;
		private ListIterator<InstanceSet> oldIter2;
		private ListIterator<InstanceSet> newIter;
		
		private InstanceSet currOld1;
		private InstanceSet currOld2;
		private InstanceSet currNew;
		
		private boolean valid = true;
		
		private Cursor() {
			oldSeriesIter1 = oldSeries1.listIterator();
			oldSeriesIter2 = oldSeries2.listIterator();
			newSeriesIter = newSeries.listIterator();
			if(hasNextSnapshot())
				advanceSnapshot();
			else { // handle the empty case
				valid = false;
				return;
			}
			advance();
		}
		
		private void advanceSnapshot() {
			currOldSeries1 = oldSeriesIter1.next();
			currOldSeries2 = oldSeriesIter2.next();
			currNewSeries = newSeriesIter.next();
			oldIter1 = currOldSeries1.listIterator();
			oldIter2 = currOldSeries2.listIterator();
			newIter = currNewSeries.listIterator();			
		}
		
		private boolean hasNextSnapshot() {
			return newSeriesIter.hasNext();			
		}

		/**
		 * Returns 'true' if the cursor is still within the structure.  Returns 'false' if the cursor has
		 * moved past the end.  isValid() == true implies that get*() will return sensible results.
		 * @return
		 */
		public boolean isValid() {
			return valid;
		}
		
		public void advance() {
			if(newIter.hasNext()) {
				currOld1 = oldIter1.next();
				currOld2 = oldIter2.next();
				currNew = newIter.next();
			}
			else if(hasNextSnapshot()) {
				advanceSnapshot();
				advance();
			}
			else {
				valid = false;
			}
		}
		
		// Returns the number of snapshots.
		public int numSnapshots() {
			return oldSeries1.size();
		}
		
		/**
		 * Removes the equivalence classes at the current cursor location.  If the current snapshot
		 * becomes empty (contains no equivalence classes), it is also removed.
		 */
		public void remove() {
			oldIter1.remove();
			oldIter2.remove();
			newIter.remove();
			if(currNewSeries.isEmpty()) {
				oldSeriesIter1.remove();
				oldSeriesIter2.remove();
				newSeriesIter.remove();
				if(hasNextSnapshot())
					advanceSnapshot();
				else
					valid = false;
			}
			advance();
		}
		
		/**
		 * Advances to the next location where old1, old2, and new are all non-empty sets.
		 */
		public void advanceNonEmpty() {
			if(isValid()) {
				advance();
				if(currOld1.isEmpty() || currOld2.isEmpty() || currNew.isEmpty())
					advanceNonEmpty();
			}
		}
		
		public InstanceSet getOld1() {
			return currOld1;
		}
		public InstanceSet getOld2() {
			return currOld2;
		}
		public InstanceSet getNew() {
			return currNew;
		}
		
		/** 
		 * Splits the current old1, old2, and new InstanceSets into the given subsets.
		 */
		public void splitAs(List<InstanceSet> oldSets1, List<InstanceSet> oldSets2, List<InstanceSet> newSets) {
			oldIter1.remove();
			for(InstanceSet set : oldSets1)
				oldIter1.add(set);
			oldIter2.remove();
			for(InstanceSet set : oldSets2)
				oldIter2.add(set);
			newIter.remove();
			for(InstanceSet set : newSets)
				newIter.add(set);
		}
	}
	
	public Matching(LinkedList<InstanceSet> oldSnapshots1, LinkedList<InstanceSet> oldSnapshots2,
			LinkedList<InstanceSet> newSnapshots) {
		oldSeries1 = new LinkedList<LinkedList<InstanceSet>>();
		oldSeries1.add(oldSnapshots1);
		oldSeries2 = new LinkedList<LinkedList<InstanceSet>>();
		oldSeries2.add(oldSnapshots2);
		newSeries = new LinkedList<LinkedList<InstanceSet>>();
		newSeries.add(newSnapshots);
	}
	
	/**
	 * Returns the number of snapshots.  This method is fast (no traversal required).
	 * @return
	 */
	public int numSnapshots() {
		return newSeries.size();
	}
	
	private static ExpandedObject getFirstExample(List<ExpandedObject> lst) {
		if(lst.size() == 0)
			return null;
		else
			return lst.get(0);
	}
	
	public List<Example> getExamples() {
		LinkedList<Example> result = new LinkedList<Example>();
		Cursor cur = getCursor();
		while(cur.isValid()) {
			result.add(new Example(getFirstExample(cur.getOld1().instances),
					getFirstExample(cur.getOld2().instances),
					getFirstExample(cur.getNew().instances)));
			cur.advance();
		}
		return result;
	}
	
	private Matching() {
		oldSeries1 = new LinkedList<LinkedList<InstanceSet>>();
		oldSeries2 = new LinkedList<LinkedList<InstanceSet>>();
		newSeries = new LinkedList<LinkedList<InstanceSet>>();
	}
	
	@SuppressWarnings("unchecked")
	public Matching clone() {
		Matching result = new Matching();
		ListIterator<LinkedList<InstanceSet>> oldSnapshotIter1 = oldSeries1.listIterator();
		ListIterator<LinkedList<InstanceSet>> oldSnapshotIter2 = oldSeries2.listIterator();
		ListIterator<LinkedList<InstanceSet>> newSnapshotIter = newSeries.listIterator();
		while(oldSnapshotIter1.hasNext()) {
			result.oldSeries1.add((LinkedList<InstanceSet>)oldSnapshotIter1.next().clone());
			result.oldSeries2.add((LinkedList<InstanceSet>)oldSnapshotIter2.next().clone());
			result.newSeries.add((LinkedList<InstanceSet>)newSnapshotIter.next().clone());
		}
		return result;
	}
	
	public Cursor getCursor() {
		return new Cursor();
	}

	/** Splits each equivalence class into N new classes, where N is the number of values taken on by
	 *  'field' in that class.  In each new class, all objects will agree on the value of 'field'.
	 *  Returns true if a split actually occurred. */
	public boolean splitOnField(FieldPath field) {
	    boolean split_happened = false;
		Cursor cur = getCursor();
		
		System.out.println("Splitting on field " + field);
		while(cur.isValid()) {
			InstanceSet valueSource = cur.getOld1();
			if(valueSource.isEmpty())
				valueSource = cur.getOld2();
			if(valueSource.isEmpty())
				valueSource = cur.getNew();
			if(valueSource.isEmpty()) {
				cur.advance();
				continue;
			}
			Set<ExpandedObject> values = valueSource.getValues(field);
			if(values.size() == 1) {
				//System.out.println("no effect");
				cur.advance();
			}
			else {
			    split_happened = true;
			    System.out.println(values.size() + " values to split.");
				LinkedList<InstanceSet> oldPartition1 = new LinkedList<InstanceSet>();
				LinkedList<InstanceSet> oldPartition2 = new LinkedList<InstanceSet>();
				LinkedList<InstanceSet> newPartition = new LinkedList<InstanceSet>();
				for(ExpandedObject value : valueSource.getValues(field)) {
					Set<ExpandedObject> instances1 = cur.getOld1().getInstancesMatching(field, value);
					Set<ExpandedObject> instances2 = cur.getOld2().getInstancesMatching(field, value);
					Set<ExpandedObject> instances3 = cur.getNew().getInstancesMatching(field, value);
					
					oldPartition1.add(new InstanceSet(instances1));
					oldPartition2.add(new InstanceSet(instances2));
					newPartition.add(new InstanceSet(instances3));
				}
				cur.splitAs(oldPartition1, oldPartition2, newPartition);
				cur.advance();
			}
		}
		return split_happened;
	}
	
	/**
	 * Returns the full size of the collections (number of equivalences classes in all snapshots).
	 * This requires a full traversal of the data structure.
	 * @return
	 */
	public int numClasses() {
		int size = 0;
		Cursor cur = getCursor();
		while(cur.isValid()) {
			size++;
			cur.advance();
		}
		return size;
	}

    /**
       Returns true if all equivalence classes are singletons (to accommodate
       N-to-M matching, it also returns true if an equivalence class contains
       one old object but multiple new objects (or vice-versa)).
     */
    public boolean allSingleton() {
	Cursor cur = getCursor();
	while(cur.isValid()) {
	    if(cur.getOld1().size() != 1 && cur.getOld2().size() != 1
	       && cur.getNew().size() != 1)
		return false;
	    cur.advance();
	}
	return true;
    }
}
