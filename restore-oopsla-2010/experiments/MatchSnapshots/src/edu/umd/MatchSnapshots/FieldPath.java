package edu.umd.MatchSnapshots;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

public class FieldPath implements Comparable<FieldPath> {
	private LinkedList<String> path;
	
	public FieldPath() {
		path = new LinkedList<String>();
	}

	public FieldPath(String field) {
		path = new LinkedList<String>();

		StringTokenizer tok = new StringTokenizer(field,".");
		while(tok.hasMoreElements()) {
			String s = tok.nextToken();
			path.addLast(s);
		}
	}

	public FieldPath(FieldPath other) {
		path = (LinkedList<String>) other.path.clone();
	}
	
	public FieldPath append(String component) {
		FieldPath result = new FieldPath(this);
		result.path.addLast(component);
		return result;
	}
	
	public int length() {
		return path.size();
	}
	
	boolean isImplicit() {
		return path.getLast().contains("IMPLICIT");
	}
	
	public FieldPath prepend(String component) {
		FieldPath result = new FieldPath(this);
		result.path.addFirst(component);
		return result;
	}

	public void prependMutate(String component) {
		path.addFirst(component);
	}

	public String toString() {
		StringBuffer result = new StringBuffer("");
		boolean first = true;
		for(String component : path) {
			if(!first)
				result.append(".");
			first = false;
			result.append(component);
		}
		return result.toString();
	}
	
	public List<String> getPath() {
		return path;
	}
	
	public boolean equals(Object other) {
		if(other instanceof FieldPath)
			return path.equals(((FieldPath)other).path);
		else
			return false;
	}
	
	public int hashCode() {
		return path.hashCode();
	}
	
	@Override
	public int compareTo(FieldPath other) {
		Iterator<String> iter1 = path.iterator();
		Iterator<String> iter2 = other.path.iterator();
		
		while(iter1.hasNext()) {
			if(!iter2.hasNext())
				return -1;
			String s1 = iter1.next();
			String s2 = iter2.next();
			if(s1.equals(s2))
				continue;
			else
				return s1.compareTo(s2);
		}
		if(iter2.hasNext())
			return 1;
		else
			return 0;
	}
}
