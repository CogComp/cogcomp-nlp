/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.chunker.realign;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * Translate part of speech tagging convention changes into chunker training 
 * data so the chunker can be retrained for use with the newest POS tagging. This 
 * was testing against the 00-23.br files from both the new POS and the older chunker training
 * data, mostly because these are the only two files that seemed to align. 
 * <p>
 * And tho these files contains similar data, the data order was permuted, and there was data
 * missing from the POS data. So in additional to aligning the tree structures and  repairing
 * the structure so they align, corresponding records were also identified by using a hashing
 * scheme, hashing the catenated tokens from each sentence of each of the files to identify the
 * aligning test. And still, there's more; Some of the actual text was different between the files,
 * it looks like some typos, or programmatic issues in producing the tagged rep of the files was
 * also fixed, or maybe they were based on different versions of the original data. As you can see
 * there is much surmised.
 * <p>
 * The main method takes one argument, the name of the directory containing a directory for the chunker
 * data, "chunker", and another directory named "pos". It will create a directory named "revised" that will
 * contain the results. There are expected to be files with corresponding names in "chunker" and "pos"
 * directories, the ones in "pos" contain the new POS tagging data, the "chunker" files the older chunker
 * training data that will be revised and placed in the "revised" folder.
 * <p>
 * @author redman
 */
public class TranslatePOSTagging {

    /** the sentences from the POS data. */
    static ArrayList<ChunkTree> posSentences = new ArrayList<>();
    
    /** the chunked sentences hashed. */
    static HashMap<String, ChunkTree> chunkedSentences = new HashMap<>();

    /**
     * @param args the working directory containing the dir with chunker data another directory
     * with POS data. It will create a third folder named revised with the fixed data.
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.err.println("Pass in the directory containing both the chunker data and the pos data.");
            System.exit(1);
        }
        
        // set up the directories, create the output dir in that same directory.
        File directory = new File(args[0]);
        File chunkerTrainDir = new File(directory, "chunker");
        File posTrainDir = new File(directory, "pos");
        File outputTrainDir = new File(directory, "revised");
        
        // sanity check for errors.
        if (chunkerTrainDir.exists() == false) {
            System.err.println("There is not directory named \""+chunkerTrainDir.toString()+"\".");
            System.exit(1);
        }
        if (posTrainDir.exists() == false) {
            System.err.println("There is no directory named \""+posTrainDir.toString()+"\".");
            System.exit(1);
        }
        if (outputTrainDir.exists() == true) {
            System.out.println("Output directory already exists \""+outputTrainDir.toString()+"\", destroying");
            final File[] files = outputTrainDir.listFiles();
            for (File f: files) 
                f.delete();
            outputTrainDir.delete();
        }
        outputTrainDir.mkdir();
        System.out.println("Created "+outputTrainDir.toString());
        String resultspath = outputTrainDir.toString() + File.separator;
        
        // enumerate each line of each file reconciling differences.
        File[] chunkerfiles = chunkerTrainDir.listFiles();
        File[] posFiles = posTrainDir.listFiles();
        for (int i = 0; i < chunkerfiles.length; i++) {
            File chunkfile = chunkerfiles[i];
            File posfile = posFiles[i];
            if (!chunkfile.getName().equals(posfile.getName())) {
                System.err.println("Files names were not equal!");
                System.exit(1);
            }
            
            // read corresponding doc from each directory, loop over their lines, creating
            // chunks for each. reconcile does the work here.
            try (BufferedReader chunkreader = new BufferedReader(new FileReader(chunkfile))) {
                try (BufferedReader posreader = new BufferedReader(new FileReader(posfile))) {
                    String pl = null;
                    while (true) {
                        
                        // read a chunk every time.
                        String cl = chunkreader.readLine();
                        if (cl == null)
                            break;
                        
                        // only read a POS if the previous one didn't match, if it didn't match,
                        // the previous chunk rep didn't exist in the pos file
                        pl = posreader.readLine();
                        
                        // create the chunked trees for each.
                        ChunkTree ct = new ChunkTree(cl);
                        String ctsent = ct.sentence();
                        if (!chunkedSentences.containsKey(ctsent)) {
                            chunkedSentences.put(ctsent, ct);
                        }
                        if (pl != null) {
                            ChunkTree pt = new ChunkTree(pl);
                            if (pl != null)
                                posSentences.add(pt);
                        }
                    }
                }
            }
            
            // merge the trees, produce the output
            int line = 0;
            int diff = 0;
            StringBuffer result = new StringBuffer();
            for (ChunkTree postree : posSentences) {
                ChunkTree chunktree = chunkedSentences.get(postree.sentence());
                if (chunktree == null) {
                    System.err.println("Hell fire.");
                } else {
                    String before = chunktree.sentence();
                    String beforet = chunktree.toString();
                    chunktree.merge(postree);
                    if (!chunktree.sentence().equals(before)) {
                        throw new RuntimeException("Sentences differed : \n"+before+"\n"+chunktree.sentence());
                    }
                    if (!beforet.equals(chunktree.toString())) {
                        System.out.println();
                        System.out.println(beforet);
                        System.out.println(chunktree);
                        System.out.println(postree);
                        diff++;
                    }
                    result.append(chunktree.toString());
                    result.append('\n');
                }
                line++;
            }
            System.out.println("Of "+line+", "+diff+" changed.");
            Files.write(Paths.get(resultspath+chunkfile.getName()), result.toString().getBytes(), StandardOpenOption.CREATE);
        }
    }
    
    /**
     * This tree represents the POS tagging, and potentially the chunking.
     * @author redman
     */
    private static class ChunkTree {
        
        /**
         * superclass of all tree nodes, leaves have only a token, branches 
         * have a token and a label
         */
        abstract class Node {
            /**
             * This will return some representation of the sentence, but without spaces (for hashing).
             * @return the sentence stripped of spaces.
             */
            abstract public String sentence();
        }
        
        /**
         * A leaf contains only a term.
         */
        class Leaf extends Node {
            
            /** the term. */
            String value;
            
            /** 
             * Given the term, create the value, but also fix several differences tween teh
             * two representations, putting spaces between "-" in some cases but not others, 
             * replacing some "\/" with "/" and so on.
             * 
             * @param term the word or phrase.
             */
            Leaf(String term) {
                if (term.equals("-LCB-")) {
                    value = "{";
                } else if (term.equals("-RCB-")) {
                    value = "}";
                } else {
                    value = term.replaceAll("([a-zA-Z0-9.%])-(['`a-zA-Z0-9])", "$1 - $2");
                    value = value.replaceAll("([a-zA-Z0-9.%])-(['`a-zA-Z0-9])", "$1 - $2");
                    value = value.replaceAll("([a-zA-Z0-9.%])-(['`a-zA-Z0-9])", "$1 - $2");
                    value = value.replaceAll("([0-9])\\\\/([0-9])", "$1/$2");
                    value = value.replaceAll("([0-9])\\\\/([0-9])", "$1/$2");
                    value = value.replaceAll("([0-9])\\\\/([0-9])", "$1/$2");
                    value = value.replaceAll("([a-zA-Z])\\\\/([a-zA-Z0-9])", "$1 / $2");
                    value = value.replaceAll("([a-zA-Z])\\\\/([a-zA-Z0-9])", "$1 / $2");
                    value = value.replaceAll("([a-zA-Z])\\\\/([a-zA-Z0-9])", "$1 / $2");
                    value = value.replaceAll("([0-9]{4,})/([0-9]{4,})", "$1 / $2");
                    value = value.replaceAll("([0-9])%", "$1 %");
                }
            }
            
            /** 
             * the value of a leaf is simply it's value. 
             */
            public String toString() {
                return value;
            }
            
            /** 
             * leaf returns it's value.
             */
            public String sentence() {
                return toString();
            }
        }
        
        /**
         * represents a node.
         */
        class Branch extends Node {
            
            /** the token or null if this is the root. */
            String token;
            
            /** the value or null if this is the root. */
            String label;
            
            /** the children. */
            ArrayList<Node> children =  new ArrayList<>();
            
            /** 
             * given the token and value. 
             * @param token the token.
             * @param label the value.
             */
            Branch(String token, String label) {
                this.token = token;
                this.label = label;
            }
            
            
            /** the value of a leaf is simply it's value. */
            public String toString() {
                StringBuffer sb = new StringBuffer();
                sb.append(token);
                sb.append(label);
                for (Node child : children) {
                    sb.append(' ');
                    sb.append(child.toString()); 
                }
                if (token.equals("["))
                    sb.append(" ]");
                else
                    sb.append(')');

                return sb.toString();
            }
            
            /** the value of a leaf is simply it's value. */
            public String sentence() {
                StringBuffer sb = new StringBuffer();
                for (Node child : children) {
                    String s = child.sentence();
                    sb.append(s.replaceAll("\\s", ""));
                }
                return sb.toString();
            }

        }
        
        /** the tree root. */
        Branch root;

        /**
         * construct a tree from the chunk.
         * @param chunk one chunked sentence
         */
        private ChunkTree(String chunk) {
            StringTokenizer clst = new StringTokenizer(chunk, "[]() ", true);
            root = new Branch(null, null);
            traverse(clst, root);
        }
        
        /**
         * merge POS tagging into this tree by replacing the POS structural nodes with the corresponding
         * POS structural nodes from the branch passed in.
         * @param pos the structural data.
         */
        public void merge(ChunkTree pos) {
            
            // basically replace all top level POS nodes with corresponding 
            // POS nodes from input.
            int inpos = 0;
            Branch pn = (Branch) pos.root.children.get(inpos);
            ArrayList<Node> newkids =  new ArrayList<>();
            for (int i = 0 ; i < root.children.size(); i++) {
                Branch cn = (Branch) root.children.get(i);
                String cnsent = cn.sentence();
                String pnsent = pn.sentence();
                
                // special case, change in text.
                if (cnsent.contains("I'm-coming-down-your-throat"))
                    cnsent = cnsent.replaceAll("I'm-coming-down-your-throat", "I-'mcoming-down-your-throat");
                if (cnsent.contains("S$1million"))
                    cnsent = "US$1million";
                if (cn.token.equals("(")) {
                    
                    // Top level node, not in a chunk.
                    if (cnsent.equals(pnsent)) {
                        
                        // nodes match exactly.
                        newkids.add(pn);
                    } else if (cnsent.contains(pnsent)) {
                        newkids.add(pn);
                        
                        // POS node is one of a few terms apparently.
                        StringBuilder proposal = new StringBuilder(pnsent);
                        while (!cnsent.equals(proposal.toString())) {
                            pn = (Branch) pos.root.children.get(++inpos);
                            proposal.append(pn.sentence());
                            if (!cnsent.contains(proposal.toString())) {
                                System.err.println("Could not find a match.");
                            }
                            newkids.add(pn);
                        }
                    } else if (pnsent.contains(cnsent)) {
                        newkids.add(pn);
                        
                        // the addition to newkids is complete, now we need to advance
                        // the index of the old kids beyond the associated tokens.
                        StringBuilder proposal = new StringBuilder(cnsent);
                        while (!proposal.toString().equals(pnsent)) {
                            cn = (Branch) root.children.get(++i);
                            proposal.append(cn.sentence());
                            if (!pnsent.contains(proposal.toString())) {
                                throw new RuntimeException("Could not find a match.");
                            }
                        }
                    }
                } else if (cn.token.equals("[")) {
                    newkids.add(cn);
                    
                    // find a string of tokens that matches the 
                    // string of tokens from the original chunk.
                    StringBuilder proposal = new StringBuilder(pnsent);
                    int start = inpos;
                    while (true) {
                        if (cnsent.equals(proposal.toString())) {
                            
                            // clear this chunks kids, replace with the POS labeled chunks.
                            cn.children.clear();
                            for (int posindx = start; posindx <= inpos; posindx++) {
                                cn.children.add(pos.root.children.get(posindx));
                            }
                            break;
                        } else {
                            pn = (Branch) pos.root.children.get(++inpos);
                            proposal.append(pn.sentence());
                            String psent = proposal.toString();
                            if (!cnsent.contains(psent)) {
                                
                                // this is a case where the remainer of the content for this chunk falls 
                                // outside the old chunk, it's probably a period after Co., Inc. or Corp.
                                if (psent.endsWith(".")) {
                                    Branch lookahead = (Branch) root.children.get(++i);
                                    if (lookahead.sentence().equals(".")) {
                                        
                                        // clear this chunks kids, replace with the POS labeled chunks.
                                        cn.children.clear();
                                        for (int posindx = start; posindx <= inpos; posindx++) {
                                            cn.children.add(pos.root.children.get(posindx));
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
                } else {
                    System.err.println("There was a top level node not a branch???");
                }
                if ((++inpos) < pos.root.children.size())
                    pn = (Branch) pos.root.children.get(inpos);
            }
            this.root.children = newkids;
        }
        
        /**
         * 
         */
        /** the value of a leaf is simply it's value. */
        public String toString() {
            StringBuffer sb = new StringBuffer();
            for (Node child : root.children) {
                sb.append(child.toString()); 
                sb.append(' ');
            }
            return sb.toString().trim();
        }

        /**
         * Return a sentence spaces stripped to generate a unique hash. Some 
         * cleaning occurs here to make the sentences from POS and those 
         * from the chunker line up.
         * @return the sentence that can then be hashed.
         */
        public String sentence() {
            String ss = root.sentence();
            if (ss.contains("Showtimeoraservice")) {
                return ss.replace("Showtimeoraservice", "Showtimeorasisterservice");
            } if (ss.contains("``I-'m")) { 
                return ss.replace("``I-'m", "``I'm-");
            } if (ss.contains("receivingS$500")) { 
                return ss.replace("receivingS$500", "receivingUS$500");
            } if (ss.contains("donbailofS$1million")) { 
                return ss.replace("donbailofS$1million", "donbailofUS$1million");
            } else {
                return ss;
            }
        }
        
        /**
         * Hashcode from the sentence.
         * @return the hashcode computed from the sentence stripped of space.
         */
        public int hashCode() {
            return root.sentence().hashCode();
        }
        
        /**
         * given a node, traverse it's children given the tokenizer, and recurse into it's 
         * children
         * @param clst the string tokenizer.
         * @param node the tree node.
         */
        private void traverse(StringTokenizer clst, Branch node) {
            
            // we have a node, add it's children
            while(clst.hasMoreTokens()) {
                String nextToken = clst.nextToken();
                if (nextToken == null) {
                    return;
                } else if (nextToken.trim().length() == 0) {
                    continue;
                } else if (nextToken.equals("[")) {

                    // we have a chunk. 
                    if (clst.hasMoreTokens()) {
                        Branch nb = new Branch(nextToken, clst.nextToken());
                        node.children.add(nb);
                        this.traverse(clst, nb);
                    } else {
                        System.err.println("We have a line with a '[' but no label!");
                        System.exit(1);
                    }
                } else if (nextToken.equals("(")) {
                    
                    // we have a chunk. 
                    if (clst.hasMoreTokens()) {
                        Branch nb = new Branch(nextToken, clst.nextToken());
                        node.children.add(nb);
                        this.traverse(clst, nb);
                    } else {
                        System.err.println("We have a line with a '(' but no label!");
                        System.exit(1);
                    }
                } else if (nextToken.equals(")")) {
                    
                    // here we should be at the end of this branch, or a child would have consumed 
                    // this bracket, do sanity checks and return.
                    if (node.token.equals("(")) {
                        
                        // all good we are done.
                        return;
                    } else {
                        System.err.println("End delimiter ')' does not match the opening delim.");
                        System.exit(1);
                    }
                } else if (nextToken.equals("]")) {
                    
                    // here we should be at the end of this branch, or a child would have consumed 
                    // this bracket, do sanity checks and return.
                    if (node.token.equals("[")) {
                        
                        // all good we are done.
                        return;
                    } else {
                        System.err.println("End delimiter ']' does not match the opening delim.");
                        System.exit(1);
                    }
                } else {
                    
                    // we it must be the term, just put it as a value child.
                    node.children.add(new Leaf(nextToken));
                }
            }
        }
    }
}
