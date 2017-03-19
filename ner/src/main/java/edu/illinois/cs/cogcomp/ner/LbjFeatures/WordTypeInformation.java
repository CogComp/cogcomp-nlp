// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D61516B62C0301DFB27616AD26D236F175B381E6381319D7056F936B7A6A4C4A429EC935FFBFE2D65B5AC8248EDB7FEEDBBC523E625D861F4B162C06806579879B38F25A3B5E1B0C779B15A7FCC2752DF5CBA341E0474003D90C908F60CFF46A9DE9C04B9735A7EBEC76A23C82D25B6496B5E2AD023B5A643B45B234CB29691796E30F8EB7FF4697140ECE5A50E13C537129A2B3401CC8D6A8818A70C7E45ED7C0C16A90C30C0785B460908C2588816C36EECFA9EB6E2214557421F7C6C079E9FC31C4A2F9344586CF6EAA43DABD46259BD1AB65DAEE6E513928A7C584CD71BB0D0FA36DA59218C42031266CA0E6990EFB891538B7B80D1CF2C7BCDA9E22B0527E86DD66B14FAB80A595E5F5227892529B5964205E6DEECF0CD62277199EE89684DDE0B618BAF9DAF9392A735C03189119C25ACA88B955150AE91338E7A5B6B91D846E775751C4A183F9D5E4203A864DEF783AAB9B37B0238147D0A6CBA49B08149601CB4D3813DBDC36F66C8FAC76D1EBE9BE6451D1AAD457ED4FDF10D701B6F940300000

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


public class WordTypeInformation extends Classifier
{
  public WordTypeInformation()
  {
    containingPackage = "edu.illinois.cs.cogcomp.ner.LbjFeatures";
    name = "WordTypeInformation";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord"; }
  public String getOutputType() { return "discrete%"; }

  private static String[] __allowableValues = DiscreteFeature.BooleanValues;
  public static String[] getAllowableValues() { return __allowableValues; }
  public String[] allowableValues() { return __allowableValues; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof NEWord))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'WordTypeInformation(NEWord)' defined on line 180 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    NEWord word = (NEWord) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    if (ParametersForLbjCode.currentParameters.featuresToUse.containsKey("WordTypeInformation"))
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
      for (; w != last; w = (NEWord) w.next, ++i)
      {
        boolean allCapitalized = true, allDigits = true, allNonLetters = true;
        for (int j = 0; j < w.form.length(); ++j)
        {
          char c = w.form.charAt(j);
          allCapitalized &= Character.isUpperCase(c);
          allDigits &= (Character.isDigit(c) || c == '.' || c == ',');
          allNonLetters &= !Character.isLetter(c);
        }
        __id = "" + ("c" + i);
        __value = "" + (allCapitalized);
        __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 2));
        __id = "" + ("d" + i);
        __value = "" + (allDigits);
        __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 2));
        __id = "" + ("p" + i);
        __value = "" + (allNonLetters);
        __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 2));
      }
    }
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof NEWord[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'WordTypeInformation(NEWord)' defined on line 180 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "WordTypeInformation".hashCode(); }
  public boolean equals(Object o) { return o instanceof WordTypeInformation; }
}

