package edu.umd.MatchSnapshots.ExpandedObjects;

import java.util.List;

public class ObjectArrayObject extends CollectionObject {
	public ObjectArrayObject(String type, List<ExpandedObject> elements) {
		super(type,elements,true);
		this.elements = elements;
		this.type = type;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer("Object array with " + elements.size() + " elements.\n");
		for(ExpandedObject obj : elements)
			buf.append(obj.toString() + "\n");
		return buf.toString();
	}
}
