package edu.illinois.cs.cogcomp.nlp.corpusreaders;

import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.annotation.XmlTextAnnotationMaker;
import edu.illinois.cs.cogcomp.core.constants.Language;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
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
import java.util.*;

/**
 * @author Chase Duncan
 *
 * Reader for TAC KBP EDL 2016 data.
 */
public class TACReader extends XmlDocumentReader {
    private static final String CORPUS_NAME = "TAC";
    private static final String ANNOTATION_EXTENSION = ".tab";
    private static final String[] SOURCE_TYPES = {"nw","df"};

    /* DTD for TAC KBP discussion forum data
        <!ELEMENT  doc         (headline, post+)                     >
        <!ATTLIST  doc         id                 CDATA #REQUIRED    >

        <!ELEMENT  headline    (#PCDATA)                             >

        <!ELEMENT  post        (#PCDATA|quote|a|img|post)*           >
        <!ATTLIST  post        id                 ID    #REQUIRED
                               author             CDATA #IMPLIED
                               datetime           CDATA #IMPLIED
                               datetime_collected CDATA #IMPLIED     >

        <!ELEMENT  quote       (#PCDATA|quote|a|img|post)*           >
        <!ATTLIST  quote       orig_author        CDATA #IMPLIED
                               orig_datetime      CDATA #IMPLIED
                               orig_post          IDREF #IMPLIED     >

        <!ELEMENT  a           (#PCDATA)                             >
        <!ATTLIST  a           href               CDATA #IMPLIED     >

        <!ELEMENT  img         EMPTY                                 >
        <!ATTLIST  img         src                CDATA #IMPLIED
                               alt                CDATA #IMPLIED     >

      DTD for TAC KBP newswire data
        <!ELEMENT DOC (DATE_TIME?, HEADLINE?, DATELINE?, AUTHOR?, TEXT)>
        <!ELEMENT HEADLINE ANY>
        <!ELEMENT DATELINE ANY>
        <!ELEMENT DATE_TIME ANY>
        <!ELEMENT TEXT ANY>
        <!ELEMENT AUTHOR ANY>
        <!ELEMENT P ANY>
        <!ATTLIST DOC id ID #REQUIRED>
        <!ATTLIST DOC type CDATA #IMPLIED>
        <!ATTLIST DOC from_file CDATA #IMPLIED>
     */

    /**
     * tags in document files
     */
    private static final String DOC = "doc";
    private static final String HEADLINE = "headline";
    private static final String POST = "post";
    private static final String QUOTE = "quote";
    private static final String A = "a";
    private static final String IMG = "img";

    private static final String ID = "id";
    private static final String AUTHOR = "author";
    private static final String DATETIME = "datetime";
    private static final String DATETIME_COLLECTED = "datetime_collected";
    private static final String ORIG_AUTHOR = "orig_author";
    private static final String ORIG_DATETIME = "orig_datetime";
    private static final String ORIG_POST = "orig_post";
    private static final String SRC = "src";
    private static final String ALT = "alt";
    private static final String FROM_FILE = "from_file";
    private static final String TYPE = "type";
    private static final String HREF = "href";

    private static final String P = "p";

    /*
        Tags to ignore. These were accumulated by looking at thrown excpetions while trying to process the corpus.
     */
    private static final String ACK = "ack";
    private static final String THREE_D = "3d";

    private static final String NAME = XmlDocumentReader.class.getCanonicalName();
    private static Logger logger = LoggerFactory.getLogger(TACReader.class);

    /**
     * tag sets for xml processor for TAC KBP documents
     */
    public final Map<String, Set<String>> tagsWithAtts = new HashMap<>();
    public final Set<String> deletableSpanTags = new HashSet<>();
    public final Set<String> tagsToIgnore = new HashSet<>();

    /*
     * Builds a TAC reader for English
     */
    public TACReader(String corpusRoot, boolean throwExceptionOnXmlParseFail) throws Exception {
        super(TACReader.buildTACConfig(corpusRoot, Language.English),
                buildXmlTextAnnotationMaker(new TokenizerTextAnnotationBuilder(new StatefulTokenizer()),
                throwExceptionOnXmlParseFail));
    }

    /*
     * General constructor for any of the 3 TAC languages
     */
    public TACReader(String corpusRoot,
                     TextAnnotationBuilder textAnnotationBuilder,
                     Language language,
                     boolean throwExceptionOnXmlParseFail) throws Exception {
        super(TACReader.buildTACConfig(corpusRoot, language),
                buildXmlTextAnnotationMaker(textAnnotationBuilder, throwExceptionOnXmlParseFail));
    }

    /*
     * This method handles the configuration of the XmlTextAnnotationMaker
     * based on the DTDs (in the comments above) of the TAC KBP data.
     *
     * @param textAnnotationBuilder TA builder for target language
     * @param throwExceptionOnXmlParseFail
     */
    private static XmlTextAnnotationMaker buildXmlTextAnnotationMaker(TextAnnotationBuilder textAnnotationBuilder,
                                                                      boolean throwExceptionOnXmlParseFail) {


        Map<String, Set<String>> tagsWithAtts = new HashMap<>();

        // initialize tags for discussion forum docs
        Set<String> attributeNames = new HashSet<>();
        attributeNames.add(ID);
        attributeNames.add(TYPE);
        attributeNames.add(FROM_FILE);

        tagsWithAtts.put(DOC, attributeNames);

        attributeNames = new HashSet<>();
        attributeNames.add(ID);
        attributeNames.add(AUTHOR);
        attributeNames.add(DATETIME);
        attributeNames.add(DATETIME_COLLECTED);

        tagsWithAtts.put(POST, attributeNames);

        attributeNames = new HashSet<>();
        attributeNames.add(ORIG_AUTHOR);
        attributeNames.add(ORIG_DATETIME);
        attributeNames.add(ORIG_POST);

        tagsWithAtts.put(QUOTE, attributeNames);

        attributeNames = new HashSet<>();
        attributeNames.add(HREF);

        tagsWithAtts.put(A, attributeNames);

        attributeNames = new HashSet<>();
        attributeNames.add(SRC);
        attributeNames.add(ALT);

        tagsWithAtts.put(IMG, attributeNames);

        attributeNames = new HashSet<>();
        attributeNames.add(ID);

        tagsWithAtts.put(DOC, attributeNames);

        Set<String> deletableSpanTags = new HashSet<>();

        Set<String> tagsToIgnore = new HashSet<>(); // implies "delete spans enclosed by these tags"
        tagsToIgnore.add(ACK);
        tagsToIgnore.add(THREE_D);

        XmlDocumentProcessor xmlProcessor =
                new XmlDocumentProcessor(deletableSpanTags, tagsWithAtts, tagsToIgnore, throwExceptionOnXmlParseFail);
        return new XmlTextAnnotationMaker(textAnnotationBuilder, xmlProcessor);
    }

    /**
     * This method sets basic configuration parameters.
     *
     * @param corpusRoot the root directory of the corpus on your file system
     * @param language   language of data to be read, i.e. ENG, SPA, CMN
     * @return a ResourceManager with the appropriate configuration,
     */
    private static ResourceManager buildTACConfig(String corpusRoot, Language language) {
        String langSubDir;
        if (language == Language.Chinese)
            langSubDir = "cmn";
        else
            langSubDir = language.getCode();

        String sourceDir = corpusRoot + langSubDir;
        String sourceExtension = ".xml";

        Properties props = new Properties();
        //set source, annotation directories relative to specified corpus root dir
        props.setProperty(CorpusReaderConfigurator.SOURCE_DIRECTORY.key, sourceDir);
        props.setProperty(CorpusReaderConfigurator.SOURCE_EXTENSION.key, sourceExtension);
        props.setProperty(CorpusReaderConfigurator.CORPUS_NAME.key, CORPUS_NAME);
        props.setProperty(CorpusReaderConfigurator.ANNOTATION_EXTENSION.key, ANNOTATION_EXTENSION);
        return new ResourceManager(props);
    }

    /**
     * This is overridden to handle the multiple subdirectories of the TAC KBP data.
     *
     * @return a list of lists of paths: each element is a singleton list containing a TAC source file
     * @throws IOException if the paths are not specified correctly, causing failure to read
     *                     files expected to be present
     */
    @Override
    public List<List<Path>> getFileListing() throws IOException {
        String sourceDir = resourceManager.getString(CorpusReaderConfigurator.SOURCE_DIRECTORY);

        List<List<Path>> corpusPaths = new ArrayList<>();
        for (String st : SOURCE_TYPES) {

            String dir = sourceDir + "/" + st;
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(getRequiredSourceFileExtension());
                }
            };
            String[] fileList = IOUtils.lsFilesRecursive(dir, filter);
            List<List<Path>> pathList = new ArrayList<>(fileList.length);
            for (String file : fileList)
                pathList.add(Collections.singletonList(Paths.get(file)));
            corpusPaths.addAll(pathList);
        }
        return corpusPaths;
    }
}
