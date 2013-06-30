import org.gudy.azureus2.core3.peer.impl.transport.sharedport.PESharedPortServerImpl;
import java.io.*;

public aspect PrintAllocations {
    private int PESharedPortServerImpl.IMPLICIT_alloc_num;
    private int PESharedPortServerImpl.IMPLICIT_alloc_point;

    static class Count {
	public static int count;
	public static int getCount() { return count; }
	public static int getAndIncr() { return count++; }
    }

    after() returning(PESharedPortServerImpl impl): call(PESharedPortServerImpl.new()) {
	PrintStream out = null;
	try {
	    out = new PrintStream(new FileOutputStream("alloc.txt",true));
	    impl.IMPLICIT_alloc_num = Count.getAndIncr();
	    impl.IMPLICIT_alloc_point = thisJoinPointStaticPart.getSourceLocation().getLine();
	}
	catch(FileNotFoundException e) {
	    System.out.println("File not found.");
	}
	StackTraceElement[] es = Thread.currentThread().getStackTrace();
	for(int i = 0 ; i < es.length ; i++) {
	    out.println(es[i]);
	}
	out.println();
	out.close();
    }
}