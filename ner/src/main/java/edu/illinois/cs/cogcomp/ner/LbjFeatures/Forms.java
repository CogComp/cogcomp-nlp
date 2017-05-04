// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B880000000000000005A191DB43C03016CFF59370E869AD22E3A9DC71DD044D128ACE93EA7B172D6229C5D67A8FFBB794BA59DB984124F29FEE7FD77C594E73E091F81696D5DE395D26D6D590DAC192CC278F00AD2427FAD9EA545ECB8AE6F9F5E2D6985C6A17E0D0F8F65C615373E0DF3AD72F2FE6D0B623EF607F9C422D7296A108681841C7B31CC3ABD9045AD3F05828DA570909497AA080663783389E4B79C1DC1C43555A02BC820F68EB12A7A1BE2C067CA0E3F083710974E4B5CBA3C732BD8F19697E196DEF64D8AB0CA01CD3B67C7D6A4CE4473E411FA3EF9C0034A752C21A7883AE01B12E5073F86C3205691C9BCDA4B5D1D844C3A15FDCF3E0F77AB0BFE6FF0CE8CCEA03261AB2A77CB2AD117857E2BF5AD2A367A73C46D42D70945D0DF16125496B69D5EA463D09DF38823968105EBF216C5636697200000

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
      System.err.println("Classifier 'Forms(NEWord)' defined on line 93 of LbjTagger.lbj received '" + type + "' as input.");
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
        __id = "" + (word.domainName + i);
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
      System.err.println("Classifier 'Forms(NEWord)' defined on line 93 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "Forms".hashCode(); }
  public boolean equals(Object o) { return o instanceof Forms; }
}

