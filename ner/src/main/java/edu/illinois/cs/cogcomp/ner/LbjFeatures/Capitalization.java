// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D50915B43C034158FFAC170E869EA54C743BAF234F5411F141F936B7B0774C46CD4A65D1BFFE625B3BA3201837FE93FD12969D7324186FD963E965802D3D1EC1B1DB3E0AD0F79E0CEC66F073F2E4A5C01FAC1BE21B70778CE15B8E7B8951F7BE4EEF57BB17D255D4F22463CCBBAA32D1A712FF4EE9D7CDB3B143B5F774F99D2EF7DC22FC319D600B2C1B215F85BB281DE3C4241A3728C83ACB0506CAB6C5269BCF1BC95D0BDB13A0541C98735E6A734E3AEAC2D740583C907EA15E8C91AAD90DB3BBEDFC4B2BC1963CF54DCEBF569A8E8899452AE4DC9A4313C395F40E2A0C55C9637CF30A63152E9F60ABDB280F49100000

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


public class Capitalization extends Classifier
{
  public Capitalization()
  {
    containingPackage = "edu.illinois.cs.cogcomp.ner.LbjFeatures";
    name = "Capitalization";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord"; }
  public String getOutputType() { return "discrete%"; }

  private static String[] __allowableValues = DiscreteFeature.BooleanValues;
  public static String[] getAllowableValues() { return __allowableValues; }
  public String[] allowableValues() { return __allowableValues; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof NEWord))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'Capitalization(NEWord)' defined on line 166 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    NEWord word = (NEWord) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    if (ParametersForLbjCode.currentParameters.featuresToUse.containsKey("Capitalization"))
    {
      int i;
      NEWord w = word, last = word;
      for (i = 0; i <= 2 && last != null; ++i)
      {
        last = (NEWord) last.next;
      }
      for (i = 0; i > -2 && w.previous != null; --i)
      {
        w = (NEWord) w.previous;
      }
      for (; w != last; w = (NEWord) w.next)
      {
        __id = "" + (i++);
        __value = "" + (w.capitalized);
        __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 2));
      }
    }
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof NEWord[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'Capitalization(NEWord)' defined on line 166 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "Capitalization".hashCode(); }
  public boolean equals(Object o) { return o instanceof Capitalization; }
}

