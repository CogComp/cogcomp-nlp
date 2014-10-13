package edu.illinois.cs.cogcomp.comma;

import edu.illinois.cs.cogcomp.core.algorithms.Sorters;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;

import java.io.File;
import java.util.*;

/**
 * Data reader for the comma dataset of Srikumar et al.
 */
public class CommaReader implements Parser {
    private Scanner scanner;
    private final String annotationFile;
    private List<Comma> commas, trainCommas, testCommas;
    private int currentComma;
    boolean train;

    public CommaReader(String annotationFile, String setting) {
        this.annotationFile = annotationFile;
        commas = new ArrayList<Comma>();
        trainCommas = new ArrayList<Comma>();
        testCommas = new ArrayList<Comma>();
        if (setting.equals("train")) train = true;
        else if (setting.equals("test")) train = false;
        readData();
    }

    private void readData() {
        try {
            scanner = new Scanner(new File(annotationFile));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        String line;
        while (scanner.hasNext()) {
            // A list of commas positions and their labels
            List<Comma> commaList = new ArrayList<Comma>();

            line = scanner.nextLine().trim();
            assert line.startsWith("%%%");

            // Next line is the sentence id (in PTB), ignore for now
            scanner.nextLine();

            String rawText = scanner.nextLine().trim();

            line = scanner.nextLine().trim();
            assert line.length() == 0;

            line = scanner.nextLine().trim();
            assert line.equals("ANNOTATION:");

            line = scanner.nextLine().trim();

            Map<Integer, Set<Integer>> labeledCommas = getLabeledCommas(line);

            line = scanner.nextLine().trim();
            assert line.length() == 0;

            line = scanner.nextLine().trim();
            assert line.equals("COMMAS: " + labeledCommas.size() + " Total") : rawText
                    + "\n" + labeledCommas.size() + "\n" + line;

            for (int commaId : Sorters.sortSet(labeledCommas.keySet())) {
                line = scanner.nextLine().trim();
                assert line.startsWith(commaId + ".");

                String relationName = line.split("\\]")[0].split("\\[")[1].trim();

                for (int commaIndex : labeledCommas.get(commaId))
                    commaList.add(new Comma(commaIndex, relationName, rawText));

                String tmp = line.substring(line.indexOf(":") + 1, line.indexOf(" relation")).trim();
                int numRelations = Integer.parseInt(tmp);

                // Skip the rest of the lines
                for (int relationId = 0; relationId < numRelations + 1; relationId++)
                    scanner.nextLine();

                // Skip the final (empty) line
                line = scanner.nextLine().trim();

                assert line.length() == 0;
            }
            commas.addAll(commaList);
        }
        // Now do the train/test splits (80/20)
        // TODO This should become cross-fold validation (5 folds)
        int trainTestSplitPoint = (int) Math.round(.8 * commas.size());
        for (int i = 0; i < trainTestSplitPoint; i++) {
            trainCommas.add(commas.get(i));
        }
        for (int i = trainTestSplitPoint; i < commas.size(); i++) {
            testCommas.add(commas.get(i));
        }
    }

    @Override
    public Object next() {
        if (train) {
            if (trainCommas.size() > currentComma)
                return trainCommas.get(currentComma++);
            return null;
        }
        else {
            if (testCommas.size() > currentComma)
                return testCommas.get(currentComma++);
            return null;
        }
    }

    @Override
    public void reset() {
        currentComma = 0;
    }

    @Override
    public void close() {
        scanner.close();
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

    public static void main(String[] args) {
        CommaReader reader = new CommaReader("data/comma_resolution_data.txt", "train");
        reader.readData();
        Comma c;
        while ((c = (Comma) reader.next()) != null)
            System.out.println(c.getWordToLeft(2));
    }
}
