/**
 * This software is released under the University of Illinois/Research and
 *  Academic Use License. See the LICENSE file in the root folder for details.
 * Copyright (c) 2016
 *
 * Developed by:
 * The Cognitive Computation Group
 * University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.ner.config;

import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

import java.util.Properties;

/**
 * A configurator for illinois ner OntoNotes model.
 *
 * Created by mssammon on 10/15/15.
 */
public class NerOntonotesConfigurator extends Configurator {
    private static final String DEFAULT_ONTONOTES_MODEL_PATH = "ner/models";
    private static final String ONTONOTES_LABEL_TYPES =
            "TIME LAW GPE NORP LANGUAGE PERCENT FAC PRODUCT ORDINAL LOC PERSON WORK_OF_ART MONEY DATE EVENT QUANTITY ORG CARDINAL";
    private static final String ONTONOTES_MODEL_NAME = "OntoNotes";

    // private static final String ONTONOTES_VIEW_NAME = ViewNames.NER_ONTONOTES;

    @Override
    public ResourceManager getDefaultConfig() {
        // treatAllFilesInFolderAsOneBigDocument false
        Properties props = new Properties();

        props.setProperty(NerBaseConfigurator.TREAT_ALL_FILES_AS_ONE, FALSE);
        props.setProperty(NerBaseConfigurator.PATH_TO_MODEL, DEFAULT_ONTONOTES_MODEL_PATH);
        props.setProperty(NerBaseConfigurator.LABEL_TYPES, ONTONOTES_LABEL_TYPES);
        props.setProperty(NerBaseConfigurator.MODEL_NAME, ONTONOTES_MODEL_NAME);
        return (new NerBaseConfigurator()).getConfig(new ResourceManager(props));
    }
}
