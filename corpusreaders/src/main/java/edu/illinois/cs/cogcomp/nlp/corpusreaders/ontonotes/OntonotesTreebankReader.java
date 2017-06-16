 /**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.ontonotes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TreeView;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.core.datastructures.trees.TreeParserFactory;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.AnnotationReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.CorpusReaderConfigurator;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ontonotes.utils.DocumentIterator;
import edu.illinois.cs.cogcomp.nlp.utilities.POSFromParse;
import edu.illinois.cs.cogcomp.nlp.utilities.ParseUtils;

/**
 * This class will traverse a directory hierarchy searching for any file with a 
 * certain file extension. The file is expected to be a penn treebank parse file in
 * text format. It will go any number of levels deep. The iteration produces a
 * TextAnnotation for each file.
 * @author redman
 */
public class OntonotesTreebankReader extends AnnotationReader<TextAnnotation> {

    /** the view name we will employ. */
    public static final String PENN_TREEBANK_ONTONOTES = "PennTreebank-Ontonotes";

    /** the home directory to traverse. */
    protected final String homeDirectory;
    
    /** the name of the resulting view. */
    protected String parseViewName = "PARSETREE";
    
    /** the list of files, compiled during initialization, used to iterate over the parse trees. */
    protected ArrayList<File> filelist = new ArrayList<File> ();
    
    /** the index of the current file we are looking at. */
    protected int fileindex = 0;
    
    /** the current file ready to be read. */
    protected String currentfile = null;
    
    /** the number of trees produced. */
    protected int treesProduced = 0;
    
    /**
     * Reads the specified sections from penn treebank
     * @param treebankHome The directory that points to the merged (mrg) files of the WSJ portion
     * @param language the language
     * @param annotationFileExtension the name of the annotation file
     * @throws IOException 
     * @throws IllegalArgumentException 
     */
    public OntonotesTreebankReader(String treebankHome, String language) 
                    throws IllegalArgumentException, IOException {
        super(CorpusReaderConfigurator.buildResourceManager(PENN_TREEBANK_ONTONOTES, treebankHome, treebankHome, ".parse", ".parse"));
        homeDirectory = treebankHome;
        
        // compile the list of all treebank annotation files
        DocumentIterator di = new DocumentIterator(homeDirectory, DocumentIterator.Language.valueOf(language), 
            DocumentIterator.FileKind.parse);
        while (di.hasNext()) {
            filelist.add(di.next());
        }
    }

    /**
     * we assume all files found are correct, hence if we have another file, we will produce
     * another text annotation.
     */
    @Override
    public boolean hasNext() {
        if (fileindex == filelist.size())
            return false;
        else {
            this.currentfile = filelist.get(fileindex).getAbsolutePath();
            fileindex++;
            return true;
        }
    }
    
    /** the annotation exception, or null if none. */
    private Exception error = null;
    
    /**
     * return the next annotation object. Don't forget to increment currentAnnotationId.
     *
     * @return an annotation object.
     */
    @Override
    public TextAnnotation next() {
        ArrayList<String> lines;
        try {
            lines = LineIO.read(currentfile);
        } catch (FileNotFoundException e1) {
            error = e1;
            e1.printStackTrace();
            return null;
        }
        try {
            TextAnnotation ta = parseTreebankData(lines);
            return ta;
        } catch (AnnotatorException e) {
            error = e;
            e.printStackTrace();
            return null;
        }
    }

    /**
     * parse the pen treebank parse file, producing an annotation covering the entire file.
     * @param lines the data from the file, each line.
     * @return the text annotation.
     * @throws AnnotatorException
     */
    private TextAnnotation parseTreebankData(ArrayList<String> lines) throws AnnotatorException {
        StringBuilder sb = new StringBuilder();
        int numParen = 0;
        int currentLineId = 0;
        ArrayList<String[]> sentences = new ArrayList<> ();
        ArrayList<Tree<String>> trees = new ArrayList<> ();
        while (currentLineId < lines.size()) {
            String line = lines.get(currentLineId++);
            if (line.length() == 0)
                continue;
            
            numParen += countUnclosedParens(line);
            sb.append(line);
            if (numParen == 0) {
                // parse the tree, add the sentence tokens to the list of sentences.
                Tree<String> tree = TreeParserFactory.getStringTreeParser().parse(
                    sb.toString().replaceAll("\\\\/", "/"));
                
                // get the tokens.
                String[] text = ParseUtils.getTerminalStringSentence(tree);
                if (text.length != 0) {
                    sentences.add(text);
                    trees.add(tree);
                    treesProduced++;
                } else 
                    System.err.println("Y "+tree);
                sb = new StringBuilder();
            }
        }

        TextAnnotation ta = BasicTextAnnotationBuilder.createTextAnnotationFromTokens(
            PENN_TREEBANK_ONTONOTES, currentfile, sentences);
        TreeView parse = new TreeView(parseViewName, "PTB-GOLD", ta, 1.0);
        
        // add each parse tree
        int treecount = 0;
        for (Tree<String> tree : trees)
            parse.setParseTree(treecount++, tree);
        ta.addView(parseViewName, parse);
        POSFromParse pos = new POSFromParse(parseViewName);
        ta.addView(pos);
        return ta;
    }

    /**
     * count the number of unclosed opening parens, this can be negative if there are more closing
     * parens that opening ones.
     * @param line the line of data.
     * @return the number of unclosed parens.
     */
    private int countUnclosedParens(String line) {
        int unclosedParens = 0;
        for (char character : line.toCharArray()) {
            if (character == ')') unclosedParens--;
            else if (character == '(') unclosedParens++;
        }
        return unclosedParens;
    }

    /**
     * TODO: generate a human-readable report of annotations read from the source file (plus whatever
     * other relevant statistics the user should know about).
     */
    public String generateReport() {
        if (error != null) {
            return "OntonotesTreebankReader produced "+treesProduced+" trees from "+fileindex+" of "+filelist.size()+" files.\n"
                            +"Error encountered: "+error.getMessage();
        } else {
            return "OntonotesTreebankReader produced "+treesProduced+" trees from "+fileindex+" of "+filelist.size()+" files.";
        }
    }

    @Override
    protected void initializeReader() {
    }
    
    /**
     * This class will read the ontonotes data from the provided directory, and write the resulting
     * serialized json form of the penn bank data to the specified output directory. It will retain
     * the directory structure of the original data.
     * @param args command lines args specify input data directory, language and output directory.
     * @throws IOException
     */
    static public void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.err.println("This executable requires four arguments:\n"
                    + " OntonotesTreebankReader <OntoNotes Directory> <language> <output_directory>");
            System.exit(-1);
        }
        String topdir = args[0];
        OntonotesTreebankReader otr = new OntonotesTreebankReader(topdir, args[1]);
        int count = 0;
        while (otr.hasNext()) {
            TextAnnotation ta = otr.next();
            String json = SerializationHelper.serializeToJson(ta);
            
            String outfile = otr.currentfile.replace(topdir, args[2]);
            File outputfile = new File(outfile);
            outputfile.getParentFile().mkdirs();
            try (PrintWriter out = new PrintWriter(outputfile)) {
                out.print(json);
            }
            
            count++;
            if ((count % 100) == 0)
                System.out.println("Completed "+count+" of "+otr.filelist.size());
        }
        System.out.println(otr.generateReport());
    }
}
