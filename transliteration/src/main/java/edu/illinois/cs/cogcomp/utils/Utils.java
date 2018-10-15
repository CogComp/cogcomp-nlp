/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.utils;

import com.ibm.icu.text.Transliterator;
import edu.illinois.cs.cogcomp.core.algorithms.LevensteinDistance;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.transliteration.Example;
import edu.illinois.cs.cogcomp.transliteration.MultiExample;
import edu.illinois.cs.cogcomp.transliteration.SPModel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * Created by mayhew2 on 11/17/15.
 */
public class Utils {

    private static Logger logger = LoggerFactory.getLogger(Utils.class);

    /**
     * This reads a file in the ngram-format
     * http://www.speech.sri.com/projects/srilm/manpages/ngram-format.5.html
     *
     * and populates the languagemodel datastructure.
     *
     * @param fname
     * @return
     * @throws FileNotFoundException
     */
    public static HashMap<String, Double> readSRILM(String fname) throws FileNotFoundException {
        List<String> lines = LineIO.read(fname);

        HashMap<String, Double> out = new HashMap<>();

        for(String line : lines){

            if(line.trim().length() == 0 || line.startsWith("\\") || line.contains("ngram") ) {
                // do nothing.
            }else{
                String[] sline = line.trim().split("\t");
                // important because of the log probabilities
                Double v = Math.exp(Double.parseDouble(sline[0]));
                String ngram = sline[1];

                String[] chars = ngram.split(" ");

                out.put(StringUtils.join(chars, ""), v);
            }
        }

        return out;
    }

    /**
     * This measures the WAVE score of a set of productions. WAVE score comes from (Kumaran et al 2010)
     * It is a measure of transliterability.
     * @param fname the file name of a set of learned productions.
     * @return WAVE score
     */
    public static double WAVE(String fname) throws FileNotFoundException {
        List<String> lines = LineIO.read(fname);

        HashMap<String, Integer> srcFreq = new HashMap<>();
        HashMap<String, Integer> tgtFreq = new HashMap<>();

        HashMap<String, Double> entropy = new HashMap<>();

        for(String line : lines){

            if(line.trim().length() == 0 || line.startsWith("#")){
                continue;
            }

            String[] sline = line.split("\t");

            String src = sline[0];
            String tgt = sline[1];
            double prob = Double.parseDouble(sline[2]);

            Dictionaries.IncrementOrSet(srcFreq, src, 1, 1);

            Dictionaries.IncrementOrSet(tgtFreq, tgt, 1, 1);

            double v = prob * Math.log(prob);
            Dictionaries.IncrementOrSet(entropy, src, v, v);

        }

        double total = 0;
        for(int v : srcFreq.values()){
            total += v;
        }

        double WAVE = 0;

        for(String i : srcFreq.keySet()){
            // -= because entropy should be negative, but I never do it.
            WAVE -= srcFreq.get(i) / total * entropy.get(i) ;
        }

        return WAVE;
    }


    /**
     * This is a measure used by NEWS2015.
     * @param prediction
     * @param referents
     * @return
     */
    public static double GetFuzzyF1(String prediction, List<String> referents){
        // calculate Fuzzy F1
        String cand = prediction;
        double bestld = Double.MAX_VALUE;
        String bestref = "";
        for (String reference : referents) {
            double ld = LevensteinDistance.getLevensteinDistance(reference, cand);
            if (ld < bestld) {
                bestref = reference;
                bestld = ld;
            }
        }

        double lcs = (cand.length() + bestref.length() - bestld) * 0.5;
        double R = lcs / bestref.length();
        double P = lcs / cand.length();
        double F1 = 2 * R * P / (R + P);
        return F1;
    }

    /**
     * Helper method, this will always rearrange the data according to edit distance.
     * @param file
     * @return
     */
    public static List<Example> readWikiData(String file) throws FileNotFoundException {
        return readWikiData(file, true);
    }

    /**
     * This reads data in the format created by the wikipedia-api project, commonly named wikidata.Language
     * @param file name of file
     * @param fix whether or not the names should be reordered according to edit distance.
     * @return list of examples
     * @throws FileNotFoundException
     */
    public static List<Example> readWikiData(String file, boolean fix) throws FileNotFoundException {
        List<Example> examples = new ArrayList<>();
        List<String> lines = LineIO.read(file);

        String id = "Any-Latin; NFD; [^\\p{Alnum}] Remove";
        //id = "Any-Latin; NFD";
        Transliterator t = Transliterator.getInstance(id);

        HashSet<Example> unique = new HashSet<>();

        int skipping = 0;

        for(String line : lines)
        {
            if(line.contains("#")){
                continue;
            }

            String[] parts = line.split("\t");

            if(parts.length < 2){
                continue;
            }


            // In wikipedia data, the foreign name comes first, English second.
            String foreign = parts[0].toLowerCase();
            String english = parts[1].toLowerCase();
            String[] ftoks = foreign.split(" ");
            String[] etoks = english.split(" ");

            if(ftoks.length != etoks.length){
                logger.error("Mismatching length of tokens: " + english);
                skipping++;
                continue;
            }

            // other heuristics to help clean data
            if(english.contains("jr.") || english.contains("sr.") ||
                    english.contains(" of ") || english.contains(" de ") ||
                    english.contains("(") || english.contains("pope ")){
                skipping++;
                //logger.debug("Skipping: " + english);
                continue;
            }



            int numtoks = ftoks.length;

            for(int i = 0; i < numtoks; i++){
                String ftrans = t.transform(ftoks[i]);

                int mindist = Integer.MAX_VALUE;
                String bestmatch = null;

                // this is intended to help with ordering.
                for(int j = 0; j < numtoks; j++){
                    int d = LevensteinDistance.getLevensteinDistance(ftrans, etoks[j]);
                    if(d < mindist){
                        // match etoks[j] with ftrans
                        bestmatch = etoks[j];
                        mindist = d;

                        // then take etoks[j] out of the running
                    }
                }

                // strip those pesky commas.
                if(ftoks[i].endsWith(",")){
                    ftoks[i] = ftoks[i].substring(0,ftoks[i].length()-1);
                }

                // This version uses transliterated words as the target (cheating)
                //examples.add(new Example(bestmatch, ftrans));

                Example addme;
                if(fix) {
                    // This uses the best aligned version (recommended)
                    addme = new Example(bestmatch, ftoks[i]);

                }else {
                    // This assumes the file ordering is correct
                    addme = new Example(etoks[i], ftoks[i]);
                }
                examples.add(addme);
                unique.add(addme);
            }

        }
        //System.out.println(file.split("\\.")[1] + " & " + numnames + " & " + examples.size() + " & " + unique.size() + " \\\\");
        logger.debug(String.format("Skipped %d lines", skipping));
        return new ArrayList<>(unique);

    }

    /**
     * This reads data from the Anne Irvine, CCB paper called Transliterating from Any Language.
     * @return
     */
    public static List<Example> readCCBData(String srccode, String targetcode) throws FileNotFoundException {
        List<Example> examples = new ArrayList<>();

        String fname = "/shared/corpora/transliteration/from_anne_irvine/wikipedia_names";
        List<String> lines = LineIO.read(fname);

        List<String> key = Arrays.asList(lines.get(0).split("\t"));
        int srcind = key.indexOf(srccode);
        int tgtind = key.indexOf(targetcode);

        System.out.println(srcind + ", " + tgtind);

        int i = 0;
        for(String line : lines) {
            if (i == 0 || line.trim().length() == 0) {
                i++;
                continue;
            }

            String[] sline = line.split("\t");

            // Java removes whitespace at the end of a line.
            if(tgtind >= sline.length){
                i++;
                continue;
            }

            String src = sline[srcind].trim();
            String tgt = sline[tgtind].trim();

            if (src.length() > 0 && tgt.length() > 0) {
                Example e = new Example(src, tgt);
                examples.add(e);
            }

            i++;
        }

        return examples;
    }


    public static List<Example> convertMulti(List<MultiExample> lme){
        List<Example> training = new ArrayList<>();
        for(MultiExample me : lme){
            for(Example e : me.toExampleList()){
                String[] tls = e.getTransliteratedWord().split(" ");
                String[] ss = e.sourceWord.split(" ");

                if(tls.length != ss.length){
                    logger.error("Mismatched length: " + e.sourceWord);
                    continue;
                }

                for(int i = 0; i < tls.length; i++){
                    training.add(new Example(ss[i], tls[i]));
                }
            }
        }
        return training;
    }

    /**
     * Used for reading data from the NEWS2015 dataset.
     * @param fname
     * @return
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    public static List<MultiExample> readNEWSData(String fname) throws ParserConfigurationException, IOException, SAXException {
        File file = new File(fname);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(file);

        NodeList nl = document.getElementsByTagName("Name");

        List<MultiExample> examples = new ArrayList<>();

        for(int i = 0; i < nl.getLength(); i++){
            Node n = nl.item(i);

            NodeList sourceandtargets = n.getChildNodes();
            MultiExample me = null;
            for(int j = 0; j < sourceandtargets.getLength(); j++){

                Node st = sourceandtargets.item(j);
                if(st.getNodeName().equals("SourceName")){
                    me = new MultiExample(st.getTextContent().toLowerCase(), new ArrayList<String>());
                }else if(st.getNodeName().equals("TargetName")){
                    if(me != null) {
                        me.addTransliteratedWord(st.getTextContent());
                    }
                }
            }
            examples.add(me);
        }

        return examples;
    }

    public static void romanization() throws FileNotFoundException {
        List<String> lines = LineIO.read("/shared/corpora/transliteration/wikidata/wikidata.Russian.fixed");

        String id = "Any-Arabic; NFD; [^\\p{Alnum}] Remove";
        //id = "Any-Latin; NFD";
        Transliterator t = Transliterator.getInstance(id);

        int jj = 0;

        List<Example> examples = new ArrayList<>();

        for(String line : lines){

            if(line.contains("#")){
                continue;
            }

            jj++;
            String[] parts = line.split("\t");

            if(parts.length < 2){
                continue;
            }

            // In wikipedia data, the foreign name comes first, English second.
            String foreign = parts[0].toLowerCase();
            String english = parts[1].toLowerCase();
            String[] ftoks = foreign.split(" ");
            String[] etoks = english.split(" ");

            if(ftoks.length != etoks.length){
                logger.error("Mismatching length of tokens: " + english);
                continue;
            }

            int numtoks = ftoks.length;

            for(int i = 0; i < numtoks; i++){
                String ftrans = t.transform(ftoks[i]);
                ftoks[i] = ftrans;

                int mindist = Integer.MAX_VALUE;
                String bestmatch = null;

                for(int j = 0; j < numtoks; j++){
                    int d = LevensteinDistance.getLevensteinDistance(ftrans, etoks[j]);
                    if(d < mindist){
                        // match etoks[j] with ftrans
                        bestmatch = etoks[j];
                        mindist = d;

                        // then take etoks[j] out of the running
                    }
                }

                //System.out.print(ftrans + " : " + bestmatch + ", ");
                examples.add(new Example(bestmatch, ftrans));

            }

            if(jj%1000 == 0){
                System.out.println(jj);
            }

        }

        System.out.println(examples.size());

        Enumeration<String> tids = t.getAvailableIDs();
        while(tids.hasMoreElements()){
            String e = tids.nextElement();
            //System.out.println(e);
        }
    }

    public static void getSize(String langname) throws FileNotFoundException {
        String wikidata = "/shared/corpora/transliteration/wikidata/wikidata.";
        List<Example> e = readWikiData(wikidata + langname);

    }


    public static void main(String[] args) throws Exception {
        //romanization();

        String[] arabic_names = {"Urdu", "Arabic", "Egyptian_Arabic", "Mazandarani", "Pashto", "Persian", "Western_Punjabi"};
        String[] devanagari_names = {"Newar", "Hindi", "Marathi", "Nepali", "Sanskrit"};
        String[] cyrillic_names = {"Chuvash", "Bashkir", "Bulgarian", "Chechen", "Kirghiz", "Macedonian", "Russian", "Ukrainian"};

        //for(String name : arabic_names){
            //System.out.println(name + " : " + WAVE("models/probs-"+name+"-Urdu.txt"));
            //getSize(name);
        //}

        String lang= "Arabic";
        String wikidata = "Data/wikidata." + lang;

        List<String> allnames = LineIO.read("/Users/stephen/Dropbox/papers/NAACL2016/data/all-names2.txt");

        List<Example> training = readWikiData(wikidata);

        training = training.subList(0, 2000);

        SPModel m = new SPModel(training);
        m.Train(5);

        TopList<Double, String> res = m.Generate("stephen");
        System.out.println(res);

        List<String> outlines = new ArrayList<>();

        int i = 0;
        for(String nameAndLabel : allnames){
            if(i%100 == 0){
                System.out.println(i);
            }
            i++;

            String[] s = nameAndLabel.split("\t");
            String name = s[0];
            String label = s[1];

            String[] sname = name.split(" ");

            String line = "";
            for(String tok : sname){
                res = m.Generate(tok.toLowerCase());
                if(res.size() > 0) {
                    String topcand = res.getFirst().getSecond();
                    line += topcand + " ";
                }else{
                }
            }

            if(line.trim().length() > 0) {
                outlines.add(line.trim() + "\t" + label);
            }
        }

        LineIO.write("/Users/stephen/Dropbox/papers/NAACL2016/data/all-names-"+ lang +"2.txt", outlines);


//        Transliterator t = Transliterator.getInstance("Any-am_FONIPA");
//
//        String result = t.transform("Stephen");
//        System.out.println(result);
//
//        Enumeration<String> tids = t.getAvailableIDs();
//
//        while(tids.hasMoreElements()){
//            String e = tids.nextElement();
//            System.out.println(e);
//        }

    }

}
