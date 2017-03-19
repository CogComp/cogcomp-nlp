// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D8F814B82C030158FFAC3B0B0DC1C08B74BA7A5AB79D22E1A2E93AD1B469C622339A222EF773DA51D3E23037997FED7F6A1B2B76A84F18533D9C68E4A63DECA2A319BFCC755E63073837A5A0B892EA0B704EB63C6EF29585EB3075BBFDFA0D09ED77CC4E3EBE6AF046267C42578D84AB70F1D85F2F3479C3B77456A45F9BE32C6187201BC10A5C0CB3BE3E8A7C4690FD937A6C62BA23524B4CF8ABADA4DC9816D7BDB048CB012B9EC23CC19F8FB82CB2D496F4599D19B207309B42EF7A9AEBC19E18CED93CCD1002272A0974100000

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
      System.err.println("Classifier 'PreviousTag1Level2(NEWord)' defined on line 383 of LbjTagger.lbj received '" + type + "' as input.");
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
        }
        else
        {
          __id = "-1";
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
      System.err.println("Classifier 'PreviousTag1Level2(NEWord)' defined on line 383 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "PreviousTag1Level2".hashCode(); }
  public boolean equals(Object o) { return o instanceof PreviousTag1Level2; }
}

