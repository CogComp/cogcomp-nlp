package edu.illinois.cs.cogcomp.transliteration;


import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.Triple;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.utils.Dictionaries;
import edu.illinois.cs.cogcomp.utils.InternDictionary;
import edu.illinois.cs.cogcomp.utils.SparseDoubleVector;
import edu.illinois.cs.cogcomp.utils.TopList;
import java.io.FileNotFoundException;
import java.text.Normalizer;
import java.util.*;


import net.sourceforge.pinyin4j.PinyinHelper;

class Program {

    static void main(String[] args) throws FileNotFoundException {
        //RussianDiscoveryTest(); return;
        //ChineseDiscoveryTest(); return;
        HebrewDiscoveryTest();


    }

    /*
    static void MakeAliasTable(String wikiFile, String redirectTableFile, String tableFile, String tableKeyValueFile)
    {
        ICSharpCode.SharpZipLib.BZip2.BZip2InputStream bzipped =
            new ICSharpCode.SharpZipLib.BZip2.BZip2InputStream(File.OpenRead(wikiFile));
        WikiXMLReader reader = new WikiXMLReader(bzipped);

        StreamReader redirectReader = new StreamReader(redirectTableFile);
        WikiRedirectTable redirectTable = new WikiRedirectTable(redirectReader);
        redirectReader.Close();

        List<String> disambigs = new ArrayList<>();
        WikiNamespace templateNS = new WikiNamespace("Template",10);
        for (WikiNamespace ns : reader.WikiInfo.Namespaces)
            if (ns.Key == 10)
            {
                templateNS = ns;
                break;
            }

        String disambigMain = redirectTable.Redirect(templateNS.Name + ":Disambig");
        disambigs.add(disambigMain);
        HashMap<String,List<String>> inverted = redirectTable.InvertedTable;
        if (inverted.containsKey(disambigMain))
            for (String template : inverted[disambigMain])
                disambigs.add(template);

        HashMap<String,List<WikiAlias>> result = WikiTransliteration.MakeAliasTable(reader, disambigs);

        StreamDictionary<String,List<WikiAlias>> table = new StreamDictionary<String,List<WikiAlias>>(
            result.Count*4,0.5,tableFile,null,tableKeyValueFile,null,null,null,null,null);


        table.AddDictionary(result);

        table.Close();
    }

    static WikiRedirectTable ReadRedirectTable(String filename)
    {
        StreamReader reader = new StreamReader(filename);
        try
        {
            return new WikiRedirectTable(reader);
        }
        finally
        {
            reader.Close();
        }
    }

    static BetterBufferedStream OpenBBS(String filename, int bufferSize)
    {
        return new BetterBufferedStream(File.Open(filename, FileMode.Open), bufferSize);
    }

    static FileStream OpenFS(String filename, int bufferSize, bool random)
    {
        return new FileStream(filename, FileMode.Open, FileAccess.ReadWrite, FileShare.Read, bufferSize, random ? FileOptions.RandomAccess : FileOptions.None);
    }

    static void MakeTranslationMap(String sourceLanguageCode, String sourceAliasTableFile, String sourceAliasTableKeyValueFile, String sourceRedirectFile, String targetLanguageCode, String targetAliasTableFile, String targetAliasTableKeyValueFile, String targetRedirectFile, String translationMapFile)
    {
        StreamDictionary<String, List<WikiAlias>> sourceAliasTable = new StreamDictionary<String, List<WikiAlias>>(
            5, 0.5, OpenBBS(sourceAliasTableFile, 1000000), null, OpenBBS(sourceAliasTableKeyValueFile, 1000000), null, null, null, null, null);
        StreamDictionary<String, List<WikiAlias>> targetAliasTable = new StreamDictionary<String, List<WikiAlias>>(
            5, 0.5, OpenBBS(targetAliasTableFile, 1000000), null, OpenBBS(targetAliasTableKeyValueFile, 1000000), null, null, null, null, null);

        HashMap<Pair<String, String>, Integer> weights;
        Map<String,String> map = WikiTransliteration.MakeTranslationMap(sourceLanguageCode, sourceAliasTable, ReadRedirectTable(sourceRedirectFile), targetLanguageCode, targetAliasTable, ReadRedirectTable(targetRedirectFile), out weights);

        System.Runtime.Serialization.Formatters.Binary.BinaryFormatter bf = new System.Runtime.Serialization.Formatters.Binary.BinaryFormatter();
        FileStream fs = File.Create(translationMapFile);
        bf.Serialize(fs, map);
        bf.Serialize(fs, weights);
        fs.Close();
    }

    static void MakeTranslationMap2(String sourceLanguageCode, String sourceAliasTableFile, String sourceAliasTableKeyValueFile, String sourceRedirectFile, String targetLanguageCode, String targetAliasTableFile, String targetAliasTableKeyValueFile, String targetRedirectFile, String translationMapFile)
    {
        StreamDictionary<String, List<WikiAlias>> sourceAliasTable = new StreamDictionary<String, List<WikiAlias>>(
            5, 0.5, OpenBBS(sourceAliasTableFile, 1000000), null, OpenBBS(sourceAliasTableKeyValueFile, 1000000), null, null, null, null, null);
        StreamDictionary<String, List<WikiAlias>> targetAliasTable = new StreamDictionary<String, List<WikiAlias>>(
            5, 0.5, OpenBBS(targetAliasTableFile, 1000000), null, OpenBBS(targetAliasTableKeyValueFile, 1000000), null, null, null, null, null);

        HashMap<Pair<String, String>, WordAlignment> weights;
        HashMap<String, bool> personTable;
        Map<String, String> map = WikiTransliteration.MakeTranslationMap2(sourceLanguageCode, sourceAliasTable, ReadRedirectTable(sourceRedirectFile), targetLanguageCode, targetAliasTable, ReadRedirectTable(targetRedirectFile), out weights, null, null, false);

        System.Runtime.Serialization.Formatters.Binary.BinaryFormatter bf = new System.Runtime.Serialization.Formatters.Binary.BinaryFormatter();
        FileStream fs = File.Create(translationMapFile);
        bf.Serialize(fs, map);
        bf.Serialize(fs, weights);
        fs.Close();
    }

    static void MakeTrainingList(String sourceLanguageCode, String sourceAliasTableFile, String sourceAliasTableKeyValueFile, String sourceCategoryGraphFile, String sourcePersondataFile, String targetLanguageCode, String targetAliasTableFile, String targetAliasTableKeyValueFile, String listFile, bool peopleOnly)
    {
        FileStream fs = File.OpenRead(sourceCategoryGraphFile);
        WikiCategoryGraph graph = (peopleOnly ? WikiCategoryGraph.ReadFromStream(fs) : null);
        fs.Close();

        HashMap<String, bool> persondataTitles=null;

        if (peopleOnly)
        {
            persondataTitles = new HashMap<String, bool>();
            for (String line : File.ReadAllLines(sourcePersondataFile))
                persondataTitles[line.ToLower()] = true;
        }

        StreamDictionary<String, List<WikiAlias>> sourceAliasTable = new StreamDictionary<String, List<WikiAlias>>(
            5, 0.5, OpenBBS(sourceAliasTableFile, 1000000), null, OpenBBS(sourceAliasTableKeyValueFile, 1000000), null, null, null, null, null);

        StreamDictionary<String, List<WikiAlias>> targetAliasTable = new StreamDictionary<String, List<WikiAlias>>(
            5, 0.5, OpenBBS(targetAliasTableFile, 1000000), null, OpenBBS(targetAliasTableKeyValueFile, 1000000), null, null, null, null, null);

        HashMap<Pair<String, String>, WordAlignment> weights;

        Map<String, String> map = WikiTransliteration.MakeTranslationMap2(sourceLanguageCode, sourceAliasTable, null, targetLanguageCode, targetAliasTable, null, out weights, graph, persondataTitles, targetLanguageCode == "zh");

        targetAliasTable.Close();
        sourceAliasTable.Close();

        StreamWriter writer = new StreamWriter(listFile, false);
        for (Pair<Pair<String, String>, WordAlignment> pair in weights)
        {
            //if (!personTable.ContainsKey(pair.Key.x)) continue; //not a person--ignore
            //bool person = false;
            //for (WikiAlias alias in sourceAliasTable[pair.Key.x])
            //    if (alias.type == AliasType.Link && alias.alias.StartsWith("category:",StringComparison.OrdinalIgnoreCase) && (alias.alias.Contains("births") || alias.alias.Contains("deaths")))
            //    {
            //        person=true;
            //        break;
            //    }

            //if (!person) continue;

            writer.WriteLine(pair.Key.x + "\t" + pair.Key.y + "\t" + pair.Value.ToString());
        }

        writer.Close();
    }

    static void FilterTrainingListLoose(String sourceListFile, String filteredListFile, bool removePureLatin, double minRatio, double minScore)
    {
        String[] lines = File.ReadAllLines(sourceListFile);

        HashMap<String, String> pairMap = new HashMap<String, String>();
        SparseDoubleVector<Pair<String, String>> scores = new SparseDoubleVector<Pair<String, String>>();

        for (String line : lines)
        {
            String[] parts = line.split("\t");
            pairMap.Add(parts[0], parts[1]);
            WordAlignment wa = new WordAlignment(parts[2]);
            double score = wa.oneToOne * 10 + wa.equalNumber * 5 + wa.unequalNumber;
            scores[new Pair<String, String>(parts[0], parts[1])] = score;
        }

        boolean progress = true;

        StreamWriter writer = new StreamWriter(filteredListFile);
        while (progress)
        {
            List<Pair<String, String>> pairs = new List<Pair<String, String>>();
            List<Double> ratios = new List<Double>();

            for (Pair<String, String> pair : pairMap)
            {
                double ratio = double.PositiveInfinity;
                double pairScore = scores[pair];
                if (pairScore < 15) continue;
                bool goodPair = true;

                for (Pair<String, String> oPair in JoinEnumerable.Join<Pair<String, String>>(pairMap.GetPairsForKey(pair.Key), pairMap.GetPairsForValue(pair.Value)))
                {
                    if (oPair.Key == pair.Key && oPair.Value == pair.Value) continue; //this is the example we're currently looking at
                    double oScore = scores[oPair];
                    ratio = Math.Min(ratio, pairScore / oScore);
                    if (ratio < minRatio) break;
                }

                if (ratio < minRatio) continue; //no good
                pairs.Add(pair);
                ratios.Add(ratio);
            }

            progress = pairs.Count > 0;

            for (Pair<String, String> pair : pairs)
            {
                pairMap.Remove(pair);
                scores.Remove(pair);
            }
            Pair<String, String>[] pairArray = pairs.ToArray();
            double[] ratioArray = ratios.ToArray();

            Array.Sort<double, Pair<String, String>>(ratioArray, pairArray);

            for (int i = pairArray.Length - 1; i >= 0; i--)
                if (removePureLatin && IsLatin(pairArray[i].y)) continue; else writer.WriteLine(pairArray[i].x + "\t" + pairArray[i].y);

        }

        writer.Close();
    }

    static void FilterTrainingList(String sourceListFile, String filteredListFile, bool removePureLatin)
    {
        String[] lines = File.ReadAllLines(sourceListFile);

        Map<String, String> pairMap = new Map<String, String>();
        SparseDoubleVector<Pair<String, String>> scores = new SparseDoubleVector<Pair<String, String>>();

        for (String line in lines)
        {
            String[] parts = line.Split('\t');
            pairMap.Add(parts[0], parts[1]);
            WordAlignment wa = new WordAlignment(parts[2]);
            double score = wa.oneToOne * 10 + wa.equalNumber * 5 + wa.unequalNumber;
            scores[new Pair<String, String>(parts[0], parts[1])] = score;
        }

        List<Pair<String, String>> pairs = new List<Pair<String, String>>();
        List<double> ratios = new List<double>();

        for (Pair<String, String> pair in pairMap)
        {
            double ratio = double.PositiveInfinity;
            double pairScore = scores[pair];
            if (pairScore < 15) continue;
            bool goodPair = true;

            for (Pair<String, String> oPair in JoinEnumerable.Join<Pair<String, String>>(pairMap.GetPairsForKey(pair.Key), pairMap.GetPairsForValue(pair.Value)))
            {
                if (oPair.Key == pair.Key && oPair.Value == pair.Value) continue; //this is the example we're currently looking at
                double oScore = scores[oPair];
                ratio = Math.Min(ratio, pairScore / oScore);
                if (ratio < 3) break;
            }

            if (ratio < 3) continue; //no good
            pairs.Add(pair);
            ratios.Add(ratio);
        }

        Pair<String, String>[] pairArray = pairs.ToArray();
        double[] ratioArray = ratios.ToArray();

        Array.Sort<double, Pair<String, String>>(ratioArray, pairArray);

        StreamWriter writer = new StreamWriter(filteredListFile);
        for (int i = pairArray.Length - 1; i >= 0; i--)
            if (removePureLatin && IsLatin(pairArray[i].y)) continue; else writer.WriteLine(pairArray[i].x + "\t" + pairArray[i].y);

        writer.Close();
    }

    static Regex latinRegex = new Regex("[0-9a-zA-Z]+", RegexOptions.Compiled);
    static bool IsLatin(String word)
    {
        return latinRegex.IsMatch(word);
    }

    static void TestForMissedTranslations(String sourceAliasTableFile, String sourceAliasTableKeyValueFile, String targetLanguageCode)
    {
        StreamDictionary<String, List<WikiAlias>> sourceAliasTable = new StreamDictionary<String, List<WikiAlias>>(
            5, 0.5, OpenBBS(sourceAliasTableFile, 1000000), null, OpenBBS(sourceAliasTableKeyValueFile, 1000000), null, null, null, null, null);

        targetLanguageCode += ":";

        int targetCount = 0;
        int totalCount = 0;
        int otherCount = 0;
        for (Pair<String, List<WikiAlias>> pair in sourceAliasTable)
        {
            bool target = false;
            bool others = false;
            for (WikiAlias alias in pair.Value)
            {
                if (alias.type == AliasType.Interlanguage)
                    if (alias.alias.StartsWith(targetLanguageCode, StringComparison.OrdinalIgnoreCase))
                    {
                        target = true;
                    }
                    else
                    {
                        others = true;
                    }
            }

            totalCount++;
            if (!target && others) otherCount++;
            if (target) targetCount++;
        }

        System.out.println("Total: " + totalCount);
        System.out.println("Target: " + targetCount);
        System.out.println("Others: " + otherCount);
        Console.ReadLine();
    }

    public static Map<String, String> ReadTranslationMap(String translationMapFile, out HashMap<Pair<String, String>, int> weights)
    {
        System.Runtime.Serialization.Formatters.Binary.BinaryFormatter bf = new System.Runtime.Serialization.Formatters.Binary.BinaryFormatter();
        FileStream fs = File.OpenRead(translationMapFile);
        Map<String,String> map = (Map<String, String>)bf.Deserialize(fs);
        weights = (HashMap<Pair<String, String>, int>)bf.Deserialize(fs);
        fs.Close();

        return map;
    }

    public static Map<String, String> ReadTranslationMap2(String translationMapFile, out HashMap<Pair<String, String>, WordAlignment> weights)
    {
        System.Runtime.Serialization.Formatters.Binary.BinaryFormatter bf = new System.Runtime.Serialization.Formatters.Binary.BinaryFormatter();
        FileStream fs = File.OpenRead(translationMapFile);
        Map<String, String> map = (Map<String, String>)bf.Deserialize(fs);
        weights = (HashMap<Pair<String, String>, WordAlignment>)bf.Deserialize(fs);
        fs.Close();

        return map;
    }

    static String FindClosest(Iterable<String> strings, String original)
    {
        int min = int.MaxValue;
        int lengthDistance = int.MaxValue;
        String result = null;
        for (String str : strings)
        {
            int aD;
            int dist = WikiTransliteration.EditDistance<char>(str, original, out aD);
            if (dist < min || (dist==min && Math.Abs(str.Length-original.Length) < lengthDistance))
            {
                lengthDistance = Math.Abs(str.Length - original.Length);
                result = str;
                min = dist;
            }
        }

        return result;
    }

    static List<Pair<String, Double>> FindDistances(Collection<String> strings, String original, int multiplier)
    {
        if (original.length() > 5) original = original.substring(0, 5);
        int editLength;
        double fmultiplier = ((double)1) / multiplier;
        List<Pair<String, Double>> result = new ArrayList<>(strings.size());
        for (String s : strings)
            result.add(new Pair<String, Double>(s, fmultiplier * (1 + WikiTransliteration.EditDistance <char>(s.Length > 5 ? s.Substring(0,5) : s, original, out editLength) + (Math.Abs(original.Length - s.Length))/((double)100))));

        return result;
    }
*/
    public static String StripAccent(String stIn) {
        String strNFD = Normalizer.normalize(stIn, Normalizer.Form.NFD);
        StringBuilder sb = new StringBuilder();
        for (char ch : strNFD.toCharArray()) {
            if (Character.getType(ch) != Character.NON_SPACING_MARK) {
                sb.append(ch);
            }
        }
        return sb.toString();

    }
/*
        static void TestAlexData()
        {
            HashMap<Pair<String, String>, Integer> weights;
            Map<String, String> translationMap = ReadTranslationMap("/path/to/WikiTransliteration/enRuTranslationMap.dat", out weights);

            Map<String, String> deaccent = new HashMap<>();
            for (String key : translationMap.Keys)
                deaccent.put(StripAccent(key), key);


            HashMap<String, List<String>> alexData = GetAlexData("/path/to/res/res/Russian/evalpairs.txt");
            List<String> alexWords = new List<>(GetAlexWords().Keys);

            int total = 0;
            int found = 0;
            int correct = 0;
            StringBuilder output = new StringBuilder();

            for (Pair<String, List<String>> pair : alexData)
            {
                total++;
                String english = pair.Key.ToLower();

                //if (english == "boston")
                //    System.out.println("PLASMA!");

                //if (!translationMap.ContainsKey(english)) continue;
                if (!translationMap.containsKey(english))
                {
                    if (deaccent.containsKey(StripAccent(english)))
                        english = deaccent.GetValuesForKey(StripAccent(english))[0];
                    else
                    {
                        output.AppendLine("Couldn't find english word: " + english);
                        continue; //english = FindClosest(translationMap.Keys, english);
                    }
                }

                found++;

                bool correctFlag = false;

                String[] rawRussianTranslations = translationMap.GetValuesForKey(english);
                List<String> russianTranslations = new List<String>();
                for (String r in rawRussianTranslations)
                {
                    if (!Regex.IsMatch(r, "^[0-9a-zA-Z]+$", RegexOptions.Compiled))
                        russianTranslations.Add(r);
                }

                //Array.Sort<String>(russianTranslations, delegate(String a, String b) { return weights[new Pair<String, String>(english, b)] - weights[new Pair<String, String>(english, a)]; });
                russianTranslations.Sort(delegate(String a, String b) { return weights[new Pair<String, String>(english, b)] - weights[new Pair<String, String>(english, a)]; });

                List<Pair<String,Double>> possibilities = new ArrayList<>();

                HashMap<String, Boolean> closest = new HashMap<>(21);

                for (int i = 0; i < Math.Min(20, russianTranslations.Count); i++)
                {
                    //closest[russianTranslations[i]] = true;
                    possibilities.AddRange(FindDistances(alexWords, russianTranslations[i], weights[new Pair<String, String>(english, russianTranslations[i])]));
                }

                possibilities.Sort(delegate(Pair<String, Double> a, Pair<String, Double> b) { return Math.sign(a.y - b.y); });

                for (Pair<String, Double> poss : possibilities)
                {
                    closest[poss.x] = true;
                    if (closest.Count >= 20) break;
                }

                for (String russian : pair.Value)
                {                    
                    //if (((ICollection<String>)translationMap.GetValuesForKey(english)).Contains(russian.ToLower()))
                    if (closest.ContainsKey(russian))
                    {
                        correct++;
                        correctFlag = true;
                        break;
                    }
                }

                if (!correctFlag)
                {
                    output.AppendLine("English name: " + english);
                    output.AppendLine("Should be one of: " + String.Join(", ",pair.Value.ToArray()));
                    output.Append("Is: ");
                    for (String s in translationMap.GetValuesForKey(english))
                    {
                        output.Append(s); output.Append(" (" + weights[new Pair<String, String>(english, s)] + "), ");
                    }

                    output.AppendLine();

                    output.AppendLine();
                    //output.AppendLine("Is: " + String.Join(", ", ));
                }
                
            }
            

            output.AppendLine();
            output.AppendLine("Total: " + total);
            output.AppendLine("Found: " + (((double)found) / total));
            output.AppendLine("Correct: " + (((double)correct) / total));
            output.AppendLine("Correct out of found: " + (((double)correct) / found));

            File.WriteAllText(@"C:\Data\Wikitransliteration\AlexResults.txt",output.ToString());
        }

        public static HashMap<String, Boolean> GetAlexWords()
        {
            String[] files = Directory.GetFiles(@"C:\Data\WikiTransliteration\complete\ver.1\", "*.rus");
            HashMap<String, Boolean> result = new HashMap<String, Boolean>();

            for (String file : files)
            {
                String text = File.ReadAllText(file, Encoding.Unicode);
                String[] words = Regex.Split(text, @"[\W\d]", RegexOptions.Compiled);
                for (String word in words)
                    if (word.Length > 0)
                        result[word.ToLower()] = true;
            }

            return result;
        }

        static void WriteRedirectTable(String wikiFile, String redirectFile)
        {
            ICSharpCode.SharpZipLib.BZip2.BZip2InputStream bzipped =
                new ICSharpCode.SharpZipLib.BZip2.BZip2InputStream(File.OpenRead(wikiFile));
            WikiXMLReader reader = new WikiXMLReader(bzipped);

            StreamWriter writer = new StreamWriter(redirectFile);
            WikiRedirectTable.WriteRedirectPairs(reader, writer);
            writer.Close();
            reader.Close();
        }

        static void WriteWPTitles()
        {
            StreamWriter writer = new StreamWriter(@"C:\Data\WikiTransliteration\Segmentation\enWPTitles.txt");

            ICSharpCode.SharpZipLib.BZip2.BZip2InputStream bzipped =
                new ICSharpCode.SharpZipLib.BZip2.BZip2InputStream(File.OpenRead(@"C:\Users\jpaster2\Downloads\enwiki-20090530-pages-articles.xml.bz2"));
            WikiXMLReader reader = new WikiXMLReader(bzipped);
            for (WikiPage page in reader.Pages)
                writer.WriteLine(page.Title);

            writer.Close();
            reader.Close();
        }

        public static void WriteSegmentCounts()
        {            
            StreamReader reader = new StreamReader(@"C:\Data\WikiTransliteration\Segmentation\enWPTitles.txt");
            HashMap<String, bool> words = new HashMap<String, bool>();
            String line;
            while ((line = reader.ReadLine()) != null)
            {
                if (WikiNamespace.GetNamespace(line, Wikipedia.Namespaces) != Wikipedia.DefaultNS) continue;
                for (String word in WordSegmentation.SplitWords(line))
                    words[word.ToLower()] = true;
            }

            WordSegmentation.WriteCounts(@"C:\Data\WikiTransliteration\Segmentation\enSegCounts.txt", WordSegmentation.GetNgramCounts(words.Keys));
        }


        public static void RunSegEMExperiment()
        {
            StreamReader reader = new StreamReader(@"C:\Data\WikiTransliteration\Segmentation\enWPTitles.txt");
            SparseDoubleVector<String> words = new SparseDoubleVector<String>();
            String line;
            while ((line = reader.ReadLine()) != null)
            {
                if (WikiNamespace.GetNamespace(line, Wikipedia.Namespaces) != Wikipedia.DefaultNS) continue;
                for (String word in WordSegmentation.SplitWords(line))
                    if (word.Length < 20) words[word.ToLower()] += 1;  //no insanely long words
            }

            WordSegmentation.Learn(words);
        }

        public static void RunCompressExperiment()
        {
            StreamReader reader = new StreamReader(@"C:\Data\WikiTransliteration\Segmentation\enWPTitles.txt");
            SparseDoubleVector<String> words = new SparseDoubleVector<String>();
            String line;
            while ((line = reader.ReadLine()) != null)
            {
                if (WikiNamespace.GetNamespace(line, Wikipedia.Namespaces) != Wikipedia.DefaultNS) continue;
                for (String word in WordSegmentation.SplitWords(line))
                    if (word.Length < 20) words[word.ToLower()] += 1;  //no insanely long words
            }

            words = (words / 10).Floor().Sign();
            words.RemoveRedundantElements();

            WordCompression.Compress(words);
        }

        public static void RunCompressDemo()
        {
            StreamReader reader = new StreamReader(@"C:\Data\WikiTransliteration\Segmentation\chunks.txt");
            SparseDoubleVector<String> words = new SparseDoubleVector<String>();
            String line;
            while ((line = reader.ReadLine()) != null) words.Add(line, 0.5);

            System.out.println("Proceed.");

            while (true)
            {
                String word = Console.ReadLine();
                WordSegmentation.OutputSegmentations(word, 10, words);
            }
        }
        */

    public static void HebrewDiscoveryTest() throws FileNotFoundException {
        List<Pair<String, String>> wordList = NormalizeHebrew(GetTabDelimitedPairs("/path/to/res/res/Hebrew/evalwords.txt"));
        List<Pair<String, String>> trainList = NormalizeHebrew(GetTabDelimitedPairs("/path/to/res/res/Hebrew/train_EnglishHebrew.txt"));
        List<Pair<String, String>> trainList2 = NormalizeHebrew(GetTabDelimitedPairs("/path/to/WikiTransliteration/Aliases/heExamples.txt"));
        //List<Pair<String, String>> trainList2 = RemoveVeryLong(NormalizeHebrew(GetTabDelimitedPairs(@"C:\Data\WikiTransliteration\Aliases\heExamples-Lax.txt")), 20);

        List<Pair<String, String>> wordAndTrain = new ArrayList<>(wordList);
        wordAndTrain.addAll(trainList);

        HashMap<String, Boolean> usedExamples = new HashMap<>();
        for (Pair<String, String> pair : wordAndTrain) {
            usedExamples.put(pair.getFirst(), true);
        }
        //trainList2 = TruncateList(trainList2, 2000);
        for (Pair<String, String> pair : trainList2) {
            if (!usedExamples.containsKey(pair.getFirst()))
                trainList.add(pair);
        }

        //DiscoveryTestDual(RemoveDuplicates(GetListValues(wordList)), trainList, LiftPairList(wordList), 15, 15);
        //TestXMLData(trainList, wordList, 15, 15);


        List<String> candidateList = GetListValues(wordList);
        //wordList = GetRandomPartOfList(trainList, 50, 31);
        candidateList.addAll(GetListValues(wordList));

        DiscoveryEM(200, RemoveDuplicates(candidateList), trainList, LiftPairList(wordList), new CSPModel(40, 0, 0, 0.000000000000001, CSPModel.SegMode.None, false, CSPModel.SmoothMode.BySum, FallbackStrategy.NotDuringTraining, CSPModel.EMMode.Normal, false));
        //DiscoveryEM(200, RemoveDuplicates(candidateList), trainList, LiftPairList(wordList), new CSPModel(40, 0, 0, 0, FallbackStrategy.Standard));

        //DiscoveryTestDual(RemoveDuplicates(candidateList), trainList, LiftPairList(wordList), 40, 40);
        //DiscoveryTest(RemoveDuplicates(candidateList), trainList, LiftPairList(wordList), 40, 40);
    }

    public static void ChineseDiscoveryTest() throws FileNotFoundException {

        //List<Pair<String, String>> trainList = CharifyTargetWords(GetTabDelimitedPairs(@"C:\Users\jpaster2\Desktop\res\res\Chinese\chinese_full"),chMap);
        List<Pair<String, String>> trainList = UndotTargetWords(GetTabDelimitedPairs("/path/to/res/res/Chinese/chinese_full"));
        List<Pair<String, String>> wordList = GetRandomPartOfList(trainList, 700, 123);

        //StreamWriter writer = new StreamWriter(@"C:\Users\jpaster2\Desktop\res\res\Chinese\chinese_test_pairs.txt");
        //for (Pair<String,String> pair in wordList)
        //    writer.WriteLine(pair.Key + "\t" + pair.Value);

        //writer.Close();

        List<String> candidates = GetListValues(wordList);
        //wordList.RemoveRange(600, 100);
        wordList = wordList.subList(0, 600);

        //DiscoveryTestDual(RemoveDuplicates(candidates), trainList, LiftPairList(wordList), 15, 15);
        DiscoveryEM(200, RemoveDuplicates(candidates), trainList, LiftPairList(wordList), new CSPModel(40, 0, 0, 0.000000000000001, CSPModel.SegMode.Entropy, false, CSPModel.SmoothMode.BySource, FallbackStrategy.Standard, CSPModel.EMMode.Normal, false));
        //TestXMLData(trainList, wordList, 15, 15);


    }

    public static void RussianDiscoveryTest() throws FileNotFoundException {

        List<String> candidateList = LineIO.read("/path/to/res/res/Russian/RussianWords");
        for (int i = 0; i < candidateList.size(); i++)
            candidateList.set(i, candidateList.get(i).toLowerCase());
        //candidateList.Clear();

        //HashMap<String, List<String>> evalList = GetAlexData(@"C:\Users\jpaster2\Desktop\res\res\Russian\evalpairs.txt");//@"C:\Users\jpaster2\Desktop\res\res\Russian\evalpairs.txt");
        HashMap<String, List<String>> evalList = GetAlexData("/path/to/res/res/Russian/evalpairsShort.txt");//@"C:\Users\jpaster2\Desktop\res\res\Russian\evalpairs.txt");

        //List<Pair<String, String>> trainList = NormalizeHebrew(GetTabDelimitedPairs(@"C:\Users\jpaster2\Desktop\res\res\Hebrew\train_EnglishHebrew.txt"));
        List<Pair<String, String>> trainList = GetTabDelimitedPairs("/path/to/WikiTransliteration/Aliases/ruExamples.txt");
        //List<Pair<String, String>> trainList2 = RemoveVeryLong(NormalizeHebrew(GetTabDelimitedPairs(@"C:\Data\WikiTransliteration\Aliases\heExamples-Lax.txt")), 20);
        List<Pair<String, String>> trainList2 = new ArrayList<>(trainList.size());

        HashMap<String, Boolean> usedExamples = new HashMap<>();
        for (String s : evalList.keySet())
            usedExamples.put(s, true);

        //trainList2 = TruncateList(trainList2, 2000);
        for (Pair<String, String> pair : trainList)
            if (!usedExamples.containsKey(pair.getFirst())) trainList2.add(pair);

        DiscoveryEM(200, RemoveDuplicates(GetWords(evalList)), trainList2, evalList, new CSPModel(40, 0, 0, 0.000000000000001, CSPModel.SegMode.Entropy, false, CSPModel.SmoothMode.BySource, FallbackStrategy.Standard, CSPModel.EMMode.Normal, false));
        //DiscoveryTestDual(RemoveDuplicates(candidateList), trainList2, evalList, 15, 15);
        //DiscoveryTestDual(RemoveDuplicates(GetWords(evalList)), trainList2, evalList, 15, 15);

    }

    public static List<String> GetWords(HashMap<String, List<String>> dict) {
        List<String> result = new ArrayList<>();
        for (List<String> list : dict.values())
            result.addAll(list);
        return result;
    }

    public static HashMap<String, List<String>> LiftPairList(List<Pair<String, String>> list) {
        HashMap<String, List<String>> result = new HashMap<>(list.size());
        for (Pair<String, String> pair : list) {
            ArrayList<String> tmp = new ArrayList<>();
            tmp.add(pair.getSecond());
            result.put(pair.getFirst(), tmp);
        }

        return result;
    }

    private static List<Pair<String, String>> UndotTargetWords(List<Pair<String, String>> list) {
        List<Pair<String, String>> result = new ArrayList<>(list.size());
        for (Pair<String, String> pair : list) {
            result.add(new Pair<>(pair.getFirst(), pair.getSecond().replace(".", "")));
        }

        return result;
    }

//        public static List<Pair<String, String>> TruncateList(List<Pair<String, String>> list, int maxCount)
//        {
//            List<Pair<String, String>> result = new List<Pair<String,String>>(Math.Min(maxCount, list.Count));
//            for (int i = 0; i < maxCount && i < list.Count; i++)
//                result.Add(list[i]);
//
//            return result;
//        }

//        private static List<Pair<String, String>> RemoveVeryLong(List<Pair<String, String>> list, int maxLength)
//        {
//            List<Pair<String, String>> result = new List<>(list.Count);
//            for (Pair<String,String> pair in list)
//            {
//                if (pair.Key.Length <= maxLength && pair.Value.Length <= maxLength) result.Add(pair);
//            }
//
//            return result;
//        }
//
//
//        private static String Charify(String word, HashMap<String,Integer> map)
//        {
//            String[] parts = word.split(".");
//            StringBuilder result = new StringBuilder(parts.Length);
//            for (String part : parts)
//            {
//                if (!map.ContainsKey(part))
//                    map[part] = map.Count;
//
//                result.Append((char)map[part]);
//            }
//
//            return result.toString();
//        }
//        private static List<Pair<String, String>> CharifyTargetWords(List<Pair<String, String>> list, HashMap<String,int> map)
//        {
//            List<Pair<String, String>> result = new List<Pair<String, String>>(list.Count);
//            for (Pair<String, String> pair : list)
//            {
//                result.Add(new Pair<String, String>(pair.Key, Charify(pair.Value, map)));
//            }
//
//            return result;
//        }

    private static List<String> RemoveDuplicates(List<String> list) {
        List<String> result = new ArrayList<>(list.size());
        HashMap<String, Boolean> seen = new HashMap<>();
        for (String s : list) {
            if (seen.containsKey(s)) continue;
            seen.put(s, true);
            result.add(s);
        }

        return result;
    }

    private static List<Triple<String, String, Double>> ConvertExamples(List<Pair<String, String>> examples) {
        List<Triple<String, String, Double>> fExamples = new ArrayList<>(examples.size());
        for (Pair<String, String> pair : examples)
            fExamples.add(new Triple<>(pair.getFirst(), pair.getSecond(), 1.0));

        return fExamples;
    }

    /**
     * Calculates a probability table for P(String2 | String1)
     * This normalizes by source counts for each production.
     *
     * FIXME: This is identical to... WikiTransliteration.NormalizeBySourceSubstring
     *
     * @param ?
     * @return
     */
        public static HashMap<Production, Double> PSecondGivenFirst(HashMap<Production, Double> counts)
        {
            // counts of first words in productions.
            HashMap<String, Double> totals1 = WikiTransliteration.GetAlignmentTotals1(counts);

            HashMap<Production, Double> result = new HashMap<>(counts.size());
            for (Production prod : counts.keySet()) // loop over all productions
            {
                double prodcount = counts.get(prod);
                double sourcecounts = totals1.get(prod.getFirst()); // be careful of unboxing!
                double value = sourcecounts == 0 ? 0 : (prodcount / sourcecounts);
                result.put(prod, value);
            }

            return result;
        }


//        private static HashMap<Pair<String, String>, Double> MakeAlignmentTable(int maxSubstringLength1, int maxSubstringLength2, List<Triple<String,String,double>> examples, HashMap<Pair<String, String>, double> probs, bool weightedAlignments)
//        {
//            Pasternack.Collections.Generic.Specialized.InternDictionary<String> internTable = new Pasternack.Collections.Generic.Specialized.InternDictionary<String>();
//            HashMap<Pair<String,String>,double> counts = new HashMap<Pair<String,String>,double>();
//
//            int alignmentCount=0;
//            for (Triple<String,String,double> example in examples)
//            {
//                String sourceWord = example.x;
//                String bestWord = example.y;
//                if (sourceWord.Length * maxSubstringLength2 >= bestWord.Length && bestWord.Length * maxSubstringLength1 >= sourceWord.Length)
//                {
//                    alignmentCount++;
//                    HashMap<Pair<String,String>,double> wordCounts;
//                    if (weightedAlignments && probs != null)
//                        wordCounts = WikiTransliteration.FindWeightedAlignments(sourceWord, bestWord, maxSubstringLength1,maxSubstringLength2, probs, internTable,NormalizationMode.AllProductions);
//                    else
//                        wordCounts = WikiTransliteration.FindAlignments(sourceWord, bestWord, maxSubstringLength1,maxSubstringLength2, internTable,NormalizationMode.AllProductions);
//
//                    if (!weightedAlignments && probs != null) wordCounts = SumNormalize(Dictionaries.Multiply<Pair<String,String>>(wordCounts,probs));
//
//                    Dictionaries.AddTo<Pair<String, String>>(counts, wordCounts,example.z);
//                }
//            }
//
//            //HashMap<Pair<String, String>, double> newCounts = new HashMap<Pair<String, String>, double>(counts.Count);
//            //for (Pair<Pair<String, String>, double> pair in counts)
//            //    newCounts.Add(pair.Key, pair.Value > 0 ? pair.Value : double.Epsilon);
//            //counts = newCounts;
//
//            HashMap<String, double> totals1 = WikiTransliteration.GetAlignmentTotals1(counts);
//            HashMap<String, double> totals2 = WikiTransliteration.GetAlignmentTotals2(counts);
//            HashMap<Pair<String, String>, double> result = new HashMap<Pair<String, String>, double>(counts.Count);
//            for (Pair<Pair<String, String>, double> pair in counts)
//            //result[pair.Key] = pair.Value * pair.Value / (totals1[pair.Key.x]*totals2[pair.Key.y]);
//            {
//                //if (pair.Value == 0)
//                //    result[pair.Key] = double.Epsilon;
//                //else
//                {
//                    double value = (pair.Value / totals1[pair.Key.x]) * (pair.Value / totals2[pair.Key.y]);
//                    if (double.IsNaN(value) || double.IsInfinity(value))
//                        return null; // System.out.println("Bad!");
//                    result[pair.Key] = value;
//                }
//            }
//
//            System.out.println(alignmentCount + " words aligned.");
//
//            return result;
//        }

        private static HashMap<Production, Double> SumNormalize(HashMap<Production, Double> vector)
        {
            HashMap<Production, Double> result = new HashMap<>(vector.size());
            double sum = 0;
            for (double value : vector.values()) {
                sum += value;
            }

            for (Production key : vector.keySet()) {
                Double value = vector.get(key);
                result.put(key, value / sum);
            }

            return result;
        }

//        private static List<Triple<String, String,WordAlignment>> GetTrainingExamples(String translationMapFile)
//        {
//            List<Triple<String, String, WordAlignment>> result = new List<Triple<String, String, WordAlignment>>();
//
//            HashMap<Pair<String, String>, WordAlignment> weights;
//            Map<String, String> translationMap = ReadTranslationMap2(translationMapFile, out weights);
//
//            for (String sourceWord in translationMap.Keys)
//            {
//                WordAlignment maxWeight = new WordAlignment(0,0,0); String bestWord = null;
//                for (String targetWord in translationMap.GetValuesForKey(sourceWord))
//                    if (weights[new Pair<String, String>(sourceWord, targetWord)] > maxWeight)
//                    {
//                        maxWeight = weights[new Pair<String, String>(sourceWord, targetWord)];
//                        bestWord = targetWord;
//                    }
//
//                if (maxWeight.oneToOne >= 1 && !Regex.IsMatch(bestWord,"^[A-Za-z0-9]+$",RegexOptions.Compiled))
//                {
//                    result.Add(new Triple<String,String,WordAlignment>(sourceWord,bestWord,maxWeight));
//                    //Dictionaries.Add<Pair<String, String>>(counts, oneAlignmentPerWord ? WikiTransliteration.FindAlignments(sourceWord, bestWord, maxSubstringLength, internTable) : WikiTransliteration.CountAlignments(sourceWord, bestWord, maxSubstringLength, internTable), 1);
//                }
//            }
//
//            return result;
//        }

//        public static void CheckTopList(TopList<double, String> list)
//        {
//            HashMap<String,bool> dict = new HashMap<String,bool>(list.Count);
//            for (String val in list.Values)
//                if (dict.ContainsKey(val)) throw new InvalidOperationException();
//                else dict[val] = true;
//        }

    //public static void TestXMLData2(String trainingFile, String testFile, int maxSubstringLength1, int maxSubstringLength2)
    //{
    //    List<Pair<String, String>> trainingPairs = GetTaskPairs(trainingFile);
    //    List<Pair<String, String>> testingPairs = GetTaskPairs(testFile);

    //    List<String> languageExamples = new List<String>(trainingPairs.Count);
    //    for (Pair<String, String> pair in trainingPairs)
    //        languageExamples.Add(pair.Value);
    //    HashMap<String, int> ngramCounts = null;  WikiTransliteration.GetNgramCounts(3, languageExamples);

    //    HashMap<Pair<String, String>, double> probs = MakeAlignmentTable(maxSubstringLength1, maxSubstringLength2, trainingPairs, null,false);

    //    Map<String, String> probMap = WikiTransliteration.GetProbMap(probs);
    //    //HashMap<String, Pair<String, double>> maxProbs = WikiTransliteration.GetMaxProbs(probs);

    //    int correct = 0;
    //    int contained = 0;
    //    double mrr = 0;
    //    for (Pair<String, String> pair in testingPairs)
    //    {
    //        TopList<double, String> predictions = WikiTransliteration.Predict(20, pair.Key, maxSubstringLength1, probMap,probs, new HashMap<String, TopList<double, String>>(),ngramCounts,3);
    //        CheckTopList(predictions);
    //        int position = predictions.Values.IndexOf(pair.Value);
    //        if (position == 0)
    //            correct++;

    //        if (position >= 0)
    //            contained++;

    //        if (position < 0)
    //            position = 20;

    //        mrr += 1 / ((double)position + 1);
    //    }

    //    mrr /= testingPairs.Count;

    //    System.out.println(testingPairs.Count + " pairs tested in total.");
    //    System.out.println(contained + " predictions contained (" + (((double)contained) / testingPairs.Count) + ")");
    //    System.out.println(correct + " predictions exactly correct (" + (((double)correct) / testingPairs.Count) + ")");
    //    System.out.println("MRR: " + mrr);
    //    Console.ReadLine();
    //}

//        public static void Reweight(HashMap<Pair<String, String>, double> rawProbs, List<Triple<String, String, double>> examples, List<List<Pair<Pair<String, String>, double>>> exampleCounts, int maxSubstringLength)
//        {
//            Map<String,String> rawMap = new Map<String,String>();
//            for (Pair<String, String> pair in rawProbs.Keys)
//                rawMap.Add(pair);
//
//            double maxWeight = 0;
//            for (int i = 0; i < examples.Count; i++)
//            {
//                List<Pair<Pair<String, String>, double>> curExampleCounts = exampleCounts[i];
//                if (curExampleCounts == null) //alignment impossible
//                {
//                    examples[i] = new Triple<String,String,double>(examples[i].x,examples[i].y,0);
//                    continue;
//                }
//
//                double oldWeight = examples[i].z;
//
//                for (Pair<Pair<String, String>, double> pair in curExampleCounts)
//                {
//                    rawProbs[pair.Key] = rawProbs[pair.Key] - (oldWeight * pair.Value);
//                }
//
//                //double newWeight = WikiTransliteration.GetAlignmentProbability(examples[i].x, examples[i].y, maxSubstringLength, PSecondGivenFirst(examples[i].x,examples[i].y, maxSubstringLength, curExampleCounts, rawMap,rawProbs), 0);
//                HashMap<Pair<String,String>,double> probs = PSecondGivenFirst(examples[i].x,examples[i].y, maxSubstringLength, curExampleCounts, rawMap,rawProbs);
//                Map<String,String> probMap = new Map<String,String>();
//                for (Pair<String, String> pair in probs.Keys)
//                    probMap.Add(pair);
//
//                double newWeight = 0.5;
//                if (WikiTransliteration.Predict(1, examples[i].x, maxSubstringLength, probMap, probs, new HashMap<String, TopList<double, String>>(), null, 4).Values.Contains(examples[i].y))
//                    newWeight = 1;
//
//                //int index = WikiTransliteration.Predict(20, examples[i].x, maxSubstringLength, probMap, probs, new HashMap<String, TopList<double, String>>(), null, 4).Values.IndexOf(examples[i].y);
//                //if (index >= 0)
//                //    newWeight = 1d / (index + 1);
//
//                for (Pair<Pair<String, String>, double> pair in curExampleCounts)
//                {
//                    rawProbs[pair.Key] = rawProbs[pair.Key] + (oldWeight * pair.Value);
//                }
//
//                maxWeight = Math.Max(maxWeight, newWeight);
//                examples[i] = new Triple<String, String, double>(examples[i].x, examples[i].y, newWeight);
//            }
//
//            for (int i = 0; i < examples.Count; i++)
//                examples[i] = new Triple<String,String,double>(examples[i].x,examples[i].y,examples[i].z/maxWeight);
//        }
//
//        public static List<Pair<String, String>> FilterExamplePairs(List<Pair<String, String>> examplePairs)
//        {
//            List<Pair<String, String>> result = new List<Pair<String, String>>(examplePairs.Count);
//            for (Pair<String, String> examplePair in examplePairs)
//                result.Add(new Pair<String, String>(Regex.Replace(examplePair.Key, @"[`'ʻ-]", "", RegexOptions.Compiled), examplePair.Value));
//
//            return result;
//        }
//
//        public static List<String> FilterExampleWords(List<String> exampleWords)
//        {
//            List<String> result = new List<String>(exampleWords.Count);
//            for (String exampleWord in exampleWords)
//                result.Add(Regex.Replace(exampleWord, @"[`'ʻ-]", "", RegexOptions.Compiled));
//
//            return result;
//        }
//
//        public static HashMap<String, double> UniformLanguageModel(HashMap<String, double> languageModel)
//        {
//            HashMap<String, double> result = new HashMap<String, double>(languageModel.Count);
//            for (Pair<String, double> pair in languageModel)
//                result[pair.Key] = 1;
//
//            return result;
//        }
//
//        public static void TestXMLDataOldStyle(String trainingFile, String testFile, int maxSubstringLength1, int maxSubstringLength2)
//        {
//            //Console
//            int ngramSize = 6;
//            List<Pair<String, String>> trainingPairs = FilterExamplePairs(GetTaskPairs(trainingFile));
//
//
//            List<Triple<String, String, double>> trainingTriples = ConvertExamples(trainingPairs);
//            List<Pair<String, String>> testingPairs = FilterExamplePairs(GetTaskPairs(testFile));
//
//            List<String> trainingWords = new List<String>(trainingPairs.Count);
//            for (Pair<String, String> pair in trainingPairs)
//                trainingWords.Add(pair.Value);
//            HashMap<String, double> languageModel = WikiTransliteration.GetNgramProbs(1, ngramSize, trainingWords);
//
//            List<List<Pair<Pair<String, String>, double>>> exampleCounts;
//
//            HashMap<Pair<String, String>, double> probs = MakeRawAlignmentTable(maxSubstringLength1, maxSubstringLength2, trainingTriples, null, WeightingMode.None, NormalizationMode.BySourceSubstring, false, out exampleCounts);
//            EvaluateExamples(testingPairs, PMutualProduction(probs), maxSubstringLength1, null, ngramSize, 20, false, 0);
//            //EvaluateExamples(testingPairs, PMutualProduction(probs), maxSubstringLength1, languageModel, ngramSize, 200, true);
//
//            for (int i = 0; i < 500; i++)
//            {
//                //probs = MakeRawAlignmentTable(maxSubstringLength1, maxSubstringLength2, trainingTriples, PMutualProduction(probs), true,NormalizationMode.AllProductions, true, out exampleCounts);
//                probs = MakeRawAlignmentTable(maxSubstringLength1, maxSubstringLength2, trainingTriples, PSemiJoint(probs, maxSubstringLength1), WeightingMode.SuperficiallyWeighted, NormalizationMode.BySourceSubstring, false, out exampleCounts);
//                if (probs == null) break;
//
//                System.out.println("Iteration #" + i);
//                EvaluateExamples(testingPairs, PMutualProduction(probs), maxSubstringLength1, null, ngramSize, 20, false, 0);
//                EvaluateExamples(testingPairs, PMutualProduction(probs), maxSubstringLength1, languageModel, ngramSize, 200, true, 0);
//            }
//
//            System.out.println("Finished.");
//        }
//
//        //This method gets a surprising 60% correct on the test data
//        public static void TestXMLDataOldOldStyle(String trainingFile, String testFile, int maxSubstringLength1, int maxSubstringLength2)
//        {
//            //Console
//            int ngramSize = 4;
//            List<Pair<String, String>> trainingPairs = FilterExamplePairs(GetTaskPairs(trainingFile));
//
//
//            List<Triple<String, String, double>> trainingTriples = ConvertExamples(trainingPairs);
//            List<Pair<String, String>> testingPairs = FilterExamplePairs(GetTaskPairs(testFile));
//
//            List<String> trainingWords = new List<String>(trainingPairs.Count);
//            for (Pair<String, String> pair in trainingPairs)
//                trainingWords.Add(pair.Value);
//            HashMap<String, double> languageModel = WikiTransliteration.GetNgramProbs(1, ngramSize, trainingWords);
//
//            List<List<Pair<Pair<String, String>, double>>> exampleCounts;
//
//            HashMap<Pair<String, String>, double> probs = MakeRawAlignmentTable(maxSubstringLength1, maxSubstringLength2, trainingTriples, null, WeightingMode.None, NormalizationMode.AllProductions, false, out exampleCounts);
//            EvaluateExamples(testingPairs, PMutualProduction(probs), maxSubstringLength1, null, ngramSize, 20, false, 0);
//            //EvaluateExamples(testingPairs, PMutualProduction(probs), maxSubstringLength1, languageModel, ngramSize, 200, true);
//
//            for (int i = 0; i < 500; i++)
//            {
//                //probs = MakeRawAlignmentTable(maxSubstringLength1, maxSubstringLength2, trainingTriples, PMutualProduction(probs), true,NormalizationMode.AllProductions, true, out exampleCounts);
//                probs = MakeRawAlignmentTable(maxSubstringLength1, maxSubstringLength2, trainingTriples, PMutualProduction(probs), WeightingMode.FindWeighted, NormalizationMode.AllProductions, false, out exampleCounts);
//                if (probs == null) break;
//
//                System.out.println("Iteration #" + i);
//                EvaluateExamples(testingPairs, PMutualProduction(probs), maxSubstringLength1, null, ngramSize, 20, false, 0);
//                EvaluateExamples(testingPairs, PMutualProduction(probs), maxSubstringLength1, languageModel, ngramSize, 10, true, 0);
//            }
//
//            System.out.println("Finished.");
//            Console.ReadLine();
//        }
//
//        public static HashMap<String, double> GetNgramCounts(List<Pair<String, String>> examples, int maxSubstringLength)
//        {
//            InternDictionary<String> internTable = new InternDictionary<String>();
//            HashMap<String, double> result = new HashMap<String, double>();
//
//            List<String> exampleSources = new List<String>(examples.Count);
//
//            for (Pair<String, String> example in examples)
//                exampleSources.Add(example.Key);
//
//            return WikiTransliteration.GetNgramCounts(1, maxSubstringLength, exampleSources, false);
//        }
//

    /**
     * Does this not need to have access to ngramsize? No. It gets all ngrams so it can backoff.
     * @param examples
     * @param maxSubstringLength
     * @return
     */
    public static HashMap<String, Double> GetNgramCounts(List<String> examples, int maxSubstringLength)
    {
        return WikiTransliteration.GetNgramCounts(1, maxSubstringLength, examples, false);
    }

    /**
     * Given a wikidata file, this gets all the words in the foreign language for the language model.
     * @param fname
     * @return
     */
    public static List<String> getForeignWords(String fname) throws FileNotFoundException {
        List<String> lines = LineIO.read(fname);
        List<String> words = new ArrayList<>();

        for(String line : lines){
            String[] parts = line.trim().split("\t");
            String foreign = parts[0];

            String[] fsplit = foreign.split(" ");
            for(String word : fsplit){
                words.add(word);
            }
        }

        return words;
    }

    /**
     * Given a wikidata file, this gets all the words in the foreign language for the language model.
     * @return
     */
    public static List<String> getForeignWords(List<Example> examples) throws FileNotFoundException {

        List<String> words = new ArrayList<>();

        for(Example e : examples){
            words.add(e.getTransliteratedWord());
        }

        return words;
    }

//
//        public static SparseDoubleVector<Pair<String, String>> MultiPair(HashMap<String, double> vector1, HashMap<Pair<String, String>, double> vector2)
//        {
//            HashMap<Pair<String, String>, double> result = new HashMap<Pair<String, String>, double>(vector2.Count);
//
//            for (Pair<Pair<String, String>, double> pair in vector2)
//            {
//                result[pair.Key] = vector1.ContainsKey(pair.Key.x) ? pair.Value * vector1[pair.Key.x] : 0;
//            }
//
//            return result;
//        }
//
//        public static SparseDoubleVector<Pair<String, String>> MultiPair2(HashMap<String, double> vector1, HashMap<Pair<String, String>, double> vector2)
//        {
//            HashMap<Pair<String, String>, double> result = new HashMap<Pair<String, String>, double>(vector2.Count);
//
//            for (Pair<Pair<String, String>, double> pair in vector2)
//            {
//                result[pair.Key] = vector1.ContainsKey(pair.Key.y) ? pair.Value * vector1[pair.Key.y] : 0;
//            }
//
//            return result;
//        }
//
//        public static void TestXMLData(String trainingFile, String testFile, int maxSubstringLength1, int maxSubstringLength2)
//        {
//            List<Pair<String, String>> trainingPairs = FilterExamplePairs(GetTaskPairs(trainingFile));
//            List<Pair<String, String>> testingPairs = FilterExamplePairs(GetTaskPairs(testFile));
//
//            TestXMLData(trainingPairs, testingPairs, maxSubstringLength1, maxSubstringLength2);
//        }
//
//        public static String Reverse(String word)
//        {
//            char[] wordChars = new char[word.Length];
//            for (int i = 0; i < word.Length; i++)
//                wordChars[wordChars.Length - 1 - i] = word[i];
//
//            return new String(wordChars);
//        }
//
//        public static List<Pair<String, String>> ReverseTargetWord(List<Pair<String, String>> wordPairs)
//        {
//            List<Pair<String, String>> result = new List<Pair<String, String>>(wordPairs.Count);
//            for (Pair<String, String> pair : wordPairs)
//            {
//                result.Add(new Pair<String, String>(pair.Key, Reverse(pair.Value)));
//            }
//
//            return result;
//        }
//
    public static List<Pair<String, String>> GetTabDelimitedPairs(String filename) throws FileNotFoundException {
        List<Pair<String, String>> result = new ArrayList<>();

        for (String line : LineIO.read(filename)) {
            String[] pair = line.trim().split("\t");
            if (pair.length != 2) continue;
            result.add(new Pair<>(pair[0].trim().toLowerCase(), pair[1].trim().toLowerCase()));
        }

        return result;
    }

//        private static HashMap<Pair<String, String>, Double> MakeAlignmentTable(int maxSubstringLength1, int maxSubstringLength2, List<Pair<String, String>> examples, HashMap<Pair<String, String>, double> probs, bool weightedAlignments)
//        {
//            List<Triple<String, String, Double>> fExamples = new List<Triple<String, String, double>>(examples.Count);
//            for (Pair<String, String> pair : examples)
//                fExamples.Add(new Triple<String, String, Double>(pair.Key, pair.Value, 1));
//
//            return MakeAlignmentTable(maxSubstringLength1, maxSubstringLength2, fExamples, probs,weightedAlignments);
//        }

//        private static HashMap<Pair<String, String>, double> MakeAlignmentTableLog(int maxSubstringLength1, int maxSubstringLength2, List<Pair<String, String>> examples, HashMap<Pair<String, String>, double> probs, bool weightedAlignments)
//        {
//            List<Triple<String, String, double>> fExamples = new List<Triple<String, String, double>>(examples.Count);
//            for (Pair<String, String> pair : examples)
//                fExamples.Add(new Triple<String, String, double>(pair.Key, pair.Value, 1));
//
//            return MakeAlignmentTableLog(maxSubstringLength1, maxSubstringLength2, fExamples, probs, weightedAlignments);
//        }

//        private static HashMap<Pair<String, String>, double> MakeAlignmentTableLog(int maxSubstringLength1, int maxSubstringLength2, List<Triple<String, String, double>> examples, HashMap<Pair<String, String>, double> probs, bool weightedAlignments)
//        {
//            Pasternack.Collections.Generic.Specialized.InternDictionary<String> internTable = new Pasternack.Collections.Generic.Specialized.InternDictionary<String>();
//            HashMap<Pair<String, String>, double> counts = new HashMap<Pair<String, String>, double>();
//
//            int alignmentCount = 0;
//            for (Triple<String, String, double> example in examples)
//            {
//                String sourceWord = example.x;
//                String bestWord = example.y;
//                if (sourceWord.Length * maxSubstringLength2 >= bestWord.Length && bestWord.Length * maxSubstringLength1 >= sourceWord.Length)
//                {
//                    alignmentCount++;
//                    HashMap<Pair<String, String>, double> wordCounts;
//                    if (weightedAlignments && probs != null)
//                        wordCounts = WikiTransliteration.FindLogWeightedAlignments(sourceWord, bestWord, maxSubstringLength1, maxSubstringLength2, probs, internTable,true);
//                    else
//                        wordCounts = WikiTransliteration.FindLogAlignments(sourceWord, bestWord, maxSubstringLength1, maxSubstringLength2, internTable, true);
//
//                    //if (!weightedAlignments && probs != null) wordCounts = SumNormalize(Dictionaries.Multiply<Pair<String, String>>(wordCounts, probs));
//
//                    HashMap<Pair<String, String>, double> expWordCounts = new HashMap<Pair<String, String>, double>(wordCounts.Count);
//                    for (Pair<Pair<String, String>, double> pair in wordCounts)
//                        expWordCounts.Add(pair.Key, Math.Exp(pair.Value));
//
//                    Dictionaries.AddTo<Pair<String, String>>(counts, expWordCounts, example.z);
//                }
//            }
//
//            //HashMap<Pair<String, String>, double> newCounts = new HashMap<Pair<String, String>, double>(counts.Count);
//            //for (Pair<Pair<String, String>, double> pair in counts)
//            //    newCounts.Add(pair.Key, pair.Value > 0 ? pair.Value : double.Epsilon);
//            //counts = newCounts;
//
//            HashMap<String, double> totals1 = WikiTransliteration.GetAlignmentTotals1(counts);
//            HashMap<String, double> totals2 = WikiTransliteration.GetAlignmentTotals2(counts);
//            HashMap<Pair<String, String>, double> result = new HashMap<Pair<String, String>, double>(counts.Count);
//            for (Pair<Pair<String, String>, double> pair in counts)
//            {
//                double value = (pair.Value == 0 ? 0 : (pair.Value / totals1[pair.Key.x]) * (pair.Value / totals2[pair.Key.y]));
//                //if (double.IsNaN(value) || double.IsInfinity(value))
//                //    return null; // System.out.println("Bad!");
//                result[pair.Key] = Math.Log(value);
//            }
//
//            System.out.println(alignmentCount + " words aligned.");
//
//            return result;
//        }

    static HashMap<Production, HashMap<Production, Double>> maxCache = new HashMap<>();

    /**
     * This returns a map of productions to counts. These are counts over the entire training corpus. These are all possible
     * productions seen in training data. If a production does not show up in training, it will not be seen here.
     *
     * normalization parameter decides if it is normalized (typically not).
     *
     * @param maxSubstringLength1
     * @param maxSubstringLength2
     * @param examples
     * @param probs
     * @param weightingMode
     * @param normalization
     * @param getExampleCounts
     * @return
     */
     static HashMap<Production, Double> MakeRawAlignmentTable(int maxSubstringLength1, int maxSubstringLength2, List<Triple<String, String, Double>> examples, HashMap<Production, Double> probs, WeightingMode weightingMode, WikiTransliteration.NormalizationMode normalization, boolean getExampleCounts) {
        InternDictionary<String> internTable = new InternDictionary<>();
        HashMap<Production, Double> counts = new HashMap<>();

        List<List<Pair<Production, Double>>> exampleCounts = (getExampleCounts ? new ArrayList<List<Pair<Production, Double>>>(examples.size()) : null);

        int alignmentCount = 0;
        for (Triple<String, String, Double> example : examples) {
            String sourceWord = example.getFirst();
            String bestWord = example.getSecond(); // bestWord? Shouldn't it be target word?
            if (sourceWord.length() * maxSubstringLength2 >= bestWord.length() && bestWord.length() * maxSubstringLength1 >= sourceWord.length()) {
                alignmentCount++;

                HashMap<Production, Double> wordCounts;

                if (weightingMode == WeightingMode.FindWeighted && probs != null)
                    wordCounts = WikiTransliteration.FindWeightedAlignments(sourceWord, bestWord, maxSubstringLength1, maxSubstringLength2, probs, internTable, normalization);
                    //wordCounts = WikiTransliteration.FindWeightedAlignmentsAverage(sourceWord, bestWord, maxSubstringLength1, maxSubstringLength2, probs, internTable, true, normalization);
                else if (weightingMode == WeightingMode.CountWeighted)
                    wordCounts = WikiTransliteration.CountWeightedAlignments(sourceWord, bestWord, maxSubstringLength1, maxSubstringLength2, probs, internTable, normalization, false);
                else if (weightingMode == WeightingMode.MaxAlignment) {

                    HashMap<Production, Double> cached = new HashMap<>();
                    Production p = new Production(sourceWord, bestWord);
                    if(maxCache.containsKey(p)){
                        cached = maxCache.get(p);
                    }

                    Dictionaries.AddTo(probs, cached, -1.);

                    wordCounts = WikiTransliteration.CountMaxAlignments(sourceWord, bestWord, maxSubstringLength1, probs, internTable, false);
                    maxCache.put(new Production(sourceWord, bestWord), wordCounts);

                    Dictionaries.AddTo(probs, cached, 1);
                } else if (weightingMode == WeightingMode.MaxAlignmentWeighted)
                    wordCounts = WikiTransliteration.CountMaxAlignments(sourceWord, bestWord, maxSubstringLength1, probs, internTable, true);
                else {//if (weightingMode == WeightingMode.None || weightingMode == WeightingMode.SuperficiallyWeighted)
                    // This executes if probs is null
                    wordCounts = WikiTransliteration.FindAlignments(sourceWord, bestWord, maxSubstringLength1, maxSubstringLength2, internTable, normalization);
                }

                if (weightingMode == WeightingMode.SuperficiallyWeighted && probs != null) {
                    wordCounts = SumNormalize(Dictionaries.MultiplyDouble(wordCounts, probs));
                }

                Dictionaries.AddTo(counts, wordCounts, example.getThird());

                if (getExampleCounts) {
                    List<Pair<Production, Double>> curExampleCounts = new ArrayList<>(wordCounts.size());
                    for (Production key : wordCounts.keySet()) {
                        Double value = wordCounts.get(key);
                        curExampleCounts.add(new Pair<>(key, value));
                    }

                    exampleCounts.add(curExampleCounts);
                }
            } else if (getExampleCounts) {
                exampleCounts.add(null);
            }
        }

        return counts;
    }

//        public static ContextModel LearnContextModel(List<Triple<String, String, double>> examples, ContextModel model)
//        {
//            int alignmentCount = 0;
//            ContextModel result = new ContextModel();
//            result.segContextSize = model.segContextSize;
//            result.productionContextSize = model.productionContextSize;
//            result.maxSubstringLength = model.maxSubstringLength;
//
//            WikiTransliteration.ExampleCounts totals = new WikiTransliteration.ExampleCounts();
//            totals.counts = new SparseDoubleVector<Pair<Triple<String,String,String>,String>>();
//            totals.notSegCounts = new SparseDoubleVector<Pair<String,String>>();
//            totals.segCounts = new SparseDoubleVector<Pair<String,String>>();
//
//            for (Triple<String, String, double> example in examples)
//            {
//                String sourceWord = example.x;
//                String bestWord = example.y;
//                if (sourceWord.Length * model.maxSubstringLength >= bestWord.Length && bestWord.Length * model.maxSubstringLength >= sourceWord.Length)
//                {
//                    WikiTransliteration.ExampleCounts exampleCounts = WikiTransliteration.CountWeightedAlignments2(model.productionContextSize, model.segContextSize, sourceWord, bestWord, model.maxSubstringLength, model.maxSubstringLength, model.productionProbs, model.segProbs);
//                    totals.counts += exampleCounts.counts;
//                    totals.segCounts += exampleCounts.segCounts;
//                    totals.notSegCounts += exampleCounts.notSegCounts;
//                }
//            }
//
//            InternDictionary<String> internTable = new InternDictionary<String>();
//            result.productionProbs = CreateFallback(totals.counts, internTable);
//            result.segProbs = CreateFallback(totals.segCounts, totals.notSegCounts, internTable);
//
//            return result;
//        }

    public static SparseDoubleVector<Pair<Triple<String, String, String>, String>> PSecondGivenFirst(SparseDoubleVector<Pair<Triple<String, String, String>, String>> productionProbs)
    {
        SparseDoubleVector<Pair<Triple<String, String, String>, String>> result = new SparseDoubleVector<>();
        SparseDoubleVector<Triple<String, String, String>> totals = new SparseDoubleVector<>();
        for (Pair<Triple<String, String, String>, String> key : productionProbs.keySet()) {
            Double value = productionProbs.get(key);
            totals.put(key.getFirst(), totals.get(key.getFirst()) + value);
        }

        for (Pair<Triple<String, String, String>, String> key : productionProbs.keySet()) {
            Double value = productionProbs.get(key);
            result.put(key, value / totals.get(key.getFirst()));
        }

        return result;

    }

//        private static HashMap<Triple<String, String,String>, Double> MakeRawAlignmentTableWithContext(int maxSubstringLength1, int maxSubstringLength2, List<Triple<String, String, double>> examples, HashMap<Triple<String, String, String>, double> probs, int contextSize, bool fallback, bool weightByContextOnly, NormalizationMode normalization, bool getExampleCounts, out List<List<Pair<Triple<String, String, String>, double>>> exampleCounts)
//        {
//            Pasternack.Collections.Generic.Specialized.InternDictionary<String> internTable = new Pasternack.Collections.Generic.Specialized.InternDictionary<String>();
//            HashMap<Triple<String, String, String>, double> counts = new HashMap<Triple<String, String, String>, double>();
//            exampleCounts = (getExampleCounts ? new List<List<Pair<Triple<String,String, String>, double>>>(examples.Count) : null);
//
//            int alignmentCount = 0;
//            for (Triple<String, String, double> example in examples)
//            {
//                String sourceWord = example.x;
//                String bestWord = example.y;
//                if (sourceWord.Length * maxSubstringLength2 >= bestWord.Length && bestWord.Length * maxSubstringLength1 >= sourceWord.Length)
//                {
//                    alignmentCount++;
//                    HashMap<Triple<String, String, String>, double> wordCounts;
//                    wordCounts = WikiTransliteration.CountWeightedAlignmentsWithContext(contextSize, sourceWord, bestWord, maxSubstringLength1, maxSubstringLength2, probs, internTable, normalization, weightByContextOnly, fallback);
//
//                    Dictionaries.AddTo<Triple<String,String, String>>(counts, wordCounts, example.z);
//
//                    if (getExampleCounts)
//                    {
//                        List<Pair<Triple<String, String, String>, double>> curExampleCounts = new List<Pair<Triple<String, String, String>, double>>(wordCounts.Count);
//                        for (Pair<Triple<String, String, String>, double> pair in wordCounts)
//                            curExampleCounts.Add(pair);
//
//                        exampleCounts.Add(curExampleCounts);
//                    }
//                }
//                else
//                    if (getExampleCounts) exampleCounts.Add(null);
//            }
//
//            return counts;
//        }

    /**
     * Calculates a probability table for P(String2 | String1) * P(String1 | Length(String1)) == P(String2, String1 | Length(String1))
     * @param counts
     * @param maxSubstringLength
     * @return
     */
//        public static HashMap<Pair<String, String>, Double> PJointGivenLength(HashMap<Pair<String, String>, Double> counts, int maxSubstringLength)
//        {
//            double[] sums = GetSourceCountSumsByLength(counts, maxSubstringLength);
//
//            //HashMap<String, double> totals1 = WikiTransliteration.GetAlignmentTotals1(counts);
//            HashMap<Pair<String, String>, Double> result = new HashMap<Pair<String, String>, Double>(counts.size());
//            for (Pair<Pair<String, String>, Double> pair : counts)
//            {
//                double value = pair.getSecond() == 0 ? 0 : (pair.getFirst() / sums[pair.getFirst().getFirst().length()-1]);
//                result[pair.getFirst()] = value;
//            }
//
//            return result;
//        }

//        public static HashMap<String,Double> PFirstGivenLength(HashMap<Pair<String, String>, Double> counts, int maxSubstringLength)
//        {
//            double[] sums = GetSourceCountSumsByLength(counts, maxSubstringLength);
//
//            HashMap<String, Double> result = new HashMap<>();
//            for (Pair<Pair<String, String>, Double> pair : counts)
//            {
//                double value = pair.getSecond() / sums[pair.getFirst().getFirst().length() - 1];
//                Dictionaries.IncrementOrSet<String>(result, pair.getFirst().getFirst(), value, value);
//            }
//
//            return result;
//        }

    /**
     * Gets an array of totals of counts of the source substring by length.
     * @param counts
     * @param maxSubstringLength
     * @return
     */
//        public static double[] GetSourceCountSumsByLength(HashMap<Pair<String, String>, Double> counts, int maxSubstringLength)
//        {
//            double[] result = new double[maxSubstringLength];
//
//            for (Pair<Pair<String, String>, Double> pair : counts)
//                result[pair.Key.x.Length - 1] += pair.Value;
//
//            return result;
//        }

//        public static SparseDoubleVector<Pair<Triple<String, String, String>, String>> CreateFallback(SparseDoubleVector<Pair<Triple<String, String, String>, String>> counts, InternDictionary<String> internTable)
//        {
//            SparseDoubleVector<Pair<Triple<String, String, String>, String>> result = new SparseDoubleVector<>();
//            for (Pair<Pair<Triple<String, String, String>, String>, Double> pair : counts)
//            {
//                for (int i = 0; i <= pair.Key.x.x.Length; i++)
//                {
//                    result[new Pair<Triple<String,String,String>,String>(
//                        new Triple<String,String,String>(
//                            internTable.Intern(pair.Key.x.x.Substring(i)),
//                            internTable.Intern(pair.Key.x.y),
//                            internTable.Intern(pair.Key.x.z.Substring(0,pair.Key.x.z.Length-i))),pair.Key.y)] += pair.Value;
//                }
//            }
//
//            return result;
//        }

    //Create fallback for segmentation model
//        public static SparseDoubleVector<Pair<String, String>> CreateFallback(SparseDoubleVector<Pair<String, String>> segCounts, SparseDoubleVector<Pair<String, String>> notSegCounts, InternDictionary<String> internTable)
//        {
//            SparseDoubleVector<Pair<String, String>> segTotals = new SparseDoubleVector<Pair<String,String>>(segCounts.Count);
//            SparseDoubleVector<Pair<String, String>> notSegTotals = new SparseDoubleVector<Pair<String, String>>(notSegCounts.Count);
//
//            for (Pair<Pair<String,String>, Double> pair : segCounts)
//            {
//                for (int i = 0; i <= pair.getFirst().getFirst().length(); i++)
//                {
//                    String left = internTable.Intern(pair.getFirst().getFirst().substring(i) );
//                    String right = internTable.Intern(pair.getFirst().getSecond().substring(0, pair.getFirst().getSecond().length() - i));
//                    Pair<String,String> contextPair = new Pair<>(left, right);
//                    segTotals[contextPair] += pair.getSecond();
//                    notSegTotals[contextPair] += notSegCounts[pair.getFirst()];
//                }
//            }
//
//            return segTotals / (segTotals + notSegTotals);
//        }

    /**
     * Calculates a probability table for P(String2 | String1)
     * @param counts
     * @return
     */
    public static SparseDoubleVector<Pair<Triple<String, String, String>, String>> PSecondGivenFirstTriple(SparseDoubleVector<Pair<Triple<String, String, String>, String>> counts) {
        SparseDoubleVector<Triple<String, String, String>> totals = new SparseDoubleVector<>();
        for (Pair<Triple<String, String, String>, String> key : counts.keySet()) {
            Double value = counts.get(key);
            totals.put(key.getFirst(), totals.get(key.getFirst()) + value);
        }

        SparseDoubleVector<Pair<Triple<String, String, String>, String>> result = new SparseDoubleVector<Pair<Triple<String, String, String>, String>>(counts.size());
        for (Pair<Triple<String, String, String>, String> key : counts.keySet())
        {
            Double value = counts.get(key);
            double total = totals.get(key.getFirst());
            result.put(key, total == 0 ? 0 : value / total);
        }

        return result;
    }

    /**
     * Returns a random subset of a list; the list provided is modified to remove the selected items.
     *
     * @param wordList
     * @param count
     * @param seed
     * @return
     */
    public static List<Pair<String, String>> GetRandomPartOfList(List<Pair<String, String>> wordList, int count, int seed) {
        Random r = new Random(seed);

        List<Pair<String, String>> randomList = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {

            int index = r.nextInt(wordList.size()); //r.Next(wordList.size());
            randomList.add(wordList.get(index));
            wordList.remove(index);
        }

        return randomList;
    }

    public static List<String> GetListValues(List<Pair<String, String>> wordList) {
        List<String> valueList = new ArrayList<>(wordList.size());
        for (Pair<String, String> pair : wordList)
            valueList.add(pair.getSecond());

        return valueList;
    }

    //
//        public static List<Pair<String, String>> InvertPairs(List<Pair<String, String>> pairs)
//        {
//            List<Pair<String, String>> result = new ArrayList<>(pairs.size());
//            for (Pair<String, String> pair : pairs)
//                result.add(new Pair<String, String>(pair.getSecond(), pair.getFirst()));
//
//            return result;
//        }
//
//        public static void ContextDiscoveryTest(List<String> candidateWords, List<Pair<String, String>> trainingPairs, HashMap<String, List<String>> testingPairs, int maxSubstringLength, int productionContextSize, int segContextSize)
//        {
//            double minProductionProbability = 0.000000000000001;
//            //double minProductionProbability = 0.00000001;
//
//            List<Triple<String, String, Double>> trainingTriples = ConvertExamples(trainingPairs);
//
//            WikiTransliteration.ContextModel model = LearnContextModel(trainingTriples, new WikiTransliteration.ContextModel());
//
//            for (int i = 0; i < 2000; i++)
//            {
//                System.out.println("Iteration #" + i);
//            }
//
//            System.out.println("Finished.");
//        }
//
//
//
        static double Choose(double n, double k)
        {
            double result = 1;

            for (double i = Math.max(k, n - k) + 1; i <= n; ++i)
                result *= i;

            for (double i = 2; i <= Math.min(k, n - k); ++i)
                result /= i;

            return result;
        }

        public static double[][] SegmentationCounts(int maxLength)
        {
            double[][] result = new double[maxLength][];
            for (int i = 0; i < maxLength; i++)
            {
                result[i] = new double[i+1];
                for (int j = 0; j <= i; j++)
                    result[i][j] = Choose(i, j);
            }

            return result;
        }

        public static double[][] SegSums(int maxLength)
        {
            double[][] segmentationCounts = SegmentationCounts(maxLength);
            double[][] result = new double[maxLength][];
            for (int i = 0; i < maxLength; i++)
            {
                result[i] = new double[maxLength];
                for (int j = 0; j < maxLength; j++)
                {
                    int minIJ = Math.min(i, j);
                    for (int k = 0; k <= minIJ; k++)
                        result[i][j] += segmentationCounts[i][k] * segmentationCounts[j][k];// *Math.Pow(0.5, k + 1);
                }
            }

            return result;
        }

        public static double[][] segSums = SegSums(40);
//
//        public static void DiscoveryTest(List<String> candidateWords, List<Pair<String, String>> trainingPairs, HashMap<String, List<String>> testingPairs, int maxSubstringLength1, int maxSubstringLength2)
//        {
//            double minProductionProbability = 0.000000000000001;
//            //double minProductionProbability = 0.00000001;
//
//            List<Triple<String, String, double>> trainingTriples = ConvertExamples(trainingPairs);
//
//            List<List<Pair<Pair<String, String>, double>>> exampleCounts;
//
//            SparseDoubleVector<Pair<String, String>> probs = MakeRawAlignmentTable(maxSubstringLength1, maxSubstringLength2, trainingTriples, null, WeightingMode.None, NormalizationMode.None, false, out exampleCounts);
//            //probs = MultiPair(languageModel,probs);
//
//            EvaluateExamples(testingPairs, candidateWords, PSecondGivenFirst(probs), maxSubstringLength1, true, minProductionProbability);
//
//            for (int i = 0; i < 2000; i++)
//            {
//                //probs = MakeRawAlignmentTable(maxSubstringLength1, maxSubstringLength2, trainingTriples, (SparseDoubleVector<Pair<String, String>>)PSecondGivenFirst(probs)*.5, WeightingMode.CountWeighted, NormalizationMode.None, true, out exampleCounts);
//                probs = MakeRawAlignmentTable(maxSubstringLength1, maxSubstringLength2, trainingTriples, PSecondGivenFirst(probs), WeightingMode.CountWeighted, NormalizationMode.None, true, out exampleCounts);
//                //probs += (0.00001*probs.PNorm(1));
//                //probs = MultiPair(languageModel.Sign(), probs);
//
//                System.out.println("Iteration #" + i);
//                {
//                    //EvaluateExamples(testingPairs, candidateWords, (SparseDoubleVector<Pair<String, String>>)PSecondGivenFirst(probs)*.5, maxSubstringLength1, true, minProductionProbability);
//                    //EvaluateExamples(testingPairs, candidateWords, (SparseDoubleVector<Pair<String, String>>)PSecondGivenFirst(probs) * .5, maxSubstringLength1, false, minProductionProbability);
//                    EvaluateExamples(testingPairs, candidateWords, PSecondGivenFirst(probs), maxSubstringLength1, true, minProductionProbability);
//                    //EvaluateExamples(testingPairs, candidateWords, PSecondGivenFirst(probs), maxSubstringLength1, false, minProductionProbability);
//                }
//
//                //Reweight(probs, trainingTriples, exampleCounts, maxSubstringLength1);
//            }
//
//            System.out.println("Finished.");
//        }
//
    public static void DiscoveryEM(int iterations, List<String> candidateWords, List<Pair<String, String>> trainingPairs, HashMap<String, List<String>> testingPairs, TransliterationModel model) {
        List<Triple<String, String, Double>> trainingTriples = ConvertExamples(trainingPairs);

        for (int i = 0; i < iterations; i++) {
            System.out.println("Iteration #" + i);

            long startTime = System.nanoTime();
            System.out.print("Training...");
            model = model.LearnModel(trainingTriples);
            long endTime = System.nanoTime();
            System.out.println("Finished in " + (startTime - endTime) / (1000000 * 1000) + " seconds.");

            DiscoveryEvaluation(testingPairs, candidateWords, model);
        }

        System.out.println("Finished.");
    }

    //
//        public static SparseDoubleVector<Pair<Triple<String, String, String>, String>> LiftProbs(SparseDoubleVector<Pair<String, String>> probs)
//        {
//            SparseDoubleVector<Pair<Triple<String, String, String>, String>> result = new SparseDoubleVector<Pair<Triple<String, String, String>, String>>(probs.Count);
//            for (Pair<Pair<String, String>, double> pair : probs)
//                result.Add(new Pair<Triple<String, String, String>, String>(new Triple<String, String, String>("", pair.Key.x, ""), pair.Key.y), pair.Value);
//
//            return result;
//        }
//
//        public static SparseDoubleVector<Triple<String, String, String>> LiftSegProbs(SparseDoubleVector<String> probs)
//        {
//            SparseDoubleVector<Triple<String, String, String>> result = new SparseDoubleVector<Triple<String,String,String>>(probs.Count);
//            for (Pair<String, double> pair in probs)
//                result.Add(new Triple<String, String, String>("", pair.Key, ""), pair.Value);
//
//            return result;
//        }
//
//        public static SparseDoubleVector<Pair<String, String>> InvertProbs(SparseDoubleVector<Pair<String, String>> probs)
//        {
//            SparseDoubleVector<Pair<String, String>> result = new SparseDoubleVector<Pair<String, String>>(probs.Count);
//            for (Pair<Pair<String, String>, double> pair in probs)
//                result.Add(new Pair<String, String>(pair.Key.y, pair.Key.x), pair.Value);
//            return result;
//        }
//
//        public static void DiscoveryGenerationTest(List<String> candidateWords, List<Pair<String, String>> trainingPairs, HashMap<String, List<String>> testingPairs, int maxSubstringLength1, int maxSubstringLength2)
//        {
//            double minProductionProbability = 0.000000000000001;
//            //double minProductionProbability = 0.00000001;
//
//            List<Triple<String, String, Double>> trainingTriples = ConvertExamples(trainingPairs);
//
//            SparseDoubleVector<String> possibleProductions = new SparseDoubleVector<String>();
//            for (String s : candidateWords)
//            {
//                for (int i = 0; i < s.length(); i++)
//                    for (int j = 1; i + j <= s.length(); j++)
//                        possibleProductions[s.substring(i, j)] = 1;
//            }
//
//            List<List<Pair<Pair<String, String>, Double>>> exampleCounts;
//
//            SparseDoubleVector<Pair<String, String>> probs = MakeRawAlignmentTable(maxSubstringLength1, maxSubstringLength2, trainingTriples, null, WeightingMode.None, NormalizationMode.None, false, out exampleCounts);
//
//            for (int i = 0; i < 2000; i++)
//            {
//                probs = MakeRawAlignmentTable(maxSubstringLength1, maxSubstringLength2, trainingTriples, PSecondGivenFirst(probs), WeightingMode.CountWeighted, NormalizationMode.None, true, out exampleCounts);
//
//                System.out.println("Iteration #" + i);
//                {
//                    SparseDoubleVector<Pair<String, String>> probs2 = MultiPair2(possibleProductions, PSecondGivenFirst(probs));
//                    probs2.RemoveRedundantElements();
//
//                    //EvaluateExamples(testingPairs, candidateWords, (SparseDoubleVector<Pair<String, String>>)PSecondGivenFirst(probs)*.5, maxSubstringLength1, true, minProductionProbability);
//                    //EvaluateExamples(testingPairs, candidateWords, (SparseDoubleVector<Pair<String, String>>)PSecondGivenFirst(probs) * .5, maxSubstringLength1, false, minProductionProbability);
//                    EvaluateExamplesDiscoveryGeneration(testingPairs, candidateWords, probs2, maxSubstringLength1, true, 100);
//                    //EvaluateExamplesDual(testingPairs, candidateWords, PSecondGivenFirst(probs), PSecondGivenFirst(probsDual), maxSubstringLength1, true, minProductionProbability);
//                    //EvaluateExamplesDual(testingPairs, candidateWords, PSecondGivenFirst(probs), PSecondGivenFirst(probsDual), maxSubstringLength1, true, minProductionProbability, 0);
//                    //EvaluateExamples(testingPairs, candidateWords, PSecondGivenFirst(probs)* InvertProbs( PSecondGivenFirst(probsDual) ), maxSubstringLength1, true, minProductionProbability*minProductionProbability);
//                }
//
//                //Reweight(probs, trainingTriples, exampleCounts, maxSubstringLength1);
//            }
//
//            System.out.println("Finished.");
//        }
//
//        public static void DiscoveryTestDual(List<String> candidateWords, List<Pair<String, String>> trainingPairs, HashMap<String,List<String>> testingPairs, int maxSubstringLength1, int maxSubstringLength2)
//        {
//            double minProductionProbability = 0.000000000000001;
//            //double minProductionProbability = 0.00000001;
//
//            List<Triple<String, String, double>> trainingTriples = ConvertExamples(trainingPairs);
//            List<Triple<String, String, double>> trainingTriplesDual = ConvertExamples( InvertPairs( trainingPairs ));
//
//            List<List<Pair<Pair<String, String>, double>>> exampleCounts;
//
//            SparseDoubleVector<Pair<String, String>> probs = MakeRawAlignmentTable(maxSubstringLength1, maxSubstringLength2, trainingTriples, null, WeightingMode.None, NormalizationMode.None, false, out exampleCounts);
//            SparseDoubleVector<Pair<String, String>> probsDual = MakeRawAlignmentTable(maxSubstringLength1, maxSubstringLength2, trainingTriplesDual, null, WeightingMode.None, NormalizationMode.None, false, out exampleCounts);
//            //probs = MultiPair(languageModel,probs);
//
//            for (int i = 0; i < 2000; i++)
//            {
//                //probs = MakeRawAlignmentTable(maxSubstringLength1, maxSubstringLength2, trainingTriples, (SparseDoubleVector<Pair<String, String>>)PSecondGivenFirst(probs)*.5, WeightingMode.CountWeighted, NormalizationMode.None, true, out exampleCounts);
//                probs = MakeRawAlignmentTable(maxSubstringLength1, maxSubstringLength2, trainingTriples, PSecondGivenFirst(probs), WeightingMode.CountWeighted, NormalizationMode.None, true, out exampleCounts);
//                probsDual = MakeRawAlignmentTable(maxSubstringLength1, maxSubstringLength2, trainingTriplesDual, PSecondGivenFirst(probsDual), WeightingMode.CountWeighted, NormalizationMode.None, true, out exampleCounts);
//                //probs += (0.00001*probs.PNorm(1));
//                //probs = MultiPair(languageModel.Sign(), probs);
//
//                System.out.println("Iteration #" + i);
//                {
//                    //if (i < 9) continue;
//
//                    //EvaluateExamples(testingPairs, candidateWords, (SparseDoubleVector<Pair<String, String>>)PSecondGivenFirst(probs)*.5, maxSubstringLength1, true, minProductionProbability);
//                    //EvaluateExamples(testingPairs, candidateWords, (SparseDoubleVector<Pair<String, String>>)PSecondGivenFirst(probs) * .5, maxSubstringLength1, false, minProductionProbability);
//
//                    //EvaluateExamplesDual(testingPairs, candidateWords, PSecondGivenFirst(probs), PSecondGivenFirst(probsDual), maxSubstringLength1, true, minProductionProbability);
//                    EvaluateExamplesDual(testingPairs, candidateWords, PSecondGivenFirst(probs), PSecondGivenFirst(probsDual), maxSubstringLength1, true, minProductionProbability,0);
//                    //EvaluateExamples(testingPairs, candidateWords, PSecondGivenFirst(probs)* InvertProbs( PSecondGivenFirst(probsDual) ), maxSubstringLength1, true, minProductionProbability*minProductionProbability);
//                }
//
//                //Reweight(probs, trainingTriples, exampleCounts, maxSubstringLength1);
//            }
//
//            System.out.println("Finished."); Console.ReadLine();
//        }
//
//        public static double ProbSum(HashMap<Pair<String, String>, double> probs)
//        {
//            double result = 0;
//            for (Pair<Pair<String, String>, double> pair in probs)
//                result += pair.Value;
//
//            return result;
//        }
//
//        public static HashMap<Pair<String, String>, double> PlusNSmoothing(HashMap<Pair<String, String>, double> probs, double n)
//        {
//            HashMap<Pair<String, String>, double> result = new HashMap<Pair<String, String>, double>(probs.Count);
//            for (Pair<Pair<String, String>, double> pair in probs)
//                result[pair.Key] = pair.Value + n;
//
//            return result;
//        }
//
//        public static void TestXMLData(List<Pair<String, String>> trainingPairs, List<Pair<String, String>> testingPairs, int maxSubstringLength1, int maxSubstringLength2)
//        {
//            bool capitalizeFirst = false;
//            //Console
//            int ngramSize = 4;
//            //List<Pair<String, String>> trainingPairs = FilterExamplePairs(GetTaskPairs(trainingFile));
//
//            if (capitalizeFirst) trainingPairs = UppercaseFirstSourceLetter(trainingPairs);
//
//            List<Triple<String, String, double>> trainingTriples = ConvertExamples(trainingPairs);
//            //List<Pair<String, String>> testingPairs = FilterExamplePairs(GetTaskPairs(testFile));
//
//            if (capitalizeFirst) testingPairs = UppercaseFirstSourceLetter(testingPairs);
//
//            List<String> trainingWords = new List<String>(trainingPairs.Count);
//            for (Pair<String, String> pair in trainingPairs)
//                trainingWords.Add(pair.Value);
//            HashMap<String, double> languageModel = new HashMap<String, double>(); //GetNgramCounts(trainingPairs, maxSubstringLength1);
//            Dictionaries.AddTo<String>(languageModel, GetNgramCounts(testingPairs, maxSubstringLength1),1); // null; // UniformLanguageModel(WikiTransliteration.GetNgramCounts(3, ngramSize, trainingWords, false));
//
//            List<List<Pair<Pair<String, String>, double>>> exampleCounts;
//
//
//
//            SparseDoubleVector<Pair<String, String>> probs = MakeRawAlignmentTable(maxSubstringLength1, maxSubstringLength2, trainingTriples, null, WeightingMode.None,NormalizationMode.None,false,out exampleCounts);
//            //EvaluateExamples(testingPairs, PSecondGivenFirst(probs), maxSubstringLength1, languageModel, ngramSize, false);
//
//            //EvaluateExamples(testingPairs, PSecondGivenFirst(probs), maxSubstringLength1, null, ngramSize, 20, false, 100);
//            //EvaluateExamples(testingPairs, PSecondGivenFirst(probs), maxSubstringLength1, null, ngramSize, 20, false, 10);
//            //EvaluateExamples(testingPairs, PSecondGivenFirst(probs), maxSubstringLength1, null, ngramSize, 20, true, 0);
//
//            for (int i = 0; i < 20; i++)
//            {
//                //probs = MakeRawAlignmentTable(maxSubstringLength1, maxSubstringLength2, trainingTriples, PMutualProduction(probs), true,NormalizationMode.AllProductions, true, out exampleCounts);
//                probs = MakeRawAlignmentTable(maxSubstringLength1, maxSubstringLength2, trainingTriples, (SparseDoubleVector<Pair<String,String>>)PSecondGivenFirst(probs)*0.5, WeightingMode.CountWeighted, NormalizationMode.None, true, out exampleCounts);
//                //if (probs == null) break;
//                //if (i>0)
//
//                System.out.println("Iteration #" + i);
//                //if (i > 2)
//                {
//                    //EvaluateExamples(testingPairs, PSecondGivenFirst(probs), maxSubstringLength1, null, ngramSize, 20, false, 200);
//                    EvaluateExamples(testingPairs, (SparseDoubleVector<Pair<String, String>>)PSecondGivenFirst(probs)*0.5, maxSubstringLength1, null, ngramSize, 20, false, 100);
//                    //EvaluateExamples(testingPairs, (SparseDoubleVector<Pair<String, String>>)PSecondGivenFirst(probs)*1, maxSubstringLength1, null, ngramSize, 20, false, 10);
//                    EvaluateExamples(testingPairs, (SparseDoubleVector<Pair<String, String>>)PSecondGivenFirst(probs)*0.5, maxSubstringLength1, null, ngramSize, 20, true, 0);
//                    //EvaluateExamples(testingPairs, LiftProbs(PSecondGivenFirst(probs)), maxSubstringLength1, 1, 0, false);
//                    //EvaluateExamples(testingPairs, PSecondGivenFirst(probs), maxSubstringLength1, languageModel, ngramSize, true);
//                }
//
//                //Reweight(probs, trainingTriples, exampleCounts, maxSubstringLength1);
//            }
//
//            System.out.println("Finished.");
//
//            //HashMap<String, Pair<String, double>> maxProbs = WikiTransliteration.GetMaxProbs(probs);
//
//            //List<Triple<String, String, double>> weightedTrainingPairs = new List<Triple<String, String, double>>(trainingPairs.Count);
//            //for (Pair<String, String> trainingPair in trainingPairs)
//            //{
//            //    int position = WikiTransliteration.Predict(20, trainingPair.Key, maxSubstringLength, maxProbs, new HashMap<String, TopList<double, String>>()).IndexOfValue(trainingPair.Value);
//            //    if (position >= 0) position++; else position = 20;
//            //    weightedTrainingPairs.Add(new Triple<String, String, double>(trainingPair.Key, trainingPair.Value, position >= 0 ? ((double)1) / position : 0));
//            //}
//
//            //probs = MakeAlignmentTable(maxSubstringLength, weightedTrainingPairs, probs, true);
//
//
//            //maxProbs = WikiTransliteration.GetMaxProbs(probs);
//            //Map<String, String> probMap = WikiTransliteration.GetProbMap(probs);
//
//            #region High-performing code
//            //5977 words aligned.
//            //5977 words aligned.
//            //5977 words aligned.
//            //943 pairs tested in total.
//            //737 predictions contained (0.781548250265111)
//            //586 predictions exactly correct (0.621420996818664)
//            //MRR: 0.684359313795249
//
//            //HashMap<Pair<String, String>, double> probs = MakeAlignmentTable(maxSubstringLength, trainingPairs, null);
//            //HashMap<String, Pair<String, double>> maxProbs = WikiTransliteration.GetMaxProbs(probs);
//            //Map<String, String> probMap = WikiTransliteration.GetProbMap(probs);
//
//            //List<Triple<String, String, double>> weightedTrainingPairs = new List<Triple<String, String, double>>(trainingPairs.Count);
//            ////for (Pair<String,String> trainingPair in trainingPairs)
//            ////    weightedTrainingPairs.Add(new Triple<String, String, double>(trainingPair.Key, trainingPair.Value, Math.Pow(WikiTransliteration.GetAlignmentProbability(trainingPair.Key, trainingPair.Value, maxSubstringLength, probs, 0), ((double)1)/trainingPair.Value.Length ) ));
//
//            //for (Pair<String, String> trainingPair in trainingPairs)
//            //{
//            //    int position = WikiTransliteration.Predict(20, trainingPair.Key, maxSubstringLength, maxProbs, new HashMap<String, TopList<double, String>>()).IndexOfValue(trainingPair.Value);
//            //    if (position >= 0) position++; else position = 20;
//            //    weightedTrainingPairs.Add(new Triple<String, String, double>(trainingPair.Key, trainingPair.Value, position >= 0 ? ((double)1)/position : 0));
//            //}
//
//            //probs = MakeAlignmentTable(maxSubstringLength, weightedTrainingPairs, null);
//            //probs = MakeAlignmentTable(maxSubstringLength, weightedTrainingPairs, probs);
//            #endregion
//
//
//            Console.ReadLine();
//        }
//
//        public static void TestXMLDataWithContext(String trainingFile, String testFile, int maxSubstringLength, int contextSize, bool fallback)
//        {
//            bool capitalizeFirst = false;
//
//            List<Pair<String, String>> trainingPairs = FilterExamplePairs(GetTaskPairs(trainingFile));
//
//            if (capitalizeFirst) trainingPairs = UppercaseFirstSourceLetter(trainingPairs);
//
//            List<Triple<String, String, double>> trainingTriples = ConvertExamples(trainingPairs);
//            List<Pair<String, String>> testingPairs = FilterExamplePairs(GetTaskPairs(testFile));
//
//            if (capitalizeFirst) testingPairs = UppercaseFirstSourceLetter(testingPairs);
//
//            List<String> trainingWords = new List<String>(trainingPairs.Count);
//            for (Pair<String, String> pair in trainingPairs)
//                trainingWords.Add(pair.Value);
//
//            List<List<Pair<Triple<String, String, String>, double>>> exampleCounts;
//
//            HashMap<Triple<String, String, String>, double> probs = MakeRawAlignmentTableWithContext(maxSubstringLength, maxSubstringLength, trainingTriples, null, contextSize, fallback, false, NormalizationMode.BySourceSubstring, false, out exampleCounts);
//            EvaluateExamples(testingPairs, Dictionaries.Multiply<Triple<String,String,String>>(PSecondGivenFirst(probs),PFirstGivenSecond(probs)), maxSubstringLength,1,contextSize,fallback);
//
//            for (int i = 1; i < 50; i++)
//            {
//                //probs = MakeRawAlignmentTableWithContext(maxSubstringLength, maxSubstringLength, trainingTriples, Dictionaries.Multiply<Triple<String, String, String>>(PSecondGivenFirst(probs), PFirstGivenSecond(probs)), contextSize, fallback, false, NormalizationMode.BySourceSubstring, false, out exampleCounts);
//                probs = MakeRawAlignmentTableWithContext(maxSubstringLength, maxSubstringLength, trainingTriples, (probs), contextSize, fallback, false, NormalizationMode.BySourceSubstring, false, out exampleCounts);
//
//                System.out.println("Iteration #" + i);
//                //EvaluateExamples(testingPairs, Dictionaries.Multiply<Triple<String, String, String>>(PSecondGivenFirst(probs), PFirstGivenSecond(probs)), maxSubstringLength, 1, contextSize, fallback);
//                EvaluateExamples(testingPairs, PSecondGivenFirst(probs), maxSubstringLength, 1, contextSize, fallback);
//            }
//
//            System.out.println("Finished.");
//
//            Console.ReadLine();
//        }
//
//
//        private static String PositionalizeString(String word)
//        {
//            char[] chars = new char[word.Length];
//            for (int i = 0; i < word.Length; i++)
//                chars[i] = (char)((int)word[i] + (i << 8));
//
//            return new String(chars);
//        }
//
//        public static List<Pair<String, String>> UppercaseFirstSourceLetter(List<Pair<String, String>> pairs)
//        {
//            List<Pair<String, String>> result = new List<Pair<String, String>>(pairs.Count);
//
//            for (int i = 0; i < pairs.Count; i++)
//                result.Add( new Pair<String, String>(Char.ToUpper(pairs[i].Key[0]) + pairs[i].Key.Substring(1), pairs[i].Value) );
//                //result.Add(new Pair<String, String>(PositionalizeString(pairs[i].Key), pairs[i].Value));
//
//            return result;
//        }
//
    public static String NormalizeHebrew(String word) {
        word = word.replace('ן', 'נ');
        word = word.replace('ך', 'כ');
        word = word.replace('ץ', 'צ');
        word = word.replace('ם', 'מ');
        word = word.replace('ף', 'פ');

        return word;
    }

    public static List<Pair<String, String>> NormalizeHebrew(List<Pair<String, String>> pairs) {
        List<Pair<String, String>> result = new ArrayList<>(pairs.size());

        for (int i = 0; i < pairs.size(); i++)
            result.add(new Pair<>(pairs.get(i).getFirst(), NormalizeHebrew(pairs.get(i).getSecond())));

        return result;
    }

    /**
     * This is still broken...
     *
     * @param path
     * @return
     * @throws FileNotFoundException
     */
    static HashMap<String, List<String>> GetAlexData(String path) throws FileNotFoundException {

        HashMap<String, List<String>> result = new HashMap<>();

        // TODO: this is all broken.
//            ArrayList<String> data = LineIO.read(path);
//
//            for (String line : data)
//            {
//                if (line.length() == 0) continue;
//
//                Match match = Regex.Match(line, "(?<eng>\\w+)\t(?<rroot>\\w+)(?: {(?:-(?<rsuf>\\w*?)(?:(?:, )|}))+)?", RegexOptions.Compiled);
//
//                String russianRoot = match.Groups["rroot"].Value;
//                if (russianRoot.length() == 0)
//                    System.out.println("Parse error");
//
//                List<String> russianList = new ArrayList<>();
//
//                //if (match.Groups["rsuf"].Captures.Count == 0)
//                    russianList.Add(russianRoot.toLower()); //root only
//                //else
//                    for (Capture capture : match.Groups["rsuf"].Captures)
//                        russianList.add((russianRoot + capture.Value).ToLower());
//
//                result[match.Groups["eng"].Value.ToLower()] = russianList;
//
//            }

        return result;
    }

    //
//        public static List<Pair<String, String>> UnderscoreSurround(List<Pair<String, String>> pairs)
//        {
//            List<Pair<String, String>> result = new List<Pair<String, String>>(pairs.Count);
//
//            for (int i = 0; i < pairs.Count; i++)
//                result.Add(new Pair<String, String>(Char.ToUpper(pairs[i].Key[0]) + pairs[i].Key.Substring(1, pairs[i].Key.Length - 2) + ((char)(pairs[i].Key[pairs[i].Key.Length - 1]+ ((char)500))), pairs[i].Value));
//            //result.Add(new Pair<String, String>(PositionalizeString(pairs[i].Key), pairs[i].Value));
//
//            return result;
//        }
//
//        public static void TestXMLDataOldOldStyleForTask(String trainingFile, String secondTrainingFile, String testFile, int maxSubstringLength1, int maxSubstringLength2)
//        {
//            //Console
//            int ngramSize = 3;
//            List<Pair<String, String>> trainingPairs = (FilterExamplePairs(GetTaskPairs(trainingFile)));
//            List<String> testingWords = null;
//            List<Pair<String, String>> testingPairs = null;
//
//            if (secondTrainingFile != null)
//            {
//                trainingPairs.AddRange(FilterExamplePairs(GetTaskPairs(secondTrainingFile)));
//                testingWords = FilterExampleWords(GetTaskWords(testFile));
//            }
//            else
//            {
//                testingPairs = (FilterExamplePairs(GetTaskPairs(testFile)));
//            }
//
//            List<Triple<String, String, double>> trainingTriples = ConvertExamples(trainingPairs);
//
//            List<String> trainingWords = new List<String>(trainingPairs.Count);
//            for (Pair<String, String> pair in trainingPairs)
//                trainingWords.Add(pair.Value);
//            HashMap<String, double> languageModel = WikiTransliteration.GetNgramProbs(1, ngramSize, trainingWords);
//
//            List<List<Pair<Pair<String, String>, double>>> exampleCounts;
//
//            HashMap<Pair<String, String>, double> probs = MakeRawAlignmentTable(maxSubstringLength1, maxSubstringLength2, trainingTriples, null, WeightingMode.None, NormalizationMode.AllProductions, false, out exampleCounts);
//            //EvaluateExamples(testingPairs, PMutualProduction(probs), maxSubstringLength1, null, ngramSize, 20, false);
//            //EvaluateExamples(testingPairs, PMutualProduction(probs), maxSubstringLength1, languageModel, ngramSize, 200, true);
//
//            for (int i = 0; i < 500; i++)
//            {
//                //probs = MakeRawAlignmentTable(maxSubstringLength1, maxSubstringLength2, trainingTriples, PMutualProduction(probs), true,NormalizationMode.AllProductions, true, out exampleCounts);
//                probs = MakeRawAlignmentTable(maxSubstringLength1, maxSubstringLength2, trainingTriples, PMutualProduction(probs), WeightingMode.FindWeighted, NormalizationMode.AllProductions, false, out exampleCounts);
//                if (probs == null) break;
//
//                if (i == 4)
//                {
//                    EvaluateExamplesToFile(testingWords, PMutualProduction(probs), maxSubstringLength1, languageModel, ngramSize, 10, true, @"C:\Data\WikiTransliteration\enChTaskPredictions.xml");
//                    return;
//                }
//
//                //Reweight(probs, trainingTriples, exampleCounts, maxSubstringLength1);
//            }
//
//            System.out.println("Finished.");
//
//            Console.ReadLine();
//        }
//
//        public static void TestXMLDataNewStyle(String trainingFile, String secondTrainingFile, String testFile, int maxSubstringLength1, int maxSubstringLength2)
//        {
//            //Console
//            int ngramSize = 3;
//            List<Pair<String, String>> trainingPairs = (FilterExamplePairs(GetTaskPairs(trainingFile)));
//            List<String> testingWords = null;
//            List<Pair<String, String>> testingPairs = null;
//
//            if (secondTrainingFile != null)
//            {
//                trainingPairs.AddRange(FilterExamplePairs(GetTaskPairs(secondTrainingFile)));
//                testingWords = FilterExampleWords( GetTaskWords(testFile) );
//            }
//            else
//            {
//                testingPairs = (FilterExamplePairs(GetTaskPairs(testFile)));
//            }
//
//            List<Triple<String, String, double>> trainingTriples = ConvertExamples(trainingPairs);
//
//            List<String> trainingWords = new List<String>(trainingPairs.Count);
//            for (Pair<String, String> pair in trainingPairs)
//                trainingWords.Add(pair.Value);
//            HashMap<String, double> languageModel = null; // UniformLanguageModel(WikiTransliteration.GetNgramCounts(3, ngramSize, trainingWords, false));
//
//            List<List<Pair<Pair<String, String>, double>>> exampleCounts;
//
//            HashMap<Pair<String, String>, double> probs = MakeRawAlignmentTable(maxSubstringLength1, maxSubstringLength2, trainingTriples, null, WeightingMode.None, NormalizationMode.None, false, out exampleCounts);
//            //EvaluateExamples(testingPairs, PSecondGivenFirst(probs), maxSubstringLength1, languageModel, ngramSize, false);
//
//            for (int i = 0; i < 500; i++)
//            {
//                //probs = MakeRawAlignmentTable(maxSubstringLength1, maxSubstringLength2, trainingTriples, PMutualProduction(probs), true,NormalizationMode.AllProductions, true, out exampleCounts);
//                probs = MakeRawAlignmentTable(maxSubstringLength1, maxSubstringLength2, trainingTriples, PSecondGivenFirst(probs), WeightingMode.CountWeighted, NormalizationMode.None, true, out exampleCounts);
//                if (probs == null) break;
//                //if (i>0)
//
//                System.out.println("Iteration #" + i);
//                //if (i > 2)
//                {
//                    //EvaluateExamples(testingPairs, PSecondGivenFirst(probs), maxSubstringLength1, null, ngramSize, 20, false);
//                    //EvaluateExamples(testingPairs, PSecondGivenFirst(probs), maxSubstringLength1, languageModel, ngramSize, true);
//                }
//
//                if (i == 5)
//                {
//                    EvaluateExamplesToFile(testingWords, PSecondGivenFirst(probs), maxSubstringLength1, null, ngramSize, 10, true, @"C:\Data\WikiTransliteration\enRuTaskPredictions.xml");
//                    return;
//                }
//                //Reweight(probs, trainingTriples, exampleCounts, maxSubstringLength1);
//            }
//
//            System.out.println("Finished.");
//
//            Console.ReadLine();
//        }
//
//
//        public static void TestXMLDataLog(String trainingFile, String testFile, int maxSubstringLength1, int maxSubstringLength2)
//        {
//            List<Pair<String, String>> trainingPairs = GetTaskPairs(trainingFile);
//            List<Pair<String, String>> testingPairs = GetTaskPairs(testFile);
//
//            HashMap<Pair<String, String>, double> probs = MakeAlignmentTableLog(maxSubstringLength1, maxSubstringLength2, trainingPairs, null, false);
//            EvaluateExamplesLog(testingPairs, probs, maxSubstringLength1, true);
//
//            for (int i = 0; i < 30; i++)
//            {
//                probs = MakeAlignmentTableLog(maxSubstringLength1, maxSubstringLength2, trainingPairs, probs, true);
//                if (probs == null) break;
//                EvaluateExamplesLog(testingPairs, probs, maxSubstringLength1, true);
//            }
//
//            System.out.println("Finished.");
//
//            Console.ReadLine();
//        }
//
//        private static void EvaluateExamplesLog(List<Pair<String, String>> testingPairs, HashMap<Pair<String, String>, double> probs, int maxSubstringLength, bool tryAllPredictions)
//        {
//            //HashMap<String, Pair<String, double>> maxProbs = tryAllPredictions ? null : WikiTransliteration.GetMaxProbs(probs);
//            Map<String, String> probMap = tryAllPredictions ? WikiTransliteration.GetProbMap(probs) : null;
//
//            int correct = 0;
//            int contained = 0;
//            double mrr = 0;
//            for (Pair<String, String> pair in testingPairs)
//            {
//                TopList<double, String> predictions =
//                        WikiTransliteration.PredictLog(20, pair.Key, maxSubstringLength, probMap, probs, new HashMap<String, TopList<double, String>>(), null, 0);
//
//                int position = predictions.Values.IndexOf(pair.Value);
//                if (position == 0)
//                    correct++;
//
//                if (position >= 0)
//                    contained++;
//
//                if (position < 0)
//                    position = 20;
//
//                mrr += 1 / ((double)position + 1);
//            }
//
//            mrr /= testingPairs.Count;
//
//            System.out.println(testingPairs.Count + " pairs tested in total.");
//            System.out.println(contained + " predictions contained (" + (((double)contained) / testingPairs.Count) + ")");
//            System.out.println(correct + " predictions exactly correct (" + (((double)correct) / testingPairs.Count) + ")");
//            System.out.println("MRR: " + mrr);
//        }
//
//        private static void WriteTaskResultsHeader(StreamWriter s, String targetLanguage, String runType, String runID, String comments)
//        {
//            s.WriteLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
//            s.WriteLine("<TransliterationTaskResults");
//            s.WriteLine("SourceLang = \"English\"");
//            s.WriteLine("TargetLang = \"" + targetLanguage + "\"");
//            s.WriteLine("GroupID = \"Jeff Pasternack, University of Illinois, Urbana-Champaign\"");
//            s.WriteLine("RunID = \"" + runID + "\"");
//            s.WriteLine("RunType = \"" + runType + "\"");
//            s.WriteLine("Comments = \"" + comments + "\">");
//
//        }
//
//        private static void WriteTaskResults(StreamWriter s, int index, String sourceWord, IList<String> predictions)
//        {
//            s.WriteLine("<Name ID=\""+ (index+1) +"\">");
//            s.WriteLine("<SourceName>"+sourceWord+"</SourceName>");
//            for (int i = 0; i < predictions.Count && i < 10; i++)
//            {
//                s.WriteLine("<TargetName ID=\"" + (i+1) + "\">" + predictions[i] + "</TargetName>");
//            }
//
//            s.WriteLine("</Name>");
//        }
//
//        private static void WriteTaskResultsFooter(StreamWriter s)
//        {
//            s.WriteLine("</TransliterationTaskResults>");
//        }
//
//        private static void EvaluateExamplesToFile(List<String> testWords, HashMap<Pair<String, String>, double> probs, int maxSubstringLength, HashMap<String, double> languageModel, int ngramSize, int predictionCount, bool tryAllPredictions, String resultsFile)
//        {
//            HashMap<String, Pair<String, double>> maxProbs = tryAllPredictions ? null : WikiTransliteration.GetMaxProbs(probs);
//            Map<String, String> probMap = tryAllPredictions ? WikiTransliteration.GetProbMap(probs) : null;
//
//            StreamWriter writer = null;
//
//            writer = new StreamWriter(resultsFile, false, Encoding.UTF8);
//            WriteTaskResultsHeader(writer, "Russian", "standard", "1", "EM-alignments with no normalization");
//
//            int index = 0;
//
//            for (String testWord2 in testWords)
//            {
//                String testWord = testWord2;
//                bool retry = false;
//
//                TopList<double, String> predictions;
//
//                do
//                {
//                    predictions =
//                        tryAllPredictions ?
//                        WikiTransliteration.Predict((languageModel == null ? predictionCount : 20), testWord, maxSubstringLength, probMap, probs, new HashMap<String, TopList<Double, String>>(), null, 0)
//                            :
//                            WikiTransliteration.Predict((languageModel == null ? predictionCount : 20), testWord, maxSubstringLength, maxProbs, new HashMap<String, TopList<Double, String>>());
//
//                    if (languageModel != null)
//                    {
//                        TopList<Double, String> fPredictions = new TopList<Double, String>(predictionCount);
//                        for (Pair<Double, String> prediction in predictions)
//                            //fPredictions.Add(prediction.Key * Math.Pow(WikiTransliteration.GetLanguageProbabilityViterbi(prediction.Value, languageModel, ngramSize),1d/(prediction.Value.Length)), prediction.Value);
//                            fPredictions.Add(prediction.Key * Math.Pow(WikiTransliteration.GetLanguageProbability(prediction.Value, languageModel, ngramSize), 1), prediction.Value);
//                        predictions = fPredictions;
//                    }
//
//                    if (predictions.Count == 0)
//                    {
//                        if (!retry)
//                        {
//                            retry = true;
//                            testWord = StripAccent(testWord);
//                        }
//                        else
//                        {
//                            retry = false;
//                            predictions.Add(1, "Unknown");
//                        }
//                    }
//                    else
//                        retry = false;
//                }
//                while (retry);
//
//                WriteTaskResults(writer, index++, testWord2, predictions.Values);
//            }
//
//
//
//
//            WriteTaskResultsFooter(writer);
//            writer.Close();
//        }
//
    public static HashMap<Production, Double> PruneProbs(int topK, HashMap<Production, Double> probs) {
        HashMap<String, List<Pair<String, Double>>> lists = new HashMap<>();
        for (Production key : probs.keySet()) {
            Double value = probs.get(key);

            if (!lists.containsKey(key.getFirst())) {
                lists.put(key.getFirst(), new ArrayList<Pair<String, Double>>());
            }

            lists.get(key.getFirst()).add(new Pair<>(key.getSecond(), value));
        }

        HashMap<Production, Double> result = new HashMap<>();
        for (String key : lists.keySet()) {
            List<Pair<String, Double>> value = lists.get(key);
            Collections.sort(value, new Comparator<Pair<String, Double>>() {
                @Override
                public int compare(Pair<String, Double> o1, Pair<String, Double> o2) {
                    double v = o2.getSecond() - o1.getSecond();
                    if(v > 0){
                        return 1;
                    }else if (v < 0){
                        return -1;
                    }else{
                        return 0;
                    }
                }
            });
            int toAdd = Math.min(topK, value.size());
            for (int i = 0; i < toAdd; i++) {
                result.put(new Production(key, value.get(i).getFirst()), value.get(i).getSecond());
            }
        }

        return result;
    }
//
//        public static TopList<Double, String> TopProbs(HashMap<Pair<String, String>, Double> probs, String sourceSubstring)
//        {
//            TopList<Double, String> topList = new TopList<Double, String>(20);
//            for (Pair<Pair<String, String>, Double> pair : probs)
//                if (pair.Key.x == sourceSubstring) topList.Add(pair.Value, pair.Key.y);
//
//            return topList;
//
//            ////System.out.println();
//
//            ////for (Pair<Double, String> pair in topList)
//            ////    System.out.println(pair.Value + "\t" + pair.Key);
//
//            ////System.out.println();
//        }
//
//        private static void EvaluateExamplesDual(HashMap<String, List<String>> testingPairs, List<String> candidates, HashMap<Pair<String, String>, Double> probs, HashMap<Pair<String, String>, Double> probsDual, int maxSubstringLength, bool summedPredications, double minProductionProbability, int maxRank)
//        {
//            int correct = 0;
//            //int contained = 0;
//            double mrr = 0;
//            int misses = 0;
//
//            for (Pair<String, List<String>> pair in testingPairs)
//            {
//                int index = 0;
//
//                String[] words;
//                if (false) //File.Exists(@"C:\Data\WikiTransliteration\RWords\" + pair.Key + ".txt"))
//                {
//                    List<String> readWords = new List<String>();
//                    for (String line : File.ReadAllLines(@"C:\Data\WikiTransliteration\RWords\" + pair.Key + ".txt"))
//                    {
//                        readWords.Add(line.Split('\t')[0]);
//                        if (readWords.Count == 50) break;
//                    }
//
//                    words = readWords.ToArray();
//                }
//                else
//                    words = candidates.ToArray(); //new String[candidates.Count];
//
//                if (maxRank == int.MaxValue || maxRank <= 0)
//                {
//                    double[] scores = new double[words.Length];
//                    double[] scores2 = new double[words.Length];
//
//                    for (int i = 0; i < words.Length; i++)
//                    {
//                        scores[i] = Math.Log(summedPredications ?
//                            WikiTransliteration.GetSummedAlignmentProbability(pair.Key, words[i], maxSubstringLength, maxSubstringLength, probs, new HashMap<Pair<String, String>, Double>(), minProductionProbability)
//                            / segSums[pair.Key.Length - 1][words[i].Length - 1]
//                            : WikiTransliteration.GetAlignmentProbability(pair.Key, words[i], maxSubstringLength, probs, 0, minProductionProbability))
//                        + Math.Log(summedPredications ?
//                            WikiTransliteration.GetSummedAlignmentProbability(words[i], pair.Key, maxSubstringLength, maxSubstringLength, probsDual, new HashMap<Pair<String, String>, Double>(), minProductionProbability)
//                            / segSums[pair.Key.Length - 1][words[i].Length - 1]
//                            : WikiTransliteration.GetAlignmentProbability(words[i], pair.Key, maxSubstringLength, probsDual, 0, minProductionProbability));
//                    }
//
//                    Array.Sort<double, String>(scores, words);
//
//                    //write to disk
//                    //try
//                    //{
//                    //    StreamWriter w = new StreamWriter(@"C:\Data\WikiTransliteration\RWords\" + pair.Key + ".txt");
//                    //    for (int i = words.Length - 1; i >= 0; i--)
//                    //        w.WriteLine(words[i] + "\t" + scores[i]);
//                    //    w.Close();
//                    //}
//                    //catch { }
//
//                    for (int i = words.Length - 1; i >= 0; i--)
//                        if (pair.Value.Contains(words[i]))
//                        {
//                            index = i; break;
//                        }
//
//                    index = words.Length - index;
//                }
//                else
//                {
//                    TopList<Double, String> results = new TopList<Double, String>(maxRank);
//
//                    for (int i = 0; i < words.Length; i++)
//                    {
//                        double score = Math.Log(summedPredications ?
//                                WikiTransliteration.GetSummedAlignmentProbability(pair.Key, words[i], maxSubstringLength, maxSubstringLength, probs, new HashMap<Pair<String, String>, Double>(), minProductionProbability)
//                                : WikiTransliteration.GetAlignmentProbability(pair.Key, words[i], maxSubstringLength, probs, results.Count < maxRank ? 0 : Math.Exp(results[maxRank - 1].Key), minProductionProbability));
//                        if (results.Count == maxRank && results[maxRank - 1].Key >= score) continue;
//                        score += Math.Log(summedPredications ?
//                                WikiTransliteration.GetSummedAlignmentProbability(words[i], pair.Key, maxSubstringLength, maxSubstringLength, probsDual, new HashMap<Pair<String, String>, Double>(), minProductionProbability)
//                                : WikiTransliteration.GetAlignmentProbability(words[i], pair.Key, maxSubstringLength, probsDual, results.Count < maxRank ? 0 : Math.Exp(results[maxRank - 1].Key - score), minProductionProbability));
//
//                        results.Add(score, words[i]);
//                    }
//
//                    index = maxRank + 2;
//                    for (int i = 0; i < results.Count; i++)
//                        if (pair.Value.Contains(results[i].Value))
//                        {
//                            index = i+1; break;
//                        }
//                }
//
//                if (index == 1)
//                    correct++;
//                else
//                    misses++;
//                mrr += ((double)1) / index;
//            }
//
//            mrr /= testingPairs.Count;
//
//            System.out.println(testingPairs.Count + " pairs tested in total; " + candidates.Count + " candidates.");
//            //System.out.println(contained + " predictions contained (" + (((double)contained) / testingPairs.Count) + ")");
//            System.out.println(correct + " predictions exactly correct (" + (((double)correct) / testingPairs.Count) + ")");
//            System.out.println("MRR: " + mrr);
//        }
//
//        private static void EvaluateExamplesDiscoveryGeneration(HashMap<String, List<String>> testingPairs, List<String> candidates, HashMap<Pair<String, String>, double> probs, int maxSubstringLength, bool summedPredications, int generationPruning)
//        {
//            int correct = 0;
//            //int contained = 0;
//            double mrr = 0;
//            int misses = 0;
//
//            Map<String,String> probMap = WikiTransliteration.GetProbMap(probs);
//            Map<String, String> candidateMap = new Map<String, String>();
//            for (String candidate in candidates)
//            {
//                for (int i = 1; i <= candidate.Length; i++)
//                    candidateMap.Add(candidate.Substring(0, i), candidate);
//            }
//
//            for (Pair<String, List<String>> pair in testingPairs)
//            {
//                TopList<Double,String> topList;
//
//                if (summedPredications)
//                    topList = WikiTransliteration.Predict2(generationPruning, pair.Key, maxSubstringLength, probMap, probs, new HashMap<String,HashMap<String,Double>>(), generationPruning);
//                else
//                    topList = WikiTransliteration.Predict(generationPruning, pair.Key, maxSubstringLength, probMap, probs, new HashMap<String, TopList<Double, String>>(), null, 0);
//
//                int index = int.MaxValue;
//
//                for (int i = 0; i < topList.Count; i++)
//                {
//                    for (int j = topList[i].Value.Length; j>=3; j--)
//                    {
//                        String ss = topList[i].Value.Substring(0,j);
//                        if (candidateMap.ContainsKey(ss))
//                        {
//                            if (pair.Value.Contains(candidateMap.GetValuesForKey(ss)[0]))
//                            {
//                                index = i + 1;
//                                break;
//                            }
//                        }
//                    }
//
//                    if (index < int.MaxValue) break; //done
//                }
//
//                if (index == 1)
//                    correct++;
//                else
//                    misses++;
//
//                mrr += (index == int.MaxValue ? 0 : ((double)1) / index);
//            }
//
//            mrr /= testingPairs.Count;
//
//            System.out.println(testingPairs.Count + " pairs tested in total; " + candidates.Count + " candidates.");
//            //System.out.println(contained + " predictions contained (" + (((double)contained) / testingPairs.Count) + ")");
//            System.out.println(correct + " predictions exactly correct (" + (((double)correct) / testingPairs.Count) + ")");
//            System.out.println("MRR: " + mrr);
//        }

    private static void DiscoveryEvaluation(HashMap<String, List<String>> testingPairs, List<String> candidates, TransliterationModel model) {
        int correct = 0;
        //int contained = 0;
        double mrr = 0;
        int misses = 0;

        for (String key : testingPairs.keySet()) {
            List<String> value = testingPairs.get(key);

            //double[] scores = new double[candidates.size()];
            final List<Double> scores = new ArrayList<>(candidates.size());
            String[] words = candidates.toArray(new String[candidates.size()]);

            final List<String> fakewords = new ArrayList<>(candidates);

            for (int i = 0; i < words.length; i++)
                scores.set(i, model.GetProbability(key, words[i]));

            // sort the words according to the scores. Assume that the indices match up
            //Array.Sort<double, String>(scores, words);
            Collections.sort(candidates, new Comparator<String>() {
                public int compare(String left, String right) {
                    return Double.compare(scores.get(fakewords.indexOf(left)), scores.get(fakewords.indexOf(right)));
                }
            });

            int index = 0;
            for (int i = words.length - 1; i >= 0; i--)
                if (value.contains(words[i])) {
                    index = i;
                    break;
                }

            index = words.length - index;

            if (index == 1)
                correct++;
            else
                misses++;
            mrr += ((double) 1) / index;
        }

        mrr /= testingPairs.size();

        System.out.println(testingPairs.size() + " pairs tested in total; " + candidates.size() + " candidates.");
        //System.out.println(contained + " predictions contained (" + (((double)contained) / testingPairs.Count) + ")");
        System.out.println(correct + " predictions exactly correct (" + (((double) correct) / testingPairs.size()) + ")");
        System.out.println("MRR: " + mrr);
    }

    public static SparseDoubleVector<Production> InitializeWithRomanization(SparseDoubleVector<Production> probs, List<Triple<String, String, Double>> trainingTriples, List<Example> testing) {

//        List<String> hebtable;
//        try {
//             hebtable = LineIO.read("hebrewromanization.txt");
//        } catch (FileNotFoundException e) {
//            return probs;
//        }
//
//        for(String line : hebtable){
//            String[] sline = line.split(" ");
//            String heb = sline[0];
//            String eng = sline[1];
//
//            probs.put(new Production(eng, heb), 1.0);
//        }

        // get all values from training.
        for(Triple<String, String, Double> t : trainingTriples){
            String chinese = t.getSecond();
            for(char c : chinese.toCharArray()){
                // CHINESE
                String[] res = PinyinHelper.toHanyuPinyinStringArray(c);
                for(String s : res) {
                    // FIXME: strip number from s?
                    String ss = s.substring(0,s.length()-1);
                    probs.put(new Production(ss, c + ""), 1.);
                }
            }
        }

        // get all values from testing also
        for(MultiExample t : testing){
            List<String> chineseWords = t.getTransliteratedWords();
            for(String chinese : chineseWords) {
                for (char c : chinese.toCharArray()) {
                    // CHINESE
                    String[] res = PinyinHelper.toHanyuPinyinStringArray(c);
                    for (String s : res) {
                        // FIXME: strip number from s?
                        String sss = s.substring(0, s.length() - 1);
                        probs.put(new Production(sss, c + ""), 1.);
                    }
                }
            }
        }


        return probs;
    }

//
//        private static void EvaluateExamples(HashMap<String,List<String>> testingPairs, List<String> candidates, HashMap<Pair<String, String>, Double> probs, int maxSubstringLength, bool summedPredications, double minProductionProbability)
//        {
//            int correct = 0;
//            //int contained = 0;
//            double mrr = 0;
//            int misses = 0;
//
//            for (Pair<String, List<String>> pair : testingPairs)
//            {
//                double[] scores = new double[candidates.Count];
//                String[] words = candidates.ToArray(); //new String[candidates.Count];
//
//                for (int i = 0; i < words.Length; i++)
//                {
//                    scores[i] = (summedPredications ?
//                        WikiTransliteration.GetSummedAlignmentProbability(pair.Key, words[i], maxSubstringLength, maxSubstringLength, probs, new HashMap<Pair<String, String>, double>(), minProductionProbability)
//                            / segSums[pair.Key.Length-1][words[i].Length-1]
//                        : WikiTransliteration.GetAlignmentProbability(pair.Key, words[i], maxSubstringLength, probs, 0, minProductionProbability));
//                }
//
//                Array.Sort<double, String>(scores, words);
//
//                int index = 0;
//                for (int i = words.Length - 1; i >= 0; i--)
//                    if (pair.Value.Contains(words[i]))
//                    {
//                        index = i; break;
//                    }
//
//                index = words.Length - index;
//
//                if (index == 1)
//                    correct++;
//                else
//                    misses++;
//                mrr += ((double)1) / index;
//            }
//
//            mrr /= testingPairs.Count;
//
//            System.out.println(testingPairs.Count + " pairs tested in total; " + candidates.Count + " candidates.");
//            //System.out.println(contained + " predictions contained (" + (((double)contained) / testingPairs.Count) + ")");
//            System.out.println(correct + " predictions exactly correct (" + (((double)correct) / testingPairs.Count) + ")");
//            System.out.println("MRR: " + mrr);
//        }
//
//        private static void EvaluateExamples(List<Pair<String, String>> testingPairs, HashMap<Pair<String, String>, double> probs, int maxSubstringLength, HashMap<String, double> languageModel, int ngramSize, int predictionCount, bool tryAllPredictions, int summedProbPredictionsPruningSize)
//        {
//            if (summedProbPredictionsPruningSize > 0)
//            {
//                probs = PruneProbs(summedProbPredictionsPruningSize, probs);
//            }
//
//            HashMap<String, Pair<String, double>> maxProbs = tryAllPredictions ? null : WikiTransliteration.GetMaxProbs(probs);
//            Map<String, String> probMap = (summedProbPredictionsPruningSize > 0 || tryAllPredictions) ? WikiTransliteration.GetProbMap(probs) : null;
//
//            int correct = 0;
//            int contained = 0;
//            double mrr = 0;
//
//            for (Pair<String, String> pair in testingPairs)
//            {
//                TopList<double, String> predictions;
//
//                if (summedProbPredictionsPruningSize > 0)
//                    predictions = WikiTransliteration.Predict2(predictionCount, pair.Key, maxSubstringLength, probMap, probs, new HashMap<String, HashMap<String, double>>(), summedProbPredictionsPruningSize);
//                else
//                    predictions =
//                        tryAllPredictions ?
//                            WikiTransliteration.Predict(predictionCount, pair.Key, maxSubstringLength, probMap, probs, new HashMap<String, TopList<double, String>>(), null, 0)
//                            :
//                            WikiTransliteration.Predict(predictionCount, pair.Key, maxSubstringLength, maxProbs, new HashMap<String, TopList<double, String>>());
//
//                if (languageModel != null)
//                {
//                    TopList<double, String> fPredictions = new TopList<double, String>(20);
//                    for (Pair<double, String> prediction in predictions)
//                        //fPredictions.Add(prediction.Key * Math.Pow(WikiTransliteration.GetLanguageProbabilityViterbi(prediction.Value, languageModel, ngramSize),1d/(prediction.Value.Length)), prediction.Value);
//                        fPredictions.Add(prediction.Key * Math.Pow(WikiTransliteration.GetLanguageProbability(prediction.Value, languageModel, ngramSize), 1), prediction.Value);
//                    predictions = fPredictions;
//                }
//
//
//
//                int position = predictions.Values.IndexOf(pair.Value);
//                if (position == 0)
//                    correct++;
//
//                if (position >= 0)
//                    contained++;
//
//                if (position < 0)
//                    position = 20;
//
//                mrr += 1 / ((double)position + 1);
//            }
//
//            mrr /= testingPairs.Count;
//
//            System.out.println(testingPairs.Count + " pairs tested in total.");
//            System.out.println(contained + " predictions contained (" + (((double)contained) / testingPairs.Count) + ")");
//            System.out.println(correct + " predictions exactly correct (" + (((double)correct) / testingPairs.Count) + ")");
//            System.out.println("MRR: " + mrr);
//        }
//
//
//        public static HashMap<Triple<String, String, String>, double> LiftProbs(HashMap<Pair<String, String>, double> probs)
//        {
//            HashMap<Triple<String, String, String>, double> result = new HashMap<Triple<String, String, String>, double>(probs.Count);
//            for (Pair<Pair<String, String>, double> pair in probs)
//                result.Add(new Triple<String, String, String>("", pair.Key.x, pair.Key.y), pair.Value);
//
//            return result;
//        }
//
//        private static void EvaluateExamples(List<Pair<String, String>> testingPairs, HashMap<Triple<String, String, String>, double> probs, int maxSubstringLength, int predictionCount, int contextSize, bool fallback)
//        {
//            Map<Pair<String,String>, String> probMap = WikiTransliteration.GetProbMap(probs);
//
//            int correct = 0;
//            int contained = 0;
//            double mrr = 0;
//
//            for (Pair<String, String> pair in testingPairs)
//            {
//                TopList<double, String> predictions =
//                        WikiTransliteration.PredictViterbi(predictionCount, contextSize, fallback, pair.Key, maxSubstringLength, probMap, probs);
//
//                int position = predictions.Values.IndexOf(pair.Value);
//                if (position == 0)
//                    correct++;
//
//                if (position >= 0)
//                    contained++;
//
//                mrr += position >= 0 ? 1 / ((double)position + 1) : 0;
//            }
//
//            mrr /= testingPairs.Count;
//
//            System.out.println(testingPairs.Count + " pairs tested in total.");
//            System.out.println(contained + " predictions contained (" + (((double)contained) / testingPairs.Count) + ")");
//            System.out.println(correct + " predictions exactly correct (" + (((double)correct) / testingPairs.Count) + ")");
//            System.out.println("MRR: " + mrr);
//        }
//
//        private static void MakeAlignmentTableEM(String translationMapFile, String alignmentFile, int maxSubstringLength, int iterations)
//        {
//            Pasternack.Collections.Generic.Specialized.InternDictionary<String> internTable = new Pasternack.Collections.Generic.Specialized.InternDictionary<String>();
//            List<Triple<String, String, WordAlignment>> examples = GetTrainingExamples(translationMapFile);
//            List<Triple<String, String, Double>> examples2 = new List<Triple<String, String, double>>(examples.Count);
//            for (Triple<String, String, WordAlignment> triple in examples)
//                examples2.Add(new Triple<String, String, double>(triple.x, triple.y, 1));
//
//            HashMap<Pair<String, String>, double> probs = MakeAlignmentTable(maxSubstringLength,maxSubstringLength, examples2, null,false);
//
//            for (int i = 0; i < iterations; i++)
//                probs = MakeAlignmentTable(maxSubstringLength, maxSubstringLength, examples2, probs,true);
//
//            WriteProbDictionary(alignmentFile, probs);
//
//        }
//
//        public static void WriteProbDictionary(String filename, HashMap<Pair<String,String>,double> HashMap)
//        {
//            BinaryWriter writer = new BinaryWriter(File.Create(filename));
//
//            writer.Write(HashMap.Count);
//            for (Pair<Pair<String, String>, double> pair in HashMap)
//            {
//                writer.Write(pair.Key.x); writer.Write(pair.Key.y);
//                writer.Write(pair.Value);
//            }
//            writer.Close();
//        }
//
//        public static HashMap<Pair<String, String>, double> ReadProbDictionary(String filename)
//        {
//            Pasternack.Collections.Generic.Specialized.InternDictionary<String> internTable = new Pasternack.Collections.Generic.Specialized.InternDictionary<String>();
//
//            BinaryReader reader = new BinaryReader(File.OpenRead(filename));
//            int count = reader.ReadInt32();
//            HashMap<Pair<String, String>, double> result = new HashMap<Pair<String, String>, double>(count);
//
//            for (int i = 0; i < count; i++)
//                result.Add(new Pair<String, String>(internTable.Intern(reader.ReadString()), internTable.Intern(reader.ReadString())), reader.ReadDouble());
//
//            reader.Close();
//
//            return result;
//        }
//
//        private class DoubleStringKeyValuePairConverter : IComparer<Pair<double, String>>
//        {
//            #region IComparer<Pair<double,String>> Members
//
//            public int Compare(Pair<double, String> x, Pair<double, String> y)
//            {
//                return Math.Sign(y.Key - x.Key); //note that order is reversed
//            }
//
//            #endregion
//        }
//
//        private static void TestAlexAlignment(String alignmentFile, int maxSubstringLength)
//        {
//            DoubleStringKeyValuePairConverter doubleStringKeyValuePairConverter = new DoubleStringKeyValuePairConverter();
//
//            int total = 0;
//            int correct = 0;
//
//            //FileStream fs = File.OpenRead(alignmentFile);
//            //System.Runtime.Serialization.Formatters.Binary.BinaryFormatter bf = new System.Runtime.Serialization.Formatters.Binary.BinaryFormatter();
//            //HashMap<Pair<String,String>,double> probs = (HashMap<Pair<String,String>,double>)bf.Deserialize(fs);
//            ////HashMap<String,long> totals = (HashMap<String,long>)bf.Deserialize(fs);
//            //fs.Close();
//
//            HashMap<Pair<String, String>, double> probs = ReadProbDictionary(alignmentFile);
//
//            HashMap<String, List<String>> alexData = GetAlexData(@"C:\Users\jpaster2\Desktop\res\res\Russian\evalpairs.txt");
//            List<String> alexWords = new List<String>(GetAlexWords().Keys);
//
//            for (Pair<String, List<String>> pair in alexData)
//            {
//                List<Pair<double, String>> top20 = new List<Pair<double, String>>();
//
//                for (String russian in alexWords)
//                {
//                    double prob = WikiTransliteration.GetAlignmentProbability(pair.Key, russian, maxSubstringLength, probs, top20.Count >= 20 ? top20[top20.Count-1].Key : 0,0);
//                    if (top20.Count < 20 || top20[top20.Count - 1].Key < prob)
//                    {
//                        Pair<double,String> russianPair = new Pair<double,String>(prob,russian);
//                        int index = top20.BinarySearch(russianPair, doubleStringKeyValuePairConverter);
//                        if (index < 0) index = ~index; //complement if necessary
//                        top20.Insert(index, russianPair);
//                        if (top20.Count > 20) top20.RemoveAt(top20.Count - 1);
//                    }
//                }
//
//                total++;
//
//                bool correctFlag = false;
//                for (Pair<double,String> russianPair in top20)
//                    if (pair.Value.Contains(russianPair.Value))
//                    {
//                        correctFlag = true; correct++; break;
//                    }
//
//                if (!correctFlag)
//                    System.out.println("Missed " + pair.Key);
//            }
//
//            System.out.println("Total: " + total);
//            System.out.println("Correct: " + correct);
//            System.out.println("Accuracy: " + (((double)correct) / total));
//            Console.ReadLine();
//        }
//
//        private static void FindSynonyms(String redirectFile)
//        {
//            StreamReader reader = new StreamReader(redirectFile);
//            WikiRedirectTable redirectTable = new WikiRedirectTable(reader);
//            reader.Close();
//
//            HashMap<Pair<String, String>, long> counts = new HashMap<Pair<String, String>, long>();
//
//            HashMap<String,List<String>> inverted = redirectTable.InvertedTable;
//            for (Pair<String, List<String>> pair in inverted)
//            {
//                if (pair.Key.Length < 4 || !Regex.IsMatch(pair.Key,"^\\w+$",RegexOptions.Compiled)) continue;
//
//                int maxDistance = pair.Key.Length / 4;
//                List<String> synonyms = new List<String>();
//                int alignmentLength;
//
//                String lWord = pair.Key.ToLower();
//
//                for (String syn in pair.Value)
//                    if (!syn.Equals(pair.Key, StringComparison.OrdinalIgnoreCase) && WikiTransliteration.EditDistance<char>(lWord, syn.ToLower(), out alignmentLength) <= maxDistance)
//                    {
//                        synonyms.Add(syn);
//                        //if (lWord.Length > syn.Length)
//                        //    WikiTransliteration.CountAlignments(lWord, syn.ToLower(), 1, counts);
//                    }
//
//                if (synonyms.Count > 0)
//                    System.out.println(pair.Key + ": " + String.Join(", ", synonyms.ToArray()));
//            }
//
//            Console.ReadLine();
//        }
//
//        private static void CheckCoverage(String translationMapFile, params String[] xmlDataFiles)
//        {
//            List<Pair<String, String>> pairs = new List<Pair<String, String>>();
//            for (String xmlDataFile in xmlDataFiles)
//                pairs.AddRange(GetTaskPairs(xmlDataFile));
//
//            int wordCount = 0;
//            int coveredWords = 0;
//            int correctWords = 0;
//            int containedCorrectedWords = 0;
//
//            HashMap<Pair<String, String>, int> weights;
//            Map<String, String> translationMap = ReadTranslationMap(translationMapFile, out weights);
//            Map<String,String> flattenedTranslationMap = new Map<String,String>();
//            for (Pair<String,String> tPair in translationMap)
//                flattenedTranslationMap.TryAdd(StripAccent(tPair.Key),tPair.Value);
//
//            for (Pair<String, String> pair in pairs)
//            {
//                String[] words = Regex.Split(pair.Key, "\\W", RegexOptions.Compiled);
//                for (String word in words)
//                {
//                    if (word.Length == 0) continue;
//                    String fWord = StripAccent(word);
//                    wordCount++;
//                    if (flattenedTranslationMap.ContainsKey(fWord))
//                    {
//                        coveredWords++;
//                        if (((ICollection<String>)flattenedTranslationMap.GetValuesForKey(fWord)).Contains(pair.Value))
//                            correctWords++;
//
//                        for (String tWord in flattenedTranslationMap.GetValuesForKey(fWord))
//                            if (tWord.Contains(pair.Value)) { containedCorrectedWords++; break; }
//                    }
//                }
//            }
//
//            System.out.println("Word count: " + wordCount);
//            System.out.println("Covered words: " + coveredWords);
//            System.out.println("Correct words: " + correctWords);
//            System.out.println("Contained correct words: " + containedCorrectedWords);
//            Console.ReadLine();
//        }
//
//        private static String GetMax(HashMap<String, int> dict)
//        {
//            int highestValue = int.MinValue;
//            String highestWord = null;
//            for (Pair<String, int> pair in dict)
//                if (pair.Value > highestValue)
//                {
//                    highestValue = pair.Value;
//                    highestWord = pair.Key;
//                }
//
//            return highestWord;
//        }

    public enum WeightingMode {
        None, FindWeighted, SuperficiallyWeighted, CountWeighted, MaxAlignment, MaxAlignmentWeighted
    }
//
//        static void AlexTestTable()
//        {
//            StreamDictionary<String, HashMap<String, int>> sd = new StreamDictionary<String, HashMap<String, int>>(
//                                    100, 0.5, @"C:\Data\WikiTransliteration\translationTableKeys.dat", null, @"C:\Data\WikiTransliteration\transliterationTableValues.dat", null);
//
//            HashMap<String, List<String>> alexData = GetAlexData(@"C:\Users\jpaster2\Desktop\res\res\Russian\evalpairs.txt");
//
//            double correct = 0;
//            double total = 0;
//            double found = 0;
//
//            for (Pair<String,List<String>> pair in alexData)
//            {
//                String english = pair.Key;
//                for (String name in pair.Value)
//                {
//                    HashMap<String, int> wordTable;
//                    if (sd.TryGetValue(name, out wordTable) && wordTable.Count > 0)
//                    {
//                        found++;
//
//                        String hypothesis = GetMax(wordTable);
//
//                        bool foundEnglish = false;
//                        for (String w in wordTable.Keys)
//                            if (w == english)
//                            {
//                                foundEnglish = true;
//                                break;
//                            }
//
//
//
//                        if (foundEnglish)
//                        {
//                            correct++;
//                            break;
//                        }
//                        else
//                            System.out.println("Bad translation found for " + name + "(" + hypothesis + "; should be " + english + ")");
//                    }
//                }
//
//                total++;
//            }
//
//            System.out.println("Found: " + found);
//            System.out.println("Correct: " + correct);
//            System.out.println("Total: " + total);
//            System.out.println("Accuracy of found names: " + (correct / found));
//            System.out.println("Accuracy: " + (correct / total));
//            System.out.println("Found: " + (found / total));
//            Console.ReadLine();
//        }
//
//        static List<String> GetTaskWords(String xmlPath)
//        {
//            List<String> result = new List<String>();
//            String input = File.ReadAllText(xmlPath);
//
//            Regex regex = new Regex("<SourceName[^>]*>(?<english>.+?)</SourceName>", RegexOptions.IgnoreCase | RegexOptions.Compiled);
//            Match match = regex.Match(input);
//
//            while (match.Success)
//            {
//                String english = match.Groups["english"].ToString().ToLower().Trim();
//
//                result.Add(english);
//
//                match = match.NextMatch();
//            }
//
//            return result;
//
//        }
//
//        static List<Pair<String, String>> GetTaskPairs(String xmlPath)
//        {
//            List<Pair<String, String>> result = new List<Pair<String, String>>();
//            String input = File.ReadAllText(xmlPath);
//
//            Regex regex = new Regex("<SourceName[^>]*>(?<english>.+?)</SourceName>\\s*<TargetName[^>]*>(?<name>.+?)</TargetName>", RegexOptions.IgnoreCase | RegexOptions.Compiled);
//            Match match = regex.Match(input);
//
//            while (match.Success)
//            {
//                String name = match.Groups["name"].ToString().ToLower().Trim();
//                String english = match.Groups["english"].ToString().ToLower().Trim();
//
//                result.Add(new Pair<String, String>(english, name));
//
//                match = match.NextMatch();
//            }
//
//            return result;
//
//        }
//
//        static void TestTable()
//        {
//            StreamDictionary<String, HashMap<String, int>> sd = new StreamDictionary<String, HashMap<String, int>>(
//                                    100, 0.5, @"C:\Data\WikiTransliteration\translationTableKeys.dat", null, @"C:\Data\WikiTransliteration\transliterationTableValues.dat", null);
//
//            String data = File.ReadAllText(@"C:\Data\WikiTransliteration\NEWS09_train_EnRu_5977.xml");
//            data += File.ReadAllText(@"C:\Data\WikiTransliteration\NEWS09_dev_EnRu_943.xml");
//
//            double correct = 0;
//            double total = 0;
//            double found = 0;
//
//            Regex regex = new Regex("<SourceName[^>]*>(?<english>.+?)</SourceName>\\w*<TargetName[^>]*>(?<name>.+?)</TargetName>", RegexOptions.IgnoreCase | RegexOptions.Compiled);
//            Match match = regex.Match(data);
//
//            while (match.Success)
//            {
//                String name = match.Groups["name"].ToString().ToLower().Trim();
//                String english = match.Groups["english"].ToString().ToLower().Trim();
//
//                HashMap<String, int> wordTable;
//                if (sd.TryGetValue(name, out wordTable) && wordTable.Count > 0)
//                {
//                    found++;
//
//                    String hypothesis = GetMax(wordTable);
//                    if (hypothesis == english)
//                        correct++;
//                    else
//                        System.out.println("Bad translation found for " + name + "(" + hypothesis + "; should be " + english + ")");
//                }
//                else
//                    System.out.println("No translation found for " + name + "(should be " + english + ")");
//
//                total++;
//
//                match = match.NextMatch();
//            }
//
//            System.out.println("Found: " + found);
//            System.out.println("Correct: " + correct);
//            System.out.println("Total: " + total);
//            System.out.println("Accuracy of found names: " + (correct / found));
//            System.out.println("Accuracy: " + (correct / total));
//            System.out.println("Found: " + (found / total));
//            Console.ReadLine();
//        }
//
//        static WikiLocalDB GetStubDB()
//        {
//            return new WikiLocalDB(
//                                File.OpenRead(@"F:\Wikipedia_Stub\metadata.dat"),
//                                new BetterBufferedStream(new FileStream(@"F:/Wikipedia_Stub/title_table.dat", FileMode.Open), 100000),
//                                new BetterBufferedStream(new FileStream(@"F:/Wikipedia_Stub/page_offsets.dat", FileMode.Open), 1000000),
//                                new BetterBufferedStream(new FileStream(@"F:/Wikipedia_Stub/page_records.dat", FileMode.Open), 5000000),
//                                new BetterBufferedStream(new FileStream(@"F:/Wikipedia_Stub/revision_records.dat", FileMode.Open), 200000),
//                                new BetterBufferedStream(new FileStream(@"F:/Wikipedia_Stub/revision_text_offset.dat", FileMode.Open), 200000, 16, true),
//                                new BetterBufferedStream(new FileStream(@"F:/Wikipedia_Stub/revision_text.dat", FileMode.Open), 1000000),
//                                new BetterBufferedStream(new FileStream(@"F:/Wikipedia_Stub/username_table.dat", FileMode.Open), 10000), 50000000, false);
//        }
//
//        static void MakeTable()
//        {
//            //HashMap<String, List<String>> translations = new HashMap<String, List<String>>();
//
//
//            List<Pair<String, String>> russianNames = new List<Pair<String, String>>();
//
//            StreamReader reader = new StreamReader(@"C:\Data\WikiTransliteration\translations.txt");
//            while (!reader.EndOfStream)
//            {
//                String[] line = reader.ReadLine().Split('\t');
//
//                if (line[0].StartsWith("en:", StringComparison.OrdinalIgnoreCase)) line[0] = line[0].Substring(3);
//                if (line[1].StartsWith("ru:", StringComparison.OrdinalIgnoreCase)) line[1] = line[1].Substring(3);
//
//                //remove parenthesized portion (if any))
//                line[0] = Regex.Replace(line[0], "\\(.*\\)", "", RegexOptions.Compiled).Trim();
//                line[1] = Regex.Replace(line[1], "\\(.*\\)", "", RegexOptions.Compiled).Trim();
//
//                russianNames.Add(new Pair<String, String>(line[1], line[0]));
//            }
//
//            reader.Close();
//
//            HashMap<String, HashMap<String, int>> translationTable = new HashMap<String, HashMap<String, int>>();
//
//            for (Pair<String, String> pair in russianNames)
//            {
//                String[] russianWords = Regex.Split(pair.Key, "\\W", RegexOptions.Compiled);
//                String[] englishWords = Regex.Split(pair.Value, "\\W", RegexOptions.Compiled);
//
//                int score = 1;
//                if (englishWords.Length == russianWords.Length) score = 2;
//
//                for (String rawRussianWord in russianWords)
//                {
//                    if (rawRussianWord.Length == 0) continue;
//                    String russianWord = rawRussianWord.ToLower();
//                    if (!translationTable.ContainsKey(russianWord))
//                        translationTable[russianWord] = new HashMap<String, int>();
//
//                    HashMap<String, int> wordTable = translationTable[russianWord];
//
//                    for (String rawEnglishWord in englishWords)
//                    {
//                        if (rawEnglishWord.Length == 0) continue;
//                        String englishWord = rawEnglishWord.ToLower();
//                        Dictionaries.IncrementOrSet<String>(wordTable, englishWord, score, score);
//                    }
//                }
//            }
//
//            StreamDictionary<String, HashMap<String, int>> streamTable = new StreamDictionary<String, HashMap<String, int>>(
//                translationTable.Count * 2, 0.5, @"C:\Data\WikiTransliteration\translationTableKeys.dat", null, @"C:\Data\WikiTransliteration\transliterationTableValues.dat", null);
//
//            for (Pair<String, HashMap<String, int>> pair in translationTable)
//                streamTable.Add(pair);
//
//            streamTable.Close();
//        }
//
//        static void FindOverlap()
//        {
//            double found=0;
//            double total=0;
//
//            StreamReader fs = new StreamReader(@"C:\Data\WikiTransliteration\enRedirectTable.dat");
//            WikiRedirectTable redirectTable = new WikiRedirectTable(fs);
//            fs.Close();
//
//            HashMap<String, List<String>> translations = new HashMap<String, List<String>>();
//
//            List<Pair<String, String>> russianNames = new List<Pair<String, String>>();
//
//            StreamReader reader = new StreamReader(@"C:\Data\WikiTransliteration\translations.txt");
//            while (!reader.EndOfStream)
//            {
//                String[] line = reader.ReadLine().Split('\t');
//
//                if (line[0].StartsWith("en:",StringComparison.OrdinalIgnoreCase)) line[0] = line[0].Substring(3);
//                if (line[1].StartsWith("ru:",StringComparison.OrdinalIgnoreCase)) line[1] = line[1].Substring(3);
//
//                russianNames.Add(new Pair<String,String>(line[1],line[0]));
//            }
//
//            reader.Close();
//
//            HashMap<String, bool> dict = new HashMap<String, bool>();
//            for (Pair<String,String> rPair in russianNames)
//            {
//                //String[] words = rPair.Key.Split(' ', '\'', '.', ';', ':');
//                //String[] english = rPair.Value.Split(' ', '\'', '.', ';', ':');
//
//                //String englishString = rPair.Value;
//                String englishString = redirectTable.Redirect(rPair.Value);
//                englishString = Regex.Replace(englishString, "\\(.*\\)", "", RegexOptions.Compiled).Trim();
//
//                String[] words = new String[] { rPair.Key };
//                String[] english = new String[] { englishString };
//
//                for (int i = 0; i < english.Length; i++) english[i] = english[i].ToLower().Trim();
//                for (int i = 0; i < words.Length; i++) words[i] = words[i].ToLower().Trim();
//
//                for (String word in words)
//                {
//                    if (word == "") continue;
//
//                    dict[word.ToLower()] = true;
//                    for (String eWord in english)
//                    {
//                        if (eWord.Length == 0) continue;
//
//                        if (!translations.ContainsKey(word))
//                            translations[word] = new List<String>();
//
//                        if (!translations[word].Contains(eWord))
//                            translations[word].Add(eWord);
//                    }
//                }
//            }
//
//            //filter out all but the word with length closest to the original English
//            for (Pair<String, List<String>> pair in translations)
//            {
//                String best = null;
//                for (int i = 0; i < pair.Value.Count; i++)
//                {
//                    if (pair.Value[i].Contains(" "))
//                    {
//                        //pair.Value.RemoveAt(i);
//                        //i--;
//                        continue;
//                    }
//
//                    //pair.Value[i] = Regex.Replace(pair.Value[i], "\\W", "", RegexOptions.Compiled);
//
//                    if (best == null || Math.Abs(pair.Key.Length - pair.Value[i].Length) < Math.Abs(pair.Key.Length - best.Length))
//                        best = pair.Value[i];
//                }
//
//                pair.Value.Clear();
//                if (best != null) pair.Value.Add(best);
//            }
//
//            String data = File.ReadAllText(@"C:\Data\WikiTransliteration\NEWS09_train_EnRu_5977.xml");
//            data += File.ReadAllText(@"C:\Data\WikiTransliteration\NEWS09_dev_EnRu_943.xml");
//
//            double correct = 0;
//
//            Regex regex = new Regex("<SourceName[^>]*>(?<english>.+?)</SourceName>\\w*<TargetName[^>]*>(?<name>.+?)</TargetName>", RegexOptions.IgnoreCase | RegexOptions.Compiled);
//            Match match = regex.Match(data);
//
//            while (match.Success)
//            {
//                String name = match.Groups["name"].ToString().ToLower().Trim();
//                String english = match.Groups["english"].ToString().ToLower().Trim();
//
//                if (dict.ContainsKey(name))
//                {
//                    found++;
//                    if (translations.ContainsKey(name) && translations[name].Contains(english))
//                        correct++;
//                    else
//                    {
//                        if (!translations.ContainsKey(name) || translations[name].Count == 0)
//                            System.out.println("No translation found for " + name + "(should be " + english + ")");
//                        else
//                            System.out.println("Bad translation found for " + name + "(" + translations[name][0] + "; should be " + english + ")");
//                    }
//                }
//
//                total++;
//
//                match = match.NextMatch();
//            }
//
//            //StreamDictionary<String, String> sd = new StreamDictionary<String, String>(translations.Count * 2, 0.5, @"C:\data\WikiTransliteration\translationTable.dat", null, true, 255, 255, null, delegate(BinaryReader r) { return r.ReadString(); }, delegate(BinaryReader r) { return r.ReadString(); }, delegate(BinaryWriter w, String s) { w.Write(s); }, delegate(BinaryWriter w, String s) { w.Write(s); });
//            //for (Pair<String, List<String>> pair in translations)
//            //{
//            //    if (pair.Value.Count == 0) continue;
//            //    sd[(pair.Key.Length > 250 ? pair.Key.Substring(0, 250) : pair.Key)] = pair.Value[0].Length > 250 ? pair.Value[0].Substring(0, 250) : pair.Value[0];
//            //}
//
//            //sd.Close();
//
//            System.out.println("Found: " + found);
//            System.out.println("Correct: " + correct);
//            System.out.println("Total: " + total);
//            System.out.println("Accuracy of found names: " + (correct / found));
//            System.out.println("Accuracy: " + (correct / total));
//            System.out.println("Found: " + (found / total));
//            Console.ReadLine();
//        }
//
//

//        static void GetEntities(String wikiFile, String redirectTableFileName, bool english, TextWriter writer, bool useRedirects)
//        {
//            WikiRedirectTable redirectTable = null;
//
//            StreamReader fs = new StreamReader(redirectTableFileName);
//            redirectTable = new WikiRedirectTable(fs);
//            fs.Close();
//
//            HashMap<String, List<String>> redirects = redirectTable.InvertedTable;
//
//            ICSharpCode.SharpZipLib.BZip2.BZip2InputStream bzipped =
//                new ICSharpCode.SharpZipLib.BZip2.BZip2InputStream(File.OpenRead(wikiFile));
//            WikiXMLReader reader = new WikiXMLReader(bzipped);
//
//            for (WikiPage page : reader.Pages)
//            {
//                if (WikiNamespace.GetNamespace(page.Title, reader.WikiInfo.Namespaces) != WikiNamespace.Default) continue;
//                if (redirectTable.Redirect(page.Title) != page.Title) continue; //skip redirects
//
//                for (WikiRevision revision : reader.Revisions)
//                {
//                    List<WikiLink> links = WikiLink.GetWikiLinks(revision.Text, true, false);
//                    String other = null;
//                    for (WikiLink link : links)
//                        if (link.Target.StartsWith((english ? "ru:" : "en:"),StringComparison.OrdinalIgnoreCase))
//                        {
//                            other = WikiLink.GetTitleFromTarget(link.Target,reader.WikiInfo);
//                            break;
//                        }
//
//                    if (other == null) continue; //no other language version
//                    List<String> redirectTitles;
//                    if (!useRedirects || !redirects.TryGetValue(page.Title, out redirectTitles))
//                        redirectTitles = new List<String>();
//                    if (!redirectTitles.Contains(page.Title)) redirectTitles.Add(page.Title);
//
//                    for (String title in redirectTitles)
//                    {
//                        if (english)
//                            writer.WriteLine(title + "\t" + other);
//                        else
//                            writer.WriteLine(other + "\t" + title);
//                    }
//                }
//
//            }
//
//            reader.Close();
//        }
}
