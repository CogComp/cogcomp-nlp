package edu.illinois.cs.cogcomp.srl.core;

import edu.illinois.cs.cogcomp.core.datastructures.Lexicon;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.manifest.FeatureManifest;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;
import edu.illinois.cs.cogcomp.srl.utilities.WeightVectorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * All information about a model: The feature extractor and the weight vector.
 * At training time, this also gives cached features.
 *
 * @author Vivek Srikumar
 *
 */
public class ModelInfo {
	private final static Logger log = LoggerFactory.getLogger(ModelInfo.class);

	public final FeatureManifest featureManifest;
	public final FeatureExtractor fex;

	private WeightVector w;

	private final SRLManager manager;

	private Models model;

	private Lexicon lexicon;

	public ModelInfo(SRLManager manager, Models m) throws Exception {
		this.manager = manager;
		this.model = m;

		String file = "features/" + manager.getSRLType() + "." + m + ".fex";
		log.info("Loading feature extractor for {} from {}", m, file);
		featureManifest = new FeatureManifest(file);
		featureManifest.useCompressedName();
		featureManifest.setVariable("*default-parser*", manager.defaultParser);

		fex = featureManifest.createFex();
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
				String modelFile = manager.getModelFileName(model);
				long start = System.currentTimeMillis();

				if (!IOUtils.exists(modelFile)) {
					log.info("Loading weight vector for {} from {} in classpath", model, modelFile);
					w = WeightVectorUtils.loadWeightVectorFromClassPath(modelFile);
				} else {
					log.info("Loading weight vector for {} from {}", model, modelFile);
					w = WeightVectorUtils.load(modelFile);
				}
				long end = System.currentTimeMillis();
				log.info("Finished loading {} weight vector. Took {} ms", model, (end - start));
			}
		}
	}

	/**
	 * This function checks if the lexicon file exists. If so, it loads the
	 * file. Otherwise, it creates a new lexicon.
	 */
	private Lexicon loadLexicon() throws IOException {
		Lexicon lexicon;
		String lexiconFile = manager.getLexiconFileName(model);
		URL url = null;

		try {
			if (!IOUtils.exists(lexiconFile)) {
				List<URL> list = IOUtils.lsResources(SRLManager.class, lexiconFile);
				if (list.size() > 0)
					url = list.get(0);
			}
			else {
				url = new File(lexiconFile).toURI().toURL();
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}

		if (url == null) {
			log.info("Lexicon file {} missing. Creating new lexicon.",
					lexiconFile);
			lexicon = new Lexicon(true, false);
		} else {
			log.info("Lexicon file {} found.", lexiconFile);
			long start = System.currentTimeMillis();
			lexicon = new Lexicon(url.openStream());
			long end = System.currentTimeMillis();

			log.info("Finished loading {} lexicon. Took {} ms", model,
					(end - start));
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
