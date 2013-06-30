package edu.umd.MatchSnapshots.Synthesis;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.umd.MatchSnapshots.FieldPath;
import edu.umd.MatchSnapshots.InstanceSet;
import edu.umd.MatchSnapshots.Matching;
import edu.umd.MatchSnapshots.Pair;
import edu.umd.MatchSnapshots.ExpandedObjects.ExpandedObject;
import edu.umd.MatchSnapshots.Synthesis.SynthFuncs.SynthFun;
import edu.umd.MatchSnapshots.Synthesis.SynthFuncs.SynthSet;

public class SynthMatching {
	LinkedList<Example> examples = null;
	Matching matching;
	FieldPath field;
	String className;
	
	public SynthMatching(Matching matching, String className, FieldPath field) {
		this.className = className;
		this.field = field;
		this.matching = matching;
		computeMatching();
	}
	
	private int score(SynthFun f, InstanceSet oldSet, InstanceSet newSet) {
		int score = 0;
		oldSet = oldSet.clone();
		newSet = newSet.clone();
		Iterator<ExpandedObject> oldIter = oldSet.iterator();
		
	OuterLoop:
		while(oldIter.hasNext()) {
			ExpandedObject oldObj = oldIter.next();
			Iterator<ExpandedObject> newIter = newSet.iterator();
			while(newIter.hasNext()) {
				ExpandedObject newObj = newIter.next();
				if(f.consistentWith(new Example(oldObj, newObj))) {
					oldIter.remove();
					newIter.remove();
					score++;
					continue OuterLoop;
				}
			}
		}
		
		return score;
	}
	
	private int score(SynthFun f) {
		int score = 0;
		Matching.Cursor cur = matching.getCursor();
		while(cur.isValid()) {
			score += score(f, cur.getOld1(), cur.getNew());
			cur.advance();
		}
		return score;
	}
	
	// Find snapshot pair with the most examples
	private Pair<InstanceSet,InstanceSet> findMostInstances() {
		Matching.Cursor cur = matching.getCursor();
		int most = 0;
		InstanceSet oldBest = null, newBest = null;
		while(cur.isValid()) {
			InstanceSet oldSet = cur.getOld1();
			InstanceSet newSet = cur.getNew();
			if(oldSet.size() > most) {
				oldBest = oldSet;
				newBest = newSet;
				most = oldSet.size();
			}
		}
		return new Pair<InstanceSet,InstanceSet>(oldBest,newBest);
	}
	
	/** Take the first old object.  This must match some new-version object.  Choose the matching that
	 *  results in a transformer function that is consistent with the largest number of
	 *  (old-version, new-version) instance pairs.  Remove those consistent pairs and repeat.
	 */
	public void computeMatching() {
		Matching.Cursor cur = matching.getCursor();
		while(cur.isValid()) {
		    if(cur.getOld1().size() <= 1 || cur.getNew().size() <= 1) {
			cur.advance();
			continue;  // These equivalence classes do not need to be broken down further.
		    }
		    InstanceSet oldSet = cur.getOld1();
		    InstanceSet newSet = cur.getNew();
		    List<InstanceSet> oldMatched = new LinkedList<InstanceSet>();
		    List<InstanceSet> newMatched = new LinkedList<InstanceSet>();
		    while(!oldSet.isEmpty() && !newSet.isEmpty()) {
				Iterator<ExpandedObject> oldIter = oldSet.iterator();
				ExpandedObject oldObj = oldIter.next();
				Iterator<ExpandedObject> newIter = newSet.iterator();
				int bestScore = 0;
				ExpandedObject bestObj = null;
				SynthFun bestFun = null;
				// For each new-version object
				while(newIter.hasNext()) {
					ExpandedObject newObj = newIter.next();
					// Find set of valid synthesis functions
					SynthFun f = SynthSet.getSynthFun(className, field, new Example(oldObj, newObj));
					if(f == null)
					    continue;
					System.out.println("f : " + f.getClass().toString() + " = \n");
					System.out.println(f.toString());
					// Remember the pair that results in the most-applicable update function
					int score = score(f, oldSet, newSet);
					if(score > bestScore) {
						bestObj = newObj;
						bestScore = score;
						bestFun = f;
					}
				}
				if(bestFun == null)
				    break;
				System.out.println("bestFun : " + bestFun.getClass().toString() + " = \n");
				System.out.println(bestFun.toString());
				System.out.println("bestScore = " + bestScore);
				if(bestScore == 1)
					break; // stop if the best function only works for one example pair
				// Remove pairs consistent with the winning function and add them to the list of examples
				Pair<List<InstanceSet>, List<InstanceSet>> matched =
					removePairsMatchingFun(bestFun, oldSet, newSet);
				// oldSet and newSet now contain only those object that are unmatched and "matched" contains
				// those objects that were matched by bestFun.
				oldMatched.addAll(matched.getFirst());
				newMatched.addAll(matched.getSecond());
			}
			// Now we may have left-over old or new object that don't match.  These should be dropped (filtered out)
			// by the transformer in the case of old object or generated in the case of new objects.  We don't
			// have generators implemented yet (lack of examples), so unmatched new objects will result in
			// synthesis failure.
			if(!oldSet.isEmpty() || !newSet.isEmpty()) {
				InstanceSet nonEmptySet = null;
				List<InstanceSet> nonEmptyMatched = null;
				List<InstanceSet> emptyMatched = null;
				if(!oldSet.isEmpty()) {
					nonEmptySet = oldSet;
					nonEmptyMatched = oldMatched;
					emptyMatched = newMatched;
				}
				else {
					nonEmptySet = newSet;
					nonEmptyMatched = newMatched;
					emptyMatched = oldMatched;
				}
				for(ExpandedObject obj : nonEmptySet.instances) {
					nonEmptyMatched.add(new InstanceSet(obj));
					emptyMatched.add(new InstanceSet());
				}
			}
			// Since we don't care about old2 at this point, we just over-write these objects with duplicate
			// references to old1.
			cur.splitAs(oldMatched, oldMatched, newMatched);
			cur.advance();
		}
	}

	private Pair<List<InstanceSet>, List<InstanceSet>>
	removePairsMatchingFun(SynthFun f, InstanceSet oldSet, InstanceSet newSet) {
		int score = 0;
		Iterator<ExpandedObject> oldIter = oldSet.iterator();
		List<InstanceSet> oldMatched = new LinkedList<InstanceSet>();
		List<InstanceSet> newMatched = new LinkedList<InstanceSet>();
		
	OuterLoop:
		while(oldIter.hasNext()) {
			ExpandedObject oldObj = oldIter.next();
			Iterator<ExpandedObject> newIter = newSet.iterator();
			while(newIter.hasNext()) {
				ExpandedObject newObj = newIter.next();
				if(f.consistentWith(new Example(oldObj, newObj))) {
					f.consistentWith(new Example(oldObj, newObj));
					oldIter.remove();
					newIter.remove();
					oldMatched.add(new InstanceSet(oldObj));
					newMatched.add(new InstanceSet(newObj));
					continue OuterLoop;
				}
			}
		}
		return new Pair<List<InstanceSet>, List<InstanceSet>>(oldMatched, newMatched);
	}
	
	public List<Example> getExamples() {
		return matching.getExamples();
	}
	
	public Matching getMatching() {
		return matching;
	}
}
