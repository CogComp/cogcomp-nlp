package edu.illinois.cs.cogcomp.finetyper.wsd.math;

/**
 * Created by haowu4 on 1/13/17.
 */
public class Similarity {
    /**
     * Compute cosine similarity of two vectors.
     * You need to provide a precomputed l2norm for both vector.
     * This function is useful when you want to avoid re-computation of the l2norm of one vector.
     *
     * @param v1  First vector.
     * @param v2  Second vector.
     * @param v1n L2 Norm of first vector.
     * @param v2n L2 Norm of second vector.
     * @return Cosine similarity of v1 and v2.
     */
    public static double cosine(FloatDenseVector v1, FloatDenseVector v2, double v1n,
                                double v2n) {
        float[] v1_ = v1.getDataReference();
        float[] v2_ = v2.getDataReference();
        double r = 0;
        for (int i = 0; i < v1_.length; i++) {
            r += (v1_[i] * v2_[i]);
        }
        return r / (v1n * v2n);

    }

    /**
     * Compute cosine similarity of two vectors.
     *
     * @param v1 First vector.
     * @param v2 Second vector.
     * @return Cosine similarity of v1 and v2.
     */
    public static double cosine(FloatDenseVector v1, FloatDenseVector v2) {
        return cosine(v1, v2, l2norm(v1), l2norm(v2));
    }

    /**
     * Compute L2 l2norm of the vector.
     *
     * @param v1 Vector to compute l2norm.
     * @return L2 l2norm of the vector.
     */
    public static double l2norm(FloatDenseVector v1) {
        float[] v1_ = v1.getDataReference();
        double r = 0;
        for (int i = 0; i < v1_.length; i++) {
            r += (v1_[i] * v1_[i]);
        }
        return Math.sqrt(r);

    }
}
