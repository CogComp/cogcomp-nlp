/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
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
    public static void annotate(Data data, ParametersForLbjCode params) throws Exception {

        // logger.info("Annotating the data with expressive features...");

        /*
         * must be after the linkability has been initialized!!!
         */
        if (params.normalizeTitleText) {
            // logger.info("Normalizing text case ...");
            TitleTextNormalizer.normalizeCase(data);
        }

        if (params.featuresToUse.containsKey("BrownClusterPaths")) {
            // logger.info("Brown clusters OOV statistics:");
            params.brownClusters.printOovData(data);
        }
        if (params.featuresToUse.containsKey("WordEmbeddings")) {
            // logger.info("Word Embeddings OOV statistics:");
            WordEmbeddings.printOovData(data);
        }
        // annotating with Gazetteers;
        if (params.featuresToUse != null) {
            if (params.featuresToUse
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

                Gazetteers gaz = params.gazetteers;
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
         * 
         * @redman I changed this considerable, since the properties are no longer static, this bit is easier
         */
        for (int i = 0; i < params.auxiliaryModels.size(); i++) {
            Decoder.annotateDataBIO(data, params.auxiliaryModels.elementAt(i));
            Vector<Data> v = new Vector<>();
            v.addElement(data);
            NETesterMultiDataset.printAllTestResultsAsOneDataset(v, false, params);
            TextChunkRepresentationManager.changeChunkRepresentation(
                    TextChunkRepresentationManager.EncodingScheme.BIO,
                    TextChunkRepresentationManager.EncodingScheme.BILOU, data,
                    NEWord.LabelToLookAt.PredictionLevel1Tagger);
            TextChunkRepresentationManager.changeChunkRepresentation(
                    TextChunkRepresentationManager.EncodingScheme.BIO,
                    TextChunkRepresentationManager.EncodingScheme.BILOU, data,
                    NEWord.LabelToLookAt.PredictionLevel2Tagger);
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
