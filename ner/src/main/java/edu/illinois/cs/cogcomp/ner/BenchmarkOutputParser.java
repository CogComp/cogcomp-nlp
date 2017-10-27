/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.ner;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

/**
 * <p>This class, given a result file containing output from a benchmark run, will report
 * the F1 values for the level one and level 2 models from the NER output. It can also 
 * report the single token F1 values by passing the "-single" flag.</p>
 * 
 * <p>The values are identified using simple regular expressions, and this class is
 * prone ot break each time the output format changes. But for long parameter sweeps,
 * it's worth it to ensure this class works and fix it up if not, as there may be
 * hundreds of results to extract.</p>
 * @author redman
 */
public class BenchmarkOutputParser {

    /** the file with the results. */
    static File resultsfile = null;
    
    /** report F1 for single tokens only. */
    static boolean single = false;
    
    /** get the F1 score for token only.  */
    static Pattern l2tokenlevelpattern = Pattern.compile(
        "Token-level Acc Level2:[^\n]*[\n]+"
        + " Label[^\\n]*[\\n]+"
        + "--[^\\n]*[\\n]+"
        + "LOC[^\\n]*[\\n]+"
        + "MISC[^\\n]*[\\n]+"
        + "ORG[^\\n]*[\\n]+"
        + "PER[^\\n]*[\\n]+"
        + "---[^\\n]*[\\n]+"
        + "O[^\\n]*[\\n]+"
        + "----[^\\n]*[\\n]+"
        + "Overall[\\s]*[\\d\\.]+[\\s]*[\\d\\.]+[\\s]*([\\d\\.]+)");
        
    /** get the F1 score for token only.  */
    static Pattern ol2tokenlevelpattern = Pattern.compile(
        "Token-level Acc Level2:[^\n]*[\n]+"
        + "[^\\n]*[\\n]+"
        + "--[^\\n]*[\\n]+"
        + "CARD[^\\n]*[\\n]+"
        + "DATE[^\\n]*[\\n]+"
        + "EVENT[^\\n]*[\\n]+"
        + "FAC[^\\n]*[\\n]+"
        + "GPE[^\\n]*[\\n]+"
        + "LAN[^\\n]*[\\n]+"
        + "LAW[^\\n]*[\\n]+"
        + "LOC[^\\n]*[\\n]+"
        + "MON[^\\n]*[\\n]+"
        + "NORP[^\\n]*[\\n]+"
        + "ORDINAL[^\\n]*[\\n]+"
        + "ORG[^\\n]*[\\n]+"
        + "PERCENT[^\\n]*[\\n]+"
        + "PERSON[^\\n]*[\\n]+"
        + "PRODUCT[^\\n]*[\\n]+"
        + "QUANTITY[^\\n]*[\\n]+"
        + "TIME[^\\n]*[\\n]+"
        + "WORK_OF_ART[^\\n]*[\\n]+"
        + "---[^\\n]*[\\n]+"
        + "O[^\\n]*[\\n]+"
        + "----[^\\n]*[\\n]+"
        + "Overall[\\s]*[\\d\\.]+[\\s]*[\\d\\.]+[\\s]*([\\d\\.]+)");

    /** get the F1 score for token only.  */
    static Pattern l1tokenlevelpattern = Pattern.compile(
        "Token-level Acc Level1:[^\n]*[\n]+"
        + " Label[^\\n]*[\\n]+"
        + "--[^\\n]*[\\n]+"
        + "LOC[^\\n]*[\\n]+"
        + "MISC[^\\n]*[\\n]+"
        + "ORG[^\\n]*[\\n]+"
        + "PER[^\\n]*[\\n]+"
        + "---[^\\n]*[\\n]+"
        + "O[^\\n]*[\\n]+"
        + "----[^\\n]*[\\n]+"
        + "Overall[\\s]*[\\d\\.]+[\\s]*[\\d\\.]+[\\s]*([\\d\\.]+)");
    
    /** get the F1 score for token only.  */
    static Pattern ol1tokenlevelpattern = Pattern.compile(
        "Token-level Acc Level1:[^\n]*[\n]+"
            + "[^\\n]*[\\n]+"
            + "--[^\\n]*[\\n]+"
            + "CARD[^\\n]*[\\n]+"
            + "DATE[^\\n]*[\\n]+"
            + "EVENT[^\\n]*[\\n]+"
            + "FAC[^\\n]*[\\n]+"
            + "GPE[^\\n]*[\\n]+"
            + "LAN[^\\n]*[\\n]+"
            + "LAW[^\\n]*[\\n]+"
            + "LOC[^\\n]*[\\n]+"
            + "MON[^\\n]*[\\n]+"
            + "NORP[^\\n]*[\\n]+"
            + "ORDINAL[^\\n]*[\\n]+"
            + "ORG[^\\n]*[\\n]+"
            + "PERCENT[^\\n]*[\\n]+"
            + "PERSON[^\\n]*[\\n]+"
            + "PRODUCT[^\\n]*[\\n]+"
            + "QUANTITY[^\\n]*[\\n]+"
            + "TIME[^\\n]*[\\n]+"
            + "WORK_OF_ART[^\\n]*[\\n]+"
            + "---[^\\n]*[\\n]+"
            + "O[^\\n]*[\\n]+"
            + "----[^\\n]*[\\n]+"
            + "Overall[\\s]*[\\d\\.]+[\\s]*[\\d\\.]+[\\s]*([\\d\\.]+)");
    
    /** get the F1 score for phrase only.  */
    static Pattern phraselevelpattern = Pattern.compile(
        "Phrase-level F1 on the dataset:[^\n]*[\n]+"
        + "[\\s]*Level 1:[\\s]*([\\d\\.]*)[^\\n]*[\\n]+"
        + "[\\s]*Level 2:[\\s]*([\\d\\.]*)");
    
    /**
     * report an error.
     * @param message the error message
     */
    static private void reportError(String message) {
        System.err.println(message);
        System.out.println("java edu.illinois.cs.cogcomp.nerBenchmarkOutputParser -single <results_file_name>");
        System.exit(1);
    }
    
    /**
     * Parse the arguments, look for the file name, and possible "-single" flag.
     * @param args
     */
    static private void parseArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-single")) {
                single = true;
            } else {
                resultsfile = new File(args[i]);
                if (!resultsfile.exists()) {
                    reportError("The directory name \""+args[i]+"\" did not exist, we expect directories with benchmark runs within. ");
                }
                if (!resultsfile.isDirectory()) {
                    reportError("The file \""+args[i]+"\" was not a directory, we expected a directory with benchmark runs within. ");
                }
            }
        }
        if (resultsfile == null)
            reportError("You must specify a directory where the results are found.");
    }
    
    /**
     * get the learning rates and thicknesses from the file name.
     * @param file the file containing the results to parse, filename includes the params.
     * @return the set of parameters for the learner.
     */
    static Parameters parseFilename(File file) {
        String name = file.getName();
        float l1lr, l2lr, l1t, l2t;
        int s = name.indexOf("L1r");
        int e = name.indexOf("-t");
        String sub = name.substring(s+3, e);
        l1lr = Float.parseFloat(sub);
        
        s = e+2;
        e = name.indexOf("+L2r");
        sub = name.substring(s, e);
        l1t = Float.parseFloat(sub);
        
        s = e+4;
        e = name.indexOf("-t", e);
        sub = name.substring(s, e);
        l2lr = Float.parseFloat(sub);
        
        s = e+2;
        sub = name.substring(s);
        l2t = Float.parseFloat(sub);
        return new Parameters(l1lr, l1t, l2lr, l2t);
    }
    
    
    /**
     * This main method will take one required argument, idenfitying the file containing 
     * the results. Optionally, "-single" may also be passed indicating it will extract
     * the F1 value for single token values only.
     * @param args
     * @throws IOException 
     */
    public static void main1(String[] args) throws IOException {
        /** get the F1 score for token only.  */
        String fuck = "Token-level Acc Level1:\n" + 
            "   Label    Precision Recall   F1   LCount PCount\n" + 
            "-------------------------------------------------\n" + 
            "CARDINAL       80.556 84.083 82.281   1690   1764\n" + 
            "DATE           92.273 86.528 89.308   5990   5617\n" + 
            "EVENT          89.668 47.184 61.832    515    271\n" + 
            "FAC            74.494 33.273 46.000    553    247\n" + 
            "GPE            92.100 92.054 92.077   4002   4000\n" + 
            "LANGUAGE       81.818 42.857 56.250     21     11\n" + 
            "LAW            76.531 27.881 40.872    269     98\n" + 
            "LOC            76.068 71.429 73.675    623    585\n" + 
            "MONEY          94.816 91.446 93.100   1660   1601\n" + 
            "NORP           92.418 93.841 93.125   1299   1319\n" + 
            "ORDINAL        80.051 94.627 86.731    335    396\n" + 
            "ORG            88.290 88.837 88.563   8080   8130\n" + 
            "PERCENT        96.899 92.957 94.887   1647   1580\n" + 
            "PERSON         93.896 94.858 94.374   4492   4538\n" + 
            "PRODUCT        74.658 50.463 60.221    216    146\n" + 
            "QUANTITY       96.581 72.128 82.582    470    351\n" + 
            "TIME           83.688 55.573 66.792    637    423\n" + 
            "WORK_OF_ART    73.611 33.491 46.037    633    288\n" + 
            "-------------------------------------------------\n" + 
            "O              98.293 99.313 98.801 170279 172046\n" + 
            "-------------------------------------------------\n" + 
            "Overall        90.171 85.362 87.700  33132  31365\n" + 
            "Accuracy       97.041   -      -      -    203411\n" + 
            "";
        Pattern tt = Pattern.compile(
            "Token-level Acc Level1:[^\n]*[\n]+"
            + "[^\\n]*[\\n]+"
            + "--[^\\n]*[\\n]+"
            + "CARD[^\\n]*[\\n]+"
            + "DATE[^\\n]*[\\n]+"
            + "EVENT[^\\n]*[\\n]+"
            + "FAC[^\\n]*[\\n]+"
            + "GPE[^\\n]*[\\n]+"
            + "LAN[^\\n]*[\\n]+"
            + "LAW[^\\n]*[\\n]+"
            + "LOC[^\\n]*[\\n]+"
            + "MON[^\\n]*[\\n]+"
            + "NORP[^\\n]*[\\n]+"
            + "ORDINAL[^\\n]*[\\n]+"
            + "ORG[^\\n]*[\\n]+"
            + "PERCENT[^\\n]*[\\n]+"
            + "PERSON[^\\n]*[\\n]+"
            + "PRODUCT[^\\n]*[\\n]+"
            + "QUANTITY[^\\n]*[\\n]+"
            + "TIME[^\\n]*[\\n]+"
            + "WORK_OF_ART[^\\n]*[\\n]+"
            + "---[^\\n]*[\\n]+"
            + "O[^\\n]*[\\n]+"
            + "----[^\\n]*[\\n]+"
            + "Overall[\\s]*[\\d\\.]+[\\s]*[\\d\\.]+[\\s]*([\\d\\.]+)");
        
        Matcher matcher = tt.matcher(fuck);
        if (matcher.find()) {
            String tokenL2 = matcher.group(1);
            System.out.println(tokenL2);
        } else System.err.println("NOT");


    }
    /**
     * This main method will take one required argument, idenfitying the file containing 
     * the results. Optionally, "-single" may also be passed indicating it will extract
     * the F1 value for single token values only.
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        parseArgs(args);
        System.out.println("L1lr,L1t,L2lr,L2t,L1 token,L2 token,F1,F2");
        for (File file : resultsfile.listFiles()) {
            if (file.getName().startsWith("L1r")) {
                File resultsfile = new File(file, "ner/results.out");
                if (resultsfile.exists()) {
                    try {
                    Parameters p = parseFilename(file);
                    String lines = FileUtils.readFileToString(resultsfile);
                    
                    // get the token level score.
                    String tokenL2 = null, tokenL1 = null;
                    Matcher matcher = l2tokenlevelpattern.matcher(lines);
                    if (matcher.find())
                        tokenL2 = matcher.group(1);
                    else {
                        matcher = ol2tokenlevelpattern.matcher(lines);
                        if (matcher.find())
                            tokenL2 = matcher.group(1);
                        else
                            System.err.println("No token level match");
                    }
                    
                    matcher = l1tokenlevelpattern.matcher(lines);
                    if (matcher.find())
                        tokenL1 = matcher.group(1);
                    else {
                        matcher = ol1tokenlevelpattern.matcher(lines);
                        if (matcher.find())
                            tokenL1 = matcher.group(1);
                        else
                            System.err.println("No token level match");
                    }
                    
                    matcher = phraselevelpattern.matcher(lines);
                    matcher.find();
                    String phraseL1 = matcher.group(1);
                    String phraseL2 = matcher.group(2);
                    System.out.println(p.toString()+","+tokenL1+","+tokenL2+","+phraseL1+","+phraseL2);
                    } catch (java.lang.IllegalStateException ise) {
                        System.err.println("The results file could not be parsed : \""+resultsfile+"\"");
                    }
                } else {
                    System.err.println("no results in "+resultsfile);
                }
                
            }
        }
    }
    
    /**
     * wraps the args.
     * @author redman
     */
    static class Parameters {
        
        /** learning rates for L1 model. */
        float l1lr;
        
        /** thicknesses for L1. */
        float l2lr;
        
        /** learning rates for L2. */
        float l1t;
       
        /** thicknesses for L2 models. */
        float l2t;
        
        /**
         * Construct the wrapper.
         * @param l1lr L1 learning rate.
         * @param l1t L1 thickness.
         * @param l2lr L2 lr.
         * @param l2t L2 thickness.
         */
        Parameters (float l1lr, float l1t, float l2lr, float l2t) {
            this.l1lr = l1lr;
            this.l1t = l1t;
            this.l2lr = l2lr;
            this.l2t = l2t;
        }
        
        /**
         * this produces csv output.
         */
        public String toString() {
            return l1lr+","+l1t+","+l2lr+","+l2t;
        }
    }
}
