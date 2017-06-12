// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B880000000000000005919FDF42C03017CFF5939442B5AEA13E3A50F1420F2A1242A6C702C3C8DE60D564B4EAD93C81EF77FAC0C12E39962D4FEEEB7F9BF1DC5BBC80D3E53C329D6CC8AAAD974A95AE7DE2A9E8FDD25E0D0F513C0218F20D50443B492DD07609B985A7E56932B93ACCA62243EBB89C2035F531AB75BF6E83E6D8F45B17F48F915FEF45AE5C133CFCDDE06991C0F7BB4EA0D741B20D6C386507AEE8561A3CB18A257EF868282C2144A9DCB5E4081C01EE0AFDF652753403575592012478AB7CCB3E8B1F1C696077E51CEF283F009C183D8CD21E786B5BBE8694270A537E8AE47F3C25CA0EC90544D5A8341D0C871F4ADCA6EB08D68DE0B6699581E764858ED1AB8A98F4302FA2AC6B9B2160DAC5658665E7DCE11220CC1A178CDBFD7B1E979B05FF9F7CAD1E46CD172C6756E673CF993569088B02FE9F8612AD7CEFB10E8A96CC6E5200000

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


public class BrownClusterPaths extends Classifier
{
  public BrownClusterPaths()
  {
    containingPackage = "edu.illinois.cs.cogcomp.ner.LbjFeatures";
    name = "BrownClusterPaths";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof NEWord))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'BrownClusterPaths(NEWord)' defined on line 128 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    NEWord word = (NEWord) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    if (ParametersForLbjCode.currentParameters.featuresToUse.containsKey("BrownClusterPaths"))
    {
      BrownClusters bc = BrownClusters.get();
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
        String[] paths = bc.getPrefixes(w);
        for (int j = 0; j < paths.length; j++)
        {
          __id = "" + (i);
          __value = "" + (paths[j]);
          __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
          if (ParametersForLbjCode.currentParameters.useFE)
          {
            __id = "" + (word.domainName + i);
            __value = "" + (paths[j]);
            __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
          }
        }
        i++;
      }
    }
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof NEWord[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'BrownClusterPaths(NEWord)' defined on line 128 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "BrownClusterPaths".hashCode(); }
  public boolean equals(Object o) { return o instanceof BrownClusterPaths; }
}

