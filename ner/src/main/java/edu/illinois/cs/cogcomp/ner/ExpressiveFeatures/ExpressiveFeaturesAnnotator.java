/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.ner.ExpressiveFeatures;

import edu.illinois.cs.cogcomp.ner.InferenceMethods.Decoder;
import edu.illinois.cs.cogcomp.ner.LbjFeatures.NETaggerLevel1;
import edu.illinois.cs.cogcomp.ner.LbjFeatures.NETaggerLevel2;
import edu.illinois.cs.cogcomp.ner.LbjTagger.*;
import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

public class ExpressiveFeaturesAnnotator {
    /**
     * Do not worry about the brown clusters and word embeddings, this stuff is added on the fly in
     * the .lbj feature generators...
     */
    public static void oldannotate(Data data, Gazetteers gaz) throws Exception {
        // annotating with Gazetteers;
        if (ParametersForLbjCode.currentParameters.featuresToUse != null) {
            if (ParametersForLbjCode.currentParameters.featuresToUse
                    .containsKey("GazetteersFeatures")) {

                // first make sure the gazetteers arrays are inited for each word.
                for (int docid = 0; docid < data.documents.size(); docid++) {
                    ArrayList<LinkedVector> sentences = data.documents.get(docid).sentences;
                    for (LinkedVector sentence : sentences) {
                        for (int j = 0; j < sentence.size(); j++) {
                            NEWord ww = (NEWord) sentence.get(j);
                            if (ww.gazetteers == null)
                                ww.gazetteers = new ArrayList<>();
                        }
                    }
                }

                // Annotating the data with gazetteers
                for (int docid = 0; docid < data.documents.size(); docid++) {
                    ArrayList<LinkedVector> sentences = data.documents.get(docid).sentences;
                    for (LinkedVector sentence : sentences) {
                        for (int j = 0; j < sentence.size(); j++)
                            gaz.annotate((NEWord) sentence.get(j));
                    }
                }
            }
        }
    }

    /**
     * Do not worry about the brown clusters and word embeddings, this stuff is added on the fly in
     * the .lbj feature generators...
     */
    public static void annotate(Data data) throws Exception {

        // logger.info("Annotating the data with expressive features...");

        /*
         * must be after the linkability has been initialized!!!
         */
        if (ParametersForLbjCode.currentParameters.normalizeTitleText) {
            // logger.info("Normalizing text case ...");
            TitleTextNormalizer.normalizeCase(data);
        }

        if (ParametersForLbjCode.currentParameters.featuresToUse.containsKey("BrownClusterPaths")) {
            // logger.info("Brown clusters OOV statistics:");
            BrownClusters.get().printOovData(data);
        }
        if (ParametersForLbjCode.currentParameters.featuresToUse.containsKey("WordEmbeddings")) {
            // logger.info("Word Embeddings OOV statistics:");
            WordEmbeddings.printOovData(data);
        }
        // annotating with Gazetteers;
        if (ParametersForLbjCode.currentParameters.featuresToUse != null) {
            if (ParametersForLbjCode.currentParameters.featuresToUse
                    .containsKey("GazetteersFeatures")) {
                // first make sure the gazetteers arrays are inited for each word.
                for (int docid = 0; docid < data.documents.size(); docid++) {
                    ArrayList<LinkedVector> sentences = data.documents.get(docid).sentences;
                    for (LinkedVector sentence : sentences) {
                        for (int j = 0; j < sentence.size(); j++) {
                            NEWord ww = (NEWord) sentence.get(j);
                            if (ww.gazetteers == null)
                                ww.gazetteers = new ArrayList<>();
                        }
                    }
                }

                Gazetteers gaz = GazetteersFactory.get();
                for (int docid = 0; docid < data.documents.size(); docid++) {
                    ArrayList<LinkedVector> sentences = data.documents.get(docid).sentences;
                    for (LinkedVector vector : sentences) {
                        for (int j = 0; j < vector.size(); j++)
                            gaz.annotate((NEWord) vector.get(j));
                    }
                }

                // sort the gazetteers.
                for (int docid = 0; docid < data.documents.size(); docid++) {
                    ArrayList<LinkedVector> sentences = data.documents.get(docid).sentences;
                    for (LinkedVector vector : sentences) {
                        for (int j = 0; j < vector.size(); j++)
                            Collections.sort(((NEWord) vector.get(j)).gazetteers);
                    }
                }
            }
        }
        // annotating the nonlocal features;
        for (int docid = 0; docid < data.documents.size(); docid++) {
            ArrayList<LinkedVector> sentences = data.documents.get(docid).sentences;
            for (LinkedVector vector : sentences) {
                for (int j = 0; j < vector.size(); j++)
                    ContextAggregation.annotate((NEWord) vector.get(j));
            }
        }

        /*
         * Note that this piece of code must be the last!!! Here we are adding as features the
         * predictions of the aux models
         */
        for (int i = 0; i < ParametersForLbjCode.currentParameters.auxiliaryModels.size(); i++) {
            ParametersForLbjCode currentModel = ParametersForLbjCode.currentParameters;
            ParametersForLbjCode.currentParameters =
                    ParametersForLbjCode.currentParameters.auxiliaryModels.elementAt(i);
            Decoder.annotateDataBIO(data,
                    (NETaggerLevel1) ParametersForLbjCode.currentParameters.taggerLevel1,
                    (NETaggerLevel2) ParametersForLbjCode.currentParameters.taggerLevel2);
            Vector<Data> v = new Vector<>();
            v.addElement(data);
            NETesterMultiDataset.printAllTestResultsAsOneDataset(v, false);
            TextChunkRepresentationManager.changeChunkRepresentation(
                    TextChunkRepresentationManager.EncodingScheme.BIO,
                    TextChunkRepresentationManager.EncodingScheme.BILOU, data,
                    NEWord.LabelToLookAt.PredictionLevel1Tagger);
            TextChunkRepresentationManager.changeChunkRepresentation(
                    TextChunkRepresentationManager.EncodingScheme.BIO,
                    TextChunkRepresentationManager.EncodingScheme.BILOU, data,
                    NEWord.LabelToLookAt.PredictionLevel2Tagger);
            // addAuxiliaryClassifierFeatures(data, "aux_model_" + i);

            ParametersForLbjCode.currentParameters = currentModel;
        }
    }

    // private static void addAuxiliaryClassifierFeatures(Data data, String auxModelId) {
    // for (int docid = 0; docid < data.documents.size(); docid++) {
    // ArrayList<LinkedVector> sentences = data.documents.get(docid).sentences;
    // for (LinkedVector sentence : sentences)
    // for (int k = 0; k < sentence.size(); k++) {
    // NEWord w = (NEWord) sentence.get(k);
    // NEWord.DiscreteFeature f = new NEWord.DiscreteFeature();
    // f.featureGroupName = auxModelId;
    // f.featureValue = w.neTypeLevel2;
    // f.useWithinTokenWindow = true;
    // w.getGeneratedDiscreteFeaturesNonConjunctive().add(f);
    // }
    // }
    // }
}
