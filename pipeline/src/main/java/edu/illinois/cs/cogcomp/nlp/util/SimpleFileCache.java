package edu.illinois.cs.cogcomp.nlp.util;

import edu.illinois.cs.cogcomp.nlp.common.AnnotationCacheException;
import edu.illinois.cs.cogcomp.nlp.iface.AnnotationCache;

/**
 * Created by mssammon on 10/14/15.
 */
public class SimpleFileCache implements AnnotationCache
{

    private final String pathToSaveCachedFiles;
    private final boolean throwExceptionIfNotCached;

    public SimpleFileCache( String cacheDir, boolean throwExceptionIfNotCached )
    {
        this.pathToSaveCachedFiles = cacheDir;
        this.throwExceptionIfNotCached = throwExceptionIfNotCached;

    }

    /**
     * store the value of object T. Throw exception if this is not possible.
     *
     * @param key   the key to be used to retrieve the stored object
     * @param value the value to be stored
     * @return the stored object, or null if none found
     * @throws edu.illinois.cs.cogcomp.nlp.common.AnnotationCacheException
     */
    @Override
    public <T> void store(String key, T value) throws AnnotationCacheException {

    }

    /**
     * retrieve an object T associated with the specified key. Return 'null' if object not found.
     *
     * @param key the key associated with the value.
     * @return
     * @throws edu.illinois.cs.cogcomp.nlp.common.AnnotationCacheException
     */
    @Override
    public <T> T retrieve(String key) throws AnnotationCacheException {
        return null;
    }

    /**
     * remove the value associated with the specified key.
     *
     * @param key
     * @return
     * @throws edu.illinois.cs.cogcomp.nlp.common.AnnotationCacheException
     */
    @Override
    public boolean removeValue(String key) throws AnnotationCacheException {
        return false;
    }
}
