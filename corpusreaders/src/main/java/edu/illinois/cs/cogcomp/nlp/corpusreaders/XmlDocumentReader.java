/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders;

import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.annotation.XmlTextAnnotationMaker;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.XmlTextAnnotation;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.core.utilities.StringTransformation;
import edu.illinois.cs.cogcomp.core.utilities.TextCleaner;
import edu.illinois.cs.cogcomp.core.utilities.XmlDocumentProcessor;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Generates an {@link XmlTextAnnotation} object per file for a corpus consisting of files containing xml
 * fragments or full xml trees. This implementation has been created for the DEFT ERE collection, but should
 * generalize to other tasks by substituting an appropriately parameterized XmlDocumentReader.
 * The ERE documents appear to be forum data, not full xml, but xml-ish. LDC README IN LDC2016E27 INDICATES
 * THAT THESE DOCUMENTS ARE XML FRAGMENTS, NOT FULL XML. Therefore, they should be treated as raw
 * text, even though they contain xml-escaped character forms: character offsets for standoff
 * annotation will refer to these expanded forms. This reader generates a cleaned-up, text-only
 * version of the document and also retrieves information from the xml markup. The
 * {@link edu.illinois.cs.cogcomp.core.utilities.StringTransformation} that accompanies it maps the
 * cleaned-up text offsets back to the original xml file. The xml markup offsets correspond to the
 * original xml document.
 *
 * The Xml document structure consists of one or more "post" elements, each possibly containing one or
 * more "quote" elements (which may be nested) and which may have other tags (image files and other url-like
 * stuff, possibly html formatting), though these will generally be escaped. This reader handles
 * these problems, internally normalizing these escaped tags and treating them like regular xml elements.
 *
 * The XmlTextAnnotations will be returned with TextID fields set to the name of the source file.
 *
 * While no effort is made to represent the inter-post/quoted segment structure, the xml markup information
 * allows it to be reconstructed (look for entries with key SPAN_INFO, whose value set will contain one entry
 * naming the xml tag.
 *
 * When accessing the TextAnnotation element of the XmlTextAnnotation, be aware that you must use the
 * accompanying StringTransformation to recover the offsets from the source xml file.
 */
public class XmlDocumentReader extends AbstractIncrementalCorpusReader<XmlTextAnnotation> {
    private static Logger logger = LoggerFactory
            .getLogger(XmlDocumentReader.class);
    private final XmlTextAnnotationMaker xmlTextAnnotationMaker;

    protected String fileId;
    private int numTextAnnotations;
    private int numFiles;
    private String sourceFileExtension;
    private String annotationFileExtension;


    /**
     * Instantiate a reader for an xml corpus.  Default implementation assumes a single source corpus from which
     *    user wants to strip xml markup, but record relevant xml markup info.
     *
     * The {@link XmlTextAnnotationMaker} should be configured to
     *   process the xml markup in the files you want to process.
     *
     * @param rm resourceManager with configuration specs (source and annotation directories, file extensions, etc.)
     * @param xmlTextAnnotationMaker parses xml text and generates an XmlTextAnnotation.
     * @throws IOException
     */
    public XmlDocumentReader(ResourceManager rm, XmlTextAnnotationMaker xmlTextAnnotationMaker)
            throws Exception {
        super(rm);
        this.xmlTextAnnotationMaker = xmlTextAnnotationMaker;
    }


    /**
     * this method is called by the base class constructor, so all subclass-specific object
     * initialization must be done here.
     *
     * This default implementation assumes that annotation and source are both provided in the same file.
     */
    @Override
    protected void initializeReader() {
        this.sourceDirectory = resourceManager.getString(CorpusReaderConfigurator.SOURCE_DIRECTORY);
        this.annotationFileExtension = resourceManager.getString(CorpusReaderConfigurator.ANNOTATION_EXTENSION);
        this.sourceFileExtension = resourceManager.getString(CorpusReaderConfigurator.SOURCE_EXTENSION);

        try {
            fileList = getFileListing();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException(e.getMessage());
        }
        numFiles = 0;
        numTextAnnotations = 0;
    }


    /**
     * set the reader to start from the beginning of the corpus.
     */
    @Override
    public void reset() {
        super.reset();
        numFiles = 0;
        numTextAnnotations = 0;
    }


    /**
     * generate a list of lists of files comprising the corpus. Each entry is expected to generate one or more
     * TextAnnotation objects, though the way the iterator is implemented allows for corpus files to
     * generate zero TextAnnotations if you are feeling picky. Each entry in the list is itself a list in
     * which the first file contains the source document. If that file does not also contain the annotation
     * info, the remaining entries in the list name the file(s) containing the annotation markup.
     *
     * The default implementation assumes only a single self-contained file is provided for each document.
     *
     * @return a List of Lists of Path objects, each containing a source file and corresponding markup files.
     */
    @Override
    public List<List<Path>> getFileListing() throws IOException {
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(getRequiredSourceFileExtension());
            }
        };
        String[] fileList = IOUtils.lsFilesRecursive(super.getSourceDirectory(), filter);
        List<List<Path>> pathList = new ArrayList<>(fileList.length);
        for (String file : fileList)
            pathList.add(Collections.singletonList(Paths.get(file)));

        return pathList;
    }

    /**
     * given an entry from the corpus file list generated by {@link #getFileListing()} , parse its
     * contents and get zero or more TextAnnotation objects. This allows for the case where corpus
     * annotations are provided in standoff format in one or more files separate from the source
     * document.  In such cases, the first file in the list should contain the source document
     * and the rest should be the corresponding markup files.
     *
     * In this default implementation, it is assumed that a single file contains both source and markup.
     *
     * @param corpusFileListEntry a list of files, the first of which is a source file.
     * @return List of TextAnnotation objects extracted from the corpus file.
     */
    @Override
    public List<XmlTextAnnotation> getAnnotationsFromFile(List<Path> corpusFileListEntry) throws Exception {
        Path sourceTextAndAnnotationFile = corpusFileListEntry.get(0);
        fileId =
                sourceTextAndAnnotationFile.getName(sourceTextAndAnnotationFile.getNameCount() - 1)
                        .toString();
        logger.debug("read source file {}", fileId);
        numFiles++;
        String fileText = LineIO.slurp(sourceTextAndAnnotationFile.toString());
        List<XmlTextAnnotation> xmlTaList = new ArrayList<>(1);
        XmlTextAnnotation xmlTa = xmlTextAnnotationMaker.createTextAnnotation(fileText, this.corpusName, fileId);
        if (null != xmlTa) {
            xmlTaList.add(xmlTa);
            numTextAnnotations++;
        }

        return xmlTaList;
    }


    /**
     * generate a human-readable report of annotations read from the source file (plus whatever
     * other relevant statistics the user should know about).
     */
    @Override
    public String generateReport() {
        StringBuilder bldr = new StringBuilder();
        bldr.append("Number of files read: ").append(numFiles).append(System.lineSeparator());
        bldr.append("Number of TextAnnotations generated: ").append(numTextAnnotations)
                .append(System.lineSeparator());
        return bldr.toString();
    }


    /**
     * Exclude any files not possessing this extension.
     * @return the required file extension.
     */
    protected String getRequiredSourceFileExtension() {
        return sourceFileExtension;
    }


    /**
     * Exclude any files not possessing this extension.
     * @return the required file extension.
     */
    protected String getRequiredAnnotationFileExtension() {
        return annotationFileExtension;
    }


}
