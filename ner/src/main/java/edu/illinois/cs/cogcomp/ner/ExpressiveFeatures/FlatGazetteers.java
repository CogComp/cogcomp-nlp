/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.ExpressiveFeatures;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.ner.IO.ResourceUtilities;
import edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord;
import edu.illinois.cs.cogcomp.ner.LbjTagger.ParametersForLbjCode;
import edu.illinois.cs.cogcomp.ner.StringStatisticsUtils.MyString;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import gnu.trove.set.hash.THashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This singleton class contains all the gazetteer data and dictionaries. Can only be accessed via
 * the get() method all constructors are private.
 *
 * annotate() takes a NEWord object and scans up to 4 words beyond. For each k-word expression 1 <=
 * k <=5, it checks if that expression is present in any gazetteer. If so, every NEWord object
 * corresponding to a word in that expression gets a new gazetteers attribute with a BIOLU tag plus
 * gazetteer name as label.
 * 
 * @author redman
 */
public class FlatGazetteers implements Gazetteers {

    /**
     * Making this private ensures singleton.
     */
    private FlatGazetteers() {}

    private Logger logger = LoggerFactory.getLogger(Gazetteers.class);
    private ArrayList<String> dictNames = new ArrayList<>();
    private ArrayList<THashSet<String>> dictionaries = null;
    private ArrayList<THashSet<String>> dictionariesIgnoreCase = null;
    private ArrayList<THashSet<String>> dictionariesOneWordIgnorePunctuation = null;

    public FlatGazetteers(String pathToDictionaries) throws IOException {
        dictNames = new ArrayList<>();
        dictionaries = null;
        dictionariesIgnoreCase = null;
        dictionariesOneWordIgnorePunctuation = null;
        ArrayList<String> filenames = new ArrayList<>();
        // List the Gazetteers directory (either local or in the classpath)
        // XXX Needed to add the dir listing file since there is no easy way to read inside the
        // directories of a jar
        String[] allfiles =
                ResourceUtilities.lsDirectory(pathToDictionaries, "gazetteers-list.txt");
        for (String file : allfiles) {
            if (!IOUtils.isDirectory(file)) {
                filenames.add(file);
                dictNames.add(file);
            }
        }
        Arrays.sort(allfiles);

        dictionaries = new ArrayList<>(filenames.size());
        dictionariesIgnoreCase = new ArrayList<>(filenames.size());
        dictionariesOneWordIgnorePunctuation = new ArrayList<>(filenames.size());

        for (int i = 0; i < filenames.size(); i++) {
            String file = filenames.get(i);
            dictionaries.add(new THashSet<String>());
            dictionariesIgnoreCase.add(new THashSet<String>());
            dictionariesOneWordIgnorePunctuation.add(new THashSet<String>());
            // The reading of the files
            InputStream res = ResourceUtilities.loadResource(filenames.get(i));
            if (null != res) {
                BufferedReader in = null;
                try {
                    in = new BufferedReader(new InputStreamReader(res));
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(0);
                }

                String line = in.readLine();
                while (line != null) {
                    line = line.trim();
                    dictionaries.get(i).add(line);
                    if ((!line.equalsIgnoreCase("in")) && (!line.equalsIgnoreCase("on"))
                            && (!line.equalsIgnoreCase("us")) && (!line.equalsIgnoreCase("or"))
                            && (!line.equalsIgnoreCase("am")))
                        dictionariesIgnoreCase.get(i).add(line.toLowerCase());
                    String[] tmp = line.split(" ");
                    for (String ss : tmp) {
                        String s = MyString.cleanPunctuation(ss);
                        if (s.length() >= 5 && Character.isUpperCase(s.charAt(0))) {
                            dictionariesOneWordIgnorePunctuation.get(i).add(s);
                        }
                    }
                    line = in.readLine();
                }
                in.close();
            }
        }
    }

    public void annotate(NEWord w) {
        if (w.gazetteers == null)
            w.gazetteers = new ArrayList<>();
        NEWord endWord = (NEWord) (w.next);
        String expression = w.form;
        boolean changeEnd = true;
        // w.normalizedMostLinkableExpression is populated if wikifier link functionality is active;
        // default inactive
        if (w.normalizedMostLinkableExpression != null) {
            if (w.gazetteers == null)
                w.gazetteers = new ArrayList<>();
            for (int j = 0; j < dictionaries.size(); j++)
                if (dictionaries.get(j).contains(w.normalizedMostLinkableExpression)) {
                    if (w.normalizedForm != null && !w.normalizedForm.equalsIgnoreCase(w.form))
                        w.gazetteers.add("Normalized_Expression_Gaz_Match(*)" + dictNames.get(j));
                    else
                        w.gazetteers.add("Normalized_Expression_Gaz_Match" + dictNames.get(j));
                }
        }
        for (int i = 0; i < 5 && changeEnd; i++) {
            changeEnd = false;

            for (int j = 0; j < dictionaries.size(); j++) {
                if (dictionaries.get(j).contains(expression)) {
                    NEWord temp = w;
                    if (temp.gazetteers == null)
                        temp.gazetteers = new ArrayList<>();
                    if (i == 0) {
                        temp.gazetteers.add("U-" + dictNames.get(j));
                    } else {
                        int loc = 0;
                        while (temp != endWord) {
                            if (temp.gazetteers == null) {
                                temp.gazetteers = new ArrayList<>();
                            }
                            if (loc == 0) {
                                temp.gazetteers.add("B-" + dictNames.get(j));
                            }
                            if (loc > 0 && loc < i) {
                                temp.gazetteers.add("I-" + dictNames.get(j));
                            }
                            if (loc == i) {
                                temp.gazetteers.add("L-" + dictNames.get(j));
                            }
                            temp = (NEWord) temp.next;
                            loc++;
                        }
                    }
                }
                if (dictionariesIgnoreCase.get(j).contains(expression.toLowerCase())) {
                    NEWord temp = w;
                    if (temp.gazetteers == null)
                        temp.gazetteers = new ArrayList<>();
                    if (i == 0) {
                        temp.gazetteers.add("U-" + dictNames.get(j) + "(IC)");
                    } else {
                        int loc = 0;
                        while (temp != endWord) {
                            if (temp.gazetteers == null) {
                                temp.gazetteers = new ArrayList<>();
                            }
                            if (loc == 0) {
                                temp.gazetteers.add("B-" + dictNames.get(j) + "(IC)");
                            }
                            if (loc > 0 && loc < i) {
                                temp.gazetteers.add("I-" + dictNames.get(j) + "(IC)");
                            }
                            if (loc == i) {
                                temp.gazetteers.add("L-" + dictNames.get(j) + "(IC)");
                            }
                            temp = (NEWord) temp.next;
                            loc++;
                        }
                    }
                }
            } // dictionaries
            if (endWord != null) {
                expression += " " + endWord.form;
                endWord = (NEWord) endWord.next;
                changeEnd = true;
            }
        } // i
    }

    public String annotateConstituent(Constituent c, boolean isBIO){
        String expression = c.toString();
        String ret = "";
        View bioView = c.getTextAnnotation().getView(ViewNames.TOKENS);
        for (int startIdx = -4; startIdx <= 0; startIdx ++){
            for (int len = Math.abs(startIdx); len <= 4; len ++) {
                String combinedExpression = "";
                boolean integrity = true;
                for (int pointer = startIdx; pointer <= startIdx + len; pointer++) {
                    int curTokenIdx = c.getStartSpan() + pointer;
                    if (curTokenIdx < 0 || curTokenIdx >= bioView.getEndSpan()) {
                        integrity = false;
                        break;
                    }
                    String pointerString = bioView.getConstituentsCoveringToken(curTokenIdx).get(0).toString();
                    combinedExpression += pointerString + " ";
                }
                if (combinedExpression.endsWith(" ")) {
                    combinedExpression = combinedExpression.substring(0, combinedExpression.length() - 1);
                }
                if (integrity) {
                    for (int i = 0; i < dictionaries.size(); i++) {
                        if (dictionaries.get(i).contains(combinedExpression)) {
                            String fullName = dictNames.get(i);
                            String shortName = fullName.split("/")[fullName.split("/").length - 1];
                            if (isBIO) {
                                if (startIdx == 0) {
                                    ret += "B-" + shortName + ",";
                                } else {
                                    ret += "L-" + shortName + ",";
                                }
                            }
                            else {
                                if (startIdx == 0 && len == 0){
                                    ret += "U-" + shortName + ",";
                                }
                                if (startIdx + len == 0){
                                    ret += "L-" + shortName + ",";
                                }
                                if (startIdx == 0 && len > 0){
                                    ret += "B-" + shortName + ",";
                                }
                                if (startIdx < 0 && startIdx + len > 0){
                                    ret += "I-" + shortName + ",";
                                }
                            }
                        }
                    }
                    for (int i = 0; i < dictionariesIgnoreCase.size(); i++) {
                        if (dictionariesIgnoreCase.get(i).contains(combinedExpression.toLowerCase())) {
                            String fullName = dictNames.get(i);
                            String shortName = fullName.split("/")[fullName.split("/").length - 1] + "(IC)";
                            if (isBIO) {
                                if (startIdx == 0) {
                                    ret += "B-" + shortName + ",";
                                } else {
                                    ret += "L-" + shortName + ",";
                                }
                            }
                            else {
                                if (startIdx == 0 && len == 0){
                                    ret += "U-" + shortName + ",";
                                }
                                if (startIdx + len == 0){
                                    ret += "L-" + shortName + ",";
                                }
                                if (startIdx == 0 && len > 0){
                                    ret += "B-" + shortName + ",";
                                }
                                if (startIdx < 0 && startIdx + len > 0){
                                    ret += "I-" + shortName + ",";
                                }
                            }
                        }
                    }
                }
            }
        }
        return ret;
    }

    public String annotatePhrase(Constituent phrase){
        String expression = phrase.toString();
        String ret = "";
        for (int i = 0; i < dictionaries.size(); i++) {
            if (dictionaries.get(i).contains(expression)) {
                String fullName = dictNames.get(i);
                String shortName = fullName.split("/")[fullName.split("/").length - 1];
                ret += shortName + ",";
            }
        }
        for (int i = 0; i < dictionariesIgnoreCase.size(); i++){
            if (dictionaries.get(i).contains(expression.toLowerCase())){
                String fullName = dictNames.get(i);
                String shortName = fullName.split("/")[fullName.split("/").length - 1];
                ret += shortName + "(IC),";
            }
        }
        return ret;
    }
}
