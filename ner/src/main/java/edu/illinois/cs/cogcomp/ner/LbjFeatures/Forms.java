// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B880000000000000005919DDA430130158F556C28567957711F2D4BED86B02EF028A4FA3677A5646B98C466D6BA8FEEE42BBAB2DB39048CC4ECC77E484D4169C8287C037FCB909DDFC61E9B68DAE693C4A48F40A5146F069DE6455C14557BF2FA79EB6CA69D233A391EEAA51A596913C39F7E0A7FED985271E607F9D821D7497E118E408C0CF831C439BD90436384F581859768C84BC3530403992C91C87CD94E86A0EAD6A1305414197DFC5F1D3F4575E077260EBE083710562EC6BA736C772FD68186569986BDFB8A147195138701BC27DEA6CD9AE68754A67A39E30AF4F6495A4F817CC12636CB8E610D5040A2A0837DEAE8D62919A8703AE68F7D1EFF4756FDDEF1589CDAB27A616B1AF0CB2A5394CFEC977F963BD90BD5A0977957142731375C83ABEB106B86E7272200000

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


public class Forms extends Classifier
{
  public Forms()
  {
    containingPackage = "edu.illinois.cs.cogcomp.ner.LbjFeatures";
    name = "Forms";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof NEWord))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'Forms(NEWord)' defined on line 86 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    NEWord word = (NEWord) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    if (ParametersForLbjCode.currentParameters.featuresToUse.containsKey("Forms"))
    {
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
      int startIndex = i;
      NEWord startWord = w;
      for (; w != last; w = (NEWord) w.next)
      {
        __id = "" + (i++);
        __value = "" + (w.form);
        __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
      }
      i = startIndex;
      w = startWord;
      for (; w != last; w = (NEWord) w.next)
      {
        __id = "" + (i);
        __value = "" + (MyString.normalizeDigitsForFeatureExtraction(w.form));
        __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
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
      System.err.println("Classifier 'Forms(NEWord)' defined on line 86 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "Forms".hashCode(); }
  public boolean equals(Object o) { return o instanceof Forms; }
}

