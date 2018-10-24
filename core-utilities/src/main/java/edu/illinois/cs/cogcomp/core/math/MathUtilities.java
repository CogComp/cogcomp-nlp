/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.math;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static edu.illinois.cs.cogcomp.core.utilities.ArrayUtilities.asDoubleList;
import static java.lang.Math.*;
import static java.util.Arrays.asList;

/**
 * Some math utilities that are not present in the standard math library.
 *
 * @author Vivek Srikumar
 */

public class MathUtilities {

    /**
     * Find the argmax and max in a double array. If the array is empty, the function returns (-1,
     * Double.NEGATIVE_INFINITY)
     */
    public static Pair<Integer, Double> max(double[] array) {
        double max = Double.NEGATIVE_INFINITY;
        int argMin = -1;

        int id = 0;
        for (double d : array) {
            if (d > max) {
                max = d;
                argMin = id;
            }

            id++;
        }

        return new Pair<>(argMin, max);
    }

    /**
     * Find the argmax and max in an int array. If the array is empty, the function returns (-1,
     * Integer.MIN_VALUE)
     */
    public static Pair<Integer, Integer> max(int[] array) {
        int max = Integer.MIN_VALUE;
        int argMin = -1;

        int id = 0;
        for (int d : array) {
            if (d > max) {
                max = d;
                argMin = id;
            }

            id++;
        }

        return new Pair<>(argMin, max);
    }

    /**
     * Find the argmax and max in a list of elements that can are ordered. If the list is empty, the
     * function returns (-1, null).
     */
    public static <T extends Comparable<T>> Pair<Integer, T> max(Collection<T> list) {
        T max = null;
        int argMax = -1;

        if (list.size() > 0) {
            max = list.iterator().next();
            argMax = 0;
        }
        int id = 0;
        for (T d : list) {
            if (d.compareTo(max) > 0) {
                max = d;
                argMax = id;
            }
            id++;
        }

        return new Pair<>(argMax, max);
    }

    /**
     * Find the argmax and max in a array of elements that can are ordered. If the list is empty,
     * the function returns (-1, null).
     */
    public static <T extends Comparable<T>> Pair<Integer, T> max(T[] array) {
        return max(asList(array));
    }

    /**
     * Find the argmax and max in a map from items to values. If the list is empty, the function
     * returns (null, null).
     *
     * @param <T> represents items
     * @param <S> represents values, should be {@code Comparable}
     * @return Pair of (argmax, max)
     */
    public static <T, S extends Comparable<S>> Pair<T, S> max(Map<T, S> map) {
        S max = null;
        T argMax = null;

        if (map.size() > 0) {
            argMax = map.keySet().iterator().next();
            max = map.get(argMax);
        }

        for (Entry<T, S> entry : map.entrySet()) {
            T item = entry.getKey();
            S value = entry.getValue();

            if (value.compareTo(max) > 0) {
                max = value;
                argMax = item;
            }
        }
        return new Pair<>(argMax, max);
    }

    /**
     * Find the argmin and min in a double array. If the array is empty, the function returns (-1,
     * Double.POSITIVE_INFINITY)
     */
    public static Pair<Integer, Double> min(double[] array) {
        double min = Double.POSITIVE_INFINITY;
        int argMin = -1;

        int id = 0;
        for (double d : array) {
            if (d < min) {
                min = d;
                argMin = id;
            }

            id++;
        }

        return new Pair<>(argMin, min);
    }

    /**
     * Find the argmin and min in a int array. If the array is empty, the function returns (-1,
     * Integer.MAX_VALUE)
     *
     * @return A pair of integers containing (argMin, min).
     */
    public static Pair<Integer, Integer> min(int[] array) {
        int min = Integer.MAX_VALUE;
        int argMin = -1;

        int id = 0;
        for (int d : array) {
            if (d < min) {
                min = d;
                argMin = id;
            }

            id++;
        }

        return new Pair<>(argMin, min);
    }

    /**
     * Find the argmin and min in a list of elements that can are ordered. If the list is empty, the
     * function returns (-1, null).
     */
    public static <T extends Comparable<T>> Pair<Integer, T> min(Collection<T> list) {
        T min = null;
        int argMin = -1;

        if (list.size() > 0) {
            min = list.iterator().next();
            argMin = 0;
        }
        int id = 0;
        for (T d : list) {
            if (d.compareTo(min) < 0) {
                min = d;
                argMin = id;
            }
            id++;
        }

        return new Pair<>(argMin, min);
    }

    /**
     * Find the argmin and min in a map from items to values. If the list is empty, the function
     * returns (null, null).
     *
     * @param <T> represents items
     * @param <S> represents values, should be {@code Comparable}
     * @return Pair of (argmin, min)
     */
    public static <T, S extends Comparable<S>> Pair<T, S> min(Map<T, S> map) {
        S max = null;
        T argMax = null;

        if (map.size() > 0) {
            argMax = map.keySet().iterator().next();
            max = map.get(argMax);
        }

        for (T item : map.keySet()) {
            S value = map.get(item);

            if (value.compareTo(max) < 0) {
                max = value;
                argMax = item;
            }
        }
        return new Pair<>(argMax, max);
    }

    /**
     * Find the argmin and min in a array of elements that can are ordered. If the list is empty,
     * the function returns (-1, null).
     */
    public static <T extends Comparable<T>> Pair<Integer, T> min(T[] array) {
        return min(asList(array));
    }

    /**
     * Multiply a list of doubles with a scalar
     */
    public static List<Double> multiply(List<Double> l, double m) {
        List<Double> out = new ArrayList<>();

        for (double d : l) {
            out.add(d * m);
        }
        return out;
    }

    /**
     * Elementwise multiplication of two lists of doubles. Both lists should be equally long. The
     * output is also a list that has the same length, where each element is the product of the
     * corresponding elements of the input lists.
     */
    public static List<Double> multiply(List<Double> d1, List<Double> d2) {
        assert d1.size() == d2.size();

        List<Double> out = new ArrayList<>();
        for (int i = 0; i < d1.size(); i++) {
            out.add(d1.get(i) * d2.get(i));
        }
        return out;

    }

    /**
     * Elementwise multiplication of two double arrays. Both arrays should be equally long. The
     * output is an array of the same length.
     */
    public static double[] multiply(double[] d1, double[] d2) {
        assert d1.length == d2.length;
        double[] out = new double[d1.length];

        for (int i = 0; i < d1.length; i++) {
            out[i] = d1[i] * d2[i];
        }
        return out;

    }

    /**
     * Multiply all elements of a double array with a scalar.
     */
    public static double[] multiply(double[] d, double m) {
        double[] out = new double[d.length];

        for (int i = 0; i < d.length; i++) {
            out[i] = d[i] * m;
        }
        return out;
    }

    /**
     * Efficient computation of softmax. This does not create overflow errors because of
     * exponentiation
     */
    public static double[] softmax(double[] input) {
        double min = min(input).getSecond();

        double denominator = 0d;

        for (double a : input) {
            denominator += exp(a - min);
        }

        double[] output = new double[input.length];

        int i = 0;
        for (double a : input) {
            output[i++] = exp(a - min) / denominator;
        }

        return output;
    }

    public static double[] softmax(double[] input, double[] multiplier) {
        return softmax(multiply(input, multiplier));
    }

    /**
     * Efficient computation of softmax. This does not create overflow errors because of
     * exponentiation
     */
    public static List<Double> softmax(List<Double> input) {
        double min = min(input).getSecond();

        double denominator = 0d;
        for (double d : input)
            denominator += exp(d - min);

        List<Double> output = new ArrayList<>();
        for (double a : input)
            output.add(exp(a - min) / denominator);

        return output;
    }

    public static List<Double> softmax(List<Double> input, List<Double> multiplier) {
        return softmax(multiply(input, multiplier));
    }

    /**
     * An attempt to fix the value of epsilon used across different applications.
     */
    public final static double EPSILON = 1e-7;

    /**
     * Check equality of two doubles upto
     * {@link edu.illinois.cs.cogcomp.core.math.MathUtilities#EPSILON}
     */
    public static boolean epsilonEquals(double d1, double d2) {
        return abs(d1 - d2) < EPSILON;
    }

    /**
     * Add two numbers in log space.
     * <p>
     * Suppose y1 and y2 are two positive reals and let x1 = log(y1) and x2 = log(y2). This function
     * (and the other versions of logAdd) computes log(exp(x1) + exp(x2)) = log(y1 + y2) without
     * underflow or overflow errors. This can be used, for example, to maintain log probabilities
     * without worrying about the probabilities getting too small.
     */
    public static double logAdd(double x1, double x2) {
        if (x1 == Double.NEGATIVE_INFINITY)
            return x2;
        else if (x2 == Double.NEGATIVE_INFINITY)
            return x1;
        else if (x1 > x2)
            return x2 + log(1 + exp(x1 - x2));
        else
            return x1 + log(1 + exp(x2 - x1));
    }

    /**
     * Add an array of numbers in log space
     */
    public static double logAdd(double[] doubles) {
        double max = max(doubles).getSecond();

        if (max == Double.NEGATIVE_INFINITY)
            return max;

        double l = 0;
        for (double d : doubles) {
            if (d == Double.NEGATIVE_INFINITY)
                continue;

            l += exp(d - max);
        }

        if (l > 0)
            return max + log(l);
        else
            return max;
    }

    /**
     * Add a collection of numbers in log space
     */
    public static double logAdd(Collection<Double> doubles) {
        double max = max(doubles).getSecond();

        if (max == Double.NEGATIVE_INFINITY)
            return max;

        double l = 0;
        for (double d : doubles) {
            if (d == Double.NEGATIVE_INFINITY)
                continue;
            l += exp(d - max);
        }

        if (l > 0)
            return max + log(l);
        else
            return max;
    }

    private static double[] gammaCof = new double[] {76.18009172947146, -86.50532032941677,
            24.01409824083091, -1.231739572450155, 0.1208650973866179e-2, -0.5395239384953e-5};

    /**
     * Computes log(gamma(xx)). From numerical recipies. This just works.
     */
    public static double lnGamma(double xx) {
        double x = xx;
        double y = xx;
        double tmp = x + 5.5;

        tmp -= (x + 0.5) * log(tmp);
        double ser = 1.000000000190015;
        for (double aGammaCof : gammaCof) {
            y++;
            ser += aGammaCof / y;
        }

        return -tmp + log(2.5066282746310005 * ser / x);

    }

    private static List<Double> factorialsBuffer = asDoubleList(new double[] {0.0, 0.0,
            0.6931471805599453, 1.791759469228055, 3.1780538303479458});

    /**
     * Get the log of the factorial of a number. Upto 32, the factorial is exact. After that, the
     * gamma function is used because the numbers can't be represented anyway.
     */
    public static double lnFactorial(int x) {
        if (x < 0)
            throw new IllegalArgumentException("Invalid input to factorial: " + x);
        else if (x > 32)
            return lnGamma(x + 1);
        else {
            int l = factorialsBuffer.size();
            for (int i = l; i <= x; i++) {
                factorialsBuffer.add(log(i) + factorialsBuffer.get(i - 1));
            }
            return factorialsBuffer.get(x);
        }
    }

    public static int binomialCoeffs(int n, int k) {
        if (k > n)
            return 0;
        else if (n < 0)
            return 0;
        else if (k < 0)
            return 0;
        else
            return (int) floor(0.5 + exp(lnFactorial(n) - lnFactorial(k) - lnFactorial(n - k)));
    }

    /**
     * Get the Beta(x,w)
     */
    public static double beta(double x, double w) {
        return exp(lnGamma(x) + lnGamma(w) - lnGamma(x + w));
    }

    /*
     * Now, a bunch of functions to help compute the incomplete gamma function. These are directly
     * lifted from Numerical Recipies, 2nd Ed.
     */

    private static int MAX_ITERS = 100;
    private static double EPS = 3.0e-7;
    private static double FPMIN = 1.0e-30;

    private static double gcf(double x, double a) {
        double b = x + 1 - a;
        double c = 1 / FPMIN;
        double d = 1 / b;
        double h = d;
        double an;
        double del;

        for (int i = 1; i <= MAX_ITERS; i++) {
            an = -i * (i - a);
            b += 2.0;
            d = an * d + b;
            if (abs(d) < FPMIN)
                d = FPMIN;
            c = b + an / c;
            if (abs(c) < FPMIN)
                c = FPMIN;

            d = 1 / d;
            del = d * c;
            h *= del;

            if (abs(del - 1) < EPS) {
                return exp(-x + a * log(x) - lnGamma(a)) * h;
            }

        }
        throw new IllegalStateException("a = " + a + " is too large, " + MAX_ITERS
                + " is too small");
    }

    private static double gser(double x, double a) {
        if (x <= 0)
            return 0;
        else {
            double ap = a;
            double del = 1.0 / a;
            double sum = del;

            for (int n = 1; n <= MAX_ITERS; n++) {
                ap += 1;

                del *= (x / ap);
                sum += del;

                if (abs(del) < abs(sum) * EPS) {
                    return sum * exp(-x + a * log(x) - lnGamma(a));
                }
            }
            throw new IllegalStateException("a = " + a + " too large, " + MAX_ITERS + " too small.");
        }
    }

    /**
     * Get the incomplete gamma function P(a,x). This implementation is directly taken from
     * numerical recipies 2nd Ed.
     * <p>
     * Wikipedia calls this function the lower incomplete gamma function where the integral is from
     * 0 to x.
     */
    public static double incompleteGammaP(double a, double x) {

        if (x < 0 || a <= 0)
            throw new IllegalStateException(
                    "Invalid parameters for incomplete gamma: x, a should be positive");

        if (x < a + 1)
            return gser(x, a);
        else
            return 1 - gcf(x, a);
    }

    public static double incompleteGammaQ(double a, double x) {
        return 1 - incompleteGammaP(a, x);
    }

    public static double erf(double x) {
        if (x < 0)
            return -incompleteGammaP(0.5, x * x);
        else
            return incompleteGammaP(0.5, x * x);
    }

    public static double erfc(double x) {
        return 1 - erf(x);
    }

}
