package edu.umd.MatchSnapshots.ExpandedObjects;

import java.util.Iterator;
import java.util.List;

public class CollectionObject extends ExpandedObject {
	public List<ExpandedObject> elements;
	boolean ordered;
	String type;
	
	public CollectionObject(String type, List<ExpandedObject> elements, boolean ordered) {
		this.elements = elements;
		this.type = type;
		this.ordered = ordered;
	}

	@Override
	public String toString() {
		char left_brace = ordered ? '[': '{';
		char right_brace = ordered ? ']' : '}';
		StringBuffer buf = new StringBuffer();
		Iterator<ExpandedObject> iter = elements.iterator();
		buf.append(left_brace);
		while(iter.hasNext()) {
			ExpandedObject obj = iter.next();
			if(iter.hasNext())
				buf.append(obj.toString() + ",\n");
			else
				buf.append(obj.toString() + "\n");
		}
		buf.append(right_brace);
		return buf.toString();
	}
	public String getType() { return type; }
}
