// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B880000000000000005925FEB43C0301DF75E6307B695754CF8665146A28823CF0A8F936B737929525294D9FB6FFBB796AB5BC89884124FEEEDDB7FEE29248DCD0A3CFA52796131076A6CDE90C3B635C3E74587BA69ADCA9B31A554B8B6F958D0D113CC6A0F5026901D3073C7D4406CED8637FF25EC571896E5B138AC575B4798CD5D60DE3AE72B457DAC171ACED1E7443C32A53C836F4FAC18060B3D58CA1DE40427BEAD08105F044282C3560206691C91C86410238C045D252389C4487EB6BFAD142E62E451EBB360BD30E9B08963C3B94B238F6247D6B36B9E4B16BD4F9AA3CDE9B81128A7CB80B340B71D4676BE54B698C5107927ECB21E8B41F9850538FBB84C7AFA4CBA076BF9958657F8ECF66BDCEE6114B2B23C0152CC849CFA43598AE5DDA2A8DF62A4F299FA8B18CDFE0300C7C79E2A290170E26491CC9A8C372DA458D7AAA24337E613AC306B5B640B8AF8B62D4818FEF6FA391C83D177FF9C83437F6A022814F9024EB159D28545611689F016247737E70E19DEAE54FAE1C7DEB455FA4D9A2BFFCB0EAD2EDC5BF5E50A4FFAF2D24FA9E91F280244FF79FB32D1FBB3E320EFD668D68FE70238C5DE6AC300000

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
      System.err.println("Classifier 'WordTypeInformation(NEWord)' defined on line 216 of LbjTagger.lbj received '" + type + "' as input.");
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
        if (ParametersForLbjCode.currentParameters.useFE)
        {
          __id = "" + (word.domainName + "c" + i);
          __value = "" + (allCapitalized);
          __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 2));
          __id = "" + (word.domainName + "d" + i);
          __value = "" + (allDigits);
          __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 2));
          __id = "" + (word.domainName + "p" + i);
          __value = "" + (allNonLetters);
          __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 2));
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
      System.err.println("Classifier 'WordTypeInformation(NEWord)' defined on line 216 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "WordTypeInformation".hashCode(); }
  public boolean equals(Object o) { return o instanceof WordTypeInformation; }
}

