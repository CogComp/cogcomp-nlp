// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D919D6B43C03017CFBAC150125AEAC44F59DD602E3CB15A80E4C713C7195B7DD2AB6233792E3EEBBB7DCA2E64DDC921848CDDFEEEEF7779B2ACC2A3CD38392A05F28422D3FB33637876EB248E570ED145102EA5A59523769E2C8DBA1F3C9A9C13ECA2B615BBF6F5C50A475954A1A9B526FB1DE4A2D4798FA22862F7016857A4DE0258EBFA21716C691FC05F4CD4548900F7144D08260E031E7A7D783A402A8458D8A96781C00CB7905312403FF800E8752B3553627659E988EEE338943C97027352B8C49B50886788C50F9539870FF3984B1B05A0D9DEA1F7F0DA54EF16D6ECC32A65F62D923AF62B9269813E3552764F372816B1F336ABD66FBBE5B1657DF0B69AD7E2DA3A66C3C6E82A5D502BF20EE55D753AE1E19ABFD6BABDC13B85E9F406D358181EE200000

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
      System.err.println("Classifier 'Affixes(NEWord)' defined on line 226 of LbjTagger.lbj received '" + type + "' as input.");
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
          __id = "" + (word.domainName + "p|");
          __value = "" + (word.form.substring(0, i));
          __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
        }
      }
      for (int i = 1; i <= 4; ++i)
      {
        if (word.form.length() > i)
        {
          __id = "s|";
          __value = "" + (word.form.substring(N - i));
          __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
          __id = "" + (word.domainName + "s|");
          __value = "" + (word.form.substring(N - i));
          __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
        }
      }
      if (ParametersForLbjCode.currentParameters.tokenizationScheme.equals(ParametersForLbjCode.TokenizationScheme.DualTokenizationScheme))
      {
        for (int i = 0; i < word.parts.length; i++)
        {
          __id = "" + ("part" + i);
          __value = "" + (word.parts[i]);
          __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
          __id = "" + (word.domainName + "part" + i);
          __value = "" + (word.parts[i]);
          __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
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
      System.err.println("Classifier 'Affixes(NEWord)' defined on line 226 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "Affixes".hashCode(); }
  public boolean equals(Object o) { return o instanceof Affixes; }
}

