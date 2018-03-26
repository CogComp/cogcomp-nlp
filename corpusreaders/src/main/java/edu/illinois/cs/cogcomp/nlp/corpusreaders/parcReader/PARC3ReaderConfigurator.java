package edu.illinois.cs.cogcomp.nlp.corpusreaders.parcReader;

import edu.illinois.cs.cogcomp.core.utilities.AvoidUsing;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Property;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.CorpusReaderConfigurator;

import java.util.Properties;

/**

 * Configurations for PARC3 Reader

 * @author Sihao Chen

 */
public class PARC3ReaderConfigurator extends CorpusReaderConfigurator {

    public static final String DEFAULT_CORPUS_NAME = "PARC3";

    public static final Property POPULATE_POS = new Property("populatePOS", FALSE);
    public static final Property POPULATE_LEMMA = new Property("populateLEMMA", FALSE);
    public static final Property POPULATE_ATTRIBUTION = new Property("populateAttribution", TRUE);

    /**
     * Dummy, never use this.
     * @return a broken ResourceManager in the wind
     */
    @AvoidUsing(reason="Please use getDefaultConfigWithSourceDir() to set source directory for corpus")
    @Override
    public ResourceManager getDefaultConfig() {
        Property[] props = new Property[] {
                POPULATE_POS,
                POPULATE_LEMMA,
                POPULATE_ATTRIBUTION,
                new Property(CorpusReaderConfigurator.CORPUS_NAME.key, DEFAULT_CORPUS_NAME),
                SOURCE_DIRECTORY,
                SOURCE_EXTENSION,
                new Property(CorpusReaderConfigurator.SUPPRESS_FILE_ERRORS.key, TRUE),
        };

        return new ResourceManager(generateProperties(props));
    }

    /**
     * Get default configuration with user specified source directory to PARC corpus
     * By default, POS and lemma from original document are not populated into the new TextAnnotation.
     * The generated TextAnnotation will only keep tokenization and sentence split from document.
     *
     * @param sourceDir source directory of corpus
     * @return a default config for PARC reader
     */
    public static ResourceManager getDefaultConfigWithSourceDir(String sourceDir) {
        Properties props = new Properties();
        props.setProperty(POPULATE_POS.key, POPULATE_POS.value);
        props.setProperty(POPULATE_LEMMA.key, POPULATE_LEMMA.value);
        props.setProperty(POPULATE_ATTRIBUTION.key, POPULATE_ATTRIBUTION.value);
        props.setProperty(CorpusReaderConfigurator.CORPUS_NAME.key, DEFAULT_CORPUS_NAME);
        props.setProperty(CorpusReaderConfigurator.SOURCE_DIRECTORY.key, sourceDir);
        props.setProperty(CorpusReaderConfigurator.SOURCE_EXTENSION.key, ".xml");
        props.setProperty(CorpusReaderConfigurator.SUPPRESS_FILE_ERRORS.key, TRUE);

        return new ResourceManager(props);
    }
}
