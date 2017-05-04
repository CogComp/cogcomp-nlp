// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B880000000000000005619FDB43C03017CFF59370E8698D02E3A9DC7078EB82360A2E3C8D347DE57B49E291794DE446FFBB795BBF1652090777FDBFCDF84EAD5648E1FA1E19C6366255DEC32DC25FB671D4F9E3D25E0D0F513C8218F10D50443B492DD07609B76B4FABC2726374995D4486CF9362B0C4D7D48EEDDE783E8B53E35D6CDB0E77438F7596017CC0FB47B3856603EFBE29B24F15CA04B1F0A51C1BB36958E0F60AA4D9FEC05058528843B97BC9003A13CD1C078DA4EA6C06AEAA250248E057BBCBE6CD8F06B438B3FA06FD3EC304270E4327B48F5AD6DEEC4B42930DA9B44D957726926507E4822AAF2E054303EDC3963BA9F206B16B3CA9566168F91161A778E2A62E3E08CBA82BD6EA4815B27951A959F53B7488003786C127FE7FD687E5E2457EB0B9299BDD0F7CC49FBD044F45B70D244878FCF20B3FBFACC82200000

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
      System.err.println("Classifier 'BrownClusterPaths(NEWord)' defined on line 116 of LbjTagger.lbj received '" + type + "' as input.");
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
          __id = "" + (word.domainName + i);
          __value = "" + (paths[j]);
          __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
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
      System.err.println("Classifier 'BrownClusterPaths(NEWord)' defined on line 116 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "BrownClusterPaths".hashCode(); }
  public boolean equals(Object o) { return o instanceof BrownClusterPaths; }
}

