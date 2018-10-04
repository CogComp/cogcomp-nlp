/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.bigdata.mapdb;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;

import org.mapdb.BTreeMap;
import org.mapdb.DB.BTreeMapMaker;
import org.mapdb.DBMaker;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.collect.Ordering;


/**
 * <a href="http://www.mapdb.org/">MapDB</a> interface, a db location hosting multiple customized
 * disk-backed maps
 * @author cheng88
 * 
 */
public final class MapDB{
    
    private static final Logger logger = LoggerFactory.getLogger(MapDB.class);
    /**
     * Simple abstraction for the reverse sorting
     * requirement for batch insertion
     * @author cheng88
     *
     * @param <K>
     * @param <V>
     */
    public static class Entries<K,V>{
        private final List<Tuple2<K,V>> entries = new ArrayList<Tuple2<K,V>>();
        private Entries(){}
        public void add(K key,V value){
            entries.add(Fun.t2(key, value));
        }
        public Iterator<Tuple2<K,V>> batchInsertIterator(){
            Collections.sort(entries,Fun.TUPLE2_COMPARATOR);
            Collections.reverse(entries);
            return entries.iterator();
        }
        public static <K,V> Entries<K,V> create(){
            return new Entries<K, V>();
        }
    }

    public interface DBConfig{
        
        /**
         * Only closes DB on shutdown
         */
        public static final DBConfig VOID = new DBConfig(){
            @Override
            public DBMaker<?> configure(DBMaker<?> maker) {
                return maker.closeOnJvmShutdown();
            }
        };
        
        /**
         * sacrifices memory and disk for the fastest write speed possible, useful for single-threaded processes
         * Enables mode where all modifications are queued and written into disk on Background Writer Thread
         */
        public static final DBConfig SINGLE_THREAD_WRITE = new DBConfig(){
            @Override
            public DBMaker<?> configure(DBMaker<?> maker) {
                return maker.asyncWriteEnable();
            }
        };
        
        public static final DBConfig READ_ONLY = new DBConfig(){
            @Override
            public DBMaker<?> configure(DBMaker<?> maker) {
                return maker.cacheLRUEnable().readOnly();
            }
        };
        
        /**
         * Disables cache and read-only
         */
        public static final DBConfig SCAN = new DBConfig(){
            @Override
            public DBMaker<?> configure(DBMaker<?> maker) {
                return maker.cacheDisable().readOnly();
            }
        };
        
        public static final DBConfig MULTITHREAD_WRITE = new DBConfig(){
            @Override
            public DBMaker<?> configure(DBMaker<?> maker) {
                return maker.cacheDisable();
            }
        };

        public DBMaker<?> configure(DBMaker<?> maker);
    }
    
    /**
     * Creates a DB for some enumeration
     * @param loc
     * @param e
     * @return
     */
    public static DBMaker<?> newDefaultDb(String loc,Enum<?> e){
        return newDefaultDb(loc, e.name());
    }

    /**
     * Creates a db for a given name under
     * location loc
     * @param loc
     * @param name
     * @return
     */
    public static DBMaker<?> newDbCollection(String loc,String name){
        return newDefaultDb(new File(loc,name).getPath(), name);
    }
    
    /**
     * Moderate space saving;
     * mmap file;
     * LRU instance cache;
     * close on shutdown;
     * write-through;
     * @param loc
     * @param name
     * @return
     */
    public static synchronized DBMaker<?> newDefaultDb(String loc,String name){
        File dbFile = new File(loc);
        if (!dbFile.exists())
            dbFile.mkdirs();
        dbFile = new File(dbFile, "mapdb");
        logger.info("Opening DB "+name+" at " + dbFile);
        // Storing gigantic maps in this temporary DB
        return DBMaker.newFileDB(dbFile).freeSpaceReclaimQ(3)
                .mmapFileEnablePartial()
                .transactionDisable()
                .cacheLRUEnable()
                .closeOnJvmShutdown();
    }

    public static synchronized DBMaker<?> SafeDefaultDb(String loc,String name){
        File dbFile = new File(loc);
        if (!dbFile.exists())
            dbFile.mkdirs();
        dbFile = new File(dbFile, "mapdb");
        logger.info("Opening DB "+name+" at " + dbFile);
        // Storing gigantic maps in this temporary DB
        return DBMaker.newFileDB(dbFile).freeSpaceReclaimQ(3)
                .mmapFileEnablePartial()
                .cacheLRUEnable()
                .closeOnJvmShutdown();
    }
    public static <K extends Comparable<K>, V> BTreeMap<K, V> batchCreate(final Map<K, V> map,
            BTreeMapMaker maker) {
        List<K> keys = Ordering.natural().reverse().sortedCopy(map.keySet());
        return maker.pumpSource(keys.iterator(), new Fun.Function1<V, K>() {
            @Override
            public V run(K a) {
                return map.get(a);
            }
        }).make();
    };
    
    public static <K extends Comparable<K>, V> BTreeMap<K, V> batchCreate(final NavigableMap<K, V> map,
            BTreeMapMaker maker) {
        if(map.keySet().iterator().next() instanceof String)
            throw new IllegalArgumentException("Unicode String comparator is inconsistent.");
        return maker.pumpSource(Iterators.transform(
                map.descendingMap().entrySet().iterator(),
                new Function<Entry<K,V>,Tuple2<K,V>>(){
                    public Tuple2<K,V> apply(Entry<K,V> e){
                        return new Tuple2<K,V>(e.getKey(), e.getValue());
                    }
                })).make();
    };
    
    public static <K extends Comparable<K>, V> BTreeMap<K, V> batchCreate(final Entries<K, V> entries,
            BTreeMapMaker maker) {
        return maker.pumpSource(entries.batchInsertIterator()).make();
    };
    
    
}
