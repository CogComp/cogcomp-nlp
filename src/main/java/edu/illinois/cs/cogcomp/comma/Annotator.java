package edu.illinois.cs.cogcomp.comma;

import edu.illinois.cs.cogcomp.core.utilities.ResourceManager;
import edu.illinois.cs.cogcomp.edison.data.curator.CuratorClient;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;
import edu.illinois.cs.cogcomp.nlp.pipeline.IllinoisPreprocessor;
import edu.illinois.cs.cogcomp.thrift.base.AnnotationFailedException;
import edu.illinois.cs.cogcomp.thrift.base.ServiceUnavailableException;
import org.apache.thrift.TException;

import java.util.Arrays;

/**
 * A class that contains all the necessary pre-processing for each sentence.
 * Can be used with Curator, or with the illinois-nlp-pipeline
 */
public class Annotator {
    private final CuratorClient curator;
    private final IllinoisPreprocessor illinoisPreprocessor;
    private final boolean useCurator, tokenized;

    private static final boolean forceUpdate = false;
    private static final String curatorHost = "trollope.cs.illinois.edu";
    private static final int curatorPort = 9010;
    private static final String pipelineConfigFile = "config/pipeline-config.properties";


    public Annotator(boolean useCurator, boolean tokenized) throws Exception {
        this.useCurator = useCurator;
        this.tokenized = tokenized;
        if (useCurator) {
            curator = new CuratorClient(curatorHost, curatorPort, tokenized);
            illinoisPreprocessor = null;
        }
        else {
            ResourceManager rm = new ResourceManager(pipelineConfigFile);
            illinoisPreprocessor = new IllinoisPreprocessor(rm);
            curator = null;
        }
    }

    public TextAnnotation preProcess(String corpusId, String id, String text) throws AnnotationFailedException, TException,
            ServiceUnavailableException {
        TextAnnotation ta;
        if (useCurator) {
            if (tokenized)
                ta = new TextAnnotation(corpusId, id, Arrays.asList(text));
            else ta = curator.getTextAnnotation(corpusId, id, text, forceUpdate);
            addViewsFromCurator(ta);
        }
        else {
            ta = illinoisPreprocessor.processTextToTextAnnotation(corpusId, id, text, tokenized);
            addAdditionalViewsFromPipeline(ta);
        }
        return ta;
    }

    private void addViewsFromCurator(TextAnnotation ta) throws AnnotationFailedException, TException,
            ServiceUnavailableException {
        curator.addNamedEntityView(ta, forceUpdate);
        curator.addChunkView(ta, forceUpdate);
        curator.addStanfordParse(ta, forceUpdate);
        curator.addBerkeleyParse(ta, forceUpdate);
        curator.addCharniakParse(ta, forceUpdate);
        curator.addPOSView(ta, forceUpdate);
        curator.addSRLVerbView(ta, forceUpdate);
        curator.addSRLNomView(ta, forceUpdate);
        curator.addStanfordDependencyView(ta, forceUpdate);
        curator.addEasyFirstDependencyView(ta, forceUpdate);
        // PrepSRL doesn't have a wrapper
        curator.addPredicateArgumentView(ta, forceUpdate, "prep", ViewNames.SRL_PREP);
    }

    private void addAdditionalViewsFromPipeline(TextAnnotation ta) {
        //TODO Add SRL views
    }
}
