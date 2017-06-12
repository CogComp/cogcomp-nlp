// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B880000000000000005C0914B620130158FFAC471A0B1A810D3AB5F456DB4795C3C69E93AB3E29213199944111FFB73BAB21F2A05C34918C56E5ED7FED4BA97548E1F516148B3DE2073AAB9458B3433ECBE2FBD15B0BF8F8087F11C114FA12F58225B98F5887EE8AA5EFC78B615EA2011A5F96727D8AC7024E6CD717CDB3BE596BCF9878C3BB5456244FEB6D38E20EA0489D91A5C997B79BD14F0F2330B1C8113429C7848202964CD557D564E5784792725373413196BD5F2064BC809D86291C4129FE36280961B25B4435CF5E031817E56266F5456BE6313B4D1520F6F44A835C1431D4F9824DC16B87931DFB7BABD827A43FB0353676D74B200000

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
      System.err.println("Classifier 'PreviousTag2Level1(NEWord)' defined on line 344 of LbjTagger.lbj received '" + type + "' as input.");
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
            if (ParametersForLbjCode.currentParameters.useFE)
            {
              __id = "" + (word.domainName + "-2");
              __value = "" + (((NEWord) ((NEWord) w.previous).previous).neLabel);
              __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
            }
          }
          else
          {
            __id = "-2";
            __value = "" + (((NEWord) ((NEWord) w.previous).previous).neTypeLevel1);
            __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
            if (ParametersForLbjCode.currentParameters.useFE)
            {
              __id = "" + (word.domainName + "-2");
              __value = "" + (((NEWord) ((NEWord) w.previous).previous).neTypeLevel1);
              __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
            }
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
      System.err.println("Classifier 'PreviousTag2Level1(NEWord)' defined on line 344 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "PreviousTag2Level1".hashCode(); }
  public boolean equals(Object o) { return o instanceof PreviousTag2Level1; }
}

