/**
 * This software is released under the University of Illinois/Research and
 *  Academic Use License. See the LICENSE file in the root folder for details.
 * Copyright (c) 2016
 *
 * Developed by:
 * The Cognitive Computation Group
 * University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D5C81BE028034154F75EE0C0431434D9D5C18D80121D4C9F94FA598D412FA51CFC71723179BBC937EAB13D0ACC447DC54CB7A6BC581C69D537D94D16DD6C0E8B340A86CD4A65E6C0875A13AFFB4A82C26D13F3019B2EC3B86267CCBD1C3BDF6B4D2F70E4B055C3D5F4D18376D9269B7B51AEA078AD81CCAC6ED95568C8BB484403ABF0E4649A469A000000

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




public class NETaggerLevel1 extends SparseNetworkLearner
{
  private static java.net.URL _lcFilePath;
  private static java.net.URL _lexFilePath;


  public static boolean isTraining;
  public static NETaggerLevel1 instance;




	public NETaggerLevel1(String modelPath, String lexiconPath) { this(new Parameters(), modelPath, lexiconPath); }

	public NETaggerLevel1(Parameters p, String modelPath, String lexiconPath) {
		super(p);
		try {
			lcFilePath = new java.net.URL("file:" + modelPath);
			lexFilePath = new java.net.URL("file:" + lexiconPath);
		}
		catch (Exception e) {
			System.err.println("ERROR: Can't create model or lexicon URL: " + e);
			e.printStackTrace();
			System.exit(1);
		}

		if (new java.io.File(modelPath).exists()) {
			readModel(lcFilePath);
			readLexiconOnDemand(lexFilePath);
		}
		else if (IOUtilities.existsInClasspath(NETaggerLevel1.class, modelPath)) {
			readModel(IOUtilities.loadFromClasspath(NETaggerLevel1.class, modelPath));
			readLexiconOnDemand(IOUtilities.loadFromClasspath(NETaggerLevel1.class, lexiconPath));
		}
		else {
			containingPackage = "edu.illinois.cs.cogcomp.ner.LbjFeatures";
			name = "NETaggerLevel1";
			setLabeler(new NELabel());
			setExtractor(new NETaggerLevel1$$1());
		}
	}


  public String getInputType() { return "edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord"; }

  public String getOutputType() { return "discrete"; }

  public FeatureVector classify(Object __example)
  {
    return new FeatureVector(featureValue(__example));
  }

  public String discreteValue(Object __example)
  {
    return featureValue(__example).getStringValue();
  }

  public int hashCode() { return "NETaggerLevel1".hashCode(); }

  public boolean equals(Object o) { return o instanceof NETaggerLevel1; }


}

