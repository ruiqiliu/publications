package edu.umd.MatchSnapshots;

import java.util.Vector;

/* A lexicographically ordered list of integers used to rank key fields. */

public class KeyFieldRank implements Comparable<KeyFieldRank> {
	Vector<Integer> rank;
	public KeyFieldRank(int rank_size) {
		rank = new Vector<Integer>(rank_size);
		rank.setSize(rank_size);
		for(int i = 0 ; i < rank.size() ; i++)
			rank.set(i,new Integer(0));
	}
	private void ensure_size(int size) {
		if(rank.size() < size) {
			int currSize = rank.size();
			rank.setSize(size);
			for(int i = currSize ; i < rank.size() ; i++)
				rank.set(i, new Integer(0));
		}		
	}
	public boolean isZero() {
		for(Integer i : rank) {
			if(i != 0)
				return false;
		}
		return true;
	}
	public void increment(int position) {
		ensure_size(position + 1);
		rank.set(position, rank.get(position) + 1);
	}
	public boolean equals(Object other) {
		if(other instanceof KeyFieldRank)
			return rank.equals(((KeyFieldRank)other).rank);
		else return false;
	}
	public int compareTo(KeyFieldRank other) {
		int size1 = rank.size();
		int size2 = other.rank.size();
		int limit = Math.min(size1, size2);
		
		for(int i = 0 ; i < limit ; i++) {
			if(rank.get(i) != other.rank.get(i))
				return other.rank.get(i) - rank.get(i);
		}
		//return other.rank.size() - rank.size();
		return 0;
	}
	public void add(KeyFieldRank other) {
		ensure_size(other.rank.size());
		for(int i = 0 ; i < other.rank.size() ; i++)
			rank.set(i, rank.get(i) + other.rank.get(i));
	}
	public void add(int toadd) {
		for(int i = 0 ; i < rank.size() ; i++)
			rank.set(i, rank.get(i) + toadd);
	}
	public void add(int amount_to_add, int position) {
		ensure_size(position + 1);
		rank.set(position, rank.get(position) + amount_to_add);
	}
	public void multiply(int factor) {
		for(int i = 0; i < rank.size(); i++)
			rank.set(i, rank.get(i) * factor);
	}
	public void multiply(KeyFieldRank other) {
		ensure_size(other.rank.size());
		for(int i = 0; i < rank.size(); i++) {
			if(i < other.rank.size())
				rank.set(i, rank.get(i) * other.rank.get(i));
			else
				rank.set(i, 0);
		}
	}
	/** Adds the values in the rankings unless one or the other is zero, in which case the result is zero. */
	public void addIfNotZero(KeyFieldRank other) {
		ensure_size(other.rank.size());
		for(int i = 0; i < rank.size(); i++) {
			if(i < other.rank.size() && rank.get(i) > 0 && other.rank.get(i) > 0)
				rank.set(i, rank.get(i) + other.rank.get(i));
			else
				rank.set(i, 0);
		}		
	}
	public String toString() {
		StringBuffer result = new StringBuffer();
		for(Integer i : rank)
			result.append(i + " ");
		return result.toString();
	}
}
