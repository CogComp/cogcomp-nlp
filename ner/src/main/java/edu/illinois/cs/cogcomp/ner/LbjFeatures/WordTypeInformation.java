// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B880000000000000005825D6B43C0301EFB27B18B5BCAD22E74BBA025514491E78D0F37C6FAE252B4A429A3FD6FFDDB4F5C691319248EDD3FCD3FCDD5293739964B8F5503160300BAB6C3C91C3B2D9FAF3A2CB79582DB3669B29EDAE6D5616F478F0BC01EB08710ED313DC67420ADCD92DF8F256AA27C82BA5B649670C2A2099DA53A95BAD812C594BC8B43F08F1ED4F487D4D7FD9CB4B0C368ED71296CB3001CC8DE28818A60C3E41E97C0C16990C50CC66D2562908CA5888161B0EE4FABABE601FB983298FE63683C19EC5148D8ECE3AA43EB1755B914D2C0B15BDF85A60EDFA65C4C0AA17621F139D9960D7B5F2A4904621890192BA8B5628F726E450EEE22079EB1EFACDA917665A4E32A57B9DE2BDF22865656B34490B42727B2D840AC75BB5FC77B982D9566B56A1237B3C690E2EB6BE594C83AE266904A402BC8CB22E6635558A35660DBCA5E67DA11DCB13FA943170EBFBD9F4203F86E3CF703F6B87435090C46C20D6EB1793081496016A9D416147737974D36CD3E9F80F6BFAF58AA11438B6F0BB79415EA674F42754F0698AFF97D98A8FBDDF409FFAE7E0EEBF10B8F941FB49300000

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
      System.err.println("Classifier 'WordTypeInformation(NEWord)' defined on line 196 of LbjTagger.lbj received '" + type + "' as input.");
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
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof NEWord[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'WordTypeInformation(NEWord)' defined on line 196 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "WordTypeInformation".hashCode(); }
  public boolean equals(Object o) { return o instanceof WordTypeInformation; }
}

