package edu.illinois.cs.cogcomp.comma;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.curator.CuratorClient;
import edu.illinois.cs.cogcomp.nlp.utilities.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.thrift.base.AnnotationFailedException;
import edu.illinois.cs.cogcomp.thrift.base.ServiceUnavailableException;
import org.apache.thrift.TException;

import java.net.SocketException;
import java.util.List;

/**
 * A class that contains all the necessary pre-processing for each sentence.
 * Can be used with Curator, or
 * TODO with the illinois-nlp-pipeline
 */
public class PreProcessor {
    private final CuratorClient curator;
    private static final boolean tokenized = true;
    private static final boolean forceUpdate = false;
    private static final String curatorHost = "trollope.cs.illinois.edu";
    private static final int curatorPort = 9010;

    public PreProcessor() throws Exception {
        curator = new CuratorClient(curatorHost, curatorPort, tokenized, forceUpdate);
    }

    public TextAnnotation preProcess(List<String[]> text) throws AnnotationFailedException, TException,
            ServiceUnavailableException, SocketException {
        TextAnnotation ta = BasicTextAnnotationBuilder.createTextAnnotationFromTokens(text);
        addViewsFromCurator(ta);
        return ta;
    }

    private void addViewsFromCurator(TextAnnotation ta) throws AnnotationFailedException, TException,
            ServiceUnavailableException, SocketException {
        curator.addTextAnnotationView(ta, ViewNames.NER_CONLL);
        curator.addTextAnnotationView(ta, ViewNames.SHALLOW_PARSE);
        curator.addTextAnnotationView(ta, ViewNames.PARSE_STANFORD);
        curator.addTextAnnotationView(ta, ViewNames.POS);
        curator.addTextAnnotationView(ta, ViewNames.SRL_VERB);
        curator.addTextAnnotationView(ta, ViewNames.SRL_NOM);
        curator.addTextAnnotationView(ta, ViewNames.SRL_PREP);
    }
}
