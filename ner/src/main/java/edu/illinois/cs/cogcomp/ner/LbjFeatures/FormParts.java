// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000DC19FDF42C03017CFF59379846BC2B50C7463C711871F74C40C8F04878A3E083C1D26BD930C8FFBB75AA064031F1D4396FA777DBBBFE7A31235864B87E0D75A797F24B531ED5FE159E904DCB5409720F6043580936269C99AD076EDC3D2EA4D403D2A2DA15AD34C2D92A0B59643345F0683EA4A5142DC53E62C0C57031440D860CF1B8955FC8296B2C29293826E8B4C41F5A215A93D3DC0F83DBBC9D7CEE822615605A1480A9104B77A53D92FC791094B040D184A5EDE2455CBBFBF7191EBE725A8CF3D596C7525599C0AE93598016D076D1095595A35B4079CFEF2C53D185D42763A13492C8DD1E6EA557AB276560C31048EA9E28DBDCC8F8CF098969827667ECED83E3CCFE7CB63CDE6C79E452BA0152D61BB4332BE0F4FDF794F6D65B82C14807F5374B817499FA194291CBFF096FD4719F8B323DAC6F058DC3681217D6F73019770EF937883E31C1CB422F13E5F1C781B06173300000

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
      System.err.println("Classifier 'FormParts(NEWord)' defined on line 129 of LbjTagger.lbj received '" + type + "' as input.");
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
      System.err.println("Classifier 'FormParts(NEWord)' defined on line 129 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "FormParts".hashCode(); }
  public boolean equals(Object o) { return o instanceof FormParts; }
}

