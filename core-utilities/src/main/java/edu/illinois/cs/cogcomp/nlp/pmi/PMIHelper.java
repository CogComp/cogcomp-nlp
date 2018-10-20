/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.pmi;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Compute PMI for pairs of terms. Allows for variants to be specified as lists.
 * Requires a set of precomputed ngrams in a {@link CachedNgramCounter}.
 */
public class PMIHelper {
    private static Logger logger = LoggerFactory.getLogger(PMIHelper.class);

    private final static double EPSILON = 1e-15;

    private final CachedNgramCounter ngrams;

    public PMIHelper(CachedNgramCounter ngrams) {
        this.ngrams = ngrams;
    }

    public double getNPMI(String[] leftRight, String[] left, String[] right) {
        double pmi = getPMI(leftRight, left, right);

        double px = getP(left);
        double py = getP(right);

        double denom = -Math.log(Math.max(px, py));

        return pmi / denom;

    }

    public double getNPMI(String leftRight, String left, String right) {
        double pmi = getPMI(leftRight, left, right);

        double px = getP(left);
        double py = getP(right);

        double denom = -Math.log(Math.max(px, py));

        return pmi / denom;

    }

    private double getP(String xs) {

        long[] counts = ngrams.getCount(new String[] {xs});

        long cx = 0;
        for (long c : counts)
            cx += c;

        long tx = ngrams.getTotalCount(getNumTokens(xs));

        return cx * 1.0 / tx;
    }

    private double getP(String[] xs) {
        List<String> items = Arrays.asList(xs);

        long[] counts = ngrams.getCount(items);

        long cx = 0;
        for (long c : counts)
            cx += c;

        long tx = getTotalCount(xs);

        return cx * 1.0 / tx;
    }

    /**
     * Use this funtion to include variants into the same bucket.
     * <p>
     * For example, say you want to PMI for "hair of girl" and want to include {"hair", "the hair"}
     * to get counts for the left part, and {"girl", "the girl", "a girl"} for the right and all
     * combinations for leftRight, you need to call
     * <p>
     * <code>
     * String[] leftRight = {"hair of girl", "hair of the girl", ..., "the hair of girl",...};
     * String[] right = {"girl", "the girl", "a girl"};
     * String[] left = {"the hair", "hair"}
     * double pmi = getPMI(leftRight, left, right);
     * </code>
     */
    public double getPMI(String[] leftRight, String[] left, String[] right) {
        List<String> items = new ArrayList<>();
        items.addAll(Arrays.asList(leftRight));
        items.addAll(Arrays.asList(left));
        items.addAll(Arrays.asList(right));

        long[] counts = ngrams.getCount(items);

        assert counts.length == leftRight.length + left.length + right.length;

        long cxy = 0, cx = 0, cy = 0;

        for (int i = 0; i < leftRight.length; i++)
            cxy += counts[i];

        for (int i = leftRight.length; i < left.length + leftRight.length; i++)
            cx += counts[i];

        for (int i = left.length + leftRight.length; i < counts.length; i++)
            cy += counts[i];

        long txy = getTotalCount(leftRight);
        long tx = getTotalCount(left);
        long ty = getTotalCount(right);

        return getPMI(cxy, cx, cy, txy, tx, ty);

    }

    private long getTotalCount(String[] ss) {
        long l = 0;

        Set<Integer> set = new HashSet<>();
        for (String s : ss)
            set.add(getNumTokens(s));

        for (int i : set)
            l += ngrams.getTotalCount(i);

        return l;
    }

    private int getNumTokens(String s) {
        return s.replaceAll("\\s+", " ").trim().split(" ").length;
    }

    public double getPMI(String leftRight, String left, String right) {
        long[] counts = ngrams.getCount(Arrays.asList(leftRight, left, right));

        long txy = ngrams.getTotalCount(getNumTokens(leftRight));
        long tx = ngrams.getTotalCount(getNumTokens(left));
        long ty = ngrams.getTotalCount(getNumTokens(right));

        return getPMI(counts[0], counts[1], counts[2], txy, tx, ty);
    }

    /**
     * returns c * PMI, where C is a constant that is dependent on the dataset .
     */
    private double getPMI(long cxy, long cx, long cy, long txy, long tx, long ty) {

        double pxy = cxy * 1.0 / txy + EPSILON;
        double px = cx * 1.0 / tx + EPSILON;
        double py = cy * 1.0 / ty + EPSILON;

        return Math.log(pxy) - Math.log(px) - Math.log(py);
    }

    public static void main(String[] args) {
        GoogleNgramsCounts ngrams = new GoogleNgramsCounts("db", "/scratch/vsrikum2/ngrams/data");

        PMIHelper pmi = new PMIHelper(ngrams);

        String[] strs =
                new String[] {"Abraham Lincoln", "Isaac Lincoln", "Steve Jobs", "Steve Wozniak",
                        "great wall", "china", "little wall", "great wall of china",
                        "little wall of china", "girl with hair", "girl with the hair",
                        "the girl with hair", " the girl with the hair", " a girl with hair",
                        " a girl with the hair", "a girl", "girl", "the girl", "hair", "the hair"};

        ngrams.getCount(strs);

        for (String xy : new String[] {"Abraham Lincoln", "Isaac Lincoln", "Steve Jobs",
                "Steve Wozniak"}) {
            String[] parts = xy.split(" ");
            String x = parts[0];
            String y = parts[1];

            System.out.println(xy + "\t" + pmi.getPMI(xy, x, y) + "\t" + pmi.getNPMI(xy, x, y));
        }

        List<Pair<String, String>> items = new ArrayList<>();
        items.add(new Pair<>("great wall", "china"));
        items.add(new Pair<>("little wall", "china"));

        for (Pair<String, String> item : items) {
            String xy = item.getFirst() + " of " + item.getSecond();

            System.out.println(xy + "\t" + pmi.getPMI(xy, item.getFirst(), item.getSecond()) + "\t"
                    + pmi.getNPMI(xy, item.getFirst(), item.getSecond()));
        }

        String[] xy =
                {"girl with hair", "girl with the hair", "the girl with hair",
                        " the girl with the hair", " a girl with hair", " a girl with the hair"};
        String[] x = {"a girl", "girl", "the girl"};
        String[] y = {"hair", "the hair"};

        System.out.println("girl with hair" + "\t" + pmi.getPMI(xy, x, y) + "\t"
                + pmi.getNPMI(xy, x, y));

    }
}
