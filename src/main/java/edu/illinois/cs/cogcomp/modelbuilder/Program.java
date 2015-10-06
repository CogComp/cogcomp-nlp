package edu.illinois.cs.cogcomp.modelbuilder;

import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.transliteration.Example;
import edu.illinois.cs.cogcomp.transliteration.SPModel;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

class Program {
    static String modelPath = "/path/to/WikiTransliteration/Models/";
    static String dataPath = "/path/to/WikiTransliteration/Data/";

    static void main(String[] args) {
        try {
            HeTest();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //BuildModels(int.Parse(args[0]), int.Parse(args[1]));
    }

    static void HeTest() throws IOException {
        String evalFile = "/path/to/WikiTransliteration/eval/heEval.txt";

        ArrayList<String> lines = LineIO.read(evalFile);

        List<Example> examples = new ArrayList<Example>();
        for (String line : lines) {
            String[] parts = line.split("\t");
            examples.add(new Example(Example.NormalizeHebrew(parts[0].trim().toLowerCase()), Example.NormalizeHebrew(parts[1].trim().toLowerCase())));
        }

        File modelDir = new File(modelPath);
        for (File modelFile : modelDir.listFiles()) {
            if (modelFile.getName().startsWith("enhe")) {
                //FileStream s = File.OpenRead(modelFile);
                DataInputStream s = new DataInputStream(new FileInputStream(modelFile));
                SPModel model = new SPModel(s);
                s.close();

                double mrr = Test(model, examples);
                System.out.println(modelFile);
                System.out.println(mrr);
                System.out.println();
            }
        }
    }

    static void BuildModels(int id, int count) throws IOException {
        int counter = 0;
        File dataDir = new File(dataPath);
        for (File file : dataDir.listFiles()) {
            if (counter++ % count != id) continue;
            String filename = FilenameUtils.getBaseName(file.getName());

            ArrayList<String> lines = LineIO.read(file.getName());

            System.out.println(file + " = " + lines.size());

            List<Example> examples = new ArrayList<>();
            for (String line : lines) {
                String[] parts = line.split("\t");
                if (parts[0].length() > 15 || parts[1].length() > 15)
                    continue; //drop super-words
                examples.add(new Example(Example.NormalizeHebrew(parts[0]), Example.NormalizeHebrew(parts[1])));
            }

            System.out.println("Short examples: " + examples.size());

            java.util.Collections.shuffle(examples);

            List<Example> training = examples.subList(0, (int) (examples.size() * 0.8));
            List<Example> testing = examples.subList((int) (examples.size() * 0.8), (int) (examples.size() - examples.size() * 0.8));

            Train(training, testing, false, filename);

            //FileStream ms1 = File.Create(modelPath + "en" + filename + ".dat");
            //formatter.Serialize(ms1, model);

            Train(training, testing, true, filename);

            //FileStream ms2 = File.Create(modelPath + filename.Insert(2,"en") + ".dat");
            //formatter.Serialize(ms2, model2);
        }

        System.out.println("Done");
    }

    public static void Reverse(List<Example> examples) {
        for (int i = 0; i < examples.size(); i++) {
            examples.set(i, examples.get(i).Reverse());
        }

    }

    public static double Test(SPModel model, List<Example> testing) {
        double correct = 0;
        for (Example example : testing) {
            int index = (model.Generate(example.sourceWord).indexOf(example.transliteratedWord));
            if (index >= 0)
                correct += 1 / (index + 1);
        }

        return correct / testing.size(); //return MRR
    }

    public static void Train(List<Example> training, List<Example> testing, boolean reversed, String filename) throws IOException {
        if (reversed) {
            Reverse(training);
            Reverse(testing);
        }

        System.out.println("Reversed: " + reversed);

        SPModel model = new SPModel(training);
        for (int i = 0; i < 20; i++) {
            model.Train(1);
            double mrr = Test(model, testing);
            System.out.println("MRR #" + i + " == " + mrr);

            String outname = modelPath + (reversed ? "reverse-en" + filename : "en" + filename) + "-" + i + "-" + mrr + ".dat";
            DataOutputStream ms2 = new DataOutputStream(new FileOutputStream(outname));
            //formatter.Serialize(ms2, model);
            model.WriteToStream(ms2);
            ms2.close();
        }
    }
}

