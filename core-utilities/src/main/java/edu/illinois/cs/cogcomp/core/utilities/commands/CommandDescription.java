/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.utilities.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description for a command.
 * <p>
 * Usage:
 * <p>
 * <code> @CommandDescription(usage="Foo bar", description = "Does foo to bar") </code>
 * <p>
 * <code> public static void Foo(String bar) {...</code>
 *
 * @author Vivek Srikumar
 */

@Target(value = {ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandDescription {

    String usage() default "";

    String description() default "";
}
