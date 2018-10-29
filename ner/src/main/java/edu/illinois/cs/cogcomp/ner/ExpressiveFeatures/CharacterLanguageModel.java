package edu.illinois.cs.cogcomp.ner.ExpressiveFeatures;

import edu.illinois.cs.cogcomp.ner.IO.InFile;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.core.utilities.StringUtils;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;
import edu.illinois.cs.cogcomp.ner.LbjTagger.*;
import gnu.trove.map.hash.THashMap;

import java.io.InputStream;
import java.io.FileInputStream;


import javax.annotation.Resource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class CharacterLanguageModel {

    private HashMap<String, HashMap<String, Double>> counts;
    private int order;
    private String pad = "_";
    private static THashMap<String, CharacterLanguageModel> charlms = new THashMap<>();

    public CharacterLanguageModel(){
        // parameterized how? order of ngrams?
        // what kind of backoff?
        // what kind of interpolation?
        // let's just start with none.

        // counts maps: history -> { word: count, word : count, etc }
        counts = new HashMap<>();
        order = 4;
    }

    public static void addLM(String key, CharacterLanguageModel clm) {
        charlms.put(key, clm);
    }

    public static CharacterLanguageModel getLM(String key) {
        return charlms.get(key);
    }

    /**
     * Actually returns the log perplexity.
     * @param seq
     * @return
     */
    public double perplexity(List<String> seq){
        // get perplexity wrt counts

        List<String> sequence = new ArrayList<>(seq);
        for(int i = 0; i < order; i++){
            sequence.add(0, pad);
        }

        double logppl = 0;

        for(int j = order; j < sequence.size(); j++){
            // simple stupid backoff.
            double prob = 0.001;
            // history and word
            String word = sequence.get(j);
            String history = StringUtils.join("", sequence.subList(j-order, j));

            HashMap<String, Double> hist_counts = counts.getOrDefault(history, null);
            if(hist_counts != null){
                prob = hist_counts.getOrDefault(word, prob);
            }

            logppl += Math.log(1. / prob);
        }

        logppl /= sequence.size();

        return Math.exp(logppl);

    }

    public static void trainEntityNotEntity(Data trainData, Data testData) throws IOException {

        List<List<String>> entities = new ArrayList<>();
        List<List<String>> nonentities = new ArrayList<>();

        for(NERDocument doc : trainData.documents){
            for(LinkedVector sentence : doc.sentences){
                for(int i = 0; i < sentence.size(); i++) {
                    NEWord word = (NEWord) sentence.get(i);
                    if(word.neLabel.equals("O")){
                        nonentities.add(string2list(word.form));
                    }else {
                        entities.add(string2list(word.form));
                    }
                }
            }
        }

        CharacterLanguageModel eclm = new CharacterLanguageModel();
        eclm.train(entities);

        CharacterLanguageModel neclm = new CharacterLanguageModel();
        neclm.train(nonentities);

        double correct = 0;
        double total = 0;
        List<String> outpreds = new ArrayList<>();
        for(NERDocument doc : testData.documents){
            for(LinkedVector sentence : doc.sentences){
                for(int i = 0; i < sentence.size(); i++) {
                    NEWord word = (NEWord) sentence.get(i);
                    String label = word.neLabel.equals("O")? "O" : "B-ENT";
                    double eppl = eclm.perplexity(string2list(word.form));
                    double neppl = neclm.perplexity(string2list(word.form));

                    String pred;

                    if(word.form.length() < 3){
                        pred = "O";
                    }else if(eppl < neppl){
                        pred = "B-ENT";
                    }else{
                        pred = "O";
                    }

                    if (pred.equals(label)){
                        //System.out.println(word.form + ": correct");
                        correct += 1;
                    }else{
                        System.out.println(word.form + ": WRONG***");
                    }
                    total +=1;

                    outpreds.add(word.form + " " + label + " " + pred);
                }
                outpreds.add("");
            }
        }

        System.out.println("Accuracy: " + correct / total);

        LineIO.write("pred.txt", outpreds);
        System.out.println("Wrote to pred.txt. Now run $ conlleval pred.txt to get F1 scores.");

    }

    public void train(List<List<String>> sequences){

        for(List<String> sequence : sequences){
            for(int i = 0; i < order; i++){
                sequence.add(0, pad);
            }

            for(int j = order; j < sequence.size(); j++){
                // history and word
                String word = sequence.get(j);
                String history = StringUtils.join("", sequence.subList(j-order, j));

                HashMap<String, Double> hist_counts = counts.getOrDefault(history, new HashMap<>());
                double cnt = hist_counts.getOrDefault(word, 0.0);
                hist_counts.put(word, cnt + 1);
                counts.put(history, hist_counts);
            }
        }

        // normalize counts, so everything is a probability?
        // potentially also do backoff here.
        // now these are probabilities.
        for(String hist : counts.keySet()){
            HashMap<String, Double> hist_counts = counts.get(hist);
            double total = hist_counts.values().stream().mapToDouble(i -> i.doubleValue()).sum();
            for(String w : hist_counts.keySet()){
                double cnt = hist_counts.get(w);
                hist_counts.put(w, cnt / total);
            }
        }
    }

    public static List<String> string2list(String s){
        List<String> chars = new ArrayList<>();
        for(char c : s.toCharArray()){
            chars.add(c + "");
        }
        return chars;
    }

    // this test is for a large file at :
    // /shared/corpora/ner/clm/wikiEntity_train.out
    public static void test2() throws Exception {
        List<String> lines = LineIO.read("/shared/corpora/ner/clm/wikiEntity_train.out");
        List<List<String>> seqs = new ArrayList<>();
        for(String line : lines){
            String[] chars = line.trim().split(" ");
            ArrayList<String> seq = new ArrayList<String>(Arrays.asList(chars));
            seqs.add(seq);
        }

        CharacterLanguageModel clm = new CharacterLanguageModel();
        System.out.println(seqs.size());
        clm.train(seqs);


        System.out.println(clm.perplexity(Arrays.asList("H o e k s t e n b e r g e r".split(" "))));
        System.out.println(clm.perplexity(Arrays.asList("a b s t r a c t u a l l y".split(" "))));
    }
    
    public static void test() throws FileNotFoundException {
        String dir = "/home/mayhew/data/pytorch-example/data/names/";
        File names = new File(dir);
        String[] fnames = names.list();
        HashMap<String, CharacterLanguageModel> name2clm = new HashMap<>();

        Random rand = new Random(1234567);

        List<Pair<String, String>> testexamples = new ArrayList<>();

        for(String fname : fnames){
            System.out.println(fname);
            List<String> lines = LineIO.read(dir + fname);

            Collections.shuffle(lines, rand);

            int splitpoint = (int) Math.round(lines.size()*0.8);
            List<String> lines_train = lines.subList(0, splitpoint);
            List<String> lines_test = lines.subList(splitpoint, lines.size());

            List<List<String>> seqs = new ArrayList<>();
            for(String name : lines_train){
                List<String> chars = string2list(name);
                seqs.add(chars);
            }

            CharacterLanguageModel clm = new CharacterLanguageModel();
            clm.train(seqs);

            name2clm.put(fname, clm);

            for(String line : lines_test){
                testexamples.add(new Pair(line, fname));
            }
        }

        // probably not strictly necessary.
        Collections.shuffle(testexamples,rand);

        float correct = 0;
        for(Pair<String, String> ex : testexamples){
            String word = ex.getFirst();
            String label = ex.getSecond();

            List<String> chars = string2list(word);

            double best = 1000000000;
            String pred = null;
            for(String fname : name2clm.keySet()) {
                CharacterLanguageModel clm = name2clm.get(fname);
                double ppl = clm.perplexity(chars);
                if(pred == null || ppl < best){
                    best = ppl;
                    pred = fname;
                }
            }

            if(pred.equals(label)){
                correct += 1;
            }
        }

        System.out.println("Accuracy: " + correct / testexamples.size());
        System.out.println("Total number: " + testexamples.size());


    }


    public static void test(CharacterLanguageModel eclm, CharacterLanguageModel neclm, Data testData) throws IOException {

        double correct = 0;
        double total = 0;
        List<String> outpreds = new ArrayList<>();
        for(NERDocument doc : testData.documents){
            for(LinkedVector sentence : doc.sentences){
                for(int i = 0; i < sentence.size(); i++) {
                    NEWord word = (NEWord) sentence.get(i);
                    String label = word.neLabel.equals("O")? "O" : "B-ENT";
                    double eppl = eclm.perplexity(string2list(word.form));
                    double neppl = neclm.perplexity(string2list(word.form));

                    String pred;

                    if(word.form.length() < 3){
                        pred = "O";
                    }else if(eppl < neppl){
                        pred = "B-ENT";
                    }else{
                        pred = "O";
                    }

                    if (pred.equals(label)){
                        //System.out.println(word.form + ": correct");
                        correct += 1;
                    }else{
                        System.out.println(word.form + ": WRONG***");
                    }
                    total +=1;

                    outpreds.add(word.form + " " + label + " " + pred);
                }
                outpreds.add("");
            }
        }

        System.out.println("Accuracy: " + correct / total);

        LineIO.write("pred.txt", outpreds);
        System.out.println("Wrote to pred.txt. Now run $ conlleval pred.txt to get F1 scores.");


    }



    public static List<List<String>> readList(String path) {

        List<List<String>> seqs = new ArrayList<>();
        try {
            List<String> lines = LineIO.read("/shared/corpora/ner/clm/wikiEntity_train.out");
            for(String line : lines){
                String[] chars = line.trim().split(" ");
                ArrayList<String> seq = new ArrayList<String>(Arrays.asList(chars));
                seqs.add(seq);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return seqs;
    }


    public static void main(String[] args) throws Exception {
        // this trains models, and provides perplexities.
//        test2();

        ParametersForLbjCode params = Parameters.readConfigAndLoadExternalData("config/ner.properties", false);

        String trainpath= "/shared/corpora/ner/conll2003/eng-files/Train-json/";
        String testpath = "/shared/corpora/ner/conll2003/eng-files/Test-json/";

//        String trainpath= "/shared/corpora/ner/lorelei-swm-new/ben/Train/";
//        String testpath = "/shared/corpora/ner/lorelei-swm-new/ben/Test/";

        System.out.println("Reading List");
        String wiki_ent_file = "/shared/corpora/ner/clm/wikiEntity_train.out";
        String wiki_nonent_file = "/shared/corpora/ner/clm/wikiNotEntity_train.out";

//        List<List<String>> wiki_ent = CharacterLanguageModel.readList(wiki_ent_file);
//        List<List<String>> wiki_non_ent = CharacterLanguageModel.readList(wiki_nonent_file);

        System.out.println("train entity clm");
        CharacterLanguageModel eclm = new CharacterLanguageModel();
        eclm.train(CharacterLanguageModel.readList(wiki_ent_file));

        System.out.println("train non entity clm");
        CharacterLanguageModel neclm = new CharacterLanguageModel();
        neclm.train(CharacterLanguageModel.readList(wiki_nonent_file));

        System.out.println("Testing");
//        Data trainData = new Data(trainpath, trainpath, "-json", new String[] {}, new String[] {}, params);
        Data testData = new Data(testpath, testpath, "-json", new String[] {}, new String[] {}, params);
        CharacterLanguageModel.test(eclm, neclm, testData);


//        trainEntityNotEntity(trainData, testData);

    }


}
