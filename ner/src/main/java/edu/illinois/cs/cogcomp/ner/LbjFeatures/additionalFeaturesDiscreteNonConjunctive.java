// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B880000000000000005B25BCE43C0301CF59D652AA4462612E88B1E2C3A7BE901D3B55F6AC240BB2F3A1402EFD1B357B58C1A245482297DB2FCECCA662A8CD2D2A7C3309A419723A56B7F82D70B8EE63FBDCD8EB13AF938E5A7AD0613FBB581BA0AB874903DA0E30A1361A02DE1A125053C588EF23DE12C758E768A1DA4F8A67C8B35810379CD1DB7C2DFC336564AEDA81F1C02439B6D72864407119F3AA05305C82351F0E0714EF984F389714DB02DAC47963078ADDE5097EA33B62CA7E2F511EA68F4F82BD0820E33E79C21210B33C2F6B7E0DA47E37322B97B5BD806A53C52C462B58C8A60D1AD65003649659C3793182BFE9B6C73F94EE72FC5345D3F47C7D61734628B30B5555FC6DD77AA30E6FC522222EC42111340721D2F852FFFE1BFF523FFD54E3E5B1736061DC4603E55A0C3577ADF80C85F79F20168F85B0E6300000

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


public class additionalFeaturesDiscreteNonConjunctive extends Classifier
{
  public additionalFeaturesDiscreteNonConjunctive()
  {
    containingPackage = "edu.illinois.cs.cogcomp.ner.LbjFeatures";
    name = "additionalFeaturesDiscreteNonConjunctive";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof NEWord))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'additionalFeaturesDiscreteNonConjunctive(NEWord)' defined on line 25 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    NEWord word = (NEWord) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    for (int fid = 0; fid < word.getGeneratedDiscreteFeaturesNonConjunctive().size(); fid++)
    {
      NEWord.DiscreteFeature feature = word.getGeneratedDiscreteFeaturesNonConjunctive().get(fid);
      if (!feature.useWithinTokenWindow)
      {
        __id = "" + (feature.featureGroupName);
        __value = "" + (feature.featureValue);
        __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
      }
    }
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
      for (int fid = 0; fid < w.getGeneratedDiscreteFeaturesNonConjunctive().size(); fid++)
      {
        NEWord.DiscreteFeature feature = w.getGeneratedDiscreteFeaturesNonConjunctive().get(fid);
        if (feature.useWithinTokenWindow)
        {
          __id = "" + ("pos" + i + "group" + feature.featureGroupName);
          __value = "" + (feature.featureValue);
          __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
        }
      }
      i++;
    }
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof NEWord[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'additionalFeaturesDiscreteNonConjunctive(NEWord)' defined on line 25 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "additionalFeaturesDiscreteNonConjunctive".hashCode(); }
  public boolean equals(Object o) { return o instanceof additionalFeaturesDiscreteNonConjunctive; }
}

