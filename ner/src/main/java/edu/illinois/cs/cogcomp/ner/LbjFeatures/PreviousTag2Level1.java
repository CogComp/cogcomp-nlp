// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000DA0913B63C030158FFAC5D050B82114336DDC412D5A6C46079ECA4CF26454192CD9C901A4EFB75E4C1C95AD52540A54FEEBFED9A1B2B6644C32D291B7B1A39AD4B3D21B78B7ECBA5C760E68E09E254FA31AF62BB1AC7968DC63D80BCB50E275F53F0D04FAB3668F83EB9ED0C4CE812578F094FE1C7436DBCB3E89767BAAC49AE9BE329D28EA24A9D95A5C9D770DBB12F4F0332FD937A68629F0D051D8915FB6ABA542F5B0EBC27ADA4DC9A195FD6F101871056399664F24327FF6382DE15A951C513CC7F5A57316B98B55AF70A7AB3887228B48CB3AA5D7CD1E2BCEFB6FBB5CE9EA7E700B6AB1EB484200000

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


public class PreviousTag2Level1 extends Classifier
{
  public PreviousTag2Level1()
  {
    containingPackage = "edu.illinois.cs.cogcomp.ner.LbjFeatures";
    name = "PreviousTag2Level1";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof NEWord))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'PreviousTag2Level1(NEWord)' defined on line 306 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    NEWord word = (NEWord) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    if (ParametersForLbjCode.currentParameters.featuresToUse.containsKey("PreviousTag2"))
    {
      int i;
      NEWord w = word;
      if (w.previous != null)
      {
        if (((NEWord) w.previous).previous != null)
        {
          if (NETaggerLevel1.isTraining)
          {
            __id = "-2";
            __value = "" + (((NEWord) ((NEWord) w.previous).previous).neLabel);
            __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
            __id = "" + (word.domainName + "-2");
            __value = "" + (((NEWord) ((NEWord) w.previous).previous).neLabel);
            __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
          }
          else
          {
            __id = "-2";
            __value = "" + (((NEWord) ((NEWord) w.previous).previous).neTypeLevel1);
            __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
            __id = "" + (word.domainName + "-2");
            __value = "" + (((NEWord) ((NEWord) w.previous).previous).neTypeLevel1);
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
      System.err.println("Classifier 'PreviousTag2Level1(NEWord)' defined on line 306 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "PreviousTag2Level1".hashCode(); }
  public boolean equals(Object o) { return o instanceof PreviousTag2Level1; }
}

