package edu.illinois.cs.cogcomp.core.utilities;

import java.lang.annotation.*;

/**
 * Annotation to specify that the use of a particular class, or method should be avoided if possible.
 * Optionally, the annotator should provide an explanation and an alternative.
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
