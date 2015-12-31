package edu.illinois.cs.cogcomp.transliteration;

import com.ibm.icu.text.Transliterator;
import edu.illinois.cs.cogcomp.core.algorithms.LevensteinDistance;
import edu.illinois.cs.cogcomp.core.algorithms.ProducerConsumer;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.utils.Dictionaries;
import edu.illinois.cs.cogcomp.utils.SparseDoubleVector;
import edu.illinois.cs.cogcomp.utils.TopList;
import edu.illinois.cs.cogcomp.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

class Runner {

    static String dataPath = "Data/hebrewEnglishAlignment/";
    static String wikidata = "/shared/corpora/transliteration/wikidata/";
    static String wikidataextra = "/shared/corpora/transliteration/wikidata-extra/";
    static String NEWS = "/shared/corpora/transliteration/NEWS2015/";
    static String tl = "/shared/corpora/transliteration/";
    static String irvinedata = tl + "from_anne_irvine/";

    public static final int NUMTRAIN = 500;
    public static final int NUMTEST = 500;

    private static Logger logger = LoggerFactory.getLogger(Runner.class);

    public static void main(String[] args) throws Exception {

        //String trainfile = dataPath + "he_train_pairs.txt";
        //String testfile = dataPath + "he_test_pairs.txt";
//
        //String trainfile = "Data/chinese-train.txt";
        //String testfile = "Data/chinese-test.txt";

        String trainlang = "Urdu";
        String testlang = "Newar";
        String probFile = "nonsenseword"; //String.format("probs-%s.txt", trainlang);
        String trainfile = wikidata + String.format("wikidata.%s", trainlang);
        String testfile = wikidata + String.format("wikidata.%s", testlang);

        ///String trainfile = irvinedata + String.format("irvine-data.%s", trainlang);
        //String testfile = irvinedata + String.format("irvine-data.%s", testlang);


        //String trainfile = tl + "chinese/zhExamples.train.500";
        //String testfile = tl + "chinese/zhExamples.test.500";

        //String trainfile = NEWS + "NEWS2015_MSRI/NEWS15_train_EnHi_11946.xml";
        //String testfile = NEWS + "NEWS2015_MSRI/NEWS15_dev_EnHi_997.xml";

        //String trainfile = NEWS + "NEWS2015_MSRI/NEWS15_train_EnHe_9501.xml";
        //String testfile = NEWS + "NEWS2015_MSRI/NEWS15_dev_EnHe_1000.xml";

        String method = "wikidata";

        if(method == "interactive"){
            interactive();

        }else if(method == "wikidata") {
            TrainAndTest(trainfile, testfile);
        }else if(method == "wikidata-pretrain"){
            LoadAndTest(probFile, testfile);
        }else if(method == "NEWS"){
            TrainAndTestNEWS(trainfile, testfile);
        }else if(method == "CCB") {
            List<Example> data = Utils.readCCBData("en", "ja");
            System.out.println(data.size());
        }else if(method == "compare"){
            compare(trainfile, testfile);
        }else if(method == "test") {
            test();
        }else if(method == "makedata"){
            makedata(trainfile, testfile);
        }else{
            logger.error("Should never get here! Try a new method. It was: " + method);
        }

    }


    // Use this method to get data between languages.
    private static void makedata(String trainfile, String testfile) throws IOException {
        List<Example> training = Utils.readWikiData(trainfile);
        List<Example> testing = Utils.readWikiData(testfile);

        String langA = trainfile.split("\\.")[1];
        String langB = testfile.split("\\.")[1];

        // this creates examples that map from training tgt lang to testing tgt lang
        List<Example> a2b = new ArrayList<>();

        HashMap<String, HashSet<Example>> engToEx = new HashMap<>();
        for(Example e : training){
            String eng = e.sourceWord;

            HashSet<Example> prods;
            if(engToEx.containsKey(eng)){
                prods = engToEx.get(eng);
            }else {
                prods = new HashSet<>();
            }
            prods.add(e);
            engToEx.put(eng, prods);
        }

        logger.debug("Done with reading " + trainfile);

        for(Example e : testing){
            String eng = e.sourceWord;
            if(engToEx.containsKey(eng)){
                HashSet<Example> examples = engToEx.get(eng);

                for(Example e2a : examples) {
                    // eng of e2a is same as eng of e
                    a2b.add(new Example(e2a.getTransliteratedWord(), e.getTransliteratedWord()));
                }

            }
        }

        logger.debug("Done with reading " + testfile);


        HashSet<String> outlines = new HashSet<>();

        for(Example e : a2b){
            // wikidata file ordering is tgt src.
            outlines.add(e.getTransliteratedWord() + "\t" + e.sourceWord);
        }

        List<String> listlines = new ArrayList<>(outlines);
        listlines.add(0, "# " + langA + "\t" + langB + "\n");
        LineIO.write("wikidata." + langA + "-" + langB, listlines);

    }

    static void compare(String trainfile, String testfile) throws FileNotFoundException {
        List<Example> training = Utils.readWikiData(trainfile);
        List<Example> testing = Utils.readWikiData(testfile);

        // get transliteration pairs between these two.
        HashMap<String, String> english2train = new HashMap<>();
        for(Example e : training){
            if(english2train.containsKey(e.sourceWord)){
                // probably not a problem. Because the readWikiData splits the names into first and last,
                // it is likely that we will see the same first name many times. Assume it is the same
                // each time (weak!)
            }
            english2train.put(e.sourceWord,e.getTransliteratedWord());
        }

        int num = 0;
        double sum = 0;
        for(Example e : testing){
            if(english2train.containsKey(e.sourceWord)){
                String train = english2train.get(e.sourceWord);
                // there is only one of these, so it doesn't matter if it is a list.
                List<String> test = e.getTransliteratedWords();

                // get edit distance between these.
                double F1 = Utils.GetFuzzyF1(train, test);
                sum += F1;
                num++;
            }
        }
        System.out.println("Num pairs: " + num);
        System.out.println("Avg F1: " + sum / num);


    }


    /**
     * A function for testing bridge languages. This creates a transitive model.
     * @throws IOException
     */
    static void test() throws IOException {
        // open probs.Nepali
        // open probs.Arabic
        SPModel t = new SPModel("probs-Nepali.txt");
        SPModel a = new SPModel("probs-Western_Punjabi.txt");

        SparseDoubleVector<Production> tprobs = t.getProbs();
        SparseDoubleVector<Production> aprobs = a.getProbs();

        // this creates productions that map from Nepali to arabic.
        SparseDoubleVector<Production> t2a = new SparseDoubleVector<>();

        // this maps from English segment to Nepali segment set. This set should add to 1

        HashMap<String, HashSet<Production>> firstToProd = new HashMap<>();
        for(Production p : tprobs.keySet()){
            String eng = p.getFirst();
            // don't include the tiny ones...
            if(tprobs.get(p) < 0.1){
                continue;
            }
            HashSet<Production> prods;
            if(firstToProd.containsKey(eng)){
                prods = firstToProd.get(eng);
            }else {
                 prods = new HashSet<>();
            }
            prods.add(p);
            firstToProd.put(eng, prods);
        }

        logger.debug("Done with reading nepali...");

        for(Production p : aprobs.keySet()){
            String eng = p.getFirst();
            if(firstToProd.containsKey(eng)){
                HashSet<Production> telugu_prods = firstToProd.get(eng);

                for(Production tel : telugu_prods) {
                    // eng of tel is same as eng of p

                    // maybe should be log probs.
                    double score = tprobs.get(tel) * aprobs.get(p);

                    t2a.put(new Production(tel.getSecond(), p.getSecond()), score);
                }

            }
        }

        logger.debug("Done with reading arabic...");


        double threshold = 0.;
        ArrayList<String> outlines = new ArrayList<>();
        for(Production p : t2a.keySet()){
            if(t2a.get(p) > threshold) {
                outlines.add(p.getFirst() + "\t" + p.getSecond() + "\t" + t2a.get(p));
            }
        }
        LineIO.write("probs-nepali-to-arabic.txt", outlines);

    }


    static void interactive() throws Exception {
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
     * Given a model and set of testing examples, this will get scores using the generation method.
     * @param model this needs to be a trained model.
     * @param testing a set of examples.
     * @return an 3-element double array of scores with elements MRR,ACC,F1
     * @throws Exception
     */
    public static double[] TestGenerate(SPModel model, List<? extends MultiExample> testing, String lang) throws Exception {
        double correctmrr = 0;
        double correctacc = 0;
        double totalf1 = 0;

        List<String> outlines = new ArrayList<>();

        //String id = "Any-Arabic; NFD";
        //Transliterator t = Transliterator.getInstance(id);

        //logger.warn("CREATING A PNJ-URDU MODEL RIGHT HERE.");
        //boolean fix = false; // don't try to fix the data... edit distance is weird in 2 foreign langs.
        //List<Example> training = Utils.readWikiData("wikidata.Western_Punjabi-Urdu", fix);
        //SPModel bashkir2chuvash = new SPModel(training);
        //bashkir2chuvash.Train(5);

        // FIXME: CAREFUL HERE!!!
        //logger.warn("SETTING MAXCANDS TO JUST 5");
        //model.setMaxCandidates(5);

        int i = 0;
        for (MultiExample example : testing) {
            if(i%50 == 0) {
                logger.debug("on example " + i + " out of " + testing.size());
                //logger.debug("USING THE CREATED MODEL TO GET INTO URDU.");
            }
            i++;

            outlines.add("SourceWord: " + example.sourceWord + "");
            for(String tw : example.getTransliteratedWords()){
                outlines.add("TransliteratedWords: " + tw);
            }

            TopList<Double,String> prediction = model.Generate(example.sourceWord);

            // This block is for the second stage in the pipeline.
            //TopList<Double, String> scriptpreds = new TopList<>(prediction.size());
            // transform into arabic
            //for(Pair<Double, String> cand : prediction){
                //scriptpreds.add(cand.getFirst(), t.transform(cand.getSecond()));
            //    TopList<Double,String> chuvashcands = bashkir2chuvash.Generate(cand.getSecond());
            //    if(chuvashcands.size() > 0) {
            //        scriptpreds.add(cand.getFirst(), chuvashcands.getFirst().getSecond());
            //    }
            // }
            //prediction = scriptpreds;

            for(Pair<Double, String> cand : prediction){
                if(example.getTransliteratedWords().contains(cand.getSecond())){
                    outlines.add("**" + cand.getSecond() + ", " + cand.getFirst() + "**");
                }else{
                    outlines.add("" + cand.getSecond() + ", " + cand.getFirst() + "");
                }
            }
            outlines.add("\n");

            int bestindex = -1;

            double F1 = 0;
            if(prediction.size() == 0){
                logger.error("No cands for this word: " + example.sourceWord);
            }else {
                F1 = Utils.GetFuzzyF1(prediction.getFirst().getSecond(), example.getTransliteratedWords());
            }
            totalf1 += F1;

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

        LineIO.write("out-gen-"+ lang +".txt", outlines);

        double mrr = correctmrr / (double)testing.size();
        double acc = correctacc / (double)testing.size();
        double f1 = totalf1 / (double)testing.size();

        double[] res = new double[3];
        res[0] = mrr;
        res[1] = acc;
        res[2] = f1;

        return res;
    }

    public static Pair<Double,Double> TestDiscovery(SPModel model, List<Example> testing) throws IOException {
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

        LineIO.write("out-disc.txt", outlines);

        double mrr = correctmrr / (double)testing.size();
        double acc = correctacc / (double)testing.size();

        return new Pair<>(mrr, acc);
    }

    /**
     * This loads a prob file (having been generated from a previous testing run) and tests
     * on the test file.
     * @param probFile
     * @param testfile
     * @throws Exception
     */
    public static void LoadAndTest(String probFile, String testfile) throws Exception {

        List<Example> testing = Utils.readWikiData(testfile);
        java.util.Collections.shuffle(testing);

        double avgmrr= 0;
        double avgacc = 0;
        double avgf1 = 0;

        SPModel model = new SPModel(probFile);

        logger.info("Testing.");
        double[] res = TestGenerate(model, testing, "unknown");
        double mrr = res[0];
        double acc = res[1];
        double f1 = res[2];
        avgmrr += mrr;
        avgacc += acc;
        avgf1 += f1;
        model.WriteProbs("probs-" + testfile.split("\\.")[1] +".txt");

        System.out.println("=============");
        System.out.println("AVGMRR=" + avgmrr);
        System.out.println("AVGACC=" + avgacc);
        System.out.println("AVGF1 =" + avgf1);
    }


    public static void TrainAndTest(String trainfile, String testfile) throws Exception {

        List<Example> training = Utils.readWikiData(trainfile);
        List<Example> testing = Utils.readWikiData(testfile);

        // params
        int emiterations = 5;
        boolean rom = false; // use romanization or not.


        double avgmrr= 0;
        double avgacc = 0;
        double avgf1 = 0;
        int num = 1;

        for (int i = 0; i < num; i++) {


            java.util.Collections.shuffle(training);
        
            List<Example> subtraining = training.subList(0,NUMTRAIN);
            List<Example> subtesting;

            System.out.println("Orig len of testing: " + testing.size());

            if(trainfile.equals(testfile)){
                subtesting = testing.subList(NUMTRAIN,NUMTRAIN+NUMTEST);
            }else{
                subtesting = testing.subList(0,NUMTEST);
            }

            System.out.println("Actual Training: " + subtraining.size());
            System.out.println("Actual Training: " + subtesting.size());
        
            
            SPModel model = new SPModel(subtraining);
            //model.setUseNPLM(true);
            //model.setNPLMfile("lm/newar/nplm-new.txt");
            //List<String> langstrings = Program.getForeignWords(training);
            //model.SetLanguageModel(langstrings);

            model.Train(emiterations, rom, subtesting);

//            Pair<Double, Double> p = TestDiscovery(model, testing);
//            double mrr = p.getFirst();
//            double acc = p.getSecond();
            logger.info("Testing.");
            String[] pathsplit = trainfile.split("\\.");
            String trainlang = pathsplit[pathsplit.length-1]; // get the last element, filename should be wikidata.Lang

            // This is for testing
            double[] res = TestGenerate(model, subtesting, trainlang);
            double mrr = res[0];
            double acc = res[1];
            double f1 = res[2];
            avgmrr += mrr;
            avgacc += acc;
            avgf1 += f1;
            System.out.println(mrr + "," + acc + "," + f1);
            model.WriteProbs("probs-" + trainlang +".txt");
        }

        System.out.println("=============");
        System.out.println("AVGMRR=" + avgmrr / num);
        System.out.println("AVGACC=" + avgacc / num);
        System.out.println("AVGF1 =" + avgf1 / num);
    }


    public static void TrainAndTestNEWS(String trainfile, String testfile) throws Exception {

        List<MultiExample> trainingMulti = Utils.readNEWSData(trainfile);

        // convert the MultiExamples into single examples.
        List<Example> training = Utils.convertMulti(trainingMulti);

        List<MultiExample> testing = Utils.readNEWSData(testfile);

        System.out.println("Training examples: " + training.size());
        System.out.println("Testing examples: " + testing.size());

        double avgmrr= 0;
        double avgacc = 0;
        double avgf1 = 0;
        int num = 1;

        for (int i = 0; i < num; i++) {

            java.util.Collections.shuffle(training);
            java.util.Collections.shuffle(testing);

            SPModel model = new SPModel(training);
            //List<String> langstrings = Program.getForeignWords(wikidata + "wikidata.Hebrew");
            //List<String> langstrings = Program.getForeignWords(training);
            //List<String> langstrings = LineIO.read("Data/heWords.txt");
            //model.SetLanguageModel(langstrings);
            //model.SetLanguageModel(Utils.readSRILM("lm/lm-he.txt"));
            model.setUseNPLM(true);

            // This is set by the shared task.
            model.setMaxCandidates(50);
            model.setNgramSize(3);

            int emiterations = 3;

            logger.info("Training with " + emiterations + " iterations.");
            model.Train(emiterations);

            logger.info("Testing.");
            double[] res = TestGenerate(model, testing, "unknown");
            double mrr = res[0];
            double acc = res[1];
            double f1 = res[2];
            avgmrr += mrr;
            avgacc += acc;
            avgf1 += f1;
            model.WriteProbs("probs.txt", 0.1);
        }

        System.out.println("=============");
        System.out.println("AVGMRR=" + avgmrr / num);
        System.out.println("AVGACC=" + avgacc / num);
        System.out.println("AVGF1 =" + avgf1 / num);
    }

}

