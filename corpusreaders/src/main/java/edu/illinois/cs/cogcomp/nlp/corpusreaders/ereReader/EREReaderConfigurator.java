/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader;

import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Property;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

/**
 * Manage configuration options for the different ERE corpora.
 *
 * @author mssammon
 */
public class EREReaderConfigurator extends Configurator {

    public static final Property SOURCE_DIR = new Property("sourceDir", "source");
    public static final Property ANNOTATION_DIR = new Property("annotationDir", "ere");
    public static final Property SOURCE_EXTENSION = new Property("sourceExt", ".xml");
    public static final Property ANNOTATION_EXTENSION = new Property("annotationExt", ".xml");


    /**
     * get a ResourceManager object with the default key/value pairs for this configurator
     *
     * @return a non-null ResourceManager with appropriate values set.
     */
    @Override
    public ResourceManager getDefaultConfig() {
        Property[] properties = {SOURCE_DIR, ANNOTATION_DIR, SOURCE_EXTENSION, ANNOTATION_EXTENSION};

        return new ResourceManager(generateProperties(properties));
    }
}
