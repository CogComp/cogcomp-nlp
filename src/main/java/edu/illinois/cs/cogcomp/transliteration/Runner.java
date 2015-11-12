package edu.illinois.cs.cogcomp.transliteration;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.utils.TopList;
import net.sourceforge.pinyin4j.PinyinHelper;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

class Runner {

    static String dataPath = "Data/hebrewEnglishAlignment/";
    static String wikidata = "/shared/corpora/transliteration/wikidata/";
    static String tl = "/shared/corpora/transliteration/";

    public static void main(String[] args) throws IOException {

//        String chinese = "艾蓮娜";
//        for(char c : chinese.toCharArray()){
//            // CHINESE
//            String[] res = PinyinHelper.toHanyuPinyinStringArray(c);
//            for(String s : res) {
//                System.out.println(s);
//            }
//        }

//        String trainfile = dataPath + "he_train_pairs.txt";
//        String testfile = dataPath + "he_test_pairs.txt";
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

        String trainfile = tl + "hebrew/wikidata.Hebrew.train.500";
        String testfile = tl + "hebrew/wikidata.Hebrew.test.500";

        //String trainfile = tl + "chinese/zhExamples.train.500";
        //String testfile = tl + "chinese/zhExamples.test.500";

        boolean interactive = true;

        if(interactive){
            interactive();
        }else {
            List<Example> training = readWikiData(trainfile);
            List<Example> testing = readWikiData(testfile);

            System.out.println("Training examples: " + training.size());
            System.out.println("Testing examples: " + testing.size());

            java.util.Collections.shuffle(training);
            java.util.Collections.shuffle(testing);

            TrainAndTest(training, testing);
        }


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
                System.err.println("Mismatching length of tokens: " + english);
                continue;
            }

            int numtoks = ftoks.length;
            for(int i = 0; i < numtoks; i++){
                examples.add(new Example(Example.NormalizeHebrew(etoks[i]), Example.NormalizeHebrew(ftoks[i])));
            }
        }
        return examples;
    }

    public static void TestGenerate(SPModel model, List<Example> testing) {
        double correctmrr = 0;
        double correctacc = 0;
        for (Example example : testing) {
            int index = (model.Generate(example.sourceWord).indexOf(example.transliteratedWord));
            if (index >= 0) {
                correctmrr += 1.0 / (index + 1);
                if(index == 0){
                    correctacc += 1.0;
                }
            }

            System.out.println();
        }
        System.out.println("MRR=" + correctmrr / testing.size());
        System.out.println("ACC=" + correctacc / testing.size());
    }

    public static Pair<Double,Double> TestDiscovery(SPModel model, List<Example> testing) {
        double correctmrr = 0;
        double correctacc = 0;

        List<String> possibilities = new ArrayList<>();
        for(Example e : testing){
            possibilities.add(e.transliteratedWord);
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

                if(s.equals(example.transliteratedWord)){
                    s = "**" + s + "**";
                }

                outlines.add(s);
            }
            outlines.add("");

            int index = ll.indexOf(example.transliteratedWord);
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

            java.util.Collections.shuffle(training);
            //java.util.Collections.shuffle(testing);


            List<Example> training2 = new ArrayList<>();

            for(Example e : training){
                if(training2.size() == min){
                    break;
                }
                training2.add(e);

            }

            SPModel model = new SPModel(training2);

            model.Train(emiterations,rom, testing);
            Pair<Double, Double> p = TestDiscovery(model, testing);
            double mrr = p.getFirst();
            double acc = p.getSecond();
            avgmrr += mrr;
            avgacc += acc;
            model.WriteProbs("probs.txt", 0.0);
        }

        System.out.println("=============");
        System.out.println("AVGMRR=" + avgmrr / num);
        System.out.println("AVGacc=" + avgacc / num);




    }
}

