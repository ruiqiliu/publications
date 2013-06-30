package edu.umd.MatchSnapshots.Synthesis.SynthFuncs;

import java.util.Iterator;

import edu.umd.MatchSnapshots.FieldPath;
import edu.umd.MatchSnapshots.ExpandedObjects.*;
import edu.umd.MatchSnapshots.Synthesis.Example;

public class CollectionMap extends SynthFun {
	// The function to apply to each element of the collection.
	//   mapFun == null indicates that any function works (we have only seen empty collections
	//    in the examples so far)
	private SynthFun mapFun;
	private CollectionMap(String className, FieldPath field) { super(className, field); }

	public static CollectionMap getSynthFun(String className, FieldPath field, Example ex) {
		ExpandedObject eo1 = ex.getOld().getField(field);
		ExpandedObject eo2 = ex.getNew().getField(field);
		CollectionMap result = new CollectionMap(className, field);
		if(eo1 instanceof CollectionObject && eo2 instanceof CollectionObject) {
			CollectionObject o1 = (CollectionObject) eo1;
			CollectionObject o2 = (CollectionObject) eo2;
			// Any function works when both collections are empty
			if(o1.elements.size() == 0 && o2.elements.size() == 0) {
				result.mapFun = null;
				return result;
			}
			if(o1.elements.size() != o2.elements.size())
				return null;
			
			result.mapFun = computeMapFun(o1,o2);
			return result;
		}
		return null;
	}
	
	private static SynthFun computeMapFun(CollectionObject o1, CollectionObject o2) {
		Iterator<ExpandedObject> iter1 = o1.elements.iterator();
		Iterator<ExpandedObject> iter2 = o2.elements.iterator();
		String className;
		if(o1.elements.isEmpty())
			className = "";
		else
			className = o1.elements.get(0).getType();
		SynthFun result =
			SynthFields.getSynthFun(className, new Example(iter1.next(), iter2.next()));
		while(iter1.hasNext())
			result.refine(new Example(iter1.next(), iter2.next()));
		return result;
	}
	
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("Iterator oldIter = " + make_old(field) + ".iterator();\n");
		result.append("while(oldIter.hasNext()) {\n");
		result.append(indent(old_obj + " = oldIter.next();\n"));
		result.append(indent(new_obj + " = new " + className + "();\n"));
		result.append(indent(mapFun.toString()) + "\n");
		result.append("}\n");
		return result.toString();
	}
	
	@Override
	public int size() { return mapFun.size(); }
	
	@Override
	public boolean consistentWith(Example ex) {
		ExpandedObject eo1 = ex.getOld().getField(field);
		ExpandedObject eo2 = ex.getNew().getField(field);
		if(eo1 instanceof CollectionObject && eo2 instanceof CollectionObject) {
			CollectionObject o1 = (CollectionObject) eo1;
			CollectionObject o2 = (CollectionObject) eo2;
			// Any function works when both collections are empty
			if(o1.elements.size() == 0 && o2.elements.size() == 0)
				return true;
			if(o1.elements.size() != o2.elements.size())
				return false;
			Iterator<ExpandedObject> iter1 = o1.elements.iterator();
			Iterator<ExpandedObject> iter2 = o2.elements.iterator();
			while(iter1.hasNext()) {
				if(!mapFun.consistentWith(new Example(iter1.next(), iter2.next())))
					return false;
			}
			return true;
		}
		return false;
	}	
}
