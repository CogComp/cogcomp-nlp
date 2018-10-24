/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.verbsense.core;

import edu.illinois.cs.cogcomp.core.datastructures.Lexicon;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.resources.ResourceConfigurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.manifest.FeatureManifest;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;
import edu.illinois.cs.cogcomp.verbsense.utilities.VerbSenseConfigurator;
import edu.illinois.cs.cogcomp.verbsense.utilities.WeightVectorUtils;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import org.cogcomp.Datastore;
import org.cogcomp.DatastoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * All information about a model: The feature extractor and the weight vector. At training time,
 * this also gives cached features.
 *
 * @author Vivek Srikumar
 *
 */
public class ModelInfo {
    private final static Logger log = LoggerFactory.getLogger(ModelInfo.class);

    public final FeatureManifest featureManifest;
    public final FeatureExtractor fex;

    private ResourceManager rm = new VerbSenseConfigurator().getDefaultConfig();

    private WeightVector w;

    private final SenseManager manager;

    private Lexicon lexicon;

    private Datastore ds = null;
    File datastoreModels = null;

    public ModelInfo(SenseManager manager) throws Exception {
        this.manager = manager;

        String file = "features.fex";
        log.info("Loading feature extractor from {}", file);
        featureManifest = new FeatureManifest(file);
        featureManifest.useCompressedName();

        fex = featureManifest.createFex();

        if (Boolean.valueOf(rm.getString(VerbSenseConfigurator.LOAD_MODELS_FROM_DATASTORE.key))) {
            try {
                ds = new Datastore(new ResourceConfigurator().getDefaultConfig());
                datastoreModels =
                        ds.getDirectory("edu.illinois.cs.cogcomp.verbsense", "verbsense-models",
                                1.0, false);
            } catch (InvalidPortException | InvalidEndpointException e) {
                e.printStackTrace();
            }
        }
    }

    public WeightVector getWeights() {
        assert w != null;
        return w;
    }

    public void loadWeightVector() {
        if (w != null) {
            log.info("Weight vector already loaded!");
            return;
        }

        synchronized (manager) {
            if (w == null) {
                long start = System.currentTimeMillis();
                String modelFile = manager.getModelFileName();
                if (Boolean.valueOf(rm
                        .getString(VerbSenseConfigurator.LOAD_MODELS_FROM_DATASTORE.key))) {
                    w = WeightVectorUtils.load(datastoreModels + File.separator + modelFile);
                } else {
                    if (!IOUtils.exists(modelFile)) {
                        log.info("Loading weight vector from {} in classpath", modelFile);
                        w = WeightVectorUtils.loadWeightVectorFromClassPath(modelFile);
                    } else {
                        log.info("Loading weight vector from {}", modelFile);
                        w = WeightVectorUtils.load(modelFile);
                    }
                }
                long end = System.currentTimeMillis();
                log.info("Finished loading weight vector. Took {} ms", (end - start));
            }
        }
    }

    /**
     * This function checks if the lexicon file exists. If so, it loads the file. Otherwise, it
     * creates a new lexicon.
     */
    private Lexicon loadLexicon() throws IOException {
        Lexicon lexicon;
        String lexiconFile = manager.getLexiconFileName();
        URL url = null;

        if (Boolean.valueOf(rm.getString(VerbSenseConfigurator.LOAD_MODELS_FROM_DATASTORE.key))) {
            url =
                    new File(datastoreModels + File.separator + manager.getLexiconFileName())
                            .toURI().toURL();
        } else {
            try {
                if (!IOUtils.exists(lexiconFile)) {
                    List<URL> list = IOUtils.lsResources(SenseManager.class, lexiconFile);
                    if (list.size() > 0)
                        url = list.get(0);
                } else {
                    url = new File(lexiconFile).toURI().toURL();
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        if (url == null) {
            log.info("Lexicon file {} missing. Creating new lexicon.", lexiconFile);
            lexicon = new Lexicon(true, false);
        } else {
            log.info("Lexicon file {} found.", lexiconFile);
            long start = System.currentTimeMillis();
            lexicon = new Lexicon(url.openStream());
            long end = System.currentTimeMillis();

            log.info("Finished loading lexicon. Took {} ms", (end - start));
        }
        return lexicon;
    }

    public Lexicon getLexicon() {
        if (this.lexicon == null) {
            synchronized (this) {
                if (this.lexicon == null) {
                    try {
                        this.lexicon = loadLexicon();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return lexicon;
    }
}
