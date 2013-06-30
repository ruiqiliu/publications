package edu.umd.MatchSnapshots;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import edu.umd.MatchSnapshots.ExpandedObjects.ExpandedObject;
import edu.umd.MatchSnapshots.ExpandedObjects.GenericObject;

/**
 * Stores a set of object instances.  These sets are mutable, though they are treated as
 * immutable by the matching and synthesis code.  The mutability is only used when
 * initially building the set from a heap snapshot.
 * @author smagill
 *
 */
public class InstanceSet {
	public ArrayList<ExpandedObject> instances;
	
	// maps (field name, object) pairs to an integer that gives the number of instances with
	// a value in "field name" that equals "object"
	public HashMap<FieldPath,HashMap<ExpandedObject,AtomicInteger>> fieldKeyness = null;
	
	public InstanceSet() {
		instances = new ArrayList<ExpandedObject>(10);
	}
	
	public InstanceSet(ExpandedObject obj) {
		instances = new ArrayList<ExpandedObject>(10);
		addInstance(obj);
	}
	
	public InstanceSet(Collection<ExpandedObject> objs) {
		this();
		addAll(objs);
	}
	
	public void addInstance(ExpandedObject obj) {
	    if(obj != null) {
		instances.add(obj);
		fieldKeyness = null;
	    }
	}
	
	public void removeInstance(ExpandedObject obj) {
		instances.remove(obj);
		fieldKeyness = null;
	}
	
	private class WrappedIterator implements Iterator<ExpandedObject> {
		Iterator<ExpandedObject> iter;
		ExpandedObject lastSeen = null;
		
		public WrappedIterator(Iterator<ExpandedObject> iter) {
			this.iter = iter;
		}

		@Override
		public boolean hasNext() {
			return iter.hasNext();
		}

		@Override
		public ExpandedObject next() {
			lastSeen = iter.next();
			return lastSeen;
		}

		@Override
		public void remove() {
			//removeInstance(lastSeen);
			fieldKeyness = null;
			iter.remove();
		}
	}
	
	public Iterator<ExpandedObject> iterator() {
		return new WrappedIterator(instances.iterator());
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public InstanceSet clone() {
		InstanceSet result = new InstanceSet();
		result.instances = (ArrayList<ExpandedObject>) this.instances.clone();
		return result;
	}
	
	public boolean isEmpty() {
		return instances.isEmpty();
	}
	
	public void addAll(Collection<ExpandedObject> objs) {
		for(ExpandedObject obj : objs)
			addInstance(obj);
	}
	
	public void addAll(InstanceSet set) {
		for(ExpandedObject obj : set.instances) {
			addInstance(obj);
		}
	}

	public void removeAll(InstanceSet set) {
		for(ExpandedObject obj : set.instances) {
			removeInstance(obj);
		}
	}
	
	private void computeKeyness() {
		if(fieldKeyness == null)
			computeStats();
	}

	public Set<FieldPath> getFields() {
		computeKeyness();
		return fieldKeyness.keySet();
	}
	
	public int size() {
		return instances.size();
	}
	
	public int instancesMatching(FieldPath path, ExpandedObject value) {
		computeKeyness();
		HashMap<ExpandedObject,AtomicInteger> map = fieldKeyness.get(path);
		if(map == null) return 0;
		AtomicInteger i = map.get(value);
		if(i == null) return 0;
		return i.get();
	}

	public Set<ExpandedObject> getInstancesMatching(FieldPath path, ExpandedObject value) {
		HashSet<ExpandedObject> result = new HashSet<ExpandedObject>();
		for(ExpandedObject o : instances) {
			ExpandedObject field_val = o.getField(path);
			if(field_val != null && field_val.equals(value))
				result.add(o);
		}
		return result;
	}
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof InstanceSet))
			return false;
		else {
			InstanceSet other_set = (InstanceSet)other;
			return other_set.instances.equals(this.instances);
		}
	}

	public Set<ExpandedObject> getValues(FieldPath path) {
		computeKeyness();
		HashMap<ExpandedObject,AtomicInteger> map = fieldKeyness.get(path);
		if(map == null) return new HashSet<ExpandedObject>();
		return map.keySet();
	}
	
	public int numberOfValues(FieldPath field) {
		computeKeyness();
		HashMap<ExpandedObject,AtomicInteger> map = fieldKeyness.get(field);
		if(map == null)
			return 0;
		else
			return map.size();
	}
	
	public void incrementKeyness(FieldPath fieldname, ExpandedObject value) {
		HashMap<ExpandedObject,AtomicInteger> map = fieldKeyness.get(fieldname);
		if(map == null) {
			map = new HashMap<ExpandedObject, AtomicInteger>(10);
			fieldKeyness.put(fieldname, map);
		}
		AtomicInteger i = map.get(value);
		if(i == null) {
			i = new AtomicInteger(0);
			map.put(value, i);
		}
		i.incrementAndGet();
	}
	
	public void computeStats() {
		fieldKeyness = new HashMap<FieldPath,HashMap<ExpandedObject,AtomicInteger>>();
		
		for(ExpandedObject obj : instances)
			for(FieldPath field : obj.getFields())
				incrementKeyness(field, obj.getField(field));
	}
	
	public String toString() {
		StringBuffer result = new StringBuffer();
		Iterator<ExpandedObject> iter = instances.iterator();
		while(iter.hasNext()) {
			ExpandedObject obj = iter.next();
			result.append(obj.toString());
			if(iter.hasNext())
				result.append(",\n");
		}
		return result.toString();
	}
}
