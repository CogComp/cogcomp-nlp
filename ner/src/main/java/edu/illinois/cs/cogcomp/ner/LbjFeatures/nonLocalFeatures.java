// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000059E81BA02C030168F55E611A1285C9D6D1444729C2E0E05A340B7DA184FE02941709EBBB76A8283ABCD1CDFFDFF1710DAF51013D99F66DF90D6A9206C2CC1FAC1A3878C0505FA1E907941C1D0D4B0DB03116793B270C4B7FED039FF9ABAA0A7E00583A40E48DD452BAE5AA5A74A12DD5E2A5B211744A88B44D8B616BF51B9CF4DBD970E98251F1444EB47238D1F86D191B3228EF3D233CCF20ED811BBAFF000000

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


public class nonLocalFeatures extends Classifier
{
  public nonLocalFeatures()
  {
    containingPackage = "edu.illinois.cs.cogcomp.ner.LbjFeatures";
    name = "nonLocalFeatures";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord"; }
  public String getOutputType() { return "real%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof NEWord))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'nonLocalFeatures(NEWord)' defined on line 274 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    NEWord word = (NEWord) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    double __value;

    String[] feats = word.getAllNonlocalFeatures();
    for (int i = 0; i < feats.length; i++)
    {
      __id = "" + (feats[i]);
      __value = word.getNonLocFeatCount(feats[i]);
      __result.addFeature(new RealPrimitiveStringFeature(this.containingPackage, this.name, __id, __value));
      __id = "" + (word.domainName + feats[i]);
      __value = word.getNonLocFeatCount(feats[i]);
      __result.addFeature(new RealPrimitiveStringFeature(this.containingPackage, this.name, __id, __value));
    }
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof NEWord[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'nonLocalFeatures(NEWord)' defined on line 274 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "nonLocalFeatures".hashCode(); }
  public boolean equals(Object o) { return o instanceof nonLocalFeatures; }
}

