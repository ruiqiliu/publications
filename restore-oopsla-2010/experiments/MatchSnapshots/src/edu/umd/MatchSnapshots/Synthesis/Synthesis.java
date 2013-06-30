package edu.umd.MatchSnapshots.Synthesis;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.umd.MatchSnapshots.FieldPath;
import edu.umd.MatchSnapshots.Synthesis.SynthFuncs.*;

public class Synthesis {
	public static SynthFun synthesize(String className, FieldPath field, List<Example> exs) {
		Iterator<Example> iter = exs.iterator();
		SynthPartition part = null;
		int i = 0;
		while(iter.hasNext()) {
			Example ex = iter.next();
			if(part == null)
				part = SynthPartition.getSynthFun(className, field, ex);
			else
				part.refine(ex);
		}
		return part;
	}
}
