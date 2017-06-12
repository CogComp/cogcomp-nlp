// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000DE15D4F42C0401DFB232984AD43438E19287118B8A4C40C8702C16D67081C6771777B2F16CFFEEC2B2653862298743D46B3B33F676EDCB79199453A5C358EB2D5CDA0D6D4038EDDB2D91CA8F801ADD08710A9240C53150325B164E5F3C2E2556817A5A6D82D655B87A82C69A1DC84DD91EAB29650943758B90A6E6289A5805FA3CF2B9955F88296B2C29293C47E8506C8F45A8CDC7FC664780FE23AF03D168CB49149601A6DCA14B67BB6C356E790094B040D186C99F835552F9EFEB7568FAFEA258EF9E253E339A2D420BA935E801CA0E4A302BCC377BD21C50FBF37734786539C9D87209B036772737FA55CB471520CC002073471C9D662CFB6750C8374933B37EC64155CFD3DB61CDC6C7BE852F61227AD26796646D9C3D7F694F6D65B84D9801C74FC1F2621627C8DB5A1CE7FAA9EBB5E33550B7C3068044F72C7E5D779643C58F55CEF4A439FAB3FA8DA9DBD4C1E1AD3217D6F72B6ADD18F776E84762A8EB176CB8C4EB6CFDB13288FDB8B5400000

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
      System.err.println("Classifier 'FormParts(NEWord)' defined on line 153 of LbjTagger.lbj received '" + type + "' as input.");
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
          if (ParametersForLbjCode.currentParameters.useFE)
          {
            __id = "" + (word.domainName + count);
            __value = "" + (MyString.normalizeDigitsForFeatureExtraction(lastParts[j]));
            __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
          }
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
          if (ParametersForLbjCode.currentParameters.useFE)
          {
            __id = "" + (word.domainName + count);
            __value = "" + (MyString.normalizeDigitsForFeatureExtraction(lastParts[j]));
            __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
          }
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
      System.err.println("Classifier 'FormParts(NEWord)' defined on line 153 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "FormParts".hashCode(); }
  public boolean equals(Object o) { return o instanceof FormParts; }
}

