/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.datastructures;

import gnu.trove.map.hash.TIntFloatHashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.procedure.TIntIntProcedure;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * A lexicon manager that manages features. Stores a hash value for string features and maps to an
 * integer id. Optionally stores the string values too. Method previewFeature( String ) gets the
 *
 * @author Vivek Srikumar
 */
public class Lexicon {
    private static Logger logger = LoggerFactory.getLogger(Lexicon.class);    
    
    private static final String lexManagerVersion = "0.1";

    public static final String GLOBAL_BIAS = "*-global-bias-*";

    private final TIntIntHashMap feature2Id;

    private final List<String> featureNames;

    private int nextFeatureId;

    public final TIntIntHashMap featureCounts;

    /**
     * Create a new lexicon object
     *
     * @param hasBias Include a default entry in the lexicon for GLOBAL_BIAS?
     * @param storeStrings Store strings in the lexicon? Useful for debugging at the expense of much
     *        more memory consumption
     */
    public Lexicon(boolean hasBias, boolean storeStrings) {
        feature2Id = new TIntIntHashMap();

        nextFeatureId = 0;

        if (hasBias)
            this.previewFeature(GLOBAL_BIAS);

        if (storeStrings)
            featureNames = new ArrayList<>();
        else
            featureNames = null;

        featureCounts = new TIntIntHashMap();
    }

    /**
     * Load a lexicon from the inputstream. This does not load the strings into the lexicon, even if
     * they are present.
     */
    public Lexicon(InputStream in) throws IOException {
        this(in, false);
    }

    public Lexicon(InputStream in, boolean loadStrings) throws IOException {
        GZIPInputStream zipin = new GZIPInputStream(in);

        BufferedReader reader = new BufferedReader(new InputStreamReader(zipin));

        String line;

        long start = System.currentTimeMillis();
        line = reader.readLine().trim();

        if (!line.equals(lexManagerVersion))
            throw new IOException("Invalid file. Looking for a lexicon "
                    + "written by lexicon manger version " + lexManagerVersion);

        nextFeatureId = readInt(reader);

        int n = readInt(reader);

        feature2Id = new TIntIntHashMap(n + 1);

        for (int i = 0; i < n; i++) {
            int featureHash = readInt(reader);
            int featureId = readInt(reader);

            feature2Id.put(featureHash, featureId);

        }

        logger.info("Found {} features", feature2Id.size());

        if (loadStrings) {
            featureNames = new ArrayList<>();
            int nStrings = readInt(reader);
            for (int i = 0; i < nStrings; i++) {
                featureNames.add(reader.readLine().trim());
            }
        } else {
            featureNames = null;
        }

        reader.close();

        long end = System.currentTimeMillis();

        logger.info("Loading lexicon took {} ms", (end - start));

        featureCounts = new TIntIntHashMap();
    }

    private int readInt(BufferedReader reader) throws IOException {
        return Integer.parseInt(reader.readLine().trim());
    }

    /**
     * Get the internal id for this feature
     */
    public int lookupId(String featureName) {
        int featureHash = getFeatureHash(featureName);
        return feature2Id.get(featureHash);
    }

    /**
     * Get the feature corresponding to the name. Note: This function will throw a
     * NullPointerException if the lexicon is not explicitly asked to keep the feature strings in
     * memory. The default is not to keep strings in memory.
     */
    public String lookupName(int id) {
        return featureNames.get(id);
    }

    /**
     * Increment the count for featureId.
     */
    public synchronized void countFeature(int featureId) {
        synchronized (featureCounts) {
            if (!featureCounts.containsKey(featureId))
                featureCounts.put(featureId, 1);
            else
                featureCounts.put(featureId, featureCounts.get(featureId) + 1);
        }
    }


    /**
     * a more intuitive method for adding a feature. If already added, return id that was assigned;
     * if not, add it with a unique id and return that id.
     *
     * @param feature Feature value to put in lexicon
     * @return integer id for feature
     */
    public synchronized int getFeatureId(String feature) {
        previewFeature(feature);
        return this.lookupId(feature);
    }

    /**
     * Add a new feature to this lexicon
     */
    public synchronized void previewFeature(String f) {
        int featureHash = getFeatureHash(f);

        // If there is a hash collision, print a warning
        if (feature2Id.containsKey(featureHash)) {
            logger.warn("Possible hash collision in lexicon " + "for feature name = {}, hash = {}", f,
                    featureHash);
        } else {

            feature2Id.put(featureHash, nextFeatureId++);
        }

        if (featureNames != null) {
            featureNames.add(f);
        }
    }

    public boolean contains(String f) {
        return feature2Id.containsKey(getFeatureHash(f));
    }

    /**
     * The number of features in this lexicon
     */
    public int size() {
        return feature2Id.size();
    }

    /**
     * A hash function from feature names to integers. The lexicon will lose features whenever there
     * is a hash collision because it does not keep track of any of the strings.
     */
    protected int getFeatureHash(String featureName) {
        /*
         * Some instrumentation suggests that using java's hashcode directly gives collisions for 1%
         * of randomly generated strings, while taking the hashcode of their MD5 representation
         * gives a collision of only 0.05%. So MD5 it is.
         * 
         * Using MD5, however, requires us to pay a time penalty. However, this doesn't seem to be
         * too much.
         */
        return DigestUtils.md5Hex(featureName).hashCode();
        // return featureName.hashCode();
    }

    /**
     * generate a feature id representation from a feature vector with associated weights
     *
     * @param featureMap
     * @return
     */
    public Pair<int[], float[]> getFeatureVector(Map<String, Float> featureMap) {
        TIntFloatHashMap feats = new TIntFloatHashMap();
        for (Entry<String, Float> f : featureMap.entrySet()) {

            String key = f.getKey();
            if (!contains(key))
                continue;
            int id = lookupId(key);
            float value = f.getValue();

            if (!feats.containsKey(id))
                feats.put(id, value);
        }

        float[] vals = new float[feats.size()];

        int[] idsOriginal = feats.keys();
        int[] ids = new int[idsOriginal.length];
        System.arraycopy(idsOriginal, 0, ids, 0, ids.length);
        Arrays.sort(ids);

        for (int i = 0; i < ids.length; i++) {
            vals[i] = feats.get(ids[i]);
        }

        return new Pair<>(ids, vals);
    }

    public Pair<int[], float[]> pruneFeaturesByCount(int[] idx, float[] fs, int threshold) {
        int[] array = new int[idx.length];
        float[] vals = new float[array.length];
        int count = 0;

        for (int i = 0; i < idx.length; i++) {
            int id = idx[i];
            int c = featureCounts.get(id);
            if (c <= threshold)
                continue;
            array[count] = id;

            vals[count] = fs[i];
            count++;

        }

        int[] idxF = new int[count];
        float[] valF = new float[count];

        System.arraycopy(array, 0, idxF, 0, count);
        System.arraycopy(vals, 0, valF, 0, count);

        return new Pair<>(idxF, valF);
    }


    public void writeIntegerToFeatureStringFormat(PrintStream out) throws IOException {
        if (null == this.featureNames)
            throw new IllegalStateException(
                    "Error: Lexicon has not been configured to store feature names.");

        TreeMap<Integer, String> idToFeat = new TreeMap();

        for (String feat : this.featureNames) {
            int id = lookupId(feat);
            idToFeat.put(id, feat);
        }

        for (Integer id : idToFeat.keySet()) {
            out.print(id);
            out.print("\t");
            out.print(idToFeat.get(id));
        }
        out.flush();
    }

    /**
     * Saves the feature to id mapping. Note: This does not store the feature names.
     */
    public void save(String file) throws IOException {
        BufferedOutputStream stream =
                new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(file)));

        final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream));

        writer.write(lexManagerVersion);
        writer.newLine();

        writeInt(writer, nextFeatureId);

        logger.info("Lexicon contains {} features", feature2Id.size());

        writeInt(writer, feature2Id.size());

        feature2Id.forEachEntry(new TIntIntProcedure() {

            @Override
            public boolean execute(int a, int b) {
                try {
                    writeInt(writer, a);
                    writeInt(writer, b);

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return true;
            }
        });

        if (featureNames != null) {
            writeInt(writer, featureNames.size());
            for (String s : featureNames) {
                writer.write(s);
                writer.newLine();
            }

        } else {
            writeInt(writer, 0);
        }

        writer.close();

        logger.info("Verifying save...");
        new Lexicon(new FileInputStream(new File(file)), false);
        logger.info("Done.");
    }

    private void writeInt(BufferedWriter writer, int integer) throws IOException {
        writer.write(integer + "");
        writer.newLine();
    }

    /***
     * prunes the lexicon by removing features with less than threshold many counts
     */
    public Lexicon getPrunedLexicon(final int threshold) {
        final Lexicon lex = new Lexicon(false, false);

        this.feature2Id.forEachEntry(new TIntIntProcedure() {

            @Override
            public boolean execute(int hash, int id) {

                if (featureCounts.get(id) > threshold)
                    lex.feature2Id.put(hash, id);
                return true;
            }
        });
        lex.nextFeatureId = this.nextFeatureId;

        logger.info("Number of features after pruning: " + lex.size());

        return lex;
    }

}
