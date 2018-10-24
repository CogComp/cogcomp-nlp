/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.io.caches;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.IResetableIterator;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import org.jetbrains.annotations.NotNull;
import org.mapdb.DB;
import org.mapdb.DBException;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Implements the {@link TextAnnotationCache} interface using {@code MapDB} storage.
 * The keys in the map-based DB are the hash codes of the {@link TextAnnotation#getTokenizedText()}.
 *
 * @author Christos Christodoulopoulos
 */
public class TextAnnotationMapDBHandler implements TextAnnotationCache {
    private DB db;
    private Logger logger = LoggerFactory.getLogger(TextAnnotationMapDBHandler.class);

    public TextAnnotationMapDBHandler(String dbFile) {
        try {
            // enabling transactions avoids cache corruption if service fails.
            this.db = DBMaker.fileDB(dbFile).closeOnJvmShutdown().transactionEnable().make();
        }
        catch (DBException e) {
//            logger.warn("mapdb couldn't instantiate db using file '{}': check error and either remove lock, " +
//                    "repair file, or delete file.", dbFile);
            e.printStackTrace();
            System.err.println("mapdb couldn't instantiate db using file '" + dbFile +
                    "': check error and either remove lock, repair file, or delete file.");
            throw e;
        }
    }
    /**
     * MapDB requires the database to be closed at the end of operations. This is usually handled by the
     * {@code closeOnJvmShutdown()} snippet in the initializer, but this method needs to be called if
     * multiple instances of the {@link TextAnnotationMapDBHandler} are used.
     */
    public void close() {
        db.commit();
        db.close();
    }

    /**
     * Checks if the dataset is cached in the DB.
     *
     * @param dataset The name of the dataset (e.g. "train", "test")
     * @param dbFile The name of the MapDB file
     * @return Whether the dataset exists in the DB
     */
    public boolean isCached(String dataset, String dbFile) {
        return IOUtils.exists(dbFile) && getMap(dataset).size() > 0;
    }

    @Override
    public void addTextAnnotation(String dataset, TextAnnotation ta) {
        final ConcurrentMap<Integer, byte[]> data = getMap(dataset);
        try {
            data.put(ta.getTokenizedText().hashCode(), SerializationHelper.serializeTextAnnotationToBytes(ta));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        db.commit();
    }

    @Override
    public void updateTextAnnotation(TextAnnotation ta) {
        for (String dataset : getAllDatasets()) {
            final ConcurrentMap<Integer, byte[]> data = getMap(dataset);
            try {
                data.replace(ta.getTokenizedText().hashCode(), SerializationHelper.serializeTextAnnotationToBytes(ta));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        db.commit();
    }

    @Override
    public IResetableIterator<TextAnnotation> getDataset(String dataset) {
        final Collection<byte[]> list = getMap(dataset).values();
        return new IResetableIterator<TextAnnotation>() {
            Iterator<byte[]> iterator = list.iterator();

            @Override
            public void remove() {}

            @Override
            public void reset() {
                iterator = list.iterator();
            }

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public TextAnnotation next() {
                byte[] bytes = iterator.next();
                return SerializationHelper.deserializeTextAnnotationFromBytes(bytes);
            }
        };
    }

    /**
     * checks whether ta with corresponding TEXT is in database -- not whether the same
     *    annotations are present
     * @param ta
     * @return
     */
    @Override
    public boolean contains(TextAnnotation ta) {
        boolean isContained = false;
        for (String dataset : getAllDatasets()) {
            final ConcurrentMap<Integer, byte[]> data = getMap(dataset);
            isContained |= data.containsKey(ta.getTokenizedText().hashCode());
        }
        return isContained;
    }

    @Override
    public void removeTextAnnotation(TextAnnotation ta) {
        for (String dataset : getAllDatasets()) {
            final ConcurrentMap<Integer, byte[]> data = getMap(dataset);
            data.remove(ta.getTokenizedText().hashCode());
        }
    }

    @Override
    public TextAnnotation getTextAnnotation(TextAnnotation ta) {
        for (String dataset : getAllDatasets()) {
            final ConcurrentMap<Integer, byte[]> data = getMap(dataset);
            if(data.containsKey(ta.getTokenizedText().hashCode())) {
                byte[] taData = data.get(ta.getTokenizedText().hashCode());
                return SerializationHelper.deserializeTextAnnotationFromBytes(taData);
            }
        }
        return null;
    }

    private ConcurrentMap<Integer, byte[]> getMap(String dataset) {
        return db.hashMap(dataset, Serializer.INTEGER, Serializer.BYTE_ARRAY).createOrOpen();
    }

    @SuppressWarnings("ConstantConditions")
    @NotNull
    private Iterable<String> getAllDatasets() {
        // danielkh: apparently the new mapdb doesn't support this locking.
        //ReentrantReadWriteLock.ReadLock lock = db.getLock$mapdb().readLock();
        //lock.tryLock();
        Iterable<String> allNames = db.getAllNames();
        //lock.unlock();
        return allNames;
    }
}
