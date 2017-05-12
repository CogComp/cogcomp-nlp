package edu.illinois.cs.cogcomp.nlp.corpusreaders.ontonotes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.illinois.cs.cogcomp.annotation.XmlTextAnnotationMaker;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.XmlTextAnnotation;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.core.utilities.XmlDocumentProcessor;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ontonotes.utils.DocumentIterator;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ontonotes.utils.DocumentIterator.Language;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;

/**
 * convert the Ontonotes 5.0 format to CoNLL 2003 column format. Input arguments are
 * required, the database url, the account name, the password, and the language, and
 * finally the output file name.
 * @author redman
 */
public class ConvertOntonotesToColumn {
    /**
     * a structure to store span information: label, offsets, attributes (including value offsets)
     */
    static class SpanInfo {

        /** the label of the named entity. */
        public final String label;
        
        /** the start and end of the span. */
        public final IntPair spanOffsets;
        
        /** the attributes taken from the xml tag. */
        public final Map<String, Pair<String, IntPair>> attributes;

        /**
         * need the label, start and end, and the attributes.
         * @param label the named entity label.
         * @param spanOffsets the offsets.
         * @param attributes the xml tag attributes.
         */
        public SpanInfo(String label, IntPair spanOffsets, Map<String, Pair<String, IntPair>> attributes ) {
            this.label = label;
            this.spanOffsets = spanOffsets;
            this.attributes = attributes;
        }
    }

    static public void main2(String[] args) throws FileNotFoundException {
        
        String inFile = "/shared/corpora/corporaWeb/multi-mode/multi/ontonotes-release-5.0/data/files/data/english/annotations/nw/wsj/00/wsj_0061.name";
        // make sure the output directory exists.
 
        // "en"
        int counter = 0;
        long start = System.currentTimeMillis();
 
        // define all tags with text.
        Set<String> tagsWithText = new HashSet<>();
 
        // define the attributes we want to keep for the tags we have.
        Map<String, Set<String>> tagsWithAtts = new HashMap<>();
        {
            Set<String> docAttrs = new HashSet<>();
            docAttrs.add("docno");
            tagsWithAtts.put("doc", docAttrs);
        }
        {
            Set<String> nameAttrs = new HashSet<>();
            nameAttrs.add("type");
            tagsWithAtts.put("enamex", nameAttrs);
        }
 
        boolean throwExceptionOnXmlParseFail = true;
        // we keep everything.
        Set<String> dropTags = new HashSet<>();
        XmlDocumentProcessor xmlProcessor = new XmlDocumentProcessor(tagsWithText, tagsWithAtts, dropTags);
        StatefulTokenizer st = new StatefulTokenizer();
        TokenizerTextAnnotationBuilder taBuilder = new TokenizerTextAnnotationBuilder(st);
        XmlTextAnnotationMaker xtam = new XmlTextAnnotationMaker(taBuilder, xmlProcessor);
 
        String document = LineIO.slurp(inFile);
        XmlTextAnnotation xta = xtam.createTextAnnotation(document, "OntoNotes 5.0", "test");
        TextAnnotation ta = xta.getTextAnnotation();
        List<SpanInfo> fudge = getSpanInfos(xta);
        //List<XmlDocumentProcessor.SpanInfo> fudge = xta.getXmlMarkup();
        System.out.println(ta + "\n");
 
        View nerView = new SpanLabelView(ViewNames.NER_ONTONOTES, ta);
        String cleanText = ta.getText();
        for (SpanInfo si : fudge) {
            if ("enamex".equalsIgnoreCase(si.label)) {
 
                IntPair charOffsets = si.spanOffsets;
                String neLabel = si.attributes.get("type").getFirst();
                int cleanTextCharStart = xta.getXmlSt().computeModifiedOffsetFromOriginal(charOffsets.getFirst());
                int cleanTextCharEnd = xta.getXmlSt().computeModifiedOffsetFromOriginal(charOffsets.getSecond());
                System.err.println("ne string: '" + cleanText.substring(cleanTextCharStart, cleanTextCharEnd) + "'");
                int cleanTextNeTokStart = ta.getTokenIdFromCharacterOffset(cleanTextCharStart);
                int cleanTextNeTokEnd = ta.getTokenIdFromCharacterOffset(cleanTextCharEnd-1); // StringTransformation returns one-past-the-end index; TextAnnotation maps at-the-end index
                Constituent neCon = new Constituent(neLabel, nerView.getViewName(), ta, cleanTextNeTokStart, cleanTextNeTokEnd + 1); //constituent token indexing uses one-past-the-end
                nerView.addConstituent(neCon);
            }
            counter++;
            System.out.println("Read " + counter + " documents in " + (System.currentTimeMillis() - start));
            System.out.println(nerView.toString());
        }
    }
    
	private static List<SpanInfo> getSpanInfos(XmlTextAnnotation xta) {
	    ArrayList<SpanInfo> spans = new ArrayList<>();
        Map<IntPair, Map<String, String>> fudge = xta.getXmlMarkup();
        for (Entry<IntPair, Map<String, String>> entry : fudge.entrySet()) {
            IntPair loc = entry.getKey();
            Map<String, String> attributes = entry.getValue();
            SpanInfo si = new SpanInfo(attributes.get("type"), )
        }
        return null;
    }
    /**
	 * This is used primarily for quick testing. The bulk of this should be used as a
	 * starting point for a unit test, question is can we include some of the files for
	 * the test in the code repository?
	 * @param args
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws IOException 
	 */
	static public void main(String[] args) throws IOException {
		if (args.length < 4) {
			System.err.println("This executable requires four arguments:\n"
					+ " ConvertOntonotesToColumn <OntoNotes Directory> <language>"
					+ " <file_kind> <output_directory>");
			System.exit(-1);
		}
		String topdir = args[0];
		Language language = null;
        try {
            language = DocumentIterator.Language.valueOf(args[1]);
        } catch (IllegalArgumentException iae) {
            System.out.print("language must be one of : ");
            for (DocumentIterator.Language l : DocumentIterator.Language.values())
                System.out.print(l.toString()+" ");
            System.out.println();
            System.exit(-1);
        }

		DocumentIterator.FileKind kind = null;
        try {
            kind = DocumentIterator.FileKind.valueOf(args[2]);
        } catch (IllegalArgumentException iae) {
            System.out.print("file_kind must be one of : ");
            for (DocumentIterator.FileKind l : DocumentIterator.FileKind.values())
                System.out.print(l.toString()+" ");
            System.out.println();
            System.exit(-1);
        }
		String outputdir = args[3];
		
		// make sure the output directory exists.
		File f = new File(outputdir);
		if (f.exists()) {
			if (!f.isDirectory()) {
				System.err.println("The output directory must specify a directory:\n"
				                + " ConvertOntonotesToColumn <OntoNotes Directory> <language>"
			                    + " <file_kind> <output_directory>");
			}
		} else {
			f.mkdirs();
		}
		
		// "en"
		DocumentIterator di = new DocumentIterator(topdir, language, kind);
		int counter = 0;
		long start = System.currentTimeMillis();
		
		// define all tags with text.
		Set <String> tagsWithText = new HashSet<>();
        
        // define the attributes we want to keep for the tags we have.
        Map<String, Set<String>> tagsWithAtts = new HashMap<>();
        {
            Set <String> docAttrs = new HashSet<>();
            docAttrs.add("docno");
            tagsWithAtts.put("doc", docAttrs);
        }
        {
            Set <String> nameAttrs = new HashSet<>();
            nameAttrs.add("type");
            tagsWithAtts.put("enamex", nameAttrs);
        }
        
        // we keep everything.
        Set <String> dropTags = new HashSet<>();
		XmlDocumentProcessor xmlProcessor = new XmlDocumentProcessor(tagsWithText, tagsWithAtts, dropTags);
		StatefulTokenizer st = new StatefulTokenizer();
		TokenizerTextAnnotationBuilder taBuilder = new TokenizerTextAnnotationBuilder(st);
		XmlTextAnnotationMaker xtam = new XmlTextAnnotationMaker(taBuilder, xmlProcessor);
		while (di.hasNext()) {
		    String docId = di.documentId();
			String document = di.next();
			XmlTextAnnotation xta = xtam.createTextAnnotation(document, "OntoNotes 5.0", docId);
			TextAnnotation ta = xta.getTextAnnotation();
			Map<IntPair, Map<String, String>> fudge = xta.getXmlMarkup();
            System.out.println(ta+"\n");
			System.out.println(fudge);
			counter++;
		}
		long end = System.currentTimeMillis();
		System.out.println("Read "+counter+" documents in "+(System.currentTimeMillis()-start));
	}
}