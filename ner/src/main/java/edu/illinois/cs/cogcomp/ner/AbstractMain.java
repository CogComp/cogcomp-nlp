/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
/**
 * 
 */
package edu.illinois.cs.cogcomp.ner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * This is an abstract base class that can be used build a menu driven command line interface.
 * Processing state (e.g. what submenu you are on) is maintained by subclasses. This class extends
 * Thread, the thread starts in the constructure.
 * <p>
 * Menus and prompts are displayed on standard output. The {@link inputMenu} method is called to
 * render the current menu to standard out. When the user issues a selection on standard output, the
 * {@link processCommand} method is called to perform whatever required action.
 * <p>
 * There are also abstract methods that parse command line arguments ({@link processArguments} and
 * to present the command line syntax ({@link getCommandSyntax}) in the event of a command line
 * argument error. Any exception thrown by the processArguments method will result in the command
 * line syntax being displayed, then the system will exit. Subclasses may also call the
 * {@link errorProcessingArguments} method with a message argument to provide a custom error
 * message. If the processArguments method throws an exception, the message associated with that
 * exception will be displayed along with the command line syntax.
 * 
 * @author redman
 */
abstract public class AbstractMain extends Thread {

    /**
     * This class takes a list of command line arguments. Typically a configuration file will be
     * passed in.
     * 
     * @param args the arguments.
     */
    protected AbstractMain(String[] args) {}

    /**
     * this method is called if there is a catastrophic error processing arguments. It will report
     * the message, report the command syntax, and exit.
     * 
     * @param message the error message to report before the command line syntax.
     */
    protected void errorProcessingArguments(String message) {
        System.err.println(message);
        System.err.println(this.getCommandSyntax());
        System.exit(-1);
    }

    /**
     * this method will traverse all arguments calling the processArgument method with each
     * argument.
     * 
     * @param args the array of arguments.
     */
    protected void processArguments(String[] args) {

        // traverse the args.
        for (int i = 0; i < args.length; i++) {
            try {
                processArgument(args, i);
            } catch (Exception e) {
                this.errorProcessingArguments(e.getMessage());
            }
        }
    }

    /** the buffered reader. */
    BufferedReader bis = new BufferedReader(new InputStreamReader(System.in));

    /**
     * Present a menu indicating the current display and providing an input menu, fetch the next
     * line, the process the assumed command there.
     */
    @Override
    public void run() {
        while (true) {
            this.inputMenu();
            String line;
            try {
                line = bis.readLine();
            } catch (IOException e1) {
                e1.printStackTrace();
                System.err.println("Bye");
                System.exit(0);
                return;
            }
            try {
                this.processCommand(line);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
            }
            if (Thread.currentThread().isInterrupted()) {
                System.err.println("Bye");
                System.exit(0);
            }
        }
    }

    /**
     * Process the command provided in line.
     * 
     * @param line the command to process.
     * @throws Exception
     */
    abstract protected void processCommand(String line) throws Exception;

    /**
     * present the input menu to standard out.
     */
    abstract protected void inputMenu();

    /**
     * This method will return a string which documents the command line arguments before a system
     * exit.
     * 
     * @return the command line systex documented in a string.
     */
    abstract protected String getCommandSyntax();

    /**
     * This will process the current argument, and return the integer indicating the next argument
     * to be processed. This mechanism will allow the implementer to pull additional arguments off
     * the stack as needed.
     * 
     * @param args the arguments.
     * @param current the index of the current argument.
     * @return the index of the next argument.
     * @throws Exception if fatal error, throw an exception will cause a system exit.
     */
    abstract protected int processArgument(String[] args, int current) throws Exception;
}
