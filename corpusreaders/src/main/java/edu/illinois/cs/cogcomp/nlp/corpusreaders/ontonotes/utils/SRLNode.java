/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.ontonotes.utils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;

/**
 * base class for all SRL node arguments. Each one represents one line from the propbank
 * file for a single verb and it's arguments.
 * @author redman
 */
public class SRLNode {
    
    /** represent the linkages. */
    private ArrayList<SRLLink> links = new ArrayList<SRLLink>();

    /** the sentence and term offset within of the sentence of the predicate. */
    private IntPair linked;

    /** the sense of the key term. */
    private String sense;

    /**
     * give me the argument.
     * @param l the sentence and token within the sentence.
     * @param rolesetID this is the word sense identifier.
     * @throws ParseException 
     */
    public SRLNode(IntPair l, String rolesetID) {
        this.linked = l;
        this.sense = rolesetID;
    }
    
    /**
     * parse the linkage, if it can be represented(discontiguous text can not be represented 
     * by a Constituent), a new link is added.
     * @param arg the argument
     * @throws ParseException 
     */
    public void addLinks(String arg) throws ParseException {
        // parse the linkages
        int index = arg.indexOf('-');
        String argument = arg.substring(index + 1);
        String lt = arg.substring(0, index);
        StringTokenizer st = new StringTokenizer(lt, "*;,", true);
        char link = 0;
        while (st.hasMoreTokens()) {
            String tok = st.nextToken();
            if (tok.equals("*")) {
                link = '*';
            } else if (tok.equals(",") || tok.equals(";")) {
                link = tok.charAt(0);
            } else {
                links.add(new SRLLink(link, getPair(tok)));
                link = 0;
            }
        }
        links.get(links.size()-1).argument = argument;
    }

    /**
     * This is a translation of the SRLSpan into the coordinate system of the text annotation, 
     * including the predicate argument. argument is empty, this is part of a discontiguous
     * run.
     * @author redman
     */
    public class PredicateArgument {
        
        /** the label, like ARG1 */
        public String link;
        
        /** the span range. */
        public IntPair pair;
        
        /** the relationship type. */
        public String argument;
        
        /**
         * constructs the container.
         * @param arg the relationship.
         * @param link the linkage type to the previous span, for discontiguous runs.
         * @param pair the span.
         */
        PredicateArgument(String arg, String link, IntPair pair) {
            this.link = link;
            this.pair = pair;
            this.argument = arg;
        }
    }
    
    /**
     * compile a list of predicates.
     * @param tree the tree with the data.
     * @param tokenmap the token maps with the terminal nodes.
     * @return the string indicating the leaves.
     */
    public ArrayList<PredicateArgument> compileLinks(HashMap<Integer, Tree<Constituent>> tokenmap) {
        ArrayList<PredicateArgument> map = new ArrayList<PredicateArgument>();
        
        // To find the range of tokens for the constituent, we must go up the tree some number of levels, 
        // then compile all the tokens in that subtree.
        for (SRLLink link : links) {
            Tree<Constituent> node = tokenmap.get(link.where.getFirst());
            for (int i = link.where.getSecond(); i > 0 && node.getParent() != null; i--) {
                Tree<Constituent> up = node.getParent();
                node = up;
            }
            Constituent constituent = node.getLabel();
            map.add(new PredicateArgument(link.argument, link.link, new IntPair(constituent.getStartSpan(), 
                constituent.getEndSpan())));
        }
        return map;
    }

    /**
     * compile a list of predicates.
     * @param tree the tree with the data.
     * @param tokenmap the token maps with the terminal nodes.
     * @return the string indicating the leaves.
     */
    public String printableLinks(Tree<Constituent> tree, HashMap<Integer, Tree<Constituent>> tokenmap) {
        StringBuffer sb = new StringBuffer();
        for (SRLLink link : links) {
            Tree<Constituent> node = tokenmap.get(link.where.getFirst());
            for (int i = link.where.getSecond(); i > 0 && node.getParent() != null; i--) {
                Tree<Constituent> up = node.getParent();
                node = up;
            }
            sb.append(link.link + node.getLabel().toString());
        }
        return sb.toString();
    }

    /**
     * Get an int pair from text in the form Number:Number.
     * @param txt the text to parse.
     * @return the int pair.
     * @throws ParseException if the text is not properly formatted.
     */
    protected IntPair getPair(String txt) throws ParseException {
        int indexFirst = txt.indexOf(':');
        if (indexFirst == -1)
            throw new ParseException("There was a bad matching line in props file : " + txt, indexFirst);
        int token = Integer.parseInt(txt.substring(0, indexFirst));
        int height = Integer.parseInt(txt.substring(indexFirst + 1));
        return new IntPair(token, height);
    }

    /**
     * @return the links
     */
    public ArrayList<SRLLink> getLinks() {
        return links;
    }

    /**
     * @return the linked
     */
    public IntPair getLinked() {
        return linked;
    }

    /**
     * @return the sense
     */
    public String getSense() {
        return sense;
    }

    public String toString() {
        StringBuffer b = new StringBuffer();
        b.append(linked + " " + sense + " -> ");
        for (SRLLink l : links) {
            b.append(l.toString());
        }
        return b.toString();
    }
}
