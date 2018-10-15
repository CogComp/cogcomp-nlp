/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.bigdata.lucene;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.util.CharTokenizer;
import org.apache.lucene.util.Version;

public class CharacterShingleAnalyzer extends Analyzer{

    private static Version matchVersion = Version.LUCENE_6_0_0;

    public static class CharacterShingleTokenizer extends CharTokenizer{

        public CharacterShingleTokenizer() {
            super();
        }

        @Override
        protected boolean isTokenChar(int arg0) {
            return true;
        }

    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        final Tokenizer source = new CharacterShingleTokenizer();
        TokenStream result = new StandardFilter(source);
        result = new ASCIIFoldingFilter(result);
        result = new LowerCaseFilter(result);
        result = new ShingleFilter(result, 3);

//        result = new WordDelimiterFilter(result, WordDelimiterFilter.DIGIT, null);

        return new TokenStreamComponents(source, result);
    }



}
