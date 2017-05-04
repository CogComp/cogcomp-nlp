// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000DA0913B63C030158FFACBA1A06152A1C36DDC452D5A6C46079ECA4C713A2A841EE4E48052FFDB272E0EC2DE29220D2A77FD7FE4DA5953354A74C2996F634F2D89EA8A86F4EA8CBE5C760E61784792CBCC0FD0BB14EB43C66B96485ED2075BAFA7D0D29E57FCC4E3E4F6A734626FC42D48F094FE1C7436DBCB3D13FCE6559925307D748D2175126E7696976FD14FE6CC3E16E0FDB37A6C629F8D0516AC8AF53D5F229FA32EBC27ADA43C9A195FDD01012F248C66546876C4CDFBD0A4B7AACCA8C593ECF05A57B16B98B57AF70C3DD14C318C524ED15DA93EE8E2BCEFB6FBB5CE9EA7E7098625D5D84200000

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
      System.err.println("Classifier 'PreviousTag2Level2(NEWord)' defined on line 449 of LbjTagger.lbj received '" + type + "' as input.");
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
            __id = "" + (word.domainName + "-2");
            __value = "" + (((NEWord) ((NEWord) w.previous).previous).neLabel);
            __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
          }
          else
          {
            __id = "-2";
            __value = "" + (((NEWord) ((NEWord) w.previous).previous).neTypeLevel2);
            __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
            __id = "" + (word.domainName + "-2");
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
      System.err.println("Classifier 'PreviousTag2Level2(NEWord)' defined on line 449 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "PreviousTag2Level2".hashCode(); }
  public boolean equals(Object o) { return o instanceof PreviousTag2Level2; }
}

