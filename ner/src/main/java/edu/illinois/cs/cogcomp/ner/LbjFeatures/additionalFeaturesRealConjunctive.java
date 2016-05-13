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
// F1B880000000000000005B19BCE43C030154F756A598A2199858852E68D028EEAB0488EAD22392301CE8CF860188F77C67DD70D545059054AC87299937DAB7D0ACEEC0463D0932DA4677782D97386FE3C7FB1DAE5CBA7274B2C26E7BB0D6A1812CBA489650F90DA630509270D253053C58847896A11EB4473345864A3C622A638DD7469CD2D748296191B23037DA2C7F630ADC5BEFB083C45110A10D4D2413ACC0EED2E28CD339A70DFA8A614AA1D34495B8ACE65987EA333AD7F379F6807578FBE1567E150C75872A10420636F4EB6E93472DABCD88C65DAD422896D0790399CA74645382FD5720813A8759CB79DFE235FC51EBBB827F3937D0552EC0CB738B22DEDEE865559863CE3A6737B56980311672A8883C1E82A5E1BC9FF724E3921EFD50C3EE5BD130B06D1381F236CB1BB3D2676C2D1EB1E55D4C67F3300000

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


public class additionalFeaturesRealConjunctive extends Classifier
{
  public additionalFeaturesRealConjunctive()
  {
    containingPackage = "edu.illinois.cs.cogcomp.ner.LbjFeatures";
    name = "additionalFeaturesRealConjunctive";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord"; }
  public String getOutputType() { return "real%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof NEWord))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'additionalFeaturesRealConjunctive(NEWord)' defined on line 101 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    NEWord word = (NEWord) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    double __value;

    for (int fid = 0; fid < word.getGeneratedRealFeaturesConjunctive().size(); fid++)
    {
      NEWord.RealFeature feature = word.getGeneratedRealFeaturesConjunctive().get(fid);
      if (!feature.useWithinTokenWindow)
      {
        __id = "" + (feature.featureGroupName);
        __value = feature.featureValue;
        __result.addFeature(new RealPrimitiveStringFeature(this.containingPackage, this.name, __id, __value));
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
      for (int fid = 0; fid < w.getGeneratedRealFeaturesConjunctive().size(); fid++)
      {
        NEWord.RealFeature feature = w.getGeneratedRealFeaturesConjunctive().get(fid);
        if (feature.useWithinTokenWindow)
        {
          __id = "" + ("pos" + i + "group" + feature.featureGroupName);
          __value = feature.featureValue;
          __result.addFeature(new RealPrimitiveStringFeature(this.containingPackage, this.name, __id, __value));
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
      System.err.println("Classifier 'additionalFeaturesRealConjunctive(NEWord)' defined on line 101 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "additionalFeaturesRealConjunctive".hashCode(); }
  public boolean equals(Object o) { return o instanceof additionalFeaturesRealConjunctive; }
}

