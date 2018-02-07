package edu.illinois.cs.cogcomp.finetyper.wsd.math;

/**
 * Created by haowu4 on 1/13/17.
 */
public class FloatDenseVector {
    float[] data;

    public FloatDenseVector(int dim) {
        this(new float[dim]);
    }

    public FloatDenseVector(float[] data) {
        this.data = data;
    }

    /**
     * Copy constructor.
     *
     * @param other
     */
    public FloatDenseVector(FloatDenseVector other) {
        float[] k = other.getDataReference();
        this.data = new float[k.length];
        for (int i = 0; i < k.length; i++) {
            this.data[i] = k[i];
        }
    }

    /**
     * Return the reference of the internal float array.
     *
     * @return reference of its data.
     */
    public float[] getDataReference() {
        return data;
    }

    /**
     * Inplace add another dense vector.
     *
     * @param v vector to add.
     */
    public void iadd(FloatDenseVector v) {
        float[] dv = v.getDataReference();
        for (int i = 0; i < dv.length; i++) {
            this.data[i] += dv[i];
        }
    }

    /**
     * Inplace divide by a constant scalar.
     *
     * @param v constant to divided by.
     */
    public void idivide(float v) {
        for (int i = 0; i < data.length; i++) {
            this.data[i] /= v;
        }
    }

    /**
     * Inplace subtract another vector.
     *
     * @param v constant to subtract.
     */
    public void isub(FloatDenseVector v) {
        float[] dv = v.getDataReference();
        for (int i = 0; i < dv.length; i++) {
            this.data[i] -= dv[i];
        }
    }
}
