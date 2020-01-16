/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.edison.features;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

import java.util.*;
import java.io.*;

/**
 * The ExtractAbbrev class implements a simple algorithm for
 * extraction of abbreviations and their definitions from biomedical text.
 * Abbreviations (short forms) are extracted from the input file, and those abbreviations
 * for which a definition (long form) is found are printed out, along with that definition,
 * one per line.
 *
 * The algorithm supposed based on "A SIMPLE ALGORITHM FOR IDENTIFYING ABBREVIATION
 * DEFINITIONS IN BIOMEDICAL TEXT", ARIEL S. SCHWARTZ MARTI A. HEARST:
 * http://biotext.berkeley.edu/papers/psb03.pdf
 *
 * A file consisting of short-form/long-form pairs (tab separated) can be specified
 * in tandem with the -testlist option for the purposes of evaluating the algorithm.
 *
 * @see <a href="http://biotext.berkeley.edu/papers/psb03.pdf">A Simple Algorithm for Identifying Abbreviation Definitions in Biomedical Text</a>
 * A.S. Schwartz, M.A. Hearst; Pacific Symposium on Biocomputing 8:451-462(2003)
 * for a detailed description of the algorithm.
 *
 *
 * @author Ariel Schwartz
 * @author Daniel Khashabi (created the annotator)
 * @version 03/12/03
 */
public class AbbreviationFeatureExtractor extends Annotator {

    static String defaultViewName = "ABBREVIATION";
    HashMap mTestDefinitions = new HashMap();
    HashMap mStats = new HashMap();
    int truePositives = 0, falsePositives = 0, falseNegatives = 0, trueNegatives = 0;
    char delimiter = '\t';
    boolean testMode = false;

    public AbbreviationFeatureExtractor() {
        super(defaultViewName, new String[]{});
    }

    public AbbreviationFeatureExtractor(String viewName, String[] requiredViews) {
        super(viewName, requiredViews);
    }

    private boolean isValidShortForm(String str) {
        return (hasLetter(str) && (Character.isLetterOrDigit(str.charAt(0)) || (str.charAt(0) == '(')));
    }

    private boolean hasLetter(String str) {
        for (int i=0; i < str.length() ; i++)
            if (Character.isLetter(str.charAt(i)))
                return true;
        return false;
    }

    private boolean hasCapital(String str) {
        for (int i=0; i < str.length() ; i++)
            if (Character.isUpperCase(str.charAt(i)))
                return true;
        return false;
    }

    private void loadTrueDefinitions(String inFile) {
        String abbrString, defnString, str = "";
        Vector entry;
        HashMap definitions = mTestDefinitions;

        try {
            BufferedReader fin = new BufferedReader(new FileReader (inFile));
            while ((str = fin.readLine()) != null) {
                int j = str.indexOf(delimiter);
                abbrString = str.substring(0,j).trim();
                defnString = str.substring(j,str.length()).trim();
                entry = (Vector)definitions.get(abbrString);
                if (entry == null)
                    entry = new Vector();
                entry.add(defnString);
                definitions.put(abbrString, entry);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(str);
        }
    }

    private boolean isTrueDefinition(String shortForm, String longForm) {
        Vector entry;
        Iterator itr;

        entry = (Vector)mTestDefinitions.get(shortForm);
        if (entry == null)
            return false;
        itr = entry.iterator();
        while(itr.hasNext()){
            if (itr.next().toString().equalsIgnoreCase(longForm))
                return true;
        }
        return false;
    }

    private Vector<Pair> extractAbbrPairsFromFile(String inFile) {
        BufferedReader fin = null;
        try {
            fin = new BufferedReader(new FileReader(inFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return extractAbbrPairs(fin);
    }

    private Vector<Pair> extractAbbrPairsFromRawString(String input) {
        BufferedReader fin = null;
        // convert String into InputStream
        InputStream is = new ByteArrayInputStream(input.getBytes());

        // read it with BufferedReader
        fin  = new BufferedReader(new InputStreamReader(is));
        return extractAbbrPairs(fin);
    }

    private Vector<Pair> extractAbbrPairs(BufferedReader fin) {
        String str, tmpStr, longForm = "", shortForm = "";
        String currSentence = "";
        int openParenIndex, closeParenIndex = -1, sentenceEnd, newCloseParenIndex, tmpIndex = -1;
        boolean newParagraph = true;
        StringTokenizer shortTokenizer;
        Vector candidates = new Vector();
        try {

            while ((str = fin.readLine()) != null) {
                if (str.length() == 0 || newParagraph &&
                        ! Character.isUpperCase(str.charAt(0))){
                    currSentence = "";
                    newParagraph = true;
                    continue;
                }
                newParagraph = false;
                str += " ";
                currSentence += str;
                openParenIndex =  currSentence.indexOf(" (");
                do {
                    if (openParenIndex > -1)
                        openParenIndex++;
                    sentenceEnd = Math.max(currSentence.lastIndexOf(". "), currSentence.lastIndexOf(", "));
                    if ((openParenIndex == -1) && (sentenceEnd == -1)) {
                        //Do nothing
                    }
                    else if (openParenIndex == -1) {
                        currSentence = currSentence.substring(sentenceEnd + 2);
                    } else if ((closeParenIndex = currSentence.indexOf(')',openParenIndex)) > -1){
                        sentenceEnd = Math.max(currSentence.lastIndexOf(". ", openParenIndex),
                                currSentence.lastIndexOf(", ", openParenIndex));
                        if (sentenceEnd == -1)
                            sentenceEnd = -2;
                        longForm = currSentence.substring(sentenceEnd + 2, openParenIndex);
                        shortForm = currSentence.substring(openParenIndex + 1, closeParenIndex);
                    }
                    if (shortForm.length() > 0 || longForm.length() > 0) {
                        if (shortForm.length() > 1 && longForm.length() > 1) {
                            if ((shortForm.indexOf('(') > -1) &&
                                    ((newCloseParenIndex = currSentence.indexOf(')', closeParenIndex + 1)) > -1)){
                                shortForm = currSentence.substring(openParenIndex + 1, newCloseParenIndex);
                                closeParenIndex = newCloseParenIndex;
                            }
                            if ((tmpIndex = shortForm.indexOf(", ")) > -1)
                                shortForm = shortForm.substring(0, tmpIndex);
                            if ((tmpIndex = shortForm.indexOf("; ")) > -1)
                                shortForm = shortForm.substring(0, tmpIndex);
                            shortTokenizer = new StringTokenizer(shortForm);
                            if (shortTokenizer.countTokens() > 2 || shortForm.length() > longForm.length()) {
                                // Long form in ( )
                                tmpIndex = currSentence.lastIndexOf(" ", openParenIndex - 2);
                                tmpStr = currSentence.substring(tmpIndex + 1, openParenIndex - 1);
                                longForm = shortForm;
                                shortForm = tmpStr;
                                if (! hasCapital(shortForm))
                                    shortForm = "";
                            }
                            if (isValidShortForm(shortForm)){
                                if(extractValidAbbrPair(shortForm.trim(), longForm.trim())) {
                                    candidates.add(new Pair(shortForm.trim(), longForm.trim()));
                                }
                            }
                        }
                        currSentence = currSentence.substring(closeParenIndex + 1);
                    } else if (openParenIndex > -1) {
                        if ((currSentence.length() - openParenIndex) > 200)
                            // Matching close paren was not found
                            currSentence = currSentence.substring(openParenIndex + 1);
                        break; // Read next line
                    }
                    shortForm = "";
                    longForm = "";
                } while ((openParenIndex =  currSentence.indexOf(" (")) > -1);
            }
            fin.close();
        } catch (Exception ioe) {
            ioe.printStackTrace();
            System.out.println(currSentence);
            System.out.println(tmpIndex);
        }
        return candidates;
    }

    private String findBestLongForm(String shortForm, String longForm) {
        int sIndex;
        int lIndex;
        char currChar;

        sIndex = shortForm.length() - 1;
        lIndex = longForm.length() - 1;
        for ( ; sIndex >= 0; sIndex--) {
            currChar = Character.toLowerCase(shortForm.charAt(sIndex));
            if (!Character.isLetterOrDigit(currChar))
                continue;
            while (((lIndex >= 0) && (Character.toLowerCase(longForm.charAt(lIndex)) != currChar)) ||
                    ((sIndex == 0) && (lIndex > 0) && (Character.isLetterOrDigit(longForm.charAt(lIndex - 1)))))
                lIndex--;
            if (lIndex < 0)
                return null;
            lIndex--;
        }
        lIndex = longForm.lastIndexOf(" ", lIndex) + 1;
        return longForm.substring(lIndex);
    }

    /**
     * True shows valid abbreviation pairs
     */
    private boolean extractValidAbbrPair(String shortForm, String longForm) {
        String bestLongForm;
        StringTokenizer tokenizer;
        int longFormSize, shortFormSize;

        if (shortForm.length() == 1)
            return false;
        bestLongForm = findBestLongForm(shortForm, longForm);
        if (bestLongForm == null)
            return false;
        tokenizer = new StringTokenizer(bestLongForm, " \t\n\r\f-");
        longFormSize = tokenizer.countTokens();
        shortFormSize = shortForm.length();
        for (int i=shortFormSize - 1; i >= 0; i--)
            if (!Character.isLetterOrDigit(shortForm.charAt(i)))
                shortFormSize--;
        if (bestLongForm.length() < shortForm.length() ||
                bestLongForm.indexOf(shortForm + " ") > -1 ||
                bestLongForm.endsWith(shortForm) ||
                longFormSize > shortFormSize * 2 ||
                longFormSize > shortFormSize + 5 ||
                shortFormSize > 10)
            return false;

        if (testMode) {
            if (isTrueDefinition(shortForm, bestLongForm)) {
                System.out.println(shortForm + delimiter + bestLongForm + delimiter + "TP");
                truePositives++;
            }
            else {
                falsePositives++;
                System.out.println(shortForm + delimiter + bestLongForm + delimiter + "FP");
            }
        }
        else {
            System.out.println(shortForm + delimiter + bestLongForm);
        }
        return true;
    }

    private static void usage() {
        System.err.println("Usage: ExtractAbbrev [-options] <filename>");
        System.err.println("       <filename> contains text from which abbreviations are extracted" );
        System.err.println("       -testlist <file> = list of true abbreviation definition pairs");
        System.err.println("       -usage or -help = this message");
        System.exit(1);
    }

    public static void main(String[] args) {
        String filename =  null;
        String testList = null;

        AbbreviationFeatureExtractor extractAbbrev = new AbbreviationFeatureExtractor();
        //parse arguments
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-testlist")) {
                if (i == args.length - 1) {
                    usage();
                }
                testList = args[++i];
                extractAbbrev.testMode = true;
            } else if (args[i].equals("-usage")) {
                usage();
            } else if (args[i].equals("-help")) {
                usage();
            } else {
                filename = args[i];
                // Must be last arg
                if (i != args.length - 1) {
                    usage();
                }
            }
        }
        if (filename == null) {
            usage();
        }

        if (extractAbbrev.testMode)
            extractAbbrev.loadTrueDefinitions(testList);
        extractAbbrev.extractAbbrPairsFromFile(filename);
        if (extractAbbrev.testMode)
            System.out.println("TP: " + extractAbbrev.truePositives + " FP: " + extractAbbrev.falsePositives +
                    " FN: " + extractAbbrev.falseNegatives + " TN: " + extractAbbrev.trueNegatives);
    }

    @Override
    public void initialize(ResourceManager rm) {
        // do nothing
    }

    @Override
    protected void addView(TextAnnotation ta) throws AnnotatorException {
        String text = ta.getText();
        System.out.println("text: " + text);
        Vector<Pair> v = extractAbbrPairsFromRawString(text);
        System.out.println("v = " + v);
        if (!ta.hasView(this.viewName)) {
            View view = new SpanLabelView(this.viewName, ta);
            for(Pair p : v) {
                System.out.println(p);
                String shortS = p.getFirst().toString().trim();
                String longS = p.getSecond().toString().trim();
                int charStart = text.indexOf(longS);
                int charEnd = charStart + longS.length() - 1;
                int start = ta.getTokenIdFromCharacterOffset(charStart);
                int end = ta.getTokenIdFromCharacterOffset(charEnd);
                view.addConstituent(new Constituent(shortS, this.viewName, ta, start, end));
            }
            ta.addView(this.viewName, view);
        }
        else {
            System.err.println("Already has the abbreviation view");
        }
    }
}

