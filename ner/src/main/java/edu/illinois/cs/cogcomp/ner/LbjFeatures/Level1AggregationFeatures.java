// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D8F8BCA02013C054F7528024B88547BE36122E8B05444C7CABA3992591B594B3A8A8FFE664746652841258CD4ECDC524D97316E871CCBB323680D8E86DBB4057C2803885C467E9258B271903863C30C660269A94F90322584C3DC7F7C1BF415D1A022471B6D4565156DE731857FE2A6BE2CC0F62A1B42C4DE1A4BA0F13F6849C0FCC3180BE228581247AFCFD0EDEDAC06CFD74A451CED1584E9F65BA4CC7EA65B2E075D0146C0CF784C2B0BC4A08E20F6D733C49C717E507038E5DDCDAECB0CE3C3BC7F2E45BA36794100000

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


public class Level1AggregationFeatures extends Classifier
{
  public Level1AggregationFeatures()
  {
    containingPackage = "edu.illinois.cs.cogcomp.ner.LbjFeatures";
    name = "Level1AggregationFeatures";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord"; }
  public String getOutputType() { return "real%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof NEWord))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'Level1AggregationFeatures(NEWord)' defined on line 462 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    NEWord word = (NEWord) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    double __value;

    if (ParametersForLbjCode.currentParameters.featuresToUse.containsKey("PredictionsLevel1"))
    {
      for (int i = 0; i < word.getLevel1AggregationFeatures().size(); i++)
      {
        NEWord.RealFeature f = word.getLevel1AggregationFeatures().get(i);
        __id = "" + (f.featureGroupName);
        __value = f.featureValue;
        __result.addFeature(new RealPrimitiveStringFeature(this.containingPackage, this.name, __id, __value));
      }
    }
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof NEWord[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'Level1AggregationFeatures(NEWord)' defined on line 462 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "Level1AggregationFeatures".hashCode(); }
  public boolean equals(Object o) { return o instanceof Level1AggregationFeatures; }
}

