/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.bigdata.lucene;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.en.EnglishPossessiveFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.util.Version;

import java.io.Reader;

/**
 * Converts Unicode characters to the corresponding ASCII characters
 * as well as deliminating texts on dashes and underscores
 * @author cheng88
 *
 */
public class ASCIIEnglishAnalyzer extends StopwordAnalyzerBase{

	public ASCIIEnglishAnalyzer() {
		super();
	}

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
    	final Tokenizer source = new StandardTokenizer();
    	TokenStream result = new StandardFilter(source);
    	result = new ASCIIFoldingFilter(result);
    	result = new EnglishPossessiveFilter(result);
    	result = new WordDelimiterFilter(result,WordDelimiterFilter.ALPHA,null);
    	result = new LowerCaseFilter(result);
    	result = new StopFilter(result, EnglishAnalyzer.getDefaultStopSet());
    	result = new PorterStemFilter(result);
    	return new TokenStreamComponents(source, result);
    }

}