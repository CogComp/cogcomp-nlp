package edu.illinois.cs.cogcomp.modelbuilder;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.transliteration.Example;
import edu.illinois.cs.cogcomp.transliteration.SPModel;
import edu.illinois.cs.cogcomp.utils.TopList;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

class Program {

    static String dataPath = "Data/hebrewEnglishAlignment/";

    public static void main(String[] args) throws IOException {


        String trainfile = dataPath + "he_train_pairs.txt";
        String testfile = dataPath + "he_test_pairs.txt";

        BuildModels(trainfile, testfile);
    }

    static void BuildModels(String trainingfile, String testingfile) throws IOException {


        List<String> lines = LineIO.read(trainingfile);
        System.out.println(trainingfile + " = " + lines.size());
        List<Example> training = new ArrayList<>();
        for(String line : lines)
        {
            String[] parts = line.split("\t");
            if (parts[0].length() > 15 || parts[1].length() > 15) continue; //drop super-words
            training.add(new Example(Example.NormalizeHebrew(parts[0]), Example.NormalizeHebrew(parts[1])));
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

        //List<Example> training = examples.subList(0, (int) (examples.size() * 0.8));
        //List<Example> testing = examples.subList((int) (examples.size() * 0.8), (int) (examples.size() - examples.size() * 0.8));

        Train(training, testing);

        //FileStream ms1 = File.Create(modelPath + "en" + filename + ".dat");
        //formatter.Serialize(ms1, model);

        //FileStream ms2 = File.Create(modelPath + filename.Insert(2,"en") + ".dat");
        //formatter.Serialize(ms2, model2);


        System.out.println("Done");
    }

    public static void Reverse(List<Example> examples) {
        for (int i = 0; i < examples.size(); i++) {
            examples.set(i, examples.get(i).Reverse());
        }

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

    public static void TestDiscovery(SPModel model, List<Example> testing) {
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

        System.out.println("MRR=" + correctmrr / (double)testing.size());
        System.out.println("ACC=" + correctacc / (double)testing.size());
    }

    public static void Train(List<Example> training, List<Example> testing) throws IOException {


        SPModel model = new SPModel(training);
        for (int i = 0; i < 1; i++) {
            int emiterations = 3;
            model.Train(emiterations);
            TestDiscovery(model, testing);

        }
    }
}

