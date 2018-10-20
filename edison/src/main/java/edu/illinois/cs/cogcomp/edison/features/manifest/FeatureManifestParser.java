/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.manifest;

import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.core.datastructures.trees.TreeParserFactory;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.io.InputStream;
import java.util.Scanner;

/**
 * This class is unused, use {@link ManifestParser} instead.
 */
@Deprecated
public class FeatureManifestParser {
    private String contents;

    public FeatureManifestParser(InputStream file) throws EdisonException {
        Scanner scanner = new Scanner(file);

        StringBuilder content = new StringBuilder();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            int indexOf = line.indexOf(";");
            if (indexOf >= 0)
                line = line.substring(0, indexOf);

            if (line.trim().length() == 0)
                continue;

            content.append(line).append("\n");
        }

        initialize(content.toString());
    }

    public FeatureManifestParser(String contents) throws EdisonException {
        initialize(removeComments(contents));
    }

    private void initialize(String contents) {
        this.contents = contents;
        Tree<String> parse = TreeParserFactory.getStringTreeParser().parse(contents);

    }

    private String removeComments(String contents) {
        StringBuilder sb = new StringBuilder();

        for (String line : contents.split("\\n+")) {

            int indexOf = line.indexOf(";");
            if (indexOf >= 0)
                line = line.substring(0, indexOf);

            if (line.trim().length() == 0)
                continue;

            sb.append(line).append("\n");
        }

        return sb.toString();
    }

}
