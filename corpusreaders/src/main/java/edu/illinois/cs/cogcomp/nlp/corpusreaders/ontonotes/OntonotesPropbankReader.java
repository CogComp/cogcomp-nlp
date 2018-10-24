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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.PredicateArgumentView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TreeView;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ontonotes.utils.DocumentIterator;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ontonotes.utils.SRLNode;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ontonotes.utils.SRLNode.PredicateArgument;

/**
 * This class will traverse a directory hierarchy searching for any file with a 
 * certain file extension. The file is expected to be a penn propbank parse file in
 * text format. It will go any number of levels deep. The iteration produces a
 * TextAnnotation for each file. The resulting text annotation contains the treebank 
 * and propbank data in separate POS and SRL views. The treebank files are expected 
 * to end in ".parse" and the propbank files must end in ".prop".
 * @author redman
 */
public class OntonotesPropbankReader extends AbstractOntonotesReader {
    
    /** turn this on, get TONS of treebank style output along with propbank data. */
    final boolean debug = false;

    /** the name of the resulting view. */
    final protected static String VIEW_NAME = "SRL_ONTONOTES_5_GOLD";
    
    /** we will use this to generate the text annotation with the treebank parse, 
     * then we will add the propbank parse over that.
     */
    private OntonotesTreebankReader otr = null;
    
    /** holds the character that indicates if this is a continuation of a previous relation. */
    final private String CONTINUATION_ATTR = "CONTINUATION_ATTR";
    
    /**
     * Reads the specified sections from propbank data. Creates an instance
     * of a treebank read as well, since this is required to properly parse
     * and map the propbank data.
     * @param dir The directory that contains to the .prop files
     * @param language the language we want.
     * @param annotationFileExtension the name of the annotation file
     * @throws IOException 
     * @throws IllegalArgumentException 
     */
    public OntonotesPropbankReader(String dir, String language) 
                    throws IllegalArgumentException, IOException {
        super(VIEW_NAME, dir, language, DocumentIterator.FileKind.prop);
        
        // Need to be able to iterate over the ".parse" files so we can vet the required 
        // treebank data. We have to make sure for each propbank file we have, we have the
        // corresponding treebank file. treebank file has same name with .parse extension.
        ArrayList<File> treefilelist = new ArrayList<File> ();
        for (File propfile : this.filelist) {
            String name = propfile.getName();
            name = name.replaceAll(".prop", ".parse");
            File nf = new File(propfile.getParentFile(), name);
            if (nf.exists()) {
                treefilelist.add(nf);
            } else
                throw new IllegalArgumentException("The treebank parse file is required and did not exist for "+propfile);
        }
        otr = new OntonotesTreebankReader(dir, language, treefilelist);
    }

    /**
     * Traverse the tree counting the terminal nodes as we go along, adding the index as the key, trhe
     * tree node as the value to a hashmap.
     * @param tree the tree we are looking at.
     * @param data the map we are populating.
     * @param offset the current index.
     * @return the index of the next terminal node.
     */
    private int traverseTree(Tree<Constituent> tree, HashMap<Integer, Tree<Constituent>> data, int offset) {
        if (tree.isLeaf()) {
            data.put(offset++, tree);
            if (tree.getNumberOfChildren() > 0)
                throw new RuntimeException("There is a leaf with kids???");
        } else {
            for (Iterator<Tree<Constituent>> iterator = tree.childrenIterator().iterator() ; iterator.hasNext(); ) {
                offset = traverseTree(iterator.next(), data, offset);
            }
        }
        return offset;
    }
    
    /**
     * recursively traverse a tree map representing one sentence or setence component to a set of 
     * constituent trees for each leaf node.
     * @param tree the tree to traverse
     * @return the map.
     */
    private HashMap<Integer, Tree<Constituent>> compileTokenMap(Tree<Constituent> tree) {
        HashMap<Integer, Tree<Constituent>> data = new HashMap<Integer, Tree<Constituent>>();
        traverseTree(tree, data, 0);
        return data;
    }
    
    /**
     * parse the propbank file, producing an annotation covering the entire file. This method will
     * use the treebank reader to first produce the parse tree this method will then use to map the
     * data.
     * @param lines the data from the file, each line.
     * @return the text annotation.
     * @throws AnnotatorException
     */
    protected TextAnnotation parseLines(ArrayList<String> lines) throws AnnotatorException {
        if (!this.otr.hasNext())
            throw new RuntimeException("There were not as many treebank files as there were propbank files.");
        
        // get the treebank parse using the ontonotes treebank reader.
        TextAnnotation ta = this.otr.next();
        if (ta == null)
            return null;
        TreeView tv = (TreeView)ta.getView(OntonotesTreebankReader.VIEW_NAME);
        
        // now parse out the propbank data, we will compile the data into SRLNode container
        // class instances that capture all thee data form the file, we will then need to 
        // map that data to line it up with the content of the text annotation.
        ArrayList<SRLNode> srlRecords = new ArrayList<SRLNode>();
        for (String line : lines) {
            String[] splits = line.split(" ");
            if (splits!=null && splits.length > 7) {
                int treeid = Integer.parseInt(splits[1]);
                int predicateid = Integer.parseInt(splits[2]);
                
                // there is one SRLNode per line, includes all relations to that predicate.
                SRLNode node = new SRLNode(new IntPair(treeid, predicateid), splits[5]);
                srlRecords.add(node);
                
                // add the relations
                for (int i = 7; i < splits.length; i++) {
                    if (splits[i].contains("ARG")) { // omit the predicate
                        try {
                            node.addLinks(splits[i]);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        
        // we have all the parsed SRL relations, let's try to make sense of them. We will get the 
        // tree for each sentence. 
        int sentenceIndx = 0;            
        Tree<Constituent> tree = tv.getConstituentTree(0);
        if (debug) {
            System.out.println("\n---------------\n"+sentenceIndx+") "+ta.getSentence(sentenceIndx)+":"+otr.currentfile);
            System.out.println(tv.getTree(0));
        }
        
        // create the predicate argument view.
        PredicateArgumentView view = new PredicateArgumentView(ViewNames.SRL_VERB, ta);

        // to find terminal nodes, we will map trees per each token to it's token offset.
        HashMap<Integer, Tree<Constituent>> tokenmap = compileTokenMap(tree);
        HashMap<String, Constituent> newconstituents = new HashMap<>(); // ensures no duplicate constituents.
        for (SRLNode node : srlRecords) {
            int nsentenceIndx = node.getLinked().getFirst();
            if (nsentenceIndx != sentenceIndx) {
                sentenceIndx = nsentenceIndx;
                tree = tv.getConstituentTree(sentenceIndx);
                tokenmap = compileTokenMap(tree);
                if (debug) {
                    System.out.println("\n---------------\n"+sentenceIndx+") "+ta.getSentence(sentenceIndx));
                    System.out.println(tv.getTree(sentenceIndx));
                    if (sentenceIndx > 5)
                        break;
                }
            }
            
            // for the node, the second of the int pair is the token offset within the sentence.
            this.addSrlFrame(view, ta, node, tokenmap, newconstituents);
        }
        
        ta.addView(ViewNames.SRL_VERB, view);
        return ta;
    }

    /**
     * We want to reuse any constituent with the same label and token span and attributes. The 
     * equals method checks all kinds of stuff, in going and out going relations, which are changing
     * as we construct the view.
     * @param c the constituent.
     * @return the disambiguation key for constituents.
     */
    private String constituentDisambiguationKey(Constituent c) {
        StringBuffer sb = new StringBuffer();
        sb.append(c.getLabel());
        sb.append(':');
        sb.append(c.getSpan().getFirst());
        sb.append(':');
        sb.append(c.getSpan().getSecond());
        return c.getLabel()+":"+c.getStartSpan();
    }
    
    /**
     * Add an SRL frame to a PredicateArgumentView. Create the constituent for the predicate, and the relations as well (unless
     * they already exist in the newconstituents, in which case we just fetch them).
     * @param srlView The SRL view object.
     * @param viewName the name of the view.
     * @param ta the text annotation object containing the text, and views.
     * @param node the predicate and it's arguments.
     * @param tokenmap maps token offsets in sentence tree to constituent in the treebank data.
     * @param newconstituents used to deduplicate.
     */
    private void addSrlFrame(PredicateArgumentView srlView, TextAnnotation ta, SRLNode node, HashMap<Integer, 
            Tree<Constituent>> tokenmap, HashMap<String, Constituent> newconstituents) {
        
        // get the token for the predicate from the token map. 
        Tree<Constituent> child = tokenmap.get(node.getLinked().getSecond());
        Constituent constituent = child.getLabel();
        
        // Create the verb constituent for the predicate.
        String label = "VERB"; // "VERB("+constituent.getLabel()+")"
        Constituent predicate = new Constituent(label, srlView.getViewName(), ta, constituent.getStartSpan(), constituent.getEndSpan());
        predicate.addAttribute(PredicateArgumentView.LemmaIdentifier, constituent.getSurfaceForm());
        predicate.addAttribute(PredicateArgumentView.SenseIdentifer, node.getSense());
        String key = constituentDisambiguationKey(predicate);
        
        
        if (newconstituents.containsKey(key)) {
            predicate = newconstituents.get(key);
        } else {
            newconstituents.put(key, predicate);
        }
        
        
        if (debug) {
            System.out.print(predicate.getSurfaceForm()+"("+node.getSense()+") -> ");
        }
        
        // add a constituent for each argument.
        ArrayList<PredicateArgument> arguments = node.compileLinks(tokenmap);
        List<Constituent> args = new ArrayList<>();
        List<String> tempArgLabels = new ArrayList<>();
        List<HashMap<String, String>> continuationArgs = new ArrayList<>();
        for (int i = 0; i < arguments.size() ;) {
            PredicateArgument terminal = arguments.get(i);
            
            // find the terminal
            int j = i;
            for ( ; terminal.argument == null; j++)
                terminal = arguments.get(j);
            
            // since Constituents are contiguous, make one constituent for each contiguous span.
            String arg = terminal.argument;
            for (; i <= j && i < arguments.size() ; i++) {
                PredicateArgument a = arguments.get(i);
                String rclabel = arg;
                Constituent rc = new Constituent(rclabel, srlView.getViewName(), ta, a.pair.getFirst(), a.pair.getSecond());
                
                // is this a continuation?
                HashMap<String, String> relargs = new HashMap<String, String>();
                if (a.link != null && a.link.length() > 0)
                    relargs.put(CONTINUATION_ATTR, a.link);
                continuationArgs.add(relargs);
                key = constituentDisambiguationKey(rc);
                if (newconstituents.containsKey(key)) {
                    rc = newconstituents.get(key);
                } else {
                    newconstituents.put(key, rc);
                }                
                if (debug) {
                    System.out.print(" "+rclabel);
                }
                args.add(rc);
                tempArgLabels.add(a.argument);
            }
        }
        if (debug) 
            System.out.println();
        String[] argLabels = tempArgLabels.toArray(new String[tempArgLabels.size()]);
        double[] scores = new double[tempArgLabels.size()];
        for (int i = 0; i < scores.length; i++) scores[i] = 1.0;
        srlView.addPredicateArguments(predicate, args, argLabels, scores, continuationArgs);
    }
    
    /**
     * Report the parse status.
     */
    public String generateReport() {
        if (error != null) {
            return "OntonotesPropbankReader produced "+" trees from "+fileindex+" of "+filelist.size()+" files.\n"
                            +"Error encountered: "+error.getMessage();
        } else {
            return "OntonotesPropbankReader produced "+" trees from "+fileindex+" of "+filelist.size()+" files.";
        }
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
            System.err.println("This executable requires three arguments:\n"
                    + " OntonotesPropbankReader <OntoNotes Directory> <language> <output_directory>");
            System.exit(-1);
        }
        String topdir = args[0];
        OntonotesPropbankReader otr = new OntonotesPropbankReader(topdir, args[1]);
        int count = 0;
        int hashcollisions = 0;
        while (otr.hasNext()) {
            TextAnnotation ta = otr.next();
            if (ta != null) {

                // write the serialized form
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
        System.out.println(hashcollisions+" collisions in "+count+" documents.");
        System.out.println(otr.generateReport());
    }
}
