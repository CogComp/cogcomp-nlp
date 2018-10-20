/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.datastructures;

import java.util.Set;

/**
 * Interface for common methods when supporting attributes.
 */
public interface HasAttributes {
    void addAttribute(String key, String value);

    String getAttribute(String key);

    Set<String> getAttributeKeys();

    boolean hasAttribute(String key);

    void removeAllAttributes();
}
