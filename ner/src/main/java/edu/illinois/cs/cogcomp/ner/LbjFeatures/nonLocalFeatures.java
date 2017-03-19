// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000055E81BA02C04C068F552B807741B83BDA3888E427B838349E0786A7E1C190CD5A8384FDDD8D2A0E2904EFFEFF8464F96504C476EB9F4724F2366C26CD1FAC9FE0F4D161AD53CB0E2293258EA78149920BB93BA30ACE3527C49EFAE6B181833898420159DD43ABAD5AA5724A02F0DB4559551714A28B44D5CE16BF31BB9F9AF83F0C32989F22A2F906A739B0003C19B000000

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
      System.err.println("Classifier 'nonLocalFeatures(NEWord)' defined on line 242 of LbjTagger.lbj received '" + type + "' as input.");
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
    }
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof NEWord[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'nonLocalFeatures(NEWord)' defined on line 242 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "nonLocalFeatures".hashCode(); }
  public boolean equals(Object o) { return o instanceof nonLocalFeatures; }
}

