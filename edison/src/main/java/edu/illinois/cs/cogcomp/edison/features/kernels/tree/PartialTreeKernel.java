/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.edison.features.kernels.tree;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.RealFeature;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.*;

/**
 * Partial Tree Kernel implementation.
 * <p>
 * A Partial Tree Kernel is a convolution kernel that evaluates the tree
 * fragments shared between two trees. The considered fragments are are partial
 * trees, i.e. a node and its partial descendancy (the descendancy can be
 * incomplete, i.e. a partial production is allowed). The kernel function is
 * defined as: </br>
 * <p>
 * \(K(T_1,T_2) = \sum_{n_1 \in N_{T_1}} \sum_{n_2 \in N_{T_2}}
 * \Delta(n_1,n_2)\)
 * <p>
 * </br> where \(\Delta(n_1,n_2)=\sum^{|F|}_i=1 I_i(n_1) I_i(n_2)\), that is the
 * number of common fragments rooted at the n1 and n2. It can be computed as:
 * </br> - if the node labels of \(n_1\) and \(n_2\) are different then
 * \(\Delta(n_1,n_2)=0\) </br> - else \(\Delta(n_1,n_2)= \mu(\lambda^2 +
 * \sum_{J_1, J_2, l(J_1)=l(J_2)} \lambda^{d(J_1)+d(J_2)} \prod_{i=1}^{l(J_1)}
 * \Delta(c_{n_1}[J_{1i}], c_{n_2}[J_{2i}])\)
 * <p>
 * </br></br> Fore details see [Moschitti, EACL2006] Alessandro Moschitti.
 * Efficient convolution kernels for dependency and constituent syntactic trees.
 * In ECML 2006, Berlin, Germany.
 *
 * @author Danilo Croce
 * @author Giuseppe Castellucci
 * @author Daniel Khashabi
 */
public class PartialTreeKernel implements FeatureExtractor<Pair<Tree<Constituent>, Tree<Constituent>>> {

    float NO_RESPONSE = -1f;

    static int oddNum = 79;

    public static int getHash(int first, int second) {
        return first + oddNum * second;
    }

    private int MAX_CHILDREN = 50;
    private int MAX_RECURSION = 20;

    /**
     * Vertical Decay Factor
     */
    private float mu;

    /**
     * Horizontal Decay factor
     */
    private float lambda;

    /**
     * Horizontal Decay factor, pow 2
     */
    private float lambda2;

    /**
     * Multiplicative factor to scale up/down the leaves contribution
     */
    private float terminalFactor = 1;

    /**
     * Maximum length of common subsequences considered in the recursion. It
     * reflects the maximum branching factor allowed to the tree fragments.
     */
    private int maxSubseqLeng = Integer.MAX_VALUE;

    private int recursion_id = 0;

    private float[][] kernel_mat_buffer = new float[MAX_RECURSION][MAX_CHILDREN];
    private float[][][] DPS_buffer = new float[MAX_RECURSION][MAX_CHILDREN + 1][MAX_CHILDREN + 1];
    private float[][][] DP_buffer = new float[MAX_RECURSION][MAX_CHILDREN + 1][MAX_CHILDREN + 1];

    /**
     * The delta matrix, used to cache the delta functions applied to subtrees
     */
    Map<Integer, Float> deltaMatrix = new HashMap<>();

    /**
     * Get the Vertical Decay factor
     *
     * @return Vertical Decay factor
     */
    public float getLambda() {
        return lambda;
    }

    /**
     * Get the Horizontal Decay factor
     *
     * @return Horizontal Decay factor
     */
    public float getMu() {
        return mu;
    }

    /**
     * Get the Terminal Factor
     */
    public float getTerminalFactor() {
        return terminalFactor;
    }

    /**
     * @return The maximum length of common subsequences considered in the
     * recursion. It reflects the maximum branching factor allowed to
     * the tree fragments.
     */
    public int getMaxSubseqLeng() {
        return maxSubseqLeng;
    }

    /**
     * @param maxSubseqLeng The maximum length of common subsequences considered in the
     *                      recursion. It reflects the maximum branching factor allowed to
     *                      the tree fragments.
     */
    public void setMaxSubseqLeng(int maxSubseqLeng) {
        this.maxSubseqLeng = maxSubseqLeng;
    }

    /**
     * Default constructor. It should be used only for json
     * serialization/deserialization purposes This constructor by default uses
     * lambda=0.4, mu=0.4, terminalFactor=1 and it forces to operate to the
     * represenatation whose identifier is "0".
     * <p>
     * Please use the PartialTreeKernel(String) or
     * PartialTreeKernel(float,float,float,String) to use a Partial Tree Kernel
     * in your application.
     */
    public PartialTreeKernel() {
        this(0.4f, 0.4f, 1f);
    }

    public void setLambda(float lambda) {
        this.lambda = lambda;
        this.lambda2 = this.lambda * this.lambda;
    }

    public void setMu(float mu) {
        this.mu = mu;
    }

    public void setTerminalFactor(float terminalFactor) {
        this.terminalFactor = terminalFactor;
    }

    /**
     * A Constructor for the Partial Tree Kernel in which parameters can be set manually.
     *
     * @param LAMBDA         lambda value in the PTK formula
     * @param MU             mu value of the PTK formula
     * @param terminalFactor terminal factor
     */
    public PartialTreeKernel(float LAMBDA, float MU, float terminalFactor) {
        this.lambda = LAMBDA;
        this.lambda2 = LAMBDA * LAMBDA;
        this.mu = MU;
        this.terminalFactor = terminalFactor;
    }

    /**
     * Determine the subtrees (from the two trees) whose root have the same
     * label. This optimization has been proposed in [Moschitti, EACL 2006].
     * @param a First Tree
     * @param b Second Tree
     * @return The node pairs having the same label
     */
    private ArrayList<Pair<Tree<Constituent>, Tree<Constituent>>> determineSubList(Tree<Constituent> a, Tree<Constituent> b) {

        ArrayList<Pair<Tree<Constituent>, Tree<Constituent>>> intersect = new ArrayList<>();

        int i = 0, j = 0, j_old, j_final;

        int cfr;
        List<Tree<Constituent>> nodesA = a.getChildren();
        List<Tree<Constituent>> nodesB = a.getChildren();
        int n_a = nodesA.size();
        int n_b = nodesB.size();

        while (i < n_a && j < n_b) {

            if ((cfr = (nodesA.get(i).getLabel().getSurfaceForm().compareTo(nodesB.get(j).getLabel().getSurfaceForm()))) > 0)
                j++;
            else if (cfr < 0)
                i++;
            else {
                j_old = j;
                do {
                    do {
                        intersect.add(new Pair(nodesA.get(i), nodesB.get(j)));

                        deltaMatrix.put(getHash(nodesA.get(i).hashCode(), nodesB.get(j).hashCode()), this.NO_RESPONSE);

                        j++;
                    } while (j < n_b && (nodesA.get(i).getLabel().getSurfaceForm().equals(nodesB.get(j).getLabel().getSurfaceForm())));
                    i++;
                    j_final = j;
                    j = j_old;
                } while (i < n_a && (nodesA.get(i).getLabel().getSurfaceForm().equals(nodesB.get(j).getLabel().getSurfaceForm())));
                j = j_final;
            }
        }

        return intersect;
    }

    /**
     * Evaluate the Partial Tree Kernel
     *
     * @param a First tree
     * @param b Second Tree
     * @return Kernel value
     */
    public float evaluateKernelNotNormalize(Tree<Constituent> a, Tree<Constituent> b) {
        /*
		 * TODO CHECK FOR MULTITHREADING WITH SIMONE FILICE AND/OR DANILO CROCE
		 *
		 * In order to avoid collisions in the DeltaMatrix when multiple threads
		 * call the kernel at the same time, a specific DeltaMatrix for each
		 * thread should be initialized
		 */

        // Initialize the delta function cache
        deltaMatrix.clear();

		/*
		 * Check the size of caching matrices
		 */
        int maxBranchingFactor = Math.max(a.getBranchingFactor(), b.getBranchingFactor());
        int maxHeight = Math.max(a.getHeight(), b.getHeight());

        if (kernel_mat_buffer[0].length < maxBranchingFactor + 1 || DP_buffer.length < maxHeight) {
            if (maxBranchingFactor >= MAX_CHILDREN) {
                MAX_CHILDREN = maxBranchingFactor + 1;
            }
            if (maxHeight > MAX_RECURSION)
                MAX_RECURSION = maxHeight;
            System.out.println("Increasing the size of cache matrices to host trees with height=" + MAX_RECURSION
                    + " and maxBranchingFactor=" + MAX_CHILDREN + "");
            kernel_mat_buffer = new float[MAX_RECURSION][MAX_CHILDREN];
            DPS_buffer = new float[MAX_RECURSION][MAX_CHILDREN][MAX_CHILDREN];
            DP_buffer = new float[MAX_RECURSION][MAX_CHILDREN][MAX_CHILDREN];
        }
		/*
		 * End of the check
		 */

        // The node pairs having the same label
        ArrayList<Pair<Tree<Constituent>, Tree<Constituent>>> pairs = determineSubList(a, b);

        float k = 0;

        for (Pair<Tree<Constituent>, Tree<Constituent>> pair : pairs) {
            k += ptkDeltaFunction(pair.getFirst(), pair.getSecond());
        }

        return k;
    }

    /**
     * Partial Tree Kernel Delta Function
     *
     * @param Nx root of the first tree
     * @param Nz root of the second tree
     * @return
     */
    private float ptkDeltaFunction(Tree<Constituent> Nx, Tree<Constituent> Nz) {
        float sum = 0;

        if (deltaMatrix.get(getHash(Nx.hashCode(), Nz.hashCode())) != this.NO_RESPONSE)
            return deltaMatrix.get(getHash(Nx.hashCode(), Nz.hashCode())); // already there

        if (!Nx.getLabel().getSurfaceForm().equals(Nz.getLabel().getSurfaceForm())) {
            deltaMatrix.put(getHash(Nx.hashCode(), Nz.hashCode()), 0f);
            return 0;
        } else if (Nx.getNumberOfChildren() == 0 || Nz.getNumberOfChildren() == 0) {
            deltaMatrix.put(getHash(Nx.hashCode(), Nz.hashCode()), mu * lambda2 * terminalFactor);
            return mu * lambda2 * terminalFactor;
        } else {
            float delta_sk = stringKernelDeltaFunction(Nx.getChildren(), Nz.getChildren());

            sum = mu * (lambda2 + delta_sk);

            deltaMatrix.put(getHash(Nx.hashCode(), Nz.hashCode()), sum);
            return sum;
        }
    }

    /**
     * The String Kernel formulation, that recursively estimates the partial
     * overlap between children sequences.
     *
     * @param Sx children of the first subtree
     * @param Sz children of the second subtree
     * @return string kernel score
     */
    private float stringKernelDeltaFunction(List<Tree<Constituent>> Sx,
                                            List<Tree<Constituent>> Sz) {

        int n = Sx.size();
        int m = Sz.size();

        float[][] DPS = DPS_buffer[recursion_id];
        float[][] DP = DP_buffer[recursion_id];
        float[] kernel_mat = kernel_mat_buffer[recursion_id];

        recursion_id++;

        int i, j, l, p;
        float K;

        p = n;
        if (m < n) p = m;
        if (p > maxSubseqLeng) p = maxSubseqLeng;

        kernel_mat[0] = 0;
        for (i = 1; i <= n; i++) {
            for (j = 1; j <= m; j++) {
                if ((Sx.get(i - 1).getLabel().getSurfaceForm().equals(Sz.get(j - 1).getLabel().getSurfaceForm()))) {
                    DPS[i][j] = ptkDeltaFunction(Sx.get(i - 1), Sz.get(j - 1));
                    kernel_mat[0] += DPS[i][j];
                } else
                    DPS[i][j] = 0;
            }
        }
        for (l = 1; l < p; l++) {
            kernel_mat[l] = 0;
            for (j = 0; j <= m; j++)
                DP[l - 1][j] = 0.0f;
            for (i = 0; i <= n; i++)
                DP[i][l - 1] = 0.f;

            for (i = l; i <= n; i++)
                for (j = l; j <= m; j++) {
                    DP[i][j] = DPS[i][j] + lambda * DP[i - 1][j] + lambda
                            * DP[i][j - 1] - lambda2 * DP[i - 1][j - 1];

                    if (Sx.get(i - 1).getLabel().getSurfaceForm().equals(Sz.get(j - 1).getLabel().getSurfaceForm())) {
                        DPS[i][j] = ptkDeltaFunction(Sx.get(i - 1),
                                Sz.get(j - 1))
                                * DP[i - 1][j - 1];
                        kernel_mat[l] += DPS[i][j];
                    }
                }
        }

        K = 0;
        for (l = 0; l < p; l++) {
            K += kernel_mat[l];
        }

        recursion_id--;
        return K;
    }

    @Override
    public Set<Feature> getFeatures(Pair<Tree<Constituent>, Tree<Constituent>> c) throws EdisonException {
        Set<Feature> features = new LinkedHashSet<>();
        float result = evaluateKernelNotNormalize(c.getFirst(), c.getSecond());
        features.add(new RealFeature(this.getName(), result));
        return features;
    }

    @Override
    public String getName() {
        return "par-tree-ker";
    }
}
