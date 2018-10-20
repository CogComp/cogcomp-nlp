/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.utilities.configuration;

import java.lang.annotation.*;

/**
 * Annotation to describe a {@link Property}'s purpose, default values and alternative options
 *
 * @author Christos Christodoulopoulos
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PropertyDescription {
    String purpose();

    String defaultValue();

    String[] options() default "";
}
