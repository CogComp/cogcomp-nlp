// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000DA25FDB43C0301EF75E82809257562AF46731447B715A80E4C701F12B6FAB547D466E25CF9BFFDDB56D1BD8A34723E021277F5EBFEEB3E27549954787007E541AED0944ADBB73637875E3248E4B0E3145102E64A59523EC25FD8DBE1E3D589C13ECA2B615BB55D2E205ABAC22D0CCD117DD8672596AB2C77114DCF1481EC925B3841ABE552E2C8D23E90AE19B1B80310E72889304130E831EBA3D5839402A84585773DCF60760EBA48A901289E75007AB6CE45D09C955A742AD78C0C4671F45116FB7B2E6F4A9B929D85AC0188EF0A633F1B16CE8EF76C86BA45A0DADFB3BFD5E616D6701576E915BAF09E491D7B9D81B4C81F5A2931AF99060D48F523AB996FB75B132E6BF11F2ABF9A4BE8AE1A27A3A86D7958B610BB55B4BAE1CF0AE17FDB62BD5766BC8F607553952F09300000

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


public class Affixes extends Classifier
{
  public Affixes()
  {
    containingPackage = "edu.illinois.cs.cogcomp.ner.LbjFeatures";
    name = "Affixes";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof NEWord))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'Affixes(NEWord)' defined on line 248 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    NEWord word = (NEWord) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    if (ParametersForLbjCode.currentParameters.featuresToUse.containsKey("Affixes"))
    {
      int N = word.form.length();
      for (int i = 3; i <= 4; ++i)
      {
        if (word.form.length() > i)
        {
          __id = "p|";
          __value = "" + (word.form.substring(0, i));
          __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
          if (ParametersForLbjCode.currentParameters.useFE)
          {
            __id = "" + (word.domainName + "p|");
            __value = "" + (word.form.substring(0, i));
            __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
          }
        }
      }
      for (int i = 1; i <= 4; ++i)
      {
        if (word.form.length() > i)
        {
          __id = "s|";
          __value = "" + (word.form.substring(N - i));
          __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
          if (ParametersForLbjCode.currentParameters.useFE)
          {
            __id = "" + (word.domainName + "s|");
            __value = "" + (word.form.substring(N - i));
            __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
          }
        }
      }
      if (ParametersForLbjCode.currentParameters.tokenizationScheme.equals(ParametersForLbjCode.TokenizationScheme.DualTokenizationScheme))
      {
        for (int i = 0; i < word.parts.length; i++)
        {
          __id = "" + ("part" + i);
          __value = "" + (word.parts[i]);
          __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
          if (ParametersForLbjCode.currentParameters.useFE)
          {
            __id = "" + (word.domainName + "part" + i);
            __value = "" + (word.parts[i]);
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
      System.err.println("Classifier 'Affixes(NEWord)' defined on line 248 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "Affixes".hashCode(); }
  public boolean equals(Object o) { return o instanceof Affixes; }
}

