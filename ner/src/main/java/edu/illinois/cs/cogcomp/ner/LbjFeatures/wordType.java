// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D9E81CA02C03C068F5524148D01BF00AE544C188368789CEC57DC4A2EA91D4B8C01FDDDE68AB3B29B42CFF3F5E3D68B678E17607727ABCEB3C4A8D5517D1FE416DB08708960293A27AAD8547C919BCFC7DD296495707E0DAF9239D0A2F1C1279472E89395FAC85E306F98818A52576AE2D8FB1EED63422D432D91D2328010B48F6563657EAA7A0E3B73F4E50D75F13C15D65FB86506CC673D3B1002535B1DDA885189FF3FEF93CCB00804D334504100000

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


public class wordType extends Classifier
{
  public wordType()
  {
    containingPackage = "edu.illinois.cs.cogcomp.ner.LbjFeatures";
    name = "wordType";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof NEWord))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'wordType(NEWord)' defined on line 18 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    NEWord word = (NEWord) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    if (ParametersForLbjCode.currentParameters.featuresToUse.containsKey("WordTopicTitleInfo"))
    {
      __id = "";
      __value = "" + (WordTopicAndLayoutFeatures.getWordType(word));
      __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
      if (ParametersForLbjCode.currentParameters.useFE)
      {
        __id = "" + (word.domainName + "");
        __value = "" + (WordTopicAndLayoutFeatures.getWordType(word));
        __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
      }
    }
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof NEWord[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'wordType(NEWord)' defined on line 18 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "wordType".hashCode(); }
  public boolean equals(Object o) { return o instanceof wordType; }
}

