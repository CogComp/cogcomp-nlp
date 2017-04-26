// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000DA3516F63D0301DFB274526A8518056C732BE71AA180155998501F16AA09B9C53838B6F83B3B251ADF77EC673DA86786242419CA8FCFCFED3BFD9905AE7A07B487759C6DDB5B435B6C3EF4F33CB34D7E9DCF2FB85A6063C3808B87E0FB04DA02BB29427D8E192C689D2BB9AD60B8AB72243EF0B65CA05AFE90D556F3B3E576269AC8BF08BDC64757228321220BB1F0AE914752C0AC0398AE52C5B7256ADB954C9A3E2B1CDC05C75B821E36D9C14D835BD3352134BE0F7B38E9EA78D5B2B4099268C8B4050713183783B3365D72CB9A7DAB48C3751C8546AB15B8FB19562AC46C216635031FABC86F7779828B868F0A52CDF15997C22420AC6FEED7B6C21E53F54602F73C4ED84ADE98F48793E1F8F8C9AAC3F0E4322BB48CE8191D551A1D4BEFB17521824A89CE67B818122D54FD50EF8E5A67B748816BCCF2934B619257741AC554C1927202EFDA332C969B50A155B756D8B447F94A96CEAFD1A1429ED251DB3C9B55E03317800AB5412B96A2BFD1DCF75D2A83F99C52A669B0111A678F0AC684132032CE11065BDB5C4A8B34F176FF860F7F7218645A7820D1C2C01868AFEF5D84E08727079D2AF8E2D586CD3E6E26CEE9F77D55E81A8C2F93C8EBE88746848E4F7DF7036C7B095A5210F202BCA1BDF253A830ACB5F25725EDFD80F7FB108BD6856678400000

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
      System.err.println("Classifier 'prevTagsForContextLevel2(NEWord)' defined on line 419 of LbjTagger.lbj received '" + type + "' as input.");
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
      System.err.println("Classifier 'prevTagsForContextLevel2(NEWord)' defined on line 419 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "prevTagsForContextLevel2".hashCode(); }
  public boolean equals(Object o) { return o instanceof prevTagsForContextLevel2; }
}

