/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorConfigurator;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.vectors.OVector;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.lbjava.classify.Feature;
import edu.illinois.cs.cogcomp.lbjava.learn.Lexicon;
import edu.illinois.cs.cogcomp.lbjava.learn.SparseAveragedPerceptron;
import edu.illinois.cs.cogcomp.lbjava.learn.SparseAveragedPerceptron.AveragedWeightVector;
import edu.illinois.cs.cogcomp.lbjava.learn.SparseNetworkLearner;
import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.ExpressiveFeaturesAnnotator;
import edu.illinois.cs.cogcomp.ner.InferenceMethods.Decoder;
import edu.illinois.cs.cogcomp.ner.LbjTagger.Data;
import edu.illinois.cs.cogcomp.ner.LbjTagger.NERDocument;
import edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord;
import edu.illinois.cs.cogcomp.ner.LbjTagger.Parameters;
import edu.illinois.cs.cogcomp.ner.LbjTagger.ParametersForLbjCode;
import edu.illinois.cs.cogcomp.ner.config.NerBaseConfigurator;
import edu.illinois.cs.cogcomp.ner.config.NerOntonotesConfigurator;

/**
 * Generate NER annotations using the Annotator API.
 * @author redman
 */
public class NERAnnotator extends Annotator {

    /** our specific logger. */
    private final Logger logger = LoggerFactory.getLogger(NERAnnotator.class);

    /** params were once static, preventing mult-model runtimes, but now are stored here. Params
     * include the models, gazetteers and brown clusters.
     */
    private ParametersForLbjCode params = null;

    /**
     * @param nonDefaultConfigValues a configuration file specifying non-default parameters for the
     *        NER model to use
     * @param viewName indicates the view name, and hence the model, that you wish to use. If you
     *        specify {@link ViewNames#NER_CONLL} or {@link ViewNames#NER_ONTONOTES}, This name will
     *        be used when creating Views in TextAnnotation objects.
     * @throws IOException if we can't read the resources or models.
     */
    public NERAnnotator(String nonDefaultConfigValues, String viewName) throws IOException {
        this(new ResourceManager(nonDefaultConfigValues), viewName);

    }

    /**
     * default constructor -- NER_CONLL models will be loaded. Lazily initialized by default.
     * 
     * @param viewName name of view this annotator will add.
     * @throws IOException
     */
    public NERAnnotator(String viewName) throws IOException {
        this(new ResourceManager(new Properties()), viewName);
    }

    /**
     * Default behavior is to use lazy initialization; override with ResourceManager entry per the
     * Configurator property {@link AnnotatorConfigurator#IS_LAZILY_INITIALIZED}
     *
     * @param nonDefaultRm specify properties to override defaults, including lazy initialization
     * @param viewName name of the view to add to the TextAnnotation (and for client to request)
     */
    public NERAnnotator(ResourceManager nonDefaultRm, String viewName) {
        super(viewName, new String[]{}, nonDefaultRm.getBoolean(
                AnnotatorConfigurator.IS_LAZILY_INITIALIZED.key, Configurator.TRUE), nonDefaultRm);
    }

    /** this is used to sync loading models. */
    static final String LOADING_MODELS = "LOADING_MODELS";
    
    /**
     * Superclass calls this method either on instantiation or at first call to getView(). Logging
     * has been disabled because non-static logger is not initialized at the time this is called if
     * non-lazy initialization is specified.
     * 
     * @param nerRm configuration parameters passed to constructor
     */
    @Override
    public void initialize(ResourceManager nerRm) {
        
        // set up the configuration
        if (ViewNames.NER_ONTONOTES.equals(getViewName()))
            nerRm = new NerOntonotesConfigurator().getConfig(nerRm);
        else
            nerRm = new NerBaseConfigurator().getConfig(nerRm);
        this.params = Parameters.readConfigAndLoadExternalData(nerRm);
        this.params.forceNewSentenceOnLineBreaks = false;
        // load the models.
        synchronized (LOADING_MODELS) {
            ModelLoader.load(nerRm, viewName, false, this.params);
       }
    }

    /**
     * Generate the view representing the list of extracted entities and adds it the
     * {@link TextAnnotation}.
     */
    @Override
    public void addView(TextAnnotation ta) {
        // convert this data structure into one the NER package can deal with.
        ArrayList<LinkedVector> sentences = new ArrayList<>();
        String[] tokens = ta.getTokens();
        int[] tokenindices = new int[tokens.length];
        int tokenIndex = 0;
        int neWordIndex = 0;
        for (int i = 0; i < ta.getNumberOfSentences(); i++) {
            Sentence sentence = ta.getSentence(i);
            String[] wtoks = sentence.getTokens();
            LinkedVector words = new LinkedVector();
            for (String w : wtoks) {
                if (w.length() > 0) {
                    NEWord.addTokenToSentence(words, w, "unlabeled", this.params);
                    tokenindices[neWordIndex] = tokenIndex;
                    neWordIndex++;
                } else {
                    logger.error("Bad (zero length) token.");
                }
                tokenIndex++;
            }
            if (words.size() > 0)
                sentences.add(words);
        }

        // Do the annotation.
        Data data = new Data(new NERDocument(sentences, "input"));
        try {
            ExpressiveFeaturesAnnotator.annotate(data, this.params);
            Decoder.annotateDataBIO(data, params);
        } catch (Exception e) {
            logger.error("Cannot annotate the text, the exception was: ", e);
            return;
        }

        // now we have the parsed entities, construct the view object.
        ArrayList<LinkedVector> nerSentences = data.documents.get(0).sentences;
        SpanLabelView nerView = new SpanLabelView(getViewName(), ta);

        // the data always has a single document
        // each LinkedVector in data corresponds to a sentence.
        int tokenoffset = 0;
        for (LinkedVector vector : nerSentences) {
            boolean open = false;

            // there should be a 1:1 mapping btw sentence tokens in record and words/predictions
            // from NER.
            int startIndex = -1;
            String label = null;
            for (int j = 0; j < vector.size(); j++, tokenoffset++) {
                NEWord neWord = (NEWord) (vector.get(j));
                String prediction = neWord.neTypeLevel2;

                // LAM-tlr this is not a great way to ascertain the entity type, it's a bit
                // convoluted, and very
                // inefficient, use enums, or nominalized indexes for this sort of thing.
                if (prediction.startsWith("B-")) {
                    startIndex = tokenoffset;
                    label = prediction.substring(2);
                    open = true;
                } else if (j > 0) {
                    String previous_prediction = ((NEWord) vector.get(j - 1)).neTypeLevel2;
                    if (prediction.startsWith("I-")
                            && (!previous_prediction.endsWith(prediction.substring(2)))) {
                        startIndex = tokenoffset;
                        label = prediction.substring(2);
                        open = true;
                    }
                }

                if (open) {
                    boolean close = false;
                    if (j == vector.size() - 1) {
                        close = true;
                    } else {
                        String next_prediction = ((NEWord) vector.get(j + 1)).neTypeLevel2;
                        if (next_prediction.startsWith("B-"))
                            close = true;
                        if (next_prediction.equals("O"))
                            close = true;
                        if (next_prediction.indexOf('-') > -1
                                && (!prediction.endsWith(next_prediction.substring(2))))
                            close = true;
                    }
                    if (close) {
                        int s = tokenindices[startIndex];

                        /*
                         * MS: fixed bug. Originally, e was set using tokenindices[tokenoffset], but
                         * tokenoffset can reach tokens.length) and this exceeds array length.
                         * Constituent constructor requires one-past-the-end token indexing,
                         * requiring e > s. Hence the complicated setting of endIndex/e below.
                         */
                        int endIndex = Math.min(tokenoffset + 1, tokens.length - 1);
                        int e = tokenindices[endIndex];
                        if (e <= s)
                            e = s + 1;

                        nerView.addSpanLabel(s, e, label, 1d);
                        open = false;
                    }
                }
            }
        }
        ta.addView(viewName, nerView);
    }

    /**
     * Return possible tag values that the NERAnnotator can produce.
     *
     * @return the set of string representing the tag values
     */
    @Override
    public Set<String> getTagValues() {
        if (!isInitialized()) {
            doInitialize();
        }
        Lexicon labelLexicon =  this.params.taggerLevel1.getLabelLexicon();
        Set<String> tagSet = new HashSet<String>();
        for (int i =0; i < labelLexicon.size(); ++i) {
            tagSet.add(labelLexicon.lookupKey(i).getStringValue());
        }
        return tagSet;
    }

    /**
     * Return the features and the weight vectors for each SparseAveragedPerceptron 
     * in the network learner for the L1 model.
     * 
     * @return the set of string representing the tag values
     */
    public HashMap<Feature, double[]> getL1FeatureWeights() {
        if (!isInitialized()) {
            doInitialize();
        }
        SparseNetworkLearner l1= this.params.taggerLevel1;
        Map lex =  l1.getLexicon().getMap();
        OVector ov = l1.getNetwork();
        HashMap<Feature, double[]> weightsPerFeature = new HashMap<>();
        
        // for each feature, make a map entry keyed on feature name.
        int cnt = 0;
        for (Object mapentry : lex.entrySet()) {
        	
        	// get the feature, and the features weight index within each of
        	// the network fo learners.
        	Feature feature = (Feature) ((Entry)mapentry).getKey();
        	int index = ((Integer) ((Entry)mapentry).getValue()).intValue();
        	double [] weights = new double[ov.size()];
        	for (int i = 0 ; i < ov.size(); i++) {
        		SparseAveragedPerceptron sap = (SparseAveragedPerceptron) (ov.get(i));
        		AveragedWeightVector awv = sap.getAveragedWeightVector();
        		weights[i] = awv.getRawWeights().get(index);
        	}
        	weightsPerFeature.put(feature, weights);
        }
        return weightsPerFeature;
    }

    /**
     * Return the features and the weight vectors for each SparseAveragedPerceptron 
     * in the network learner for the L2 model.
     * 
     * @return the set of string representing the tag values
     */
    public HashMap<Feature, double[]> getL2FeatureWeights() {
        if (!isInitialized()) {
            doInitialize();
        }
        SparseNetworkLearner l2= this.params.taggerLevel2;
        Map lex =  l2.getLexicon().getMap();
        OVector ov = l2.getNetwork();
        HashMap<Feature, double[]> weightsPerFeature = new HashMap<>();
        
        // for each feature, make a map entry keyed on feature name.
        for (Object mapentry : lex.entrySet()) {
        	
        	// get the feature, and the features weight index within each of
        	// the network fo learners.
        	Feature feature = (Feature) ((Entry)mapentry).getKey();
        	int index = ((Integer) ((Entry)mapentry).getValue()).intValue();
        	double [] weights = new double[ov.size()];
        	for (int i = 0 ; i < ov.size(); i++) {
        		SparseAveragedPerceptron sap = (SparseAveragedPerceptron) (ov.get(i));
        		AveragedWeightVector awv = sap.getAveragedWeightVector();
        		weights[i] = awv.getRawWeights().get(index);
        	}
        	weightsPerFeature.put(feature, weights);
        }
        return weightsPerFeature;
    }
    
    /**
     * This method takes two arguments, first is the configuration file name, relative to the working
     * directory or the absolute path. This config can also be in a jar file, it must be found in the path
     * provided. The second argument, L1 or anything else, indicates if the weights of the L1 or L2 models
     * should be produced. The resulting tab delimited data contains a column for the feature type, the feature
     * value, and an additional column for each of the models in the network learner, in the case of the CoNLL
     * model, the following models (and consequently a column with weights) are included:
     * I-MISC	B-LOC	I-PER	L-MISC	B-PER	U-PER	I-LOC	L-ORG	B-MISC	U-LOC	U-MISC	B-ORG	U-ORG	O	L-PER	I-ORG	L-LOC
     * 
     * @param args
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("FeatureWeightsMatrix requires two arguments:\n"
                    + "   configuration file first,\n"
                    + "   [L1|L2] to indicate which model, level 1 model or level 2 model.");
            String configFile = args[0];
        }
        String conf = args[0];
        boolean l1model = args[1].equals("L1");
    
    	/**
    	 * wrap a feature and it's weights in a class enabling sorting and array acces.
    	 * @author redman
    	 */
        class FeatureWrapper implements Comparable {
        	double [] weights;
        	Feature feature;
        	
        	/**
        	 * takes the feature and weights.
        	 * @param f the feature.
        	 * @param w the weights.
        	 */
        	FeatureWrapper(Feature f, double[] w) {
        		this.feature = f;
        		this.weights = w;
        	}
        	
			@Override
			public int compareTo(Object o) {
				FeatureWrapper other = (FeatureWrapper) o;
				double s1 = this.sumWeights();
				double s2 = other.sumWeights();
				if (s1 > s2)
					return -1;
				else if (s1 == s2)
					return 0;
				else
					return 1;
			}
			
			/**
			 * Add the weights together for the sort. 
			 * @return the sum of all the weights.
			 */
			double sumWeights() {
				double sum = 0;
				for (double d : weights) {
					sum += Math.abs(d);
				}
				return sum;
			}
			
			/**
			 * String in the form of that would would feel good inside of a CSV.
			 */
			public String toString() {
				StringBuffer sb = new StringBuffer();
				sb.append(feature.toString());
				sb.append('\t');
				sb.append(feature.getStringValue());
				for (double d : weights) {
					sb.append('\t');
					sb.append(d);
				}
                return sb.toString();
			}
        }
        
        // set up the configuration
        try {
            ResourceManager rm = new ResourceManager(conf);
            NERAnnotator nerAnnotator = NerAnnotatorManager.buildNerAnnotator(rm, ViewNames.NER_CONLL);
            File tsvfile = new File(l1model ? "L1FeatureWeights.tsv" : "L2FeatureWeights.tsv");
            try (BufferedWriter bow = new BufferedWriter(new FileWriter(tsvfile))) {
	            bow.append("name\tvalue");
	            for (String p : nerAnnotator.getTagValues()) {
	            	bow.append("\t"+p);
	            }
	            bow.newLine();
	
	            Map<Feature, double[]> wm;
	            if (l1model)
	                wm = nerAnnotator.getL1FeatureWeights();
	            else
	                wm = nerAnnotator.getL2FeatureWeights();
	
	            ArrayList<FeatureWrapper> wrappers = new ArrayList<FeatureWrapper>();
	            for (Entry<Feature, double[]> entry : wm.entrySet()) {
	                FeatureWrapper fw = new FeatureWrapper(entry.getKey(), entry.getValue());
	                wrappers.add(fw);
	            }
	            Collections.sort(wrappers);
	            for (FeatureWrapper wrapper : wrappers) {
	                bow.append(wrapper.toString());
	                bow.newLine();
	            }
            }
            System.out.println("Completed.");
        } catch (IOException e) {
            System.out.println("Cannot initialize the test, can not continue.");
            e.printStackTrace();
        }

    }
}
