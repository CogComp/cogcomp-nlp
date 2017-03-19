// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D6D814B028044148FFAC4B01822A47EAC35428411D1A0F01D1C2F95FAC671EDEA4444FFDB5928024E1CCB3CCC73351BD3A093A12A0EB27DC429159EA512B1CA6E5819A077F2126A13E90EA11CAB492F6E101B99195E1E233351527C65484BBF9794DFD69D89DA5FEB1DEA46D6714F804DFF09A03CF6777359CDFFC6C0258E6B96AB05D64010B67064A81D4CFB92A71A4A12D72776F9882AE04B4AD21492C81AA8C7197CA015F39B3EDF40FAFCDB1A1CF29F8D1100000

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


public class WikifierFeatures extends Classifier
{
  public WikifierFeatures()
  {
    containingPackage = "edu.illinois.cs.cogcomp.ner.LbjFeatures";
    name = "WikifierFeatures";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof NEWord))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'WikifierFeatures(NEWord)' defined on line 66 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    NEWord word = (NEWord) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    if (ParametersForLbjCode.currentParameters.featuresToUse.containsKey("WikifierFeatures"))
    {
      if (word.wikifierFeatures != null)
      {
        for (int i = 0; i < word.wikifierFeatures.length; i++)
        {
          __id = "";
          __value = "" + ("WIKI-" + word.wikifierFeatures[i]);
          __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
        }
      }
    }
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof NEWord[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'WikifierFeatures(NEWord)' defined on line 66 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "WikifierFeatures".hashCode(); }
  public boolean equals(Object o) { return o instanceof WikifierFeatures; }
}

