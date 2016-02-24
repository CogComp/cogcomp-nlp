package edu.illinois.cs.cogcomp.ner;

import edu.illinois.cs.cogcomp.ner.config.NerBaseConfigurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static Logger logger = LoggerFactory.getLogger(NerAnnotatorManager.class);

    private static Map<String, NERAnnotator> nerAnnotatorMap;

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

        if (null == nerAnnotatorMap)
            nerAnnotatorMap = new HashMap<>();

        if (!nerAnnotatorMap.containsKey(viewName)) {

            NERAnnotator ner = new NERAnnotator(nonDefaultRm, viewName);
            nerAnnotatorMap.put(viewName, ner);
        } else {
            logger.warn("You are replacing an existing NER model for the view name '" + viewName
                    + "'. ");
        }


        return nerAnnotatorMap.get(viewName);
    }
}
