/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.core.utilities.TextCleaner;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;

/**
 * Generates a TextAnnotation object per file for a corpus consisting of files containing xml
 * fragments. Created for the DEFT ERE collection for Belief and Sentiment task (LDC2016E27). All
 * documents appear to be forum data, not full xml, but xml-ish. LDC README IN LDC2016E27 INDICATES
 * THAT THESE DOCUMENTS ARE XML FRAGMENTS, NOT FULL XML. Therefore, they should be treated as raw
 * text, even though they contain xml-escaped character forms: character offsets for standoff
 * annotation will refer to these expanded forms.
 *
 * Structure consists of one or more "post" elements, each possibly containing one or more "quote"
 * elements (which may be nested) and which may have other tags (image files and other url-like
 * stuff, possibly html formatting).
 *
 * This reader (initial implementation) tries to clean up text as much as possible while preserving
 * character offsets of the original text. This is achieved by whitespacing the xml/other tags; the
 * Illinois Tokenizer should be able to handle this in an offset-preserving way.
 *
 * The TextAnnotations will be returned with TextID fields set to the name of the source file.
 *
 * WARNING! No effort is made to represent the inter-post/quoted segment structure.
 *
 * When trying to align annotations to the original file, beware the following annotation property
 * (explained in the README from the corpus:
 *
 * <quote>Because each CMP document is extracted verbatim from source XML files, certain characters
 * in its content (ampersands, angle brackets, etc.) are escaped according to the XML specification.
 * The offsets of text extents are based on treating this escaped text as-is (e.g. "&amp;" in a
 * cmp.txt file is counted as five characters).
 * 
 * Whenever any such string of "raw" text is included in a .rich_ere.xml file (as the text extent to
 * which an annotation is applied), a second level of escaping has been applied, so that XML parsing
 * of the ERE XML file will produce a string that exactly matches the source text. </quote>
 */
public class XmlFragmentWhitespacingDocumentReader extends AbstractIncrementalCorpusReader {
    protected TextAnnotationBuilder taBuilder;
    protected String fileId;
    protected String newFileText;

    /**
     * assumes files are all from a single source directory.
     *
     * @param corpusName
     * @param sourceDirectory
     * @throws IOException
     */
    public XmlFragmentWhitespacingDocumentReader(String corpusName, String sourceDirectory)
            throws Exception {
        super(CorpusReaderConfigurator.buildResourceManager(corpusName, sourceDirectory));
        taBuilder = new TokenizerTextAnnotationBuilder(new StatefulTokenizer());
    }

    /**
     * Exclude any files not possessing this extension.
     * @return the required file extension.
     */
    protected String getRequiredFileExtension() {
        return ".cmp.txt";
    }
    
    /**
     * generate a list of files comprising the corpus. Each is expected to generate one or more
     * TextAnnotation objects, though the way the iterator is implemented allows for corpus files to
     * generate zero TextAnnotations if you are feeling picky.
     *
     * @return a list of Path objects corresponding to files containing corpus documents to process.
     */
    @Override
    public List<Path> getFileListing() throws IOException {
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(getRequiredFileExtension());
            }
        };
        String[] fileList = IOUtils.lsFilesRecursive(super.getSourceDirectory(), filter);
        List<Path> pathList = new ArrayList<>(fileList.length);
        for (String file : fileList)
            pathList.add(Paths.get(file));

        return pathList;
    }
    
    /**
     * This method can be overridden to do a more complex parsing.
     * @param original
     * @return the striped text.
     */
    protected String stripText(String original) {
    	return TextCleaner.replaceXmlTags(original);
    }
    
    /**
     * Given an entry from the corpus file list generated by {@link #getFileListing()} , parse its
     * contents and get zero or more TextAnnotation objects.
     *
     * For EreDocumentReader, there should always be exactly one TextAnnotation per file.
     *
     * @param corpusFileListEntry corpus file containing content to be processed
     * @return List of TextAnnotation objects extracted from the corpus file
     */
    public List<TextAnnotation> getTextAnnotationsFromFile(Path corpusFileListEntry)
            throws Exception {
        fileId = corpusFileListEntry.getName(corpusFileListEntry.getNameCount() - 1).toString();
        String fileText = LineIO.slurp(corpusFileListEntry.toString());
        newFileText = this.stripText(fileText);


        TextAnnotation ta = makeTextAnnotation();

        List<TextAnnotation> taList = new ArrayList<>(1);
        taList.add(ta);

        return taList;
    }

    @Override
    protected TextAnnotation makeTextAnnotation() throws Exception {
        return taBuilder.createTextAnnotation(fileId, "1", newFileText.toString());
    }
}
