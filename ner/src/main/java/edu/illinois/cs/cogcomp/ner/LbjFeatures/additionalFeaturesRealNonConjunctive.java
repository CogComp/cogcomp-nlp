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
// F1B880000000000000005B19BCE43C030154F756A598A2199858852E68D028EEAB0488EAD22392301CE8CF860188F77C67DD70D545059054AC87299937DAB7D0ACEEC0463D0932DA4677782D97386FE3C7F9B6573A5DB875F4E86585CCF671AD43034875903DA0E31A5D60A025E0A5A60A68B019E03D432C798E668A0D8478D44AD68C704D29B5AF8052DE236560CEA588FED214B9B6DF746781C220730D9A58264913CDB5C509B7625F0AF515D28453A788AC6159DDA81FC57664BFE7E2FD01EAE0F7D3ACEC3A08FA0F44B1840C6C4AC73D378E4A579B119D0B5B55403DA1E2162395F8C8A605EBBE4003641FA297F2BBE56AE9B2C77715EE727EA1AA4C9187F60754ADBDD1DAAA21D68D74DE6E6BC211622CE44111783C154BC3696FFB54D7A6ECFBB897CDB6B36061C04603E56C093677A58FC85A3C73EEFF37FCE4300000

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


public class additionalFeaturesRealNonConjunctive extends Classifier
{
  public additionalFeaturesRealNonConjunctive()
  {
    containingPackage = "edu.illinois.cs.cogcomp.ner.LbjFeatures";
    name = "additionalFeaturesRealNonConjunctive";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord"; }
  public String getOutputType() { return "real%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof NEWord))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'additionalFeaturesRealNonConjunctive(NEWord)' defined on line 74 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    NEWord word = (NEWord) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    double __value;

    for (int fid = 0; fid < word.getGeneratedRealFeaturesNonConjunctive().size(); fid++)
    {
      NEWord.RealFeature feature = word.getGeneratedRealFeaturesNonConjunctive().get(fid);
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
      for (int fid = 0; fid < w.getGeneratedRealFeaturesNonConjunctive().size(); fid++)
      {
        NEWord.RealFeature feature = w.getGeneratedRealFeaturesNonConjunctive().get(fid);
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
      System.err.println("Classifier 'additionalFeaturesRealNonConjunctive(NEWord)' defined on line 74 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "additionalFeaturesRealNonConjunctive".hashCode(); }
  public boolean equals(Object o) { return o instanceof additionalFeaturesRealNonConjunctive; }
}

