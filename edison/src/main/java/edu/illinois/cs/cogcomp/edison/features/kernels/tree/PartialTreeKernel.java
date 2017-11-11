package edu.illinois.cs.cogcomp.edison.features.kernels.tree;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TreeView;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.Set;

/**
 * Partial Tree Kernel implementation.
 *
 * A Partial Tree Kernel is a convolution kernel that evaluates the tree
 * fragments shared between two trees. The considered fragments are are partial
 * trees, i.e. a node and its partial descendancy (the descendancy can be
 * incomplete, i.e. a partial production is allowed). The kernel function is
 * defined as: </br>
 *
 * \(K(T_1,T_2) = \sum_{n_1 \in N_{T_1}} \sum_{n_2 \in N_{T_2}}
 * \Delta(n_1,n_2)\)
 *
 * </br> where \(\Delta(n_1,n_2)=\sum^{|F|}_i=1 I_i(n_1) I_i(n_2)\), that is the
 * number of common fragments rooted at the n1 and n2. It can be computed as:
 * </br> - if the node labels of \(n_1\) and \(n_2\) are different then
 * \(\Delta(n_1,n_2)=0\) </br> - else \(\Delta(n_1,n_2)= \mu(\lambda^2 +
 * \sum_{J_1, J_2, l(J_1)=l(J_2)} \lambda^{d(J_1)+d(J_2)} \prod_{i=1}^{l(J_1)}
 * \Delta(c_{n_1}[J_{1i}], c_{n_2}[J_{2i}])\)
 *
 * </br></br> Fore details see [Moschitti, EACL2006] Alessandro Moschitti.
 * Efficient convolution kernels for dependency and constituent syntactic trees.
 * In ECML 2006, Berlin, Germany.
 *
 * @author Danilo Croce
 * @author Giuseppe Castellucci
 * @author Daniel Khashabi
 */
public class PartialTreeKernel implements FeatureExtractor<Pair<TreeView,TreeView>> {

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


    public DeltaMatrix getDeltaMatrix() {
        return deltaMatrix;
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
     * Get the Terminal Factor
     *
     * @return
     */
    public float getTerminalFactor() {
        return terminalFactor;
    }

    /**
     * @return The maximum length of common subsequences considered in the
     *         recursion. It reflects the maximum branching factor allowed to
     *         the tree fragments.
     */
    public int getMaxSubseqLeng() {
        return maxSubseqLeng;
    }

    /**
     * @param maxSubseqLeng
     *            The maximum length of common subsequences considered in the
     *            recursion. It reflects the maximum branching factor allowed to
     *            the tree fragments.
     */
    public void setMaxSubseqLeng(int maxSubseqLeng) {
        this.maxSubseqLeng = maxSubseqLeng;
    }

    /**
     * Default constructor. It should be used only for json
     * serialization/deserialization purposes This constructor by default uses
     * lambda=0.4, mu=0.4, terminalFactor=1 and it forces to operate to the
     * represenatation whose identifier is "0".
     *
     * Please use the PartialTreeKernel(String) or
     * PartialTreeKernel(float,float,float,String) to use a Partial Tree Kernel
     * in your application.
     */
    public PartialTreeKernel() {
        this(0.4f, 0.4f, 1f, "0");
    }

    /**
     * A Constructor for the Partial Tree Kernel in which parameters can be set manually.
     * @param LAMBDA lambda value in the PTK formula
     * @param MU mu value of the PTK formula
     * @param terminalFactor terminal factor
     */
    public PartialTreeKernel(float LAMBDA, float MU, float terminalFactor) {
        this.lambda = LAMBDA;
        this.lambda2 = LAMBDA * LAMBDA;
        this.mu = MU;
        this.terminalFactor = terminalFactor;
        // this.deltaMatrix = new StaticDeltaMatrix();
    }

    /**
     * Determine the subtrees (from the two trees) whose root have the same
     * label. This optimization has been proposed in [Moschitti, EACL 2006].
     *
     * @param a
     *            First Tree
     * @param b
     *            Second Tree
     * @return The node pairs having the same label
     */
    private ArrayList<TreeNodePairs> determineSubList(TreeRepresentation a,
                                                      TreeRepresentation b) {

        ArrayList<TreeNodePairs> intersect = new ArrayList<TreeNodePairs>();

        int i = 0, j = 0, j_old, j_final;

        int cfr;
        List<TreeNode> nodesA = a.getOrderedNodeSetByLabel();
        List<TreeNode> nodesB = b.getOrderedNodeSetByLabel();
        int n_a = nodesA.size();
        int n_b = nodesB.size();

        while (i < n_a && j < n_b) {

            if ((cfr = (nodesA.get(i).getContent().getTextFromData()
                    .compareTo(nodesB.get(j).getContent().getTextFromData()))) > 0)
                j++;
            else if (cfr < 0)
                i++;
            else {
                j_old = j;
                do {
                    do {
                        intersect.add(new TreeNodePairs(nodesA.get(i), nodesB
                                .get(j)));

                        deltaMatrix.add(nodesA.get(i).getId(), nodesB.get(j)
                                .getId(), DeltaMatrix.NO_RESPONSE);

                        j++;
                    } while (j < n_b
                            && (nodesA.get(i).getContent().getTextFromData()
                            .equals(nodesB.get(j).getContent()
                                    .getTextFromData())));
                    i++;
                    j_final = j;
                    j = j_old;
                } while (i < n_a
                        && (nodesA.get(i).getContent().getTextFromData()
                        .equals(nodesB.get(j).getContent()
                                .getTextFromData())));
                j = j_final;
            }
        }

        return intersect;
    }

    /**
     * Evaluate the Partial Tree Kernel
     *
     * @param a
     *            First tree
     * @param b
     *            Second Tree
     * @return Kernel value
     */
    public float evaluateKernelNotNormalize(TreeRepresentation a,
                                            TreeRepresentation b) {

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
            logger.warn("Increasing the size of cache matrices to host trees with height=" + MAX_RECURSION
                    + " and maxBranchingFactor=" + MAX_CHILDREN + "");
            kernel_mat_buffer = new float[MAX_RECURSION][MAX_CHILDREN];
            DPS_buffer = new float[MAX_RECURSION][MAX_CHILDREN][MAX_CHILDREN];
            DP_buffer = new float[MAX_RECURSION][MAX_CHILDREN][MAX_CHILDREN];
        }
		/*
		 * End of the check
		 */

        ArrayList<TreeNodePairs> pairs = determineSubList(a, b);

        float k = 0;

        for (int i = 0; i < pairs.size(); i++) {
            k += ptkDeltaFunction(pairs.get(i).getNx(), pairs.get(i).getNz());
        }

        return k;
    }

    @Override
    public float kernelComputation(TreeRepresentation repA,
                                   TreeRepresentation repB) {
        return (float) evaluateKernelNotNormalize((TreeRepresentation) repA,
                (TreeRepresentation) repB);
    }

    /**
     * Partial Tree Kernel Delta Function
     *
     * @param Nx
     *            root of the first tree
     * @param Nz
     *            root of the second tree
     * @return
     */
    private float ptkDeltaFunction(TreeNode Nx, TreeNode Nz) {
        float sum = 0;

        if (deltaMatrix.get(Nx.getId(), Nz.getId()) != DeltaMatrix.NO_RESPONSE)
            return deltaMatrix.get(Nx.getId(), Nz.getId()); // already there

        if (!Nx.getContent().getTextFromData()
                .equals(Nz.getContent().getTextFromData())) {
            deltaMatrix.add(Nx.getId(), Nz.getId(), 0);
            return 0;
        } else if (Nx.getNoOfChildren() == 0 || Nz.getNoOfChildren() == 0) {
            deltaMatrix.add(Nx.getId(), Nz.getId(), mu * lambda2
                    * terminalFactor);
            return mu * lambda2 * terminalFactor;
        } else {
            float delta_sk = stringKernelDeltaFunction(Nx.getChildren(),
                    Nz.getChildren());

            sum = mu * (lambda2 + delta_sk);

            deltaMatrix.add(Nx.getId(), Nz.getId(), sum);
            return sum;
        }

    }


    /**
     * Sets the delta matrix. This method should not be used, as the new KeLP versions
     * are optimized to automatically set the proper delta matrix
     *
     * @param deltaMatrix
     */
    @Deprecated
    @JsonIgnore
    public void setDeltaMatrix(DeltaMatrix deltaMatrix) {
        this.deltaMatrix = deltaMatrix;
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
     * The String Kernel formulation, that recursively estimates the partial
     * overlap between children sequences.
     *
     * @param Sx
     *            children of the first subtree
     * @param Sz
     *            children of the second subtree
     *
     * @return string kernel score
     */
    private float stringKernelDeltaFunction(ArrayList<TreeNode> Sx,
                                            ArrayList<TreeNode> Sz) {

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

        kernel_mat[0] = 0;
        for (i = 1; i <= n; i++) {
            for (j = 1; j <= m; j++) {
                if ((Sx.get(i - 1).getContent().getTextFromData().equals(Sz
                        .get(j - 1).getContent().getTextFromData()))) {
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

                    if (Sx.get(i - 1)
                            .getContent()
                            .getTextFromData()
                            .equals(Sz.get(j - 1).getContent()
                                    .getTextFromData())) {
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
    public Set<Feature> getFeatures(Pair<TreeView, TreeView> c) throws EdisonException {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }
}
