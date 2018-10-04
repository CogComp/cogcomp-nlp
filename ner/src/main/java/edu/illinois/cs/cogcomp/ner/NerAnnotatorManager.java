/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner;

import edu.illinois.cs.cogcomp.ner.config.NerBaseConfigurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class managers NER instances, ensuring that NERAnnotator with a distinct model is
 * instantiated exactly ONCE and passed around.
 *
 * Created by mssammon on 8/25/15.
 */
public class NerAnnotatorManager {

    /** this maps annotators to labels for the ner system, there can be only one. */
    private static Map<String, NERAnnotator> nerAnnotatorMap = new HashMap<>();

    /**
     * the viewName will be used as a KEY to instantiate/get the corresponding NER instance
     *
     * @param nonDefaultConfig a file containing non-default parameters for the NER model named by
     *        the viewName parameter.
     * @param viewName assign a name to the NER view generated. As shipped, this should be
     *        ViewNames.NER_CONLL or ViewNames.NER_ONTONOTES
     * @return an NERAnnotator with models instantiated according to the values of viewName and the
     *         parameters in nonDefaultConfig.
     * @throws IOException
     */
    public static NERAnnotator buildNerAnnotator(String nonDefaultConfig, String viewName)
            throws IOException {
        return NerAnnotatorManager.buildNerAnnotator(new ResourceManager(nonDefaultConfig),
                viewName);
    }


    public static NERAnnotator buildNerAnnotator(ResourceManager nonDefaultRm) {
        return buildNerAnnotator(nonDefaultRm,
                nonDefaultRm.getString(NerBaseConfigurator.VIEW_NAME));
    }

    /**
     * the viewName will be used as a KEY to instantiate/get the corresponding NER instance
     *
     * @param nonDefaultRm a non-null ResourceManager object containing non-default parameters for
     *        the NER model named by the viewName parameter.
     * @param viewName assign a name to the NER view generated. As shipped, this should be
     *        ViewNames.NER_CONLL or ViewNames.NER_ONTONOTES
     * @return an NERAnnotator with models instantiated according to the values of viewName and the
     *         parameters in nonDefaultConfig.
     */
    public static NERAnnotator buildNerAnnotator(ResourceManager nonDefaultRm, String viewName) {
        synchronized (nerAnnotatorMap) {
            NERAnnotator ner = nerAnnotatorMap.get(viewName);
            if (ner == null) {
                ner = new NERAnnotator(nonDefaultRm, viewName);
                nerAnnotatorMap.put(viewName, ner);
            }
            return ner;
        }
    }
}
