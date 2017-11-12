package edu.illinois.cs.cogcomp.edison.features.kernels.tree;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.RealFeature;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
 * Partial Tree Kernel implementation.
 * <p>
 * A Partial Tree Kernel is a convolution kernel. The kernel function is defined
 * as: </br>
 * <p>
 * \(K(T_1,T_2) = \sum_{n_1 \in N_{T_1}} \sum_{n_2 \in N_{T_2}}
 * \Delta(n_1,n_2)\)
 * <p>
 * </br> where \(\Delta(n_1,n_2)=\sum^{|F|}_i=1 I_i(n_1) I_i(n_2)\), that is the
 * number of common fragments rooted at the n1 and n2. It can be computed as:
 * </br> - if the involved node are leaves then
 * \(\Delta_{\sigma}(n_1,n_2)=\mu\lambda\sigma(n_1,n_2)\) </br> - else \( *
 * \Delta_{\sigma}(n_1,n_2)= \displaystyle \mu \sigma(n_1,n_2)\times \Big
 * (\lambda^2 + \hspace{-2em}
 * \sum_{\vec{I}_1,\vec{I}_2,l(\vec{I}_1)=l(\vec{I}_2)}
 * \lambda^{d(\vec{I}_1)+d(\vec{I}_2)} \prod_{j=1}^{l(\vec{I}_1)}
 * \Delta_{\sigma}(c_{n_1}({\vec{I}_{1j}}),c_{n_2}({\vec{I}_{2j})})\Big) \)
 * <p>
 * </br> where:
 * <p>
 * </br> - \(\sigma(n_1, n_2)\) is any similarity function between nodes \(n_1\)
 * and \(n_2\), e.g., between their lexical labels;
 * <p>
 * </br> - \(\vec I_1\) and \(\vec I_2\) denote two sequences of indexes, i.e.,
 * \(\vec I = (i_1, i_{2},..,l(I))\), with \(1 \leq i_1 < i_2 < ..< i_{l(I)} \);
 * <p>
 * <p>
 * </br> - \({d(\vec{I}_1)} = \vec{I}_{1l(\vec{I}_1)} - \vec{I}_{11}+1$ and
 * $d(\vec{I}_2)= \vec{I}_{2l(\vec{I}_2)} -\vec{I}_{21}+1$;
 * <p>
 * </br> - \(c_{n_{1}}(h)\) is the \(h^{th}\) child of the node \(n_{1}\); </br>
 * - \(\lambda, \mu \in [0,1]\) are decay factors to penalize the contribution
 * of large sized fragments.
 * <p>
 * <p>
 * </br></br> For more details </br> [Croce et al(2011)] Croce D., Moschitti A.,
 * Basili R. (2011) Structured lexical similarity via convolution kernels on
 * dependency trees. In: Proceedings of EMNLP, Edinburgh, Scotland, UK.
 *
 * @author Danilo Croce, Giuseppe Castellucci
 * @author Daniel K
 */
public class SmoothedPartialTreeKernel implements FeatureExtractor<Pair<Tree<Constituent>, Tree<Constituent>>> {

    private Logger logger = LoggerFactory.getLogger(SmoothedPartialTreeKernel.class);

    private int MAX_CHILDREN = 20;
    private int MAX_RECURSION = 20;

    /**
     * Vertical Decay Factor
     */
    private float mu = 0.4f;
    /**
     * Horizontal Decay factor
     */
    private float lambda = 0.4f;

    /**
     * Horizontal Decay factor, pow 2
     */
    private float lambda2;

    /**
     * Multiplicative factor to scale up/down the leaves contribution
     */
    private float terminalFactor = 1;

    /**
     * All similarity score below this threshold are ignored
     */
    private float similarityThreshold = 0.01f;

    /**
     * The similarity function between tree nodes
     */
    public float sim(Constituent c1, Constituent c2) {
        // override this with something more sophisticated
        return c1.getSurfaceForm().equals(c2.getSurfaceForm()) ? 1f : 0f;
    }

    /**
     * The delta matrix, used to cache the delta functions applied to subtrees
     */
    Map<Integer, Float> deltaMatrix = new HashMap<>();

    /**
     * Maximum length of common subsequences considered in the recursion. It
     * reflects the maximum branching factor allowed to the tree fragments.
     */
    private int maxSubseqLeng = MAX_CHILDREN;

    private int recursion_id = 0;

    private static final float NO_RESPONSE = -1f;

    private float[][] kernel_mat_buffer = new float[MAX_RECURSION][MAX_CHILDREN];
    private float[][][] DPS_buffer = new float[MAX_RECURSION][MAX_CHILDREN + 1][MAX_CHILDREN + 1];
    private float[][][] DP_buffer = new float[MAX_RECURSION][MAX_CHILDREN + 1][MAX_CHILDREN + 1];

    public SmoothedPartialTreeKernel() {
    }

    /**
     * @param LAMBDA              Horizontal Decay factor
     * @param MU                  Vertical Decay Factor
     * @param terminalFactor      Multiplicative factor to scale up/down the leaves contribution
     * @param similarityThreshold All similarity score below this threshold are ignored
     */
    public SmoothedPartialTreeKernel(float LAMBDA, float MU,
                                     float terminalFactor, float similarityThreshold) {
        this.lambda = LAMBDA;
        this.lambda2 = LAMBDA * LAMBDA;
        this.mu = MU;
        this.terminalFactor = terminalFactor;
        this.similarityThreshold = similarityThreshold;
    }

    /**
     * Determine the subtrees (from the two trees) whose root have a similarity
     * score above a given threshold
     *
     * @param a First Tree
     * @param b Second Tree
     * @return The node pairs having the similarity score above @param
     * similarityThreshold
     */
    private ArrayList<Pair<Tree<Constituent>, Tree<Constituent>>> determineSubList(Tree<Constituent> a, Tree<Constituent> b) {
        ArrayList<Pair<Tree<Constituent>, Tree<Constituent>>> intersect = new ArrayList<>();
        int i = 0, j = 0;
        int n_a, n_b;
        List<Tree<Constituent>> list_a, list_b;

        list_a = a.getChildren();
        list_b = b.getChildren();
        n_a = list_a.size();
        n_b = list_b.size();

        float sim;
        for (i = 0; i < n_a; i++) {
            for (j = 0; j < n_b; j++) {
                sim = sim(list_a.get(i).getLabel(), list_b.get(j).getLabel());

                if (sim >= similarityThreshold) {
                    intersect.add(new Pair<>(list_a.get(i), list_b
                            .get(j)));
                    deltaMatrix.put(PartialTreeKernel.getHash(list_a.get(i).hashCode(), list_b.get(j)
                            .hashCode()), NO_RESPONSE);
                } else {
                    deltaMatrix.put(PartialTreeKernel.getHash(list_a.get(i).hashCode(), list_b.get(j)
                            .hashCode()), 0f);
                }
            }
        }
        return intersect;
    }

    /**
     * Evaluate the Smoothed Partial Tree Kernel
     *
     * @param a First tree
     * @param b Second Tree
     * @return Kernel value
     */
    private float evaluateKernelNotNormalize(Tree<Constituent> a,
                                             Tree<Constituent> b) {

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
            logger.warn("Increasing the size of cache matrices to host trees with height=" + MAX_RECURSION
                    + " and maxBranchingFactor=" + MAX_CHILDREN + "");
            kernel_mat_buffer = new float[MAX_RECURSION][MAX_CHILDREN];
            DPS_buffer = new float[MAX_RECURSION][MAX_CHILDREN][MAX_CHILDREN];
            DP_buffer = new float[MAX_RECURSION][MAX_CHILDREN][MAX_CHILDREN];
        }
		/*
		 * End of the check
		 */

        ArrayList<Pair<Tree<Constituent>, Tree<Constituent>>> pairs = determineSubList(a, b);

        float sum = 0;

        for (int i = 0; i < pairs.size(); i++) {
            sum += sptkDeltaFunction(pairs.get(i).getFirst(), pairs.get(i).getSecond());
        }

        return sum;
    }

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
     * @return The similarity threshold. All similarity score below this
     * threshold are ignored
     */
    public float getSimilarityThreshold() {
        return similarityThreshold;
    }

    /**
     * @return Multiplicative factor to scale up/down the leaves contribution
     */
    public float getTerminalFactor() {
        return terminalFactor;
    }


    /**
     * @param lambda Horizontal Decay factor
     */
    public void setLambda(float lambda) {
        this.lambda = lambda;
        this.lambda2 = this.lambda * this.lambda;
    }

    /**
     * @param mu Vertical Decay Factor
     */
    public void setMu(float mu) {
        this.mu = mu;
    }

    /**
     * @param similarityThreshold All similarity score below this threshold are ignored
     */
    public void setSimilarityThreshold(float similarityThreshold) {
        this.similarityThreshold = similarityThreshold;
    }

    /**
     * @param terminalFactor Multiplicative factor to scale up/down the leaves contribution
     */
    public void setTerminalFactor(float terminalFactor) {
        this.terminalFactor = terminalFactor;
    }

    /**
     * The Smoothed String Kernel formulation, that recursively estimates the
     * partial overal between children sequences.
     *
     * @param Sx childer of the first subtree
     * @param Sz childer of the second subtree
     * @return string kernel score
     */
    private float smoothedStringKernelDeltaFunction(List<Tree<Constituent>> Sx,
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
        if (m < n)
            p = m;

        if (p > maxSubseqLeng)
            p = maxSubseqLeng;

        float temp;
        kernel_mat[0] = 0;
        for (i = 1; i <= n; i++) {
            for (j = 1; j <= m; j++) {
                temp = sptkDeltaFunction(Sx.get(i - 1), Sz.get(j - 1));
                // temp = Delta_PT(*(Sx + i - 1), *(Sz + j - 1)) * node_sim(*Sx,
                // *Sz);
                if (temp != NO_RESPONSE) {
                    DPS[i][j] = temp;
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
                DP[i][l - 1] = 0.0f;
            for (i = l; i <= n; i++)
                for (j = l; j <= m; j++) {
                    DP[i][j] = DPS[i][j] + lambda * DP[i - 1][j] + lambda
                            * DP[i][j - 1] - lambda2 * DP[i - 1][j - 1];

                    temp = sptkDeltaFunction(Sx.get(i - 1), Sz.get(j - 1));
                    // temp = Delta_PT(*(Sx + i - 1), *(Sz + j - 1)) *
                    // node_sim(*Sx, *Sz);
                    if (temp != NO_RESPONSE) {
                        DPS[i][j] = temp * DP[i - 1][j - 1];
                        kernel_mat[l] += DPS[i][j];
                    } // else DPS[i][j] = 0;
                }
        }
        // K=kernel_mat[p-1];
        K = 0;
        for (l = 0; l < p; l++) {
            K += kernel_mat[l];
            // printf("String kernel of legnth %d: %1.7f \n\n",l+1,kernel_mat[l]);
        }
        recursion_id--;
        return K;
    }

    /**
     * Smoothed Partial Tree Kernel Delta Function
     *
     * @param Nx root of the first tree
     * @param Nz root of the second tree
     */
    private float sptkDeltaFunction(Tree<Constituent> Nx, Tree<Constituent> Nz) {
        if (deltaMatrix.get(PartialTreeKernel.getHash(Nx.hashCode(), Nz.hashCode())) != NO_RESPONSE) {
            return deltaMatrix.get(PartialTreeKernel.getHash(Nx.hashCode(), Nz.hashCode())); // already there
        }
        float sum = 0;
        float sim = sim(Nx.getLabel(), Nz.getLabel());

        if (sim < similarityThreshold) {
            deltaMatrix.put(PartialTreeKernel.getHash(Nx.hashCode(), Nz.hashCode()), 0f);
            return 0;
        } else {
            if (Nx.getNumberOfChildren() == 0 || Nz.getNumberOfChildren() == 0) {
                float val = mu * lambda2 * terminalFactor * sim;
                deltaMatrix.put(PartialTreeKernel.getHash(Nx.hashCode(), Nz.hashCode()), val);
                return val;
            } else {
                sum = sim
                        * mu
                        * (lambda2 + smoothedStringKernelDeltaFunction(Nx.getChildren(), Nz.getChildren()));
                deltaMatrix.put(PartialTreeKernel.getHash(Nx.hashCode(), Nz.hashCode()), sum);
                return sum;
            }
        }
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

    @Override
    public Set<Feature> getFeatures(Pair<Tree<Constituent>, Tree<Constituent>> c) throws EdisonException {
        Set<Feature> features = new LinkedHashSet<>();
        float result = evaluateKernelNotNormalize(c.getFirst(), c.getSecond());
        features.add(new RealFeature(this.getName(), result));
        return features;
    }

    @Override
    public String getName() {
        return null;
    }
}