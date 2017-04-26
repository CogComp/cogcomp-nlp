// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D5091CE43C030144F7568844598C4224C117BC505B71012E002E055F0E43BD264E8D59DE0101AEFB3672963D6F21DEA776FD466D29057D8F036BA97D5255594DBD5090FD85279EBCC3EB3AD0F9C03DC1F709B14AFA2CA8A9C3957B036F9BCFA7435151BE6CA52DE7C951B9182F66EDD589B1DE584DEE98E73D4ECCDC5295619CAD3427C1C213BEC6F60A483F343C1B13619AC0DED2784C4768B3C462DB4EA660DD825C1C89C8CB16F68C0957D71A9E7C37CEF283F08CB3E4B5CEC2D7B43D8B19697ED1DAD354D8AB32B870548D9862CF25C1D4323A23D49A869B20D12A70DD9F92A8D29F36796B91FEE0E3AE78FDA84BE32483A552BEE384C26A32CB054A7BEF3BB776C2EA832DE809CE498535206129334216A1BE8BC7F3E2F234FBA89E6F09C85F5CEFF1A7C4DBF8B2200000

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
      System.err.println("Classifier 'WordEmbeddingFeatures(NEWord)' defined on line 47 of LbjTagger.lbj received '" + type + "' as input.");
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
      System.err.println("Classifier 'WordEmbeddingFeatures(NEWord)' defined on line 47 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "WordEmbeddingFeatures".hashCode(); }
  public boolean equals(Object o) { return o instanceof WordEmbeddingFeatures; }
}

