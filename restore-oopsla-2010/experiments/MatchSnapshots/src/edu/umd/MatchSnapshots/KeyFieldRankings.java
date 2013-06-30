package edu.umd.MatchSnapshots;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

/* Associates a field path with a ranking, which is ordered lexicographically.
 * Currently this is used in two different ways.
 *   1) When analyzing old-old snapshot pairs, rank is a 1-element array that records
 *      how many N-to-N equivalence classes are induced by the given field.
 *   2) When analyzing old-new snapshot pairs, rank is an N-element array where N is
 *      the number of values that the given field takes on.  rank[i] records the
 *      number of equivalence classes that induce a S-to-T mapping where abs(S - T) = i */

public class KeyFieldRankings {
	HashMap<FieldPath,KeyFieldRank> map;
	int rank_size;

	public KeyFieldRankings(int rank_size) {
		map = new HashMap<FieldPath,KeyFieldRank>();
		this.rank_size = rank_size;
	}
	private void ensureExists(FieldPath field) {
		if(map.get(field) == null)
			map.put(field,new KeyFieldRank(rank_size));		
	}
	public KeyFieldRank getRanking(FieldPath field) {
		return map.get(field);
	}
	public void increment(FieldPath field, int position) {
		ensureExists(field);
		KeyFieldRank rank = map.get(field);
		rank.increment(position);
	}
	public boolean equals(Object other) {
		if(other instanceof KeyFieldRankings) {
			return map.equals(((KeyFieldRankings)other).map); 
		}
		else return false;
	}
	/** Adds the given amount to each position in each vector. */
	public void add(int toadd) {
		for(Map.Entry<FieldPath, KeyFieldRank> entry : map.entrySet())
			entry.getValue().add(toadd);
	}
	public void add(KeyFieldRankings other) {
		for(Map.Entry<FieldPath, KeyFieldRank> entry : other.map.entrySet()) {
			ensureExists(entry.getKey());
			KeyFieldRank rank = map.get(entry.getKey());
			rank.add(entry.getValue());
		}
	}
	public void add(FieldPath field, int amount_to_add, int position) {
		ensureExists(field);
		KeyFieldRank rank = map.get(field);
		rank.add(amount_to_add, position);		
	}
	/** Multiplies the values in this ranking by the values in corresponding positions of the
	 * two ranking passed in. */
	public void multiply(KeyFieldRankings other) {
		for(Map.Entry<FieldPath, KeyFieldRank> entry : other.map.entrySet()) {
			ensureExists(entry.getKey());
			KeyFieldRank rank = map.get(entry.getKey());
			rank.multiply(entry.getValue());
		}
	}
	/** Adds the values in the rankings unless one or the other is zero, in which case the result is zero. */
	public void addIfNotZero(KeyFieldRankings other) {
		for(Map.Entry<FieldPath, KeyFieldRank> entry : other.map.entrySet()) {
			ensureExists(entry.getKey());
			KeyFieldRank rank = map.get(entry.getKey());
			rank.addIfNotZero(entry.getValue());
		}
	}
	/** Multiplies the ranking associated with the given field by the given factor. */
	public void multiply(FieldPath field, int factor) {
		ensureExists(field);
		map.get(field).multiply(factor);
	}
	public Set<FieldPath> getFields() {
		Set<FieldPath> set = new TreeSet<FieldPath>();
		for(Map.Entry<FieldPath,KeyFieldRank> entry : map.entrySet())
			set.add(entry.getKey());
		return set;
	}
	
	public class FieldRankPairing implements Comparable<FieldRankPairing> {
		FieldPath field;
		KeyFieldRank rank;
		
		public FieldRankPairing(Map.Entry<FieldPath, KeyFieldRank> entry) {
			field = entry.getKey();
			rank = entry.getValue();
		}
		
		public int compareTo(FieldRankPairing other) {
			if(rank.compareTo(other.rank) == 0)
				return field.length() - other.field.length();
			else
				return rank.compareTo(other.rank);
		}
		
		@Override
		public boolean equals(Object other) {
			if(other instanceof FieldRankPairing) {
				return compareTo((FieldRankPairing)other) == 0;
			}
			else return false;
		}
		
		public String toString() {
			StringBuffer result = new StringBuffer(field.toString() + ":");
			for(Integer i : rank.rank)
				result.append(" " + i);
			return result.toString();
		}
	}
	
	public List<FieldRankPairing> getSorted() {
		LinkedList<FieldRankPairing> result = new LinkedList<FieldRankPairing>();
		for(Map.Entry<FieldPath, KeyFieldRank> entry : map.entrySet())
			result.add(new FieldRankPairing(entry));
		Collections.sort(result);
		return result;
	}
	
	public String toString() {
		StringBuffer result = new StringBuffer();
		for(Map.Entry<FieldPath, KeyFieldRank> entry : map.entrySet()) {
			result.append(entry.getKey().toString());
			result.append(": ");
			result.append(entry.getValue().toString());
			result.append("\n");
		}
		return result.toString();
	}
}
