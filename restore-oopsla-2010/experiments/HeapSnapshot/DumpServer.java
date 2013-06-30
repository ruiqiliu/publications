

import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.EOFException;

import java.util.Date;

/**
 * This hacky class is used to trigger a heap dump on a running VM.  To
 * use, start this program like this: <pre>
 *
 *     java NetDumper /tmp/foo.hprof 7000
 *
 * </pre>
 * This will cause it to listen to a ServerSocket on port 7000.  Next,
 * invoke the program you want a heap dump for, like this: <pre>
 *
 *     java -Xhprof:net=localhost:7000,format=b com.random.MyProgram
 * 
 * </pre>
 * It will connect to NetDumper.  Whenever you press return in the NetDumper
 * window, it will request a heap dump, and write it to /tmp/foo.hprof.
 **/

public class DumpServer implements Runnable {

	//
	// Data members
	//
	private InputStream in;
	private OutputStream out;
	private int identifierSize;
	private byte[] buf = new byte[16 * 1024];

	public DumpServer(InputStream in, OutputStream out) {
		this.in = in;
		this.out = out;
	}

	public void run() {
		try {
			for(;;) {
				int read = in.read(buf);
				if (read == -1) {
					System.out.println("EOF seen, terminating.");
					out.close();
					System.exit(0);
				}
				out.write(buf, 0, read);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			System.exit(1);
		}
	}


	private static void usage() {
		System.out.println("Usage:  java NetDumper <file> <port>");
		System.exit(1);
	}

	public static void main(String args[]) {
		/*
		if (args.length != 2) {
			usage();
		}*/
		try {
			String file = "snapshot-";
			int file_count = 0;
			int dump_port = 7000;
			int control_port = dump_port + 1;
			ServerSocket ss_dump = new ServerSocket(dump_port);
			ServerSocket ss_cont = new ServerSocket(control_port);

			System.out.println("Listenening for dumps on port " + dump_port);
			System.out.println("Listenening for control on port " + control_port);
			Socket dump_s = ss_dump.accept();

			while(true) {
				Socket cont_s = ss_cont.accept();
				InputStream dump_in = new BufferedInputStream(dump_s.getInputStream());
				InputStream cont_in = new BufferedInputStream(cont_s.getInputStream());
				DataOutputStream dump_out = new DataOutputStream(dump_s.getOutputStream());
				DataOutputStream cont_out = new DataOutputStream(cont_s.getOutputStream());
				while(true) {
					int ch = cont_in.read();
					if (ch == -1) {
						System.out.println("EOF on control.");
						break;
					}
					if (ch == '\n')
						break;
				}
				System.out.println("Requesting heap dump.");
				dump_out.writeByte(2);	// HPROF_CMD_DUMP_HEAP
				dump_out.writeInt(1);	// seq_num
				dump_out.writeInt(0);	// length
				dump_out.flush();
				String fcount = String.format("%02d", file_count);
				String filename = file + fcount + ".hprof";
				System.out.println("Writing to file " + filename);
				OutputStream file_out = new BufferedOutputStream(new FileOutputStream(filename));
				file_count++;
				readNetwork(dump_in,file_out);
				file_out.close();
				System.out.println("Done writing.");
				cont_out.write(0);  // signal that dump is finished
				System.out.println("Signaled that dump is finished.");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

  static void readNetwork(InputStream in, OutputStream out) throws InterruptedException {
    byte[] buf = new byte[16 * 1024];
    int timeout = 1000;
    boolean timeout_triggered = false;
  	try {
  		while(true) {
  			if(in.available() == 0) {
  				if(timeout_triggered) {
  					System.out.println("done reading");
  					return;
  				}
  				else {
  					System.out.println("timeout triggered");
  					timeout_triggered = true;
  					Thread.sleep(timeout);
  					continue;
  				}
  			}
  			else
  				timeout_triggered = false;
  			int read = in.read(buf);
  			if (read == -1) {
  				out.close();
  				System.exit(0);
  			}
  			out.write(buf, 0, read);
	    }
  	} catch (IOException ex) {
	    ex.printStackTrace();
	    System.exit(1);
  	}    	
  }
}

