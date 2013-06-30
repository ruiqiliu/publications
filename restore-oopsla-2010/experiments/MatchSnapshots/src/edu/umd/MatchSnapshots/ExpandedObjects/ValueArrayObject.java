package edu.umd.MatchSnapshots.ExpandedObjects;

import java.util.ArrayList;
import java.util.List;

public class ValueArrayObject extends CollectionObject {
	String element_type;
	
	public ValueArrayObject(String type, Object array) {
		super(type, null, true); // we fill in "elements" later
		ArrayList<ExpandedObject> elements = new ArrayList<ExpandedObject>();
		if(array instanceof byte[]) {
			element_type = "byte";
			byte[] tarray = (byte[]) array;
			for(int i = 0; i < tarray.length; i++)
				elements.add(new ByteObject(tarray[i]));
		}
		else if(array instanceof boolean[]) {
			element_type = "boolean";
			boolean[] tarray = (boolean[]) array;
			for(int i = 0; i < tarray.length; i++)
				elements.add(new BoolObject(tarray[i]));
		}
		else if(array instanceof char[]) {
			element_type = "char";
			char[] tarray = (char[]) array;
			for(int i = 0; i < tarray.length; i++)
				elements.add(new CharObject(tarray[i]));
		}
		else if(array instanceof short[]) {
			element_type = "short";
			short[] tarray = (short[]) array;
			for(int i = 0; i < tarray.length; i++)
				elements.add(new IntObject(tarray[i]));
		}
		else if(array instanceof int[]) {
			element_type = "int";
			int[] tarray = (int[]) array;
			for(int i = 0; i < tarray.length; i++)
				elements.add(new IntObject(tarray[i]));
		}
		else if(array instanceof float[]) {
			element_type = "float";
			float[] tarray = (float[]) array;
			for(int i = 0; i < tarray.length; i++)
				elements.add(new FloatObject(tarray[i]));
		}
		else if(array instanceof long[]) {
			element_type = "long";
			long[] tarray = (long[]) array;
			for(int i = 0; i < tarray.length; i++)
				elements.add(new LongObject(tarray[i]));
		}
		else if(array instanceof double[]) {
			element_type = "double";
			double[] tarray = (double[]) array;
			for(int i = 0; i < tarray.length; i++)
				elements.add(new DoubleObject(tarray[i]));
		}
		
		this.elements = elements;
		this.type = type;
	}

	public String getType() { return type + "[]"; }

	@Override
	public String toString() {
		return element_type + " array.";
	}
	
	@Override
	//TODO: we might have to iterate over the elements here.
	public boolean equals(Object other) {
		return elements.equals(other);
	}
}
