// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B880000000000000005609DCE43C030148F5569A4456BC4C24C194BC501070012E002EC6A9D41741C646B381A8AFEECA317AF31E21963B3BFDCC8B2DE75E030E92CD9AD068088ECFD2AA0D934FCE1F6E5DABA022D783C2A08F50D530B725E4D70D9090DAB787B6EAD658275D937862CE772BEC4976BF2E96F6D405AD8FB7CF163BFF6533EC31DD400D0B483F2164B6A929DF914BAC70A12762EDFF5960FB34905B570C2FD96850C50CC7E38EF46906AB6BD21480D9C06A0943768C67289B282A7E449F9E0FB4BDECFE96541D3D2E15E9D9E21BA2B9F1B2AC5FEACE80847B38915FD6603C6827F1A65A7DB146C9612442DB743E1926D5E1BA6D81853C393E614B015E433D8DD2EBBE6192D4A809A4F072F77142DCCFDF100000

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
      System.err.println("Classifier 'GazetteersFeatures(NEWord)' defined on line 25 of LbjTagger.lbj received '" + type + "' as input.");
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
      System.err.println("Classifier 'GazetteersFeatures(NEWord)' defined on line 25 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "GazetteersFeatures".hashCode(); }
  public boolean equals(Object o) { return o instanceof GazetteersFeatures; }
}

