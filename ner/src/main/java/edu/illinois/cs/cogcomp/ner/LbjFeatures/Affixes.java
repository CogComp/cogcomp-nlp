// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D905D4B43C0401DFB23C280B1263454F4D4B288F171528065C38878D6239675B9DDA33B1CFEFFEE43D0A921D38230BBCECB7BFEDCB92C27E4810770E8AC2DE332BECE4F6C3510F4274403E10CB18D214F5A123590F88FCC3D5CCEEF8D71894E5311AB0FD85252A905312F4DF53B0EED5036D1F93EB865DAEBA82A5B4AB0091C4A1794A4F4552B4473F0B0D15A02F40DB628512CE7A27D872070924C1BD8AD96AFFD0E01A1491D1328A5DBB281574D9BE91702BE6EA78BB24C41E34A6BC86FEF764CFBA156038F27AF3C6038F70476F5D40BEDD55EB0CA031C7CADC29F761896F9E722CEE7BB95BF652E16398733BFAC050E633A4B3E8BBBB41C414C24E1578C7B6FE639D4AE310933447E554200000

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
      System.err.println("Classifier 'Affixes(NEWord)' defined on line 207 of LbjTagger.lbj received '" + type + "' as input.");
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
        }
      }
      for (int i = 1; i <= 4; ++i)
      {
        if (word.form.length() > i)
        {
          __id = "s|";
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
      System.err.println("Classifier 'Affixes(NEWord)' defined on line 207 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "Affixes".hashCode(); }
  public boolean equals(Object o) { return o instanceof Affixes; }
}

