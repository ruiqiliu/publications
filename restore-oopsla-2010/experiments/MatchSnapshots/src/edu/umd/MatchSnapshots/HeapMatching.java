package edu.umd.MatchSnapshots;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import edu.umd.MatchSnapshots.ExpandedObjects.ExpandedObject;
import edu.umd.MatchSnapshots.Synthesis.Example;
import edu.umd.MatchSnapshots.Synthesis.SynthFuncs.SynthFun;

/**
 * Records global matching information:
 *   - For each class: the stable fields and the key fields or key function
 *   - For each old2 object: the address of the old1 object to which it corresponds
 *   - For each new object: the address of the old1 object to which it corresponds
 * Thus, old1 addresses become unique labels for corresponding objects and if the corresponding
 * old1 address for an object has not yet been computed, it can be derived by looking at the
 * per-class key field / function information.
 *  
 * If needed, this could be further broken down.  i.e. instead of per-class key fields, we could have
 * per-region key fields (enabling collections of objects reachable through one heap path to have
 * difference matching characteristics than those from another region).
 *  
 * @author smagill
 *
 */
public class HeapMatching {
	public class MatchInfo {
		public List<FieldPath> stablefields;
		public List<FieldPath> keyfields;
		public SynthFun matchfun;
		
		public Boolean matches(ExpandedObject o1, ExpandedObject o2) {
			if(keyfields != null) {
				return allFieldsMatch(keyfields, o1, o2);
			}
			else if(matchfun != null) {
				return matchfun.consistentWith(new Example(o1,o2));
			}
			else
				return null;
		}
		
		private boolean allFieldsMatch(List<FieldPath> keyfields2,
				ExpandedObject o1, ExpandedObject o2) {
			for(FieldPath field : keyfields2) {
				if(!o1.getField(field).equals(o2.getField(field)))
					return false;
			}
			return true;
		}
	}
	
	HashMap<String,MatchInfo> classMatching;
	HashMap<Long,Long> old2ToOld1;
	HashMap<Long,Long> newToOld1;
	
	public void traverseFrom(ExpandedObject old1, ExpandedObject old2, ExpandedObject newObj) {
		HashSet<ExpandedObject> seen;
		LinkedList<LinkedList<ExpandedObject>> old1stack = new LinkedList<LinkedList<ExpandedObject>>();
		
		LinkedList<ExpandedObject> initList = new LinkedList<ExpandedObject>();
		initList.add(old1);
		old1stack.push(initList);
		while(!old1stack.isEmpty()) {
			if(old1stack.peek().isEmpty())
				old1stack.pop();
			ExpandedObject old1top = old1stack.peek().pop();
			List<FieldPath> fields = old1top.getFields();
			// TODO: this is incomplete
		}
	}
}
