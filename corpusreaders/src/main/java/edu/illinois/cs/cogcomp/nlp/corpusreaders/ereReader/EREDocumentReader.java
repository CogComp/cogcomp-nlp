/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader;

import java.util.ArrayList;

import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.core.utilities.TextCleaner;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.XmlFragmentWhitespacingDocumentReader;

/**
 * Strips all useless XML markup from an ERE xml document leaving the original text and 
 * where needed, appropriate attribute values.
 * @author redman
 *
 */
public class EREDocumentReader extends XmlFragmentWhitespacingDocumentReader {
	
	/** these tags contain attributes we want to keep. */
    static private ArrayList<String> retainTags = new ArrayList<String>();
    static {
    	retainTags.add("quote");
    	retainTags.add("post");
    }
    
    /** the attributes to keep for the above tags. */
    static private ArrayList<String> retainAttributes = new ArrayList<String>();
    {
    	retainAttributes.add("orig_author");
    	retainAttributes.add("author");
    }
	/**
	 * @param corpusName the name of the corpus, this can be anything.
	 * @param sourceDirectory the name of the directory containing the file.
	 * @throws Exception
	 */
	public EREDocumentReader(String corpusName, String sourceDirectory) throws Exception {
		super(corpusName, sourceDirectory);
	}
	
    /**
     * Strip all XML markup, but leave all the text content, and possibly some attributes
     * for certain tags, while retaining the same offset for all remaining text content.
     * @param original the original text string.
     * @return the striped text.
     */
    protected String stripText(String original) {
    	return TextCleaner.removeXmlLeaveAttributes(original, retainTags, retainAttributes);
    }
    
    /**
     * Exclude any files not possessing this extension.
     * @return the required file extension.
     */
    protected String getRequiredFileExtension() {
        return ".xml";
    }
}
