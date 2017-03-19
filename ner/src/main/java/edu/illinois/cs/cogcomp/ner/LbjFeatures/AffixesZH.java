// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000DAF8DDA430130158F55E010B424BB14DB47BD2888250561F2411A7796772BD8C631DC428FFFEE6A5A851440F24606681EC999F66A57CBC8498E007A6DAB762E9FC4637E77126B87A29416C51ED0EC24E5B9866D5C919F224CBA5CDF958694F237C84E3DE53D69C4AC198F62CD271D38F46C97EB4A719267401A03810EF8BEA73EBBC6A32D4F89D4FC25CBEA4825B939C724389C6FA4D634C5BEE9C77965255D82D24E6C0EA81E8AE256C31C17D81E0D9ADD73F37C035C65562FC4013F955F0FE2072FD00C971C92A3FD9C3C151F6D8F821FF4C2EF595D0AAF28598F443904D981B100000

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
      System.err.println("Classifier 'AffixesZH(NEWord)' defined on line 224 of LbjTagger.lbj received '" + type + "' as input.");
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
        }
      }
      for (int i = 1; i <= 2; ++i)
      {
        if (word.form.length() > i)
        {
          __id = "ZH-s|";
          __value = "" + (word.form.substring(N - i));
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
      System.err.println("Classifier 'AffixesZH(NEWord)' defined on line 224 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "AffixesZH".hashCode(); }
  public boolean equals(Object o) { return o instanceof AffixesZH; }
}

