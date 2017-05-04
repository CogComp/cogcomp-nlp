// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000DA351DE63D0341DF59B4526A8510356CB195F58A602445562614C3C4112739BD0E0EAD3C6765AA9AFFEC5BDD4B2AD1A9096A8465C7D7C7EC1BFC5B541AE93CDA5CBB254BEEDB1B353AD3EF6F33CB3457E9DCF2FB91BD0CA9606071F21EE14E212BB2165CA0D3AD0B166B8E6A6A14E57F6D2A6F7853EB441EB7B8EA43F5D1DA311B09ADD72CD463ABA311C113618D5B709F20AB20605689445F20EADB59ABDB9A2E4D15953EA782EB9AA08FC5727053E4D4F449690A578FBD14F47D3CEA5A1B099428C8B0090713183783B3325D764B9A75AA08C3791C8546AB195DFB8C23654236903B6E44CBA22ADFDD5223A2A6A38610BD3A23D8C34202D4FEE36BAD85CB6AB8C04EFE88CB1167372E31E5F87C3E327A23FC383D88CE212B3246475C51AE6DFF0AA420584139DDEAA18122D54FDC1F75F249BD328D0B56E7941A5B863577079EA4B4192520CEFFA332C969A50A195B7964BB447F548E6CCAE30A64B2CB1BCB7783732D166CE010475171D435A9F98AF945BC6C9FC4C20519C58880593C705634A8118116F800BCDCD2625CD1AF83BF7438FDE940A159E1A0470B0340A1AEBF7532820E9C1C56B8E3AB471A17F8B9B80BB7EFD5759328229C7E03AFE32A19021A3DFDEF5C81FD24696981CB28C2B6C4FB058CE082F6CB0594994831B6C70047DF4DA437A4D88DF9A576B3C7F700CFA0D7B9FE400000

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


public class prevTagsForContextLevel2 extends Classifier
{
  public prevTagsForContextLevel2()
  {
    containingPackage = "edu.illinois.cs.cogcomp.ner.LbjFeatures";
    name = "prevTagsForContextLevel2";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord"; }
  public String getOutputType() { return "real%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof NEWord))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'prevTagsForContextLevel2(NEWord)' defined on line 472 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    NEWord word = (NEWord) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    double __value;

    if (ParametersForLbjCode.currentParameters.featuresToUse.containsKey("PrevTagsForContext"))
    {
      int i, j;
      NEWord w = word;
      String[] words = new String[3];
      OccurrenceCounter[] count = new OccurrenceCounter[3];
      for (i = 0; i <= 2 && w != null; ++i)
      {
        count[i] = new OccurrenceCounter();
        words[i] = w.form;
        w = (NEWord) w.next;
      }
      w = (NEWord) word.previousIgnoreSentenceBoundary;
      for (i = 0; i < 1000 && w != null; i++)
      {
        for (j = 0; j < words.length; j++)
        {
          if (words[j] != null && w.form.equals(words[j]))
          {
            if (NETaggerLevel2.isTraining)
            {
              if (ParametersForLbjCode.currentParameters.prevPredictionsLevel2RandomGenerator.useNoise())
              {
                count[j].addToken(ParametersForLbjCode.currentParameters.prevPredictionsLevel2RandomGenerator.randomLabel());
              }
              else
              {
                count[j].addToken(w.neLabel);
              }
            }
            else
            {
              count[j].addToken(w.neTypeLevel2);
            }
          }
        }
        w = (NEWord) w.previousIgnoreSentenceBoundary;
      }
      for (j = 0; j < count.length; j++)
      {
        if (count[j] != null)
        {
          String[] all = count[j].getTokens();
          for (i = 0; i < all.length; i++)
          {
            __id = "" + (j + "_" + all[i]);
            __value = count[j].getCount(all[i]) / ((double) count[j].totalTokens);
            __result.addFeature(new RealPrimitiveStringFeature(this.containingPackage, this.name, __id, __value));
            __id = "" + (word.domainName + j + "_" + all[i]);
            __value = count[j].getCount(all[i]) / ((double) count[j].totalTokens);
            __result.addFeature(new RealPrimitiveStringFeature(this.containingPackage, this.name, __id, __value));
          }
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
      System.err.println("Classifier 'prevTagsForContextLevel2(NEWord)' defined on line 472 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "prevTagsForContextLevel2".hashCode(); }
  public boolean equals(Object o) { return o instanceof prevTagsForContextLevel2; }
}

