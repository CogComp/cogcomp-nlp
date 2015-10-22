package edu.illinois.cs.cogcomp.curator;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Annotator;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.thrift.base.AnnotationFailedException;
import edu.illinois.cs.cogcomp.thrift.base.ServiceUnavailableException;
import edu.illinois.cs.cogcomp.thrift.curator.Curator;
import org.apache.thrift.TException;

import java.net.SocketException;

/**
 * A single annotator object, corresponding to a {@link edu.illinois.cs.cogcomp.thrift.curator.Curator.Client}'s annotator. Multiple instances of this
 * class will be used as {@link edu.illinois.cs.cogcomp.core.datastructures.textannotation.Annotator}s in {@link CuratorAnnotatorService}.
 *
 * The {@link #viewName} and {@link #requiredViews} fields are defined in Curator's configuration file
 * ({@code dist/configs/annotators.xml}).
 *
 * @author Christos Christodoulopoulos
 */
public class CuratorAnnotator implements Annotator {
    private CuratorClient curatorClient;
    private final String viewName;
    private final String[] requiredViews;

    public CuratorAnnotator(CuratorClient curatorClient, String viewName, String[] requiredViews) {
        this.curatorClient = curatorClient;
        this.viewName = viewName;
        this.requiredViews = requiredViews;
    }

    @Override
    public String getViewName() {
        return viewName;
    }

    @Override
    public View getView(TextAnnotation ta) throws AnnotatorException {
        try {
            return curatorClient.getTextAnnotationView(ta, viewName);
        } catch (TException | AnnotationFailedException | SocketException | ServiceUnavailableException e) {
            throw new AnnotatorException(e.getMessage());
        }
    }

    @Override
    public String[] getRequiredViews() {
        return requiredViews;
    }
}
