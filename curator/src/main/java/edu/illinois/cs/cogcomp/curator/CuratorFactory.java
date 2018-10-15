/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
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
