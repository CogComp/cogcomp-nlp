/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.utilities;

import java.lang.annotation.*;

/**
 * Annotation to specify that the use of a particular class, or method should be avoided if
 * possible. Optionally, the annotator should provide an explanation and an alternative.
 *
 * @author Christos Christodoulopoulos
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface AvoidUsing {
    String reason();

    String alternative() default "";
}
