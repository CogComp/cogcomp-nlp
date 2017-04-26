// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B880000000000000005AF813F62C030158FFACB6424A870C3464256AA061224C09AA3B12744646C6477E0801AEF7FA309A2D5069AE4AB5EE9FBFE9B1B2B36A8431C689EC634725B96B8A8EC4EA8C7DB8FA0CD0E29692CBF41738DD32FD81637C4F4856918BADE1E32434A77D1339F83ED4FE9C4CE894AE0F929EE1C7436DBCA8EA976F7559925D37D748D21FB24CCFE2D2FEEBB8E3D097CBDC1EB37E4D0D42F1A1A2C8915F43DBE542F5B4CF8FC96B2537A646D7BD7048CB012B9619166819BFAD0A4B7AACC69C598F609B408F708AEBE98E15DA7AD03F3073ED373CF9100000

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


public class PreviousTag2Level2 extends Classifier
{
  public PreviousTag2Level2()
  {
    containingPackage = "edu.illinois.cs.cogcomp.ner.LbjFeatures";
    name = "PreviousTag2Level2";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof NEWord))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'PreviousTag2Level2(NEWord)' defined on line 400 of LbjTagger.lbj received '" + type + "' as input.");
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
          if (NETaggerLevel2.isTraining)
          {
            __id = "-2";
            __value = "" + (((NEWord) ((NEWord) w.previous).previous).neLabel);
            __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
          }
          else
          {
            __id = "-2";
            __value = "" + (((NEWord) ((NEWord) w.previous).previous).neTypeLevel2);
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
      System.err.println("Classifier 'PreviousTag2Level2(NEWord)' defined on line 400 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "PreviousTag2Level2".hashCode(); }
  public boolean equals(Object o) { return o instanceof PreviousTag2Level2; }
}

