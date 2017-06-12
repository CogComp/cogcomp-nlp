// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000059E813B02C04C058FFA4611EA41B83BDA3888D9AC128383883C963DA727D4027778388FFDD3B2A2E8218402FE5EB71143E6204C4D0F9C8BA1D48828E59E5FE85A58B6A69145358B1C63885AE7F708E297C3C264DA8E13C2D93DC4EE7EC3B21A3610569208D4ED9599645FA3D2C125F1EC96397E9520B742F8F296F6F003FF085F8F4D399B2E8414DBD290E6B305B132660C082EB6696E879517B85C92A80258FA6544F85FAFB9332CB5E1C852D9C209FF99C7F76D300C774F00553100000

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
      System.err.println("Classifier 'nonLocalFeatures(NEWord)' defined on line 306 of LbjTagger.lbj received '" + type + "' as input.");
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
      if (ParametersForLbjCode.currentParameters.useFE)
      {
        __id = "" + (word.domainName + feats[i]);
        __value = word.getNonLocFeatCount(feats[i]);
        __result.addFeature(new RealPrimitiveStringFeature(this.containingPackage, this.name, __id, __value));
      }
    }
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof NEWord[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'nonLocalFeatures(NEWord)' defined on line 306 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "nonLocalFeatures".hashCode(); }
  public boolean equals(Object o) { return o instanceof nonLocalFeatures; }
}

