package edu.umd.MatchSnapshots.Conditions;

import java.util.Iterator;

import edu.umd.MatchSnapshots.InstanceSet;
import edu.umd.MatchSnapshots.ExpandedObjects.ExpandedObject;

abstract public class Condition {
	public static Condition getCondition(InstanceSet inOrig, InstanceSet outOrig) {
		InstanceSet in = inOrig.clone();
		InstanceSet out = outOrig.clone();
		InstanceSet in_uncovered = in.clone();
		InstanceSet in_uncovered_prime;
		InstanceSet out_covered;
		Condition b = new FalseCondition();
		//System.out.println("in_uncovered = ");
		//System.out.println(in_uncovered);
		while(!in_uncovered.isEmpty()) {
			InstanceSet old_in_uncovered = in_uncovered.clone();
			in_uncovered_prime = in_uncovered.clone();
			//System.out.println("in_uncovered_prime = ");
			//System.out.println(in_uncovered_prime);
			out_covered = out.clone();
			System.out.println("DEBUG: out_covered.size() = " + out_covered.size());
			Condition d = new TrueCondition();
			//System.out.println("out_covered = ");
			//System.out.println(out_covered);
			while(!out_covered.isEmpty()) {
				Condition pi = getBestPred(in_uncovered_prime, out_covered);
				if(pi == null) {
					System.out.println("Could not infer condition (pi == null).");
					System.exit(-1);
				}
				d = AndCondition.makeAndSimplify(pi,d);
				pi.keepSatisfying(in_uncovered_prime);
				//System.out.println("in_uncovered_prime2 = ");
				//System.out.println(in_uncovered_prime);
				pi.keepSatisfying(out_covered);
				//System.out.println("out_covered2 = ");
				//System.out.println(out_covered);
				if(out_covered.size() == out.size()) {
					System.out.println("Could not infer condition (out_remaining == out).");
					System.exit(-1);
				}
				System.out.println("DEBUG: pi = " + pi);
				System.out.println("DEBUG: d = " + d);
			}
			d.keepNotSatisfying(in_uncovered);
			b = OrCondition.makeAndSimplify(b,d);
			if(old_in_uncovered.size() == in_uncovered.size()) {
				System.out.println("Could not infer condition (old_in_remaining == in_remaining).");
				System.exit(-1);
			}
		}
		return b;
	}
	
	private static Condition getBestPred(InstanceSet in, InstanceSet out) {
		Condition best_cond = EqCondition.getCondition(in, out);
		return best_cond;
	}
	
	protected static int getRank(int num_in, int num_out) {
		return num_in * num_out;
	}
	
	abstract public boolean satisfies(ExpandedObject obj);
	
	public void keepSatisfying(InstanceSet set) {
		Iterator<ExpandedObject> iter = set.instances.iterator();
		while(iter.hasNext()) {
			ExpandedObject obj = iter.next();
			if(!satisfies(obj))
				iter.remove();
		}
	}
	
	public void keepNotSatisfying(InstanceSet set) {
		Iterator<ExpandedObject> iter = set.instances.iterator();
		while(iter.hasNext()) {
			ExpandedObject obj = iter.next();
			if(satisfies(obj))
				iter.remove();
		}
	}
}
