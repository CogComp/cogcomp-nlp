package edu.illinois.cs.cogcomp.transliteration;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.transliteration.Example;
import edu.illinois.cs.cogcomp.transliteration.SPModel;
import edu.illinois.cs.cogcomp.utils.TopList;
import net.sourceforge.pinyin4j.PinyinHelper;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class Runner {

    static String dataPath = "Data/hebrewEnglishAlignment/";

    public static void main(String[] args) throws IOException {


        String chinese = "艾蓮娜";
        for(char c : chinese.toCharArray()){
            // CHINESE
            String[] res = PinyinHelper.toHanyuPinyinStringArray(c);
            for(String s : res) {
                System.out.println(s);
            }
        }



//        String trainfile = dataPath + "he_train_pairs.txt";
//        String testfile = dataPath + "he_test_pairs.txt";
//
        String trainfile = "Data/chinese-train.txt";
        String testfile = "Data/chinese-test.txt";

        RunTest(trainfile, testfile);
    }

    static void RunTest(String trainingfile, String testingfile) throws IOException {


        List<String> lines = LineIO.read(trainingfile);
        System.out.println(trainingfile + " = " + lines.size());
        List<Example> training = new ArrayList<>();
        Random r = new Random();
        for(String line : lines)
        {
            String[] parts = line.split("\t");
            if (parts[0].length() > 15 || parts[1].length() > 15) continue; //drop super-words
            if(r.nextDouble() < 1.0) {
                training.add(new Example(Example.NormalizeHebrew(parts[0]), Example.NormalizeHebrew(parts[1])));
            }
        }

        lines = LineIO.read(testingfile);
        System.out.println(testingfile + " = " + lines.size());
        List<Example> testing = new ArrayList<>();
        for (String line : lines)
        {
            String[] parts = line.split("\t");
            if (parts[0].length() > 15 || parts[1].length() > 15) continue; //drop super-words
            testing.add(new Example(Example.NormalizeHebrew(parts[0]), Example.NormalizeHebrew(parts[1])));
        }

        System.out.println("Training examples: " + training.size());
        System.out.println("Testing examples: " + testing.size());

        java.util.Collections.shuffle(training);
        java.util.Collections.shuffle(testing);

        Train(training, testing);

        System.out.println("Done");
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

    public static void Train(List<Example> training, List<Example> testing) throws IOException {

        // params
        int emiterations = 15;
        boolean rom = false; // use romanization or not.
        int min = 50;

        System.out.println("Actual Training: " + min);

        double avgmrr= 0;
        double avgacc = 0;
        int num = 3;

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
            model.WriteProbs("probs.txt");
        }

        System.out.println("=============");
        System.out.println("AVGMRR=" + avgmrr / num);
        System.out.println("AVGacc=" + avgacc / num);


    }
}

