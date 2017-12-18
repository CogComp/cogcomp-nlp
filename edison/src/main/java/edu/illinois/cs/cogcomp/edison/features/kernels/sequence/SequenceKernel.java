/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.edison.features.kernels.sequence;


import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.RealFeature;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Sequence Kernel implementation. <br>
 * A Sequence Kernel is a convolution kernel between sequences. The algorithm
 * corresponds to the recursive computation presented in [Bunescu&Mooney,2006]. <br>
 * <br>
 * More information at: <br>
 * [Bunescu&Mooney,2006] Razvan Bunescu and Raymond Mooney. Subsequence kernels
 * for relation extraction. In Y. Weiss, B. Scholkopf, and J. Platt, editors,
 * Advances in Neural Information Processing Systems 18, pages 171-178. MIT
 * Press, Cambridge, MA, 2006.
 *
 * Adoped from: https://github.com/SAG-KeLP/kelp-additional-kernels/blob/master/src/main/java/it/uniroma2/sag/kelp/kernel/sequence/SequenceKernel.java
 *
 * @author Danilo Croce
 * @author Daniel Khashabi
 *
 */
public class SequenceKernel implements FeatureExtractor<Pair<View, View>> {
    /**
     * Maximum length of common subsequences
     */
    private int maxSubseqLeng = 4;
    /**
     * Gap penalty
     */
    private float lambda = 0.75f;

    /**
     * @param maxSubseqLeng Maximum length of common subsequences
     * @param lambda Gap penalty
     */
    public SequenceKernel(int maxSubseqLeng, float lambda) {
        this.maxSubseqLeng = maxSubseqLeng;
        this.lambda = lambda;
    }

    /**
     * @return Gap penalty
     */
    public float getLambda() {
        return lambda;
    }

    /**
     * @return Maximum length of common subsequences
     */
    public int getMaxSubseqLeng() {
        return maxSubseqLeng;
    }

    /**
     * @param lambda Gap penalty
     */
    public void setLambda(float lambda) {
        this.lambda = lambda;
    }

    /**
     * @param maxSubseqLeng Maximum length of common subsequences
     */
    public void setMaxSubseqLeng(int maxSubseqLeng) {
        this.maxSubseqLeng = maxSubseqLeng;
    }

    @Override
    public Set<Feature> getFeatures(Pair<View, View> views) throws EdisonException {
        Set<Feature> features = new LinkedHashSet<>();
        float[] sk = stringKernel(views.getFirst(), views.getFirst());
        float result = 0;
        for (int i = 0; i < sk.length; i++) result += sk[i];
        features.add(new RealFeature(this.getName(), result));
        return features;
    }

    /**
     * Computes a simple the Identity similarity function between two element of
     * the sequence. Ideally in applications you can override this definitions.
     * @param se1 an element from the first sequence
     * @param se2 an element from the second sequence
     * @return 1 if both elements have the same label. 0 otherwise.
     */
    private float elementSimilarity(Constituent se1, Constituent se2) {
        if(se1.getLabel().equals(se2.getLabel())) return 1;
        return 0;
    }

    /**
     * Computes the number of common subsequences between two sequences. The
     * algorithm corresponds to the recursive computation from Figure 1 in the
     * paper [Bunescu&Mooney,2005] where: - K stands for K; - Kp stands for K';
     * - Kpp stands for K''; - common stands for c;
     *
     * @param s first sequence
     * @param t second sequence
     * @return kernel value K[], one position for every length up to n.
     */
    private float[] stringKernel(View s, View t) {
        int sl = s.getNumberOfConstituents();
        int tl = t.getNumberOfConstituents();

        List<Constituent> sCons = s.getConstituents();
        List<Constituent> tCons = t.getConstituents();

        float[][][] Kp = new float[maxSubseqLeng + 1][sl][tl];

        for (int j = 0; j < sl; j++)
            for (int k = 0; k < tl; k++)
                Kp[0][j][k] = 1;

        for (int i = 0; i < maxSubseqLeng; i++) {
            for (int j = 0; j < sl - 1; j++) {
                float Kpp = 0f;
                for (int k = 0; k < tl - 1; k++) {
                    Kpp = lambda
                            * (Kpp + lambda
                            * elementSimilarity(sCons.get(j), tCons.get(k))
                            * Kp[i][j][k]);
                    Kp[i + 1][j + 1][k + 1] = lambda * Kp[i + 1][j][k + 1]
                            + Kpp;
                }
            }
        }

        float[] K = new float[maxSubseqLeng];
        for (int l = 0; l < K.length; l++) {
            K[l] = 0f;
            for (int j = 0; j < sl; j++) {
                for (int k = 0; k < tl; k++)
                    K[l] += lambda * lambda
                            * elementSimilarity(sCons.get(j), tCons.get(k))
                            * Kp[l][j][k];
            }
        }

        return K;
    }

    @Override
    public String getName() {
        return "#sequence-kernel";
    }
}
