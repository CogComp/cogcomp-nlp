/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders;

import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Property;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

import java.util.Properties;

/**
 * Created by mssammon on 6/21/16.
 */
public class CorpusReaderConfigurator extends Configurator {
    public static final Property CORPUS_NAME = new Property("corpusName", "dummyCorpusName");
    public static final Property CORPUS_DIRECTORY = new Property("corpusDirectory",
            "dummyCorpusDirectory");
    public static final Property SOURCE_DIRECTORY = new Property("sourceDirectory", "dummySourceDir");
    public static final Property ANNOTATION_DIRECTORY = new Property("annotationDirectory", "dummyAnnotationDir");
    public static final Property SOURCE_EXTENSION = new Property("sourceFileExtension", ".xml");
    public static final Property ANNOTATION_EXTENSION = new Property("annotationFileExtension", ".xml");
    public static final Property SUPPRESS_FILE_ERRORS = new Property("suppressFileErrors", FALSE);

    public static ResourceManager buildResourceManager(String corpus) {
        Properties props = new Properties();
        props.setProperty(CorpusReaderConfigurator.CORPUS_NAME.key, corpus);
        return new ResourceManager(props);
    }

    public static ResourceManager buildResourceManager(String corpusName, String sourceDir, String annotationDir,
                                                       String sourceFileExtension, String annotationFileExtension) {
        Properties props = new Properties();
        props.setProperty(CorpusReaderConfigurator.CORPUS_NAME.key, corpusName);
        props.setProperty(CorpusReaderConfigurator.SOURCE_DIRECTORY.key, sourceDir);
        props.setProperty(CorpusReaderConfigurator.ANNOTATION_DIRECTORY.key, annotationDir);
        props.setProperty(CorpusReaderConfigurator.SOURCE_EXTENSION.key, sourceFileExtension);
        props.setProperty(CorpusReaderConfigurator.ANNOTATION_EXTENSION.key, annotationFileExtension);
        props.setProperty(SUPPRESS_FILE_ERRORS.key, SUPPRESS_FILE_ERRORS.value);

        return new ResourceManager(props);
    }

    /**
     * get a ResourceManager object with the default key/value pairs for this configurator
     *
     * @return a non-null ResourceManager with appropriate values set.
     */
    @Override
    public ResourceManager getDefaultConfig() {
        // for now, omit source and annotation directories because they must be set to path on User host machine
        Property[] props = new Property[] {CORPUS_NAME, CORPUS_DIRECTORY, // SOURCE_DIRECTORY, ANNOTATION_DIRECTORY,
            SOURCE_EXTENSION, ANNOTATION_EXTENSION};
        return new ResourceManager(generateProperties(props));
    }

}
