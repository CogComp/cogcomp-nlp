// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B880000000000000005AF814B82C030158FFACBD280DC1C08E17BA7217F2B54C3479D374B36984C4466255469DFFEB9A679A71DB8C0CC56E5EBFE5A6B2B56A8432CA99E4634B259966A52D98CD42F5D2FB307D837A5A03B13E706778C7D68DC12D3169F80C5E66FB80539ED6BCC4E3E073DB3231B5629A2C794AB70F1D85F2F9479C3BB7556A457C5F116B0CFB013FBA4B8BAFECAF8D7E1F6378F6D935D739CBF68A03464D3C4FA699C7D01FDE37ADA45C9A195FD471012F248C6C3DC0FE818BFCD0A4B7A2DC68C518F509B40871045D5E847BA6D1DAF9F3BB20C0C0F9100000

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
      System.err.println("Classifier 'PreviousTag2Level1(NEWord)' defined on line 268 of LbjTagger.lbj received '" + type + "' as input.");
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
          }
          else
          {
            __id = "-2";
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
      System.err.println("Classifier 'PreviousTag2Level1(NEWord)' defined on line 268 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "PreviousTag2Level1".hashCode(); }
  public boolean equals(Object o) { return o instanceof PreviousTag2Level1; }
}

