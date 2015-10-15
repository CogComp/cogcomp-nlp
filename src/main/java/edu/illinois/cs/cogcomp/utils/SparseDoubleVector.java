package edu.illinois.cs.cogcomp.utils;

import org.apache.commons.lang3.NotImplementedException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * SparseDoubleVector: basically a hashmap.
 * Created by stephen on 9/24/15.
 */
public class SparseDoubleVector<TKey> extends HashMap<TKey, Double> implements Serializable  {

    public SparseDoubleVector(){
        super();
    }

    public SparseDoubleVector(Map<TKey, Double> dictionary) {
        super(dictionary);
        //this.putAll(dictionary);
    }

    public SparseDoubleVector(int capacity){
        super(capacity);
    }

    // Operators won't be implemented in Java.
    //public static SparseDoubleVector<TKey> operator -(SparseDoubleVector<TKey> vector);
    //public static SparseDoubleVector<TKey> operator -(double value, SparseDoubleVector<TKey> vector);
    //public static SparseDoubleVector<TKey> operator -(SparseDoubleVector<TKey> vector, double value);
    //public static SparseDoubleVector<TKey> operator -(SparseDoubleVector<TKey> vector1, SparseDoubleVector<TKey> vector2);
    //public static bool operator !=(SparseDoubleVector<TKey> vector1, SparseDoubleVector<TKey> vector2);
    //public static SparseDoubleVector<TKey> operator %(double value, SparseDoubleVector<TKey> vector);
    //public static SparseDoubleVector<TKey> operator %(SparseDoubleVector<TKey> vector, double value);
    //public static SparseDoubleVector<TKey> operator *(double value, SparseDoubleVector<TKey> vector);
    //public static SparseDoubleVector<TKey> operator *(SparseDoubleVector<TKey> vector, double value);
    //public static SparseDoubleVector<TKey> operator *(SparseDoubleVector<TKey> vector1, SparseDoubleVector<TKey> vector2);
    //public static SparseDoubleVector<TKey> operator /(double value, SparseDoubleVector<TKey> vector);
    //public static SparseDoubleVector<TKey> operator /(SparseDoubleVector<TKey> vector, double value);
    //public static SparseDoubleVector<TKey> operator /(SparseDoubleVector<TKey> vector1, SparseDoubleVector<TKey> vector2);
    //public static SparseDoubleVector<TKey> operator +(double value, SparseDoubleVector<TKey> vector);
    //public static SparseDoubleVector<TKey> operator +(SparseDoubleVector<TKey> vector, double value);
    //public static SparseDoubleVector<TKey> operator +(SparseDoubleVector<TKey> vector1, SparseDoubleVector<TKey> vector2);
    //public static bool operator ==(SparseDoubleVector<TKey> vector1, SparseDoubleVector<TKey> vector2);
    //public static implicit operator SparseDoubleVector<TKey>(Dictionary<TKey, double> vector);
    //public static implicit operator Dictionary<TKey, double>(SparseDoubleVector<TKey> vector);


    /**
     * SWM: created to replicate operator method
     * @param value
     * @return
     */
    public SparseDoubleVector<TKey> divide(double value){
        SparseDoubleVector<TKey> ret = new SparseDoubleVector<>();

        for(TKey t : this.keySet()){
            ret.put(t, this.get(t) / value);
        }

        return ret;
    }

    /**
     * SWM: created to replicate operator method
     * FIXME: NOT SURE IF THIS IS CORRECT.
     * @param value
     * @return
     */
    public SparseDoubleVector<TKey> divide(SparseDoubleVector<TKey> value){
        SparseDoubleVector<TKey> ret = new SparseDoubleVector<>();

        for(TKey k : this.keySet()){
            double denom = 1.0;
            if(value.containsKey(k)) {
                denom = value.get(k);
            }
            ret.put(k, this.get(k) / denom);
        }

        return ret;
    }

    /**
     * SWM: created to replicate operator method
     * @param value
     * @return
     */
    public SparseDoubleVector<TKey> multiply(double value){
        SparseDoubleVector<TKey> ret = new SparseDoubleVector<>();

        for(TKey t : this.keySet()){
            ret.put(t, this.get(t) * value);
        }

        return ret;
    }

    public SparseDoubleVector<TKey> Abs(){
        throw new NotImplementedException("not yet implemented...");
    }

    public void put(SparseDoubleVector<TKey> values){
        throw new NotImplementedException("not yet implemented...");
    }

    /**
     * Add all elements of another vector to this vector, and multiply by a coefficient.
     * @param coefficient
     * @param values
     */
    public void put(double coefficient, SparseDoubleVector<TKey> values){
        for(TKey k : values.keySet()){
            this.put(k, coefficient * values.get(k));
        }
    }


    public SparseDoubleVector<TKey> Ceiling(){
        throw new NotImplementedException("not yet implemented...");
    }

    public void Clear(){
        this.clear();
    }

    public boolean ContainsKey(TKey key){
        throw new NotImplementedException("not yet implemented...");
    }

//    @Override
//    public boolean equals(Object obj){
//        throw new NotImplementedException("not yet implemented...");
//    }
//
//
//    @Override
//    public int hashCode(){
//        throw new NotImplementedException("not yet implemented...");
//    }

    /**
     * Takes the exponential of each element in the vector.
     * @return
     */
    public SparseDoubleVector<TKey> Exp(){
        SparseDoubleVector<TKey> ret = new SparseDoubleVector<>();

        for(TKey k : this.keySet()){
            ret.put(k, Math.exp(this.get(k)));
        }
        return ret;
    }

    // Probably won't implement this.
    // public SparseDoubleVector<TKey> Filter(Predicate<TKey> inclusionPredicate){}

    public SparseDoubleVector<TKey> Floor(){
        throw new NotImplementedException("not yet implemented...");
    }


    public SparseDoubleVector<TKey> Log(){
        throw new NotImplementedException("not yet implemented...");
    }

    public SparseDoubleVector<TKey> Log(double newBase){
        throw new NotImplementedException("not yet implemented...");
    }

    public SparseDoubleVector<TKey> Max(double maximum){
        throw new NotImplementedException("not yet implemented...");
    }

    public SparseDoubleVector<TKey> Max(SparseDoubleVector<TKey> otherVector){
        throw new NotImplementedException("not yet implemented...");
    }

    public double MaxElement(){
        throw new NotImplementedException("not yet implemented...");
    }

    public SparseDoubleVector<TKey> Min(double minimum){
        throw new NotImplementedException("not yet implemented...");
    }

    public SparseDoubleVector<TKey> Min(SparseDoubleVector<TKey> otherVector){
        throw new NotImplementedException("not yet implemented...");
    }


    public double MinElement(){
        throw new NotImplementedException("not yet implemented...");
    }

    /**
     * Returns the p-norm of this vector.
     * @param p
     * @return
     */
    public double PNorm(double p){
        double ret = 0;
        for(TKey t : this.keySet()){
            ret += Math.pow(this.get(t), p);
        }

        return Math.pow(ret, 1/p);
    }

    public SparseDoubleVector<TKey> Pow(double exponent){
        throw new NotImplementedException("not yet implemented...");
    }

    public boolean Remove(TKey key){
        throw new NotImplementedException("not yet implemented...");
    }

    public void RemoveRedundantElements(){
        throw new NotImplementedException("not yet implemented...");
    }

    public SparseDoubleVector<TKey> Sign(){
        throw new NotImplementedException("not yet implemented...");
    }

//    @Override
//    public Iterator<TKey> iterator() {
//        return this.iterator();
//    }


    // Won't implement this.
    //public boolean TryGetValue(TKey key, out double value){}

}
