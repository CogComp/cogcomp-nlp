package edu.illinois.cs.cogcomp.utils;

import org.apache.commons.lang3.NotImplementedException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by stephen on 9/24/15.
 */
public class SparseDoubleVector<TKey> extends HashMap<TKey, Double> implements Serializable,Iterable  {


//public class SparseDoubleVector<TKey> : IDictionary<TKey, double>, ICollection<KeyValuePair<TKey, double>>, IEnumerable<KeyValuePair<TKey, double>>, IEnumerable

    public SparseDoubleVector(){
        throw new NotImplementedException("not yet implemented...");
    }

    public SparseDoubleVector(Map<TKey, Double> dictionary){
        throw new NotImplementedException("not yet implemented...");
    }

    public SparseDoubleVector(int capacity){
        throw new NotImplementedException("not yet implemented...");
    }

    public SparseDoubleVector(SparseDoubleVector<TKey> vector){
        throw new NotImplementedException("not yet implemented...");
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
        throw new NotImplementedException("not yet implemented...");
    }

    public SparseDoubleVector<TKey> Abs(){
        throw new NotImplementedException("not yet implemented...");
    }

    public void put(SparseDoubleVector<TKey> values){
        throw new NotImplementedException("not yet implemented...");
    }

    public void put(double coefficient, SparseDoubleVector<TKey> values){
        throw new NotImplementedException("not yet implemented...");
    }

    public void put(TKey key, double value){
        throw new NotImplementedException("not yet implemented...");
    }

    public SparseDoubleVector<TKey> Ceiling(){
        throw new NotImplementedException("not yet implemented...");
    }

    public void Clear(){
        throw new NotImplementedException("not yet implemented...");
    }

    public boolean ContainsKey(TKey key){
        throw new NotImplementedException("not yet implemented...");
    }

    @Override
    public boolean equals(Object obj){
        throw new NotImplementedException("not yet implemented...");
    }


    @Override
    public int hashCode(){
        throw new NotImplementedException("not yet implemented...");
    }

    public SparseDoubleVector<TKey> Exp(){
        throw new NotImplementedException("not yet implemented...");
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

    public double PNorm(double p){
        throw new NotImplementedException("not yet implemented...");
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

    @Override
    public Iterator<TKey> iterator() {
        throw new NotImplementedException("not yet implemented...");
    }


    // Won't implement this.
    //public boolean TryGetValue(TKey key, out double value){}

}
