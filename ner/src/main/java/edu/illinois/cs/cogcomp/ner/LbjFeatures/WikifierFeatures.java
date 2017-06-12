// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000059F8BBA02C040154F75EAB024240385BF8A4210154C249412611DC44743EE2CCE60111FFDDD88228858230335CD7C16A06BB51274D646C74E299425ACD5D2463895299192076FB24C0A38B28B4403FC52F39F088D4D8C47378199282ED6D224ADDB5B8BC76BC2CC2DA7DD86793B6B31AB40AE314A2C0F9DDD0A2E3F78C86D01ABEAAA6C45A1140CAD1813447BFEFC00F5341754A77E6FED1154D40D296B405A0D38AC6C39177412AFE9C51FABFFFC3A5B5A439732E1D958939FF676ED2E92F334F6FA9B3FC54C31149100000

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
      System.err.println("Classifier 'WikifierFeatures(NEWord)' defined on line 77 of LbjTagger.lbj received '" + type + "' as input.");
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
          if (ParametersForLbjCode.currentParameters.useFE)
          {
            __id = "" + (word.domainName + "");
            __value = "" + ("WIKI-" + word.wikifierFeatures[i]);
            __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
          }
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
      System.err.println("Classifier 'WikifierFeatures(NEWord)' defined on line 77 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "WikifierFeatures".hashCode(); }
  public boolean equals(Object o) { return o instanceof WikifierFeatures; }
}

