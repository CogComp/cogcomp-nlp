package edu.illinois.cs.cogcomp.nlp.iface;

import edu.illinois.cs.cogcomp.nlp.common.AnnotationCacheException;

/**
 * Created by mssammon on 10/14/15.
 */
public interface AnnotationCache
{

    /**
     * store the value of object T. Throw exception if this is not possible.
     *
     * @param key  the key to be used to retrieve the stored object
     * @param value  the value to be stored
     * @param <T>  the type of the stored object
     * @return  the stored object, or null if none found
     * @throws AnnotationCacheException
     */
    public <T>  void  store( String key, T value ) throws AnnotationCacheException;


    /**
     * retrieve an object T associated with the specified key. Return 'null' if object not found.
     * @param key  the key associated with the value.
     * @param <T>
     * @return
     * @throws AnnotationCacheException
     */
    public <T> T retrieve( String key ) throws AnnotationCacheException;


    /**
     * remove the value associated with the specified key.
     * @param key
     * @return
     * @throws AnnotationCacheException
     */
    public boolean removeValue( String key ) throws AnnotationCacheException;
}
