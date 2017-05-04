// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B880000000000000005E1915F42C03017CFBAC9B442B561610F1D18F220F2A213103E301E1AE670C1E6D26BD930C8FDDDBDA8064031F1C733D4ADBEDD5FF777FB6A4621D861FC160A47E7F24B53EF0BFF8A47A052F61047A90F604330F936227EC4D6833F6F9697DA25C82924B6496F01B86682C61A1DC85D381E8B29650943738B1FDBAA281F2086430E792665D33A4ADA0B4A4E829506E811EB4122337ABB91F17A7F83BF8DD140C349149601CB6970795FCA1DC8BFB8184A5028E243BDECE4451CBBBBF7291EBEE8250EE9EA43EB29A2C4C05E282340FB483BE28C22BCAA9690EA8FDF54554746539C9F46A099036B6CDCA5564BAACA818B300FBA2AB46F6B26E3A3784C8234937BB06F681E1AF77DED52CDD6C9474297A0191D61B74372B51E918BF29EFADA6194501CFDB6E4693D02ED925D384AA27EFFA123B580FF44BB6D86633687FF68DEB033271FA8A7B3ED30763F814B4C5BDF90277AB0FF891781E906CE0719B81FAF0706E28B4FE300000

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


public class FormParts extends Classifier
{
  public FormParts()
  {
    containingPackage = "edu.illinois.cs.cogcomp.ner.LbjFeatures";
    name = "FormParts";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof NEWord))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'FormParts(NEWord)' defined on line 139 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    NEWord word = (NEWord) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    if (ParametersForLbjCode.currentParameters.featuresToUse.containsKey("Forms") && ParametersForLbjCode.currentParameters.tokenizationScheme.equals(ParametersForLbjCode.TokenizationScheme.DualTokenizationScheme))
    {
      __id = "0";
      __value = "" + (word.form);
      __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
      int i = -1;
      int count = -1;
      NEWord w = (NEWord) word.previous;
      while (w != null && i >= -2)
      {
        String[] lastParts = w.parts;
        for (int j = 0; j < lastParts.length; j++)
        {
          __id = "" + (count);
          __value = "" + (MyString.normalizeDigitsForFeatureExtraction(lastParts[j]));
          __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
          __id = "" + (word.domainName + count);
          __value = "" + (MyString.normalizeDigitsForFeatureExtraction(lastParts[j]));
          __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
          count--;
        }
        w = (NEWord) w.previous;
        i--;
      }
      i = 1;
      count = 1;
      w = (NEWord) word.next;
      while (w != null && i <= 2)
      {
        String[] lastParts = w.parts;
        for (int j = 0; j < lastParts.length; j++)
        {
          __id = "" + (count);
          __value = "" + (MyString.normalizeDigitsForFeatureExtraction(lastParts[j]));
          __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
          __id = "" + (word.domainName + count);
          __value = "" + (MyString.normalizeDigitsForFeatureExtraction(lastParts[j]));
          __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
          count++;
        }
        w = (NEWord) w.next;
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
      System.err.println("Classifier 'FormParts(NEWord)' defined on line 139 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "FormParts".hashCode(); }
  public boolean equals(Object o) { return o instanceof FormParts; }
}

