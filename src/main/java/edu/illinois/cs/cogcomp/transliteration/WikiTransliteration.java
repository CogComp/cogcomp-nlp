package edu.illinois.cs.cogcomp.transliteration;


import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.Triple;
import edu.illinois.cs.cogcomp.utils.SparseDoubleVector;
import edu.illinois.cs.cogcomp.utils.TopList;
import jdk.nashorn.internal.runtime.regexp.joni.Regex;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class WikiTransliteration {

    public class ContextModel {
        public SparseDoubleVector<Pair<Triple<String, String, String>, String>> productionProbs;
        public SparseDoubleVector<Pair<String, String>> segProbs;
        public int segContextSize;
        public int productionContextSize;
        public int maxSubstringLength;
    }

    public enum NormalizationMode {
        None,
        AllProductions,
        BySourceSubstring,
        BySourceSubstringMax,
        BySourceAndTargetSubstring,
        BySourceOverlap,
        ByTargetSubstring
    }

    public enum AliasType {
        Unknown,
        Link,
        Redirect,
        Title,
        Disambig,
        Interlanguage
    }

    private static HashMap<String, Boolean> languageCodeTable;
    public static final String[] languageCodes = new String[]{
            "aa", "ab", "ae", "af", "ak", "am", "an", "ar", "as", "av", "ay", "az", "ba", "be", "bg", "bh", "bi", "bm", "bn", "bo", "br", "bs", "ca", "ce", "ch", "co", "cr", "cs", "cu", "cv", "cy", "da", "de", "dv", "dz", "ee", "el", "en", "eo", "es", "et", "eu", "fa", "ff", "fi", "fj", "fo", "fr", "fy", "ga", "gd", "gl", "gn", "gu", "gv", "ha", "he", "hi", "ho", "hr", "ht", "hu", "hy", "hz", "ia", "id", "ie", "ig", "ii", "ik", "io", "is", "it", "iu", "ja", "jv", "ka", "kg", "ki", "kj", "kk", "kl", "km", "kn", "ko", "kr", "ks", "ku", "kv", "kw", "ky", "la", "lb", "lg", "li", "ln", "lo", "lt", "lu", "lv", "mg", "mh", "mi", "mk", "ml", "mn", "mr", "ms", "mt", "my", "na", "nb", "nd", "ne", "ng", "nl", "nn", "no", "nr", "nv", "ny", "oc", "oj", "om", "or", "os", "pa", "pi", "pl", "ps", "pt", "qu", "rm", "rn", "ro", "ru", "rw", "sa", "sc", "sd", "se", "sg", "sh", "si", "sk", "sl", "sm", "sn", "so", "sq", "sr", "ss", "st", "su", "sv", "sw", "ta", "te", "tg", "th", "ti", "tk", "tl", "tn", "to", "tr", "ts", "tt", "tw", "ty", "ug", "uk", "ur", "uz", "ve", "vi", "vo", "wa", "wo", "xh", "yi", "yo", "za", "zh", "zu"
    };

//    public WikiTransliteration() {
//        languageCodeTable = new HashMap<>(languageCodes.length);
//        for (String code : languageCodes)
//            languageCodeTable.put(code, true);
//    }
//
//    static void IncrementAlias(HashMap<String, List<WikiAlias>> dict, String target, WikiAlias toAdd) {
//        List<WikiAlias> aliasList;
//
//        if (!dict.containsKey(target)) {
//            dict.put(target, new ArrayList<WikiAlias>());
//        }
//
//        aliasList = dict.get(target);
//
//        Boolean found = false;
//        for (int i = 0; i < aliasList.size(); i++) {
//            if (aliasList.get(i).type == toAdd.type && aliasList.get(i).alias == toAdd.alias) {
//                //found it
//                toAdd.count += aliasList.get(i).count;
//                aliasList.add(i, toAdd);
//
//                found = true;
//
//                break;
//            }
//        }
//
//        if (!found)
//            aliasList.add(toAdd);
//    }
//
//
//    private static String StripParenthesized(String title) {
//        return Regex.Replace(title, "\\(.*\\)", "", RegexOptions.Compiled).Trim();
//    }
//
//    public static HashMap<String, List<String>> GetRedirectSetWords(WikiRedirectTable redirectTable) {
//        Pasternack.Collections.Generic.Specialized.InternDictionary<String> stringTable = new Pasternack.Collections.Generic.Specialized.InternDictionary<String>();
//        HashMap<String, List<String>> inverted = redirectTable.InvertedTable;
//        HashMap<String, List<String>> result = new HashMap<String, List<String>>();
//        for (KeyValuePair<String, List<String>> pair : inverted) {
//            HashMap<String, Boolean> words = new HashMap<>();
//            List<String> wordList = new ArrayList<>();
//
//            //pair.Value.Add(pair.Key);
//            for (String redirect : pair.Value) {
//                String[] wordArray = Regex.Split(StripParenthesized(redirect), "\\W", RegexOptions.Compiled);
//                for (String word : wordArray) {
//                    if (word.Length == 0) continue;
//                    String lCased = word.ToLower();
//                    if (!words.ContainsKey(lCased)) {
//                        words[lCased] = true;
//                        wordList.add(stringTable.Intern(lCased));
//                    }
//                }
//            }
//
//            wordList.TrimExcess();
//            result[pair.Key] = wordList;
//        }
//
//        return result;
//    }
//
//    // FIXME: uses out variables.
//    public static List<KeyValuePair<String, String>> GetWordPairs(String term1, String term2, out int terms1Count, out int terms2Count) {
//        String[] terms1 = Regex.Split(term1, "\\W|�", RegexOptions.Compiled);
//        String[] terms2 = Regex.Split(term2, "\\W|�", RegexOptions.Compiled);
//
//        List<KeyValuePair<String, String>> result = new List<KeyValuePair<String, String>>(terms1.Length * terms2.Length);
//        terms1Count = 0;
//        terms2Count = 0;
//
//        for (int i = 0; i < terms2.Length; i++)
//            if (terms2[i].length() > 0) terms2Count++;
//
//        for (int i = 0; i < terms1.Length; i++) {
//            if (terms1[i].length() == 0) continue;
//
//            terms1Count++;
//
//            for (int j = 0; j < terms2.Length; j++) {
//                if (terms2[j].Length == 0) continue;
//                result.Add(new KeyValuePair<String, String>(terms1[i], terms2[j]));
//            }
//        }
//
//        return result;
//    }
//
//    private static int ScoreLengths(int l1, int l2) {
//        if (l1 == l2)
//            return (l1 == 1 ? 100 : 10);
//        else
//            return 1; //not equal length
//    }
//
//    // FIXME: this uses out variables.
//    public static HashMap<String, String> MakeTranslationMap(String sourceLanguageCode, HashMap<String, List<WikiAlias>> sourceAliasTable, WikiRedirectTable sourceRedirectTable, String targetLanguageCode, IDictionary<String, List<WikiAlias>> targetAliasTable, WikiRedirectTable targetRedirectTable, out HashMap<Pair<String, String>, Integer>weights) {
//        targetLanguageCode += ":";
//        sourceLanguageCode += ":";
//        HashMap<String, String> translationMap = new HashMap<String, String>();
//        weights = new HashMap<Pair<String, String>, Integer>();
//
//        for (KeyValuePair<String, List<WikiAlias>> pair : sourceAliasTable) {
//            String pageFormattedTitle = StripParenthesized(pair.Key).ToLower();
//            for (WikiAlias alias : pair.Value) {
//                if (alias.type == AliasType.Interlanguage && alias.alias.StartsWith(targetLanguageCode, StringComparison.OrdinalIgnoreCase)) {
//                    int l1, l2;
//                    for (KeyValuePair<String, String> wordPair : GetWordPairs(pageFormattedTitle, StripParenthesized(alias.alias.Substring(targetLanguageCode.Length)).ToLower(), out l1, out l2)) {
//                        translationMap.TryAdd(wordPair.Key, wordPair.Value);
//                        Dictionaries.IncrementOrSet<Pair<String, String>>
//                        (weights, wordPair, ScoreLengths(l1, l2), ScoreLengths(l1, l2));
//                    }
//                }
//            }
//        }
//
//        for (KeyValuePair<String, List<WikiAlias>> pair : targetAliasTable) {
//            String pageFormattedTitle = StripParenthesized(pair.Key).ToLower();
//            for (WikiAlias alias : pair.Value) {
//                if (alias.type == AliasType.Interlanguage && alias.alias.StartsWith(sourceLanguageCode, StringComparison.OrdinalIgnoreCase)) {
//                    int l1, l2;
//                    for (KeyValuePair<String, String> wordPair : GetWordPairs(StripParenthesized(alias.alias.Substring(sourceLanguageCode.Length)).ToLower(), pageFormattedTitle, out l1, out l2)) {
//                        translationMap.TryAdd(wordPair.Key, wordPair.Value);
//                        Dictionaries.IncrementOrSet<Pasternack.Utility.Pair<String, String>>
//                        (weights, wordPair, ScoreLengths(l1, l2), ScoreLengths(l1, l2));
//                    }
//                }
//            }
//        }
//
//        return translationMap;
//    }
//
//    private static void IncrementWeights(HashMap<Pair<String, String>, WordAlignment> weights, Pair<String, String> wordPair, int l1, int l2) {
//        WordAlignment additive;
//        if (l1 == 1 && l2 == 1)
//            additive = new WordAlignment(1, 0, 0);
//        else if (l1 == l2)
//            additive = new WordAlignment(0, 1, 0);
//        else
//            additive = new WordAlignment(0, 0, 1);
//
//        WordAlignment current;
//        if (weights.TryGetValue(wordPair, out current))
//            weights[wordPair] = current + additive;
//        else
//            weights[wordPair] = additive;
//    }
//
//    public static Boolean IsPerson(List<WikiAlias> aliasList) {
//        for (WikiAlias alias : aliasList) {
//            if (alias.type == AliasType.Link && alias.alias.StartsWith("category:", StringComparison.OrdinalIgnoreCase) && (alias.alias.Contains("births") || alias.alias.Contains("deaths")))
//                return true;
//        }
//        return false;
//    }
//
//    public static Boolean IsPerson(String title, HashMap<String, Boolean> personByCategory, HashMap<String, Boolean> persondataTitles) {
//        title = title.toLower();
//        return (personByCategory == null && persondataTitles == null) || (personByCategory != null && personByCategory.containsKey(title)) || (persondataTitles != null && persondataTitles.ContainsKey(title));
//    }
//
//    static Boolean HasBirthOrDeathCategory(List<WikiCategory> categories) {
//        for (WikiCategory category : categories) {
//            String name = category.Name.ToLower();
//            if (name.contains("births") || name.contains("deaths"))
//                return true;
//        }
//
//        return false;
//    }
//
//    public static HashMap<String, String> MakeTranslationMap2(String sourceLanguageCode, HashMap<String, List<WikiAlias>> sourceAliasTable, WikiRedirectTable sourceRedirectTable, String targetLanguageCode, IDictionary<String, List<WikiAlias>> targetAliasTable, WikiRedirectTable targetRedirectTable, out HashMap<Pasternack.Utility.Pair<String, String>, WordAlignment>weights, WikiCategoryGraph graph, HashMap<String, Boolean> persondataTitles, Boolean requireDot) {
//
//        //person = (graph != null ? new HashMap<String, Boolean>() : null);
//        HashMap<String, Boolean> personByCategory = null;
//        if (graph != null) {
//            personByCategory = new HashMap<String, Boolean>();
//            for (KeyValuePair<String, List<WikiCategory>> pair : graph.CreateMemberToCategoriesDictionary())
//                if (HasBirthOrDeathCategory(pair.Value)) personByCategory[pair.Key.ToLower()] = true;
//        }
//
//        targetLanguageCode += ":";
//        sourceLanguageCode += ":";
//        HashMap<String, String> translationMap = new HashMap<String, String>();
//        weights = new HashMap<Pair<String, String>, WordAlignment>();
//
//        for (KeyValuePair<String, List<WikiAlias>> pair : sourceAliasTable) {
//            if (!IsPerson(pair.Key, personByCategory, persondataTitles)) continue;
//
//            String pageFormattedTitle = StripParenthesized(pair.Key).ToLower();
//            for (WikiAlias alias : pair.Value) {
//                if (alias.type == AliasType.Interlanguage && alias.alias.StartsWith(targetLanguageCode, StringComparison.OrdinalIgnoreCase)) {
//                    if (requireDot && !alias.alias.Contains("�")) continue;
//                    int l1, l2;
//                    for (KeyValuePair<String, String> wordPair : GetWordPairs(pageFormattedTitle, StripParenthesized(alias.alias.Substring(targetLanguageCode.Length)).ToLower(), out l1, out l2)) {
//                        translationMap.TryAdd(wordPair.Key, wordPair.Value);
//                        IncrementWeights(weights, wordPair, l1, l2);
//                    }
//                }
//            }
//
//
//        }
//
//        for (KeyValuePair<String, List<WikiAlias>> pair : targetAliasTable) {
//            if (requireDot && !pair.Key.Contains("�")) continue;
//            String pageFormattedTitle = StripParenthesized(pair.Key).ToLower();
//            for (WikiAlias alias : pair.Value) {
//                if (alias.type == AliasType.Interlanguage && alias.alias.StartsWith(sourceLanguageCode, StringComparison.OrdinalIgnoreCase)) {
//                    if (!IsPerson(alias.alias.Substring(sourceLanguageCode.Length), personByCategory, persondataTitles))
//                        continue;
//
//                    int l1, l2;
//                    for (KeyValuePair<String, String> wordPair : GetWordPairs(StripParenthesized(alias.alias.Substring(sourceLanguageCode.Length)).ToLower(), pageFormattedTitle, out l1, out l2)) {
//                        translationMap.TryAdd(wordPair.Key, wordPair.Value);
//                        IncrementWeights(weights, wordPair, l1, l2);
//                    }
//                }
//            }
//        }
//
//        return translationMap;
//    }
//
//
//    public static HashMap<String, List<WikiAlias>> MakeAliasTable(IWikiReader reader, List<String> disambigTemplates) {
//        Pasternack.Collections.Generic.Specialized.InternDictionary<String> internTable = new Pasternack.Collections.Generic.Specialized.InternDictionary<String>();
//
//        HashList<String> dTL = new HashList<String>(disambigTemplates.Count);
//        for (String template : disambigTemplates)
//            dTL.Add(template.ToLower());
//
//        HashMap<String, List<WikiAlias>> result = new HashMap<>();
//
//        for (WikiPage page : reader.Pages) {
//            page.Title = internTable.Intern(page.Title);
//
//            if (WikiNamespace.GetNamespace(page.Title, reader.WikiInfo.Namespaces) != WikiNamespace.Default) continue;
//            String formattedTitle = internTable.Intern(Regex.Replace(page.Title, "\\(.*\\)", "", RegexOptions.Compiled).Trim());
//
//            IncrementAlias(result, page.Title, new WikiAlias(formattedTitle, AliasType.Title, 1));
//
//            for (WikiRevision revision : reader.Revisions) {
//                String redirect = WikiRedirectTable.ParseRedirect(revision.Text, reader.WikiInfo);
//                if (redirect != null) {
//                    IncrementAlias(result, page.Title, new WikiAlias(internTable.Intern(redirect), AliasType.Redirect, 1));
//                    continue;
//                }
//
//                List<WikiLink> links = WikiLink.GetWikiLinks(revision.Text, true, false);
//
//                List<String> templates = WikiUtilities.GetTemplates(revision.Text);
//                Boolean isDisambig = false;
//                for (String template : templates) {
//                    int endOfTemplateName = template.IndexOf('|');
//                    if (endOfTemplateName < 0) endOfTemplateName = template.Length - 2;
//                    String templateName = template.Substring(2, endOfTemplateName - 2).ToLower();
//                    if (dTL.Contains(templateName)) {
//                        isDisambig = true;
//                        break;
//                    }
//                }
//
//                for (WikiLink link : links) {
//                    String originalTarget = link.Target;
//                    link.Target = WikiLink.GetTitleFromTarget(link.Target, reader.WikiInfo);
//
//                    if (link.Text == null && originalTarget.Length >= 3 && originalTarget[2] == ':' && languageCodeTable.ContainsKey(originalTarget.Substring(0, 2).ToLower())) {
//                        IncrementAlias(result, page.Title, new WikiAlias(internTable.Intern(link.Target), AliasType.Interlanguage, 1));
//                        continue;
//                    } else if (link.Text != null && link.Text != link.Target) {
//                        IncrementAlias(result, internTable.Intern(link.Target), new WikiAlias(internTable.Intern(link.Text), AliasType.Link, 1));
//                    }
//
//                    if (isDisambig)
//                        IncrementAlias(result, internTable.Intern(link.Target), new WikiAlias(page.Title, AliasType.Disambig, 1));
//                }
//            }
//        }
//
//        return result;
//    }
//
//    static void MakeRawLinkTable(String wikiFile, String rawLinkTableFile) {
//        //BinaryWriter writer = new BinaryWriter(File.Create(rawLinkTableFile));
//        StreamWriter writer = new StreamWriter(rawLinkTableFile);
//
//        ICSharpCode.SharpZipLib.BZip2.BZip2InputStream bzipped =
//                new ICSharpCode.SharpZipLib.BZip2.BZip2InputStream(File.OpenRead(wikiFile));
//        WikiXMLReader reader = new WikiXMLReader(bzipped);
//
//        for (WikiPage page : reader.Pages) {
//            if (WikiNamespace.GetNamespace(page.Title, reader.WikiInfo.Namespaces) != WikiNamespace.Default) continue;
//
//            for (WikiRevision revision : reader.Revisions) {
//                if (WikiRedirectTable.IsRedirect(revision.Text)) continue;
//
//                List<WikiLink> links = WikiLink.GetWikiLinks(revision.Text, true, false);
//
//                //writer.Write(page.Title);
//                //writer.Write(links.Count);
//
//                for (WikiLink link : links) {
//                    if (link.Text == null || link.Text.Contains("\t")) continue;
//                    writer.WriteLine(link.Text + "\t" + WikiLink.GetTitleFromTarget(link.Target, reader.WikiInfo));
//                }
//            }
//        }
//
//        reader.Close();
//        writer.Close();
//    }
//
//    static void MakeTable(String sourceRawLinkFile) {
//        //HashMap<String, List<String>> translations = new HashMap<String, List<String>>();
//
//
//        List<KeyValuePair<String, String>> russianNames = new List<KeyValuePair<String, String>>();
//
//        StreamReader reader = new StreamReader("....path..../translations.txt");
//        while (!reader.EndOfStream) {
//            String[] line = reader.ReadLine().Split('\t');
//
//            if (line[0].startsWith("en:", StringComparison.OrdinalIgnoreCase)) line[0] = line[0].substring(3);
//            if (line[1].startsWith("ru:", StringComparison.OrdinalIgnoreCase)) line[1] = line[1].substring(3);
//
//            //remove parenthesized portion (if any))
//            line[0] = Regex.Replace(line[0], "\\(.*\\)", "", RegexOptions.Compiled).Trim();
//            line[1] = Regex.Replace(line[1], "\\(.*\\)", "", RegexOptions.Compiled).Trim();
//
//            russianNames.Add(new KeyValuePair<String, String>(line[1], line[0]));
//        }
//
//        reader.Close();
//
//        HashMap<String, HashMap<String, Integer>> translationTable = new HashMap<String, HashMap<String, int>>();
//
//        for (KeyValuePair<String, String> pair : russianNames) {
//            String[] russianWords = Regex.Split(pair.Key, "\\W", RegexOptions.Compiled);
//            String[] englishWords = Regex.Split(pair.Value, "\\W", RegexOptions.Compiled);
//
//            int score = 1;
//            if (englishWords.Length == russianWords.Length) score = 2;
//
//            for (String rawRussianWord : russianWords) {
//                if (rawRussianWord.Length == 0) continue;
//                String russianWord = rawRussianWord.ToLower();
//                if (!translationTable.ContainsKey(russianWord))
//                    translationTable[russianWord] = new HashMap<String, int>();
//
//                HashMap<String, int> wordTable = translationTable[russianWord];
//
//                for (String rawEnglishWord : englishWords) {
//                    if (rawEnglishWord.Length == 0) continue;
//                    String englishWord = rawEnglishWord.ToLower();
//                    Dictionaries.IncrementOrSet<String> (wordTable, englishWord, score, score);
//                }
//            }
//        }
//
//        StreamDictionary<String, HashMap<String, Integer>> streamTable = new StreamDictionary<String, HashMap<String, Integer>>(
//                translationTable.Count * 2, 0.5, "..../WikiTransliteration\translationTableKeys.dat", null,
//                "...../transliterationTableValues.dat", null);
//
//        for (KeyValuePair<String, HashMap<String, Integer>> pair : translationTable)
//            streamTable.Add(pair);
//
//        streamTable.Close();
//    }
//
//    /// <SUMMARY>Computes the Levenshtein Edit Distance between two enumerables.</SUMMARY>
//    /// <TYPEPARAM name="T">The type of the items : the enumerables.</TYPEPARAM>
//    /// <PARAM name="x">The first enumerable.</PARAM>
//    /// <PARAM name="y">The second enumerable.</PARAM>
//    /// <RETURNS>The edit distance.</RETURNS>
//    public static int EditDistance
//    <T>(
//    IEnumerable<T> x, IEnumerable
//    <T>y,out
//    int alignmentLength
//    )
//    where T
//    :IEquatable<T>
//
//    {
//        // Validate parameters
//        if (x == null) throw new ArgumentNullException("x");
//        if (y == null) throw new ArgumentNullException("y");
//
//        // Convert the parameters into IList instances
//        // in order to obtain indexing capabilities
//        IList<T> first = x as IList<T > ??new List<T>(x);
//        IList<T> second = y as IList<T > ??new List<T>(y);
//
//        // Get the length of both.  If either is 0, return
//        // the length of the other, since that number of insertions
//        // would be required.
//        int n = first.Count, m = second.Count;
//        if (n == 0) {
//            alignmentLength = m;
//            return m;
//        }
//
//        if (m == 0) {
//            alignmentLength = n;
//            return n;
//        }
//
//        // Rather than maintain an entire matrix (which would require O(n*m) space),
//        // just store the current row and the next row, each of which has a length m+1,
//        // so just O(m) space. Initialize the current row.
//        int curRow = 0, nextRow = 1;
//        int[][] rows = new int[][]{new int[m + 1], new int[m + 1]};
//        int[][] alRows = new int[][]{new int[m + 1], new int[m + 1]}; //alignment length information
//
//        for (int j = 0; j <= m; ++j) rows[curRow][j] = j;
//        for (int j = 0; j <= m; ++j) alRows[curRow][j] = j;
//
//        // For each virtual row (since we only have physical storage for two)
//        for (int i = 1; i <= n; ++i) {
//            // Fill in the values in the row
//            rows[nextRow][0] = i;
//            alRows[nextRow][0] = i;
//
//            for (int j = 1; j <= m; ++j) {
//                Boolean aligns = first[i - 1].Equals(second[j - 1]);
//                int dist1 = rows[curRow][j] + 1;
//                int dist2 = rows[nextRow][j - 1] + 1;
//                int dist3 = rows[curRow][j - 1] +
//                        (aligns ? 0 : 2);
//
//                if (dist1 < dist2 && dist1 < dist3) {
//                    alRows[nextRow][j] = alRows[curRow][j] + 1;
//                    rows[nextRow][j] = dist1;
//                } else if (dist2 < dist3) {
//                    alRows[nextRow][j] = alRows[nextRow][j - 1] + 1;
//                    rows[nextRow][j] = dist2;
//                } else {
//                    alRows[nextRow][j] = alRows[curRow][j - 1] + (aligns ? 1 : 2);
//                    rows[nextRow][j] = dist3;
//                }
//
//                //rows[nextRow][j] = Math.Min(dist1, Math.Min(dist2, dist3));
//            }
//
//            // Swap the current and next rows
//            if (curRow == 0) {
//                curRow = 1;
//                nextRow = 0;
//            } else {
//                curRow = 0;
//                nextRow = 1;
//            }
//        }
//
//        alignmentLength = alRows[curRow][m];
//
//        // Return the computed edit distance
//        return rows[curRow][m];
//    }
//
//    public static double GetAlignmentProbability(String word1, String word2, int maxSubstringLength, HashMap<Pair<String, String>, double> probs, double minProb, double minProductionProbability) {
//        return GetAlignmentProbability(word1, word2, maxSubstringLength, probs, minProb, new HashMap<Pair<String, String>, double>(), minProductionProbability);
//    }
//
//    public static double GetAlignmentProbabilityDebug(String word1, String word2, int maxSubstringLength, HashMap<Pair<String, String>, double> probs, double minProb) {
//        List<Pair<String, String>> productions;
//        double result = GetAlignmentProbabilityDebug(word1, word2, maxSubstringLength, probs, minProb, new HashMap<Pair<String, String>, Pair<double, List<Pair<String, String>>>>(), out productions);
//        return result;
//    }
//
//    public static double GetAlignmentProbabilityDebug(String word1, String word2, int maxSubstringLength, HashMap<Pair<String, String>, double> probs, out List<Pair<String, String>>productions) {
//        return GetAlignmentProbabilityDebug(word1, word2, maxSubstringLength, probs, 0, new HashMap<Pair<String, String>, Pair<double, List<Pair<String, String>>>>(), out productions);
//    }
//
//    public static double GetAlignmentProbabilityDebug(String word1, String word2, int maxSubstringLength, HashMap<Pair<String, String>, double> probs, double floorProb, HashMap<Pair<String, String>, Pair<double, List<Pair<String, String>>>> memoizationTable, out List<Pair<String, String>>productions) {
//        productions = new List<Pair<String, String>>();
//        Pair<String, String> bestPair = new Pair<String, String>(null, null);
//
//        if (word1.Length == 0 && word2.Length == 0) return 1;
//        if (word1.Length * maxSubstringLength < word2.Length) return 0; //no alignment possible
//        if (word2.Length * maxSubstringLength < word1.Length) return 0;
//
//        Pair<double, List<Pair<String, String>>> cached;
//        if (memoizationTable.TryGetValue(new Pair<String, String>(word1, word2), out cached)) {
//            productions = cached.y;
//            return cached.x;
//        }
//
//        double maxProb = 0;
//
//        int maxSubstringLength1 = Math.min(word1.length(), maxSubstringLength);
//        int maxSubstringLength2 = Math.min(word2.length(), maxSubstringLength);
//
//        for (int i = 1; i <= maxSubstringLength1; i++) {
//            String substring1 = word1.substring(0, i);
//            for (int j = 0; j <= maxSubstringLength2; j++) {
//                double localProb;
//                if (probs.TryGetValue(new Pair<String, String>(substring1, word2.substring(0, j)), out localProb)) {
//                    //double localProb = ((double)count) / totals[substring1];
//                    if (localProb < maxProb || localProb < floorProb)
//                        continue; //this is a really bad transition--discard
//
//                    List<Pair<String, String>> outProductions;
//                    localProb *= GetAlignmentProbabilityDebug(word1.substring(i), word2.substring(j), maxSubstringLength, probs, maxProb / localProb, memoizationTable, out outProductions);
//                    if (localProb > maxProb) {
//                        productions = outProductions;
//                        maxProb = localProb;
//                        bestPair = new Pair<String, String>(substring1, word2.substring(0, j));
//                    }
//                }
//            }
//        }
//
//        productions = new List<Pair<String, String>>(productions); //clone it before modifying
//        productions.Insert(0, bestPair);
//
//        memoizationTable[new Pair<String, String>(word1, word2)] = new Pair<double, List<Pair<String, String>>>(maxProb, productions);
//
//        return maxProb;
//    }
//
//    public static HashMap<String, String> GetProbMap(HashMap<Pair<String, String>, Double> probs) {
//        HashMap<String, String> result = new HashMap<String, String>();
//        for (Pair<String, String> pair : probs.Keys)
//            result.put(pair.getFirst(), pair.getSecond());
//
//        return result;
//    }
//
//    public static HashMap<Pair<String, String>, String> GetProbMap(HashMap<Triple<String, String, String>, Double> probs) {
//        HashMap<Pair<String, String>, String> result = new HashMap<Pair<String, String>, String>();
//        for (Triple<String, String, String> triple : probs.Keys)
//            result.put(triple.XY, triple.z);
//
//        return result;
//    }
//
//    public static HashMap<Pair<String, String>, String> GetProbMap(HashMap<Triple<String, String, String>, Double> probs, int topK) {
//        HashMap<Pair<String, String>, TopList<Double, String>> topProductions = new HashMap<>();
//        for (KeyValuePair<Triple<String, String, String>, Double> pair : probs) {
//            if (!topProductions.ContainsKey(pair.Key.XY))
//                topProductions[pair.Key.XY] = new TopList<Double, String>(topK);
//
//            topProductions[pair.Key.XY].Add(pair.Value, pair.Key.z);
//        }
//
//        HashMap<Pair<String, String>, String> result = new HashMap<Pair<String, String>, String>();
//        for (KeyValuePair<Pair<String, String>, TopList<Double, String>> pair : topProductions)
//            for (String generation : pair.Value.Values)
//                result.Add(pair.Key, generation);
//
//        return result;
//    }
//
//    public static HashMap<String, Pair<String, Double>> GetMaxProbs(HashMap<Pair<String, String>, Double> probs) {
//        HashMap<String, Pair<String, Double>> maxProbs = new HashMap<>();
//        for (KeyValuePair<Pair<String, String>, Double> prob : probs) {
//            Pair<String, Double> curResult;
//            if (maxProbs.TryGetValue(prob.Key.x, out curResult))
//                if (curResult.y >= prob.Value) //not need to change
//                    continue;
//
//            maxProbs[prob.Key.x] = new Pair<String, Double>(prob.Key.y, prob.Value);
//        }
//
//        return maxProbs;
//    }
//
//    public static TopList<double, String> Predict(int topK, String word1, int maxSubstringLength, HashMap<String, Pair<String, Double>> maxProbs, HashMap<String, TopList<double, String>> memoizationTable) {
//        TopList<Double, String> result;
//        if (word1.Length == 0) {
//            result = new TopList<Double, String>(1);
//            result.Add(1, "");
//            return result;
//        }
//
//        if (memoizationTable.TryGetValue(word1, out result))
//            return result;
//
//        result = new TopList<Double, String>(topK);
//
//        int maxSubstringLength1 = Math.min(word1.length(), maxSubstringLength);
//
//        for (int i = 1; i <= maxSubstringLength1; i++) {
//            String substring1 = word1.Substring(0, i);
//            Pair<String, Double> maxWord;
//            if (maxProbs.TryGetValue(substring1, out maxWord)) {
//                TopList<Double, String> bestAppends = Predict(topK, word1.substring(i), maxSubstringLength, maxProbs, memoizationTable);
//                for (KeyValuePair<Double, String> pair : bestAppends)
//                    if (result.Add(pair.Key * maxWord.y, maxWord.x + pair.Value, true) < 0) break;
//            }
//        }
//
//        memoizationTable[word1] = result;
//        return result;
//    }
//
//    public static HashMap<String, Integer> GetNgramCounts(int n, Iterable<String> examples, Boolean pad) {
//        HashMap<String, Integer> result = new HashMap<>();
//        for (String example : examples) {
//            String paddedExample = (pad ? new String('_', n - 1) + example : example);
//
//            for (int i = 0; i <= paddedExample.length() - n; i++)
//                Dictionaries.IncrementOrSet<String> (result, paddedExample.substring(i, n), 1, 1);
//        }
//
//        return result;
//    }
//
//    public static HashMap<String, Double> GetFixedSizeNgramProbs(int n, Iterable<String> examples) {
//        HashMap<String, Integer> ngramCounts = GetNgramCounts(n, examples, true);
//        HashMap<String, Integer> ngramTotals = new HashMap<>();
//        for (KeyValuePair<String, Integer> ngramPair : ngramCounts)
//            Dictionaries.IncrementOrSet<String>
//        (ngramTotals, ngramPair.Key.Substring(0, n - 1), ngramPair.Value, ngramPair.Value);
//
//        HashMap<String, Double> result = new HashMap<String, Double>(ngramCounts.Count);
//        for (KeyValuePair<String, Integer> ngramPair : ngramCounts)
//            result[ngramPair.Key] = ((double) ngramPair.Value) / ngramTotals[ngramPair.Key.Substring(0, n - 1)];
//
//        return result;
//    }
//
//    public static HashMap<String, Double> GetNgramProbs(int minN, int maxN, Iterable<String> examples) {
//        HashMap<String, Double> result = new HashMap<>();
//        for (int i = minN; i <= maxN; i++)
//            for (KeyValuePair<String, Double> probPair : GetFixedSizeNgramProbs(i, examples))
//                result.Add(probPair.Key, probPair.Value);
//
//        return result;
//    }
//
//    public static HashMap<String, Double> GetNgramCounts(int minN, int maxN, Iterable<String> examples, Boolean padding) {
//        HashMap<String, Double> result = new HashMap<>();
//        for (int i = minN; i <= maxN; i++) {
//            HashMap<String, Integer> counts = GetNgramCounts(i, examples, padding);
//            int total = 0;
//            for (KeyValuePair<String, Integer> probPair : counts)
//                total += probPair.Value;
//
//            for (KeyValuePair<String, Integer> probPair : counts)
//                result.Add(probPair.Key, ((double) probPair.Value) / total);
//        }
//
//        return result;
//    }
//
//    public static double GetLanguageProbability(String word, HashMap<String, Double> ngramProbs, int ngramSize) {
//        double probability = 1;
//        // FIXME: this means that '_' is repeated so many times
//        String paddedExample = new String('_', ngramSize - 1) + word;
//        for (int i = ngramSize - 1; i < paddedExample.length(); i++) {
//            double localProb;
//            int n = ngramSize;
//            while (!ngramProbs.TryGetValue(paddedExample.substring(i - n + 1, n), out localProb)) {
//                n--;
//                if (n == 0) return 0;
//            }
//            probability *= localProb;
//        }
//
//        return probability;
//    }
//
//    public static double GetLanguageProbability2(String word, HashMap<String, Double> ngramProbs, int ngramSize) {
//        double probability = 1;
//        String paddedExample = word;
//        for (int i = 0; i < paddedExample.length(); i++) {
//            double localProb;
//            int n = ngramSize;
//            while (!ngramProbs.TryGetValue(paddedExample.substring(i, Math.min(paddedExample.length() - i, n)), out localProb)) {
//                n--;
//                if (n == 0) return 0;
//            }
//            probability *= localProb;
//            i += n - 1; //skip consumed characters
//        }
//
//        return probability;
//    }
//
//    public static double GetLanguageProbability3(String word, HashMap<String, Double> ngramProbs, int ngramSize) {
//        double probability = 1;
//        String paddedExample = word;
//        for (int i = 0; i < paddedExample.length(); i++) {
//            double localProb;
//            int n = ngramSize;
//            while (!ngramProbs.TryGetValue(paddedExample.substring(i, Math.min(paddedExample.length() - i, n)), out localProb)) {
//                n--;
//                if (n == 0) return 0;
//            }
//            probability *= localProb;
//        }
//
//        return probability;
//    }
//
//    public static double GetLanguageProbabilityViterbi(String word, HashMap<String, Double> ngramProbs, int ngramSize) {
//        return GetLanguageProbabilityViterbi(word, ngramProbs, ngramSize, new HashMap<String, Double>());
//    }
//
//    public static double GetLanguageProbabilityViterbi(String word, HashMap<String, Double> ngramProbs, int ngramSize, HashMap<String, Double> memoized) {
//        double result = 0;
//        if (word.length() == 0) return 1;
//        if (memoized.TryGetValue(word, out result)) return result;
//
//
//        int maxLength = Math.min(word.length(), ngramSize);
//        for (int i = 1; i <= maxLength; i++) {
//            String substring = word.substring(0, i);
//            double localProb;
//            if (ngramProbs.TryGetValue(substring, out localProb)) {
//                result = Math.max(result, localProb * GetLanguageProbabilityViterbi(word.substring(i), ngramProbs, ngramSize, memoized));
//            } else
//                break; //couldn't find shorter ngram, won't find longer ones
//        }
//
//        return memoized[word] = result;
//    }
//
//    public static TopList<Double, String> PredictLog(int topK, String word1, int maxSubstringLength, Map<String, String> probMap, HashMap<Pair<String, String>, Double> probs, HashMap<String, TopList<Double, String>> memoizationTable, HashMap<String, Integer> ngramCounts, int ngramSize) {
//        TopList<Double, String> result;
//        if (word1.Length == 0) {
//            result = new TopList<Double, String>(1);
//            result.Add(0, "");
//            return result;
//        }
//
//        if (memoizationTable.TryGetValue(word1, out result))
//            return result;
//
//        result = new TopList<Double, String>(topK);
//
//        int maxSubstringLength1 = Math.Min(word1.Length, maxSubstringLength);
//
//        for (int i = 1; i <= maxSubstringLength1; i++) {
//            String substring1 = word1.Substring(0, i);
//
//            if (probMap.ContainsKey(substring1)) {
//                TopList<double, String> bestAppends = PredictLog(topK, word1.Substring(i), maxSubstringLength, probMap, probs, memoizationTable, ngramCounts, ngramSize);
//                for (Pair<String, String> alignment : probMap.GetPairsForKey(substring1)) {
//                    for (KeyValuePair<double, String> pair : bestAppends) {
//                        String word = alignment.y + pair.Value;
//                        //if (ngramCounts != null && GetLanguageProbability(word, ngramCounts, ngramSize) == 0) continue;
//                        if (result.Add(pair.Key + probs[alignment], word, true) < 0) break;
//                    }
//                }
//            }
//        }
//
//        memoizationTable[word1] = result;
//        return result;
//    }
//
//    public static TopList<Double, String> PredictViterbi(int topK, int contextSize, Boolean fallback, String word1, int maxSubstringLength, Map<Pair<String, String>, String> probMap, HashMap<Triple<String, String, String>, double> probs) {
//        HashMap<String, TopList<Double, String>>[] viterbiArray = new HashMap<String, TopList<Double, String>>[word1.Length + 1];
//        for (int i = 0; i < viterbiArray.Length; i++)
//            viterbiArray[i] = new HashMap<>();
//
//        viterbiArray[0][StringUtils.repeat('_', contextSize)] = new TopList<Double, String>(1);
//        viterbiArray[0][StringUtils.repeat('_', contextSize)].Add(1, "");
//
//        TopList<Double, String> results = new TopList<Double, String>(topK);
//
//        for (int start = 0; start < word1.length(); start++) {
//            for (int length = 1; length <= word1.length() - start; length++) {
//                String sourceSubstring = word1.substring(start, length);
//
//                for (KeyValuePair<String, TopList<Double, String>> contextAndOutputPair : viterbiArray[start]) {
//                    double pastProb = contextAndOutputPair.Value[0].Key;
//                    String bestPrepend = contextAndOutputPair.Value[0].Value;
//
//                    if (results.Count == topK && results[topK - 1].Key > pastProb)
//                        continue; //it'll never get us anywhere
//
//                    Pair<String, String> contextAndSource = new Pair<String, String>(contextAndOutputPair.Key, sourceSubstring);
//                    String originalContext = contextAndSource.x;
//
//                    //fallback?
//                    if (fallback)
//                        while (!probMap.ContainsKey(contextAndSource) && contextAndSource.x.Length > 0)
//                            contextAndSource.x = contextAndSource.x.Substring(1);
//
//                    for (KeyValuePair<Pair<String, String>, String> production : probMap.GetPairsForKey(contextAndSource)) {
//                        double probability = pastProb * probs[new Triple<String, String, String>(production.Key.x, production.Key.y, production.Value)];
//
//                        if (results.Count == topK && results[topK - 1].Key > probability)
//                            continue; //it'll never get us anywhere
//
//                        String newContext;
//                        if (production.Value.Length == contextSize) //speed things up by handling this special case quickly
//                            newContext = production.Value;
//                        else if (production.Value.Length > contextSize)
//                            newContext = production.Value.Substring(production.Value.Length - contextSize);
//                        else
//                            newContext = originalContext.Substring(production.Value.Length) + production.Value;
//
//                        TopList<double, String> outputTopList;
//                        if (!viterbiArray[start + length].TryGetValue(newContext, out outputTopList))
//                            viterbiArray[start + length][newContext] = outputTopList = new TopList<double, String>(topK);
//
//                        if (outputTopList.Count < topK || outputTopList[topK - 1].Key < probability) //don't add strings unless you have to!
//                            if (outputTopList.Add(probability, bestPrepend + production.Value, true) >= 0 && start + length == word1.Length)
//                                results.Add(probability, bestPrepend + production.Value, true);
//                    }
//                }
//            }
//        }
//
//        //TopList<double, String> result = new TopList<double, String>(topK);
//        //for (KeyValuePair<String, TopList<double, String>> contextAndOutputPair : viterbiArray[viterbiArray.Length - 1])
//        //{
//        //    result.Add(contextAndOutputPair.Value[0].Key, contextAndOutputPair.Value[0].Value);
//        //}
//
//        return results;
//    }
//
//    public static TopList<Double, String> Predict(int topK, String word1, int maxSubstringLength, Map<String, String> probMap, HashMap<Pair<String, String>, Double> probs, HashMap<String, TopList<Double, String>> memoizationTable, HashMap<String, int> ngramCounts, int ngramSize) {
//        TopList<Double, String> result;
//        if (word1.Length == 0) {
//            result = new TopList<double, String>(1);
//            result.Add(1, "");
//            return result;
//        }
//
//        if (memoizationTable.TryGetValue(word1, out result))
//            return result;
//
//        result = new TopList<double, String>(topK);
//
//        int maxSubstringLength1 = Math.Min(word1.Length, maxSubstringLength);
//
//        for (int i = 1; i <= maxSubstringLength1; i++) {
//            String substring1 = word1.Substring(0, i);
//
//            if (probMap.ContainsKey(substring1)) {
//                TopList<Double, String> bestAppends = Predict(topK, word1.Substring(i), maxSubstringLength, probMap, probs, memoizationTable, ngramCounts, ngramSize);
//                for (Pair<String, String> alignment : probMap.GetPairsForKey(substring1)) {
//                    for (KeyValuePair<Double, String> pair : bestAppends) {
//                        String word = alignment.y + pair.Value;
//                        //if (ngramCounts != null && GetLanguageProbability(word, ngramCounts, ngramSize) == 0) continue;
//                        if (result.Add(pair.Key * probs[alignment], word, true) < 0) break;
//                    }
//                }
//            }
//        }
//
//        memoizationTable[word1] = result;
//        return result;
//    }
//
//    public static TopList<Double, String> Predict2(int topK, String word1, int maxSubstringLength, Map<String, String> probMap, HashMap<Pair<String, String>, Double> probs, HashMap<String, HashMap<String, Double>> memoizationTable, int pruneToSize) {
//        TopList<Double, String> result = new TopList<Double, String>(topK);
//        HashMap<String, Double> rProbs = Predict2(word1, maxSubstringLength, probMap, probs, memoizationTable, pruneToSize);
//        double probSum = 0;
//
//        for (double prob : rProbs.Values) probSum += prob;
//
//        for (KeyValuePair<String, Double> pair : rProbs)
//            result.Add(pair.Value / probSum, pair.Key);
//
//        return result;
//    }
//
//    public static int Segmentations(int characters) {
//        if (characters <= 1) return 1;
//            //2->2
//            //abc -> 4
//            //abcd -> abcd, a-b-c-d, ab-c-d, a-bc-d. a-b-cd, ab-cd, abc-d, a-bcd = 8
//            // each character after the first is either attached to the previous character or separate (2 choices)
//            // -> segmentations == 2^(characters-1)
//        else return 1 << (characters - 1);
//    }
//
//    public static HashMap<String, Double> Predict2(String word1, int maxSubstringLength, Map<String, String> probMap, HashMap<Pair<String, String>, double> probs, HashMap<String, HashMap<String, double>> memoizationTable, int pruneToSize) {
//        HashMap<String, Double> result;
//        if (word1.length() == 0) {
//            result = new HashMap<>(1);
//            result.put("", 1.0);
//            return result;
//        }
//
//        if (memoizationTable.TryGetValue(word1, out result))
//            return result;
//
//        result = new HashMap<>();
//
//        int maxSubstringLength1 = Math.min(word1.length(), maxSubstringLength);
//
//        for (int i = 1; i <= maxSubstringLength1; i++) {
//            String substring1 = word1.substring(0, i);
//
//            if (probMap.ContainsKey(substring1)) {
//                HashMap<String, double> appends = Predict2(word1.substring(i), maxSubstringLength, probMap, probs, memoizationTable, pruneToSize);
//
//                //int segmentations = Segmentations( word1.Length - i );
//
//                for (Pair<String, String> alignment : probMap.GetPairsForKey(substring1)) {
//                    double alignmentProb = probs[alignment];
//
//                    for (KeyValuePair<String, double> pair : appends) {
//                        String word = alignment.y + pair.Key;
//                        //double combinedProb = (pair.Value/segmentations) * alignmentProb;
//                        double combinedProb = (pair.Value) * alignmentProb;
//                        Dictionaries.IncrementOrSet<String> (result, word, combinedProb, combinedProb);
//                    }
//                }
//            }
//        }
//
//        if (result.Count > pruneToSize) {
//            double[] valuesArray = new double[result.Count];
//            String[] data = new String[result.Count];
//            result.Values.CopyTo(valuesArray, 0);
//            result.Keys.CopyTo(data, 0);
//
//            Array.Sort<Double, String> (valuesArray, data);
//
//            //double sum = 0;
//            //for (int i = data.Length - pruneToSize; i < data.Length; i++)
//            //    sum += valuesArray[i];
//
//            result = new HashMap<>(pruneToSize);
//            for (int i = data.Length - pruneToSize; i < data.Length; i++)
//                result.Add(data[i], valuesArray[i]);///sum);
//        }
//
//        memoizationTable[word1] = result;
//        return result;
//    }
//
//
//    public static TopList<Double, String> Predict(int topK, int contextSize, String word1, int maxSubstringLength, Map<Pair<String, String>, String> probMap, HashMap<Triple<String, String, String>, double> probs, HashMap<Pair<String, String>, TopList<double, String>> memoizationTable) {
//        return Predict(topK, StringUtils.repeat('_', contextSize), word1, maxSubstringLength, probMap, probs, new HashMap<Pair<String, String>, TopList<double, String>>());
//    }
//
//    // FIXME: TopList is a class from Pasternack collections.
//    public static TopList<Double, String> Predict(int topK, String context, String word1, int maxSubstringLength, Map<Pair<String, String>, String> probMap, HashMap<Triple<String, String, String>, double> probs, HashMap<Pair<String, String>, TopList<double, String>> memoizationTable) {
//        TopList<Double, String> result;
//        if (word1.Length == 0) {
//            result = new TopList<Double, String>(1);
//            result.Add(1, "");
//            return result;
//        }
//
//        if (memoizationTable.TryGetValue(new Pair<>(context, word1), out result))
//            return result;
//
//        result = new TopList<Double, String>(topK);
//
//        int maxSubstringLength1 = Math.min(word1.length(), maxSubstringLength);
//
//        for (int i = 1; i <= maxSubstringLength1; i++) {
//            String substring1 = word1.substring(0, i);
//
//            String adjustedContext = context;
//
//            //fallback as necessary
//            while (adjustedContext.length() > 0 && !probMap.ContainsKey(new Pair<>(adjustedContext, substring1)))
//                adjustedContext = adjustedContext.substring(1);
//
//            if (adjustedContext.length() > 0 || probMap.ContainsKey(new Pair<>(adjustedContext, substring1))) {
//                for (KeyValuePair<Pair<String, String>, String> alignment : probMap.GetPairsForKey(new Pair<>(adjustedContext, substring1))) {
//                    String newContext;
//                    if (alignment.Value.Length < context.Length)
//                        newContext = context.Substring(alignment.Value.Length) + alignment.Value;
//                    else
//                        newContext = alignment.Value.Substring(alignment.Value.Length - context.Length, context.Length);
//
//                    TopList<Double, String> bestAppends = Predict(topK, newContext, word1.Substring(i), maxSubstringLength, probMap, probs, memoizationTable);
//                    for (KeyValuePair<Double, String> pair : bestAppends) {
//                        String word = alignment.Value + pair.Value;
//                        //if (ngramCounts != null && GetLanguageProbability(word, ngramCounts, ngramSize) == 0) continue;
//                        if (result.Add(pair.Key * probs[new Triple<String, String, String>(alignment.Key.x, alignment.Key.y, alignment.Value)], word, true) < 0)
//                            break;
//                    }
//                }
//            }
//        }
//
//        memoizationTable[new Pair<>(context, word1)] = result;
//        return result;
//    }
//
//
//    public static HashMap<Pair<String, String>, Boolean> GetProductions(String word1, String word2, int maxSubstringLength, HashMap<Pair<String, String>, Boolean> memoizationTable) {
//        HashMap<Pair<String, String>, Boolean> result = new HashMap<>();
//
//        if (memoizationTable.ContainsKey(new Pair<>(word1, word2)))
//            return result;
//
//        int maxSubstringLength1 = Math.Min(word1.Length, maxSubstringLength);
//        int maxSubstringLength2 = Math.Min(word2.Length, maxSubstringLength);
//
//        for (int i = 1; i <= maxSubstringLength1; i++) {
//            String substring1 = word1.Substring(0, i);
//            for (int j = 0; j <= maxSubstringLength2; j++) {
//                if ((word1.Length - i) * maxSubstringLength2 >= word2.Length - j && (word2.Length - j) * maxSubstringLength1 >= word1.Length - i) //if we get rid of these characters, can we still cover the remainder of word2?
//                {
//                    String substring2 = word2.Substring(0, j);
//                    result[new Pair<String, String>(substring1, substring2)] = true;
//
//                    for (KeyValuePair<Pair<String, String>, Boolean> pair : GetProductions(word1.Substring(i), word2.Substring(j), maxSubstringLength, memoizationTable))
//                        result[pair.Key] = true;
//                }
//            }
//        }
//
//        memoizationTable[new Pair<String, String>(word1, word2)] = true;
//        return result;
//    }
//
//    //Finds P(word2|word1, model)
//    public static double GetConditionalProbability(String word1, String word2, ContextModel model) {
//        int paddingSize = Math.Max(model.productionContextSize, model.segContextSize);
//        String paddedWord = new String('_', paddingSize) + word1 + new String('_', paddingSize);
//        return GetConditionalProbability(paddingSize, paddingSize, paddedWord, word1, word2, model, new SparseDoubleVector<Pair<String, String>>());
//    }
//
//    //Finds P(word2|word1, model)
//    public static double GetConditionalProbability(int position, int startPosition, String originalWord1, String word1, String word2, ContextModel model, SparseDoubleVector<Pair<String, String>> memoizationTable) {
//        //double memoized;
//        //if (memoizationTable.TryGetValue(new Pair<String, String>(word1, word2), out memoized))
//        //    return memoized; //we've been down this road before
//
//        //if (word1.Length == 0 && word2.Length == 0) //base case
//        //    return 1;
//
//        //int maxSubstringLength1f = Math.Min(word1.Length, model.maxSubstringLength);
//        //int maxSubstringLength2f = Math.Min(word2.Length, model.maxSubstringLength);
//
//        ////String leftContexts = GetLeftFallbackContexts(originalWord1, position, contextSize);
//        ////String rightContexts = GetRightFallbackContexts(originalWord1, position, contextSize);
//
//        ////String leftProductionContext = originalWord1.Substring(position - productionContextSize, productionContextSize);
//        ////String rightProductionContext = originalWord1.Substring(position, productionContextSize);
//
//        //String leftProductionContexts = GetLeftFallbackContexts(originalWord1, position, model.segContextSize);
//
//        ////find the segmentation probability
//        //double segProb;
//
//
//        //for (int cs = model.segContextSize; cs >= 0; cs--)
//        //    if (model.segProbs.TryGetValue(new Pair<String,String>(
//        //            originalWord1.Substring(position - segContextSize, segContextSize),
//        //            originalWord1.Substring(position, segContextSize)), out segProb)) break;
//
//
//        //if (position == 0) segProb = 1; else { if (segProbs == null) segProb = 0.5; else segProb = segProbs[segContext]; }
//
//        //for (int i = 1; i <= maxSubstringLength1f; i++) //for each possible substring : the first word...
//        //{
//        //    if (i > 1) //adjust segProb
//        //    {
//        //        if (segProbs == null) segProb *= 0.5;
//        //        else segProb *= 1 - segProbs[new Pair<String, String>(originalWord1.Substring((position + i - 1) - segContextSize, segContextSize),
//        //                                                            originalWord1.Substring(position + i - i, segContextSize))];
//        //    }
//
//        //    String substring1 = word1.Substring(0, i);
//
//        //    for (int j = 1; j <= maxSubstringLength2f; j++) //for possible substring in the second
//        //    {
//        //        if ((word1.Length - i) * maxSubstringLength2 >= word2.Length - j && (word2.Length - j) * maxSubstringLength1 >= word1.Length - i) //if we get rid of these characters, can we still cover the remainder of word2?
//        //        {
//        //            String substring2 = word2.Substring(0, j);
//        //            Pair<Triple<String, String, String>, String> production = new Pair<Triple<String, String, String>, String>(new Triple<String, String, String>(leftProductionContext, substring1, rightProductionContext), substring2);
//
//        //            double prob;
//        //            if (probs != null) prob = probs[production]; else prob = 1;
//
//        //            Triple<SparseDoubleVector<Triple<int, String, String>>, SparseDoubleVector<int>, double> remainder = CountWeightedAlignments2(position + i, originalWord1, productionContextSize, segContextSize, word1.Substring(i), word2.Substring(j), maxSubstringLength1, maxSubstringLength2, probs, segProbs, memoizationTable);
//
//        //            double cProb = prob * segProb;
//
//        //            //record this production in our results
//
//        //            //Dictionaries.IncrementOrSet<Pair<String, String>>(result, production, prob * remainderProbSum, prob * remainderProbSum);
//        //            Dictionaries.IncrementOrSet<Triple<int, String, String>>(result.x, new Triple<int, String, String>(position, production.x.y, production.y), cProb * remainder.z, cProb * remainder.z);
//        //            Dictionaries.IncrementOrSet<int>(result.y, position, cProb * remainder.z, cProb * remainder.z);
//
//        //            //update our probSum
//        //            //probSum += remainderProbSum * prob;
//        //            result.z += remainder.z * cProb;
//
//        //            result.x += remainder.x * cProb;
//        //            result.y += remainder.y * cProb;
//        //        }
//        //    }
//        //}
//
//        //memoizationTable[new Triple<int, String, String>(position, word1, word2)] = result;
//        //return result;
//
//        return 0;
//    }
//
//    public static double GetAlignmentProbability(String word1, String word2, int maxSubstringLength, HashMap<Pair<String, String>, double> probs, double floorProb, HashMap<Pair<String, String>, double> memoizationTable, double minProductionProbability) {
//        if (word1.Length == 0 && word2.Length == 0) return 1;
//        if (word1.Length * maxSubstringLength < word2.Length) return 0; //no alignment possible
//        if (word2.Length * maxSubstringLength < word1.Length) return 0;
//
//        double maxProb = 0;
//        if (memoizationTable.TryGetValue(new Pair<String, String>(word1, word2), out maxProb))
//            return maxProb;
//
//        int maxSubstringLength1 = Math.Min(word1.Length, maxSubstringLength);
//        int maxSubstringLength2 = Math.Min(word2.Length, maxSubstringLength);
//
//        double localMinProdProb = 1;
//        for (int i = 1; i <= maxSubstringLength1; i++) {
//            localMinProdProb *= minProductionProbability; //punish longer substrings
//            if (localMinProdProb < floorProb) localMinProdProb = 0;
//
//            String substring1 = word1.Substring(0, i);
//            for (int j = 0; j <= maxSubstringLength2; j++) {
//                double localProb = 0;
//                if (!probs.TryGetValue(new Pair<String, String>(substring1, word2.Substring(0, j)), out localProb))
//                    if (localMinProdProb == 0) continue;
//
//                localProb = Math.Max(localProb, localMinProdProb);
//
//                //double localProb = ((double)count) / totals[substring1];
//                if (localProb < maxProb || localProb < floorProb) continue; //this is a really bad transition--discard
//
//                localProb *= GetAlignmentProbability(word1.Substring(i), word2.Substring(j), maxSubstringLength, probs, Math.Max(floorProb, maxProb / localProb), memoizationTable, minProductionProbability);
//                if (localProb > maxProb)
//                    maxProb = localProb;
//
//            }
//        }
//
//        memoizationTable[new Pair<String, String>(word1, word2)] = maxProb;
//        return maxProb;
//    }
//
//    public static HashMap<String, double> GetAlignmentTotals1(HashMap<Pair<String, String>, double> counts) {
//        HashMap<String, double> result = new HashMap<String, double>();
//        for (KeyValuePair<Pair<String, String>, double> pair : counts)
//            Dictionaries.IncrementOrSet<String> (result, pair.Key.x, pair.Value, pair.Value);
//
//        return result;
//    }
//
//    public static HashMap<Pair<String, String>, double> GetAlignmentTotals1(HashMap<Triple<String, String, String>, double> counts) {
//        HashMap<Pair<String, String>, double> result = new HashMap<Pair<String, String>, double>();
//        for (KeyValuePair<Triple<String, String, String>, double> pair : counts)
//            Dictionaries.IncrementOrSet<Pair<String, String>> (result, pair.Key.XY, pair.Value, pair.Value);
//
//        return result;
//    }
//
//    public static HashMap<String, double> GetAlignmentTotalsForSource(HashMap<Triple<String, String, String>, double> counts) {
//        HashMap<String, double> result = new HashMap<String, double>();
//        for (KeyValuePair<Triple<String, String, String>, double> pair : counts)
//            Dictionaries.IncrementOrSet<String> (result, pair.Key.y, pair.Value, pair.Value);
//
//        return result;
//    }
//
//
//    public static HashMap<String, double> GetAlignmentTotals2(HashMap<Triple<String, String, String>, double> counts) {
//        HashMap<String, double> result = new HashMap<String, double>();
//        for (KeyValuePair<Triple<String, String, String>, double> pair : counts)
//            Dictionaries.IncrementOrSet<String> (result, pair.Key.z, pair.Value, pair.Value);
//
//        return result;
//    }
//
//    public static HashMap<String, double> GetAlignmentTotals2(HashMap<Pair<String, String>, double> counts) {
//        HashMap<String, double> result = new HashMap<String, double>();
//        for (KeyValuePair<Pair<String, String>, double> pair : counts)
//            Dictionaries.IncrementOrSet<String> (result, pair.Key.y, pair.Value, pair.Value);
//
//        return result;
//    }
//
//    public static HashMap<Pair<String, String>, double> FindLogAlignments(String word1, String word2, int maxSubstringLength1, int maxSubstringLength2, InternDictionary<String> internTable, Boolean normalize) {
//        HashMap<Pair<String, String>, Boolean> alignments = new HashMap<Pair<String, String>, Boolean>();
//        FindAlignments(word1, word2, maxSubstringLength1, maxSubstringLength2, alignments, new HashMap<Pair<String, String>, Boolean>());
//
//        double total = Math.Log(alignments.Count);
//
//
//        HashMap<Pair<String, String>, double> result = new HashMap<Pair<String, String>, double>(alignments.Count);
//        for (KeyValuePair<Pair<String, String>, Boolean> pair : alignments)
//            result[new Pair<String, String>(internTable.Intern(pair.Key.x), internTable.Intern(pair.Key.y))] = (normalize ? -total : 0);
//
//        return result;
//    }
//
//    public static HashMap<Pair<String, String>, double> FindAlignments(String word1, String word2, int maxSubstringLength1, int maxSubstringLength2, InternDictionary<String> internTable, NormalizationMode normalization) {
//        HashMap<Pair<String, String>, Boolean> alignments = new HashMap<Pair<String, String>, Boolean>();
//        FindAlignments(word1, word2, maxSubstringLength1, maxSubstringLength2, alignments, new HashMap<Pair<String, String>, Boolean>());
//
//        int total = alignments.Count;
//
//        HashMap<Pair<String, String>, double> result = new HashMap<Pair<String, String>, double>(alignments.Count);
//        for (KeyValuePair<Pair<String, String>, Boolean> pair : alignments)
//            result[new Pair<String, String>(internTable.Intern(pair.Key.x), internTable.Intern(pair.Key.y))] = 1;
//
//        return Normalize(word1, word2, result, internTable, normalization);
//    }
//
//    public static HashMap<Pair<String, String>, double> CountAlignments(String word1, String word2, int maxSubstringLength, InternDictionary<String> internTable) {
//        HashMap<Pair<String, String>, int> counts = CountAlignments(word1, word2, maxSubstringLength, new HashMap<Pair<String, String>, HashMap<Pair<String, String>, int>>());
//
//        long total = 0;
//        for (int value : counts.Values)
//            total += value;
//
//        HashMap<Pair<String, String>, double> result = new HashMap<Pair<String, String>, double>(counts.Count);
//        for (KeyValuePair<Pair<String, String>, int> pair : counts)
//            result[new Pair<String, String>(internTable.Intern(pair.Key.x), internTable.Intern(pair.Key.y))] = ((double) pair.Value) / total;
//
//        return result;
//    }
//    //for (int i = 0; i < word1.Length; i++)
//    //{
//    //    int localMaxSSL = Math.Min(maxSubstringLength, word1.Length - i);
//    //    for (int j = 0; j < localMaxSSL; j++)
//    //    {
//
//    //        CountAlignments(word1.Substring(i+j)
//    //    }
//    //}
//
//    //public static HashMap<Pair<String, String>, double> FindWeightedAlignments(String word1, String word2, int maxSubstringLength, HashMap<Pair<String, String>, HashMap<Pair<String, String>, double>> memoizationTable, HashMap<Pair<String,String>,double> probs)
//    //{
//    //    HashMap<Pair<String, String>, double> weights;
//    //    if (memoizationTable.TryGetValue(new Pair<String, String>(word1, word2), out weights))
//    //        return weights; //done
//    //    else
//    //        weights = new HashMap<Pair<String, String>, double>();
//
//    //    int maxSubstringLength1 = Math.Min(word1.Length, maxSubstringLength);
//    //    int maxSubstringLength2 = Math.Min(word2.Length, maxSubstringLength);
//
//    //    for (int i = 1; i <= maxSubstringLength1; i++) //for each possible substring in the first word...
//    //    {
//    //        String substring1 = word1.Substring(0, i);
//
//    //        for (int j = 1; j <= maxSubstringLength2; j++) //for possible substring in the second
//    //        {
//    //            if ((word1.Length - i) * maxSubstringLength >= word2.Length - j && (word2.Length - j) * maxSubstringLength >= word1.Length - i) //if we get rid of these characters, can we still cover the remainder of word2?
//    //            {
//    //                String substring2 = word2.Substring(0, j);
//    //                double prob = probs[new Pair<String,String>(substring1,substring2)];
//
//    //                HashMap<Pair<String,String>,double> recursiveWeights = FindWeightedAlignments(substring1,substring2,maxSubstringLength,memoizationTable,probs);
//    //                for (KeyValuePair<Pair<String,String>,double> pair in recursiveWeights)
//    //                {
//    //                    double existingWeight;
//    //                    if (!weights.TryGetValue(pair.Key,out existingWeight))
//    //                        weights[pair.Key] = pair.Value*prob;
//    //                    else
//    //                        weights[pair.Key] = Math.Max(pair.Value*prob,existingWeight);
//    //                }
//    //                Dictionaries.IncrementOrSet<Pair<String, String>>(weights, new Pair<String, String>(substring1, word2.Substring(0, j)), 1, 1);
//    //                Dictionaries.AddTo<Pair<String, String>>(weights, FindWeightedAlignments(word1.Substring(i), word2.Substring(j), maxSubstringLength, memoizationTable,probs), 1);
//    //            }
//    //        }
//    //    }
//
//    //    memoizationTable[new Pair<String, String>(word1, word2)] = weights;
//    //    return weights;
//    //}
//
//    public static void CheckDictionary(HashMap<Pair<String, String>, double> dict) {
//        for (KeyValuePair<Pair<String, String>, double> pair : dict)
//            if (double.IsInfinity(pair.Value) || double.IsNaN(pair.Value))
//                Console.WriteLine("Bad entry");
//    }
//
//    public static HashMap<Pair<String, String>, double> FindLogWeightedAlignments(String word1, String word2, int maxSubstringLength1, int maxSubstringLength2, HashMap<Pair<String, String>, double> probs, InternDictionary<String> internTable, Boolean normalize) {
//        HashMap<Pair<String, String>, double> weights = new HashMap<Pair<String, String>, double>();
//        FindLogWeightedAlignments(1, new List<Pair<String, String>>(), word1, word2, maxSubstringLength1, maxSubstringLength2, probs, weights, new HashMap<Pair<String, String>, Pair<double, double>>());
//
//        //CheckDictionary(weights);
//
//        double total = 0;
//        for (KeyValuePair<Pair<String, String>, double> pair : weights)
//            total += Math.Exp(pair.Value);
//
//        total = Math.Log(total);
//
//        HashMap<Pair<String, String>, double> result = new HashMap<Pair<String, String>, double>(weights.Count);
//
//        //if (total <= 0 || double.IsNaN(total) || double.IsInfinity(total))
//        //    Console.WriteLine("Total == 0");
//        for (KeyValuePair<Pair<String, String>, double> pair : weights)
//            result[new Pair<String, String>(internTable.Intern(pair.Key.x), internTable.Intern(pair.Key.y))] = pair.Value - (normalize ? total : 0);
//
//        return result;
//    }
//
//    public static HashMap<Triple<String, String, String>, double> InternProductions(HashMap<Triple<String, String, String>, double> counts, InternDictionary<String> internTable) {
//        HashMap<Triple<String, String, String>, double> result = new HashMap<Triple<String, String, String>, double>(counts.Count);
//
//        for (KeyValuePair<Triple<String, String, String>, double> pair : counts)
//            result[new Triple<String, String, String>(internTable.Intern(pair.Key.x), internTable.Intern(pair.Key.y), internTable.Intern(pair.Key.z))] = pair.Value;
//
//        return result;
//    }
//
//    public static HashMap<Pair<String, String>, double> InternProductions(HashMap<Pair<String, String>, double> counts, InternDictionary<String> internTable) {
//        HashMap<Pair<String, String>, double> result = new HashMap<Pair<String, String>, double>(counts.Count);
//
//        //if (total <= 0 || double.IsNaN(total) || double.IsInfinity(total))
//        //    Console.WriteLine("Total == 0");
//        for (KeyValuePair<Pair<String, String>, double> pair : counts)
//            result[new Pair<String, String>(internTable.Intern(pair.Key.x), internTable.Intern(pair.Key.y))] = pair.Value;
//
//        return result;
//    }
//
//    public static HashMap<Pair<String, String>, double> NormalizeAllProductions(HashMap<Pair<String, String>, double> counts, InternDictionary<String> internTable) {
//        double total = 0;
//        for (KeyValuePair<Pair<String, String>, double> pair : counts)
//            total += pair.Value;
//
//        HashMap<Pair<String, String>, double> result = new HashMap<Pair<String, String>, double>(counts.Count);
//
//        //if (total <= 0 || double.IsNaN(total) || double.IsInfinity(total))
//        //    Console.WriteLine("Total == 0");
//        for (KeyValuePair<Pair<String, String>, double> pair : counts)
//            result[new Pair<String, String>(internTable.Intern(pair.Key.x), internTable.Intern(pair.Key.y))] = pair.Value / total;
//
//        return result;
//    }
//
//    public static HashMap<Pair<String, String>, double> NormalizeBySourceSubstring(HashMap<Pair<String, String>, double> counts, InternDictionary<String> internTable) {
//        HashMap<String, double> totals = GetAlignmentTotals1(counts);
//
//        HashMap<Pair<String, String>, double> result = new HashMap<Pair<String, String>, double>(counts.Count);
//
//        //if (total <= 0 || double.IsNaN(total) || double.IsInfinity(total))
//        //    Console.WriteLine("Total == 0");
//        for (KeyValuePair<Pair<String, String>, double> pair : counts)
//            result[new Pair<String, String>(internTable.Intern(pair.Key.x), internTable.Intern(pair.Key.y))] = pair.Value / totals[pair.Key.x];
//
//        return result;
//    }
//
//    public static HashMap<Triple<String, String, String>, double> NormalizeBySourceSubstring(HashMap<Triple<String, String, String>, double> counts, InternDictionary<String> internTable) {
//        HashMap<String, double> totals = GetAlignmentTotalsForSource(counts);
//
//        HashMap<Triple<String, String, String>, double> result = new HashMap<Triple<String, String, String>, double>(counts.Count);
//
//        for (KeyValuePair<Triple<String, String, String>, double> pair : counts)
//            result[new Triple<String, String, String>(internTable.Intern(pair.Key.x), internTable.Intern(pair.Key.y), internTable.Intern(pair.Key.z))] = (pair.Value > 0 ? pair.Value / totals[pair.Key.y] : 0);
//
//        return result;
//    }
//
//
//    public static HashMap<Pair<String, String>, double> NormalizeByTargetSubstring(HashMap<Pair<String, String>, double> counts, InternDictionary<String> internTable) {
//        HashMap<String, double> totals = GetAlignmentTotals2(counts);
//
//        HashMap<Pair<String, String>, double> result = new HashMap<Pair<String, String>, double>(counts.Count);
//
//        for (KeyValuePair<Pair<String, String>, double> pair : counts)
//            result[new Pair<String, String>(internTable.Intern(pair.Key.x), internTable.Intern(pair.Key.y))] = pair.Value / totals[pair.Key.y];
//
//        return result;
//    }
//
//    public static HashMap<Pair<String, String>, double> NormalizeBySourceAndTargetSubstring(HashMap<Pair<String, String>, double> counts, InternDictionary<String> internTable) {
//        HashMap<String, double> totals1 = GetAlignmentTotals1(counts);
//        HashMap<String, double> totals2 = GetAlignmentTotals2(counts);
//
//
//        HashMap<Pair<String, String>, double> result = new HashMap<Pair<String, String>, double>(counts.Count);
//
//        //if (total <= 0 || double.IsNaN(total) || double.IsInfinity(total))
//        //    Console.WriteLine("Total == 0");
//        for (KeyValuePair<Pair<String, String>, double> pair : counts)
//            result[new Pair<String, String>(internTable.Intern(pair.Key.x), internTable.Intern(pair.Key.y))] = pair.Value == 0 ? 0 : (pair.Value / (totals1[pair.Key.x] + totals2[pair.Key.y])); // pair.Value == 0 ? 0 : (pair.Value / totals1[pair.Key.x]) * (pair.Value / totals2[pair.Key.y]);
//
//        return result;
//    }
//
//    public static HashMap<Pair<String, String>, double> NormalizeBySourceOverlap(String sourceWord, HashMap<Pair<String, String>, double> counts, InternDictionary<String> internTable) {
//        HashMap<Pair<String, String>, int> pairIDs = new HashMap<Pair<String, String>, int>(counts.Count);
//        double[] countValues = new double[counts.Count];
//        int[] generations = new int[counts.Count];
//        int nextID = 0;
//        for (KeyValuePair<Pair<String, String>, double> pair : counts) {
//            pairIDs[pair.Key] = nextID;
//            countValues[nextID++] = pair.Value;
//        }
//
//        List<List<int>> productionsByIndex = new List<List<int>>(sourceWord.Length);
//        for (int i = 0; i < sourceWord.Length; i++)
//            productionsByIndex.Add(new List<int>());
//
//        nextID = 0;
//        for (KeyValuePair<Pair<String, String>, double> pair : counts) {
//            int start = 0;
//            while ((start = sourceWord.IndexOf(pair.Key.x, start)) >= 0) {
//                for (int i = start; i < start + pair.Key.x.Length; i++)
//                    productionsByIndex[i].Add(nextID);
//
//                start += pair.Key.x.Length;
//            }
//
//            nextID++;
//        }
//
//        HashMap<Pair<String, String>, double> result = new HashMap<Pair<String, String>, double>(counts.Count);
//
//        int generation = 1;
//        for (KeyValuePair<Pair<String, String>, double> pair : counts) {
//            double total = 0;
//            int start = sourceWord.IndexOf(pair.Key.x);
//            for (int i = start; i < start + pair.Key.x.Length; i++)
//                for (int id : productionsByIndex[i])
//                    if (generations[id] < generation) {
//                        generations[id] = generation;
//                        total += countValues[id];
//                    }
//
//            generation++;
//
//            result[new Pair<String, String>(internTable.Intern(pair.Key.x), internTable.Intern(pair.Key.y))] = pair.Value == 0 ? 0 : (pair.Value / total); // pair.Value == 0 ? 0 : (pair.Value / totals1[pair.Key.x]) * (pair.Value / totals2[pair.Key.y]);
//        }
//
//        return result;
//    }
//
//    public static HashMap<String, double> GetSourceSubstringMax(HashMap<Pair<String, String>, double> counts) {
//        HashMap<String, double> result = new HashMap<String, double>(counts.Count);
//        for (KeyValuePair<Pair<String, String>, double> pair : counts)
//            if (result.ContainsKey(pair.Key.x))
//                result[pair.Key.x] = Math.Max(pair.Value, result[pair.Key.x]);
//            else
//                result[pair.Key.x] = pair.Value;
//
//        return result;
//    }
//
//    public static HashMap<Pair<String, String>, double> NormalizeBySourceSubstringMax(HashMap<Pair<String, String>, double> counts, InternDictionary<String> internTable) {
//        //HashMap<String, double> totals = GetAlignmentTotals1(counts);
//        HashMap<String, double> ssMax = GetSourceSubstringMax(counts);
//        HashMap<Pair<String, String>, double> result = new HashMap<Pair<String, String>, double>(counts.Count);
//
//        //if (total <= 0 || double.IsNaN(total) || double.IsInfinity(total))
//        //    Console.WriteLine("Total == 0");
//        for (KeyValuePair<Pair<String, String>, double> pair : counts)
//            result[new Pair<String, String>(internTable.Intern(pair.Key.x), internTable.Intern(pair.Key.y))] = (pair.Value / ssMax[pair.Key.x]) * ssMax[pair.Key.x];
//
//        return result;
//    }
//
//    public static HashMap<Triple<String, String, String>, double> Normalize(String sourceWord, String targetWord, HashMap<Triple<String, String, String>, double> counts, InternDictionary<String> internTable, NormalizationMode normalization) {
//        if (normalization == NormalizationMode.BySourceSubstring)
//            return NormalizeBySourceSubstring(counts, internTable);
//        //else if (normalization == NormalizationMode.AllProductions)
//        //    return NormalizeAllProductions(counts, internTable);
//        //else if (normalization == NormalizationMode.BySourceSubstringMax)
//        //    return NormalizeBySourceSubstringMax(counts, internTable);
//        //else if (normalization == NormalizationMode.BySourceAndTargetSubstring)
//        //    return NormalizeBySourceAndTargetSubstring(counts, internTable);
//        //else if (normalization == NormalizationMode.BySourceOverlap)
//        //    return NormalizeBySourceOverlap(sourceWord, counts, internTable);
//        //else if (normalization == NormalizationMode.ByTargetSubstring)
//        //    return NormalizeByTargetSubstring(counts, internTable);
//        //else
//        return InternProductions(counts, internTable);
//    }
//
//    public static HashMap<Pair<String, String>, Double> Normalize(String sourceWord, String targetWord, HashMap<Pair<String, String>, double> counts, InternDictionary<String> internTable, NormalizationMode normalization) {
//        if (normalization == NormalizationMode.BySourceSubstring)
//            return NormalizeBySourceSubstring(counts, internTable);
//        else if (normalization == NormalizationMode.AllProductions)
//            return NormalizeAllProductions(counts, internTable);
//        else if (normalization == NormalizationMode.BySourceSubstringMax)
//            return NormalizeBySourceSubstringMax(counts, internTable);
//        else if (normalization == NormalizationMode.BySourceAndTargetSubstring)
//            return NormalizeBySourceAndTargetSubstring(counts, internTable);
//        else if (normalization == NormalizationMode.BySourceOverlap)
//            return NormalizeBySourceOverlap(sourceWord, counts, internTable);
//        else if (normalization == NormalizationMode.ByTargetSubstring)
//            return NormalizeByTargetSubstring(counts, internTable);
//        else
//            return InternProductions(counts, internTable);
//    }
//
//    public static HashMap<Pair<String, String>, double> FindWeightedAlignments(String word1, String word2, int maxSubstringLength1, int maxSubstringLength2, HashMap<Pair<String, String>, double> probs, InternDictionary<String> internTable, NormalizationMode normalization) {
//        HashMap<Pair<String, String>, double> weights = new HashMap<Pair<String, String>, double>();
//        FindWeightedAlignments(1, new List<Pair<String, String>>(), word1, word2, maxSubstringLength1, maxSubstringLength2, probs, weights, new HashMap<Pair<String, String>, Pair<double, double>>());
//
//        //CheckDictionary(weights);
//
//        HashMap<Pair<String, String>, double> weights2 = new HashMap<Pair<String, String>, double>(weights.Count);
//        for (KeyValuePair<Pair<String, String>, double> wPair : weights)
//            weights2[wPair.Key] = weights[wPair.Key] == 0 ? 0 : weights[wPair.Key] / probs[wPair.Key];
//        //weights2[wPair.Key] = weights[wPair.Key] == 0 ? 0 : Math.Pow(weights[wPair.Key], 1d / word1.Length);
//        weights = weights2;
//
//        return Normalize(word1, word2, weights, internTable, normalization);
//    }
//
//    public static HashMap<Pair<String, String>, double> FindWeightedAlignmentsAverage(String word1, String word2, int maxSubstringLength1, int maxSubstringLength2, HashMap<Pair<String, String>, double> probs, InternDictionary<String> internTable, Boolean weightByOthers, NormalizationMode normalization) {
//        HashMap<Pair<String, String>, double> weights = new HashMap<Pair<String, String>, double>();
//        HashMap<Pair<String, String>, double> weightCounts = new HashMap<Pair<String, String>, double>();
//        //FindWeightedAlignmentsAverage(1, new List<Pair<String, String>>(), word1, word2, maxSubstringLength1, maxSubstringLength2, probs, weights, weightCounts, new HashMap<Pair<String, String>, Pair<double, double>>(), weightByOthers);
//        FindWeightedAlignmentsAverage(1, new List<Pair<String, String>>(), word1, word2, maxSubstringLength1, maxSubstringLength2, probs, weights, weightCounts, weightByOthers);
//
//        //CheckDictionary(weights);
//
//        HashMap<Pair<String, String>, double> weights2 = new HashMap<Pair<String, String>, double>(weights.Count);
//        for (KeyValuePair<Pair<String, String>, double> wPair : weights)
//            weights2[wPair.Key] = weights[wPair.Key] == 0 ? 0 : weights[wPair.Key] / weightCounts[wPair.Key];
//        //weights2[wPair.Key] = weights[wPair.Key] == 0 ? 0 : Math.Pow(weights[wPair.Key], 1d / word1.Length);
//        weights = weights2;
//
//        return Normalize(word1, word2, weights, internTable, normalization);
//    }
//
//    public static double FindLogWeightedAlignments(double probability, List<Pair<String, String>> productions, String word1, String word2, int maxSubstringLength1, int maxSubstringLength2, HashMap<Pair<String, String>, double> probs, HashMap<Pair<String, String>, double> weights, HashMap<Pair<String, String>, Pair<double, double>> memoizationTable) {
//        if (word1.Length == 0 && word2.Length == 0) //record probabilities
//        {
//            for (Pair<String, String> production : productions) {
//                double existingScore;
//                if (weights.TryGetValue(production, out existingScore) && existingScore > probability)
//                    continue;
//                else
//                    weights[production] = probability;
//            }
//            return 0;
//        }
//
//        //Check memoization table to see if we can return early
//        Pair<double, double> probPair;
//        if (memoizationTable.TryGetValue(new Pair<String, String>(word1, word2), out probPair)) {
//            if (probPair.x >= probability) //we ran against these words with a higher probability before;
//            {
//                probability += probPair.y; //get entire production sequence probability
//
//                for (Pair<String, String> production : productions) {
//                    double existingScore;
//                    if (weights.TryGetValue(production, out existingScore) && existingScore > probability)
//                        continue;
//                    else
//                        weights[production] = probability;
//                }
//
//                return probPair.y;
//            }
//        }
//
//        int maxSubstringLength1f = Math.Min(word1.Length, maxSubstringLength1);
//        int maxSubstringLength2f = Math.Min(word2.Length, maxSubstringLength2);
//
//        double bestProb = 0;
//
//        for (int i = 1; i <= maxSubstringLength1f; i++) //for each possible substring : the first word...
//        {
//            String substring1 = word1.Substring(0, i);
//
//            for (int j = 1; j <= maxSubstringLength2f; j++) //for possible substring in the second
//            {
//                if ((word1.Length - i) * maxSubstringLength2 >= word2.Length - j && (word2.Length - j) * maxSubstringLength1 >= word1.Length - i) //if we get rid of these characters, can we still cover the remainder of word2?
//                {
//                    String substring2 = word2.Substring(0, j);
//                    Pair<String, String> production = new Pair<String, String>(substring1, substring2);
//                    double prob = probs[production];
//
//                    productions.Add(production);
//                    double thisProb = prob + FindLogWeightedAlignments(probability + prob, productions, word1.Substring(i), word2.Substring(j), maxSubstringLength1, maxSubstringLength2, probs, weights, memoizationTable);
//                    productions.RemoveAt(productions.Count - 1);
//
//                    if (thisProb > bestProb) bestProb = thisProb;
//                }
//            }
//        }
//
//        memoizationTable[new Pair<String, String>(word1, word2)] = new Pair<double, double>(probability, bestProb);
//        return bestProb;
//    }
//
//    public static double FindWeightedAlignments(double probability, List<Pair<String, String>> productions, String word1, String word2, int maxSubstringLength1, int maxSubstringLength2, HashMap<Pair<String, String>, double> probs, HashMap<Pair<String, String>, double> weights, HashMap<Pair<String, String>, Pair<double, double>> memoizationTable) {
//        if (word1.Length == 0 && word2.Length == 0) //record probabilities
//        {
//            for (Pair<String, String> production : productions) {
//                double existingScore;
//                if (weights.TryGetValue(production, out existingScore) && existingScore > probability)
//                    continue;
//                else
//                    weights[production] = probability;
//            }
//            return 1;
//        }
//
//        //Check memoization table to see if we can return early
//        Pair<double, double> probPair;
//        if (memoizationTable.TryGetValue(new Pair<String, String>(word1, word2), out probPair)) {
//            if (probPair.x >= probability) //we ran against these words with a higher probability before;
//            {
//                probability *= probPair.y; //get entire production sequence probability
//
//                for (Pair<String, String> production : productions) {
//                    double existingScore;
//                    if (weights.TryGetValue(production, out existingScore) && existingScore > probability)
//                        continue;
//                    else
//                        weights[production] = probability;
//                }
//
//                return probPair.y;
//            }
//        }
//
//        int maxSubstringLength1f = Math.Min(word1.Length, maxSubstringLength1);
//        int maxSubstringLength2f = Math.Min(word2.Length, maxSubstringLength2);
//
//        double bestProb = 0;
//
//        for (int i = 1; i <= maxSubstringLength1f; i++) //for each possible substring in the first word...
//        {
//            String substring1 = word1.Substring(0, i);
//
//            for (int j = 1; j <= maxSubstringLength2f; j++) //for possible substring in the second
//            {
//                if ((word1.Length - i) * maxSubstringLength2 >= word2.Length - j && (word2.Length - j) * maxSubstringLength1 >= word1.Length - i) //if we get rid of these characters, can we still cover the remainder of word2?
//                {
//                    String substring2 = word2.Substring(0, j);
//                    Pair<String, String> production = new Pair<String, String>(substring1, substring2);
//                    double prob = probs[production];
//
//                    productions.Add(production);
//                    double thisProb = prob * FindWeightedAlignments(probability * prob, productions, word1.Substring(i), word2.Substring(j), maxSubstringLength1, maxSubstringLength2, probs, weights, memoizationTable);
//                    productions.RemoveAt(productions.Count - 1);
//
//                    if (thisProb > bestProb) bestProb = thisProb;
//                }
//            }
//        }
//
//        memoizationTable[new Pair<String, String>(word1, word2)] = new Pair<double, double>(probability, bestProb);
//        return bestProb;
//    }
//
//    public static double FindWeightedAlignmentsAverage(double probability, List<Pair<String, String>> productions, String word1, String word2, int maxSubstringLength1, int maxSubstringLength2, HashMap<Pair<String, String>, double> probs, HashMap<Pair<String, String>, double> weights, HashMap<Pair<String, String>, double> weightCounts, Boolean weightByOthers) {
//        if (probability == 0) return 0;
//
//        if (word1.Length == 0 && word2.Length == 0) //record probabilities
//        {
//            for (Pair<String, String> production : productions) {
//                double probValue = weightByOthers ? probability / probs[production] : probability;
//                //weight the contribution to the average by its probability (square it)
//                Dictionaries.IncrementOrSet<Pair<String, String>>
//                (weights, production, probValue * probValue, probValue * probValue);
//                Dictionaries.IncrementOrSet<Pair<String, String>> (weightCounts, production, probValue, probValue);
//            }
//            return 1;
//        }
//
//        int maxSubstringLength1f = Math.Min(word1.Length, maxSubstringLength1);
//        int maxSubstringLength2f = Math.Min(word2.Length, maxSubstringLength2);
//
//        double bestProb = 0;
//
//        for (int i = 1; i <= maxSubstringLength1f; i++) //for each possible substring in the first word...
//        {
//            String substring1 = word1.Substring(0, i);
//
//            for (int j = 1; j <= maxSubstringLength2f; j++) //for possible substring in the second
//            {
//                if ((word1.Length - i) * maxSubstringLength2 >= word2.Length - j && (word2.Length - j) * maxSubstringLength1 >= word1.Length - i) //if we get rid of these characters, can we still cover the remainder of word2?
//                {
//                    String substring2 = word2.Substring(0, j);
//                    Pair<String, String> production = new Pair<String, String>(substring1, substring2);
//                    double prob = probs[production];
//
//                    productions.Add(production);
//                    double thisProb = prob * FindWeightedAlignmentsAverage(probability * prob, productions, word1.Substring(i), word2.Substring(j), maxSubstringLength1, maxSubstringLength2, probs, weights, weightCounts, weightByOthers);
//                    productions.RemoveAt(productions.Count - 1);
//
//                    if (thisProb > bestProb) bestProb = thisProb;
//                }
//            }
//        }
//
//        //memoizationTable[new Pair<String, String>(word1, word2)] = new Pair<double, double>(probability, bestProb);
//        return bestProb;
//    }
//
//    /// <summary>
//    /// Finds the single best alignment for the two words and uses that to increment the counts.
//    /// WeighByProbability does not use the real, noramalized probability, but rather a proportional probability
//    /// and is thus not "theoretically valid".
//    /// </summary>
//    public static HashMap<Pair<String, String>, double> CountMaxAlignments(String word1, String word2, int maxSubstringLength, HashMap<Pair<String, String>, double> probs, InternDictionary<String> internTable, Boolean weighByProbability) {
//        List<Pair<String, String>> productions;
//        double prob = GetAlignmentProbabilityDebug(word1, word2, maxSubstringLength, probs, out productions);
//        //CheckDictionary(weights);
//
//        HashMap<Pair<String, String>, double> result = new HashMap<Pair<String, String>, double>(productions.Count);
//
//        if (prob == 0) //no possible alignment for some reason
//        {
//            return result; //nothing learned //result.Add(new Pair<String,String>(internTable.Intern(word1),internTable.Intern(word2),
//        }
//
//        for (Pair<String, String> production : productions)
//            Dictionaries.IncrementOrSet<Pair<String, String>>
//        (result, new Pair<String, String>(internTable.Intern(production.x), internTable.Intern(production.y)), weighByProbability ? prob : 1, weighByProbability ? prob : 1)
//        ;
//
//        return result;
//    }
//
//    public static HashMap<Pair<String, String>, double> CountWeightedAlignments(String word1, String word2, int maxSubstringLength1, int maxSubstringLength2, HashMap<Pair<String, String>, double> probs, InternDictionary<String> internTable, NormalizationMode normalization, Boolean weightByContextOnly) {
//        //HashMap<Pair<String, String>, double> weights = new HashMap<Pair<String, String>, double>();
//        //HashMap<Pair<String, String>, double> weightCounts = new HashMap<Pair<String, String>, double>();
//        //FindWeightedAlignmentsAverage(1, new List<Pair<String, String>>(), word1, word2, maxSubstringLength1, maxSubstringLength2, probs, weights, weightCounts, new HashMap<Pair<String, String>, Pair<double, double>>(), weightByOthers);
//        double probSum; //the sum of the probabilities of all possible alignments
//        HashMap<Pair<String, String>, double> weights = CountWeightedAlignments(word1, word2, maxSubstringLength1, maxSubstringLength2, probs, new HashMap<Pair<String, String>, Pair<HashMap<Pair<String, String>, double>, double>>(), out probSum);
//
//        //CheckDictionary(weights);
//
//        HashMap<Pair<String, String>, double> weights2 = new HashMap<Pair<String, String>, double>(weights.Count);
//        for (KeyValuePair<Pair<String, String>, double> wPair : weights) {
//            if (weightByContextOnly) {
//                double originalProb = probs[wPair.Key];
//                weights2[wPair.Key] = wPair.Value == 0 ? 0 : (wPair.Value / originalProb) / (probSum - wPair.Value + (wPair.Value / originalProb));
//            } else
//                weights2[wPair.Key] = wPair.Value == 0 ? 0 : wPair.Value / probSum;
//        }
//
//        //weights2[wPair.Key] = weights[wPair.Key] == 0 ? 0 : Math.Pow(weights[wPair.Key], 1d / word1.Length);
//        weights = weights2;
//
//        return Normalize(word1, word2, weights, internTable, normalization);
//    }
//
//    //Gets counts for productions by (conceptually) summing over all the possible alignments
//    //and weighing each alignment (and its constituent productions) by the given probability table.
//    //probSum is important (and memoized for input word pairs)--it keeps track and returns the sum of the probabilities of all possible alignments for the word pair
//    public static HashMap<Pair<String, String>, double> CountWeightedAlignments(String word1, String word2, int maxSubstringLength1, int maxSubstringLength2, HashMap<Pair<String, String>, double> probs, HashMap<Pair<String, String>, Pair<HashMap<Pair<String, String>, double>, double>> memoizationTable, out double probSum) {
//
//
//        Pair<HashMap<Pair<String, String>, double>, double> memoization;
//        if (memoizationTable.TryGetValue(new Pair<String, String>(word1, word2), out memoization)) {
//            probSum = memoization.y; //stored probSum
//            return memoization.x; //table of probs
//        }
//
//        HashMap<Pair<String, String>, double> result = new HashMap<Pair<String, String>, double>();
//
//        if (word1.Length == 0 && word2.Length == 0) //record probabilities
//        {
//            probSum = 1; //null -> null is always a perfect alignment
//            return result; //end of the line
//        }
//
//        probSum = 0;
//
//        int maxSubstringLength1f = Math.Min(word1.Length, maxSubstringLength1);
//        int maxSubstringLength2f = Math.Min(word2.Length, maxSubstringLength2);
//
//        for (int i = 1; i <= maxSubstringLength1f; i++) //for each possible substring in the first word...
//        {
//            String substring1 = word1.Substring(0, i);
//
//            for (int j = 1; j <= maxSubstringLength2f; j++) //for possible substring in the second
//            {
//                if ((word1.Length - i) * maxSubstringLength2 >= word2.Length - j && (word2.Length - j) * maxSubstringLength1 >= word1.Length - i) //if we get rid of these characters, can we still cover the remainder of word2?
//                {
//                    String substring2 = word2.Substring(0, j);
//                    Pair<String, String> production = new Pair<String, String>(substring1, substring2);
//                    //double prob = Math.Max(0.000000000000001, probs[production]);
//                    double prob = probs[production];
//
//                    double remainderProbSum;
//                    HashMap<Pair<String, String>, double> remainderCounts = CountWeightedAlignments(word1.Substring(i), word2.Substring(j), maxSubstringLength1, maxSubstringLength2, probs, memoizationTable, out remainderProbSum);
//
//                    //record this production in our results
//                    //IncrementPair(result, production, prob * remainderProbSum, 0);
//                    Dictionaries.IncrementOrSet<Pair<String, String>>
//                    (result, production, prob * remainderProbSum, prob * remainderProbSum);
//
//                    //update our probSum
//                    probSum += remainderProbSum * prob;
//
//                    //update all the productions that come later to take into account their preceeding production's probability
//                    for (KeyValuePair<Pair<String, String>, double> pair : remainderCounts) {
//                        Dictionaries.IncrementOrSet<Pair<String, String>>
//                        (result, pair.Key, prob * pair.Value, prob * pair.Value);
//                        //IncrementPair(result, pair.Key, pair.Value.x * prob, pair.Value.y * prob);
//                    }
//                }
//            }
//        }
//
//        memoizationTable[new Pair<String, String>(word1, word2)] = new Pair<HashMap<Pair<String, String>, double>, double>(result, probSum);
//        return result;
//    }

    public static String[] GetLeftFallbackContexts(String word, int position, int contextSize) {
        String[] result = new String[contextSize + 1];
        for (int i = 0; i < result.length; i++)
            result[i] = word.substring(position - i, i);

        return result;
    }

    public static String[] GetRightFallbackContexts(String word, int position, int contextSize) {
        String[] result = new String[contextSize + 1];
        for (int i = 0; i < result.length; i++)
            result[i] = word.substring(position, i);

        return result;
    }

    public static String GetLeftContext(String word, int position, int contextSize) {
        return word.substring(position - contextSize, contextSize);
    }

    public static String GetRightContext(String word, int position, int contextSize) {
        return word.substring(position, contextSize);
    }
//
//    public struct ExampleCounts
//
//    {
//        public SparseDoubleVector<Pair<Triple<String, String, String>, String>> counts;
//        public SparseDoubleVector<Pair<String, String>> segCounts;
//        public SparseDoubleVector<Pair<String, String>> notSegCounts;
//        public double totalProb;
//    }
//
//    public static ExampleCounts CountWeightedAlignments2(int productionContextSize, int segContextSize, String word1, String word2, int maxSubstringLength1, int maxSubstringLength2, SparseDoubleVector<Pair<Triple<String, String, String>, String>> probs, SparseDoubleVector<Pair<String, String>> segProbs) {
//        int paddingSize = Math.Max(productionContextSize, segContextSize);
//        String paddedWord = new String('_', paddingSize) + word1 + new String('_', paddingSize);
//        Triple<SparseDoubleVector<Triple<int, String, String>>, SparseDoubleVector<int>, double> raw = CountWeightedAlignments2(paddingSize, paddingSize, paddedWord, productionContextSize, segContextSize, word1, word2, maxSubstringLength1, maxSubstringLength2, probs, segProbs, new HashMap<Triple<int, String, String>, Triple<SparseDoubleVector<Triple<int, String, String>>, SparseDoubleVector<int>, double>>());
//
//        raw.x /= raw.z;
//        raw.y /= raw.z;
//
//        ExampleCounts result = new ExampleCounts();
//        result.totalProb = raw.z;
//
//        result.counts = new SparseDoubleVector<Pair<Triple<String, String, String>, String>>(raw.x.Count);
//        for (KeyValuePair<Triple<int, String, String>, double> pair : raw.x)
//            result.counts[new Pair<Triple<String, String, String>, String>(
//                    new Triple<String, String, String>(GetLeftContext(paddedWord, pair.Key.x, productionContextSize), pair.Key.y, GetRightContext(paddedWord, pair.Key.x + pair.Key.y.Length, productionContextSize))
//                    , pair.Key.z)] += pair.Value;
//
//        result.segCounts = new SparseDoubleVector<Pair<String, String>>(raw.y.Count);
//        result.notSegCounts = new SparseDoubleVector<Pair<String, String>>(raw.y.Count);
//        for (KeyValuePair<int, double> pair : raw.y) {
//            Pair<String, String> context = new Pair<String, String>(GetLeftContext(paddedWord, pair.Key, segContextSize), GetRightContext(paddedWord, pair.Key, segContextSize));
//            result.segCounts[context] += pair.Value;
//            result.notSegCounts[context] += result.totalProb - pair.Value;
//        }
//
//        return result;
//    }
//
//    //Gets counts for productions by (conceptually) summing over all the possible alignments
//    //and weighing each alignment (and its constituent productions) by the given probability table.
//    //probSum is important (and memoized for input word pairs)--it keeps track and returns the sum of the probabilities of all possible alignments for the word pair
//    public static Triple<SparseDoubleVector<Triple<int, String, String>>, SparseDoubleVector<int>, double> CountWeightedAlignments2(int startPosition, int position, String originalWord1, int productionContextSize, int segContextSize, String word1, String word2, int maxSubstringLength1, int maxSubstringLength2, SparseDoubleVector<Pair<Triple<String, String, String>, String>> probs, SparseDoubleVector<Pair<String, String>> segProbs, HashMap<Triple<int, String, String>, Triple<SparseDoubleVector<Triple<int, String, String>>, SparseDoubleVector<int>, double>> memoizationTable) {
//        Triple<SparseDoubleVector<Triple<int, String, String>>, SparseDoubleVector<int>, double> memoization;
//        if (memoizationTable.TryGetValue(new Triple<int, String, String>(position, word1, word2), out memoization))
//            return memoization; //we've been down this road before
//
//        Triple<SparseDoubleVector<Triple<int, String, String>>, SparseDoubleVector<int>, double> result
//                = new Triple<SparseDoubleVector<Triple<int, String, String>>, SparseDoubleVector<int>, double>(new SparseDoubleVector<Triple<int, String, String>>(), new SparseDoubleVector<int>(), 0);
//
//        if (word1.Length == 0 && word2.Length == 0) //record probabilities
//        {
//            result.z = 1; //null -> null is always a perfect alignment
//            return result; //end of the line
//        }
//
//        int maxSubstringLength1f = Math.Min(word1.Length, maxSubstringLength1);
//        int maxSubstringLength2f = Math.Min(word2.Length, maxSubstringLength2);
//
//        //String leftContexts = GetLeftFallbackContexts(originalWord1, position, contextSize);
//        //String rightContexts = GetRightFallbackContexts(originalWord1, position, contextSize);
//
//        String leftProductionContext = originalWord1.Substring(position - productionContextSize, productionContextSize);
//        String rightProductionContext = originalWord1.Substring(position, productionContextSize);
//
//        String leftSegContext = originalWord1.Substring(position - segContextSize, segContextSize);
//        String rightSegContext = originalWord1.Substring(position, segContextSize);
//        Pair<String, String> segContext = new Pair<String, String>(leftSegContext, rightSegContext);
//
//        double segProb;
//        if (position == startPosition) segProb = 1;
//        else {
//            if (segProbs == null) segProb = 0.5;
//            else segProb = segProbs[segContext];
//        }
//
//        for (int i = 1; i <= maxSubstringLength1f; i++) //for each possible substring in the first word...
//        {
//            if (i > 1) //adjust segProb
//            {
//                if (segProbs == null) segProb *= 0.5;
//                else
//                    segProb *= 1 - segProbs[new Pair<String, String>(originalWord1.Substring((position + i - 1) - segContextSize, segContextSize),
//                            originalWord1.Substring(position + i - i, segContextSize))];
//            }
//
//            String substring1 = word1.Substring(0, i);
//
//            for (int j = 1; j <= maxSubstringLength2f; j++) //for possible substring in the second
//            {
//                if ((word1.Length - i) * maxSubstringLength2 >= word2.Length - j && (word2.Length - j) * maxSubstringLength1 >= word1.Length - i) //if we get rid of these characters, can we still cover the remainder of word2?
//                {
//                    String substring2 = word2.Substring(0, j);
//                    Pair<Triple<String, String, String>, String> production = new Pair<Triple<String, String, String>, String>(new Triple<String, String, String>(leftProductionContext, substring1, rightProductionContext), substring2);
//
//                    double prob;
//                    if (probs != null) prob = probs[production];
//                    else prob = 1;
//
//                    Triple<SparseDoubleVector<Triple<int, String, String>>, SparseDoubleVector<int>, double> remainder = CountWeightedAlignments2(startPosition, position + i, originalWord1, productionContextSize, segContextSize, word1.Substring(i), word2.Substring(j), maxSubstringLength1, maxSubstringLength2, probs, segProbs, memoizationTable);
//
//                    double cProb = prob * segProb;
//
//                    //record this production in our results
//
//                    //Dictionaries.IncrementOrSet<Pair<String, String>>(result, production, prob * remainderProbSum, prob * remainderProbSum);
//                    Dictionaries.IncrementOrSet<Triple<int, String, String>>
//                    (result.x, new Triple<int, String, String>(position, production.x.y, production.y), cProb * remainder.z, cProb * remainder.z)
//                    ;
//                    Dictionaries.IncrementOrSet<int> (result.y, position, cProb * remainder.z, cProb * remainder.z);
//
//                    //update our probSum
//                    //probSum += remainderProbSum * prob;
//                    result.z += remainder.z * cProb;
//
//                    result.x += remainder.x * cProb;
//                    result.y += remainder.y * cProb;
//                }
//            }
//        }
//
//        memoizationTable[new Triple<int, String, String>(position, word1, word2)] = result;
//        return result;
//    }
//
//    //Finds the probability of word1 transliterating to word2 over all possible alignments
//    public static double GetSummedAlignmentProbability(String word1, String word2, int maxSubstringLength1, int maxSubstringLength2, HashMap<Pair<String, String>, double> probs, HashMap<Pair<String, String>, double> memoizationTable, double minProductionProbability) {
//        double memoization;
//        if (memoizationTable.TryGetValue(new Pair<String, String>(word1, word2), out memoization))
//            return memoization; //stored probSum
//
//        if (word1.Length == 0 && word2.Length == 0) //record probabilities
//            return 1; //null -> null is always a perfect alignment
//
//        double probSum = 0;
//
//        int maxSubstringLength1f = Math.Min(word1.Length, maxSubstringLength1);
//        int maxSubstringLength2f = Math.Min(word2.Length, maxSubstringLength2);
//
//        double localMinProdProb = 1;
//        for (int i = 1; i <= maxSubstringLength1f; i++) //for each possible substring in the first word...
//        {
//            localMinProdProb *= minProductionProbability;
//
//            String substring1 = word1.Substring(0, i);
//
//            for (int j = 1; j <= maxSubstringLength2f; j++) //for possible substring in the second
//            {
//                if ((word1.Length - i) * maxSubstringLength2 >= word2.Length - j && (word2.Length - j) * maxSubstringLength1 >= word1.Length - i) //if we get rid of these characters, can we still cover the remainder of word2?
//                {
//                    String substring2 = word2.Substring(0, j);
//                    Pair<String, String> production = new Pair<String, String>(substring1, substring2);
//
//                    double prob = 0;
//                    if (!probs.TryGetValue(production, out prob))
//                        if (localMinProdProb == 0) continue;
//
//                    prob = Math.Max(prob, localMinProdProb);
//
//                    double remainderProbSum = GetSummedAlignmentProbability(word1.Substring(i), word2.Substring(j), maxSubstringLength1, maxSubstringLength2, probs, memoizationTable, minProductionProbability);
//
//                    //update our probSum
//                    probSum += remainderProbSum * prob;
//                }
//            }
//        }
//
//        memoizationTable[new Pair<String, String>(word1, word2)] = probSum;
//        return probSum;
//    }
//
//    public static HashMap<Triple<String, String, String>, double> CountWeightedAlignmentsWithContext(int contextSize, String word1, String word2, int maxSubstringLength1, int maxSubstringLength2, HashMap<Triple<String, String, String>, double> probs, InternDictionary<String> internTable, NormalizationMode normalization, Boolean weightByContextOnly, Boolean fallback) {
//        double probSum; //the sum of the probabilities of all possible alignments
//        HashMap<Triple<String, String, String>, double> weights = CountWeightedAlignmentsWithContext(new String('_', contextSize), word1, word2, maxSubstringLength1, maxSubstringLength2, probs, new HashMap<Triple<String, String, String>, Pair<HashMap<Triple<String, String, String>, double>, double>>(), out probSum);
//        HashMap<Triple<String, String, String>, double> weights2 = new HashMap<Triple<String, String, String>, double>(weights.Count);
//
//        if (probSum == 0) probSum = 1;
//
//        for (KeyValuePair<Triple<String, String, String>, double> wPair : weights) {
//            double value;
//            if (probs == null) //everything equally weighted at 1/probSum
//                weights2[wPair.Key] = value = 1d / probSum;
//            else if (weightByContextOnly) {
//                double originalProb = probs[wPair.Key];
//                weights2[wPair.Key] = value = wPair.Value == 0 ? 0 : (wPair.Value / originalProb) / (probSum - wPair.Value + (wPair.Value / originalProb));
//            } else
//                weights2[wPair.Key] = value = wPair.Value == 0 ? 0 : wPair.Value / probSum;
//
//            if (fallback)
//                for (int i = 0; i < contextSize; i++)
//                    Dictionaries.IncrementOrSet<Triple<String, String, String>>
//            (weights2, new Triple<String, String, String>(wPair.Key.x.Substring(0, i), wPair.Key.y, wPair.Key.z), value, value)
//            ;
//        }
//
//        weights = weights2;
//
//        return Normalize(word1, word2, weights, internTable, normalization);
//    }
//
//    public static void PrintProductions(String context, String sourceWord, HashMap<Triple<String, String, String>, double> probs) {
//        TopList<double, String> topList = new TopList<double, String>(100);
//        for (KeyValuePair<Triple<String, String, String>, double> pair : probs)
//            if (pair.Key.x == context && pair.Key.y == sourceWord)
//                topList.Add(pair.Value, pair.Key.z);
//
//        Console.WriteLine("Produces:");
//        for (KeyValuePair<double, String> pair : topList)
//            Console.WriteLine(pair.Key + '\t' + pair.Value);
//
//        Console.WriteLine();
//    }
//
//    public static HashMap<Triple<String, String, String>, double> CountWeightedAlignmentsWithContext(String context, String word1, String word2, int maxSubstringLength1, int maxSubstringLength2, HashMap<Triple<String, String, String>, double> probs, HashMap<Triple<String, String, String>, Pair<HashMap<Triple<String, String, String>, double>, double>> memoizationTable, out double probSum) {
//
//        Pair<HashMap<Triple<String, String, String>, double>, double> memoization;
//        if (memoizationTable.TryGetValue(new Triple<String, String, String>(context, word1, word2), out memoization)) {
//            probSum = memoization.y; //stored probSum
//            return memoization.x; //table of probs
//        }
//
//        HashMap<Triple<String, String, String>, double> result = new HashMap<Triple<String, String, String>, double>();
//
//        if (word1.Length == 0 && word2.Length == 0) //record probabilities
//        {
//            probSum = 1; //null -> null is always a perfect alignment
//            return result; //end of the line
//        }
//
//        probSum = 0;
//
//        int maxSubstringLength1f = Math.Min(word1.Length, maxSubstringLength1);
//        int maxSubstringLength2f = Math.Min(word2.Length, maxSubstringLength2);
//
//        String[] newContexts = new String[maxSubstringLength2f + 1];
//        for (int j = 1; j <= maxSubstringLength2f; j++)
//            if (j < context.Length)
//                newContexts[j] = context.Substring(j) + word2.Substring(0, j);
//            else
//                newContexts[j] = word2.Substring(j - context.Length, context.Length);
//
//        for (int i = 1; i <= maxSubstringLength1f; i++) //for each possible substring : the first word...
//        {
//            String substring1 = word1.Substring(0, i);
//
//            for (int j = 1; j <= maxSubstringLength2f; j++) //for possible substring : the second
//            {
//                if ((word1.Length - i) * maxSubstringLength2 >= word2.Length - j && (word2.Length - j) * maxSubstringLength1 >= word1.Length - i) //if we get rid of these characters, can we still cover the remainder of word2?
//                {
//                    String substring2 = word2.Substring(0, j);
//                    Triple<String, String, String> production = new Triple<String, String, String>(context, substring1, substring2);
//
//                    double prob = (probs != null ? probs[production] : 1);
//
//                    double remainderProbSum;
//                    HashMap<Triple<String, String, String>, double> remainderCounts = CountWeightedAlignmentsWithContext(newContexts[j], word1.Substring(i), word2.Substring(j), maxSubstringLength1, maxSubstringLength2, probs, memoizationTable, out remainderProbSum);
//
//                    //record this production : our results
//                    //IncrementPair(result, production, prob * remainderProbSum, 0);
//                    Dictionaries.IncrementOrSet<Triple<String, String, String>>
//                    (result, production, prob * remainderProbSum, prob * remainderProbSum);
//
//                    //update our probSum
//                    probSum += remainderProbSum * prob;
//
//                    //update all the productions that come later to take into account their preceeding production's probability
//                    for (KeyValuePair<Triple<String, String, String>, double> pair : remainderCounts) {
//                        Dictionaries.IncrementOrSet<Triple<String, String, String>>
//                        (result, pair.Key, prob * pair.Value, prob * pair.Value);
//                        //IncrementPair(result, pair.Key, pair.Value.x * prob, pair.Value.y * prob);
//                    }
//                }
//            }
//        }
//
//        memoizationTable[new Triple<String, String, String>(context, word1, word2)] = new Pair<HashMap<Triple<String, String, String>, double>, double>(result, probSum);
//        return result;
//    }
//
//
//    private static void IncrementPair(HashMap<Pair<String, String>, Pair<double, double>> dictionary, Pair<String, String> key, double addToX, double addToY) {
//        Pair<double, double> existingCount;
//        if (!dictionary.TryGetValue(key, out existingCount))
//            existingCount = new Pair<double, double>(0, 0);
//
//        existingCount.x += addToX;
//        existingCount.y += addToY;
//        dictionary[key] = existingCount;
//    }
//
//    public static HashMap<Pair<String, String>, int> CountAlignments(String word1, String word2, int maxSubstringLength, HashMap<Pair<String, String>, HashMap<Pair<String, String>, int>> memoizationTable) {
//        HashMap<Pair<String, String>, int> counts;
//        if (memoizationTable.TryGetValue(new Pair<String, String>(word1, word2), out counts))
//            return counts; //done
//        else
//            counts = new HashMap<Pair<String, String>, int>();
//
//        int maxSubstringLength1 = Math.Min(word1.Length, maxSubstringLength);
//        int maxSubstringLength2 = Math.Min(word2.Length, maxSubstringLength);
//
//        for (int i = 1; i <= maxSubstringLength1; i++) //for each possible substring : the first word...
//        {
//            String substring1 = word1.Substring(0, i);
//
//            //if (i==1 && (word1.Length-i)*maxSubstringLength >= word2.Length) //if we get rid of these characters, can we still cover the remainder of word2?
//            //{
//            //    Dictionaries.IncrementOrSet<Pair<String, String>>(counts, new Pair<String, String>(substring1, ""), 1, 1);
//            //    Dictionaries.Add<Pair<String,String>>(counts, CountAlignments(word1.Substring(i), word2, maxSubstringLength, memoizationTable),1); //empty production
//            //}
//
//            for (int j = 1; j <= maxSubstringLength2; j++) //for possible substring in the second
//            {
//                if ((word1.Length - i) * maxSubstringLength >= word2.Length - j && (word2.Length - j) * maxSubstringLength >= word1.Length - i) //if we get rid of these characters, can we still cover the remainder of word2?
//                {
//                    Dictionaries.IncrementOrSet<Pair<String, String>>
//                    (counts, new Pair<String, String>(substring1, word2.Substring(0, j)), 1, 1);
//                    Dictionaries.AddTo<Pair<String, String>>
//                    (counts, CountAlignments(word1.Substring(i), word2.Substring(j), maxSubstringLength, memoizationTable), 1)
//                    ;
//                }
//            }
//        }
//
//        memoizationTable[new Pair<String, String>(word1, word2)] = counts;
//        return counts;
//    }
//
//    public static void FindAlignments(String word1, String word2, int maxSubstringLength1, int maxSubstringLength2, HashMap<Pair<String, String>, Boolean> alignments, HashMap<Pair<String, String>, Boolean> memoizationTable) {
//        if (memoizationTable.ContainsKey(new Pair<String, String>(word1, word2)))
//            return; //done
//
//        int maxSubstringLength1f = Math.Min(word1.Length, maxSubstringLength1);
//        int maxSubstringLength2f = Math.Min(word2.Length, maxSubstringLength2);
//
//        for (int i = 1; i <= maxSubstringLength1f; i++) //for each possible substring in the first word...
//        {
//            String substring1 = word1.Substring(0, i);
//
//            //if (i==1 && (word1.Length-i)*maxSubstringLength >= word2.Length) //if we get rid of these characters, can we still cover the remainder of word2?
//            //{
//            //    Dictionaries.IncrementOrSet<Pair<String, String>>(counts, new Pair<String, String>(substring1, ""), 1, 1);
//            //    Dictionaries.Add<Pair<String,String>>(counts, CountAlignments(word1.Substring(i), word2, maxSubstringLength, memoizationTable),1); //empty production
//            //}
//
//            for (int j = 1; j <= maxSubstringLength2f; j++) //for possible substring in the second
//            {
//                if ((word1.Length - i) * maxSubstringLength2 >= word2.Length - j && (word2.Length - j) * maxSubstringLength1 >= word1.Length - i) //if we get rid of these characters, can we still cover the remainder of word2?
//                {
//                    alignments[new Pair<String, String>(substring1, word2.Substring(0, j))] = true;
//                    FindAlignments(word1.Substring(i), word2.Substring(j), maxSubstringLength1, maxSubstringLength2, alignments, memoizationTable);
//                }
//            }
//        }
//
//        memoizationTable[new Pair<String, String>(word1, word2)] = true;
//    }
}
