// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B880000000000000005B05D5B4301301CFB2B40C2907ED1AE3A7DA022D2505E0F1411AF696F637D8CD52ABB90E76FFBB9E1D25144CA0AC2469033B33B3B5B5E50160C3083736CE332FC6A2BA1FD97AA1E92D3A06893CB185302FA539EE2139872E9EA6E7F71EB6C2611980D58D165814D12212FD8FB5E48B7714B57C798F225C6C148281C006F49B65BB62AE60B0C7C8AB5692E579249A5FA4E204503AE7BC2C87AEA861D53169255909EB027D40B9807C56A63C11C949095665D62DC7D138338E15647C802663DCF1ED50C9E7230E837E046D532F8E0317BCFDC95223E46CB39FE54B6FD5ABD4598209DE768BAEBEF82E1F7BE6514EFF29FE767C5D6BE3008423A3B589200000

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


public class AffixesZH extends Classifier
{
  public AffixesZH()
  {
    containingPackage = "edu.illinois.cs.cogcomp.ner.LbjFeatures";
    name = "AffixesZH";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof NEWord))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'AffixesZH(NEWord)' defined on line 278 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    NEWord word = (NEWord) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    if (ParametersForLbjCode.currentParameters.featuresToUse.containsKey("Affixes") && ParametersForLbjCode.currentParameters.language.equals("zh"))
    {
      int N = word.form.length();
      for (int i = 1; i <= 2; ++i)
      {
        if (word.form.length() > i)
        {
          __id = "ZH-p|";
          __value = "" + (word.form.substring(0, i));
          __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
          if (ParametersForLbjCode.currentParameters.useFE)
          {
            __id = "" + (word.domainName + "ZH-p|");
            __value = "" + (word.form.substring(0, i));
            __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
          }
        }
      }
      for (int i = 1; i <= 2; ++i)
      {
        if (word.form.length() > i)
        {
          __id = "ZH-s|";
          __value = "" + (word.form.substring(N - i));
          __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
          if (ParametersForLbjCode.currentParameters.useFE)
          {
            __id = "" + (word.domainName + "ZH-s|");
            __value = "" + (word.form.substring(N - i));
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
      System.err.println("Classifier 'AffixesZH(NEWord)' defined on line 278 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "AffixesZH".hashCode(); }
  public boolean equals(Object o) { return o instanceof AffixesZH; }
}

