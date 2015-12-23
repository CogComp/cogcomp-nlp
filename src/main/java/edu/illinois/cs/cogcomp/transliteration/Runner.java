package edu.illinois.cs.cogcomp.transliteration;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.utils.TopList;
import net.sourceforge.pinyin4j.PinyinHelper;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

class Runner {

    static String dataPath = "Data/hebrewEnglishAlignment/";
    static String wikidata = "/shared/corpora/transliteration/wikidata/";
    static String NEWS = "/shared/corpora/transliteration/NEWS2015/";
    static String tl = "/shared/corpora/transliteration/";

    private static Logger logger = LoggerFactory.getLogger(Runner.class);

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {

//        String chinese = "艾蓮娜";
//        for(char c : chinese.toCharArray()){
//            // CHINESE
//            String[] res = PinyinHelper.toHanyuPinyinStringArray(c);
//            for(String s : res) {
//                System.out.println(s);
//            }
//        }

        String trainfile = dataPath + "he_train_pairs.txt";
        String testfile = dataPath + "he_test_pairs.txt";
//
        //String trainfile = "Data/chinese-train.txt";
        //String testfile = "Data/chinese-test.txt";

//        String trainfile = wikidata + "wikidata.Russian.fixed.train.500";
//        String testfile = wikidata + "wikidata.Russian.fixed.test.500";

//        String trainfile = wikidata + "wikidata.Korean.train.2000";
//        String testfile = wikidata + "wikidata.Korean.test.500";

        //String trainfile = wikidata + "wikidata.Persian.train.500";
        //String testfile = wikidata + "wikidata.Persian.test.500";

//        String trainfile = wikidata + "wikidata.Arabic.train.500";
//        String testfile = wikidata + "wikidata.Arabic.test.500";

//        String trainfile = wikidata + "wikidata.Japanese.train.500";
//        String testfile = wikidata + "wikidata.Japanese.test.500";
//
//        String trainfile = wikidata + "wikidata.Russian.fixed.train.500";
//        String testfile = wikidata + "wikidata.Ukrainian.train.500";

//        String trainfile = wikidata + "wikidata.Polish.train.500";
//        String testfile = wikidata + "wikidata.Polish.test.500";

        //String trainfile = wikidata + "wikidata.Armenian.train.4000";
        //String testfile = wikidata + "wikidata.Armenian.test.500";

        //String trainfile = tl + "hebrew/wikidata.Hebrew.train.500";
        //String testfile = tl + "hebrew/wikidata.Hebrew.test.500";

        //String trainfile = tl + "chinese/zhExamples.train.500";
        //String testfile = tl + "chinese/zhExamples.test.500";

//        String trainfile = NEWS + "NEWS2015_MSRI/NEWS15_train_EnHi_11946.xml";
//        String testfile = NEWS + "NEWS2015_MSRI/NEWS15_dev_EnHi_997.xml";

        //String trainfile = NEWS + "NEWS2015_MSRI/NEWS15_train_EnHe_9501.xml";
        //String testfile = NEWS + "NEWS2015_MSRI/NEWS15_dev_EnHe_1000.xml";

        String method = "wikidata";

        if(method == "interactive"){
            interactive();
        }else if(method == "wikidata") {
            List<Example> training = readWikiData(trainfile);
            List<Example> testing = readWikiData(testfile);

            logger.warn("Not shuffling train/test data!");
            //java.util.Collections.shuffle(training);
            //java.util.Collections.shuffle(testing);

            TrainAndTest(training, testing);
        }else if(method == "NEWS"){

            List<MultiExample> trainingMulti = readNEWSData(trainfile);
            // convert the MultiExamples into single examples.
            // FIXME: is this the right thing to do?? This way, a given word is allowed to take multiple forms in training.
            List<Example> training = new ArrayList<>();
            for(MultiExample me : trainingMulti){
                for(Example e : me.toExampleList()){
                    String[] tls = e.getTransliteratedWord().split(" ");
                    String[] ss = e.sourceWord.split(" ");

                    if(tls.length != ss.length){
                        System.err.println("Mismatched length: " + e.sourceWord);
                        continue;
                    }

                    for(int i = 0; i < tls.length; i++){
                        training.add(new Example(ss[i], tls[i]));
                    }
                }
            }

            List<MultiExample> testing = readNEWSData(testfile);

            System.out.println("Training examples: " + training.size());
            System.out.println("Testing examples: " + testing.size());

            TrainAndTestNEWS(training, testing);
        }


    }

    private static List<MultiExample> readNEWSData(String fname) throws ParserConfigurationException, IOException, SAXException {
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

    static void interactive() throws IOException {
        SPModel model = new SPModel("probs-arabic.txt");

        //List<String> arabicStrings = Program.getForeignWords(wikidata + "wikidata.Armenian");
        //model.SetLanguageModel(arabicStrings);

        Scanner scanner = new Scanner(System.in);

        while(true){
            System.out.print("Enter something:");
            String name = scanner.nextLine().toLowerCase();

            if(name.equals("exit")){
                break;
            }

            System.out.println(name);

            TopList<Double, String> cands = model.Generate(name);
            Iterator<Pair<Double,String>> ci = cands.iterator();

            int lim = Math.min(5, cands.size());

            if(lim == 0){
                System.out.println("No candidates for this...");
            }else {
                for (int i = 0; i < lim; i++) {
                    Pair<Double, String> p = ci.next();
                    System.out.println(p.getFirst() + ": " + p.getSecond());
                }
            }
        }
    }


    /**
     * This reads data in the format created by the wikipedia-api project, commonly named wikidata.Language
     * @param file name of file
     * @return list of examples
     * @throws FileNotFoundException
     */
    static List<Example> readWikiData(String file) throws FileNotFoundException {
        List<Example> examples = new ArrayList<>();
        List<String> lines = LineIO.read(file);
        System.out.println(file + " = " + lines.size());

        for(String line : lines)
        {
            if(line.contains("#")){
                continue;
            }

            String[] parts = line.split("\t");

            if(parts.length < 2){
                continue;
            }

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
                examples.add(new Example(Example.NormalizeHebrew(etoks[i]), Example.NormalizeHebrew(ftoks[i])));
            }
        }
        return examples;
    }

    public static Pair<Double,Double> TestGenerate(SPModel model, List<MultiExample> testing) {
        double correctmrr = 0;
        double correctacc = 0;
        List<String> outlines = new ArrayList<>();

        for (MultiExample example : testing) {
            outlines.add("SourceWord: " + example.sourceWord + "");
            for(String t : example.getTransliteratedWords()){
                outlines.add("TransliteratedWords: " + t);
            }

            TopList<Double,String> prediction = model.Generate(example.sourceWord);

            for(Pair<Double, String> cand : prediction){
                if(example.getTransliteratedWords().contains(cand.getSecond())){
                    outlines.add("**" + cand.getSecond() + ", " + cand.getFirst() + "**");
                }else{
                    outlines.add("" + cand.getSecond() + ", " + cand.getFirst() + "");
                }
            }
            outlines.add("\n");

            int bestindex = -1;

            for(String target : example.getTransliteratedWords()){
                int index = prediction.indexOf(target);
                if(bestindex == -1 || index < bestindex){
                    bestindex = index;
                }
            }

            if (bestindex >= 0) {
                correctmrr += 1.0 / (bestindex + 1);
                if(bestindex == 0){
                    correctacc += 1.0;
                }
            }
        }

        try {
            LineIO.write("out-gen.txt", outlines);
        } catch (IOException e) {
            e.printStackTrace();
        }

        double mrr = correctmrr / (double)testing.size();
        double acc = correctacc / (double)testing.size();
        System.out.println("MRR=" + mrr);
        System.out.println("ACC=" + acc);

        return new Pair<>(mrr, acc);
    }

    public static Pair<Double,Double> TestDiscovery(SPModel model, List<Example> testing) {
        double correctmrr = 0;
        double correctacc = 0;

        List<String> possibilities = new ArrayList<>();
        for(Example e : testing){
            possibilities.add(e.getTransliteratedWord());
        }

        List<String> outlines = new ArrayList<>();

        for (Example example : testing) {

            int topK = 30;
            TopList<Double, String> ll = new TopList<>(topK);
            for(String target : possibilities){
                double prob = model.Probability(example.sourceWord, target);
                ll.add(prob, target);
            }


            outlines.add(example.sourceWord);
            for(Pair<Double, String> p : ll){
                String s = p.getSecond();

                if(s.equals(example.getTransliteratedWord())){
                    s = "**" + s + "**";
                }

                outlines.add(s);
            }
            outlines.add("");

            int index = ll.indexOf(example.getTransliteratedWord());
            if (index >= 0) {
                correctmrr += 1.0 / (index + 1);
                if(index == 0){
                    correctacc += 1.0;
                }
            }
        }

        try {
            LineIO.write("out.txt", outlines);
        } catch (IOException e) {
            e.printStackTrace();
        }

        double mrr = correctmrr / (double)testing.size();
        double acc = correctacc / (double)testing.size();
        System.out.println("MRR=" + mrr);
        System.out.println("ACC=" + acc);


        return new Pair<>(mrr, acc);
    }

    public static void TrainAndTest(List<Example> training, List<Example> testing) throws IOException {

        // params
        int emiterations = 5;
        boolean rom = false; // use romanization or not.
        int min = training.size();

        System.out.println("Actual Training: " + min);

        double avgmrr= 0;
        double avgacc = 0;
        int num = 1;

        for (int i = 0; i < num; i++) {

            //java.util.Collections.shuffle(training);
            //java.util.Collections.shuffle(testing);

            // this allows us to control the size of the training set (optionally smaller)
            List<Example> training2 = new ArrayList<>();
            for(Example e : training){
                if(training2.size() == min){
                    break;
                }
                training2.add(e);
            }

            SPModel model = new SPModel(training2);

            List<String> langstrings = Program.getForeignWords(training2);
            model.SetLanguageModel(langstrings);

            model.Train(emiterations,rom, testing);

            Pair<Double, Double> p = TestDiscovery(model, testing);
            double mrr = p.getFirst();
            double acc = p.getSecond();
            avgmrr += mrr;
            avgacc += acc;
            model.WriteProbs("probs.txt");
        }

        System.out.println("=============");
        System.out.println("AVGMRR=" + avgmrr / num);
        System.out.println("AVGACC=" + avgacc / num);
    }


    public static void TrainAndTestNEWS(List<Example> training, List<MultiExample> testing) throws IOException {

        // params
        int emiterations = 5;
        int min = training.size();

        System.out.println("Actual Training: " + min);

        double avgmrr= 0;
        double avgacc = 0;
        int num = 1;

        for (int i = 0; i < num; i++) {

            java.util.Collections.shuffle(training);
            java.util.Collections.shuffle(testing);

            // this allows us to control the size of the training set (optionally smaller)
            List<Example> training2 = new ArrayList<>();
            for(Example e : training){
                if(training2.size() == min){
                    break;
                }
                training2.add(e);
            }

            SPModel model = new SPModel(training2);
            List<String> langstrings = Program.getForeignWords(training2);
            model.SetLanguageModel(langstrings);
            model.setMaxCandidates(20);
            model.setNgramSize(2);

            logger.info("Training with " + emiterations + " iterations.");
            model.Train(emiterations);

            logger.info("Testing.");
            Pair<Double, Double> p = TestGenerate(model, testing);
            double mrr = p.getFirst();
            double acc = p.getSecond();
            avgmrr += mrr;
            avgacc += acc;
            model.WriteProbs("probs.txt", 0.1);
        }

        System.out.println("=============");
        System.out.println("AVGMRR=" + avgmrr / num);
        System.out.println("AVGACC=" + avgacc / num);
    }

}

