// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D9F814B62C040158FFAC43058C25C50B7C6AE9A8E5C01C344A7E5DC33C6957756663A841FFBBB1392A74923037997FED7F6A1B2B5644CB2DA8174B1A39AD4B3D21748B7FCBA9F770E68E49692AFC90DF29DD15EB23C66F9C2C2B80C5E6E7EB2430DBDE891E3EDF6A77031B3684D16D29EE1C7436DBC217EC3B74456A45F9BE329D28EF0843BB14B8B1FE4AF03AE9E5664EB37E4D8D4AA97A4861C34D5D65A6E4C0BEBDE5020F20AC623DC8E382F1F71547F435AD3A43B18B264D6F84DD48D7AC8A2D714F6F4ABF24079CEF422BE3F10341EF737F132E23CC5140986C458C100000

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
      System.err.println("Classifier 'PreviousTag1Level2(NEWord)' defined on line 428 of LbjTagger.lbj received '" + type + "' as input.");
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
          __id = "" + (word.domainName + "-1");
          __value = "" + (((NEWord) w.previous).neLabel);
          __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
        }
        else
        {
          __id = "-1";
          __value = "" + (((NEWord) w.previous).neTypeLevel2);
          __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
          __id = "" + (word.domainName + "-1");
          __value = "" + (((NEWord) w.previous).neTypeLevel2);
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
      System.err.println("Classifier 'PreviousTag1Level2(NEWord)' defined on line 428 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "PreviousTag1Level2".hashCode(); }
  public boolean equals(Object o) { return o instanceof PreviousTag1Level2; }
}

