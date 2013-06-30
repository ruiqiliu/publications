package edu.umd.MatchSnapshots.Synthesis.SynthFuncs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.umd.MatchSnapshots.FieldPath;
import edu.umd.MatchSnapshots.Pair;
import edu.umd.MatchSnapshots.ExpandedObjects.ExpandedObject;
import edu.umd.MatchSnapshots.ExpandedObjects.GenericObject;
import edu.umd.MatchSnapshots.ExpandedObjects.StringObject;
import edu.umd.MatchSnapshots.Synthesis.Example;

public class StringSegment extends SynthFun {
	private static final char[] delims = {'@',':','.','/','\\','*'};
	/* We imagine the old-version field as split into substrings at instances of the delims.
	   The integers in the following list then represent
	     1. The index into the array of substrings if the integer is positive
	     2. The (negation of) the delimiter that should be inserted at this position in the output
	        string if the integer is negative. */
	private FieldPath source_field;
	private List<Integer> transfer_fun;
	
	private StringSegment(String className, FieldPath source_field, FieldPath dest_field) {
		super(className, dest_field);
		this.source_field = source_field;
		transfer_fun = new LinkedList<Integer>();
	}
	
	public StringSegment clone() {
		StringSegment ret = new StringSegment(className, source_field, field);
		for(Integer i : transfer_fun)
			ret.transfer_fun.add(i);
		return ret;
	}
	
	/** Returns an instance of this transformer function if there is an instance that would be
	 *  consistent with the given example. */
	public static StringSegment getSynthFun(String className, FieldPath field, Example ex) {
		ExpandedObject oldObj = ex.getOld();
		List<FieldPath> fields = oldObj.getFields();
		
		for(FieldPath source_field : fields) {
			StringSegment result = getSynthFunForField(className, source_field, field, ex);
			if(result != null)
				return result;
		}
		return null;
	}
	
	public static StringSegment getSynthFunForField(String className, FieldPath source_field, FieldPath dest_field, Example ex) {
		ExpandedObject newObj = ex.getNew().getField(dest_field);
		ExpandedObject oldObj = ex.getOld().getField(source_field);

		if(!(newObj instanceof StringObject && oldObj instanceof StringObject))
			return null;

		String newStr = getNewExample(ex,dest_field);
		String oldStr = getOldExample(ex,source_field);
		StringSegment ret = new StringSegment(className, source_field, dest_field);
		List<Integer> transfer_fun = new LinkedList<Integer>();
		String regex = getDelimRegex();
		String[] split = oldStr.split(regex);
		int curr_split = 0;
		while(curr_split != split.length && !newStr.isEmpty()) {
			if(newStr.startsWith(split[curr_split])) {
				transfer_fun.add(curr_split);
				newStr = newStr.substring(split[curr_split].length());
			}
			else {
				int delim = startsWithDelim(newStr);
				if(delim != 0) {
					transfer_fun.add(-delim);
					newStr = newStr.substring(1);
					curr_split--;
				}
			}
			curr_split++;
		}
		
		if(!newStr.isEmpty())
			return null;
		
		ret.transfer_fun = transfer_fun;
		return ret;
	}
	
	static String getDelimRegex() {
		StringBuffer regex = new StringBuffer("[");
		for(int i = 0; i < delims.length; i++)
			regex.append("\\" + delims[i]);
		regex.append("]");
		return regex.toString();
	}
	
	static int startsWithDelim(String str) {
		String regex = getDelimRegex() + ".*";
		if(str.matches(regex))
			return str.codePointAt(0);
		else
			return 0;
	}
	
	// Returns the character at pos in str if that character is one of the delimiters in delims
	// If there is no character at pos (the string is too short) or the character is not a
	// delimiter then this function returns 0.
	private static char delimiterAt(String str, int pos) {
		char c = str.charAt(pos);
		for(int i = 0; i < delims.length; i++)
			if(c == delims[i])
				return c;
		return 0;
	}
	
	// Looks up the delimiter at position pos in string str.  It then returns an integer i such that
	// str[pos] is the i^th occurrence of the delimiter in str.
	private static int delimiterNum(String str, int pos) {
		char delim = str.charAt(pos);
		int ret = 0;
		int match_pos = str.indexOf(delim, ret);
		while(match_pos != pos) {
			ret = match_pos + 1;
			match_pos = str.indexOf(delim, ret);
		}
		return ret;
	}
	
	// REVISE BELOW
	
	public String toString() {
		StringBuffer ret = new StringBuffer();
		Iterator<Integer> iter = transfer_fun.iterator();
		int[] charArray = new int[1];
		ret.append("String[] splits = splitString(" + make_old(source_field) + ");\n");
		ret.append(field + " = ");
		while(iter.hasNext()) {
			int val = iter.next();
			if(val < 0) {
				charArray[0] = -val;
				ret.append('\'');
				ret.append(new String(charArray,0,1));
				ret.append('\'');
			}
			else
				ret.append("splits[" + val + "]");
			if(iter.hasNext())
				ret.append(" + ");
			else
				ret.append(";\n");
		}
		return ret.toString();
	}
	
	private static String getOldExample(Example ex, FieldPath field) {
		return ((StringObject)ex.getOld().getField(field)).val;
	}

	private static String getNewExample(Example ex, FieldPath field) {
		return ((StringObject)ex.getNew().getField(field)).val;
	}
	
	private static String clipString(String str,
			Pair<Character,Integer> start, Pair<Character,Integer> end) {
		int startPos = 0;
		int startMatch = -1;
		if(start.getSecond() != -1)
			while(startMatch != start.getSecond()) {
				startMatch++;
				startPos = str.indexOf(start.getFirst(), startPos) + 1;
			}
		int endPos = str.length() - 1;
		int endMatch = -1;
		if(end.getSecond() != -1)
			while(endMatch != end.getSecond()) {
				endMatch++;
				endPos = str.indexOf(end.getFirst(), endPos) + 1;
			}
		return str.substring(startPos, endPos);
	}
	
	@Override
	public int size() { return 1; }
	
	public boolean consistentWith(Example ex) {
		String newStr = getNewExample(ex,field);
		String oldStr = getOldExample(ex,source_field);
		String regex = getDelimRegex();
		String[] split = oldStr.split(regex);
		for(Integer i : transfer_fun) {
			if(i >= 0) {
				if(i < split.length && newStr.startsWith(split[i]))
					newStr = newStr.substring(split[i].length());
				else
					return false;
			}
			else {
				if(!newStr.isEmpty() && newStr.codePointAt(0) == -i)
					newStr = newStr.substring(1);
				else
					return false;
			}
		}
		if(newStr.isEmpty())
			return true;
		else
			return false;
	}
}