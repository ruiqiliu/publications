package edu.umd.MatchSnapshots.Synthesis;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;

import edu.umd.MatchSnapshots.FieldPath;
import edu.umd.MatchSnapshots.Pair;
import edu.umd.MatchSnapshots.ExpandedObjects.ExpandedObject;
import edu.umd.MatchSnapshots.ExpandedObjects.GenericObject;

/**
 * An input / output example for synthesis.  For top-level synthesis, o1 and o2 should both be non-null.
 * For collection synthesis, o1 == null indicates that the output collection contains more elements
 * than the input collection (and o2 is an example of an output object with no corresponding input).
 * This indicates that multiple collections may have been combined in the new version.
 * If o2 == null, it indicates that the output collection contains less objects than the input collection
 * (and o1 is an example of an input object that was dropped).  This indicates that some old-version
 * objects were either removed entirely in the new version or were placed in a separate collection. 
 * @author smagill
 *
 */
public class Example {
	int example_num;
	static int current_num = 0;
	ExpandedObject oldObj1;
	ExpandedObject oldObj2;
	ExpandedObject newObj;

	public Example(Pair<ExpandedObject,ExpandedObject> example) {
		example_num = current_num++;
		oldObj1 = example.getFirst();
		oldObj2 = oldObj1;
		newObj = example.getSecond();
	}
	
	public Example(ExpandedObject o1, ExpandedObject o2) {
		example_num = current_num++;
		oldObj1 = o1;
		oldObj2 = o1;
		newObj = o2;
	}

	public Example(ExpandedObject old1, ExpandedObject old2, ExpandedObject newObj) {
		example_num = current_num++;
		this.oldObj1 = old1;
		this.oldObj2 = old2;
		this.newObj = newObj;
	}
	
	public ExpandedObject getOld() { return oldObj1; }
	public ExpandedObject getSecondOld() { return oldObj2; }
	public ExpandedObject getNew() { return newObj; }
	public StringBuffer toHTML() {
		StringBuffer buf = new StringBuffer();
		exampleFieldsToHTML(buf, oldObj1, newObj);
		return buf;
	}
	public int getNum() { return example_num; }
	static void exampleObjToHTML(StringBuffer buf, ExpandedObject obj) {
		if(obj == null) {
			buf.append("N/A");
			return;			
		}
		if(!(obj instanceof GenericObject)) {
			String str = obj.toString();
			String ret = str.replace("\n", "<br>\n");
			buf.append(ret);
			return;
		}
		else {
			GenericObject obj1 = (GenericObject) obj;
			if(obj1.fields == null) {
				buf.append("null");
				return;
			}
			buf.append("<table>");
			for(String fieldName : obj1.fields.keySet()) {
				FieldPath field = new FieldPath(fieldName);
				buf.append("<tr valign=top><td>" + field.toString() + "</td>");
				buf.append("<td>");
				exampleObjToHTML(buf,obj1.getField(field));
				buf.append("</td></tr>");
			}
			buf.append("</table>");
		}
	}
	static void exampleFieldsToHTML(StringBuffer buf, ExpandedObject oldObj, ExpandedObject newObj) {
		if(oldObj instanceof GenericObject && newObj instanceof GenericObject) {
			buf.append("<table>");
			GenericObject obj1 = (GenericObject) oldObj;
			GenericObject obj2 = (GenericObject) newObj;
			if(obj1.fields == null || obj2.fields == null) {
				if(obj1.fields == null)
					buf.append("<td>null</td>");
				else
					buf.append("<td>non-null</td>");
				if(obj2.fields == null)
					buf.append("<td>null</td>");
				else
					buf.append("<td>non-null</td>");
				buf.append("</table>");
				return;					
			}
			for(String fieldName : obj1.fields.keySet()) {
				FieldPath field = new FieldPath(fieldName);
				buf.append("<tr valign=top><td>" + field.toString() + "</td>");
				buf.append("<td>");
				exampleFieldsToHTML(buf,oldObj.getField(field), newObj.getField(field));
				buf.append("</td></tr>");
			}
			buf.append("</table>");
		}
		else {
			buf.append("<table>");
			buf.append("<tr><td>");
			exampleObjToHTML(buf, oldObj);
			buf.append("</td><td>");
			exampleObjToHTML(buf, newObj);
			buf.append("</td></tr>");
			buf.append("</table>");
		}
	}

	public static void examplesToHTML(List<Example> exList, String filename) {
		try {
			PrintStream out = new PrintStream(new FileOutputStream(filename));
			out.println("<html><head><title>Examples</title></head><body>");
			
			for(Example ex : exList) {
				out.println("<h2>Example " + ex.getNum() + "</h2>");
				out.println(ex.toHTML());
			}
		}
		catch(FileNotFoundException e) {
			System.out.println("Could not create file examples.html");
			System.exit(-1);
		}		
	}	
}