// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D8F814B82C030158FFAC0280905C0A7DD5D38885F0A4922BAE93AD96984DC8C42DD56D56FFBB3DAA8721420904EDB9FEDC364B57F1658FD85F86A555C859D8E8C7A86363C814563FD11710F3279681F00EF0C590A27BC6F4811934A4CBADF176450A9343CC8E3E35335ED92F54B902A39F86D9F0B4CF55DBC91B077863A2CD2CB7A5B0CB42605EC740703181E7AC33EE2BD4581F5F29AD4077145A5CF942D26E6B5B95B49BBB90A410EB3421959312504F146AE1D161C4DC9339260F1FCFCDADAB1C785B38714729A8D9F297F68FFBD375849E730D68100000

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
      System.err.println("Classifier 'Level1AggregationFeatures(NEWord)' defined on line 517 of LbjTagger.lbj received '" + type + "' as input.");
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
        __id = "" + (word.domainName + f.featureGroupName);
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
      System.err.println("Classifier 'Level1AggregationFeatures(NEWord)' defined on line 517 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "Level1AggregationFeatures".hashCode(); }
  public boolean equals(Object o) { return o instanceof Level1AggregationFeatures; }
}

