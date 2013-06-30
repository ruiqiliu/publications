package edu.umd.MatchSnapshots;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import edu.umd.MatchSnapshots.KeyFieldRankings.FieldRankPairing;
import edu.umd.MatchSnapshots.ExpandedObjects.ExpandedObject;
import edu.umd.MatchSnapshots.ExpandedObjects.StringObject;
import edu.umd.MatchSnapshots.Synthesis.Example;

/** Takes three lists of instance sets and finds the key fields that induce the best
 *  matching between corresponding sets.  The assumption is that the ith element of
 *  oldSets1 / oldSets2 corresponds to an equivalent point in the execution as the
 *  ith element of newSets. */
public class KeyFields {
	Matching matching;
	Set<FieldPath> keyfields;
	int n_to_n = 0;
	boolean tryNtoN = true;
	HashMap<Relation<ExpandedObject>,Integer> relationSizes;
	
	@SuppressWarnings("unchecked")
	public KeyFields(Matching matching, boolean requireNtoN) {
		if(matching.numSnapshots() == 0)
			System.out.println("DEBUG: no snapshots");
		this.matching = matching.clone();
		if(this.matching.numSnapshots() == 0)
			System.out.println("DEBUG: this.matching no snapshots");
		if(this.matching.numClasses() == 0)
			System.out.println("DEBUG: this.matching no EQ Classes");
		keyfields = new HashSet<FieldPath>();
		relationSizes = new HashMap<Relation<ExpandedObject>, Integer>();
		tryNtoN = requireNtoN;
		if(tryNtoN)
			filterDifferentSized();
		if(this.matching.numSnapshots() == 0)
			System.out.println("DEBUG: this.matching no snapshots2");
		computeKeyFields();
	}
	
	public Set<FieldPath> getKeyFields() {
		return keyfields;
	}
	
	/** Returns the size of the largest equivalence class in the partition induced by the key fields. */
	public int largestEQClass() {
		return n_to_n;
	}
	
	public List<Example> getExamples() {
		Matching.Cursor cur = matching.getCursor();
		LinkedList<Example> result = new LinkedList<Example>();
		
		while(cur.isValid()) {
			ExpandedObject o1 = cur.getOld1().instances.get(0);
			ExpandedObject o2 = cur.getNew().instances.get(0);
			Example ex = new Example(o1, o2);
			result.add(ex);
		}
		return result;
	}
	
	public Matching getMatching() {
		return matching;
	}
	
	/** Removes those snapshots that have different numbers of objects. */
	public void filterDifferentSized() {
		Matching.Cursor cur = matching.getCursor();

		System.out.println("Removing different-sized snapshots...");
		
		while(cur.isValid()) {
			int n1 = cur.getOld1().size();
			int n2 = cur.getOld2().size();
			int n3 = cur.getNew().size();
			if(n1 != n2 || n1 != n3)
				cur.remove();
			else
				cur.advance();
		}
		System.out.println("Snapshots left: " + matching.numSnapshots());
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

	static Set<ExpandedObject> union3(Set<ExpandedObject> set1, Set<ExpandedObject> set2,
			Set<ExpandedObject> set3) {
		Set<ExpandedObject> unioned =
			new HashSet<ExpandedObject>((int)(set1.size() * set2.size() * set3.size() * 1.2));
		for(ExpandedObject val : set1)
			unioned.add(val);
		for(ExpandedObject val : set2)
			unioned.add(val);
		for(ExpandedObject val : set3)
			unioned.add(val);
		return unioned;
	}

	/** Given: old-version instance set (objects1)
	 *         old-version instance set (objects2)
	 *         new-version instance set (objects3)
	 * Assumption: objects1 and objects2 are from corresponding points in two separate
	 *             program runs
	 * Returns: a list of key field rankings that records, for each field,
	 *          the number of N-to-N matchings it introduces. 
	 *          */
	private static KeyFieldRankings
	orderByNtoN(Matching.Cursor cur) {
		KeyFieldRankings rankings = new KeyFieldRankings(1);
		for(FieldPath field : cur.getOld1().getFields()) {
			// Keep track of how many N-to-N matchings this field enables.
			Set<ExpandedObject> values =
				union3(cur.getOld1().getValues(field), cur.getOld2().getValues(field),
						cur.getNew().getValues(field));
			for(ExpandedObject val : values) {
				int n1 = cur.getOld1().instancesMatching(field, val);
				int n2 = cur.getOld2().instancesMatching(field, val);
				int n3 = cur.getNew().instancesMatching(field, val);
				if(n1 == n2 && n2 == n3)
					rankings.increment(field,0);
			}
		}
		return rankings;	
	}
	
	public KeyFieldRankings getNtoNRanking() {
		KeyFieldRankings rankings = null;
		Matching.Cursor cur = matching.getCursor();
	  // tracks the number of equivalence classes so that we know whether a given key field
		// "makes progress" (splits at least one equivalence class.
		int numEQClasses = 0;
		
		while(cur.isValid()) {
			numEQClasses++;
			KeyFieldRankings snapshotRankings = orderByNtoN(cur);
			if(rankings == null) rankings = snapshotRankings;
			else rankings.addIfNotZero(snapshotRankings);
			cur.advance();
		}
		
		if(rankings == null)
			return new KeyFieldRankings(0); // interpreted later as a failure

		System.out.println("Rankings before removing non-N-to-N:");
		System.out.println(rankings.toString());
		
		/* remove fields that are not N-to-N or do not split any sets */
		Iterator<Map.Entry<FieldPath, KeyFieldRank>> iter = rankings.map.entrySet().iterator();
		while(iter.hasNext()) {
			Map.Entry<FieldPath, KeyFieldRank> entry = iter.next();
			if(entry.getValue().rank.get(0) == 0 ||
				 entry.getValue().rank.get(0) <= numEQClasses) {
				System.out.println("Removing " + entry.getKey());
				iter.remove();
			}
		}

		return rankings;
	}

	public KeyFieldRankings getNtoMRanking() {
		KeyFieldRankings rankings = null;
		Matching.Cursor cur = matching.getCursor();

		while(cur.isValid()) {
			KeyFieldRankings snapshotRankings = orderByNtoM(cur);
			if(rankings == null) rankings = snapshotRankings;
			else rankings.add(snapshotRankings);
			cur.advance();
		}
		
		return rankings;
	}

	private FieldPath getBestKeyField(KeyFieldRankings rankings) {
		List<KeyFieldRankings.FieldRankPairing> fields = rankings.getSorted();
		System.out.println("Finding best keyfield");
		for(KeyFieldRankings.FieldRankPairing field_pair : fields) {
		    if(!field_pair.field.isImplicit()) {
			System.out.println(field_pair.field);			
			return field_pair.field;
		    }
		}
		return null;
	}
	
	public void computeKeyFields() {
		if(tryNtoN)
			computeKeyFieldsNtoN();
		else
			computeKeyFieldsNtoM();
	}
	
	public void computeKeyFieldsNtoM() {
		keyfields.clear();
		n_to_n = 0;
		
		while(n_to_n != 1) {
			KeyFieldRankings rankings = getNtoMRanking();
			if(rankings == null)
				return;
			List<FieldRankPairing> sorted = rankings.getSorted();
			if(sorted.isEmpty())
				return;
			System.out.println("Sorted Rankings:");
			for(FieldRankPairing pair : sorted)
				System.out.println(pair);
			FieldRankPairing keyfield = sorted.get(0);
			if(keyfields.contains(keyfield.field) || keyfield.rank.isZero())
				return;
			System.out.println("Chose key field: " + keyfield.field);
			System.out.println("Rank: " + keyfield.rank);
			matching.splitOnField(keyfield.field);
			keyfields.add(keyfield.field);
			
			// Determine whether we are one-to-one yet
			n_to_n = 0;
			Matching.Cursor cur = matching.getCursor();
			while(cur.isValid()) {
				if(cur.getOld1().size() > n_to_n)
					n_to_n = cur.getOld1().size();
				cur.advance();
			}
		}
	}
	
	public void computeKeyFieldsNtoN() {
		keyfields.clear();
		n_to_n = 0;
		
		// Determine whether we already one-to-one (happens if there is only one instance of the
		// class in each snapshot
		n_to_n = 0;
		Matching.Cursor cur = matching.getCursor();
		if(matching.numSnapshots() == 0)
			System.out.println("DEBUG: this.matching no snapshots3");
		if(this.matching.numClasses() == 0)
			System.out.println("DEBUG: this.matching no EQ Classes");
		while(cur.isValid()) {
			if(cur.getOld1().size() > n_to_n)
				n_to_n = cur.getOld1().size();
			cur.advance();
		}
		while(n_to_n != 1) {
		    System.out.println("DEBUG:  n_to_n = " + n_to_n);
			KeyFieldRankings rankings = getNtoNRanking();
			System.out.println("Rankings:");
			System.out.println(rankings.toString());
			FieldPath keyfield = getBestKeyField(rankings);
			if(keyfield == null)
				return;
			if(matching.splitOnField(keyfield)) {
			    keyfields.add(keyfield);
			}

			// Determine whether we are one-to-one yet
			n_to_n = 0;
			cur = matching.getCursor();
			while(cur.isValid()) {
				if(cur.getOld1().size() > n_to_n)
					n_to_n = cur.getOld1().size();
				cur.advance();
			}
		}
	}
	
	/** Given: old or new-version instance set (objects1)
	 *         old or new-version instance set (objects2)
	 *         key field rankings (ranking)
	 * Assumption: objects1 and objects2 are from corresponding points in two separate
	 *             program runs
	 * Result: Scales the rankings in 'ranking' by the number of other fields that induce
	 *         the SAME matching. */
	public static int consistency(InstanceSet objects1, InstanceSet objects2, FieldPath f)
	{
		Relation<ExpandedObject> rel1 = getRelation(f, objects1, objects2);
		int result = 1;
		Set<FieldPath> fields = objects1.getFields();
		for(FieldPath field2 : fields) {
			Relation<ExpandedObject> rel2 = getRelation(field2, objects1, objects2);
			if(rel1.equals(rel2))
				result++;
		}
		return result;
	}
	
	/** Returns the relation induced by eq_f for the given field f. */
	public static Relation<ExpandedObject> getRelation(FieldPath f,
			InstanceSet oldObjects, InstanceSet newObjects) {
		Set<ExpandedObject> values =
			union(oldObjects.getValues(f), newObjects.getValues(f));
		Relation<ExpandedObject> rel = new Relation<ExpandedObject>();
		for(ExpandedObject val : values) {
			for(ExpandedObject o1 : oldObjects.getInstancesMatching(f, val))
				for(ExpandedObject o2 : newObjects.getInstancesMatching(f, val))
					rel.relate(o1, o2);
		}
		return rel;
	}
	
	/** Given: old-version instance set (oldObjects)
	 *         old-version instance set (newObjects)
	 * Assumption: oldObjects and newObjects are from corresponding points in two separate
	 *             program runs
	 * Returns: a list of key field rankings that associates a vector with each field such
	 *          that if field f is paired with vector v, then v[i] gives the number of
	 *          N-to-M matchings the field f induces for which absolute_val(N-M) = i. */
	public static KeyFieldRankings orderByNtoM(Matching.Cursor cur)
	{
		// A list of (path,ranking) pairs
		//   "ranking[i]" is the number of n-to-m matchings where abs(n-m) = i
		//   the keyFields list is then sorted by this ranking (which is lexicographically ordered).
		InstanceSet oldObjects = cur.getOld1();
		InstanceSet newObjects = cur.getNew();
		KeyFieldRankings rankings =
			new KeyFieldRankings(Math.max(oldObjects.size(),newObjects.size()));
		if(oldObjects.size() != newObjects.size())
			System.out.println("Warning: Different number of objects in old and new snapshots.");
		for(FieldPath field : oldObjects.getFields()) {
			System.out.println(field);
			Set<ExpandedObject> values = union(oldObjects.getValues(field), newObjects.getValues(field));
			for(ExpandedObject val : values) {
				System.out.print("  " + val + ": (");
				int m = oldObjects.instancesMatching(field, val);
				System.out.print(m + ", ");
				int n = newObjects.instancesMatching(field, val);
				System.out.print(n + ")");
				
				int weight = 1;
				// Weight based on type
				if(val instanceof StringObject) {
					weight = 2;
				}
				// Disregard fields which do not match any new-version objects (or similarly match no
				// new-version objects)
				if(m != 0 && n != 0)
					rankings.add(field,weight,Math.abs(m-n));
				System.out.println();
			}
		}
		return rankings;
	}
}
