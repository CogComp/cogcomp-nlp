// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D8F8DCA02C04C048F556C501A5A85C3BF37215015A8705A701F05D6AA1D67712BB5444C7777BA5014C3840293CCC7394276B701274D54AC71E289466499BA58C609C4353293EAEB58815F0770718065994659704CECC8C27F7E989C92E34D224ADD74B8B86356D6636DBE64BBC85BD50DD205FDB845816B9DDCAA8FAF523A3368EABC2B135164010B6706C81DF1AF132CF482E294F1DD9CB32A8A10D296B405A0308A47EB897F412AFD4E69773C6DEF2D37359FBC31FF7972EFE087CBBE9E1BEAFE3E5100000

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
      System.err.println("Classifier 'WikifierFeatures(NEWord)' defined on line 71 of LbjTagger.lbj received '" + type + "' as input.");
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
          __id = "" + (word.domainName + "");
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
      System.err.println("Classifier 'WikifierFeatures(NEWord)' defined on line 71 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "WikifierFeatures".hashCode(); }
  public boolean equals(Object o) { return o instanceof WikifierFeatures; }
}

