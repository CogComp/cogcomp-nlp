package edu.illinois.cs.cogcomp.annotation.handler;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.wikifier.common.GlobalParameters;
import edu.illinois.cs.cogcomp.wikifier.inference.InferenceEngine;
import edu.illinois.cs.cogcomp.wikifier.models.LinkingProblem;

/**
 * wraps the Wikifier for the pipeline
 * Created by mssammon on 2/26/16.
 */
public class IllinoisWikifierHandler extends PipelineAnnotator
{
    private static final String WIKI_CONFIG = "wikifierConfig";
    private final InferenceEngine ie;

    public IllinoisWikifierHandler(ResourceManager rm ) throws Exception {
        super("IllinoisWikifier", "3.9", "Wikifier", ViewNames.WIKIFIER, new String[]{ViewNames.POS, ViewNames.SHALLOW_PARSE, ViewNames.NER_CONLL});

        String wikiConfig = rm.getString( WIKI_CONFIG );
        GlobalParameters.loadConfig( wikiConfig );
        ie = new InferenceEngine(false);
    }

    @Override
    public void addView(TextAnnotation textAnnotation) throws AnnotatorException {

        LinkingProblem lp = null;
        try {
//            lp = new LinkingProblem(textAnnotation );
            ie.annotate(lp);
        } catch (Exception e) {
            e.printStackTrace();
        }

//        int linkedMentions = lp.

    }
}
