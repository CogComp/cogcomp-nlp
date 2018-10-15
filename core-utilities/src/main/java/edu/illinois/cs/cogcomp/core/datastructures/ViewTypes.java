/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.datastructures;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;

/**
 * This class forces each view name in {@link edu.illinois.cs.cogcomp.core.datastructures.ViewNames}
 * to declare its type so that it can be dynamically added to a {@link TextAnnotation}.
 *
 * TODO Eventually, this should replace ViewNames
 *
 * @author Christos Christodoulopoulos
 */
public enum ViewTypes {
    TOKEN_LABEL_VIEW, SPAN_LABEL_VIEW, DEPENDENCY_VIEW, PARSE_VIEW, PREDICATE_ARGUMENT_VIEW, COREF_VIEW
}
