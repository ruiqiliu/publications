package edu.umd.MatchSnapshots.Synthesis.SynthFuncs;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import edu.umd.MatchSnapshots.FieldPath;
import edu.umd.MatchSnapshots.ExpandedObjects.ExpandedObject;
import edu.umd.MatchSnapshots.ExpandedObjects.GenericObject;
import edu.umd.MatchSnapshots.Synthesis.Example;

public class SynthSet extends SynthFun {
	List<SynthFun> fs;
	private SynthSet(String className, FieldPath field, List<SynthFun> fs) {
		super(className, field);
		this.fs = fs;
	}
	
	public static SynthSet getSynthFun(String className, FieldPath field, Example ex) {
		List<SynthFun> poss = new LinkedList<SynthFun>();
		SynthFun f;

		// If no field is specified, try to synthesize an update for the object as a whole.
		if(field == null) {
			f = SynthFields.getSynthFun(className, ex);
			if(f != null) {
				System.out.println("SynthFields");
				poss.add(f);
			}
		}
		else {
			f = PtrCopy.getSynthFun(className, field, ex);
			if(f != null) {
				System.out.println("PtrCopy");
				poss.add(f);
			}
			
			f = StringCopy.getSynthFun(className, field, ex);
			if(f != null) {
				System.out.println("String copy");
				poss.add(f);
			}

			f = IntCopy.getSynthFun(className, field, ex);
			if(f != null) {
				System.out.println("Int copy");
				poss.add(f);
			}
			
			f = StringSegment.getSynthFun(className, field, ex);
			if(f != null) {
				System.out.println("String segment");
				poss.add(f);
			}
			
			f = FilterMap.getSynthFun(className, field, ex);
			if(f != null) {
				System.out.println("Collection map");
				poss.add(f);
			}
	
			f = Constant.getSynthFun(className, field, ex);
			if(f != null) {
				System.out.println("Constant");
				poss.add(f);			
			}
		}
				
		if(poss.size() > 0)
			return new SynthSet(className, field, poss);
		else {
			System.out.println("Failed to synthesize for example " + ex.getNum());
			return null;
		}
	}
	
	public String toString() {
		if(fs.size() > 0)
			return fs.get(0).toString();
		else
			return "INCONSISTENT";
	}
	
	public SynthSet refine(Example ex) {
		System.out.println("Refining set");
		Iterator<SynthFun> iter = fs.iterator();
		while(iter.hasNext()) {
			SynthFun f = iter.next();
			System.out.println("Evaluating " + f);
			f = f.refine(ex);
			if(f == null) {
				System.out.println("Not consistent.  Removed.");
				iter.remove();
			}
		}
		if(fs.size() == 0)
			return null;
		else
			return this;
	}
	
	public SynthSet expand(Example ex) {
		if(consistentWith(ex))
			return this;
		else {
			SynthFun f = getSynthFun(className,field,ex);
			if(f == null)
				return null;
			else {
				fs.add(f);
				return this;
			}
		}
	}
	
	@Override
	public boolean consistentWith(Example ex) {
		for(SynthFun f : fs) {
			if(f.consistentWith(ex))
				return true;
		}
		return false;
	}
	
	@Override
	public int size() {
		int i = 0;
		for(SynthFun f : fs)
			i += f.size();
		return i;
	}
}
