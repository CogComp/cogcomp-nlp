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
