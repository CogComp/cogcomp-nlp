// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000DBF814B62C040158FFAC43024692E28D3635F421F2D01C34A4FCBA913C69577566675111FFB772A19874B79230379973FEB7FA5BCB12C88318511E1C684CD89E6651E10DDB5E579FD18A583AC250F1358338DD24EB2346672F24CBC045DAF7611A54DB94448E3E873DB54313112731EB85EE1C7436D3F72E92FCE1159925DBFAF806B08B3016E7586175ED15FE70D3CBCC1C727E4D094AE25C1A34AB5C5D69B121685FD5F2064FC809D47691CB34E3471503AB92DE1B23B6475CF5A6526C5693AF7F1357B1672CDA5420FAF421F2238E4C0E9C0ADC96F87BA9FFA96F11B79BFCF26162D3E643200000

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


public class PreviousTag1Level2 extends Classifier
{
  public PreviousTag1Level2()
  {
    containingPackage = "edu.illinois.cs.cogcomp.ner.LbjFeatures";
    name = "PreviousTag1Level2";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof NEWord))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'PreviousTag1Level2(NEWord)' defined on line 474 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    NEWord word = (NEWord) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    if (ParametersForLbjCode.currentParameters.featuresToUse.containsKey("PreviousTag1"))
    {
      int i;
      NEWord w = word;
      if (w.previous != null)
      {
        if (NETaggerLevel2.isTraining)
        {
          __id = "-1";
          __value = "" + (((NEWord) w.previous).neLabel);
          __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
          if (ParametersForLbjCode.currentParameters.useFE)
          {
            __id = "" + (word.domainName + "-1");
            __value = "" + (((NEWord) w.previous).neLabel);
            __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
          }
        }
        else
        {
          __id = "-1";
          __value = "" + (((NEWord) w.previous).neTypeLevel2);
          __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
          if (ParametersForLbjCode.currentParameters.useFE)
          {
            __id = "" + (word.domainName + "-1");
            __value = "" + (((NEWord) w.previous).neTypeLevel2);
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
      System.err.println("Classifier 'PreviousTag1Level2(NEWord)' defined on line 474 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "PreviousTag1Level2".hashCode(); }
  public boolean equals(Object o) { return o instanceof PreviousTag1Level2; }
}

