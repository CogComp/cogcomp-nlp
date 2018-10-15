/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
/**
 *
 */
package edu.illinois.cs.cogcomp.core.datastructures.trees;

/**
 * @author Vivek Srikumar Nov 9, 2008
 */
public class TreeParserFactory {

    private static TreeParser<String> parser = null;

    public static TreeParser<String> getStringTreeParser() {

        if (parser == null) {
            parser = new TreeParser<>(new INodeReader<String>() {

                public String parseNode(String string) {
                    return string;
                }
            });
        }

        return parser;
    }
}
