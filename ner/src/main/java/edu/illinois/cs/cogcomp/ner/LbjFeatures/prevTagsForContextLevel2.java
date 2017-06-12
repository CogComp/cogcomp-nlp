// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000DA3516B6BD0341CFB2F61851B0F6E56DD7B9B9F2B09E81B09595D36F1A891A8DF2E9C354AE429B95829FFEB7292E8952D15A052022E7A3DDD9D77238C5EB48B5387750F6CE5A6335DAC1EF1737CB3497E9C266FD5B9A1634B038B87D07F026509C5173C7D8E0D8F303F56B35D53665576C0A273CE56B24EEA3386B0DFDC2DE311371ACE76CD623ABA311C1136E9D59301FA0ADC1A75689405FC1EAD911AA9B923C3A5A1B2CD4F3C7756E0F5AA8E0A2C9AEE882D014B2FFF6F0D3DD7F7A65AD04228023EC140C5C40EC1ECEC845F50D1AE4ACC12D458736189E6449FF32B485E1D854CC62322E57E1CEFEF3223A1A2A71DC1677436A533F9080DD9DF4D82D60FA9E34A72FF044E5373BD31F90F67C3E1F1935196AED96046B119D21238BAC42AA6CDF4A94409F413ADD6BCE9120D50FD91EFEE8B4B7040BEF8C26641A538626B32316B034192520CE96DC0FF6B4518A545E4865632DD75EAA6DBEF88A0D077AD46D95C5861613163400D6991FABE24FF245FCAA562C3FC9F21529C9F8805A5C7056D7A8108106F800B8DED2645CD3AF83BF7438FDD940A149E1A04B7B0D70AE7E78B53C920E9C0E2B1471C5A5F5CD3E27116F0CFBF659528229C7A03AF132A5902EB9EFEFF16C0771298B5C0ED04294DABB5A44630AC967C5645621E724568AD0793B1C948B4341915DF61401274FCDED673CFEF286E9F48452500000

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
      System.err.println("Classifier 'prevTagsForContextLevel2(NEWord)' defined on line 526 of LbjTagger.lbj received '" + type + "' as input.");
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
            if (ParametersForLbjCode.currentParameters.useFE)
            {
              __id = "" + (word.domainName + j + "_" + all[i]);
              __value = count[j].getCount(all[i]) / ((double) count[j].totalTokens);
              __result.addFeature(new RealPrimitiveStringFeature(this.containingPackage, this.name, __id, __value));
            }
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
      System.err.println("Classifier 'prevTagsForContextLevel2(NEWord)' defined on line 526 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "prevTagsForContextLevel2".hashCode(); }
  public boolean equals(Object o) { return o instanceof prevTagsForContextLevel2; }
}

