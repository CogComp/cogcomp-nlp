// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D9F814B62C040158FFAC43024692E24EAD4D351B79680E125A7E5DC33CACABB233B1544CFFEDD8941D3A49189BCCB7FEB73DA59D0322664B26C1D68E5A13D5151E80754E5F2F7207B47A4B41DBFC9E2467B49FAC0B9D72B0BC760EA6DBBF80D24F6A7668F87FB9ED2C4CE912D48F6947F0E3A1BE5EB07EC3B74456A4D09BE329D29EF084B8B14BCB1FE4AF039E9E5614EB77E4D4D4AE56A48E0C36D5D65A1E4C0BEBB14020F20AC6E5464F649F4FB82AB7A92DE159953C59396704AE63CE3564D9EB0A7D72DD7528B46F7219DC9F081B0FFB9BF8117D17E71022C13F6B8C100000

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


public class PreviousTag1Level1 extends Classifier
{
  public PreviousTag1Level1()
  {
    containingPackage = "edu.illinois.cs.cogcomp.ner.LbjFeatures";
    name = "PreviousTag1Level1";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof NEWord))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'PreviousTag1Level1(NEWord)' defined on line 285 of LbjTagger.lbj received '" + type + "' as input.");
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
        if (NETaggerLevel1.isTraining)
        {
          __id = "-1";
          __value = "" + (((NEWord) w.previous).neLabel);
          __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
          __id = "" + (word.domainName + "-1");
          __value = "" + (((NEWord) w.previous).neLabel);
          __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
        }
        else
        {
          __id = "-1";
          __value = "" + (((NEWord) w.previous).neTypeLevel1);
          __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
          __id = "" + (word.domainName + "-1");
          __value = "" + (((NEWord) w.previous).neTypeLevel1);
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
      System.err.println("Classifier 'PreviousTag1Level1(NEWord)' defined on line 285 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "PreviousTag1Level1".hashCode(); }
  public boolean equals(Object o) { return o instanceof PreviousTag1Level1; }
}

