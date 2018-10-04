/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.wikifier.utils.datastructure;

import gnu.trove.map.hash.TCustomHashMap;
import gnu.trove.set.hash.TCustomHashSet;
import gnu.trove.strategy.HashingStrategy;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

/**
 * A much more compact map than the default hash map
 * @author cheng88
 *
 * @param <T>
 */
public class StringMap<T> extends TCustomHashMap<String,T> implements Serializable{

    public static final float DEFAULT_LOAD_FACTOR = 0.6f;
    private static final HashFunction defaultHashFunction = Hashing.murmur3_32();
    private static final HashingStrategy<String> stringHashStrat = new HashingStrategy<String>() {

        /**
         *
         */
        private static final long serialVersionUID = -7000292520586925123L;

        @Override
        public int computeHashCode(String arg0) {
            return defaultHashFunction.hashString(arg0).asInt();
        }

        @Override
        public boolean equals(String arg0, String arg1) {
            return StringUtils.equals(arg0, arg1);
        }

    };

    public StringMap(){
        super(stringHashStrat,10,DEFAULT_LOAD_FACTOR);
    }

    public StringMap(int capacity){
        super(stringHashStrat,(int) Math.floor(capacity/DEFAULT_LOAD_FACTOR),DEFAULT_LOAD_FACTOR);
    }
    
    /**
     * Compacts itself upon finishing
     * @param map
     */
    public StringMap(Map<String, T> map) {
        super(stringHashStrat,map.size(),DEFAULT_LOAD_FACTOR);
        this.putAll(map);
        compact();
    }

    private void writeObject(ObjectOutputStream oos)
            throws IOException {
                compact();
                writeExternal(oos);
            }
    
    private void readObject(ObjectInputStream ois)
            throws ClassNotFoundException, IOException {
                readExternal(ois);
            }

    /**
     * Better performance set
     * @author cheng88
     *
     */
    public static class StringSet extends TCustomHashSet<String> implements Set<String>{
        
        public StringSet(int capacity){
            super(stringHashStrat,(int) Math.floor(capacity/DEFAULT_LOAD_FACTOR),DEFAULT_LOAD_FACTOR);
        }
        
        public StringSet(Collection<? extends String> coll){
            super(stringHashStrat,coll);
        }
        
        public StringSet(){
            this(10);
        }
    }

}
