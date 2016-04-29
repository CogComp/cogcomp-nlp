/**
 * This software is released under the University of Illinois/Research and
 *  Academic Use License. See the LICENSE file in the root folder for details.
 * Copyright (c) 2016
 *
 * Developed by:
 * The Cognitive Computation Group
 * University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
/**
 * 
 */
package edu.illinois.cs.cogcomp.ner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotationUtilities;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.ner.IO.InFile;
import edu.illinois.cs.cogcomp.ner.config.NerBaseConfigurator;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;

/**
 * Process command line NER request in the simplest most untuitive possible ways.
 * @author redman
 */
public class Main extends AbstractMain {
    /**
     * enumerates the various input processing states.
     */
    enum InputSwitch {
        
        /** we are in menu page. */
        MENU,
        
        /** enter the input file or directory. */
        ENTER_IN,
        
        /** enter the output file or directory. */
        ENTER_OUT,
        
        /** enter a string to process. */
        ENTER_STRING,
        
        /** show the configuration parameters. */
        SHOW_CONFIG
    };
    
    /** set to indicate a property has changed required a reload of the ner annotator. */
    private boolean changedproperty = false;
    
    /** our current input state. */
    InputSwitch inswitch = InputSwitch.MENU;
    
    /** input directory (or file) containing data to run. */
    protected File indirectory = null;
    
    /** output directory (or file) for resulting tagged data. */
    protected File outdirectory = null;
    
    /** this helper can create text annotations from text. */
    protected final TextAnnotationBuilder tab = new TokenizerTextAnnotationBuilder(new StatefulTokenizer());
    
    /** the NER annotator. */
    protected NERAnnotator nerAnnotator = null;
    
    /** the result processor determines how to produce output, on standard out, a single file or files in a directory. */
    private ResultProcessor rp = null;
    
    /** the resource manager contains all the properties. */
    private ResourceManager resourceManager;
    
    /**
     * The only argument we take is the config file.
     * @param args
     */
    public Main(String[] args) {
        super(args);
    }

    /**
     * The only argument is {@code <config_file_name>}. This method will capture the config
     * file name, and initialize the system when it does.
     * @throws IOException 
     * @see edu.illinois.cs.cogcomp.ner.AbstractMain#processArgument(java.lang.String[], int)
     */
    @Override
    protected int processArgument(String[] args, int current) throws Exception {
        if (current > 0)
            throw new RuntimeException("This program takes only one argument.");
        else {
            if (new File(args[current]).exists()) {
                System.out.println("Loading properties from "+args[current]);
                this.resourceManager = new ResourceManager(args[current]);
                System.out.println("Completed loading properties.");
            } else
                throw new RuntimeException("The configuration file \""+args[current]+"\" did not exist.");
        }
        return current++;
    }

    @Override
    protected String getCommandSyntax() {
        return 
            "java -Xms2g edu.illinois.cs.cogcomp.ner.Main <config_file_name>\n" +
            "    <config_file_name> : specify the location of a configuration file.";
    }
    
    /**
     * We can be processing menu commands, input file or directory names, output file or directory
     * names, or we may be entering a string for NER processing directly. Input can come from a 
     * file or directory or standard out, output can to go to a file or a directory or standard out.
     * @throws Exception 
     */
    @Override
    protected void processCommand(String line) throws Exception {
        switch (inswitch) {
            case MENU:
                if (line.length() == 0) {
                    System.err.println("There is nothing to process.");
                    return;
                }
                switch (line.charAt(0)) {
                    case '1':
                        inswitch = InputSwitch.ENTER_IN;
                        break;
                    case '2':
                        inswitch = InputSwitch.ENTER_OUT;
                        break;
                    case '3':
                        if (indirectory == null) {
                            inswitch = InputSwitch.ENTER_STRING;
                            System.out.println("Enter the text to process, or blank line to return to the menu.\n");
                        } else
                            execute();
                        break;
                    case '4':
                        inswitch = InputSwitch.SHOW_CONFIG;
                        break;
                    case 'q':
                        System.out.println("Bye");
                        System.exit(0);
                        break;
                    default: System.err.println("Bad menu selection : "+line.charAt(0));
                }
                break;
            case ENTER_IN:
                if (line.trim().length() == 0)
                    indirectory = null;
                else {
                    File tryin = new File(line);
                    if (!tryin.exists()) {
                        System.out.print("\""+line+"\" did not exist, as an input it must exist.");
                    } else {
                        indirectory = tryin;
                    }
                }
                inswitch = InputSwitch.MENU;
                break;

            case ENTER_OUT:
                if (line.trim().length() == 0)
                    outdirectory = null;
                else {
                    File tryout = new File(line);
                    if (!tryout.exists() && line.endsWith(File.separator)) {
                        tryout.mkdirs();
                    } 
                    outdirectory = tryout;
                }
                inswitch = InputSwitch.MENU;
                break;
                
            case ENTER_STRING:
                if (line.trim().length() == 0) {
                    inswitch = InputSwitch.MENU;
                } else {
                    this.processInputString(line);
                    this.getResultProcessor().done();
                }
                break;

            case SHOW_CONFIG:
                if (line.trim().length() == 0) {
                    if (changedproperty) {
                        this.nerAnnotator = null;
                        changedproperty = false;
                    }
                    this.inswitch = InputSwitch.MENU;
                    return;
                } else {
                    int endkeyindex = line.indexOf(" ");
                    if (endkeyindex == -1) {
                        System.out.println("Invalid key value separator, expected a space.");
                        return;
                    }
                    String key = line.substring(0, endkeyindex);
                    String value = line.substring(endkeyindex+1);
                    this.resourceManager.getProperties().setProperty(key, value);
                    System.out.println(key+"="+value);
                    changedproperty = true;
                }
                break;
        }
    }
    
    /**
     * render the input menu to standard out. 
     */
    @Override
    protected void inputMenu() {
        if (this.nerAnnotator == null) {
            System.out.println("Loading resources...");
            if (resourceManager == null) 
                this.resourceManager = new NerBaseConfigurator().getDefaultConfig();
            this.nerAnnotator = new NERAnnotator(this.resourceManager, "CONLL_DEFAULT");
            System.out.println("Completed loading resources.");
        }
        
        // display the command prompt depending on the mode we are in.
        switch (inswitch) {
            case MENU:
                System.out.println();
                String outdesc;
                String indesc;
                String in;
                String out;
                
                if (indirectory == null) {
                    indesc = "text entered from the command line";
                    in = "standard in";
                } else {
                    if (indirectory.exists()) {
                        if (indirectory.isDirectory()) {
                            indesc = "text from all files in directory \""+indirectory+"\"";
                            in = indirectory+File.separator;
                        } else {
                            indesc = "text from file \""+indirectory+"\"";
                            in = indirectory+"";
                        }
                    } else {
                        indesc = "text from file \""+indirectory+"\"";
                        in = indirectory.toString();
                    }
                }
                if (outdirectory == null) {
                    outdesc = "presenting results to the terminal";
                    out = "standard out";
                } else {
                    if (outdirectory.exists()) {
                        if (outdirectory.isDirectory()) {
                            outdesc = "storing results in directory \""+outdirectory+"\"";
                            out = outdirectory+File.separator;
                        } else {
                            outdesc = "storing results in file \""+outdirectory+"\"";
                            out = outdirectory.toString();
                        }
                    } else {
                        outdesc = "storing results in file \""+outdirectory+"\"";
                        out = outdirectory.toString();
                    }
                }
                System.out.print( "1 - select input ["+in+"]\n"
                                + "2 - change output ["+out+"]\n"
                                + "3 - annotate "+indesc+", "+outdesc+".\n"
                                + "4 - show and modify configuration parameters.\n"
                                + "q - exit the application.\n"
                                + "Choose from above options: ");
                break;
            case ENTER_IN:
                System.out.print("Enter input filename, directory terminated by file separator, or blank for standard input \n: ");
                break;

            case ENTER_OUT:
                System.out.print("Enter output filename, directory terminated by file separator or blank for standard output \n: ");
                break;

            case ENTER_STRING:
                System.out.print(": ");
                break;
                
            case SHOW_CONFIG:
                System.out.println("\nConfiguration parameters: ");
                Properties p = this.resourceManager.getProperties();
                for (Entry<Object, Object> entry : p.entrySet())
                    System.out.println("    "+entry.getKey()+" = "+entry.getValue());
                System.out.print("Enter property name followed a space and the new value, a blank entry to return to the main menu.\n: ");
                break;
        }
    }

    /**
     * execute NER on the selected input file or directory, produce output to standard out or a
     * file by the same name as the input.
     * @throws Exception if anything goes wrong.
     */
    private void execute() throws Exception {
        if (indirectory.isDirectory()) {
            File [] files = indirectory.listFiles();
            if (outdirectory != null) {
                System.out.println("Total Files : ••••••••••••••••••••••••••••••••••••••••••••••••••");
                System.out.print  ("Completed   : ");
                double ratio = 50.0 / (double)files.length;
                int completed = 0;
                int i = 0;
                for (; i < files.length; i++) {
                    File infile = files[i];
                    processInputFile(infile);
                    
                    // present completion.
                    while ((i * ratio) > completed) {
                        System.out.print("•");
                        completed++;
                    }
                }
                this.getResultProcessor().done();
                while ((i * ratio) > completed) {
                    System.out.print("•");
                    completed++;
                    i++;
                }
                System.out.println();
            } else {
                int i = 0;
                for (; i < files.length; i++) {
                    File infile = files[i];
                    processInputFile(infile);
                }
                this.getResultProcessor().done();
            }
        } else {
            processInputFile(indirectory);
            this.getResultProcessor().done();
            System.out.println("Completed");
        }
    }
    
    /**
     * Render a string representing the original data with embedded labels in the text.
     * @param nerView the NER label view.
     * @param ta the text annotation.
     * @return the original text marked up with the annotations.
     */
    private String renderString(View nerView, TextAnnotation ta) {
        StringBuilder sb = new StringBuilder();
        List<Constituent> constituents = new ArrayList<>(nerView.getConstituents());
        Collections.sort(constituents, TextAnnotationUtilities.constituentStartComparator);
        String text = ta.getText();
        int where = 0;
        for (Constituent c : constituents) {
            
            // append everything up to this token.
            int start = c.getStartCharOffset();
            sb.append(text.substring(where, start));
            
            // append the bracketed label.
            sb.append('[');
            sb.append(c.getLabel());
            sb.append(' ');
            sb.append(c.getTokenizedSurfaceForm());
            sb.append(" ] ");
            where = c.getEndCharOffset();
        }
        if (where < text.length())
        sb.append(text.substring(where, text.length()));
        return sb.toString();
    }

    /**
     * process the single input string, produce output on standard out if no output directory is defined,
     * or produce the output in the output directory by the same file name as the input file, or if a 
     * specific output filename is specified, use that name.
     * @param data the string to process
     * @throws Exception if anything goes wrong.
     */
    private void processInputString(String data) throws Exception {
        TextAnnotation ta = tab.createTextAnnotation(data);
        data = this.renderString(this.nerAnnotator.getView(ta), ta);
        this.getResultProcessor().publish(data, Long.toString(System.currentTimeMillis())+".txt");
    }
    
    /**
     * process the single input file, produce output on standard out if no output directory is defined,
     * or produce the output in the output directory by the same file name as the input file, or if a 
     * specific output filename is specified, use that name.
     * @param infile
     * @throws Exception 
     */
    private void processInputFile(File infile) throws Exception {
        String s = InFile.readFileText(infile.toString());
        TextAnnotation ta = tab.createTextAnnotation(s);
        s = this.renderString(this.nerAnnotator.getView(ta), ta);
        this.getResultProcessor().publish(s, infile.getName());
    }
    
    /**
     * Get a result process. Results are produced differently if we are producing to
     * multiple files in a directory, to a single file, or to standard output. This 
     * provides a way to abstract away the behavior.
     * @return the result processor.
     */
    private ResultProcessor getResultProcessor () {
        if (rp == null) {
            if (outdirectory == null) {
                rp = new ResultProcessor() {
                    @Override
                    public void publish(String output, String filename) throws Exception {     
                        if (writer == null)
                            writer = new BufferedWriter(new OutputStreamWriter(System.out));
                        writer.write("-----------------\n");
                        writer.write(output+"\n");
                        writer.flush();
                    }
    
                    @Override
                    public void done() {
                        rp = null;
                    }
                };
            } else {
                if (outdirectory.isDirectory()) {
                    rp = new ResultProcessor() {
                        @Override
                        public void publish(String output, String filename) throws IOException {     
                            writer = new BufferedWriter(new FileWriter(new File(outdirectory, filename)));
                            writer.write(output+"\n");
                            writer.close();
                        }
                        @Override
                        public void done() {                    
                            rp = null;
                        }
                    };                
                } else {
                    rp = new ResultProcessor() {
                        @Override
                        public void publish(String output, String filename) throws IOException {
                            if (writer == null)
                                writer = new BufferedWriter(new FileWriter(outdirectory));
                            writer.write("-----------------\n");
                            writer.write(output+"\n");
                            writer.flush();
                        }
                        @Override
                        public void done() throws Exception {   
                            writer.close();
                            rp = null;
                        }
                    };                
                }
            }
        }
        return rp;
    }
    
    /**
     * This class processes result strings. It may append everything to one output stream, or 
     * produce a separate file for each result.
     * @author redman
     *
     */
    abstract class ResultProcessor {
        
        /** write data to this writer. */
        protected BufferedWriter writer;

        /**
         * write the resulting string containing labeled data.
         * @param output the data to output.
         * @param filename the name of the file.
         * @throws Exception 
         */
        abstract public void publish(String output, String filename) throws Exception;
        
        /**
         * when finished, we may need to close up.
         * @param output
         * @throws Exception 
         */
        abstract public void done() throws Exception;
    }
    /**
     * All we need to do is call the constructor, it will parse args, set everything up
     * and run itself (it is a thread).
     * @param args the command arguents, in this case only a config file.
     */
    static public void main(String[] args) {
        new Main(args).start();
    }
}
