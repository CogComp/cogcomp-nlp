/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.utilities.commands;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Ignores a public static function. A function that is tagged with this tag will not be exposed to
 * the shell.
 * <p>
 * Usage:
 * <p>
 * <code> @CommandIgnore </code>
 * <p>
 * <code> public static void Foo(String bar) {...</code>
 *
 * @author vivek
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandIgnore {
}
