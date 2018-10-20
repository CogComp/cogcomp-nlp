/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.bigdata.lucene;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.util.Version;

/**
 * Only use this class for short strings
 *
 * @author cheng88
 *
 */
public class WikiURLAnalyzer extends Analyzer {

    private static Version matchVersion = Version.LUCENE_6_0_0;
    public static final String normalizeChars = "- ";
    public static final String replacement = "_";

    // Only lower case and remove hyphens
    public static class URLField extends Field{
        /** Indexed, not tokenized, omits norms, indexes
         *  DOCS_ONLY, stored */
        public static final FieldType TYPE_STORED = new FieldType();

        static {
          TYPE_STORED.setOmitNorms(true);
          TYPE_STORED.setIndexOptions(IndexOptions.DOCS);
          TYPE_STORED.setStored(true);
          TYPE_STORED.setTokenized(true);
          TYPE_STORED.freeze();
        }

        /** Creates a new URLField.
         *  @param name field name
         *  @param value String value
         *  @throws IllegalArgumentException if the field name or value is null.
         */
        public URLField(String name, String value) {
          super(name, value, TYPE_STORED );
        }
    }



    @Override
    protected TokenStreamComponents createComponents(final String fieldName) {

        final Tokenizer source = new KeywordTokenizer();
        TokenStream result = new StandardFilter(source);
        result = new CharacterFilter(result);
        result = new ASCIIFoldingFilter(result);
        result = new LowerCaseFilter(result);

//        result = new WordDelimiterFilter(result, WordDelimiterFilter.DIGIT, null);

        return new TokenStreamComponents(source, result);
    }


    public static List<String> pruningTokenization(String s){
        List<String> tokens = new ArrayList<String>();
        String[] parts = StringUtils.split(s,replacement.charAt(0));

        for(String part:parts){
            if(!part.endsWith("."))
                tokens.add(part);
        }
        return tokens;
    }

    /**
     * Filters and replaces certain characters from the token stream
     * @author cheng88
     *
     */
    private static class CharacterFilter extends TokenFilter {

        private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

        private static final int UNDERSCORE = replacement.codePointAt(0);

        /**
         * Filters certain characters
         *
         * @param in
         *            TokenStream to filter
         */
        public CharacterFilter(TokenStream in) {
            super(in);
        }

        @Override
        public final boolean incrementToken() throws IOException {
            if (input.incrementToken()) {
                filterChar(termAtt.buffer(), 0, termAtt.length());
                return true;
            } else
                return false;
        }

        public static void filterChar(final char[] buffer, final int offset, final int limit) {
            assert buffer.length >= limit;
            assert offset <= 0 && offset <= buffer.length;
            for (int i = offset; i < limit;) {
                int unicodeChar = Character.codePointAt(buffer, i);

                char[] currentChars = Character.toChars(unicodeChar);

                if(currentChars.length == 1 && normalizeChars.indexOf(currentChars[0])>=0)
                    i += Character.toChars(UNDERSCORE , buffer, i);
                else
                    i += Character.toChars(unicodeChar , buffer, i);
            }
        }
    }

}
