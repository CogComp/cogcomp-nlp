/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.lbjava.nlp;

import java.io.Serializable;
import java.util.HashMap;


/**
 * This class converts the <code>String</code> names of POS tags into
 * discrete integer values.
 *
 * @author Nick Rizzolo
 **/
public class POS implements Serializable {
    /**
     * The actual part of speech is encoded as an integer.
     */
    private int value;


    /**
     * Constructor that initializes the <code>value</code> variable.  This
     * constructor should never be called, since all possible parts of speech
     * are numerated by name as <code>public static final</code> variables.
     *
     * @param v The value of the new part of speech object.
     **/
    public POS(int v) {
        value = v;
    }


    /**
     * Returns the name of the part of speech represented by this object.
     */
    public String toString() {
        return names[value];
    }

    /**
     * Returns the token that represents the same part of speech.
     */
    public String toToken() {
        return tokens[value];
    }


    /**
     * Returns the integer used to represent this part of speech tag.
     */
    public int getValue() {
        return value;
    }


    /**
     * Two <code>POS</code> objects are equal iff their <code>value</code>
     * variables are equal.
     *
     * @param o The object to compare against this <code>POS</code> object.
     * @return <code>true</code> iff the input object is a <code>POS</code>
     * object with the same <code>value</code>.
     **/
    public boolean equals(Object o) {
        return o instanceof POS && ((POS) o).value == value;
    }


    /**
     * Simply returns the <code>value</code> variable.
     */
    public int hashCode() {
        return value;
    }


    /**
     * An array of all names of part of speech tags. <br><br>
     * <p/>
     * Those names are: <br>
     * <ol start=0>
     * <li> pound sign <li> dollar sign <li> open double quote
     * <li> close double quote <li> comma <li> left bracket
     * <li> right bracket <li> final punctuation <li> (semi-)colon
     * <li> coordinating conjunction <li> cardinal number <li> determiner
     * <li> existential there <li> foreign word <li> preposition
     * <li> adjective <li> comparative adjective <li> superlative adjective
     * <li> list item marker <li> modal <li> singular noun
     * <li> proper singular noun <li> proper plural noun <li> plural noun
     * <li> predeterminer <li> possesive ending <li> possesive pronoun
     * <li> personal pronoun <li> adverb <li> comparative adverb
     * <li> superlative adverb <li> particle <li> symbol <li> to
     * <li> interjection <li> base form verb <li> verb past tense
     * <li> verb gerund / present participle <li> verb past participle
     * <li> verb non 3rd ps.  sing. present <li> verb 3rd ps.  sing. present
     * <li> wh-determiner <li> wh-pronoun <li> possesive wh-pronoun
     * <li> wh-adverb
     * </ol>
     **/
    public static final String[] names =
            {
                    "pound sign", "dollar sign", "open double quote", "close double quote",
                    "comma", "left bracket", "right bracket", "final punctuation",
                    "(semi-)colon", "coordinating conjunction", "cardinal number",
                    "determiner", "existential there", "foreign word", "preposition",
                    "adjective", "comparative adjective", "superlative adjective",
                    "list item marker", "modal", "singular noun", "proper singular noun",
                    "proper plural noun", "plural noun", "predeterminer",
                    "possesive ending", "personal pronoun", "possesive pronoun", "adverb",
                    "comparative adverb", "superlative adverb", "particle", "symbol", "to",
                    "interjection", "base form verb", "verb past tense",
                    "verb gerund / present participle", "verb past participle",
                    "verb non 3rd ps. sing.  present", "verb 3rd ps.  sing.  present",
                    "wh-determiner", "wh-pronoun", "possesive wh-pronoun", "wh-adverb"
            };


    /**
     * An array of all tokens that represent parts of speech as found in
     * corpora. <br><br>
     * <p/>
     * Those tokens are: <br>
     * <ol start=0>
     * <li> "#" <li> "$" <li> "``" <li> "''" <li> "," <li> "-LRB-"
     * <li> "-RRB-" <li> "." <li> ":" <li> "CC" <li> "CD" <li> "DT" <li> "EX"
     * <li> "FW" <li> "IN" <li> "JJ" <li> "JJR" <li> "JJS" <li> "LS"
     * <li> "MD" <li> "NN" <li> "NNP" <li> "NNPS" <li> "NNS" <li> "PDT"
     * <li> "POS" <li> "PRP" <li> "PRP$" <li> "RB" <li> "RBR" <li> "RBS"
     * <li> "RP" <li> "SYM" <li> "TO" <li> "UH" <li> "VB" <li> "VBD"
     * <li> "VBG" <li> "VBN" <li> "VBP" <li> "VBZ" <li> "WDT" <li> "WP"
     * <li> "WP$" <li> "WRB"
     * </ol>
     **/
    public static final String[] tokens =
            {
                    "#", "$", "``", "''", ",", "-LRB-", "-RRB-", ".", ":", "CC", "CD", "DT",
                    "EX", "FW", "IN", "JJ", "JJR", "JJS", "LS", "MD", "NN", "NNP", "NNPS",
                    "NNS", "PDT", "POS", "PRP", "PRP$", "RB", "RBR", "RBS", "RP", "SYM",
                    "TO", "UH", "VB", "VBD", "VBG", "VBN", "VBP", "VBZ", "WDT", "WP", "WP$",
                    "WRB"
            };


    /**
     * Map from <code>String</code> tokens to <code>POS</code> objects.
     */
    private static HashMap<String, edu.illinois.cs.cogcomp.lbjava.nlp.POS> fromTokens = null;


    /**
     * Converts a <code>POS</code> object to the token that represents the same
     * part of speech.
     *
     * @param tag The <code>POS</code> object to convert.
     * @return The token representing the same part of speech.
     **/
    public static String toToken(POS tag) {
        return tokens[tag.value];
    }


    /**
     * Converts a token to the <code>POS</code> object that represents the same
     * part of speech.
     *
     * @param s The token to convert.
     * @return The <code>POS</code> object representing the same part of
     * speech.
     **/
    public static POS fromToken(String s) {
        POS result = fromTokens.get(s);
        assert result != null
                : "LBJava ASSERTION FAILED: Unrecognized POS tag: '" + s + "'";
        return result;
    }


    /**
     * <code>POS</code> object representing the "pound sign" tag.
     */
    public static final POS pound = new POS(0);
    /**
     * <code>POS</code> object representing the "dollar sign" tag.
     */
    public static final POS dollar = new POS(1);
    /**
     * <code>POS</code> object representing the "open double quote" tag.
     */
    public static final POS openDoubleQuote = new POS(2);
    /**
     * <code>POS</code> object representing the "close double quote" tag.
     */
    public static final POS closeDoubleQuote = new POS(3);
    /**
     * <code>POS</code> object representing the "comma" tag.
     */
    public static final POS comma = new POS(4);
    /**
     * <code>POS</code> object representing the "left bracket" tag.
     */
    public static final POS leftBracket = new POS(5);
    /**
     * <code>POS</code> object representing the "right bracket" tag.
     */
    public static final POS rightBracket = new POS(6);
    /**
     * <code>POS</code> object representing the "final punctuation" tag.
     */
    public static final POS period = new POS(7);
    /**
     * <code>POS</code> object representing the "(semi-)colon" tag.
     */
    public static final POS colon = new POS(8);
    /**
     * <code>POS</code> object representing the "coordinating conjunction" tag.
     **/
    public static final POS CC = new POS(9);
    /**
     * <code>POS</code> object representing the "cardinal number" tag.
     */
    public static final POS CD = new POS(10);
    /**
     * <code>POS</code> object representing the "determiner" tag.
     */
    public static final POS DT = new POS(11);
    /**
     * <code>POS</code> object representing the "existential there" tag.
     */
    public static final POS EX = new POS(12);
    /**
     * <code>POS</code> object representing the "foreign word" tag.
     */
    public static final POS FW = new POS(13);
    /**
     * <code>POS</code> object representing the "preposition" tag.
     */
    public static final POS IN = new POS(14);
    /**
     * <code>POS</code> object representing the "adjective" tag.
     */
    public static final POS JJ = new POS(15);
    /**
     * <code>POS</code> object representing the "comparative adjective" tag.
     */
    public static final POS JJR = new POS(16);
    /**
     * <code>POS</code> object representing the "superlative adjective" tag.
     */
    public static final POS JJS = new POS(17);
    /**
     * <code>POS</code> object representing the "list item marker" tag.
     */
    public static final POS LS = new POS(18);
    /**
     * <code>POS</code> object representing the "modal" tag.
     */
    public static final POS MD = new POS(19);
    /**
     * <code>POS</code> object representing the "singular noun" tag.
     */
    public static final POS NN = new POS(20);
    /**
     * <code>POS</code> object representing the "proper singular noun" tag.
     */
    public static final POS NNP = new POS(21);
    /**
     * <code>POS</code> object representing the "proper plural noun" tag.
     */
    public static final POS NNPS = new POS(22);
    /**
     * <code>POS</code> object representing the "plural noun" tag.
     */
    public static final POS NNS = new POS(23);
    /**
     * <code>POS</code> object representing the "predeterminer" tag.
     */
    public static final POS PDT = new POS(24);
    /**
     * <code>POS</code> object representing the "possesive ending" tag.
     */
    public static final POS POS = new POS(25);
    /**
     * <code>POS</code> object representing the "personal pronoun" tag.
     */
    public static final POS PRP = new POS(26);
    /**
     * <code>POS</code> object representing the "possessive pronoun" tag.
     */
    public static final POS PRP_DOLLAR = new POS(27);
    /**
     * <code>POS</code> object representing the "adverb" tag.
     */
    public static final POS RB = new POS(28);
    /**
     * <code>POS</code> object representing the "comparative adverb" tag.
     */
    public static final POS RBR = new POS(29);
    /**
     * <code>POS</code> object representing the "superlative adverb" tag.
     */
    public static final POS RBS = new POS(30);
    /**
     * <code>POS</code> object representing the "particle" tag.
     */
    public static final POS RP = new POS(31);
    /**
     * <code>POS</code> object representing the "symbol" tag.
     */
    public static final POS SYM = new POS(32);
    /**
     * <code>POS</code> object representing the "to" tag.
     */
    public static final POS TO = new POS(33);
    /**
     * <code>POS</code> object representing the "interjection" tag.
     */
    public static final POS UH = new POS(34);
    /**
     * <code>POS</code> object representing the "base form verb" tag.
     */
    public static final POS VB = new POS(35);
    /**
     * <code>POS</code> object representing the "verb past tense" tag.
     */
    public static final POS VBD = new POS(36);
    /**
     * <code>POS</code> object representing the "verb gerund / present
     * participle" tag.
     **/
    public static final POS VBG = new POS(37);
    /**
     * <code>POS</code> object representing the "verb past participle" tag.
     */
    public static final POS VBN = new POS(38);
    /**
     * <code>POS</code> object representing the "verb non 3rd ps sing present"
     * tag.
     **/
    public static final POS VBP = new POS(39);
    /**
     * <code>POS</code> object representing the "verb 3rd ps sing present" tag.
     **/
    public static final POS VBZ = new POS(40);
    /**
     * <code>POS</code> object representing the "wh-determiner" tag.
     */
    public static final POS WDT = new POS(41);
    /**
     * <code>POS</code> object representing the "wh-pronoun" tag.
     */
    public static final POS WP = new POS(42);
    /**
     * <code>POS</code> object representing the "possesive wh-pronoun" tag.
     */
    public static final POS WP_DOLLAR = new POS(43);
    /**
     * <code>POS</code> object representing the "wh-adverb" tag.
     */
    public static final POS WRB = new POS(44);


    static {
        fromTokens = new HashMap<>();
        fromTokens.put(tokens[pound.value], pound);
        fromTokens.put(tokens[dollar.value], dollar);
        fromTokens.put(tokens[openDoubleQuote.value], openDoubleQuote);
        fromTokens.put(tokens[closeDoubleQuote.value], closeDoubleQuote);
        fromTokens.put(tokens[comma.value], comma);
        fromTokens.put(tokens[leftBracket.value], leftBracket);
        fromTokens.put(tokens[rightBracket.value], rightBracket);
        fromTokens.put(tokens[period.value], period);
        fromTokens.put(tokens[colon.value], colon);
        fromTokens.put(tokens[CC.value], CC);
        fromTokens.put(tokens[CD.value], CD);
        fromTokens.put(tokens[DT.value], DT);
        fromTokens.put(tokens[EX.value], EX);
        fromTokens.put(tokens[FW.value], FW);
        fromTokens.put(tokens[IN.value], IN);
        fromTokens.put(tokens[JJ.value], JJ);
        fromTokens.put(tokens[JJR.value], JJR);
        fromTokens.put(tokens[JJS.value], JJS);
        fromTokens.put(tokens[LS.value], LS);
        fromTokens.put(tokens[MD.value], MD);
        fromTokens.put(tokens[NN.value], NN);
        fromTokens.put(tokens[NNP.value], NNP);
        fromTokens.put(tokens[NNPS.value], NNPS);
        fromTokens.put(tokens[NNS.value], NNS);
        fromTokens.put(tokens[PDT.value], PDT);
        fromTokens.put(tokens[POS.value], POS);
        fromTokens.put(tokens[PRP.value], PRP);
        fromTokens.put(tokens[PRP_DOLLAR.value], PRP_DOLLAR);
        fromTokens.put(tokens[RB.value], RB);
        fromTokens.put(tokens[RBR.value], RBR);
        fromTokens.put(tokens[RBS.value], RBS);
        fromTokens.put(tokens[RP.value], RP);
        fromTokens.put(tokens[SYM.value], SYM);
        fromTokens.put(tokens[TO.value], TO);
        fromTokens.put(tokens[UH.value], UH);
        fromTokens.put(tokens[VB.value], VB);
        fromTokens.put(tokens[VBD.value], VBD);
        fromTokens.put(tokens[VBG.value], VBG);
        fromTokens.put(tokens[VBN.value], VBN);
        fromTokens.put(tokens[VBP.value], VBP);
        fromTokens.put(tokens[VBZ.value], VBZ);
        fromTokens.put(tokens[WDT.value], WDT);
        fromTokens.put(tokens[WP.value], WP);
        fromTokens.put(tokens[WP_DOLLAR.value], WP_DOLLAR);
        fromTokens.put(tokens[WRB.value], WRB);
    }
}

