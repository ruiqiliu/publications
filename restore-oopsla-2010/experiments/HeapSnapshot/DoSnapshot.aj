import org.gudy.azureus2.ui.console.CommandReader;
import org.gudy.azureus2.ui.console.ConsoleInput;
import edu.umd.objdump.*;
import java.io.IOException;

public aspect DoSnapshot {
	before(): call(String CommandReader.readLine()) && withincode(void ConsoleInput.run()) {
		System.out.println("loop");
		System.gc();
		try { HeapSnapshot.makeSnapshot(); }
		catch(Exception e) {System.out.println("Exception while making heap snapshot.");}
	}
}
