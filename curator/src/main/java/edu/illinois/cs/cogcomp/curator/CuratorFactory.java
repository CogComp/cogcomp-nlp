package edu.illinois.cs.cogcomp.curator;

import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

/**
 * A simple factory for an {@link CuratorAnnotatorService} object.
 *
 * @author Christos Christodouloupoulos
 */
public class CuratorFactory {

    public static AnnotatorService buildCuratorClient() throws Exception {
        return new CuratorAnnotatorService();
    }

    public static AnnotatorService buildCuratorClient(ResourceManager rm) throws Exception {
        return new CuratorAnnotatorService(rm);
    }
}
