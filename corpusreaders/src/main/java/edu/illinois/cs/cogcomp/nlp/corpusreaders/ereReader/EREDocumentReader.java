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

    /**
     * Test here.
     * @param args not used.
     * @throws Exception 
     */
    static public void main (String[] args) throws Exception {
    	String origText = LineIO.slurp("/Volumes/xdata/CCGStuff/LDC2016E31_DEFT_Rich_ERE_English_Training_Annotation_R3/data/source/ENG_DF_000170_20150322_F00000082.xml");
    	EREDocumentReader p = new EREDocumentReader("CleanERE","/Volumes/xdata/CCGStuff/LDC2016E31_DEFT_Rich_ERE_English_Training_Annotation_R3/data/source/");
        ArrayList<String> tagNames = new ArrayList<String>();
        tagNames.add("poogers");
        tagNames.add("dream");
        ArrayList<String> attributeNames = new ArrayList<String>();
        attributeNames.add("johnny");
        attributeNames.add("theater");
        System.out.println(origText);
        String nt = p.stripText(origText);
        System.out.println("\n"+nt);
    }
}
