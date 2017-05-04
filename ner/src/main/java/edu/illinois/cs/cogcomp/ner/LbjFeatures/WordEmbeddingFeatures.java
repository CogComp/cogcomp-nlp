// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000059191CE43C03C068F55C4526A651A112E8463E286BB0862E002E03DE09D2ED80A43992949280DEDD17ADE6DD673459A2B3EFFFFC1B3E15A9B68777E5D4BA51A25ADE6768236DE134E3F96A37868E750C8B48F50DB18CF54A7951644F166ECF3FAE3F1D924EBEADB74B178A1FD4F49757F618AEEC649AD6872CF9C3B3BE612B2A8446B114B0834B4894BD6F60C8C01BF440C6C978C535A7B2043C87207703A15729BA908DAD8110C89E4CBED7DFC054B937B8FD150CEF283F0056BC968FEC3E7967578186569D2DA9354D0AB32B40928C39A988B417A6A981AC5DB238B8520E16472DD9FA28F613E13BCB9244BB0F14FDF5B21DAB118696A475DD029281F007E60DE63E74B7EC852B404B1012BD919B6CC081D4EC023AA6A8399FE703F282F5A8ED1967FC59BA8EDF6E4FAB9C5FF06CE9E3DC8571CEFF00A47B3466F6200000

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
      System.err.println("Classifier 'WordEmbeddingFeatures(NEWord)' defined on line 50 of LbjTagger.lbj received '" + type + "' as input.");
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
            __id = "" + (word.domainName + "place" + i + "dim" + dim);
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
      System.err.println("Classifier 'WordEmbeddingFeatures(NEWord)' defined on line 50 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "WordEmbeddingFeatures".hashCode(); }
  public boolean equals(Object o) { return o instanceof WordEmbeddingFeatures; }
}

