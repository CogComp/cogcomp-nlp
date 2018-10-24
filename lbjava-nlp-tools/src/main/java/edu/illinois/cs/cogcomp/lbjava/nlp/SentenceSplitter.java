/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.lbjava.nlp;

import edu.illinois.cs.cogcomp.lbjava.parse.LineByLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Use this class to extract sentences from plain text.  The user constructs
 * an object of this class with the file name of a document written in
 * natural English (i.e., with no annotations added or any type of
 * preprocessing performed).  <b>It should be noted that this class will
 * interpret empty lines that appear in the input as paragraph
 * boundaries.</b>
 * <p/>
 * <p> The user can then retrieve <code>Sentence</code>s one at a time with
 * the <code>next()</code> method, or all at once with the
 * <code>splitAll()</code> method.  The returned <code>Sentence</code>s'
 * <code>start</code> and <code>end</code> fields represent offsets into the
 * file they were extracted from.  Every character in between those two
 * offsets inclusive, including extra spaces, newlines, etc., is included in
 * the <code>Sentence</code> as it appeared in the paragraph.
 * <p/>
 * <p> A {@link #main(String[])} method is also implemented which applies
 * this class to plain text in a straight-forward way.
 *
 * @author Nick Rizzolo
 * @see Sentence
 **/
public class SentenceSplitter extends LineByLine {
    private static final Logger logger = LoggerFactory.getLogger(SentenceSplitter.class);

    /**
     * Regular expression matching whitespace separated words including those
     * that are hyphenated and cross over a line boundary.
     **/
    private static final Pattern wordMatcher =
            Pattern.compile("([^-\\s]-\n\\s*(?=\\S)|\\S)+");
    /**
     * Regular expression matching an entire string if that string contains no
     * capital letters except for those within angled brackets (&lt;&gt;).
     **
     private static final Pattern lowerCaseWithXML =
     Pattern.compile("^([^A-Z]*(<[^>]*>)?)*$");
     */
    /**
     * Regular expression matching any lower case letter.
     */
    private static final Pattern lowerCaseLetter = Pattern.compile("[a-z]");
    /**
     * Regular expression matching a sequence of capital letters and dots
     * ending with a capital letter.
     **/
    private static final Pattern capitalsAndDots =
            Pattern.compile("^([A-Z]\\.)*[A-Z]$");

    /**
     * Run this program on a file containing plain text, and it will produce
     * the same text rearranged so that each line contains exactly one sentence
     * on <code>STDOUT</code>.
     * <p/>
     * <p> Usage:
     * <code> java edu.illinois.cs.cogcomp.lbjava.edu.illinois.cs.cogcomp.lbjava.nlp.SentenceSplitter &lt;file name&gt; </code>
     *
     * @param args The command line arguments.
     **/
    public static void main(String[] args) {
        String filename = null;

        try {
            filename = args[0];
            if (args.length > 1) throw new Exception();
        } catch (Exception e) {
            System.err.println("usage: java edu.illinois.cs.cogcomp.lbjava.edu.illinois.cs.cogcomp.lbjava.nlp.SentenceSplitter <file name>");
            System.exit(1);
        }

        SentenceSplitter splitter = new SentenceSplitter(filename);

        for (Sentence s = (Sentence) splitter.next(); s != null;
             s = (Sentence) splitter.next()) {
            StringBuffer buffer = new StringBuffer(s.text);

            for (int i = 0; i < buffer.length(); ++i) {
                char c = buffer.charAt(i);
                if (c == '\n' || c == '\r' || c == '\f') buffer.setCharAt(i, ' ');
            }

            System.out.println(buffer.toString());
        }
    }


    /**
     * Contains the offset of a paragraph currently being processed.
     */
    protected int currentOffset;
    /**
     * Contains sentences ready to be returned to the user upon request.
     */
    protected LinkedList<Sentence> sentences;
    /**
     * When the constructor taking an array argument is used, this variable
     * keeps track of the element in the array currently being used.
     **/
    protected int index;
    /**
     * When the constructor taking an array argument is used, this variable
     * stores that array.
     **/
    protected String[] input;


    /**
     * Sentence splits the given file.
     *
     * @param file The name of the file to sentence split.
     **/
    public SentenceSplitter(String file) {
        super(file);
        sentences = new LinkedList<>();
    }


    /**
     * Sentence splits the given input.
     *
     * @param input Plain text.  Each element of this array represents a line,
     *              with any line termination characters removed.
     **/
    public SentenceSplitter(String[] input) {
        this.input = input;
        sentences = new LinkedList<>();
    }


    /**
     * If constructor taking a file name as input was used, this method simply
     * calls the method of the same name in <code>LineByLine</code>; otherwise,
     * it returns the next element of the array.
     *
     * @return The next line of input.
     **/
    protected String readLine() {
        if (input != null) {
            if (index < input.length) return input[index++];
            return null;
        }

        return super.readLine();
    }


    /**
     * This method is used to extract a paragraph at a time from the input.
     *
     * @return The extracted paragraph, or a string containing only whitespace
     * if no text remains in the input.
     **/
    protected String getParagraph() {
        StringBuilder paragraph = new StringBuilder();
        String line;

        for (line = readLine(); line != null && line.trim().length() == 0;
             line = readLine()) {
            paragraph.append(line);
            paragraph.append("\n");
        }

        for (; line != null && line.trim().length() != 0; line = readLine()) {
            paragraph.append(line);
            paragraph.append("\n");
        }

        if (line != null) {
            paragraph.append(line);
            paragraph.append("\n");
        }

        return paragraph.toString();
    }


    /**
     * Retrieves the next sentence off the queue and returns it.
     *
     * @return The next sentence found or <code>null</code> if there are no
     * more sentences.
     **/
    public Object next() {
        if (sentences.size() == 0) {
            String paragraph = getParagraph();
            if (paragraph.trim().length() != 0) process(paragraph);
            currentOffset += paragraph.length();
        }

        if (sentences.size() == 0) return null;
        return sentences.removeFirst();
    }


    /**
     * Retrieves every sentence found in the input paragraphs that have been
     * provided so far in array form.
     *
     * @return All sentences in the input paragraphs.
     **/
    public Sentence[] splitAll() {
        for (String paragraph = getParagraph(); paragraph.trim().length() != 0;
             paragraph = getParagraph()) {
            if (paragraph.trim().length() != 0) process(paragraph);
            currentOffset += paragraph.length();
        }

        return sentences.toArray(new Sentence[sentences.size()]);
    }


    /**
     * This method does the actual work, deciding where sentences begin and end
     * and populating the <code>sentences</code> member variable.
     *
     * @param paragraph The paragraph to process.
     **/
    protected void process(String paragraph) {
        if (paragraph.trim().length() == 0) return;
        Matcher m = wordMatcher.matcher(paragraph);
        LinkedList<Word> w = new LinkedList<>();
        while (m.find()) w.add(new Word(m.group(), m.start(), m.end() - 1));
        Word[] words = w.toArray(new Word[w.size()]);

        int sentenceStart = words[0].start;
        boolean dumpTrailingWords = true;

        //boolean allLowerCase = lowerCaseWithXML.matcher(paragraph).matches();
        // The line of code commented above seems to take time exponential in the
        // distance from the start of the paragraph to the first capital letter.
        // I don't get it.  But since it does, we replace it with the code below.

        boolean allLowerCase = true;
        {
            boolean insideTag = false;
            char[] chars = paragraph.toCharArray();

            for (int i = 0; i < paragraph.length() && allLowerCase; ++i) {
                if (insideTag) insideTag = chars[i] != '>';
                else {
                    if (chars[i] == '<') insideTag = paragraph.indexOf('>', i) != -1;
                    else allLowerCase = !Character.isUpperCase(chars[i]);
                }
            }
        }

        for (int i = 0; i < words.length; ++i) {
            int punctuationIndex = words[i].form.lastIndexOf('.');

            int index = words[i].form.lastIndexOf('?');
            if (index > punctuationIndex) punctuationIndex = index;

            index = words[i].form.lastIndexOf('!');
            if (index > punctuationIndex) punctuationIndex = index;

            if (punctuationIndex != -1) {
                Word next1 = (i + 1 < words.length) ? words[i + 1] : null;
                Word next2 = (i + 2 < words.length) ? words[i + 2] : null;
                int length = words[i].form.length();
                if (allLowerCase) index = words[i].form.indexOf('.');

                if (allLowerCase && length > 5
                        && (index == -1 || index == punctuationIndex)
                        && !lowerCaseLetter.matcher(
                        words[i].form.substring(punctuationIndex)).find()
                        || boundary(punctuationIndex, words[i], next1, next2)) {
                    sentences.add(
                            new Sentence(paragraph.substring(sentenceStart,
                                    words[i].end + 1),
                                    currentOffset + sentenceStart,
                                    currentOffset + words[i].end));
                    if (i + 1 < words.length) sentenceStart = words[i + 1].start;
                    else dumpTrailingWords = false;
                }
            }
        }

        if (dumpTrailingWords)
            sentences.add(
                    new Sentence(paragraph.substring(sentenceStart,
                            words[words.length - 1].end + 1),
                            currentOffset + sentenceStart,
                            currentOffset + words[words.length - 1].end));
    }


    /**
     * Determines whether the given punctuation represents the end of a
     * sentence based on elements of the paragraph immediately surrounding the
     * punctuation.
     *
     * @param index The index of the punctuation in question in its word.
     * @param word  The word containing the punctuation.
     * @param next1 The word one after the word containing the
     *              punctuation.
     * @param next2 The word two after the word containing the
     *              punctuation.
     **/
    protected boolean boundary(int index, Word word, Word next1, Word next2) {
        char punctuation = word.form.charAt(index);
        Word prefix = new Word(word.form.substring(0, index));
        Word suffix = new Word(word.form.substring(index + 1));
        Word root = new Word(prefix.form);
        while (root.form.length() > 0
                && "\"'`{[(".indexOf(root.form.charAt(0)) != -1)
            root.form = root.form.substring(1);

        if ("yahoo!".equalsIgnoreCase(root.form + punctuation)) return false;

        if (punctuation == '?' || punctuation == '!')
            return next1 == null
                    || suffix.form.length() == 0
                    && (next1.capitalized || startsWithQuote(next1)
                    || next1.form.equals(".")
                    || next2 != null && next2.capitalized
                    && (next1.form.equals("--")
                    || next1.form.equals("-RBR-")))
                    || isClose(suffix) && hasStartMarker(next1);

        if (next1 == null) return true;

        if (suffix.form.length() == 0) {
            if (startsWithQuote(next1) || startsWithOpenBracket(next1)) return true;

            if (next1.form.equals("-RBR-") && next2 != null
                    && next2.form.equals("--"))
                return false;

            if (isClosingBracket(next1)) return true;

            if (prefix.form.length() == 0 && next1.form.equals("."))
                return false;

            if (next1.form.equals(".")) return true;

            if (next1.form.equals("--") && next2 != null && next2.capitalized
                    && endsWithQuote(prefix))
                return false;

            if (next1.form.equals("--")
                    && next2 != null && (next2.capitalized || startsWithQuote(next2)))
                return true;

            if (next1.capitalized || Character.isDigit(next1.form.charAt(0)))
                return isTerminal(root)
                        || !((root.form.equals("p.m")
                        || root.form.equals("a.m"))
                        && isTimeZone(next1)
                        || isHonorific(root) || startsWithQuote(prefix)
                        || startsWithOpenBracket(prefix)
                        && !endsWithCloseBracket(prefix)
                        || capitalsAndDots.matcher(prefix.form).find()
                        && !sentenceBeginner(next1));
        }

        return isClose(suffix) && hasStartMarker(next1) && !isHonorific(root);
    }


    /**
     * Simple check to see if the given word can reliably be identified as the
     * first word of a sentence.
     *
     * @param word The word in question.
     **/
    protected boolean sentenceBeginner(Word word) {
        return word.form.equals("The");
    }


    /**
     * Determines whether the first character of the argument is any of the
     * three varieties of quotes: ' " `.
     *
     * @param w The word in question.
     * @return <code>true</code> if and only if the first character of the
     * argument is any of the three varieties of quotes.
     **/
    protected boolean startsWithQuote(Word w) {
        return w.form.length() != 0 && (w.form.charAt(0) == '\'' || w.form.charAt(0) == '"' || w.form.charAt(0) == '`');
    }


    /**
     * Determines whether the argument ends with any of the following varieties
     * of closing quote: ' '' ''' " '" .
     *
     * @param w The word in question.
     * @return <code>true</code> if and only if the argument ends with any of
     * the varieties of quotes named above.
     **/
    protected boolean endsWithQuote(Word w) {
        return w.form.endsWith("'") || w.form.endsWith("''")
                || w.form.endsWith("'''") || w.form.endsWith("\"")
                || w.form.endsWith("'\"");
    }


    /**
     * Determines whether the argument represents a closing bracket or a
     * closing quote.
     *
     * @param w The word in question.
     * @return <code>true</code> if and only if the argument represents either
     * a closing bracket or a closing quote.
     **/
    protected boolean isClose(Word w) {
        return isClosingBracket(w) || isClosingQuote(w);
    }


    /**
     * Determines whether the argument is exactly equal to any of the following
     * varieties of closing bracket: ) } ] -RBR- .
     *
     * @param w The word in question.
     * @return <code>true</code> if and only if the argument is exactly equal
     * to any of the above varieties of closing bracket.
     **/
    protected boolean isClosingBracket(Word w) {
        return w.form.equals(")") || w.form.equals("}") || w.form.equals("]")
                || w.form.equals("-RBR-");
    }


    /**
     * Determines whether the argument is exactly equal to any of the following
     * varieties of closing quote: ' '' ''' " '" .
     *
     * @param w The word in question.
     * @return <code>true</code> if and only if the argument is exactly equal
     * to any of the above varieties of closing quote.
     **/
    protected boolean isClosingQuote(Word w) {
        return w.form.equals("'") || w.form.equals("''") || w.form.equals("'''")
                || w.form.equals("\"") || w.form.equals("'\"");
    }


    /**
     * Determines whether the argument contains any of the following varieties
     * of "start marker" at its beginning: an open quote, and open bracket, or
     * a capital letter.
     *
     * @param w The word in question.
     * @return <code>true</code> if and only if the argument starts with a
     * "start marker".
     **/
    protected boolean hasStartMarker(Word w) {
        return w.capitalized || startsWithOpenQuote(w)
                || startsWithOpenBracket(w);
    }


    /**
     * Determines whether the argument starts with any of the following
     * varieties of open quote: ` `` ``` " "` .
     *
     * @param w The word in question.
     * @return <code>true</code> if and only if the argument starts with one of
     * the varieties of open quote named above.
     **/
    protected boolean startsWithOpenQuote(Word w) {
        return w.form.startsWith("`") || w.form.startsWith("``")
                || w.form.startsWith("```") || w.form.startsWith("\"")
                || w.form.startsWith("\"`");
    }


    /**
     * Determines whether the argument starts with any of the following
     * varieties of open bracket: ( { [ -LBR- .
     *
     * @param w The word in question.
     * @return <code>true</code> if and only if the argument starts with any of
     * the varieties of open bracket named above.
     **/
    protected boolean startsWithOpenBracket(Word w) {
        return w.form.startsWith("(") || w.form.startsWith("{")
                || w.form.startsWith("[") || w.form.startsWith("-LBR-");
    }


    /**
     * Determines whether the argument ends with any of the following
     * varieties of open bracket: ) } ] -RBR- .
     *
     * @param w The word in question.
     * @return <code>true</code> if and only if the argument starts with any of
     * the varieties of open bracket named above.
     **/
    protected boolean endsWithCloseBracket(Word w) {
        return w.form.endsWith(")") || w.form.endsWith("}")
                || w.form.endsWith("]") || w.form.endsWith("-RBR-");
    }


    /**
     * Determines whether the argument is a United States time zone
     * abbreviation (AST, CST, EST, HST, MST, PST, ADT, CDT, EDT, HDT, MDT,
     * PDT, or UTC-11).
     *
     * @param w The word in question.
     * @return <code>true</code> if and only if the argument matches any of the
     * above time zone abbreviations.
     **/
    protected boolean isTimeZone(Word w) {
        return w.form.equals("AST") || w.form.equals("CST")
                || w.form.equals("EST") || w.form.equals("HST")
                || w.form.equals("MST") || w.form.equals("PST")
                || w.form.equals("ADT") || w.form.equals("CDT")
                || w.form.equals("EDT") || w.form.equals("HDT")
                || w.form.equals("MDT") || w.form.equals("PDT")
                || w.form.equals("UTC") || w.form.equals("UTC-11");
    }


    /**
     * Determines whether the argument is exactly equal to any of the following
     * terminal abbreviations: Esq Jr Sr M.D Ph.D .
     *
     * @param w The word in question.
     * @return <code>true</code> if and only if the argument matches any of the
     * above terminal abbreviations.
     **/
    protected boolean isTerminal(Word w) {
        return w.form.equals("Esq") || w.form.equals("Jr")
                || w.form.equals("Sr") || w.form.equals("M.D")
                || w.form.equals("Ph.D");
    }


    /**
     * Determines whether the argument is exactly equal to any of the honorifics
     * listed below.
     * <p/>
     * <ul>
     * <li> APR <li> AUG <li> Adj <li> Adm <li> Adv <li> Apr <li> Asst
     * <li> Aug <li> Bart <li> Bldg <li> Brig <li> Bros <li> Capt <li> Cmdr
     * <li> Col <li> Comdr <li> Con <li> Cpl <li> DEC <li> DR <li> Dec
     * <li> Dr <li> Ens <li> FEB <li> Feb <li> Gen <li> Gov <li> Hon
     * <li> Hosp <li> Insp <li> JAN <li> JUL <li> JUN <li> Jan <li> Jul
     * <li> Jun <li> Lt <li> MAR <li> MM <li> MR <li> MRS <li> MS <li> MT
     * <li> Maj <li> Mar <li> Messrs <li> Mlle <li> Mme <li> Mr <li> Mrs
     * <li> Ms <li> Msgr <li> Mt <li> NO <li> NOV <li> Nov <li> OCT <li> Oct
     * <li> Op <li> Ord <li> Pfc <li> Ph <li> Prof <li> Pvt <li> Rep
     * <li> Reps <li> Res <li> Rev <li> Rt <li> SEP <li> SEPT <li> Sen
     * <li> Sens <li> Sep <li> Sept <li> Sfc <li> Sgt <li> Sr <li> St
     * <li> Supt <li> Surg <li> U.S <li> apr <li> aug <li> dec <li> feb
     * <li> jan <li> jul <li> jun
     * <li>
     * <strike>mar</strike> -- It's a word, so it must be capitalized to be
     * considered an honorific.
     * <li> nov <li> oct <li> sep <li> sept <li> v <li> vs
     * </ul>
     *
     * @param w The word in question.
     * @return <code>true</code> if and only if the argument is exactly equal
     * to any of the honorifics listed above.
     **/
    protected boolean isHonorific(Word w) {
        return w.form.equals("APR") || w.form.equals("AUG")
                || w.form.equals("Adj") || w.form.equals("Adm")
                || w.form.equals("Adv") || w.form.equals("Apr")
                || w.form.equals("Asst") || w.form.equals("Aug")
                || w.form.equals("Bart") || w.form.equals("Bldg")
                || w.form.equals("Brig") || w.form.equals("Bros")
                || w.form.equals("Capt") || w.form.equals("Cmdr")
                || w.form.equals("Col") || w.form.equals("Comdr")
                || w.form.equals("Con") || w.form.equals("Cpl")
                || w.form.equals("DEC") || w.form.equals("DR")
                || w.form.equals("Dec") || w.form.equals("Dr")
                || w.form.equals("Ens") || w.form.equals("FEB")
                || w.form.equals("Feb") || w.form.equals("Gen")
                || w.form.equals("Gov") || w.form.equals("Hon")
                || w.form.equals("Hosp") || w.form.equals("Insp")
                || w.form.equals("JAN") || w.form.equals("JUL")
                || w.form.equals("JUN") || w.form.equals("Jan")
                || w.form.equals("Jul") || w.form.equals("Jun")
                || w.form.equals("Lt") || w.form.equals("MAR")
                || w.form.equals("MM") || w.form.equals("MR")
                || w.form.equals("MRS") || w.form.equals("MS")
                || w.form.equals("MT") || w.form.equals("Maj")
                || w.form.equals("Mar") || w.form.equals("Messrs")
                || w.form.equals("Mlle") || w.form.equals("Mme")
                || w.form.equals("Mr") || w.form.equals("Mrs")
                || w.form.equals("Ms") || w.form.equals("Msgr")
                || w.form.equals("Mt") || w.form.equals("NO")
                || w.form.equals("NOV") || w.form.equals("No")
                || w.form.equals("Nov") || w.form.equals("OCT")
                || w.form.equals("Oct") || w.form.equals("Op")
                || w.form.equals("Ord") || w.form.equals("Pfc")
                || w.form.equals("Ph") || w.form.equals("Prof")
                || w.form.equals("Pvt") || w.form.equals("Rep")
                || w.form.equals("Reps") || w.form.equals("Res")
                || w.form.equals("Rev") || w.form.equals("Rt")
                || w.form.equals("SEP") || w.form.equals("SEPT")
                || w.form.equals("ST") || w.form.equals("Sen")
                || w.form.equals("Sens") || w.form.equals("Sep")
                || w.form.equals("Sept") || w.form.equals("Sfc")
                || w.form.equals("Sgt") || w.form.equals("Sr")
                || w.form.equals("St") || w.form.equals("Supt")
                || w.form.equals("Surg") || w.form.equals("U.S")
                || w.form.equals("apr") || w.form.equals("aug")
                || w.form.equals("dec") || w.form.equals("feb")
                || w.form.equals("jan") || w.form.equals("jul")
                || w.form.equals("jun") // || w.form.equals("mar")
                || w.form.equals("nov") || w.form.equals("oct")
                || w.form.equals("sep") || w.form.equals("sept")
                || w.form.equals("v") || w.form.equals("vs");
    }
}

