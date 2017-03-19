// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D8F814B82C030158FFAC3B0B0DC1C04FA65F4B8B79D22E1A2B7E867C219C6256625544CFFEB96DA8E179189BCCB7FEB73D85930354AF0C699EC634F25B96B8A8EC4EA8C73BEF90CD0E29692C27E8B1CE119F6D0B9FD46169FA0C5DEF4F91A12D78E999C7C7DD4F19C4CE994AE0B3947F0E3A1BE5EB9EA976FE8AC49A127D748D21F40265324BC19771DDD4A7CC650FDB37A6A62B957A48698F1575B59A9313CAF6701809712463F223C209F4FB82CB2D496F4599D39B217709B42EF7A9AEB674F04E0EC17EF0EECA080474100000

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
      System.err.println("Classifier 'PreviousTag1Level1(NEWord)' defined on line 251 of LbjTagger.lbj received '" + type + "' as input.");
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
        }
        else
        {
          __id = "-1";
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
      System.err.println("Classifier 'PreviousTag1Level1(NEWord)' defined on line 251 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "PreviousTag1Level1".hashCode(); }
  public boolean equals(Object o) { return o instanceof PreviousTag1Level1; }
}

