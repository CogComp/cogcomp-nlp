// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000590914B430130158FFAC0501216138E5D6D38847D385A49255FC96776798C633233982D69EF777677B2D351A218406662FDB7F681D7B73033CF6C6FEF9A9681B1F9205C21D7ACC8266E3DF02EA0E74F2B039B583048A1CC2C3BFD26246929876BEFC76AA0DD623336C47EE9BAF44951DB986F9262F12ACB2EECC861C8558D47252388F8CA5587D4C0624C4010E11EE6CAFC4A7D67D06ABC62DA390B743657EB82A3CC0EADD253CD9680A650E5342D6B90A421C82AFBEF23CB035EFA9B603878371FDD7B917CFF9D8461C27AD93C10FDB1AA86BAB69E9D5C57287C1ECF28EACFB22CB100000

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
      System.err.println("Classifier 'Level1AggregationFeatures(NEWord)' defined on line 573 of LbjTagger.lbj received '" + type + "' as input.");
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
        if (ParametersForLbjCode.currentParameters.useFE)
        {
          __id = "" + (word.domainName + f.featureGroupName);
          __value = f.featureValue;
          __result.addFeature(new RealPrimitiveStringFeature(this.containingPackage, this.name, __id, __value));
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
      System.err.println("Classifier 'Level1AggregationFeatures(NEWord)' defined on line 573 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "Level1AggregationFeatures".hashCode(); }
  public boolean equals(Object o) { return o instanceof Level1AggregationFeatures; }
}

