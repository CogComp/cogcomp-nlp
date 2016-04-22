/**
 * This software is released under the University of Illinois/Research and
 *  Academic Use License. See the LICENSE file in the root folder for details.
 * Copyright (c) 2016
 *
 * Developed by:
 * The Cognitive Computation Group
 * University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B880000000000000005B29BCE43C030154F756A598A2199858852E68D0F8EEAB2447D653392301CEACF860188F77C67D96B47180A21A846B74EB37EA5772D09D5A14787602B968C19652BBB749ECB14B7B9FEE64BA76FA69E86385CCFE61AD430D785A489650F10DA630509270D253053C58847896290F51AB91A2432D16330E60CF126B4E69ED3C69A991B23077BE4CF8AB0ADCB7DF75380AC220C38505B05C82378B7B8B027F44AE14FB0AA509A647F1DED2A2BB373E97F991DE7D379FA80757C75F82B3F820E33C713C021034459F9A7E0D94BE271227C6BD0C806A53C52C462B59C8A605EBBE4003641F929BF276F56AA9B2C7371DEEB37EA1AA4C9E9FAD0E684B7BB7A55552A5F788ADBE67C211411A72A988361743D2F7A99FFFE0CF4E96FFE68D3E5B6B36061264603E55C157CAE4BF5081B478F2F8B72B89F5300000

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


public class additionalFeaturesDiscreteConjunctive extends Classifier
{
  public additionalFeaturesDiscreteConjunctive()
  {
    containingPackage = "edu.illinois.cs.cogcomp.ner.LbjFeatures";
    name = "additionalFeaturesDiscreteConjunctive";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof NEWord))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'additionalFeaturesDiscreteConjunctive(NEWord)' defined on line 50 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    NEWord word = (NEWord) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    for (int fid = 0; fid < word.getGeneratedDiscreteFeaturesConjunctive().size(); fid++)
    {
      NEWord.DiscreteFeature feature = word.getGeneratedDiscreteFeaturesConjunctive().get(fid);
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
      for (int fid = 0; fid < w.getGeneratedDiscreteFeaturesConjunctive().size(); fid++)
      {
        NEWord.DiscreteFeature feature = w.getGeneratedDiscreteFeaturesConjunctive().get(fid);
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
      System.err.println("Classifier 'additionalFeaturesDiscreteConjunctive(NEWord)' defined on line 50 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "additionalFeaturesDiscreteConjunctive".hashCode(); }
  public boolean equals(Object o) { return o instanceof additionalFeaturesDiscreteConjunctive; }
}

