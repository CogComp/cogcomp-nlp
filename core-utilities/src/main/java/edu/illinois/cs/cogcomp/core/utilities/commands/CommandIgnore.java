package edu.illinois.cs.cogcomp.core.utilities.commands;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Ignores a public static function. A function that is tagged with this tag
 * will not be exposed to the shell.
 * <p/>
 * Usage:
 * <p/>
 * <code> @CommandIgnore </code>
 * <p/>
 * <code> public static void Foo(String bar) {...</code>
 *
 * @author vivek
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandIgnore {
}
