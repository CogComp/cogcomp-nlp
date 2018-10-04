/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.annotation;

import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Property;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

/**
 * The default {@link Configurator} for {@link BasicAnnotatorService}
 *
 * @author Mark Sammons
 * @author Christos Christodoulopoulos
 * @since 9/11/15
 */
public class AnnotatorServiceConfigurator extends Configurator {
    public static final Property CACHE_DIR = new Property("cacheDirectory", "annotation-cache");
    public static final Property THROW_EXCEPTION_IF_NOT_CACHED = new Property(
            "throwExceptionIfNotCached", Configurator.FALSE);
    public static final Property CACHE_HEAP_SIZE_MB = new Property("cacheHeapSizeInMegabytes",
            "1000");
    public static final Property CACHE_DISK_SIZE_MB = new Property("cacheDiskSizeInMegabytes",
            "2000");
    public static final Property SET_CACHE_SHUTDOWN_HOOK = new Property("setCacheShutdownHook",
            Configurator.TRUE);
    public static final Property DISABLE_CACHE = new Property("disableCache", Configurator.FALSE);
    public static final Property FORCE_CACHE_UPDATE = new Property("forceCacheUpdate",
            Configurator.FALSE);

    /**
     * get a ResourceManager object with the default key/value pairs for this configurator
     *
     * @return a non-null ResourceManager with appropriate values set.
     */
    @Override
    public ResourceManager getDefaultConfig() {
        Property[] props =
                {CACHE_DIR, THROW_EXCEPTION_IF_NOT_CACHED, CACHE_HEAP_SIZE_MB, CACHE_DISK_SIZE_MB,
                        SET_CACHE_SHUTDOWN_HOOK, DISABLE_CACHE, FORCE_CACHE_UPDATE};
        return new ResourceManager(generateProperties(props));
    }
}
