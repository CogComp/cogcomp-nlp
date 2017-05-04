// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B880000000000000005715B5B43C0341EFB27C1832126388F867BDBC05F1491E382E37C5E476A4798429A5D9CEFBB72DBC6EA22582C9E9FE67EBA6316DE132E93CD9AD16C888E3CD2AA85B7C0C6573F2ECB6868E5C16691C7389208D3A2FA6B44120A3FF0FA5E2D9649BEADB74B1F8B3954FA2F4EE930DED9D8AC8D07F8F5C62F7DA62C97257B11C0CC1E23781CA96A46F71059A01968E331F6FBA4B8F913782C9706D3FC0CC0EA06A3DE0F76370B57555E024894603619437F22BF19CC202B657A19FEE1F3C8BA3C15D2BCA55B6E42FC10794B4BBEBCA64E6E07CE02098BD991DDB56768525EEFD8591CCE0917A580190F10D604A857D7A8AD0646521AAB57BD9867B5A2A754FB104CFF48D3D36488C7C74C05643F66A24A8F9237A69A3ACF078A787E8E2200000

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
      System.err.println("Classifier 'GazetteersFeatures(NEWord)' defined on line 26 of LbjTagger.lbj received '" + type + "' as input.");
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
            __id = "" + (word.domainName + i);
            __value = "" + (w.gazetteers.get(j));
            __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
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
      System.err.println("Classifier 'GazetteersFeatures(NEWord)' defined on line 26 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "GazetteersFeatures".hashCode(); }
  public boolean equals(Object o) { return o instanceof GazetteersFeatures; }
}

