package edu.umd.MatchSnapshots;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.sun.tools.hat.internal.model.*;
import com.sun.tools.hat.internal.parser.Reader;

import edu.umd.MatchSnapshots.ExpandedObjects.*;

public class HeapSnapshot {
  protected Snapshot snapshot;
  
  // Creates a HeapSnapshot based on a snapshot file.
  HeapSnapshot(File file) {
  	try {
	  	this.snapshot = Reader.readFile(file.getAbsolutePath(), false, 0);
	  	snapshot.resolve(true);
  	}
  	catch(IOException e) {
  		System.out.println("Error opening file: " + file);
  		System.exit(-1);
  	}
  }
  HeapSnapshot(String filename) {
  	this(new File(filename));
  }

    public LinkedList<String> getClasses() {
	Iterator<JavaClass> iter = snapshot.getClasses();
	LinkedList<String> set = new LinkedList<String>();
	while(iter.hasNext()) {
	    JavaClass cl = iter.next();
	    set.add(cl.getName());
	}
	return set;
    }

    public int numClasses() {
	Iterator<JavaClass> iter = snapshot.getClasses();
	int result = 0;
	while(iter.hasNext()) {
	    result++;
	    iter.next();
	}
	return result;
    }
  
	public InstanceSet getObjectsOfClass(String cl, boolean use_static) {
			if(!use_static) {
					InstanceSet set = new InstanceSet();
					JavaClass cls = snapshot.findClass(cl);
					if(cls == null) {
							System.out.println("Class " + cl + " not found.");
							return set;
					}
					Enumeration<JavaHeapObject> instances = cls.getInstances(true);
					if(instances == null) {
							return set;
					}
					while(instances.hasMoreElements()) {
							JavaHeapObject o = instances.nextElement();
							ReferenceChain[] refs = snapshot.rootsetReferencesTo(o,false);
							if(refs.length > 0)
									set.addInstance(expandObject(o,3));
					}
					return set;
			}
			else {
					InstanceSet set = new InstanceSet();
					JavaClass cls = snapshot.findClass(cl);
					if(cls == null) {
							System.out.println("Class " + cl + " not found.");
							return set;
					}
					HashMap<String, ExpandedObject> eobj_fields = new HashMap<String, ExpandedObject>();
					JavaStatic[] fields = cls.getStatics();
					for(JavaStatic field : fields) {
							ExpandedObject obj = expandObject(field.getValue(),2);
							eobj_fields.put(field.getField().getName(), obj);
					}
					set.addInstance(new GenericObject(eobj_fields, cl));
					return set;
			}
	}

  public ExpandedObject getFirstObjectOfClass(String cl) {
  	JavaClass cls = snapshot.findClass(cl);
  	if(cls == null)
	    return null;
  	Enumeration<JavaHeapObject> instances = snapshot.findClass(cl).getInstances(true);
  	if(instances == null)
	    return null;
	if(!instances.hasMoreElements())
	    return null;
	JavaHeapObject o = instances.nextElement();
	return expandObject(o,3);
  }

  public int countObjectsOfClass(String cl) {
  	JavaClass cls = snapshot.findClass(cl);
	int result = 0;
  	if(cls == null)
	    return 0;
  	Enumeration<JavaHeapObject> instances = snapshot.findClass(cl).getInstances(true);
  	if(instances == null)
	    return 0;
  	while(instances.hasMoreElements()) {
	    result++;
	    instances.nextElement();
  	}
  	return result;
  }
  
  protected ExpandedObject expandObject(JavaThing o, int depth_bound) {
  	if(o == null)
  		return new GenericObject("Unknown type null object");
  	if(o instanceof JavaObject) {
  		JavaObject obj = (JavaObject) o;
  		if(obj.getClazz().getName().equals("java.lang.String")) {
  			// toString() in the jhat source is not correct.  It does not account for the fact that
  			// the string may be a subset of another string.
  			int offset = Integer.parseInt(obj.getField("offset").toString());
  			int count = Integer.parseInt(obj.getField("count").toString());
			String ret = null;
			try {
			    ret = obj.toString().substring(offset, offset + count);
			}
			catch(java.lang.StringIndexOutOfBoundsException e) {
			    /* This happens if the snapshot did not capture
			       static strings. */
			}
  			return new StringObject(ret);
  		}
  		else {
  			// Generic Object
      	if(depth_bound == 0)
      		return new GenericObject(obj.getClazz().getName());
  			HashMap<String, ExpandedObject> eobj_fields = new HashMap<String, ExpandedObject>();
	  		JavaField[] fields = obj.getClazz().getFieldsForInstance();
	  		for(JavaField field : fields) {
	  			ExpandedObject recObj = expandObject(obj.getField(field.getName()), depth_bound - 1);
	  			eobj_fields.put(field.getName(),recObj);
	  		}
	  		return new GenericObject(eobj_fields, obj.getClazz().getName());
  		}
  	}
  	else if(o instanceof JavaInt) {
  		JavaInt i = (JavaInt) o;
  		return new IntObject(Integer.parseInt(i.toString()));
  	}
  	else if(o instanceof JavaByte) {
  		JavaByte b = (JavaByte) o;
  		String str = b.toString();
  		return new ByteObject(Integer.decode(str).byteValue());
  		//return new ByteObject(Byte.parseByte(str.substring(str.lastIndexOf('x')+1),16));
  	}
  	else if(o instanceof JavaLong) {
  		JavaLong i = (JavaLong) o;
  		return new LongObject(Long.parseLong(i.toString()));
  	}
  	else if(o instanceof JavaFloat) {
  		JavaFloat f = (JavaFloat) o;
  		return new FloatObject(Float.parseFloat(f.toString()));
  	}
  	else if(o instanceof JavaBoolean) {
  		JavaBoolean b = (JavaBoolean) o;
  		return new BoolObject(Boolean.parseBoolean(b.toString()));
  	}
  	else if(o instanceof JavaObjectArray) {
  		JavaObjectArray a = (JavaObjectArray) o;
    	if(depth_bound == 0)
    		return new GenericObject(a.getClazz().getName());
  		LinkedList<ExpandedObject> elements = new LinkedList<ExpandedObject>();
  		for(JavaThing thing : a.getElements())
  			elements.add(expandObject(thing, depth_bound - 1 ));
  		return new CollectionObject(a.getClazz().getName(), elements, true);
  	}
  	else if(o instanceof JavaValueArray) {
  		JavaValueArray a = (JavaValueArray) o;
    	if(depth_bound == 0)
    		return new GenericObject(a.getClazz().getName());
  		return new ValueArrayObject(a.getClazz().getName(), a.getElements());
  	}
  	else if(o instanceof HackJavaValue) {
  		if(o.getSize() == 0)
  			return new GenericObject(o.toString());
  		else {
  			System.out.println("Unhandled type encountered (JavaHackValue).");
			System.out.println(o);
			System.out.println(o.getSize());
			return null;
  		}
  	}
	System.out.println("Unhandled type encountered.");
  	System.out.println(o);
  	System.out.println(((Object)o).getClass());
  	return null;
  }
}
