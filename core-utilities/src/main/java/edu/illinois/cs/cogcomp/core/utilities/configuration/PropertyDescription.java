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
