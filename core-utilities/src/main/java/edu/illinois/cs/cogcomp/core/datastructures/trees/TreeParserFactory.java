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
