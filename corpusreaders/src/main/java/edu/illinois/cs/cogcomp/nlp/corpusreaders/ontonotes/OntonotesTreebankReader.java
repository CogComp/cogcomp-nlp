 /**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.ontonotes;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TreeView;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.core.datastructures.trees.TreeParserFactory;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
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
public class OntonotesTreebankReader extends AbstractOntonotesReader {

    /** the view name we will employ. */
    public static final String VIEW_NAME = "TREEBANK_ONTONOTES_5_GOLD";
    
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
        super(VIEW_NAME, treebankHome, language, DocumentIterator.FileKind.parse);
    }

    /**
     * This is used if we require a certain order of files, or want to exclude files or something like that.
     * @param dir the directory to look in.
     * @param language the language.
     * @param treefilelist the list of files.
     */
    public OntonotesTreebankReader(String dir, String language, ArrayList<File> treefilelist) {
        super(VIEW_NAME, dir, language, DocumentIterator.FileKind.parse, treefilelist);
    }

    /**
     * parse the pen treebank parse file, producing an annotation covering the entire file.
     * @param lines the data from the file, each line.
     * @return the text annotation.
     * @throws AnnotatorException
     */
    protected TextAnnotation parseLines(ArrayList<String> lines) throws AnnotatorException {
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
                } else {
                    System.err.println("This tree produced no sentence text:\n"+tree);
                    System.err.println("from file:\n"+this.currentfile);
                    System.err.flush();
                    return null;
                }
                sb = new StringBuilder();
            }
        }

        TextAnnotation ta = BasicTextAnnotationBuilder.createTextAnnotationFromTokens(
            VIEW_NAME, currentfile, sentences);
        TreeView parse = new TreeView(VIEW_NAME, this.getClass().getCanonicalName(), ta, 1.0);
        
        // add each parse tree
        int treecount = 0;
        for (Tree<String> tree : trees) {
            parse.setParseTree(treecount++, tree);
        }
        ta.addView(VIEW_NAME, parse);
        POSFromParse pos = new POSFromParse(VIEW_NAME);
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
     * generate a human-readable report of annotations read from the source file (plus whatever
     * other relevant statistics the user should know about).
     */
    public String generateReport() {
        StringBuffer sb = new StringBuffer();
        if (error != null) {
            sb.append("OntonotesTreebankReader produced "+treesProduced+" trees from "+fileindex+
                " of "+filelist.size()+" files.\n" +"Error encountered: "+error.getMessage()+"\n");
        } else {
            sb.append("OntonotesTreebankReader produced "+treesProduced+" trees from "+fileindex+
                " of "+filelist.size()+" files.");
        }
        sb.append("Of the documents, "+this.badFiles.size()+" files could not be parsed, they are as follows:\n");
        for (String badfile : this.badFiles) {
            sb.append("    "+badfile+"\n");
        }
        return sb.toString();
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
            if (ta != null) {
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
        }
        System.out.println(otr.generateReport());
    }
}
