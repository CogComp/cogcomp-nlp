/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.utilities;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

/**
 * StringCleanup maps characters in "broader" encodings to comparable characters in "narrower"
 * encodings -- e.g. from UTF-8 to ascii, or from non-UTF-8 to UTF-8 or smaller. This is useful for
 * some NLP applications that don't handle wide characters or other "non-standard" inputs well (such
 * as the Charniak parser).
 *
 * @author mssammon
 */
public class StringTransformationCleanup {
    private static final String LATIN1 = "ISO-8859-1";

    /**
     * tries to normalize string to specified encoding. The number of characters returned should be
     * the same, and tokens should remain contiguous in the output; non-recognized characters will
     * be substituted for *something*.
     */
    static public StringTransformation normalizeToEncoding(StringTransformation stringTransformation, Charset encoding) {

        String startStr = stringTransformation.getTransformedText();

        CharsetEncoder encoder = encoding.newEncoder();

        if (!encoder.canEncode(startStr)) {
            final int length = startStr.length();

            int charNum = 0;

            for (int offset = 0; offset < length;) {
                // do something with the codepoint
                Pair<Boolean, Integer> replacement =
                        normalizeCharacter(startStr, encoding, offset);

                Character replacedChar = (char) replacement.getSecond().intValue();

                if (null != replacedChar) {

                    stringTransformation.transformString(charNum, charNum+1, String.valueOf(replacedChar));
                    charNum++;
                }
                offset += Character.charCount(replacedChar);
            }
        }

        return stringTransformation;
    }



    /**
     * substitute based on types: for a character encoding not in range, replace punctuation with
     * generic punctuation whitespace with whitespace letter with letter number with number currency
     * symbol with currency
     */
    private static Pair<Boolean, Integer> normalizeCharacter(String origString,
            Charset encoding, int offset) {

        char normalizedChar = ' ';

        final int codepoint = origString.codePointAt(offset);

        boolean isOk = checkIsInEncoding(codepoint, encoding);

        if (isOk) {
            normalizedChar = (char) codepoint;
        } else {
            Pair<Boolean, Character> charInfo = fixCharByType(codepoint);
            normalizedChar = charInfo.getSecond();
            isOk = charInfo.getFirst();
        }

        Character newChar = null;
        if (isOk)
            newChar = normalizedChar;

        return new Pair(isOk, newChar);
    }


    private static boolean checkIsLatin(int codepoint) {
        return checkIsInEncoding(codepoint, Charset.forName(LATIN1));
    }



    private static boolean checkIsInEncoding(int codepoint, Charset encoding) {

        boolean isOk = false;

        if (encoding.equals(Charset.forName("US-ASCII"))) {
            if (codepoint < 128)
                isOk = true;
        } else if (encoding.equals(Charset.forName(LATIN1))) // latin1
        {
            if (codepoint < 256)
                isOk = true;
        } else if (encoding.equals(Charset.forName("UTF-8"))) {
            if (codepoint < 1114111)
                isOk = true;
        }

        return isOk;
    }


    /**
     * Attempt to replace an out-of-charset character with something appropriate, so that the resulting
     *    string a) makes sense and b) resembles the original. Otherwise, use whitespace.
     * @param codepoint codepoint of character to change
     * @return flag indicating whether a substitution was found, and the substituted character.
     */
    public static Pair<Boolean, Character> fixCharByType(int codepoint) {

        final int type = Character.getType(codepoint);

        char normalizedChar = ' ';

        boolean isOk = true;

        if (type == Character.CURRENCY_SYMBOL)
                normalizedChar = '$';
            else if (type == Character.DASH_PUNCTUATION)
                normalizedChar = '-';
            else if (type == Character.FINAL_QUOTE_PUNCTUATION) {
                normalizedChar = '"';
            } else if (type == Character.INITIAL_QUOTE_PUNCTUATION) {
                normalizedChar = '"';
            } else if (type == Character.END_PUNCTUATION)
                normalizedChar = '.';
            else if (type == Character.DASH_PUNCTUATION)
                normalizedChar = '-';
            else if (type == Character.OTHER_LETTER)
                normalizedChar = 'a';
            else if (type == Character.OTHER_NUMBER)
                normalizedChar = '0';
            else if (type == Character.OTHER_PUNCTUATION)
                normalizedChar = '-';
            else if (type == Character.OTHER_SYMBOL)
                normalizedChar = ' ';
            else
                isOk = false;

        return new Pair(isOk, normalizedChar);
    }


    static public StringTransformation removeDiacritics(StringTransformation origStringSt) {
        char[] startChars = origStringSt.getTransformedText().toCharArray();
        for (int i = 0; i < startChars.length; ++i) {
            int c = Character.codePointAt(startChars, i);
            if (checkIsLatin(c))
                continue;
            else {
                String newC = StringUtils.normalizeUnicodeDiacriticChar(startChars[i]);
                origStringSt.transformString(i, i+1, newC);
            }
        }
        origStringSt.getTransformedText(); // applies edits
        return origStringSt;
    }


    static public StringTransformation normalizeToLatin1(StringTransformation origStringSt) {
        return normalizeToEncoding(origStringSt, Charset.forName("ISO-8859-1"));
    }


    static public StringTransformation normalizeToAscii(StringTransformation origStringSt) {
        StringTransformation latin1St = normalizeToLatin1(origStringSt);
        return normalizeToEncoding(latin1St, Charset.forName("ascii"));
    }


}
