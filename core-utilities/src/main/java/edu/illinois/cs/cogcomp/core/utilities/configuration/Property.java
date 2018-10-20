/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.utilities.configuration;

/**
 * A container for a property name and its default value to be using with {@link Configurator}s
 *
 * @author Christos Christodoulopoulos
 */
public class Property {
    public String key;
    public String value;

    public Property(String key, String value) {
        this.key = key;
        this.value = value;
    }
}
