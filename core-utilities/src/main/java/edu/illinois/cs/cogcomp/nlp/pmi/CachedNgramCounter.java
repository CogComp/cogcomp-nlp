/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.pmi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public abstract class CachedNgramCounter {

    private final static Logger log = LoggerFactory.getLogger(CachedNgramCounter.class);

    private CountsCache cache;

    public CachedNgramCounter(String cacheFile) {
        cache = new CountsCache(cacheFile);
    }

    public long[] getCount(List<String> items) {
        return getCount(items.toArray(new String[items.size()]));
    }

    public long[] getCount(String[] items) {

        items = cleanup(items);

        long[] output = new long[items.length];

        Map<String, Integer> toCache = new HashMap<>();

        int id = 0;
        for (String item : items) {
            int numTokens = item.split("\\s+").length;
            if (numTokens > getMaxNumTokens())
                output[id] = 0;
            else {
                long cachedCount = cache.get(item);
                if (cachedCount >= 0) {
                    output[id] = cachedCount;
                } else {
                    toCache.put(item, id);
                }
            }
            id++;
        }

        log.debug("{} found in cache, getting {} from google ngrams",
                (items.length - toCache.size()), toCache.size());

        if (toCache.size() > 0) {
            Map<String, Long> counts = getNgramCount(toCache.keySet());
            assert counts.size() == toCache.size();

            for (Entry<String, Long> entry : counts.entrySet()) {
                String item = entry.getKey();

                id = toCache.get(item);
                Long count = entry.getValue();

                cache.add(item, count);
                output[id] = count;
            }
        }

        return output;
    }

    public abstract long getTotalCount(int numTokens);

    protected abstract Map<String, Long> getNgramCount(Set<String> keySet);

    protected abstract int getMaxNumTokens();

    private String[] cleanup(String[] items) {
        String[] c = new String[items.length];

        for (int i = 0; i < items.length; i++) {
            c[i] = items[i].replaceAll("\\s+", " ").trim().toLowerCase();
        }

        return c;
    }

    public int getCacheSize() {
        return cache.getStatistics();
    }

}
