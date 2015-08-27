package main.java.edu.illinois.cs.cogcomp.wikifier.utils.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
/**
 * Simply controls for command line output
 * @author cheng88
 *
 */
public class Console {
    
    public static final PrintStream stdout = System.out;
    private static final PrintStream nullout = new PrintStream(new OutputStream() {
        @Override
        public void write(int b) throws IOException {
        }
    });
    
    /**
     * Mutes stdout
     */
    public static void silence(){
        System.setOut(nullout);
    }
    
    /**
     * Restores stdout
     */
    public static void enable(){
        System.setOut(stdout);
    }
    

}
