/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D5C81BE028034154F75EE0C0431430CCE2E0C64889862ECF4A7DA4C6A097DA0E7EB5723179BBC937EAD92E8AC44C0DD54C93A6FC59EBD278EEA3BA5C697C0E0B387A868C25F273A7CB2E41CDF52541D06B92D3018B1ECB8864E0C49F0E9DF7B5A69F307C59A2E86F44D19B42D9349BF6A24D51ADAD81C2ACEED9456C48BB8F84038DF0240F7AA79A000000

package edu.illinois.cs.cogcomp.ner.LbjFeatures;

import edu.illinois.cs.cogcomp.lbjava.classify.*;
import edu.illinois.cs.cogcomp.lbjava.infer.*;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.lbjava.learn.*;
import edu.illinois.cs.cogcomp.lbjava.parse.*;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.BrownClusters;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.Gazetteers;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.WordEmbeddings;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.WordTopicAndLayoutFeatures;
import edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord;
import edu.illinois.cs.cogcomp.ner.LbjTagger.ParametersForLbjCode;
import edu.illinois.cs.cogcomp.ner.StringStatisticsUtils.*;
import java.util.*;



public class NETaggerLevel2 extends SparseNetworkLearner {
    private static java.net.URL _lcFilePath;
    private static java.net.URL _lexFilePath;


    public static boolean isTraining;
    public static NETaggerLevel2 instance;



    public NETaggerLevel2(String modelPath, String lexiconPath) {
        this(new Parameters(), modelPath, lexiconPath);
    }

    public NETaggerLevel2(Parameters p, String modelPath, String lexiconPath) {
        super(p);
        try {
            lcFilePath = new java.net.URL("file:" + modelPath);
            lexFilePath = new java.net.URL("file:" + lexiconPath);
        } catch (Exception e) {
            System.err.println("ERROR: Can't create model or lexicon URL: " + e);
            e.printStackTrace();
            System.exit(1);
        }

        if (new java.io.File(modelPath).exists()) {
            readModel(lcFilePath);
            readLexiconOnDemand(lexFilePath);
        } else if (IOUtilities.existsInClasspath(NETaggerLevel2.class, modelPath)) {
            readModel(IOUtilities.loadFromClasspath(NETaggerLevel2.class, modelPath));
            readLexiconOnDemand(IOUtilities.loadFromClasspath(NETaggerLevel2.class, lexiconPath));
        } else {
            containingPackage = "edu.illinois.cs.cogcomp.ner.LbjFeatures";
            name = "NETaggerLevel2";
            setLabeler(new NELabel());
            setExtractor(new NETaggerLevel2$$1());
        }
    }

    public String getInputType() {
        return "edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord";
    }

    public String getOutputType() {
        return "discrete";
    }


    public FeatureVector classify(Object __example) {
        return new FeatureVector(featureValue(__example));
    }

    public String discreteValue(Object __example) {
        return featureValue(__example).getStringValue();
    }


    public int hashCode() {
        return "NETaggerLevel2".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof NETaggerLevel2;
    }

}
