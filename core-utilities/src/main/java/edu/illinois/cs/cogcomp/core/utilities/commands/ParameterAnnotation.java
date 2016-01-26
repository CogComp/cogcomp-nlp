package edu.illinois.cs.cogcomp.core.utilities.commands;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author vivek June 12, 2009
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ParameterAnnotation {
    String name();

    String description();
}
