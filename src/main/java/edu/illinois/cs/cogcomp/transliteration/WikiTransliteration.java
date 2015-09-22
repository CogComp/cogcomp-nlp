using System;
using System.Collections.Generic;
using System.Text;
using WAO;
using System.Text.RegularExpressions;
using Pasternack.Collections.Generic;
using System.IO;
using Pasternack.Utility;
using Pasternack.Collections.Generic.Specialized;
using Pasternack;

namespace SPTransliteration
{
    internal struct ContextModel
    {
        public SparseDoubleVector<Pair<Triple<string, string, string>, string>> productionProbs;
        public SparseDoubleVector<Pair<string, string>> segProbs;
        public int segContextSize;
        public int productionContextSize;
        public int maxSubstringLength;
    }

    internal enum NormalizationMode
    {
        None,
        AllProductions,
        BySourceSubstring,
        BySourceSubstringMax,
        BySourceAndTargetSubstring,
        BySourceOverlap,
        ByTargetSubstring
    }

    internal enum AliasType : byte
    {
        Unknown = 0,
        Link = 1,
        Redirect = 2,
        Title = 3,
        Disambig = 4,
        Interlanguage = 5
    }

    [Serializable]
    internal struct WikiAlias
    {
        public string alias;
        public AliasType type;
        public int count;

        public WikiAlias(string alias, AliasType type, int count)
        {
            this.alias = alias;
            this.type = type;
            this.count = count;
        }
    }

    internal class WikiTransliteration
    {
        private static Dictionary<string, bool> languageCodeTable;
        public static readonly string[] languageCodes=new string[]{"aa","ab","ae","af","ak","am","an","ar","as","av","ay","az","ba","be","bg","bh","bi","bm","bn","bo","br","bs","ca","ce","ch","co","cr","cs","cu","cv","cy","da","de","dv","dz","ee","el","en","eo","es","et","eu","fa","ff","fi","fj","fo","fr","fy","ga","gd","gl","gn","gu","gv","ha","he","hi","ho","hr","ht","hu","hy","hz","ia","id","ie","ig","ii","ik","io","is","it","iu","ja","jv","ka","kg","ki","kj","kk","kl","km","kn","ko","kr","ks","ku","kv","kw","ky","la","lb","lg","li","ln","lo","lt","lu","lv","mg","mh","mi","mk","ml","mn","mr","ms","mt","my","na","nb","nd","ne","ng","nl","nn","no","nr","nv","ny","oc","oj","om","or","os","pa","pi","pl","ps","pt","qu","rm","rn","ro","ru","rw","sa","sc","sd","se","sg","sh","si","sk","sl","sm","sn","so","sq","sr","ss","st","su","sv","sw","ta","te","tg","th","ti","tk","tl","tn","to","tr","ts","tt","tw","ty","ug","uk","ur","uz","ve","vi","vo","wa","wo","xh","yi","yo","za","zh","zu"};

        static WikiTransliteration()
        {
            languageCodeTable = new Dictionary<string, bool>(languageCodes.Length);
            foreach (string code in languageCodes)
                languageCodeTable.Add(code, true);
        }

        static void IncrementAlias(Dictionary<string, List<WikiAlias>> dict, string target, WikiAlias toAdd)
        {
            List<WikiAlias> aliasList;
            if (!dict.TryGetValue(target, out aliasList))
                dict[target] = aliasList = new List<WikiAlias>();

            bool found = false;
            for (int i = 0; i < aliasList.Count; i++)
            {
                if (aliasList[i].type == toAdd.type && aliasList[i].alias == toAdd.alias)
                {
                    //found it
                    toAdd.count += aliasList[i].count;
                    aliasList[i] = toAdd;

                    found = true;

                    break;
                }
            }

            if (!found)
                aliasList.Add(toAdd);
        }


        private static string StripParenthesized(string title)
        {
            return Regex.Replace(title, "\\(.*\\)", "", RegexOptions.Compiled).Trim();
        }

        public static Dictionary<string, List<string>> GetRedirectSetWords(WikiRedirectTable redirectTable)
        {
            Pasternack.Collections.Generic.Specialized.InternDictionary<string> stringTable = new Pasternack.Collections.Generic.Specialized.InternDictionary<string>();
            Dictionary<string, List<string>> inverted = redirectTable.InvertedTable;
            Dictionary<string, List<string>> result = new Dictionary<string, List<string>>();
            foreach (KeyValuePair<string, List<string>> pair in inverted)
            {
                Dictionary<string, bool> words = new Dictionary<string, bool>();
                List<string> wordList = new List<string>();

                //pair.Value.Add(pair.Key);
                foreach (string redirect in pair.Value)
                {
                    string[] wordArray = Regex.Split(StripParenthesized(redirect), "\\W", RegexOptions.Compiled);
                    foreach (string word in wordArray)
                    {
                        if (word.Length == 0) continue;
                        string lCased = word.ToLower();
                        if (!words.ContainsKey(lCased))
                        {
                            words[lCased] = true;
                            wordList.Add(stringTable.Intern(lCased));
                        }
                    }
                }

                wordList.TrimExcess();
                result[pair.Key] = wordList;
            }

            return result;
        }

        //public static Dictionary<string, List<KeyValuePair<string, float>>> MakeTranslationTable(string sourceLanguageCode, IDictionary<string, List<WikiAlias>> sourceAliasTable, WikiRedirectTable sourceRedirectTable, string targetLanguageCode, IDictionary<string, List<WikiAlias>> targetAliasTable, WikiRedirectTable targetRedirectTable)
        //{
        //    targetLanguageCode+=":"; sourceLanguageCode +=":";
        //    Dictionary<string, string> sourceToTargetTranslations = new Dictionary<string, string>();
        //    foreach (KeyValuePair<string, List<WikiAlias>> pair in sourceAliasTable)
        //    {
        //        foreach (WikiAlias alias in pair.Value)
        //        {
        //            if (alias.type == AliasType.Interlanguage && alias.alias.StartsWith(targetLanguageCode,StringComparison.OrdinalIgnoreCase))
        //            {
        //                if (sourceToTargetTranslations.ContainsKey(pair.Key)) throw new InvalidDataException();
        //                sourceToTargetTranslations[pair.Key] = alias.alias.Substring(targetLanguageCode.Length);
        //            }
        //        }
        //    }

        //    foreach (KeyValuePair<string, List<WikiAlias>> pair in targetAliasTable)
        //    {
        //        foreach (WikiAlias alias in pair.Value)
        //        {
        //            if (alias.type == AliasType.Interlanguage && alias.alias.StartsWith(sourceLanguageCode,StringComparison.OrdinalIgnoreCase))
        //            {
        //                if (sourceToTargetTranslations.ContainsKey(alias.alias.Substring(sourceLanguageCode.Length))
        //                    && sourceToTargetTranslations[alias.alias.Substring(sourceLanguageCode.Length)] != pair.Key) throw new InvalidDataException();
        //                sourceToTargetTranslations[alias.alias.Substring(sourceLanguageCode.Length)] = pair.Key;
        //            }
        //        }
        //    }

        //    return null;
        //}

        public static List<KeyValuePair<string,string>> GetWordPairs(string term1, string term2, out int terms1Count, out int terms2Count)
        {
            string[] terms1 = Regex.Split(term1, "\\W|·", RegexOptions.Compiled);
            string[] terms2 = Regex.Split(term2, "\\W|·", RegexOptions.Compiled);

            List<KeyValuePair<string, string>> result = new List<KeyValuePair<string, string>>(terms1.Length * terms2.Length);
            terms1Count = 0; terms2Count = 0;

            for (int i = 0; i < terms2.Length; i++)
                if (terms2[i].Length > 0) terms2Count++;

            for (int i = 0; i < terms1.Length; i++)
            {
                if (terms1[i].Length == 0) continue;

                terms1Count++;

                for (int j = 0; j < terms2.Length; j++)
                {
                    if (terms2[j].Length == 0) continue;                    
                    result.Add(new KeyValuePair<string, string>(terms1[i], terms2[j]));
                }
            }

            return result;
        }

        private static int ScoreLengths(int l1, int l2)
        {
            if (l1 == l2)
                return (l1 == 1 ? 100 : 10);
            else
                return 1; //not equal length
        }

        public static Map<string, string> MakeTranslationMap(string sourceLanguageCode, IDictionary<string, List<WikiAlias>> sourceAliasTable, WikiRedirectTable sourceRedirectTable, string targetLanguageCode, IDictionary<string, List<WikiAlias>> targetAliasTable, WikiRedirectTable targetRedirectTable, out Dictionary<Pasternack.Utility.Pair<string, string>, int> weights)
        {
            targetLanguageCode += ":"; sourceLanguageCode += ":";
            Map<string, string> translationMap = new Map<string, string>();
            weights = new Dictionary<Pasternack.Utility.Pair<string, string>, int>();

            foreach (KeyValuePair<string, List<WikiAlias>> pair in sourceAliasTable)
            {
                string pageFormattedTitle = StripParenthesized( pair.Key ).ToLower();
                foreach (WikiAlias alias in pair.Value)
                {
                    if (alias.type == AliasType.Interlanguage && alias.alias.StartsWith(targetLanguageCode, StringComparison.OrdinalIgnoreCase))
                    {
                        int l1, l2;
                        foreach (KeyValuePair<string, string> wordPair in GetWordPairs(pageFormattedTitle, StripParenthesized(alias.alias.Substring(targetLanguageCode.Length)).ToLower(), out l1, out l2))
                        {
                            translationMap.TryAdd(wordPair.Key, wordPair.Value);
                            Dictionaries.IncrementOrSet<Pasternack.Utility.Pair<string, string>>(weights, wordPair, ScoreLengths(l1, l2), ScoreLengths(l1, l2));
                        }
                    }
                }
            }

            foreach (KeyValuePair<string, List<WikiAlias>> pair in targetAliasTable)
            {
                string pageFormattedTitle = StripParenthesized(pair.Key).ToLower();
                foreach (WikiAlias alias in pair.Value)
                {
                    if (alias.type == AliasType.Interlanguage && alias.alias.StartsWith(sourceLanguageCode, StringComparison.OrdinalIgnoreCase))
                    {
                        int l1, l2;
                        foreach (KeyValuePair<string, string> wordPair in GetWordPairs(StripParenthesized(alias.alias.Substring(sourceLanguageCode.Length)).ToLower(), pageFormattedTitle, out l1, out l2))
                        {
                            translationMap.TryAdd(wordPair.Key, wordPair.Value);
                            Dictionaries.IncrementOrSet<Pasternack.Utility.Pair<string, string>>(weights, wordPair, ScoreLengths(l1, l2), ScoreLengths(l1, l2));
                        }
                    }
                }
            }

            return translationMap;
        }

        private static void IncrementWeights(Dictionary<Pair<string,string>,WordAlignment> weights, Pair<string, string> wordPair, int l1, int l2)
        {
            WordAlignment additive;
            if (l1 == 1 && l2 == 1)
                additive = new WordAlignment(1, 0, 0);
            else if (l1 == l2)
                additive = new WordAlignment(0, 1, 0);
            else
                additive = new WordAlignment(0, 0, 1);

            WordAlignment current;
            if (weights.TryGetValue(wordPair, out current))
                weights[wordPair] = current + additive;
            else
                weights[wordPair] = additive;
        }

        public static bool IsPerson(List<WikiAlias> aliasList)
        {
            foreach (WikiAlias alias in aliasList)
                if (alias.type == AliasType.Link && alias.alias.StartsWith("category:", StringComparison.OrdinalIgnoreCase) && (alias.alias.Contains("births") || alias.alias.Contains("deaths")))
                    return true;

            return false;
        }

        public static bool IsPerson(string title, Dictionary<string,bool> personByCategory, Dictionary<string, bool> persondataTitles)
        {
            title = title.ToLower();
            return (personByCategory==null && persondataTitles == null) || (personByCategory != null && personByCategory.ContainsKey(title)) || (persondataTitles != null && persondataTitles.ContainsKey(title));
        }

        static bool HasBirthOrDeathCategory(List<WikiCategory> categories)
        {
            foreach (WikiCategory category in categories)
            {
                string name = category.Name.ToLower();
                if (name.Contains("births") || name.Contains("deaths"))
                    return true;
            }

            return false;
        }

        public static Map<string, string> MakeTranslationMap2(string sourceLanguageCode, IDictionary<string, List<WikiAlias>> sourceAliasTable, WikiRedirectTable sourceRedirectTable, string targetLanguageCode, IDictionary<string, List<WikiAlias>> targetAliasTable, WikiRedirectTable targetRedirectTable, out Dictionary<Pasternack.Utility.Pair<string, string>, WordAlignment> weights, WikiCategoryGraph graph, Dictionary<string, bool> persondataTitles, bool requireDot)
        {            
            
            //person = (graph != null ? new Dictionary<string, bool>() : null);
            Dictionary<string, bool> personByCategory = null;
            if (graph != null)
            {
                personByCategory = new Dictionary<string,bool>();
                foreach (KeyValuePair<string, List<WikiCategory>> pair in graph.CreateMemberToCategoriesDictionary())
                    if (HasBirthOrDeathCategory(pair.Value)) personByCategory[pair.Key.ToLower()] = true;
            }

            targetLanguageCode += ":"; sourceLanguageCode += ":";
            Map<string, string> translationMap = new Map<string, string>();
            weights = new Dictionary<Pasternack.Utility.Pair<string, string>, WordAlignment>();

            foreach (KeyValuePair<string, List<WikiAlias>> pair in sourceAliasTable)
            {
                if (!IsPerson(pair.Key, personByCategory, persondataTitles)) continue;

                string pageFormattedTitle = StripParenthesized(pair.Key).ToLower();
                foreach (WikiAlias alias in pair.Value)
                {
                    if (alias.type == AliasType.Interlanguage && alias.alias.StartsWith(targetLanguageCode, StringComparison.OrdinalIgnoreCase))
                    {
                        if (requireDot && !alias.alias.Contains("·")) continue;
                        int l1, l2;
                        foreach (KeyValuePair<string, string> wordPair in GetWordPairs(pageFormattedTitle, StripParenthesized(alias.alias.Substring(targetLanguageCode.Length)).ToLower(), out l1, out l2))
                        {
                            translationMap.TryAdd(wordPair.Key, wordPair.Value);                            
                            IncrementWeights(weights, wordPair, l1, l2);                            
                        }
                    }                    
                }

                 
            }

            foreach (KeyValuePair<string, List<WikiAlias>> pair in targetAliasTable)
            {
                if (requireDot && !pair.Key.Contains("·")) continue;
                string pageFormattedTitle = StripParenthesized(pair.Key).ToLower();
                foreach (WikiAlias alias in pair.Value)
                {
                    if (alias.type == AliasType.Interlanguage && alias.alias.StartsWith(sourceLanguageCode, StringComparison.OrdinalIgnoreCase))
                    {              
                        if (!IsPerson(alias.alias.Substring(sourceLanguageCode.Length), personByCategory, persondataTitles)) continue;

                        int l1, l2;
                        foreach (KeyValuePair<string, string> wordPair in GetWordPairs(StripParenthesized(alias.alias.Substring(sourceLanguageCode.Length)).ToLower(), pageFormattedTitle, out l1, out l2))
                        {
                            translationMap.TryAdd(wordPair.Key, wordPair.Value);
                            IncrementWeights(weights, wordPair, l1, l2);
                        }
                    }
                }
            }

            return translationMap;
        }


        public static Dictionary<string, List<WikiAlias>> MakeAliasTable(IWikiReader reader, List<string> disambigTemplates)
        {
            Pasternack.Collections.Generic.Specialized.InternDictionary<string> internTable = new Pasternack.Collections.Generic.Specialized.InternDictionary<string>();

            HashList<string> dTL = new HashList<string>(disambigTemplates.Count);
            foreach (string template in disambigTemplates)
                dTL.Add(template.ToLower());

            Dictionary<string, List<WikiAlias>> result = new Dictionary<string, List<WikiAlias>>();            

            foreach (WikiPage page in reader.Pages)
            {
                page.Title = internTable.Intern(page.Title);

                if (WikiNamespace.GetNamespace(page.Title, reader.WikiInfo.Namespaces) != WikiNamespace.Default) continue;
                string formattedTitle = internTable.Intern(Regex.Replace(page.Title, "\\(.*\\)", "", RegexOptions.Compiled).Trim());              

                IncrementAlias(result, page.Title, new WikiAlias(formattedTitle, AliasType.Title, 1));

                foreach (WikiRevision revision in reader.Revisions)
                {
                    string redirect = WikiRedirectTable.ParseRedirect(revision.Text, reader.WikiInfo);
                    if (redirect != null)
                    {
                        IncrementAlias(result, page.Title , new WikiAlias(internTable.Intern(redirect), AliasType.Redirect, 1));
                        continue;
                    }

                    List<WikiLink> links = WikiLink.GetWikiLinks(revision.Text, true, false);

                    List<string> templates = WikiUtilities.GetTemplates(revision.Text);
                    bool isDisambig = false;
                    foreach (string template in templates)
                    {
                        int endOfTemplateName = template.IndexOf('|');
                        if (endOfTemplateName < 0) endOfTemplateName = template.Length-2;
                        string templateName = template.Substring(2, endOfTemplateName - 2).ToLower();
                        if (dTL.Contains(templateName))
                        {
                            isDisambig = true;
                            break;
                        }
                    }

                    foreach (WikiLink link in links)
                    {
                        string originalTarget = link.Target;
                        link.Target = WikiLink.GetTitleFromTarget(link.Target, reader.WikiInfo);

                        if (link.Text == null && originalTarget.Length >= 3 && originalTarget[2] == ':' && languageCodeTable.ContainsKey(originalTarget.Substring(0, 2).ToLower()))
                        {
                            IncrementAlias(result, page.Title, new WikiAlias(internTable.Intern(link.Target), AliasType.Interlanguage, 1));
                            continue;
                        }
                        else if (link.Text != null && link.Text != link.Target)
                        {
                            IncrementAlias(result, internTable.Intern(link.Target), new WikiAlias(internTable.Intern(link.Text), AliasType.Link, 1));
                        }

                        if (isDisambig)
                            IncrementAlias(result, internTable.Intern(link.Target), new WikiAlias(page.Title, AliasType.Disambig, 1));
                    }
                }
            }

            return result;
        }

        static void MakeRawLinkTable(string wikiFile, string rawLinkTableFile)
        {
            //BinaryWriter writer = new BinaryWriter(File.Create(rawLinkTableFile));
            StreamWriter writer = new StreamWriter(rawLinkTableFile);

            ICSharpCode.SharpZipLib.BZip2.BZip2InputStream bzipped =
                new ICSharpCode.SharpZipLib.BZip2.BZip2InputStream(File.OpenRead(wikiFile));
            WikiXMLReader reader = new WikiXMLReader(bzipped);

            foreach (WikiPage page in reader.Pages)
            {
                if (WikiNamespace.GetNamespace(page.Title, reader.WikiInfo.Namespaces) != WikiNamespace.Default) continue;

                foreach (WikiRevision revision in reader.Revisions)
                {
                    if (WikiRedirectTable.IsRedirect(revision.Text)) continue;

                    List<WikiLink> links = WikiLink.GetWikiLinks(revision.Text, true, false);

                    //writer.Write(page.Title);
                    //writer.Write(links.Count);

                    foreach (WikiLink link in links)
                    {
                        if (link.Text == null || link.Text.Contains("\t")) continue;
                        writer.WriteLine(link.Text + "\t" + WikiLink.GetTitleFromTarget(link.Target, reader.WikiInfo));
                    }
                }
            }

            reader.Close();
            writer.Close();
        }

        static void MakeTable(string sourceRawLinkFile)
        {
            //Dictionary<string, List<string>> translations = new Dictionary<string, List<string>>();


            List<KeyValuePair<string, string>> russianNames = new List<KeyValuePair<string, string>>();

            StreamReader reader = new StreamReader(@"C:\Data\WikiTransliteration\translations.txt");
            while (!reader.EndOfStream)
            {
                string[] line = reader.ReadLine().Split('\t');

                if (line[0].StartsWith("en:", StringComparison.OrdinalIgnoreCase)) line[0] = line[0].Substring(3);
                if (line[1].StartsWith("ru:", StringComparison.OrdinalIgnoreCase)) line[1] = line[1].Substring(3);

                //remove parenthesized portion (if any))
                line[0] = Regex.Replace(line[0], "\\(.*\\)", "", RegexOptions.Compiled).Trim();
                line[1] = Regex.Replace(line[1], "\\(.*\\)", "", RegexOptions.Compiled).Trim();

                russianNames.Add(new KeyValuePair<string, string>(line[1], line[0]));
            }

            reader.Close();

            Dictionary<string, Dictionary<string, int>> translationTable = new Dictionary<string, Dictionary<string, int>>();

            foreach (KeyValuePair<string, string> pair in russianNames)
            {
                string[] russianWords = Regex.Split(pair.Key, "\\W", RegexOptions.Compiled);
                string[] englishWords = Regex.Split(pair.Value, "\\W", RegexOptions.Compiled);

                int score = 1;
                if (englishWords.Length == russianWords.Length) score = 2;

                foreach (string rawRussianWord in russianWords)
                {
                    if (rawRussianWord.Length == 0) continue;
                    string russianWord = rawRussianWord.ToLower();
                    if (!translationTable.ContainsKey(russianWord))
                        translationTable[russianWord] = new Dictionary<string, int>();

                    Dictionary<string, int> wordTable = translationTable[russianWord];

                    foreach (string rawEnglishWord in englishWords)
                    {
                        if (rawEnglishWord.Length == 0) continue;
                        string englishWord = rawEnglishWord.ToLower();
                        Dictionaries.IncrementOrSet<string>(wordTable, englishWord, score, score);
                    }
                }
            }

            StreamDictionary<string, Dictionary<string, int>> streamTable = new StreamDictionary<string, Dictionary<string, int>>(
                translationTable.Count * 2, 0.5, @"C:\Data\WikiTransliteration\translationTableKeys.dat", null, @"C:\Data\WikiTransliteration\transliterationTableValues.dat", null);

            foreach (KeyValuePair<string, Dictionary<string, int>> pair in translationTable)
                streamTable.Add(pair);

            streamTable.Close();
        }

        /// <SUMMARY>Computes the Levenshtein Edit Distance between two enumerables.</SUMMARY>
        /// <TYPEPARAM name="T">The type of the items in the enumerables.</TYPEPARAM>
        /// <PARAM name="x">The first enumerable.</PARAM>
        /// <PARAM name="y">The second enumerable.</PARAM>
        /// <RETURNS>The edit distance.</RETURNS>
        public static int EditDistance<T>(IEnumerable<T> x, IEnumerable<T> y, out int alignmentLength)
            where T : IEquatable<T>
        {
            // Validate parameters
            if (x == null) throw new ArgumentNullException("x");
            if (y == null) throw new ArgumentNullException("y");

            // Convert the parameters into IList instances
            // in order to obtain indexing capabilities
            IList<T> first = x as IList<T> ?? new List<T>(x);
            IList<T> second = y as IList<T> ?? new List<T>(y);

            // Get the length of both.  If either is 0, return
            // the length of the other, since that number of insertions
            // would be required.
            int n = first.Count, m = second.Count;
            if (n == 0)
            {
                alignmentLength = m;
                return m;
            }

            if (m == 0)
            {
                alignmentLength = n;
                return n;
            }

            // Rather than maintain an entire matrix (which would require O(n*m) space),
            // just store the current row and the next row, each of which has a length m+1,
            // so just O(m) space. Initialize the current row.
            int curRow = 0, nextRow = 1;
            int[][] rows = new int[][] { new int[m + 1], new int[m + 1] };
            int[][] alRows = new int[][] { new int[m + 1], new int[m + 1] }; //alignment length information

            for (int j = 0; j <= m; ++j) rows[curRow][j] = j;
            for (int j = 0; j <= m; ++j) alRows[curRow][j] = j;

            // For each virtual row (since we only have physical storage for two)
            for (int i = 1; i <= n; ++i)
            {
                // Fill in the values in the row
                rows[nextRow][0] = i;
                alRows[nextRow][0] = i;

                for (int j = 1; j <= m; ++j)
                {
                    bool aligns = first[i - 1].Equals(second[j - 1]);
                    int dist1 = rows[curRow][j] + 1;
                    int dist2 = rows[nextRow][j - 1] + 1;
                    int dist3 = rows[curRow][j - 1] +
                        (aligns ? 0 : 2);

                    if (dist1 < dist2 && dist1 < dist3)
                    {
                        alRows[nextRow][j] = alRows[curRow][j] + 1;
                        rows[nextRow][j] = dist1;
                    }
                    else if (dist2 < dist3)
                    {
                        alRows[nextRow][j] = alRows[nextRow][j - 1] + 1;
                        rows[nextRow][j] = dist2;
                    }
                    else
                    {
                        alRows[nextRow][j] = alRows[curRow][j - 1] + (aligns ? 1 : 2);
                        rows[nextRow][j] = dist3;
                    }

                    //rows[nextRow][j] = Math.Min(dist1, Math.Min(dist2, dist3));
                }

                // Swap the current and next rows
                if (curRow == 0)
                {
                    curRow = 1;
                    nextRow = 0;
                }
                else
                {
                    curRow = 0;
                    nextRow = 1;
                }
            }

            alignmentLength = alRows[curRow][m];

            // Return the computed edit distance
            return rows[curRow][m];
        }

        public static double GetAlignmentProbability(string word1, string word2, int maxSubstringLength, Dictionary<Pair<string, string>, double> probs, double minProb, double minProductionProbability)
        {
            return GetAlignmentProbability(word1, word2, maxSubstringLength, probs, minProb ,new Dictionary<Pair<string,string>,double>(), minProductionProbability);
        }

        public static double GetAlignmentProbabilityDebug(string word1, string word2, int maxSubstringLength, Dictionary<Pair<string, string>, double> probs, double minProb)
        {
            List<Pair<string, string>> productions;
            double result = GetAlignmentProbabilityDebug(word1, word2, maxSubstringLength, probs, minProb,new Dictionary<Pair<string,string>,Pair<double,List<Pair<string,string>>>>(),out productions);
            return result;
        }

        public static double GetAlignmentProbabilityDebug(string word1, string word2, int maxSubstringLength, Dictionary<Pair<string, string>, double> probs, out List<Pair<string, string>> productions)
        {
            return GetAlignmentProbabilityDebug(word1, word2, maxSubstringLength, probs, 0, new Dictionary<Pair<string, string>, Pair<double, List<Pair<string, string>>>>(), out productions);            
        }

        public static double GetAlignmentProbabilityDebug(string word1, string word2, int maxSubstringLength, Dictionary<Pair<string, string>, double> probs, double floorProb, Dictionary<Pair<string, string>, Pair<double,List<Pair<string,string>>>> memoizationTable, out List<Pair<string,string>> productions)
        {
            productions = new List<Pair<string, string>>();
            Pair<string,string> bestPair = new Pair<string,string>(null,null);

            if (word1.Length == 0 && word2.Length == 0) return 1;
            if (word1.Length * maxSubstringLength < word2.Length) return 0; //no alignment possible
            if (word2.Length * maxSubstringLength < word1.Length) return 0;

            Pair<double, List<Pair<string, string>>> cached;
            if (memoizationTable.TryGetValue(new Pair<string, string>(word1, word2), out cached))
            {
                productions = cached.y;
                return cached.x;
            }

            double maxProb = 0;

            int maxSubstringLength1 = Math.Min(word1.Length, maxSubstringLength);
            int maxSubstringLength2 = Math.Min(word2.Length, maxSubstringLength);

            for (int i = 1; i <= maxSubstringLength1; i++)
            {
                string substring1 = word1.Substring(0, i);
                for (int j = 0; j <= maxSubstringLength2; j++)
                {
                    double localProb;
                    if (probs.TryGetValue(new Pair<string, string>(substring1, word2.Substring(0, j)), out localProb))
                    {
                        //double localProb = ((double)count) / totals[substring1];
                        if (localProb < maxProb || localProb < floorProb) continue; //this is a really bad transition--discard

                        List<Pair<string, string>> outProductions;
                        localProb *= GetAlignmentProbabilityDebug(word1.Substring(i), word2.Substring(j), maxSubstringLength, probs, maxProb / localProb, memoizationTable, out outProductions);
                        if (localProb > maxProb)
                        {
                            productions = outProductions;
                            maxProb = localProb;
                            bestPair = new Pair<string, string>(substring1, word2.Substring(0, j));
                        }
                    }
                }
            }

            productions = new List<Pair<string, string>>(productions); //clone it before modifying
            productions.Insert(0, bestPair);

            memoizationTable[new Pair<string, string>(word1, word2)] = new Pair<double,List<Pair<string,string>>>(maxProb,productions);
            
            return maxProb;
        }

        public static Map<string, string> GetProbMap(Dictionary<Pair<string, string>, double> probs)
        {
            Map<string, string> result = new Map<string, string>();
            foreach (Pair<string, string> pair in probs.Keys)
                result.Add(pair);

            return result;
        }

        public static Map<Pair<string, string>, string> GetProbMap(Dictionary<Triple<string, string, string>, double> probs)
        {
            Map<Pair<string, string>, string> result = new Map<Pair<string, string>, string>();
            foreach (Triple<string,string,string> triple in probs.Keys)                
                    result.Add(triple.XY, triple.z);

            return result;
        }

        public static Map<Pair<string,string>, string> GetProbMap(Dictionary<Triple<string, string, string>, double> probs, int topK)
        {
            Dictionary<Pair<string, string>, TopList<double, string>> topProductions = new Dictionary<Pair<string, string>, TopList<double, string>>();
            foreach (KeyValuePair<Triple<string, string, string>, double> pair in probs)
            {
                if (!topProductions.ContainsKey(pair.Key.XY))
                    topProductions[pair.Key.XY] = new TopList<double, string>(topK);

                topProductions[pair.Key.XY].Add(pair.Value, pair.Key.z);
            }


            Map<Pair<string, string>, string> result = new Map<Pair<string, string>, string>();
            foreach (KeyValuePair<Pair<string, string>, TopList<double, string>> pair in topProductions)
                foreach (string generation in pair.Value.Values)
                    result.Add(pair.Key, generation);

            return result;
        }

        public static Dictionary<string, Pair<string,double>> GetMaxProbs(Dictionary<Pair<string, string>, double> probs)
        {
            Dictionary<string, Pair<string, double>> maxProbs = new Dictionary<string, Pair<string, double>>();
            foreach (KeyValuePair<Pair<string, string>, double> prob in probs)
            {
                Pair<string, double> curResult;
                if (maxProbs.TryGetValue(prob.Key.x, out curResult))
                    if (curResult.y >= prob.Value) //not need to change
                        continue;

                maxProbs[prob.Key.x] = new Pair<string, double>(prob.Key.y, prob.Value);
            }

            return maxProbs;
        }

        public static TopList<double,string> Predict(int topK, string word1, int maxSubstringLength, Dictionary<string, Pair<string,double>> maxProbs, Dictionary<string, TopList<double,string>> memoizationTable)
        {
            TopList<double, string> result;
            if (word1.Length == 0)
            {
                result = new TopList<double, string>(1);
                result.Add(1, "");
                return result;
            }
            
            if (memoizationTable.TryGetValue(word1, out result))
                return result;

            result = new TopList<double, string>(topK);

            int maxSubstringLength1 = Math.Min(word1.Length, maxSubstringLength);            

            for (int i = 1; i <= maxSubstringLength1; i++)
            {
                string substring1 = word1.Substring(0, i);
                Pair<string,double> maxWord;
                if (maxProbs.TryGetValue(substring1, out maxWord))
                {
                    TopList<double, string> bestAppends = Predict(topK, word1.Substring(i), maxSubstringLength, maxProbs, memoizationTable);
                    foreach (KeyValuePair<double, string> pair in bestAppends)                    
                        if (result.Add(pair.Key * maxWord.y, maxWord.x + pair.Value,true) < 0) break;                    
                }                    
            }

            memoizationTable[word1] = result;
            return result;
        }        

        public static Dictionary<string, int> GetNgramCounts(int n, IEnumerable<string> examples, bool pad)
        {
            Dictionary<string, int> result = new Dictionary<string, int>();
            foreach (string example in examples)
            {
                string paddedExample = (pad ? new string('_', n - 1) + example : example);

                for (int i = 0; i <= paddedExample.Length - n; i++)
                    Dictionaries.IncrementOrSet<string>(result, paddedExample.Substring(i, n), 1, 1);
            }

            return result;
        }

        public static Dictionary<string, double> GetFixedSizeNgramProbs(int n, IEnumerable<string> examples)
        {
            Dictionary<string, int> ngramCounts = GetNgramCounts(n, examples,true);
            Dictionary<string, int> ngramTotals = new Dictionary<string, int>();
            foreach (KeyValuePair<string, int> ngramPair in ngramCounts)
                Dictionaries.IncrementOrSet<string>(ngramTotals, ngramPair.Key.Substring(0, n - 1), ngramPair.Value, ngramPair.Value);

            Dictionary<string, double> result = new Dictionary<string, double>(ngramCounts.Count);
            foreach (KeyValuePair<string, int> ngramPair in ngramCounts)
                result[ngramPair.Key] = ((double)ngramPair.Value) / ngramTotals[ngramPair.Key.Substring(0, n - 1)];

            return result;
        }

        public static Dictionary<string, double> GetNgramProbs(int minN, int maxN, IEnumerable<string> examples)
        {
            Dictionary<string,double> result = new Dictionary<string,double>();
            for (int i = minN; i <= maxN; i++)
                foreach (KeyValuePair<string, double> probPair in GetFixedSizeNgramProbs(i, examples))
                    result.Add(probPair.Key,probPair.Value);

            return result;
        }

        public static Dictionary<string, double> GetNgramCounts(int minN, int maxN, IEnumerable<string> examples, bool padding)
        {
            Dictionary<string, double> result = new Dictionary<string, double>();
            for (int i = minN; i <= maxN; i++)
            {
                Dictionary<string, int> counts = GetNgramCounts(i, examples, padding);
                int total = 0;
                foreach (KeyValuePair<string, int> probPair in counts)
                    total += probPair.Value;

                foreach (KeyValuePair<string, int> probPair in counts)
                    result.Add(probPair.Key, ((double)probPair.Value) / total);
            }

            return result;
        }

        public static double GetLanguageProbability(string word, Dictionary<string, double> ngramProbs, int ngramSize)
        {
            double probability = 1;
            string paddedExample = new string('_', ngramSize - 1) + word;
            for (int i = ngramSize-1; i < paddedExample.Length; i++)
            {
                double localProb;
                int n = ngramSize;
                while (!ngramProbs.TryGetValue(paddedExample.Substring(i - n+1, n), out localProb)) { n--; if (n == 0) return 0; }
                probability *= localProb;
            }

            return probability;
        }

        public static double GetLanguageProbability2(string word, Dictionary<string, double> ngramProbs, int ngramSize)
        {
            double probability = 1;
            string paddedExample = word;
            for (int i = 0; i < paddedExample.Length; i++)
            {
                double localProb;
                int n = ngramSize;
                while (!ngramProbs.TryGetValue(paddedExample.Substring(i, Math.Min(paddedExample.Length-i,n)), out localProb)) { n--; if (n == 0) return 0; }
                probability *= localProb;
                i += n - 1; //skip consumed characters
            }

            return probability;
        }

        public static double GetLanguageProbability3(string word, Dictionary<string, double> ngramProbs, int ngramSize)
        {
            double probability = 1;
            string paddedExample = word;
            for (int i = 0; i < paddedExample.Length; i++)
            {
                double localProb;
                int n = ngramSize;
                while (!ngramProbs.TryGetValue(paddedExample.Substring(i, Math.Min(paddedExample.Length - i, n)), out localProb)) { n--; if (n == 0) return 0; }
                probability *= localProb;                
            }

            return probability;
        }

        public static double GetLanguageProbabilityViterbi(string word, Dictionary<string, double> ngramProbs, int ngramSize)
        {
            return GetLanguageProbabilityViterbi(word, ngramProbs, ngramSize, new Dictionary<string, double>());
        }

        public static double GetLanguageProbabilityViterbi(string word, Dictionary<string, double> ngramProbs, int ngramSize, Dictionary<string, double> memoized)
        {   
            double result = 0;
            if (word.Length == 0) return 1;
            if (memoized.TryGetValue(word,out result)) return result;
            
            
            int maxLength = Math.Min(word.Length, ngramSize);
            for (int i = 1; i <= maxLength; i++)
            {
                string substring = word.Substring(0,i);
                double localProb;
                if (ngramProbs.TryGetValue(substring, out localProb))
                {
                    result = Math.Max(result, localProb * GetLanguageProbabilityViterbi(word.Substring(i), ngramProbs, ngramSize, memoized));
                }
                else
                    break; //couldn't find shorter ngram, won't find longer ones
            }

            return memoized[word] = result;            
        }

        public static TopList<double, string> PredictLog(int topK, string word1, int maxSubstringLength, Map<string, string> probMap, Dictionary<Pair<string, string>, double> probs, Dictionary<string, TopList<double, string>> memoizationTable, Dictionary<string, int> ngramCounts, int ngramSize)
        {
            TopList<double, string> result;
            if (word1.Length == 0)
            {
                result = new TopList<double, string>(1);
                result.Add(0, "");
                return result;
            }

            if (memoizationTable.TryGetValue(word1, out result))
                return result;

            result = new TopList<double, string>(topK);

            int maxSubstringLength1 = Math.Min(word1.Length, maxSubstringLength);

            for (int i = 1; i <= maxSubstringLength1; i++)
            {
                string substring1 = word1.Substring(0, i);
                
                if (probMap.ContainsKey(substring1))
                {
                    TopList<double, string> bestAppends = PredictLog(topK, word1.Substring(i), maxSubstringLength, probMap, probs, memoizationTable,ngramCounts,ngramSize);
                    foreach (Pair<string, string> alignment in probMap.GetPairsForKey(substring1))
                    {
                        foreach (KeyValuePair<double, string> pair in bestAppends)
                        {
                            string word = alignment.y + pair.Value;
                            //if (ngramCounts != null && GetLanguageProbability(word, ngramCounts, ngramSize) == 0) continue;
                            if (result.Add(pair.Key + probs[alignment], word, true) < 0) break;
                        }
                    }
                }
            }

            memoizationTable[word1] = result;
            return result;
        }

        public static TopList<double, string> PredictViterbi(int topK, int contextSize, bool fallback, string word1, int maxSubstringLength, Map<Pair<string, string>, string> probMap, Dictionary<Triple<string, string, string>, double> probs)
        {
            Dictionary<string, TopList<double,string>>[] viterbiArray = new Dictionary<string, TopList<double,string>>[word1.Length+1];
            for (int i = 0; i < viterbiArray.Length; i++)
                viterbiArray[i] = new Dictionary<string,TopList<double,string>>();

            viterbiArray[0][new string('_', contextSize)] = new TopList<double, string>(1);
            viterbiArray[0][new string('_', contextSize)].Add(1, "");

            TopList<double, string> results = new TopList<double, string>(topK);

            for (int start = 0; start < word1.Length; start++)
            {
                for (int length = 1; length <= word1.Length - start; length++)
                {
                    string sourceSubstring = word1.Substring(start,length);

                    foreach (KeyValuePair<string,TopList<double,string>> contextAndOutputPair in viterbiArray[start])
                    {
                        double pastProb = contextAndOutputPair.Value[0].Key;
                        string bestPrepend = contextAndOutputPair.Value[0].Value;

                        if (results.Count == topK && results[topK - 1].Key > pastProb)
                            continue; //it'll never get us anywhere

                        Pair<string, string> contextAndSource = new Pair<string, string>(contextAndOutputPair.Key, sourceSubstring);
                        string originalContext = contextAndSource.x;

                        //fallback?
                        if (fallback)
                            while (!probMap.ContainsKey(contextAndSource) && contextAndSource.x.Length > 0)
                                contextAndSource.x = contextAndSource.x.Substring(1);

                        foreach (KeyValuePair<Pair<string, string>, string> production in probMap.GetPairsForKey(contextAndSource))
                        {
                            double probability = pastProb * probs[new Triple<string, string, string>(production.Key.x, production.Key.y, production.Value)];

                            if (results.Count == topK && results[topK - 1].Key > probability)
                                continue; //it'll never get us anywhere

                            string newContext;
                            if (production.Value.Length == contextSize) //speed things up by handling this special case quickly
                                newContext = production.Value;
                            else if (production.Value.Length > contextSize)
                                newContext = production.Value.Substring(production.Value.Length - contextSize);
                            else
                                newContext = originalContext.Substring(production.Value.Length) + production.Value;

                            TopList<double,string> outputTopList;
                            if (!viterbiArray[start + length].TryGetValue(newContext, out outputTopList))
                                viterbiArray[start + length][newContext] = outputTopList = new TopList<double, string>(topK);

                            if (outputTopList.Count < topK || outputTopList[topK - 1].Key < probability) //don't add strings unless you have to!                            
                                if (outputTopList.Add(probability, bestPrepend + production.Value, true) >= 0 && start + length == word1.Length)
                                    results.Add(probability, bestPrepend + production.Value, true);
                        }
                    }
                }
            }

            //TopList<double, string> result = new TopList<double, string>(topK);
            //foreach (KeyValuePair<string, TopList<double, string>> contextAndOutputPair in viterbiArray[viterbiArray.Length - 1])
            //{
            //    result.Add(contextAndOutputPair.Value[0].Key, contextAndOutputPair.Value[0].Value);
            //}

            return results;
        }

        public static TopList<double, string> Predict(int topK, string word1, int maxSubstringLength, Map<string, string> probMap, Dictionary<Pair<string, string>, double> probs, Dictionary<string, TopList<double, string>> memoizationTable, Dictionary<string, int> ngramCounts, int ngramSize)
        {
            TopList<double, string> result;
            if (word1.Length == 0)
            {
                result = new TopList<double, string>(1);
                result.Add(1, "");
                return result;
            }

            if (memoizationTable.TryGetValue(word1, out result))
                return result;

            result = new TopList<double, string>(topK);

            int maxSubstringLength1 = Math.Min(word1.Length, maxSubstringLength);

            for (int i = 1; i <= maxSubstringLength1; i++)
            {
                string substring1 = word1.Substring(0, i);

                if (probMap.ContainsKey(substring1))
                {
                    TopList<double, string> bestAppends = Predict(topK, word1.Substring(i), maxSubstringLength, probMap, probs, memoizationTable, ngramCounts, ngramSize);
                    foreach (Pair<string, string> alignment in probMap.GetPairsForKey(substring1))
                    {
                        foreach (KeyValuePair<double, string> pair in bestAppends)
                        {
                            string word = alignment.y + pair.Value;
                            //if (ngramCounts != null && GetLanguageProbability(word, ngramCounts, ngramSize) == 0) continue;
                            if (result.Add(pair.Key * probs[alignment], word, true) < 0) break;
                        }
                    }
                }
            }

            memoizationTable[word1] = result;
            return result;
        }

        public static TopList<double, string> Predict2(int topK, string word1, int maxSubstringLength, Map<string, string> probMap, Dictionary<Pair<string, string>, double> probs, Dictionary<string, Dictionary<string, double>> memoizationTable, int pruneToSize)
        {
            TopList<double, string> result = new TopList<double, string>(topK);
            Dictionary<string, double> rProbs = Predict2(word1, maxSubstringLength, probMap, probs, memoizationTable, pruneToSize);
            double probSum = 0;

            foreach (double prob in rProbs.Values) probSum += prob;

            foreach (KeyValuePair<string, double> pair in rProbs)
                result.Add(pair.Value / probSum, pair.Key);

            return result;
        }

        public static int Segmentations(int characters)
        {
            if (characters <= 1) return 1;
            //2->2
            //abc -> 4
            //abcd -> abcd, a-b-c-d, ab-c-d, a-bc-d. a-b-cd, ab-cd, abc-d, a-bcd = 8
            // each character after the first is either attached to the previous character or separate (2 choices)
            // -> segmentations == 2^(characters-1)
            else return 1 << (characters - 1);
        }        

        public static Dictionary<string, double> Predict2(string word1, int maxSubstringLength, Map<string, string> probMap, Dictionary<Pair<string, string>, double> probs, Dictionary<string, Dictionary<string,double>> memoizationTable, int pruneToSize)
        {
            Dictionary<string, double> result;
            if (word1.Length == 0)
            {
                result = new Dictionary<string, double>(1);
                result.Add("",1);
                return result;
            }

            if (memoizationTable.TryGetValue(word1, out result))
                return result;

            result = new Dictionary<string, double>();

            int maxSubstringLength1 = Math.Min(word1.Length, maxSubstringLength);

            for (int i = 1; i <= maxSubstringLength1; i++)
            {
                string substring1 = word1.Substring(0, i);

                if (probMap.ContainsKey(substring1))
                {
                    Dictionary<string,double> appends = Predict2( word1.Substring(i), maxSubstringLength, probMap, probs, memoizationTable, pruneToSize );

                    //int segmentations = Segmentations( word1.Length - i );

                    foreach (Pair<string, string> alignment in probMap.GetPairsForKey(substring1))
                    {
                        double alignmentProb = probs[alignment];

                        foreach (KeyValuePair<string, double> pair in appends)
                        {
                            string word = alignment.y + pair.Key;
                            //double combinedProb = (pair.Value/segmentations) * alignmentProb;
                            double combinedProb = (pair.Value ) * alignmentProb;
                            Dictionaries.IncrementOrSet<string>(result, word, combinedProb, combinedProb);
                        }
                    }
                }
            }

            if (result.Count > pruneToSize)
            {
                double[] valuesArray = new double[result.Count];
                string[] data = new string[result.Count];
                result.Values.CopyTo(valuesArray, 0);
                result.Keys.CopyTo(data, 0);

                Array.Sort<double,string>(valuesArray,data);

                //double sum = 0;
                //for (int i = data.Length - pruneToSize; i < data.Length; i++)
                //    sum += valuesArray[i];

                result = new Dictionary<string, double>(pruneToSize);
                for (int i = data.Length - pruneToSize; i < data.Length; i++)
                    result.Add(data[i], valuesArray[i]);///sum);
            }

            memoizationTable[word1] = result;
            return result;
        }


        public static TopList<double, string> Predict(int topK, int contextSize, string word1, int maxSubstringLength, Map<Pair<string, string>, string> probMap, Dictionary<Triple<string, string, string>, double> probs, Dictionary<Pair<string, string>, TopList<double, string>> memoizationTable)
        {
            return Predict(topK, new string('_', contextSize), word1, maxSubstringLength, probMap, probs, new Dictionary<Pair<string, string>, TopList<double, string>>());
        }

        public static TopList<double, string> Predict(int topK, string context, string word1, int maxSubstringLength, Map<Pair<string,string>, string> probMap, Dictionary<Triple<string, string, string>, double> probs, Dictionary<Pair<string,string>, TopList<double, string>> memoizationTable)
        {
            TopList<double, string> result;
            if (word1.Length == 0)
            {
                result = new TopList<double, string>(1);
                result.Add(1, "");
                return result;
            }

            if (memoizationTable.TryGetValue(new Pair<string,string>(context,word1), out result))
                return result;

            result = new TopList<double, string>(topK);

            int maxSubstringLength1 = Math.Min(word1.Length, maxSubstringLength);

            for (int i = 1; i <= maxSubstringLength1; i++)
            {
                string substring1 = word1.Substring(0, i);

                string adjustedContext = context;

                //fallback as necessary
                while (adjustedContext.Length > 0 && !probMap.ContainsKey(new Pair<string, string>(adjustedContext, substring1)))
                    adjustedContext = adjustedContext.Substring(1);

                if (adjustedContext.Length > 0 || probMap.ContainsKey(new Pair<string, string>(adjustedContext, substring1)))
                {
                    foreach (KeyValuePair<Pair<string,string>, string> alignment in probMap.GetPairsForKey(new Pair<string, string>(adjustedContext, substring1)))
                    {
                        string newContext;
                        if (alignment.Value.Length < context.Length)
                            newContext = context.Substring(alignment.Value.Length) + alignment.Value;
                        else
                            newContext = alignment.Value.Substring(alignment.Value.Length - context.Length, context.Length);

                        TopList<double, string> bestAppends = Predict(topK, newContext, word1.Substring(i), maxSubstringLength, probMap, probs, memoizationTable);
                        foreach (KeyValuePair<double, string> pair in bestAppends)
                        {
                            string word = alignment.Value + pair.Value;
                            //if (ngramCounts != null && GetLanguageProbability(word, ngramCounts, ngramSize) == 0) continue;
                            if (result.Add(pair.Key * probs[new Triple<string,string,string>(alignment.Key.x,alignment.Key.y,alignment.Value)], word, true) < 0) break;
                        }
                    }
                }
            }

            memoizationTable[new Pair<string,string>(context,word1)] = result;
            return result;
        }


        public static Dictionary<Pair<string,string>,bool> GetProductions(string word1, string word2, int maxSubstringLength, Dictionary<Pair<string, string>, bool> memoizationTable)
        {           
            Dictionary<Pair<string, string>, bool> result = new Dictionary<Pair<string, string>, bool>();
           
            if (memoizationTable.ContainsKey(new Pair<string, string>(word1, word2)))
                return result;

            int maxSubstringLength1 = Math.Min(word1.Length, maxSubstringLength);
            int maxSubstringLength2 = Math.Min(word2.Length, maxSubstringLength);

            for (int i = 1; i <= maxSubstringLength1; i++)
            {
                string substring1 = word1.Substring(0, i);
                for (int j = 0; j <= maxSubstringLength2; j++)
                {
                    if ((word1.Length - i) * maxSubstringLength2 >= word2.Length - j && (word2.Length - j) * maxSubstringLength1 >= word1.Length - i) //if we get rid of these characters, can we still cover the remainder of word2?
                    {
                        string substring2 = word2.Substring(0, j);
                        result[new Pair<string, string>(substring1, substring2)] = true;

                        foreach (KeyValuePair<Pair<string, string>, bool> pair in GetProductions(word1.Substring(i), word2.Substring(j), maxSubstringLength, memoizationTable))
                            result[pair.Key] = true;
                    }
                }
            }

            memoizationTable[new Pair<string, string>(word1, word2)] = true;
            return result;
        }

        //Finds P(word2|word1, model)
        public static double GetConditionalProbability(string word1, string word2, ContextModel model)
        {
            int paddingSize = Math.Max(model.productionContextSize, model.segContextSize);
            string paddedWord = new string('_', paddingSize) + word1 + new string('_', paddingSize);
            return GetConditionalProbability(paddingSize, paddingSize, paddedWord, word1, word2, model, new SparseDoubleVector<Pair<string, string>>());
        }

        //Finds P(word2|word1, model)
        public static double GetConditionalProbability(int position, int startPosition, string originalWord1, string word1, string word2, ContextModel model, SparseDoubleVector<Pair<string,string>> memoizationTable)
        {
            //double memoized;
            //if (memoizationTable.TryGetValue(new Pair<string, string>(word1, word2), out memoized))
            //    return memoized; //we've been down this road before            

            //if (word1.Length == 0 && word2.Length == 0) //base case
            //    return 1;

            //int maxSubstringLength1f = Math.Min(word1.Length, model.maxSubstringLength);
            //int maxSubstringLength2f = Math.Min(word2.Length, model.maxSubstringLength);

            ////string leftContexts = GetLeftFallbackContexts(originalWord1, position, contextSize);
            ////string rightContexts = GetRightFallbackContexts(originalWord1, position, contextSize);

            ////string leftProductionContext = originalWord1.Substring(position - productionContextSize, productionContextSize);
            ////string rightProductionContext = originalWord1.Substring(position, productionContextSize);

            //string leftProductionContexts = GetLeftFallbackContexts(originalWord1, position, model.segContextSize);

            ////find the segmentation probability
            //double segProb;

            

            //for (int cs = model.segContextSize; cs >= 0; cs--)
            //    if (model.segProbs.TryGetValue(new Pair<string,string>(
            //            originalWord1.Substring(position - segContextSize, segContextSize),
            //            originalWord1.Substring(position, segContextSize)), out segProb)) break;

            
            //if (position == 0) segProb = 1; else { if (segProbs == null) segProb = 0.5; else segProb = segProbs[segContext]; }

            //for (int i = 1; i <= maxSubstringLength1f; i++) //for each possible substring in the first word...
            //{
            //    if (i > 1) //adjust segProb
            //    {
            //        if (segProbs == null) segProb *= 0.5;
            //        else segProb *= 1 - segProbs[new Pair<string, string>(originalWord1.Substring((position + i - 1) - segContextSize, segContextSize),
            //                                                            originalWord1.Substring(position + i - i, segContextSize))];
            //    }

            //    string substring1 = word1.Substring(0, i);

            //    for (int j = 1; j <= maxSubstringLength2f; j++) //foreach possible substring in the second
            //    {
            //        if ((word1.Length - i) * maxSubstringLength2 >= word2.Length - j && (word2.Length - j) * maxSubstringLength1 >= word1.Length - i) //if we get rid of these characters, can we still cover the remainder of word2?
            //        {
            //            string substring2 = word2.Substring(0, j);
            //            Pair<Triple<string, string, string>, string> production = new Pair<Triple<string, string, string>, string>(new Triple<string, string, string>(leftProductionContext, substring1, rightProductionContext), substring2);

            //            double prob;
            //            if (probs != null) prob = probs[production]; else prob = 1;

            //            Triple<SparseDoubleVector<Triple<int, string, string>>, SparseDoubleVector<int>, double> remainder = CountWeightedAlignments2(position + i, originalWord1, productionContextSize, segContextSize, word1.Substring(i), word2.Substring(j), maxSubstringLength1, maxSubstringLength2, probs, segProbs, memoizationTable);

            //            double cProb = prob * segProb;

            //            //record this production in our results

            //            //Dictionaries.IncrementOrSet<Pair<string, string>>(result, production, prob * remainderProbSum, prob * remainderProbSum);
            //            Dictionaries.IncrementOrSet<Triple<int, string, string>>(result.x, new Triple<int, string, string>(position, production.x.y, production.y), cProb * remainder.z, cProb * remainder.z);
            //            Dictionaries.IncrementOrSet<int>(result.y, position, cProb * remainder.z, cProb * remainder.z);

            //            //update our probSum
            //            //probSum += remainderProbSum * prob;
            //            result.z += remainder.z * cProb;

            //            result.x += remainder.x * cProb;
            //            result.y += remainder.y * cProb;
            //        }
            //    }
            //}

            //memoizationTable[new Triple<int, string, string>(position, word1, word2)] = result;
            //return result;

            return 0;
        }

        public static double GetAlignmentProbability(string word1, string word2, int maxSubstringLength, Dictionary<Pair<string, string>, double> probs, double floorProb, Dictionary<Pair<string, string>, double> memoizationTable, double minProductionProbability)
        {
            if (word1.Length==0 && word2.Length==0) return 1;
            if (word1.Length * maxSubstringLength < word2.Length) return 0; //no alignment possible
            if (word2.Length * maxSubstringLength < word1.Length) return 0;

            double maxProb = 0;
            if (memoizationTable.TryGetValue(new Pair<string,string>(word1,word2),out maxProb))
                return maxProb;

            int maxSubstringLength1 = Math.Min(word1.Length, maxSubstringLength);
            int maxSubstringLength2 = Math.Min(word2.Length, maxSubstringLength);

            double localMinProdProb = 1;
            for (int i = 1; i <= maxSubstringLength1; i++)
            {
                localMinProdProb *= minProductionProbability; //punish longer substrings
                if (localMinProdProb < floorProb) localMinProdProb = 0;

                string substring1 = word1.Substring(0,i);
                for (int j = 0; j <= maxSubstringLength2; j++)
                {
                    double localProb=0;
                    if (!probs.TryGetValue(new Pair<string, string>(substring1, word2.Substring(0, j)), out localProb))                    
                        if (localMinProdProb == 0) continue;

                    localProb = Math.Max(localProb, localMinProdProb);

                    //double localProb = ((double)count) / totals[substring1];
                    if (localProb < maxProb || localProb < floorProb) continue; //this is a really bad transition--discard

                    localProb *= GetAlignmentProbability(word1.Substring(i), word2.Substring(j), maxSubstringLength, probs, Math.Max(floorProb, maxProb / localProb), memoizationTable, minProductionProbability);
                    if (localProb > maxProb)
                        maxProb = localProb;
                    
                }
            }

            memoizationTable[new Pair<string,string>(word1,word2)] = maxProb;
            return maxProb;
        }

        public static Dictionary<string, double> GetAlignmentTotals1(Dictionary<Pair<string, string>, double> counts)
        {
            Dictionary<string,double> result = new Dictionary<string,double>();
            foreach (KeyValuePair<Pair<string, string>, double> pair in counts)            
                Dictionaries.IncrementOrSet<string>(result, pair.Key.x, pair.Value, pair.Value);

            return result;
        }

        public static Dictionary<Pair<string,string>, double> GetAlignmentTotals1(Dictionary<Triple<string, string, string>, double> counts)
        {
            Dictionary<Pair<string, string>, double> result = new Dictionary<Pair<string, string>, double>();
            foreach (KeyValuePair<Triple<string, string, string>, double> pair in counts)
                Dictionaries.IncrementOrSet<Pair<string,string>>(result, pair.Key.XY, pair.Value, pair.Value);

            return result;
        }

        public static Dictionary<string, double> GetAlignmentTotalsForSource(Dictionary<Triple<string, string, string>, double> counts)
        {
            Dictionary<string, double> result = new Dictionary<string, double>();
            foreach (KeyValuePair<Triple<string, string, string>, double> pair in counts)
                Dictionaries.IncrementOrSet<string>(result, pair.Key.y, pair.Value, pair.Value);

            return result;
        }


        public static Dictionary<string, double> GetAlignmentTotals2(Dictionary<Triple<string, string, string>, double> counts)
        {
            Dictionary<string, double> result = new Dictionary<string, double>();
            foreach (KeyValuePair<Triple<string, string, string>, double> pair in counts)
                Dictionaries.IncrementOrSet<string>(result, pair.Key.z, pair.Value, pair.Value);

            return result;
        }

        public static Dictionary<string, double> GetAlignmentTotals2(Dictionary<Pair<string, string>, double> counts)
        {
            Dictionary<string, double> result = new Dictionary<string, double>();
            foreach (KeyValuePair<Pair<string, string>, double> pair in counts)
                Dictionaries.IncrementOrSet<string>(result, pair.Key.y, pair.Value, pair.Value);

            return result;
        }

        public static Dictionary<Pair<string, string>, double> FindLogAlignments(string word1, string word2, int maxSubstringLength1, int maxSubstringLength2, InternDictionary<string> internTable,bool normalize)
        {
            Dictionary<Pair<string, string>, bool> alignments = new Dictionary<Pair<string, string>, bool>();
            FindAlignments(word1, word2, maxSubstringLength1, maxSubstringLength2, alignments, new Dictionary<Pair<string, string>, bool>());

            double total = Math.Log(alignments.Count);


            Dictionary<Pair<string, string>, double> result = new Dictionary<Pair<string, string>, double>(alignments.Count);
            foreach (KeyValuePair<Pair<string, string>, bool> pair in alignments)
                result[new Pair<string, string>(internTable.Intern(pair.Key.x), internTable.Intern(pair.Key.y))] = (normalize ? -total : 0);

            return result;
        }

        public static Dictionary<Pair<string, string>, double> FindAlignments(string word1, string word2, int maxSubstringLength1, int maxSubstringLength2, InternDictionary<string> internTable, NormalizationMode normalization)
        {
            Dictionary<Pair<string, string>, bool> alignments = new Dictionary<Pair<string, string>, bool>();
            FindAlignments(word1, word2, maxSubstringLength1, maxSubstringLength2, alignments, new Dictionary<Pair<string, string>, bool>());

            int total = alignments.Count;

            Dictionary<Pair<string, string>, double> result = new Dictionary<Pair<string, string>, double>(alignments.Count);
            foreach (KeyValuePair<Pair<string, string>, bool> pair in alignments)
                result[new Pair<string, string>(internTable.Intern(pair.Key.x), internTable.Intern(pair.Key.y))] = 1;

            return Normalize(word1, word2, result, internTable, normalization);
        }

        public static Dictionary<Pair<string, string>, double> CountAlignments(string word1, string word2, int maxSubstringLength, InternDictionary<string> internTable)
        {
            Dictionary<Pair<string, string>, int> counts = CountAlignments(word1, word2, maxSubstringLength, new Dictionary<Pair<string, string>, Dictionary<Pair<string, string>, int>>());
            
            long total=0;
            foreach (int value in counts.Values)
                total += value;

            Dictionary<Pair<string, string>, double> result = new Dictionary<Pair<string, string>, double>(counts.Count);
            foreach (KeyValuePair<Pair<string, string>, int> pair in counts)
                result[new Pair<string,string>(internTable.Intern(pair.Key.x),internTable.Intern(pair.Key.y))] = ((double)pair.Value) / total;

            return result;
        }
            //for (int i = 0; i < word1.Length; i++)
            //{
            //    int localMaxSSL = Math.Min(maxSubstringLength, word1.Length - i);
            //    for (int j = 0; j < localMaxSSL; j++)
            //    {

            //        CountAlignments(word1.Substring(i+j)
            //    }
            //}

        //public static Dictionary<Pair<string, string>, double> FindWeightedAlignments(string word1, string word2, int maxSubstringLength, Dictionary<Pair<string, string>, Dictionary<Pair<string, string>, double>> memoizationTable, Dictionary<Pair<string,string>,double> probs)
        //{            
        //    Dictionary<Pair<string, string>, double> weights;
        //    if (memoizationTable.TryGetValue(new Pair<string, string>(word1, word2), out weights))
        //        return weights; //done
        //    else
        //        weights = new Dictionary<Pair<string, string>, double>();

        //    int maxSubstringLength1 = Math.Min(word1.Length, maxSubstringLength);
        //    int maxSubstringLength2 = Math.Min(word2.Length, maxSubstringLength);

        //    for (int i = 1; i <= maxSubstringLength1; i++) //for each possible substring in the first word...
        //    {
        //        string substring1 = word1.Substring(0, i);

        //        for (int j = 1; j <= maxSubstringLength2; j++) //foreach possible substring in the second
        //        {
        //            if ((word1.Length - i) * maxSubstringLength >= word2.Length - j && (word2.Length - j) * maxSubstringLength >= word1.Length - i) //if we get rid of these characters, can we still cover the remainder of word2?
        //            {
        //                string substring2 = word2.Substring(0, j);
        //                double prob = probs[new Pair<string,string>(substring1,substring2)];

        //                Dictionary<Pair<string,string>,double> recursiveWeights = FindWeightedAlignments(substring1,substring2,maxSubstringLength,memoizationTable,probs);
        //                foreach (KeyValuePair<Pair<string,string>,double> pair in recursiveWeights)
        //                {
        //                    double existingWeight;
        //                    if (!weights.TryGetValue(pair.Key,out existingWeight))
        //                        weights[pair.Key] = pair.Value*prob;
        //                    else
        //                        weights[pair.Key] = Math.Max(pair.Value*prob,existingWeight);
        //                }
        //                Dictionaries.IncrementOrSet<Pair<string, string>>(weights, new Pair<string, string>(substring1, word2.Substring(0, j)), 1, 1);
        //                Dictionaries.AddTo<Pair<string, string>>(weights, FindWeightedAlignments(word1.Substring(i), word2.Substring(j), maxSubstringLength, memoizationTable,probs), 1);
        //            }
        //        }
        //    }

        //    memoizationTable[new Pair<string, string>(word1, word2)] = weights;
        //    return weights;
        //}

        public static void CheckDictionary(Dictionary<Pair<string, string>, double> dict)
        {
            foreach (KeyValuePair<Pair<string, string>, double> pair in dict)
                if (double.IsInfinity(pair.Value) || double.IsNaN(pair.Value))
                    Console.WriteLine("Bad entry");
        }

        public static Dictionary<Pair<string, string>, double> FindLogWeightedAlignments(string word1, string word2, int maxSubstringLength1, int maxSubstringLength2, Dictionary<Pair<string, string>, double> probs, InternDictionary<string> internTable, bool normalize)
        {
            Dictionary<Pair<string, string>, double> weights = new Dictionary<Pair<string, string>, double>();
            FindLogWeightedAlignments(1, new List<Pair<string, string>>(), word1, word2, maxSubstringLength1, maxSubstringLength2, probs, weights, new Dictionary<Pair<string, string>, Pair<double, double>>());

            //CheckDictionary(weights);

            double total = 0;
            foreach (KeyValuePair<Pair<string, string>, double> pair in weights)
                total += Math.Exp(pair.Value);

            total = Math.Log(total);

            Dictionary<Pair<string, string>, double> result = new Dictionary<Pair<string, string>, double>(weights.Count);

            //if (total <= 0 || double.IsNaN(total) || double.IsInfinity(total))
            //    Console.WriteLine("Total == 0");
            foreach (KeyValuePair<Pair<string, string>, double> pair in weights)
                result[new Pair<string, string>(internTable.Intern(pair.Key.x), internTable.Intern(pair.Key.y))] = pair.Value - (normalize ? total : 0);

            return result;
        }

        public static Dictionary<Triple<string, string, string>, double> InternProductions(Dictionary<Triple<string, string, string>, double> counts, InternDictionary<string> internTable)
        {
            Dictionary<Triple<string, string, string>, double> result = new Dictionary<Triple<string, string, string>, double>(counts.Count);
            
            foreach (KeyValuePair<Triple<string, string, string>, double> pair in counts)
                result[new Triple<string, string, string>(internTable.Intern(pair.Key.x), internTable.Intern(pair.Key.y), internTable.Intern(pair.Key.z))] = pair.Value;

            return result;
        }

        public static Dictionary<Pair<string, string>, double> InternProductions(Dictionary<Pair<string, string>, double> counts, InternDictionary<string> internTable)
        {
            Dictionary<Pair<string, string>, double> result = new Dictionary<Pair<string, string>, double>(counts.Count);

            //if (total <= 0 || double.IsNaN(total) || double.IsInfinity(total))
            //    Console.WriteLine("Total == 0");
            foreach (KeyValuePair<Pair<string, string>, double> pair in counts)
                result[new Pair<string, string>(internTable.Intern(pair.Key.x), internTable.Intern(pair.Key.y))] = pair.Value;

            return result;
        }

        public static Dictionary<Pair<string, string>, double> NormalizeAllProductions(Dictionary<Pair<string, string>, double> counts, InternDictionary<string> internTable)
        {
            double total = 0;
            foreach (KeyValuePair<Pair<string, string>, double> pair in counts)
                total += pair.Value;

            Dictionary<Pair<string, string>, double> result = new Dictionary<Pair<string, string>, double>(counts.Count);

            //if (total <= 0 || double.IsNaN(total) || double.IsInfinity(total))
            //    Console.WriteLine("Total == 0");
            foreach (KeyValuePair<Pair<string, string>, double> pair in counts)
                result[new Pair<string, string>(internTable.Intern(pair.Key.x), internTable.Intern(pair.Key.y))] = pair.Value/total;

            return result;
        }

        public static Dictionary<Pair<string, string>, double> NormalizeBySourceSubstring(Dictionary<Pair<string, string>, double> counts, InternDictionary<string> internTable)
        {
            Dictionary<string, double> totals = GetAlignmentTotals1(counts);

            Dictionary<Pair<string, string>, double> result = new Dictionary<Pair<string, string>, double>(counts.Count);

            //if (total <= 0 || double.IsNaN(total) || double.IsInfinity(total))
            //    Console.WriteLine("Total == 0");
            foreach (KeyValuePair<Pair<string, string>, double> pair in counts)
                result[new Pair<string, string>(internTable.Intern(pair.Key.x), internTable.Intern(pair.Key.y))] = pair.Value/totals[pair.Key.x];

            return result;
        }

        public static Dictionary<Triple<string, string, string>, double> NormalizeBySourceSubstring(Dictionary<Triple<string, string, string>, double> counts, InternDictionary<string> internTable)
        {
            Dictionary<string, double> totals = GetAlignmentTotalsForSource(counts);

            Dictionary<Triple<string, string, string>, double> result = new Dictionary<Triple<string, string, string>, double>(counts.Count);
            
            foreach (KeyValuePair<Triple<string, string, string >, double> pair in counts)            
                result[new Triple<string, string, string>(internTable.Intern(pair.Key.x), internTable.Intern(pair.Key.y), internTable.Intern(pair.Key.z))] = (pair.Value > 0 ? pair.Value / totals[pair.Key.y] : 0);

            return result;
        }


        public static Dictionary<Pair<string, string>, double> NormalizeByTargetSubstring(Dictionary<Pair<string, string>, double> counts, InternDictionary<string> internTable)
        {
            Dictionary<string, double> totals = GetAlignmentTotals2(counts);

            Dictionary<Pair<string, string>, double> result = new Dictionary<Pair<string, string>, double>(counts.Count);

            foreach (KeyValuePair<Pair<string, string>, double> pair in counts)
                result[new Pair<string, string>(internTable.Intern(pair.Key.x), internTable.Intern(pair.Key.y))] = pair.Value / totals[pair.Key.y];

            return result;
        }

        public static Dictionary<Pair<string, string>, double> NormalizeBySourceAndTargetSubstring(Dictionary<Pair<string, string>, double> counts, InternDictionary<string> internTable)
        {
            Dictionary<string, double> totals1 = GetAlignmentTotals1(counts);
            Dictionary<string, double> totals2 = GetAlignmentTotals2(counts);


            Dictionary<Pair<string, string>, double> result = new Dictionary<Pair<string, string>, double>(counts.Count);

            //if (total <= 0 || double.IsNaN(total) || double.IsInfinity(total))
            //    Console.WriteLine("Total == 0");
            foreach (KeyValuePair<Pair<string, string>, double> pair in counts)
                result[new Pair<string, string>(internTable.Intern(pair.Key.x), internTable.Intern(pair.Key.y))] = pair.Value == 0 ? 0 : (pair.Value / (totals1[pair.Key.x]+totals2[pair.Key.y])); // pair.Value == 0 ? 0 : (pair.Value / totals1[pair.Key.x]) * (pair.Value / totals2[pair.Key.y]);

            return result;
        }

        public static Dictionary<Pair<string, string>, double> NormalizeBySourceOverlap(string sourceWord, Dictionary<Pair<string, string>, double> counts, InternDictionary<string> internTable)
        {
            Dictionary<Pair<string, string>, int> pairIDs = new Dictionary<Pair<string, string>, int>(counts.Count);
            double[] countValues = new double[counts.Count];
            int[] generations = new int[counts.Count];
            int nextID = 0;
            foreach (KeyValuePair<Pair<string, string>, double> pair in counts)
            {
                pairIDs[pair.Key] = nextID;
                countValues[nextID++] = pair.Value;
            }

            List<List<int>> productionsByIndex = new List<List<int>>(sourceWord.Length);
            for (int i = 0; i < sourceWord.Length; i++)
                productionsByIndex.Add(new List<int>());

            nextID = 0;
            foreach (KeyValuePair<Pair<string, string>, double> pair in counts)
            {                
                int start = 0;                
                while ((start = sourceWord.IndexOf(pair.Key.x, start)) >= 0)
                {
                    for (int i = start; i < start + pair.Key.x.Length; i++)
                        productionsByIndex[i].Add(nextID);

                    start += pair.Key.x.Length;
                }

                nextID++;
            }

            Dictionary<Pair<string, string>, double> result = new Dictionary<Pair<string, string>, double>(counts.Count);
            
            int generation = 1;
            foreach (KeyValuePair<Pair<string, string>, double> pair in counts)
            {
                double total = 0;
                int start = sourceWord.IndexOf(pair.Key.x);
                for (int i = start; i < start + pair.Key.x.Length; i++)
                    foreach (int id in productionsByIndex[i])
                        if (generations[id] < generation)
                        {
                            generations[id] = generation;
                            total += countValues[id];
                        }

                generation++;

                result[new Pair<string, string>(internTable.Intern(pair.Key.x), internTable.Intern(pair.Key.y))] = pair.Value == 0 ? 0 : (pair.Value / total); // pair.Value == 0 ? 0 : (pair.Value / totals1[pair.Key.x]) * (pair.Value / totals2[pair.Key.y]);
            }

            return result;
        }

        public static Dictionary<string, double> GetSourceSubstringMax(Dictionary<Pair<string, string>, double> counts)
        {
            Dictionary<string, double> result = new Dictionary<string, double>(counts.Count);
            foreach (KeyValuePair<Pair<string, string>, double> pair in counts)
                if (result.ContainsKey(pair.Key.x))
                    result[pair.Key.x] = Math.Max(pair.Value,result[pair.Key.x]);
                else
                    result[pair.Key.x] = pair.Value;

            return result;
        }

        public static Dictionary<Pair<string, string>, double> NormalizeBySourceSubstringMax(Dictionary<Pair<string, string>, double> counts, InternDictionary<string> internTable)
        {
            //Dictionary<string, double> totals = GetAlignmentTotals1(counts);
            Dictionary<string, double> ssMax = GetSourceSubstringMax(counts);
            Dictionary<Pair<string, string>, double> result = new Dictionary<Pair<string, string>, double>(counts.Count);

            //if (total <= 0 || double.IsNaN(total) || double.IsInfinity(total))
            //    Console.WriteLine("Total == 0");
            foreach (KeyValuePair<Pair<string, string>, double> pair in counts)
                result[new Pair<string, string>(internTable.Intern(pair.Key.x), internTable.Intern(pair.Key.y))] = (pair.Value / ssMax[pair.Key.x]) * ssMax[pair.Key.x];

            return result;
        }

        public static Dictionary<Triple<string,string, string>, double> Normalize(string sourceWord, string targetWord, Dictionary<Triple<string, string, string>, double> counts, InternDictionary<string> internTable, NormalizationMode normalization)
        {
            if (normalization == NormalizationMode.BySourceSubstring)
                return NormalizeBySourceSubstring(counts, internTable);
            //else if (normalization == NormalizationMode.AllProductions)
            //    return NormalizeAllProductions(counts, internTable);
            //else if (normalization == NormalizationMode.BySourceSubstringMax)
            //    return NormalizeBySourceSubstringMax(counts, internTable);
            //else if (normalization == NormalizationMode.BySourceAndTargetSubstring)
            //    return NormalizeBySourceAndTargetSubstring(counts, internTable);
            //else if (normalization == NormalizationMode.BySourceOverlap)
            //    return NormalizeBySourceOverlap(sourceWord, counts, internTable);
            //else if (normalization == NormalizationMode.ByTargetSubstring)
            //    return NormalizeByTargetSubstring(counts, internTable);
            //else
                return InternProductions(counts, internTable);
        }

        public static Dictionary<Pair<string, string>, double> Normalize(string sourceWord, string targetWord, Dictionary<Pair<string, string>, double> counts, InternDictionary<string> internTable, NormalizationMode normalization)
        {
            if (normalization == NormalizationMode.BySourceSubstring)
                return NormalizeBySourceSubstring(counts, internTable);
            else if (normalization == NormalizationMode.AllProductions)
                return NormalizeAllProductions(counts, internTable);
            else if (normalization == NormalizationMode.BySourceSubstringMax)
                return NormalizeBySourceSubstringMax(counts, internTable);
            else if (normalization == NormalizationMode.BySourceAndTargetSubstring)
                return NormalizeBySourceAndTargetSubstring(counts, internTable);
            else if (normalization == NormalizationMode.BySourceOverlap)
                return NormalizeBySourceOverlap(sourceWord, counts, internTable);
            else if (normalization == NormalizationMode.ByTargetSubstring)
                return NormalizeByTargetSubstring(counts, internTable);
            else
                return InternProductions(counts, internTable);
        }

        public static Dictionary<Pair<string, string>, double> FindWeightedAlignments(string word1, string word2, int maxSubstringLength1, int maxSubstringLength2, Dictionary<Pair<string, string>, double> probs, InternDictionary<string> internTable, NormalizationMode normalization)
        {
            Dictionary<Pair<string, string>, double> weights = new Dictionary<Pair<string, string>, double>();
            FindWeightedAlignments(1, new List<Pair<string, string>>(), word1, word2, maxSubstringLength1, maxSubstringLength2, probs, weights, new Dictionary<Pair<string,string>,Pair<double,double>>());

            //CheckDictionary(weights);

            Dictionary<Pair<string, string>, double> weights2 = new Dictionary<Pair<string, string>, double>(weights.Count);
            foreach (KeyValuePair<Pair<string, string>, double> wPair in weights)
                weights2[wPair.Key] = weights[wPair.Key] == 0 ? 0 : weights[wPair.Key] / probs[wPair.Key];
                //weights2[wPair.Key] = weights[wPair.Key] == 0 ? 0 : Math.Pow(weights[wPair.Key], 1d / word1.Length);
            weights = weights2;

            return Normalize(word1, word2, weights, internTable, normalization);
        }

        public static Dictionary<Pair<string, string>, double> FindWeightedAlignmentsAverage(string word1, string word2, int maxSubstringLength1, int maxSubstringLength2, Dictionary<Pair<string, string>, double> probs, InternDictionary<string> internTable, bool weightByOthers, NormalizationMode normalization)
        {
            Dictionary<Pair<string, string>, double> weights = new Dictionary<Pair<string, string>, double>();
            Dictionary<Pair<string, string>, double> weightCounts = new Dictionary<Pair<string, string>, double>();
            //FindWeightedAlignmentsAverage(1, new List<Pair<string, string>>(), word1, word2, maxSubstringLength1, maxSubstringLength2, probs, weights, weightCounts, new Dictionary<Pair<string, string>, Pair<double, double>>(), weightByOthers);
            FindWeightedAlignmentsAverage(1, new List<Pair<string, string>>(), word1, word2, maxSubstringLength1, maxSubstringLength2, probs, weights, weightCounts, weightByOthers);

            //CheckDictionary(weights);

            Dictionary<Pair<string, string>, double> weights2 = new Dictionary<Pair<string, string>, double>(weights.Count);
            foreach (KeyValuePair<Pair<string, string>, double> wPair in weights)
                weights2[wPair.Key] = weights[wPair.Key] == 0 ? 0 : weights[wPair.Key] / weightCounts[wPair.Key];
            //weights2[wPair.Key] = weights[wPair.Key] == 0 ? 0 : Math.Pow(weights[wPair.Key], 1d / word1.Length);
            weights = weights2;

            return Normalize(word1, word2, weights, internTable, normalization);
        }

        public static double FindLogWeightedAlignments(double probability, List<Pair<string, string>> productions, string word1, string word2, int maxSubstringLength1, int maxSubstringLength2, Dictionary<Pair<string, string>, double> probs, Dictionary<Pair<string, string>, double> weights, Dictionary<Pair<string, string>, Pair<double, double>> memoizationTable)
        {
            if (word1.Length == 0 && word2.Length == 0) //record probabilities
            {
                foreach (Pair<string, string> production in productions)
                {
                    double existingScore;
                    if (weights.TryGetValue(production, out existingScore) && existingScore > probability)
                        continue;
                    else
                        weights[production] = probability;
                }
                return 0;
            }

            //Check memoization table to see if we can return early
            Pair<double, double> probPair;
            if (memoizationTable.TryGetValue(new Pair<string, string>(word1, word2), out probPair))
            {
                if (probPair.x >= probability) //we ran against these words with a higher probability before;
                {
                    probability += probPair.y; //get entire production sequence probability

                    foreach (Pair<string, string> production in productions)
                    {
                        double existingScore;
                        if (weights.TryGetValue(production, out existingScore) && existingScore > probability)
                            continue;
                        else
                            weights[production] = probability;
                    }

                    return probPair.y;
                }
            }

            int maxSubstringLength1f = Math.Min(word1.Length, maxSubstringLength1);
            int maxSubstringLength2f = Math.Min(word2.Length, maxSubstringLength2);

            double bestProb = 0;

            for (int i = 1; i <= maxSubstringLength1f; i++) //for each possible substring in the first word...
            {
                string substring1 = word1.Substring(0, i);

                for (int j = 1; j <= maxSubstringLength2f; j++) //foreach possible substring in the second
                {
                    if ((word1.Length - i) * maxSubstringLength2 >= word2.Length - j && (word2.Length - j) * maxSubstringLength1 >= word1.Length - i) //if we get rid of these characters, can we still cover the remainder of word2?
                    {
                        string substring2 = word2.Substring(0, j);
                        Pair<string, string> production = new Pair<string, string>(substring1, substring2);
                        double prob = probs[production];

                        productions.Add(production);
                        double thisProb = prob + FindLogWeightedAlignments(probability + prob, productions, word1.Substring(i), word2.Substring(j), maxSubstringLength1, maxSubstringLength2, probs, weights, memoizationTable);
                        productions.RemoveAt(productions.Count - 1);

                        if (thisProb > bestProb) bestProb = thisProb;
                    }
                }
            }

            memoizationTable[new Pair<string, string>(word1, word2)] = new Pair<double, double>(probability, bestProb);
            return bestProb;
        }

        public static double FindWeightedAlignments(double probability, List<Pair<string,string>> productions, string word1, string word2, int maxSubstringLength1, int maxSubstringLength2, Dictionary<Pair<string, string>, double> probs, Dictionary<Pair<string,string>,double> weights, Dictionary<Pair<string,string>,Pair<double,double>> memoizationTable)
        {
            if (word1.Length == 0 && word2.Length == 0) //record probabilities
            {
                foreach (Pair<string, string> production in productions)
                {
                    double existingScore;
                    if (weights.TryGetValue(production, out existingScore) && existingScore > probability)
                        continue;
                    else
                        weights[production] = probability;
                }
                return 1;
            }

            //Check memoization table to see if we can return early
            Pair<double,double> probPair;
            if (memoizationTable.TryGetValue(new Pair<string, string>(word1, word2), out probPair))
            {
                if (probPair.x >= probability) //we ran against these words with a higher probability before;
                {
                    probability *= probPair.y; //get entire production sequence probability

                    foreach (Pair<string, string> production in productions)
                    {
                        double existingScore;
                        if (weights.TryGetValue(production, out existingScore) && existingScore > probability)
                            continue;
                        else
                            weights[production] = probability;
                    }

                    return probPair.y;
                }
            }

            int maxSubstringLength1f = Math.Min(word1.Length, maxSubstringLength1);
            int maxSubstringLength2f = Math.Min(word2.Length, maxSubstringLength2);

            double bestProb = 0;

            for (int i = 1; i <= maxSubstringLength1f; i++) //for each possible substring in the first word...
            {
                string substring1 = word1.Substring(0, i);

                for (int j = 1; j <= maxSubstringLength2f; j++) //foreach possible substring in the second
                {
                    if ((word1.Length - i) * maxSubstringLength2 >= word2.Length - j && (word2.Length - j) * maxSubstringLength1 >= word1.Length - i) //if we get rid of these characters, can we still cover the remainder of word2?
                    {
                        string substring2 = word2.Substring(0, j);
                        Pair<string,string> production = new Pair<string, string>(substring1, substring2);
                        double prob = probs[production];

                        productions.Add(production);
                        double thisProb = prob * FindWeightedAlignments(probability * prob, productions, word1.Substring(i), word2.Substring(j), maxSubstringLength1, maxSubstringLength2, probs, weights,memoizationTable);
                        productions.RemoveAt(productions.Count - 1);

                        if (thisProb > bestProb) bestProb = thisProb;
                    }
                }
            }

            memoizationTable[new Pair<string, string>(word1, word2)] = new Pair<double,double>(probability,bestProb);
            return bestProb;
        }

        /// <summary>
        /// Averages the possibilities for a given substring, rather than finds the maximum
        /// </summary>
        /// <param name="probability"></param>
        /// <param name="productions"></param>
        /// <param name="word1"></param>
        /// <param name="word2"></param>
        /// <param name="maxSubstringLength1"></param>
        /// <param name="maxSubstringLength2"></param>
        /// <param name="probs"></param>
        /// <param name="weights"></param>
        /// <param name="memoizationTable"></param>
        /// <param name="weightByOthers">True to exclude the existing weight for a production when calculating its new weight (e.g. judge by how probably the remainder of the word can be produced, excluding that piece)</param>
        /// <returns></returns>
        //public static double FindWeightedAlignmentsAverage(double probability, List<Pair<string, string>> productions, string word1, string word2, int maxSubstringLength1, int maxSubstringLength2, Dictionary<Pair<string, string>, double> probs, Dictionary<Pair<string, string>, double> weights, Dictionary<Pair<string, string>, double> weightCounts, Dictionary<Pair<string, string>, Pair<double, double>> memoizationTable, bool weightByOthers)
        //{
        //    if (word1.Length == 0 && word2.Length == 0) //record probabilities
        //    {
        //        foreach (Pair<string, string> production in productions)
        //        {
        //            double probValue = weightByOthers && probability > 0 ? probability/probs[production] : probability;
        //            //weight the contribution to the average by its probability (square it)
        //            Dictionaries.IncrementOrSet<Pair<string, string>>(weights, production, probValue*probValue, probValue*probValue);
        //            Dictionaries.IncrementOrSet<Pair<string, string>>(weightCounts, production, probValue, probValue);                    
        //        }
        //        return 1;
        //    }

        //    //Check memoization table to see if we can return early
        //    Pair<double, double> probPair;
        //    if (memoizationTable.TryGetValue(new Pair<string, string>(word1, word2), out probPair))
        //    {
        //        if (probPair.x >= probability) //we ran against these words with a higher probability before;
        //        {
        //            probability *= probPair.y; //get entire production sequence probability

        //            foreach (Pair<string, string> production in productions)
        //            {
        //                double probValue = weightByOthers && probability > 0 ? probability / probs[production] : probability;
        //                //weight the contribution to the average by its probability (square it)
        //                Dictionaries.IncrementOrSet<Pair<string, string>>(weights, production, probValue * probValue, probValue * probValue);
        //                Dictionaries.IncrementOrSet<Pair<string, string>>(weightCounts, production, probValue, probValue);
        //            }

        //            return probPair.y;
        //        }
        //    }

        //    int maxSubstringLength1f = Math.Min(word1.Length, maxSubstringLength1);
        //    int maxSubstringLength2f = Math.Min(word2.Length, maxSubstringLength2);

        //    double bestProb = 0;

        //    for (int i = 1; i <= maxSubstringLength1f; i++) //for each possible substring in the first word...
        //    {
        //        string substring1 = word1.Substring(0, i);

        //        for (int j = 1; j <= maxSubstringLength2f; j++) //foreach possible substring in the second
        //        {
        //            if ((word1.Length - i) * maxSubstringLength2 >= word2.Length - j && (word2.Length - j) * maxSubstringLength1 >= word1.Length - i) //if we get rid of these characters, can we still cover the remainder of word2?
        //            {
        //                string substring2 = word2.Substring(0, j);
        //                Pair<string, string> production = new Pair<string, string>(substring1, substring2);
        //                double prob = probs[production];

        //                productions.Add(production);
        //                double thisProb = prob * FindWeightedAlignmentsAverage(probability * prob, productions, word1.Substring(i), word2.Substring(j), maxSubstringLength1, maxSubstringLength2, probs, weights, weightCounts, memoizationTable, weightByOthers);
        //                productions.RemoveAt(productions.Count - 1);

        //                if (thisProb > bestProb) bestProb = thisProb;
        //            }
        //        }
        //    }

        //    memoizationTable[new Pair<string, string>(word1, word2)] = new Pair<double, double>(probability, bestProb);
        //    return bestProb;
        //}

        public static double FindWeightedAlignmentsAverage(double probability, List<Pair<string, string>> productions, string word1, string word2, int maxSubstringLength1, int maxSubstringLength2, Dictionary<Pair<string, string>, double> probs, Dictionary<Pair<string, string>, double> weights, Dictionary<Pair<string, string>, double> weightCounts, bool weightByOthers)
        {
            if (probability == 0) return 0;

            if (word1.Length == 0 && word2.Length == 0) //record probabilities
            {
                foreach (Pair<string, string> production in productions)
                {
                    double probValue = weightByOthers ? probability / probs[production] : probability;
                    //weight the contribution to the average by its probability (square it)
                    Dictionaries.IncrementOrSet<Pair<string, string>>(weights, production, probValue * probValue, probValue * probValue);
                    Dictionaries.IncrementOrSet<Pair<string, string>>(weightCounts, production, probValue, probValue);
                }
                return 1;
            }            

            int maxSubstringLength1f = Math.Min(word1.Length, maxSubstringLength1);
            int maxSubstringLength2f = Math.Min(word2.Length, maxSubstringLength2);

            double bestProb = 0;

            for (int i = 1; i <= maxSubstringLength1f; i++) //for each possible substring in the first word...
            {
                string substring1 = word1.Substring(0, i);

                for (int j = 1; j <= maxSubstringLength2f; j++) //foreach possible substring in the second
                {
                    if ((word1.Length - i) * maxSubstringLength2 >= word2.Length - j && (word2.Length - j) * maxSubstringLength1 >= word1.Length - i) //if we get rid of these characters, can we still cover the remainder of word2?
                    {
                        string substring2 = word2.Substring(0, j);
                        Pair<string, string> production = new Pair<string, string>(substring1, substring2);
                        double prob = probs[production];

                        productions.Add(production);
                        double thisProb = prob * FindWeightedAlignmentsAverage(probability * prob, productions, word1.Substring(i), word2.Substring(j), maxSubstringLength1, maxSubstringLength2, probs, weights, weightCounts, weightByOthers);
                        productions.RemoveAt(productions.Count - 1);

                        if (thisProb > bestProb) bestProb = thisProb;
                    }
                }
            }

            //memoizationTable[new Pair<string, string>(word1, word2)] = new Pair<double, double>(probability, bestProb);
            return bestProb;
        }

        /// <summary>
        /// Finds the single best alignment for the two words and uses that to increment the counts.
        /// WeighByProbability does not use the real, noramalized probability, but rather a proportional probability
        /// and is thus not "theoretically valid".
        /// </summary>
        public static Dictionary<Pair<string, string>, double> CountMaxAlignments(string word1, string word2, int maxSubstringLength, Dictionary<Pair<string, string>, double> probs, InternDictionary<string> internTable, bool weighByProbability)
        {
            List<Pair<string,string>> productions;
            double prob = GetAlignmentProbabilityDebug(word1, word2, maxSubstringLength, probs, out productions);
            //CheckDictionary(weights);            

            Dictionary<Pair<string,string>,double> result = new Dictionary<Pair<string,string>,double>(productions.Count);

            if (prob == 0) //no possible alignment for some reason
            {
                return result; //nothing learned //result.Add(new Pair<string,string>(internTable.Intern(word1),internTable.Intern(word2),
            }

            foreach (Pair<string, string> production in productions)
                Dictionaries.IncrementOrSet<Pair<string, string>>(result, new Pair<string,string>(internTable.Intern( production.x ), internTable.Intern( production.y ) ), weighByProbability ? prob : 1, weighByProbability ? prob : 1);

            return result;
        }

        public static Dictionary<Pair<string, string>, double> CountWeightedAlignments(string word1, string word2, int maxSubstringLength1, int maxSubstringLength2, Dictionary<Pair<string, string>, double> probs, InternDictionary<string> internTable, NormalizationMode normalization, bool weightByContextOnly)
        {
            //Dictionary<Pair<string, string>, double> weights = new Dictionary<Pair<string, string>, double>();
            //Dictionary<Pair<string, string>, double> weightCounts = new Dictionary<Pair<string, string>, double>();
            //FindWeightedAlignmentsAverage(1, new List<Pair<string, string>>(), word1, word2, maxSubstringLength1, maxSubstringLength2, probs, weights, weightCounts, new Dictionary<Pair<string, string>, Pair<double, double>>(), weightByOthers);
            double probSum; //the sum of the probabilities of all possible alignments
            Dictionary<Pair<string, string>, double> weights = CountWeightedAlignments(word1, word2, maxSubstringLength1, maxSubstringLength2, probs, new Dictionary<Pair<string, string>, Pair<Dictionary<Pair<string, string>, double>, double>>(), out probSum);

            //CheckDictionary(weights);            

            Dictionary<Pair<string, string>, double> weights2 = new Dictionary<Pair<string, string>, double>(weights.Count);
            foreach (KeyValuePair<Pair<string, string>, double> wPair in weights)
            {
                if (weightByContextOnly)
                {
                    double originalProb = probs[wPair.Key];
                    weights2[wPair.Key] = wPair.Value == 0 ? 0 : (wPair.Value / originalProb) / (probSum - wPair.Value + (wPair.Value / originalProb));
                }
                else
                    weights2[wPair.Key] = wPair.Value == 0 ? 0 : wPair.Value / probSum;
            }

            //weights2[wPair.Key] = weights[wPair.Key] == 0 ? 0 : Math.Pow(weights[wPair.Key], 1d / word1.Length);
            weights = weights2;

            return Normalize(word1, word2, weights, internTable, normalization);
        }

        //Gets counts for productions by (conceptually) summing over all the possible alignments
        //and weighing each alignment (and its constituent productions) by the given probability table.
        //probSum is important (and memoized for input word pairs)--it keeps track and returns the sum of the probabilities of all possible alignments for the word pair
        public static Dictionary<Pair<string,string>,double> CountWeightedAlignments(string word1, string word2, int maxSubstringLength1, int maxSubstringLength2, Dictionary<Pair<string, string>, double> probs, Dictionary<Pair<string, string>, Pair<Dictionary<Pair<string,string>,double>, double>> memoizationTable, out double probSum)
        {
            

            Pair<Dictionary<Pair<string,string>,double>,double> memoization;
            if (memoizationTable.TryGetValue(new Pair<string, string>(word1, word2), out memoization))
            {
                probSum = memoization.y; //stored probSum
                return memoization.x; //table of probs
            }

            Dictionary<Pair<string, string>, double> result = new Dictionary<Pair<string, string>, double>();

            if (word1.Length == 0 && word2.Length == 0) //record probabilities
            {
                probSum = 1; //null -> null is always a perfect alignment
                return result; //end of the line            
            }

            probSum = 0;

            int maxSubstringLength1f = Math.Min(word1.Length, maxSubstringLength1);
            int maxSubstringLength2f = Math.Min(word2.Length, maxSubstringLength2);            

            for (int i = 1; i <= maxSubstringLength1f; i++) //for each possible substring in the first word...
            {
                string substring1 = word1.Substring(0, i);

                for (int j = 1; j <= maxSubstringLength2f; j++) //foreach possible substring in the second
                {
                    if ((word1.Length - i) * maxSubstringLength2 >= word2.Length - j && (word2.Length - j) * maxSubstringLength1 >= word1.Length - i) //if we get rid of these characters, can we still cover the remainder of word2?
                    {
                        string substring2 = word2.Substring(0, j);
                        Pair<string, string> production = new Pair<string, string>(substring1, substring2);
                        //double prob = Math.Max(0.000000000000001, probs[production]);
                        double prob = probs[production];

                        double remainderProbSum;
                        Dictionary<Pair<string, string>,  double> remainderCounts = CountWeightedAlignments(word1.Substring(i), word2.Substring(j), maxSubstringLength1, maxSubstringLength2, probs, memoizationTable, out remainderProbSum);

                        //record this production in our results
                        //IncrementPair(result, production, prob * remainderProbSum, 0);
                        Dictionaries.IncrementOrSet<Pair<string, string>>(result, production, prob * remainderProbSum, prob * remainderProbSum);

                        //update our probSum
                        probSum += remainderProbSum * prob;

                        //update all the productions that come later to take into account their preceeding production's probability
                        foreach (KeyValuePair<Pair<string, string>, double> pair in remainderCounts)
                        {
                            Dictionaries.IncrementOrSet<Pair<string, string>>(result, pair.Key, prob * pair.Value, prob * pair.Value);
                            //IncrementPair(result, pair.Key, pair.Value.x * prob, pair.Value.y * prob);
                        }                        
                    }
                }
            }

            memoizationTable[new Pair<string, string>(word1, word2)] = new Pair<Dictionary<Pair<string, string>, double>, double>(result,probSum);
            return result;
        }

        public static string[] GetLeftFallbackContexts(string word, int position, int contextSize)
        {
            string[] result = new string[contextSize + 1];
            for (int i = 0; i < result.Length; i++)
                result[i] = word.Substring(position - i, i);

            return result;
        }

        public static string[] GetRightFallbackContexts(string word, int position, int contextSize)
        {
            string[] result = new string[contextSize + 1];
            for (int i = 0; i < result.Length; i++)
                result[i] = word.Substring(position, i);

            return result;
        }

        public static string GetLeftContext(string word, int position, int contextSize)
        {
            return word.Substring(position - contextSize, contextSize);                      
        }

        public static string GetRightContext(string word, int position, int contextSize)
        {
            return word.Substring(position, contextSize);
        }

        public struct ExampleCounts
        {
            public SparseDoubleVector<Pair<Triple<string,string,string>, string>> counts;
            public SparseDoubleVector<Pair<string, string>> segCounts;
            public SparseDoubleVector<Pair<string, string>> notSegCounts;
            public double totalProb;
        }
        public static ExampleCounts CountWeightedAlignments2(int productionContextSize, int segContextSize, string word1, string word2, int maxSubstringLength1, int maxSubstringLength2, SparseDoubleVector<Pair<Triple<string, string, string>, string>> probs, SparseDoubleVector<Pair<string, string>> segProbs)
        {
            int paddingSize = Math.Max(productionContextSize, segContextSize);
            string paddedWord = new string('_', paddingSize) + word1 + new string('_', paddingSize);
            Triple<SparseDoubleVector<Triple<int, string, string>>, SparseDoubleVector<int>, double> raw = CountWeightedAlignments2(paddingSize, paddingSize, paddedWord, productionContextSize, segContextSize, word1, word2, maxSubstringLength1, maxSubstringLength2, probs, segProbs, new Dictionary<Triple<int,string,string>,Triple<SparseDoubleVector<Triple<int,string,string>>,SparseDoubleVector<int>,double>>());

            raw.x /= raw.z;
            raw.y /= raw.z;

            ExampleCounts result = new ExampleCounts();
            result.totalProb = raw.z;

            result.counts = new SparseDoubleVector<Pair<Triple<string,string,string>,string>>(raw.x.Count);
            foreach (KeyValuePair<Triple<int, string, string>, double> pair in raw.x)
                result.counts[new Pair<Triple<string, string, string>, string>(
                            new Triple<string, string, string>(GetLeftContext(paddedWord, pair.Key.x, productionContextSize), pair.Key.y, GetRightContext(paddedWord, pair.Key.x + pair.Key.y.Length, productionContextSize))
                            , pair.Key.z)] += pair.Value;

            result.segCounts = new SparseDoubleVector<Pair<string, string>>(raw.y.Count);
            result.notSegCounts = new SparseDoubleVector<Pair<string, string>>(raw.y.Count);
            foreach (KeyValuePair<int, double> pair in raw.y)
            {
                Pair<string, string> context = new Pair<string, string>(GetLeftContext(paddedWord, pair.Key, segContextSize), GetRightContext(paddedWord, pair.Key, segContextSize));
                result.segCounts[context] += pair.Value;
                result.notSegCounts[context] += result.totalProb - pair.Value;
            }            

            return result;
        }

        //Gets counts for productions by (conceptually) summing over all the possible alignments
        //and weighing each alignment (and its constituent productions) by the given probability table.
        //probSum is important (and memoized for input word pairs)--it keeps track and returns the sum of the probabilities of all possible alignments for the word pair
        public static Triple<SparseDoubleVector<Triple<int, string, string>>, SparseDoubleVector<int>, double> CountWeightedAlignments2(int startPosition, int position, string originalWord1, int productionContextSize, int segContextSize, string word1, string word2, int maxSubstringLength1, int maxSubstringLength2, SparseDoubleVector<Pair<Triple<string, string, string>, string>> probs, SparseDoubleVector<Pair<string,string>> segProbs, Dictionary<Triple<int, string, string>, Triple<SparseDoubleVector<Triple<int, string, string>>, SparseDoubleVector<int>, double>> memoizationTable)
        {
            Triple<SparseDoubleVector<Triple<int, string, string>>, SparseDoubleVector<int>, double> memoization;
            if (memoizationTable.TryGetValue(new Triple<int, string, string>(position, word1, word2), out memoization))
                return memoization; //we've been down this road before            

            Triple<SparseDoubleVector<Triple<int, string, string>>, SparseDoubleVector<int>, double> result
                = new Triple<SparseDoubleVector<Triple<int, string, string>>, SparseDoubleVector<int>, double>(new SparseDoubleVector<Triple<int, string, string>>(), new SparseDoubleVector<int>(), 0);

            if (word1.Length == 0 && word2.Length == 0) //record probabilities
            {
                result.z = 1; //null -> null is always a perfect alignment
                return result; //end of the line            
            }

            int maxSubstringLength1f = Math.Min(word1.Length, maxSubstringLength1);
            int maxSubstringLength2f = Math.Min(word2.Length, maxSubstringLength2);

            //string leftContexts = GetLeftFallbackContexts(originalWord1, position, contextSize);
            //string rightContexts = GetRightFallbackContexts(originalWord1, position, contextSize);

            string leftProductionContext = originalWord1.Substring(position - productionContextSize, productionContextSize);
            string rightProductionContext = originalWord1.Substring(position, productionContextSize);

            string leftSegContext = originalWord1.Substring(position - segContextSize, segContextSize);
            string rightSegContext = originalWord1.Substring(position, segContextSize);
            Pair<string,string> segContext = new Pair<string,string>(leftSegContext,rightSegContext);

            double segProb;
            if (position==startPosition) segProb = 1; else { if (segProbs==null) segProb = 0.5; else segProb=segProbs[segContext]; }

            for (int i = 1; i <= maxSubstringLength1f; i++) //for each possible substring in the first word...
            {
                if (i > 1) //adjust segProb
                {
                    if (segProbs==null) segProb *= 0.5;
                    else segProb *= 1 - segProbs[new Pair<string,string>( originalWord1.Substring( (position+i-1) - segContextSize, segContextSize),
                                                                        originalWord1.Substring(position+i-i, segContextSize))];
                }

                string substring1 = word1.Substring(0, i);

                for (int j = 1; j <= maxSubstringLength2f; j++) //foreach possible substring in the second
                {
                    if ((word1.Length - i) * maxSubstringLength2 >= word2.Length - j && (word2.Length - j) * maxSubstringLength1 >= word1.Length - i) //if we get rid of these characters, can we still cover the remainder of word2?
                    {
                        string substring2 = word2.Substring(0, j);
                        Pair<Triple<string,string,string>, string> production = new Pair<Triple<string,string, string>, string>(new Triple<string,string,string>(leftProductionContext, substring1, rightProductionContext), substring2);

                        double prob;                        
                        if (probs != null) prob = probs[production]; else prob = 1;                                                

                        Triple<SparseDoubleVector<Triple<int, string, string>>, SparseDoubleVector<int>, double> remainder = CountWeightedAlignments2(startPosition, position + i, originalWord1, productionContextSize, segContextSize, word1.Substring(i), word2.Substring(j), maxSubstringLength1, maxSubstringLength2, probs, segProbs, memoizationTable);

                        double cProb = prob * segProb;

                        //record this production in our results
                        
                        //Dictionaries.IncrementOrSet<Pair<string, string>>(result, production, prob * remainderProbSum, prob * remainderProbSum);
                        Dictionaries.IncrementOrSet<Triple<int, string, string>>(result.x, new Triple<int, string, string>(position, production.x.y, production.y), cProb * remainder.z, cProb * remainder.z);
                        Dictionaries.IncrementOrSet<int>(result.y, position, cProb * remainder.z, cProb * remainder.z);

                        //update our probSum
                        //probSum += remainderProbSum * prob;
                        result.z += remainder.z * cProb;

                        result.x += remainder.x * cProb;
                        result.y += remainder.y * cProb;
                    }
                }
            }

            memoizationTable[new Triple<int, string, string>(position, word1, word2)] = result;            
            return result;
        }

        //Finds the probability of word1 transliterating to word2 over all possible alignments
        public static double GetSummedAlignmentProbability(string word1, string word2, int maxSubstringLength1, int maxSubstringLength2, Dictionary<Pair<string, string>, double> probs, Dictionary<Pair<string, string>, double> memoizationTable, double minProductionProbability)
        {
            double memoization;
            if (memoizationTable.TryGetValue(new Pair<string, string>(word1, word2), out memoization))            
                return memoization; //stored probSum                                        

            if (word1.Length == 0 && word2.Length == 0) //record probabilities            
                return 1; //null -> null is always a perfect alignment                            

            double probSum = 0;

            int maxSubstringLength1f = Math.Min(word1.Length, maxSubstringLength1);
            int maxSubstringLength2f = Math.Min(word2.Length, maxSubstringLength2);

            double localMinProdProb = 1;
            for (int i = 1; i <= maxSubstringLength1f; i++) //for each possible substring in the first word...
            {
                localMinProdProb *= minProductionProbability;

                string substring1 = word1.Substring(0, i);

                for (int j = 1; j <= maxSubstringLength2f; j++) //foreach possible substring in the second
                {
                    if ((word1.Length - i) * maxSubstringLength2 >= word2.Length - j && (word2.Length - j) * maxSubstringLength1 >= word1.Length - i) //if we get rid of these characters, can we still cover the remainder of word2?
                    {
                        string substring2 = word2.Substring(0, j);
                        Pair<string, string> production = new Pair<string, string>(substring1, substring2);

                        double prob = 0;
                        if (!probs.TryGetValue(production, out prob))
                            if (localMinProdProb == 0) continue;

                        prob = Math.Max(prob, localMinProdProb);

                        double remainderProbSum = GetSummedAlignmentProbability(word1.Substring(i), word2.Substring(j), maxSubstringLength1, maxSubstringLength2, probs, memoizationTable, minProductionProbability);                        

                        //update our probSum
                        probSum += remainderProbSum * prob;
                    }
                }
            }

            memoizationTable[new Pair<string, string>(word1, word2)] = probSum;
            return probSum;
        }

        public static Dictionary<Triple<string, string, string>, double> CountWeightedAlignmentsWithContext(int contextSize, string word1, string word2, int maxSubstringLength1, int maxSubstringLength2, Dictionary<Triple<string, string, string>, double> probs, InternDictionary<string> internTable, NormalizationMode normalization, bool weightByContextOnly, bool fallback)
        {            
            double probSum; //the sum of the probabilities of all possible alignments
            Dictionary<Triple<string, string, string>, double> weights = CountWeightedAlignmentsWithContext(new string('_',contextSize), word1, word2, maxSubstringLength1, maxSubstringLength2, probs,new Dictionary<Triple<string,string,string>,Pair<Dictionary<Triple<string,string,string>,double>,double>>(), out probSum);
            Dictionary<Triple<string, string, string>, double> weights2 = new Dictionary<Triple<string, string, string>, double>(weights.Count);

            if (probSum == 0) probSum = 1;

            foreach (KeyValuePair<Triple<string, string, string>, double> wPair in weights)
            {
                double value;
                if (probs == null) //everything equally weighted at 1/probSum
                    weights2[wPair.Key] = value = 1d/probSum;
                else if (weightByContextOnly)
                {
                    double originalProb = probs[wPair.Key];
                    weights2[wPair.Key] = value = wPair.Value == 0 ? 0 : (wPair.Value / originalProb) / (probSum - wPair.Value + (wPair.Value / originalProb));
                }
                else
                    weights2[wPair.Key] = value = wPair.Value == 0 ? 0 : wPair.Value / probSum;

                if (fallback)
                    for (int i = 0; i < contextSize; i++)
                        Dictionaries.IncrementOrSet<Triple<string, string, string>>(weights2, new Triple<string, string, string>(wPair.Key.x.Substring(0, i), wPair.Key.y, wPair.Key.z), value, value);
            }
            
            weights = weights2;

            return Normalize(word1, word2, weights, internTable, normalization);
        }

        public static void PrintProductions(string context, string sourceWord, Dictionary<Triple<string, string, string>, double> probs)
        {
            TopList<double,string> topList = new TopList<double,string>(100);
            foreach (KeyValuePair<Triple<string, string, string>, double> pair in probs)
                if (pair.Key.x == context && pair.Key.y == sourceWord)
                    topList.Add(pair.Value, pair.Key.z);

            Console.WriteLine("Produces:");
            foreach (KeyValuePair<double, string> pair in topList)
                Console.WriteLine(pair.Key + '\t' + pair.Value);

            Console.WriteLine();
        }

        public static Dictionary<Triple<string, string, string>, double> CountWeightedAlignmentsWithContext(string context, string word1, string word2, int maxSubstringLength1, int maxSubstringLength2, Dictionary<Triple<string, string, string>, double> probs, Dictionary<Triple<string, string, string>, Pair<Dictionary<Triple<string, string, string>, double>, double>> memoizationTable, out double probSum)
        {

            Pair<Dictionary<Triple<string, string, string>, double>, double> memoization;
            if (memoizationTable.TryGetValue(new Triple<string, string, string>(context, word1, word2), out memoization))
            {
                probSum = memoization.y; //stored probSum
                return memoization.x; //table of probs
            }

            Dictionary<Triple<string, string, string>, double> result = new Dictionary<Triple<string, string, string>, double>();

            if (word1.Length == 0 && word2.Length == 0) //record probabilities
            {
                probSum = 1; //null -> null is always a perfect alignment
                return result; //end of the line            
            }

            probSum = 0;

            int maxSubstringLength1f = Math.Min(word1.Length, maxSubstringLength1);
            int maxSubstringLength2f = Math.Min(word2.Length, maxSubstringLength2);

            string[] newContexts = new string[maxSubstringLength2f+1];
            for (int j = 1; j <= maxSubstringLength2f; j++)
                if (j < context.Length)
                    newContexts[j] = context.Substring(j) + word2.Substring(0, j);
                else
                    newContexts[j] = word2.Substring(j - context.Length, context.Length);

            for (int i = 1; i <= maxSubstringLength1f; i++) //for each possible substring in the first word...
            {
                string substring1 = word1.Substring(0, i);

                for (int j = 1; j <= maxSubstringLength2f; j++) //foreach possible substring in the second
                {
                    if ((word1.Length - i) * maxSubstringLength2 >= word2.Length - j && (word2.Length - j) * maxSubstringLength1 >= word1.Length - i) //if we get rid of these characters, can we still cover the remainder of word2?
                    {
                        string substring2 = word2.Substring(0, j);
                        Triple<string, string, string> production = new Triple<string, string, string>(context, substring1, substring2);
                                                
                        double prob = (probs!=null ? probs[production] : 1);

                        double remainderProbSum;
                        Dictionary<Triple<string, string, string>, double> remainderCounts = CountWeightedAlignmentsWithContext(newContexts[j],word1.Substring(i), word2.Substring(j), maxSubstringLength1, maxSubstringLength2, probs, memoizationTable, out remainderProbSum);

                        //record this production in our results
                        //IncrementPair(result, production, prob * remainderProbSum, 0);
                        Dictionaries.IncrementOrSet<Triple<string, string, string>>(result, production, prob * remainderProbSum, prob * remainderProbSum);

                        //update our probSum
                        probSum += remainderProbSum * prob;

                        //update all the productions that come later to take into account their preceeding production's probability
                        foreach (KeyValuePair<Triple<string, string, string>, double> pair in remainderCounts)
                        {
                            Dictionaries.IncrementOrSet<Triple<string, string, string>>(result, pair.Key, prob * pair.Value, prob * pair.Value);
                            //IncrementPair(result, pair.Key, pair.Value.x * prob, pair.Value.y * prob);
                        }
                    }
                }
            }

            memoizationTable[new Triple<string, string, string>(context, word1, word2)] = new Pair<Dictionary<Triple<string, string, string>, double>, double>(result, probSum);
            return result;
        }

        

        private static void IncrementPair(Dictionary<Pair<string,string>,Pair<double,double>> dictionary, Pair<string,string> key, double addToX, double addToY)
        {
            Pair<double, double> existingCount;
            if (!dictionary.TryGetValue(key, out existingCount))
                existingCount = new Pair<double, double>(0, 0);

            existingCount.x += addToX;
            existingCount.y += addToY;
            dictionary[key] = existingCount;
        }

        public static Dictionary<Pair<string, string>, int> CountAlignments(string word1, string word2, int maxSubstringLength, Dictionary<Pair<string,string>,Dictionary<Pair<string, string>, int>> memoizationTable)
        {
            Dictionary<Pair<string,string>,int> counts;
            if (memoizationTable.TryGetValue(new Pair<string, string>(word1, word2), out counts))
                return counts; //done
            else
                counts = new Dictionary<Pair<string, string>, int>();

            int maxSubstringLength1 = Math.Min(word1.Length, maxSubstringLength);
            int maxSubstringLength2 = Math.Min(word2.Length, maxSubstringLength);

            for (int i = 1; i <= maxSubstringLength1; i++) //for each possible substring in the first word...
            {
                string substring1 = word1.Substring(0, i);

                //if (i==1 && (word1.Length-i)*maxSubstringLength >= word2.Length) //if we get rid of these characters, can we still cover the remainder of word2?
                //{
                //    Dictionaries.IncrementOrSet<Pair<string, string>>(counts, new Pair<string, string>(substring1, ""), 1, 1);
                //    Dictionaries.Add<Pair<string,string>>(counts, CountAlignments(word1.Substring(i), word2, maxSubstringLength, memoizationTable),1); //empty production
                //}

                for (int j = 1; j <= maxSubstringLength2; j++) //foreach possible substring in the second
                {         
                   if ((word1.Length - i) * maxSubstringLength >= word2.Length - j && (word2.Length-j)*maxSubstringLength >= word1.Length-i) //if we get rid of these characters, can we still cover the remainder of word2?
                   {
                        Dictionaries.IncrementOrSet<Pair<string, string>>(counts, new Pair<string, string>(substring1,word2.Substring(0,j) ), 1, 1);
                        Dictionaries.AddTo<Pair<string,string>>(counts, CountAlignments(word1.Substring(i), word2.Substring(j), maxSubstringLength, memoizationTable),1);
                   }
                }
            }

            memoizationTable[new Pair<string, string>(word1, word2)] = counts;
            return counts;
        }

        public static void FindAlignments(string word1, string word2, int maxSubstringLength1, int maxSubstringLength2, Dictionary<Pair<string, string>, bool> alignments, Dictionary<Pair<string, string>, bool> memoizationTable)
        {            
            if (memoizationTable.ContainsKey(new Pair<string, string>(word1, word2)))
                return; //done            

            int maxSubstringLength1f = Math.Min(word1.Length, maxSubstringLength1);
            int maxSubstringLength2f = Math.Min(word2.Length, maxSubstringLength2);

            for (int i = 1; i <= maxSubstringLength1f; i++) //for each possible substring in the first word...
            {
                string substring1 = word1.Substring(0, i);

                //if (i==1 && (word1.Length-i)*maxSubstringLength >= word2.Length) //if we get rid of these characters, can we still cover the remainder of word2?
                //{
                //    Dictionaries.IncrementOrSet<Pair<string, string>>(counts, new Pair<string, string>(substring1, ""), 1, 1);
                //    Dictionaries.Add<Pair<string,string>>(counts, CountAlignments(word1.Substring(i), word2, maxSubstringLength, memoizationTable),1); //empty production
                //}

                for (int j = 1; j <= maxSubstringLength2f; j++) //foreach possible substring in the second
                {
                    if ((word1.Length - i) * maxSubstringLength2 >= word2.Length - j && (word2.Length-j)*maxSubstringLength1 >= word1.Length-i) //if we get rid of these characters, can we still cover the remainder of word2?
                    {
                        alignments[new Pair<string, string>(substring1, word2.Substring(0, j))] = true;
                        FindAlignments(word1.Substring(i), word2.Substring(j), maxSubstringLength1, maxSubstringLength2, alignments, memoizationTable);
                    }
                }
            }

            memoizationTable[new Pair<string, string>(word1, word2)] = true;            
        }
    }
}
