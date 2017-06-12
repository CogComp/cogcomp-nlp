// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B880000000000000005A191DB43C03016CFF59370E869AD22E3A95D71DD044D128ACE93EA7B172D6229C5DD6ACEF77F2965B2B7B12586379C77FBFEB4FA22FB478C87A033BEA1F9CC7AB0BEA28D8CB25894E0F504B284E15B3DD88AC9715DDFBEBD5BDA0B856BEC1A1E1ECA851A6E6D1AF76BF2E5ECDA16D46CFD1EE29154AF82D4300D030928F172823ABD914DAD37F582859570909497EA0806252C50C87CD94E4A403D6D5B28C23A0CBEBFAF8E96CAB038B5650BF30EC514E193B92EDD1E709D6DF04BC3F84BDCF54D0AB0CA01CD3B67C7B6A2CDA8E68B54CDE8B29B04F9E590B48E12E8A34C68871CDC3A1F8049560792BB2D6D443211F064D53FFA3C1F4716F3CEE98D19957164C247D4F98734B62E03EC95734B9E69D9E523953947142557CC8EB5F83B9E0E916885456B199FFC54209DF325CE3CFF90F197EB1BE7B0CB9FA200000

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
      System.err.println("Classifier 'Forms(NEWord)' defined on line 103 of LbjTagger.lbj received '" + type + "' as input.");
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
        if (ParametersForLbjCode.currentParameters.useFE)
        {
          __id = "" + (word.domainName + i);
          __value = "" + (MyString.normalizeDigitsForFeatureExtraction(w.form));
          __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
        }
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
      System.err.println("Classifier 'Forms(NEWord)' defined on line 103 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "Forms".hashCode(); }
  public boolean equals(Object o) { return o instanceof Forms; }
}

