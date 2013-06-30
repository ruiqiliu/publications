package edu.umd.MatchSnapshots.Synthesis.SynthFuncs;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.umd.MatchSnapshots.FieldPath;
import edu.umd.MatchSnapshots.InstanceSet;
import edu.umd.MatchSnapshots.Matcher;
import edu.umd.MatchSnapshots.Pair;
import edu.umd.MatchSnapshots.Conditions.Condition;
import edu.umd.MatchSnapshots.Conditions.FalseCondition;
import edu.umd.MatchSnapshots.ExpandedObjects.*;
import edu.umd.MatchSnapshots.Synthesis.Example;

public class FilterMap extends SynthFun {
	// The function to apply to each element of the collection.
	//   mapFun == null indicates that any function works (we have only seen empty collections
	//    in the examples so far)
	private SynthFun mapFun;
	private Condition filterCond;
	private FilterMap(String className, FieldPath field) {
		super(className, field);
		mapFun = null;
		filterCond = null;
	}

	public static FilterMap getSynthFun(String className, FieldPath field, Example ex) {
		ExpandedObject eo1 = ex.getOld().getField(field);
		ExpandedObject eo2 = ex.getSecondOld().getField(field);
		ExpandedObject eon = ex.getNew().getField(field);
		FilterMap result = new FilterMap(className, field);
		if(eo1 instanceof CollectionObject && eo2 instanceof CollectionObject) {
			CollectionObject o1 = (CollectionObject) eo1;
			CollectionObject o2 = (CollectionObject) eo2;
			CollectionObject on = (CollectionObject) eon;
			// Any function works when both collections are empty
			if(o1.elements.size() == 0 && o2.elements.size() == 0 && on.elements.size() == 0) {
				result.mapFun = null;
				return result;
			}
			Pair<SynthFun,Condition> r = computeFilterMapFun(o1,o2,on, className, field);
			if(r == null)
				return null;
			result.mapFun = r.getFirst();
			result.filterCond = r.getSecond();
			return result;
		}
		return null;
	}
	
	// TODO: change from here down to add filter operation.
	private static Pair<SynthFun,Condition> computeFilterMapFun(CollectionObject o1, CollectionObject o2, CollectionObject on, String className, FieldPath field) {
		System.out.println("Old1 elements: " + o1.elements.size());
		System.out.println("Old2 elements: " + o2.elements.size());
		System.out.println("New elements: " + on.elements.size());
		List<Example> examples = Matcher.match(new InstanceSet(o1.elements),
                                           new InstanceSet(o2.elements),
                                           new InstanceSet(on.elements));
		Example.examplesToHTML(examples, className + field.toString() + ".html");
		if(examples.isEmpty())
			return null;
		SynthFun mapFun = null;
		List<ExpandedObject> filteredOut = new LinkedList<ExpandedObject>();
		for(Example ex : examples) {
			if(ex.getNew() == null)
				filteredOut.add(ex.getOld());
			else {
				if(mapFun == null)
					mapFun = SynthFields.getSynthFun(ex.getNew().getType(), ex);
				else
					mapFun.refine(ex);
			}
		}
		Condition cond = Condition.getCondition(new InstanceSet(filteredOut), new InstanceSet());
		return new Pair<SynthFun,Condition>(mapFun,cond);
	}
	
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("Iterator oldIter = " + make_old(field) + ".iterator();\n");
		result.append("while(oldIter.hasNext()) {\n");
		if(!(filterCond instanceof FalseCondition)) {
			result.append(indent("if(" + filterCond + ") {"));
			result.append(indent(indent("oldIter.remove()")));
			result.append(indent("}"));
		}
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
			Iterator<ExpandedObject> iter1 = o1.elements.iterator();
			Iterator<ExpandedObject> iter2 = o2.elements.iterator();
			while(iter1.hasNext()) {
				eo1 = iter1.next();
				if(filterCond.satisfies(eo1))
					continue;
				if(!mapFun.consistentWith(new Example(eo1, iter2.next())))
					return false;
			}
			return true;
		}
		return false;
	}
}
