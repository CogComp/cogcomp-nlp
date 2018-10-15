/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.pmi;

import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.core.utilities.ShellInterface;

import java.util.*;


/**
 * This class requires that you have installed 'get1t' (e.g. https://github.com/sangmank/get1t) and
 *    an appropriate ngram corpus.
 */
public class GoogleNgramsCounts extends CachedNgramCounter {

    private static int MAX_NUM_TOKENS = 5;
    private static String ngramProgram = "get1t";

    private final String googleNgramsDataDir;

    public GoogleNgramsCounts(String cacheFile, String googleNgramsDataDir) {
        super(cacheFile);
        this.googleNgramsDataDir = googleNgramsDataDir;
    }

    protected int getMaxNumTokens() {
        return MAX_NUM_TOKENS;
    }

    /**
     * this method runs a google ngram program to retrieve the appropriate count for the specified
     *   terms.
     * @param set
     * @return
     */
    protected Map<String, Long> getNgramCount(Set<String> set) {

        Map<Integer, List<String>> byLength = splitByLengths(set);
        Map<String, Long> map = new HashMap<>();

        for (int numTokens : byLength.keySet()) {

            List<String> item = byLength.get(numTokens);

            String tmpDir = "~/tmp";
            String tmpFile = tmpDir + "/google.ngrams.get1t" + (new Random()).nextInt();

            IOUtils.mkdir(tmpDir);

            try {
                String outputFile = tmpFile + ".ngrams";

                LineIO.write(tmpFile, item);

                String command =
                        ngramProgram + " -f " + tmpFile + " -n " + numTokens + " -g " + outputFile
                                + " -z " + googleNgramsDataDir + "/" + numTokens + "gms";
                ShellInterface.executeCommandWithOutput(command);

                ArrayList<String> read = LineIO.read(outputFile);

                assert read.size() == item.size();

                for (String line : read) {
                    String[] parts = line.split("\t");
                    String s = parts[0];
                    long v = Long.parseLong(parts[1].trim());
                    map.put(s, v);
                }

                IOUtils.rm(tmpFile);
                IOUtils.rm(outputFile);
                IOUtils.rm(numTokens + "-total.txt");

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return map;
    }

    private Map<Integer, List<String>> splitByLengths(Set<String> set) {
        Map<Integer, List<String>> map = new HashMap<>();

        for (String item : set) {
            int numTokens = item.split("\\s").length;
            if (!map.containsKey(numTokens))
                map.put(numTokens, new ArrayList<String>());

            map.get(numTokens).add(item);
        }

        return map;
    }

    public long getTotalCount(int numTokens) {
        if (numTokens == 1)
            return 2049816534458L;
        else if (numTokens == 2)
            return 910884463583L;
        else if (numTokens == 3)
            return 739006848674L;
        else if (numTokens == 4)
            return 508416581134L;
        else if (numTokens == 5)
            return 254468695857L;
        else
            return 1;

    }
}
