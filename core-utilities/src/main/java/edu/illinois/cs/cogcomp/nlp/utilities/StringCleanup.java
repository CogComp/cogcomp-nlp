/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.utilities;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.utilities.StringUtils;

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


public class StringCleanup {
    /**
     * tries to normalize string to specified encoding. The number of characters returned should be
     * the same, and tokens should remain contiguous in the output; non-recognized characters will
     * be substituted for *something*.
     */

    static public String normalizeToEncoding(String origString_, Charset encoding_) {
        String normString = origString_;

        CharsetEncoder encoder = encoding_.newEncoder();

        if (!encoder.canEncode(origString_)) {
            final int length = origString_.length();
            char[] normSeq = new char[(origString_.length())];

            int charNum = 0;

            for (int offset = 0; offset < length;) {
                // do something with the codepoint
                Pair<Character, Integer> replacement =
                        normalizeCodepoint(origString_, encoding_, offset);

                Character replacedChar = replacement.getFirst();
                int codepoint = replacement.getSecond();

                if (null != replacedChar) {
                    normSeq[charNum] = replacedChar;
                    charNum++;
                }

                offset += Character.charCount(codepoint);
            }
            normString = new String(normSeq);
        }

        return normString;
    }



    /**
     * substitute based on types: for a character encoding not in range, replace punctuation with
     * generic punctuation whitespace with whitespace letter with letter number with number currency
     * symbol with currency
     */

    private static Pair<Character, Integer> normalizeCodepoint(String origString_,
            Charset encoding_, int offset_) {
        char normalizedChar = '?';
        boolean isOk = false;
        final int codepoint = origString_.codePointAt(offset_);

        // char nextChar = ' ';
        // char nextNextChar = ' ';
        //
        // if ( offset_ + 1 < origString_.length() )
        // nextChar = origString_.charAt( offset_ + 1 );
        // if ( offset_ + 2 < origString_.length() )
        // nextNextChar = origString_.charAt( offset_ + 2 );

        // CharsetDecoder decoder = encoding_.newDecoder();
        // System.err.println( "## codepoint is '" + codepoint_ + "'..." );

        if (encoding_.equals(Charset.forName("US-ASCII"))) {
            if (codepoint < 128)
                isOk = true;
        } else if (encoding_.equals(Charset.forName("ISO-8859-1"))) // latin1
        {
            if (codepoint < 256)
                isOk = true;
        } else if (encoding_.equals(Charset.forName("UTF-8"))) {
            if (codepoint < 1114111)
                isOk = true;
        }

        if (isOk) {
            normalizedChar = (char) codepoint;
        } else {
            isOk = true;
            final int type = Character.getType(codepoint);

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
        }

        Character newChar = null;
        if (isOk)
            newChar = normalizedChar;

        return new Pair<>(newChar, codepoint);
    }



    static public String normalizeToUtf8(String origString_) {
        String utf8Str = normalizeToEncoding(origString_, Charset.forName("UTF-8"));
        return StringUtils.normalizeUnicodeDiacritics(utf8Str);
    }



    static public String normalizeToLatin1(String origString_) {
        String noDiacriticStr = normalizeToUtf8(origString_);
        return normalizeToEncoding(noDiacriticStr, Charset.forName("ISO-8859-1"));
    }



    static public String normalizeToAscii(String origString_) {
        String latin1Str = normalizeToLatin1(origString_);
        return normalizeToEncoding(latin1Str, Charset.forName("ascii"));
    }


    /*
     * Control Characters such as ^C, ^\, ^M etc. are a part of the C0 ASCII Control Character Set.
     * These break our NER, COREF etc. (Eg. In John Smith Corpus) and are not needed in text
     * documents. This function removes the control characters in input string, i.e. almost all of
     * ASCII characters 0 - 31, except the \n, \r and \t characters (which it keeps intact in the
     * text).
     */
    static public String removeControlCharacters(String origString_) {
        return origString_.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");
    }

}
