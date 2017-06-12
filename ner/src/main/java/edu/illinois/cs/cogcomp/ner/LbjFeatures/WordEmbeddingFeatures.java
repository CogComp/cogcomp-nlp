// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000059191CE43C03C068F55C4526A651A112E84A3E286BB0862E002E03DE09D2ED80A43992949280DEDD942BE616BBD459AA83EFDFDF1BD619BAB6877365C4AD52A0125F66A8CD7761D593B94CB78E3CFA286A68F509B682F58B5ED2A74BE6A6CE3F2F3F1D804ABAECA54DE37E8EA70ACBA973712F64B7E25BB72CF92B8317375455542B6F0291C1C216C9C6F604177E78081CAD85825680F69184866C07703A1DE5275360DD925C0801919734DD0D345926AA1FBD338DD917E10AE4C9E9E6D2E7943D9BC4BABE44BEFF3A2BEE8C261411A62A90B371743D8C016AB5A2C9F200F0DA70DD9E828E60DF13A2BFA85A1876DF0FCA84BD7B01686246BBF642E1A9C07AA05F6CF74AB724269834DE01A8DA2EB2C20848EC904112B1FC1B8FE371FC34CB0679CABB3783D946798BF2A2C4B167E3B0298E4718FEE2D729091E8F703C11E5975A200000

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


public class WordEmbeddingFeatures extends Classifier
{
  public WordEmbeddingFeatures()
  {
    containingPackage = "edu.illinois.cs.cogcomp.ner.LbjFeatures";
    name = "WordEmbeddingFeatures";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord"; }
  public String getOutputType() { return "real%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof NEWord))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'WordEmbeddingFeatures(NEWord)' defined on line 54 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    NEWord word = (NEWord) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    double __value;

    if (ParametersForLbjCode.currentParameters.featuresToUse.containsKey("WordEmbeddings"))
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
      for (; w != last; w = (NEWord) w.next)
      {
        double[] embedding = WordEmbeddings.getEmbedding(w);
        if (embedding != null)
        {
          for (int dim = 0; dim < embedding.length; dim++)
          {
            __id = "" + ("place" + i + "dim" + dim);
            __value = embedding[dim];
            __result.addFeature(new RealPrimitiveStringFeature(this.containingPackage, this.name, __id, __value));
            if (ParametersForLbjCode.currentParameters.useFE)
            {
              __id = "" + (word.domainName + "place" + i + "dim" + dim);
              __value = embedding[dim];
              __result.addFeature(new RealPrimitiveStringFeature(this.containingPackage, this.name, __id, __value));
            }
          }
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
      System.err.println("Classifier 'WordEmbeddingFeatures(NEWord)' defined on line 54 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "WordEmbeddingFeatures".hashCode(); }
  public boolean equals(Object o) { return o instanceof WordEmbeddingFeatures; }
}

