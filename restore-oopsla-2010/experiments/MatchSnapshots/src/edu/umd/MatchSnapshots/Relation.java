package edu.umd.MatchSnapshots;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class Relation<T> {
	private HashMap<T,Set<T>> relation;
	
	public Relation() {
		relation = new HashMap<T,Set<T>>();
	}
	
	public void relate(T o1, T o2) {
		Set<T> range = relation.get(o1);
		if(range == null) {
			range = new HashSet<T>();
			relation.put(o1, range);
		}
		range.add(o2);
	}
	
	public Set<T> getRange(T value) {
		Set<T> set = relation.get(value);
		if(set == null)
			return new TreeSet<T>();
		else
			return set;
	}
	
	@Override
	public boolean equals(Object otherObj) {
		if(otherObj instanceof Relation) {
			Relation other = (Relation) otherObj;
			if(relation.size() != other.relation.size())
				return false;
			for(Map.Entry<T,Set<T>> entry : relation.entrySet()) {
				if(!entry.getValue().equals(other.getRange(entry.getKey())))
					return false;
			}
			return true;
		}
		else
			return false;
	}
}
