// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000059191CE43C03C068F55CC426A451A112E8473E2863E00A9830883785DD19AAB405292581ADBB3E4ADEAB511704192293EFDFDF6B39B27B6B8E1FC1EE4EE0DB744BE6982D7D61D1B5D2E5C8DC1A1AB21895A0FD0AA006F82DACD259090D8D787D2F6D4E826D5B5B8ADF093154749723FCE82F64B79A4BBB7CF2639FD6539429047DE141CC1E2338EDA92A06F7105947E928EA7A42EBA0D8F9E3382C8506D5D92891C51C47ADAEFC6E0ABEAAAC083751C06C09017709DF803730964E432EDD2E78235BB18696A196DC94F370D5065E6AB56532637816B7048AD6DC86ED2B53C29AFE36DA07A678C21A407E14FE05B34A6BEAF455B14FCA425DF3EF5A678BC5C08C8B7CCDC69E37654210EF7B9CE3E15C9763E1DBF5163F6AA24A1A3C4A167BD61DF04E5F678546200000

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


public class GazetteersFeatures extends Classifier
{
  public GazetteersFeatures()
  {
    containingPackage = "edu.illinois.cs.cogcomp.ner.LbjFeatures";
    name = "GazetteersFeatures";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof NEWord))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'GazetteersFeatures(NEWord)' defined on line 28 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    NEWord word = (NEWord) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    if (ParametersForLbjCode.currentParameters.featuresToUse.containsKey("GazetteersFeatures"))
    {
      int i = 0;
      NEWord w = word, last = (NEWord) word.next;
      for (i = 0; i < 2 && last != null; ++i)
      {
        last = (NEWord) last.next;
      }
      for (i = 0; i > -2 && w.previous != null; --i)
      {
        w = (NEWord) w.previous;
      }
      do
      {
        if (w.gazetteers != null)
        {
          for (int j = 0; j < w.gazetteers.size(); j++)
          {
            __id = "" + (i);
            __value = "" + (w.gazetteers.get(j));
            __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
            if (ParametersForLbjCode.currentParameters.useFE)
            {
              __id = "" + (word.domainName + i);
              __value = "" + (w.gazetteers.get(j));
              __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
            }
          }
        }
        i++;
        w = (NEWord) w.next;
      }      while (w != last);

    }
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof NEWord[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'GazetteersFeatures(NEWord)' defined on line 28 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "GazetteersFeatures".hashCode(); }
  public boolean equals(Object o) { return o instanceof GazetteersFeatures; }
}

