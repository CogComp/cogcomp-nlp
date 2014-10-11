package edu.illinois.cs.cogcomp.comma;

import edu.illinois.cs.cogcomp.core.algorithms.Sorters;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;

import java.io.File;
import java.util.*;

/**
 * Data reader for the comma dataset of Srikumar et al.
 */
public class CommaReader implements Parser {

    private Scanner scanner;
    private final String annotationFile;

    public CommaReader(String annotationFile) {
        this.annotationFile = annotationFile;
    }

    @Override
    public Object next() {
        // A list of commas positions and their labels
        List<Pair<Integer, String>> commas = new ArrayList<Pair<Integer, String>>();

        String line = nextLine();

        assert line.startsWith("%%%");

        // Next line is the sentence id (in PTB), ignore for now
        nextLine();

        String rawText = nextLine();

        line = nextLine();
        assert line.length() == 0;

        line = nextLine();
        assert line.equals("ANNOTATION:");

        line = nextLine();

        Map<Integer, Set<Integer>> labeledCommas = getLabeledCommas(line);

        line = nextLine();
        assert line.length() == 0;

        line = nextLine();
        assert line.equals("COMMAS: " + labeledCommas.size() + " Total") : rawText
                + "\n" + labeledCommas.size() + "\n" + line;

        for (int commaId : Sorters.sortSet(labeledCommas.keySet())) {
            line = nextLine();
            assert line.startsWith(commaId + ".");

            String relationName = line.split("\\]")[0].split("\\[")[1].trim();

            for (int commaIndex : labeledCommas.get(commaId))
                commas.add(new Pair<Integer, String>(commaIndex, relationName));

            String tmp = line.substring(line.indexOf(":") + 1, line.indexOf(" relation")).trim();
            int numRelations = Integer.parseInt(tmp);

            // Skip the rest of the lines
            for (int relationId = 0; relationId < numRelations + 1; relationId++)
                nextLine();

            // Skip the final (empty) line
            line = nextLine();

            assert line.length() == 0;
        }
        4
        // TODO Create a comma class that contains the sentence and label
        return commas;
    }

    @Override
    public void reset() {
        try {
            scanner = new Scanner(new File(annotationFile));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void close() {
        scanner.close();
    }

    private String nextLine() {
        if (scanner.hasNextLine())
            return scanner.nextLine().trim();
        else
            return "";
    }

    private Map<Integer, Set<Integer>> getLabeledCommas(String annotation) {
        Map<Integer, Set<Integer>> map = new HashMap<Integer, Set<Integer>>();
        String[] parts = annotation.split("\\s+");

        for (int tokenId = 0; tokenId < parts.length; tokenId++) {
            String token = parts[tokenId];
            if (token.startsWith("[")) {
                String id = token.split("\\]")[0].replaceAll("\\[", "");
                for (String cId : id.split(",")) {
                    int commaId = Integer.parseInt(cId);
                    if (!map.containsKey(commaId))
                        map.put(commaId, new HashSet<Integer>());
                    map.get(commaId).add(tokenId);
                }
            }
        }
        return map;
    }
}
