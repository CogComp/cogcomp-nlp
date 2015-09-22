using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using SPTransliteration;
using System.IO;
using System.Runtime.Serialization.Formatters.Binary;
using Pasternack.Utility;
using Pasternack.Collections.Generic;

namespace ModelBuilder
{
    class Program
    {
        const string modelPath = @"C:\Users\jpaster2\Desktop\WikiTransliteration\Models\";
        const string dataPath = @"C:\Users\jpaster2\Desktop\WikiTransliteration\Data\";
        static void Main(string[] args)
        {
            HeTest();
            //BuildModels(int.Parse(args[0]), int.Parse(args[1]));
        }

        static void HeTest()
        {
            string evalFile = @"C:\Users\jpaster2\Desktop\WikiTransliteration\eval\heEval.txt";
            
            string[] lines = File.ReadAllLines(evalFile);
            List<Example> examples = new List<Example>();
            foreach (string line in lines)
            {
                string[] parts = line.Split('\t');
                examples.Add(new Example(Example.NormalizeHebrew( parts[0].Trim().ToLower() ), Example.NormalizeHebrew( parts[1].Trim().ToLower()) ));
            }

            foreach (string modelFile in Directory.GetFiles(modelPath))
            {
                if (Path.GetFileName(modelFile).StartsWith("enhe"))
                {
                    FileStream s = File.OpenRead(modelFile);
                    SPModel model = new SPModel(s);
                    s.Close();

                    double mrr = Test(model, examples);
                    Console.WriteLine(modelFile);
                    Console.WriteLine(mrr);
                    Console.WriteLine();
                }
            }
            Console.ReadLine();
        }

        static void BuildModels(int id, int count)
        {            
            int counter = 0;
            foreach (string file in Directory.GetFiles(dataPath))
            {
                if (counter++ % count != id) continue;

                string filename = Path.GetFileNameWithoutExtension(file);
                string[] lines = File.ReadAllLines(file);                
                Console.WriteLine(file + " = " + lines.Length);
                List<Example> examples = new List<Example>();
                foreach (string line in lines)
                {                    
                    string[] parts = line.Split('\t');
                    if (parts[0].Length > 15 || parts[1].Length > 15) continue; //drop super-words
                    examples.Add(new Example(Example.NormalizeHebrew( parts[0] ), Example.NormalizeHebrew( parts[1] )));
                }

                Console.WriteLine("Short examples: " + examples.Count);

                Pasternack.Collections.Generic.Lists.RandomlyPermute<Example>(examples, 2054);

                List<Example> training = examples.GetRange(0, (int)( examples.Count * 0.8));
                List<Example> testing = examples.GetRange( (int)(examples.Count * 0.8), (int)(examples.Count - examples.Count * 0.8));

                Train(training, testing, false,filename);
                
                //FileStream ms1 = File.Create(modelPath + "en" + filename + ".dat");
                //formatter.Serialize(ms1, model);                

                Train(training, testing, true,filename);
                
                //FileStream ms2 = File.Create(modelPath + filename.Insert(2,"en") + ".dat");
                //formatter.Serialize(ms2, model2);
            }

            Console.WriteLine("Done");
            Console.ReadLine();
        }

        public static void Reverse(List<Example> examples)
        {
            for (int i = 0; i < examples.Count; i++)
                examples[i] = examples[i].Reverse;

        }

        public static double Test(SPModel model, List<Example> testing)
        {
            double correct = 0;
            foreach (Example example in testing)
            {
                int index = (model.Generate(example.sourceWord).IndexOfValue(example.transliteratedWord));
                if (index >= 0)
                    correct += 1 / (index + 1);
            }

            return correct / testing.Count; //return MRR
        }

        public static void Train(List<Example> training, List<Example> testing, bool reversed, string filename)
        {
            BinaryFormatter formatter = new BinaryFormatter();
            if (reversed)
            {
                Reverse(training);
                Reverse(testing);
            }

            Console.WriteLine("Reversed: " + reversed);

            SPModel model = new SPModel(training);
            for (int i = 0; i < 20; i++)
            {                
                model.Train(1);
                double mrr = Test(model, testing);
                Console.WriteLine("MRR #" +i+ " == " + mrr);

                FileStream ms2 = File.Create(modelPath + (reversed ? filename.Insert(2,"en") : "en"+filename) + "-" + i + "-" + mrr + ".dat");
                //formatter.Serialize(ms2, model);
                model.WriteToStream(ms2);
                ms2.Close();
            }
        }
    }
}
