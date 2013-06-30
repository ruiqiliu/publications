/**
 * Heap snapshot class for state correction work with Mike Hicks,
 * Kathryn McKinley, and Suriya Subramanian.
 *
 * Portions of the code are taken from A. Sundararajan's blog at:
 *   http://blogs.oracle.com/sundararajan/entry/programmatically_dumping_heap_from_java
 */

package edu.umd.objdump;

import javax.management.MBeanServer;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.ServerSocket;
import java.net.Socket;

import com.sun.management.HotSpotDiagnosticMXBean;

public class HeapSnapshot {
    // This is the name of the HotSpot Diagnostic MBean
    private static final String HOTSPOT_BEAN_NAME =
	"com.sun.management:type=HotSpotDiagnostic";

    // field to store the hotspot diagnostic MBean 
    private static volatile HotSpotDiagnosticMXBean hotspotMBean;

    /**
     * Call this method from your application whenever you 
     * want to dump the heap snapshot into a file.
     *
     * @param fileName name of the heap dump file
     * @param live flag that tells whether to dump
     *             only the live objects
     */
    static void dumpHeap(String fileName, boolean live) {
    	/* Old way -- doesn't store string constants
        // initialize hotspot diagnostic MBean
        initHotspotMBean();
        try {
            hotspotMBean.dumpHeap(fileName, live);
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception exp) {
            throw new RuntimeException(exp);
        }
        */
    	try {
  			int control_port = 7001;
  			Socket cont_s = new Socket("localhost",control_port);
  			InputStream cont_in = new BufferedInputStream(cont_s.getInputStream());
  			DataOutputStream cont_out = new DataOutputStream(cont_s.getOutputStream());
  			System.out.println("triggering snapshot");
  			cont_out.write('\n');
  			System.out.println("waiting for snapshot");
  			cont_in.read();
  			System.out.println("snapshot done");
  			cont_out.close();
  			cont_s.close();
    	}
    	catch (Exception ex) {
    		ex.printStackTrace();
    	}    	
    }
    	
    // initialize the hotspot diagnostic MBean field
    private static void initHotspotMBean() {
        if (hotspotMBean == null) {
            synchronized (HeapSnapshot.class) {
                if (hotspotMBean == null) {
                    hotspotMBean = getHotspotMBean();
                }
            }
        }
    }

    // get the hotspot diagnostic MBean from the
    // platform MBean server
    private static HotSpotDiagnosticMXBean getHotspotMBean() {
        try {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            HotSpotDiagnosticMXBean bean = 
                ManagementFactory.newPlatformMXBeanProxy(server,
							 HOTSPOT_BEAN_NAME, HotSpotDiagnosticMXBean.class);
            return bean;
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception exp) {
            throw new RuntimeException(exp);
        }
    }

  private static int getPID() {
    String pidString = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
    int indexOfAt = pidString.indexOf('@');
    if (indexOfAt >= 0) {
      return Integer.parseInt(pidString.substring(0, indexOfAt));
    } else {
      return Integer.parseInt(pidString);
    }
  }

  private static int snapshotNumber = 0;

  public static void makeSnapshot() throws Exception {
    int pid = getPID();
    String filename = "snapshot-" + pid + "-" + snapshotNumber + ".hprof";
    //String command = "jmap -dump:live,format=b,file=" + filename + " " + pid;
    //Runtime.getRuntime().exec(command).waitFor();
    dumpHeap(filename,true);
    snapshotNumber++;
  }
}
