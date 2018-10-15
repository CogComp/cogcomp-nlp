/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.ExpressiveFeatures;

import org.cogcomp.Datastore;
import org.cogcomp.DatastoreException;
import edu.illinois.cs.cogcomp.core.resources.ResourceConfigurator;
import edu.illinois.cs.cogcomp.ner.IO.InFile;
import edu.illinois.cs.cogcomp.ner.LbjTagger.Data;
import edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord;
import edu.illinois.cs.cogcomp.ner.LbjTagger.ParametersForLbjCode;
import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;
import gnu.trove.map.hash.THashMap;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;

public class BrownClusters {
    private static Logger logger = LoggerFactory.getLogger(BrownClusters.class);

    /** used to synchronize initialization. */
    static private final String INIT_SYNC = "Brown Cluster Initialization Synchronization Token";

    /** ensures singleton-ness. */
    private BrownClusters() {
    }

    private boolean[] isLowercaseBrownClustersByResource = null;
    private ArrayList<String> resources = null;
    private ArrayList<THashMap<String, String>> wordToPathByResource = null;
    private final int[] prefixLengths = { 4, 6, 10, 20 };

    /** clusters store, keyed on catenated paths. */
    static private HashMap<String, BrownClusters> clusters = new HashMap<>();

    /**
     * Makes a unique key based on the paths, for storage in a hashmap.
     * @param pathsToClusterFiles the paths.
     * @return the key.
     */
    private static String getKey(Vector<String> pathsToClusterFiles) {
        ArrayList<String> paths = new ArrayList<>();
        for (String path : pathsToClusterFiles) {
            paths.add(path);
        }
        Collections.sort(paths);
        StringBuffer sb = new StringBuffer();
        for (String path : paths) {
            sb.append(path);
            sb.append(" ");
        }
        return sb.toString();
    }

    /**
     * Initialze the brown cluster data. Clusters are stored in a static data structure to avoid reloading the same (read-only)
     * clusters over and over.
     * @param pathsToClusterFiles the files containing the data.
     * @param thresholds
     * @param isLowercaseBrownClusters
     */
    public static BrownClusters get(Vector<String> pathsToClusterFiles, Vector<Integer> thresholds, Vector<Boolean> isLowercaseBrownClusters) {
        boolean useLocalBrownCluster = true;
        String key = null;
        synchronized (INIT_SYNC) {
            // first check for a cluster already loaded for this data.
            key = getKey(pathsToClusterFiles);
            if (!clusters.containsKey(key)) {

                // check to see if all the paths exist on the local file system.
                for (String path : pathsToClusterFiles) {
                    if (!new File(path).exists()) {
                        useLocalBrownCluster = false;
                        break;
                    }
                }

                // create the cluster data structure.
                BrownClusters brownclusters = new BrownClusters();
                brownclusters.isLowercaseBrownClustersByResource = new boolean[isLowercaseBrownClusters.size()];
                brownclusters.wordToPathByResource = new ArrayList<>();
                brownclusters.resources = new ArrayList<>();
                if (!useLocalBrownCluster) {

                    // load everything from Minio
                    try {
                        Datastore dsNoCredentials = new Datastore(new ResourceConfigurator().getDefaultConfig());
                        File bcDirectory = dsNoCredentials.getDirectory("org.cogcomp.brown-clusters", "brown-clusters", 1.5, false);
                        for (int i = 0; i < pathsToClusterFiles.size(); i++) {
                            THashMap<String, String> h = new THashMap<>();

                            // Here we check if local resource is specified.
                            String bcFilePath = bcDirectory.getPath() + File.separator + pathsToClusterFiles.elementAt(i);
                            InputStream is = new FileInputStream(bcFilePath);
                            InFile in = new InFile(is);
                            String line = in.readLine();
                            while (line != null) {
                                StringTokenizer st = new StringTokenizer(line);
                                String path = st.nextToken();
                                String word = st.nextToken();
                                int occ = Integer.parseInt(st.nextToken());
                                if (occ >= thresholds.elementAt(i)) {
                                    h.put(word, path);
                                }
                                line = in.readLine();
                            }

                            brownclusters.wordToPathByResource.add(h);
                            brownclusters.isLowercaseBrownClustersByResource[i] = isLowercaseBrownClusters.elementAt(i);
                            brownclusters.resources.add(pathsToClusterFiles.elementAt(i));
                            in.close();
                        }
                        logger.info("Loaded brown cluster from "+key+" from Minio system.");
                        clusters.put(key, brownclusters);
                    } catch (InvalidPortException | InvalidEndpointException | DatastoreException
                                    | FileNotFoundException e) {
                        throw new RuntimeException("Brown Clusters could not be loaded.", e);
                    }
                } else {
                    
                    // load the clusters from the local file system.
                    try {
                        for (int i = 0; i < pathsToClusterFiles.size(); i++) {
                            THashMap<String, String> h = new THashMap<>();

                            // Here we check if local resource is specified.
                            String bcFilePath = pathsToClusterFiles.elementAt(i);
                            InputStream is;
                            is = new FileInputStream(bcFilePath);
                            InFile in = new InFile(is);
                            String line = in.readLine();
                            while (line != null) {
                                StringTokenizer st = new StringTokenizer(line);
                                String path = st.nextToken();
                                String word = st.nextToken();
                                int occ = Integer.parseInt(st.nextToken());
                                if (occ >= thresholds.elementAt(i)) {
                                    h.put(word, path);
                                }
                                line = in.readLine();
                            }
                            brownclusters.wordToPathByResource.add(h);
                            brownclusters.isLowercaseBrownClustersByResource[i] = isLowercaseBrownClusters.elementAt(i);
                            brownclusters.resources.add(pathsToClusterFiles.elementAt(i));
                            in.close();
                        }
                        logger.info("Loaded brown cluster from "+key+" from the local file system.");
                        clusters.put(key, brownclusters);
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException("Brown Clusters files existed on local disk, but could not be loaded.", e);
                    }
                }
            }
        }
        return clusters.get(key);
    }

    /**
     * @return the resource names array.
     */
    final public ArrayList<String> getResources() {
        return resources;
    }

    final public String[] getPrefixes(NEWord w) {
        return getPrefixes(w.form);
    }

    final public String[] getPrefixes(String word) {
        ArrayList<String> v = new ArrayList<>(wordToPathByResource.size());
        for (int j = 0; j < wordToPathByResource.size(); j++) {
            if (isLowercaseBrownClustersByResource[j])
                word = word.toLowerCase();
            THashMap<String, String> wordToPath = wordToPathByResource.get(j);
            final String prefix = "resource" + j + ":";
            if (wordToPath != null && wordToPath.containsKey(word)) {
                String path = wordToPath.get(word);
                int pathlength = path.length();
                v.add(prefix + path.substring(0, Math.min(pathlength, prefixLengths[0])));
                for (int i = 1; i < prefixLengths.length; i++)
                    if (prefixLengths[i - 1] < pathlength)
                        v.add(prefix + path.substring(0, Math.min(pathlength, prefixLengths[i])));
            }
        }
        String[] res = new String[v.size()];
        res = v.toArray(res);
        return res;
    }

    final public String getPrefixesCombined(String word) {
        String[] cl = getPrefixes(word);
        String ret = "";
        for (String s : cl) {
            ret += s + ",";
        }
        return ret;
    }

    private static void printArr(String[] arr) {
        for (String anArr : arr)
            logger.info(" " + anArr);
        logger.info("");
    }

    final public void printOovData(Data data) {
        HashMap<String, Boolean> tokensHash = new HashMap<>();
        HashMap<String, Boolean> tokensHashIC = new HashMap<>();
        ArrayList<LinkedVector> sentences = new ArrayList<>();
        for (int docid = 0; docid < data.documents.size(); docid++)
            for (int sid = 0; sid < data.documents.get(docid).sentences.size(); sid++)
                sentences.add(data.documents.get(docid).sentences.get(sid));
        for (LinkedVector sentence : sentences)
            for (int j = 0; j < sentence.size(); j++) {
                String form = ((NEWord) sentence.get(j)).form;
                tokensHash.put(form, true);
                tokensHashIC.put(form.toLowerCase(), true);
            }
        for (THashMap<String, String> wordToPath : wordToPathByResource) {
            HashMap<String, Boolean> oovCaseSensitiveHash = new HashMap<>();
            HashMap<String, Boolean> oovAfterLowercasingHash = new HashMap<>();
            for (LinkedVector sentence : sentences) {
                for (int j = 0; j < sentence.size(); j++) {
                    String form = ((NEWord) sentence.get(j)).form;
                    if (!wordToPath.containsKey(form)) {
                        oovCaseSensitiveHash.put(form, true);
                    }
                    if ((!wordToPath.containsKey(form)) && (!wordToPath.containsKey(form.toLowerCase()))) {
                        oovAfterLowercasingHash.put(form.toLowerCase(), true);
                    }
                }
            }
        }

    }
}
