// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000BCDCCA84D4155507B4D4C292D2A4D26F94D2B4DC132D0F37D0FCF2A415827021A9A063ABA010549A56999F5A5C12989E109852529A54970158A3A006AD0D13D3DB8253D31B4233F3F06629E02B6234892742133289851005C08CF267BCF227ECFCB294DA82189C0AABA4545C457410002079CFA30B000000

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


public class FeaturesLevel2 extends Classifier
{
  private static final PreviousTagPatternLevel2 __PreviousTagPatternLevel2 = new PreviousTagPatternLevel2();
  private static final Level1AggregationFeatures __Level1AggregationFeatures = new Level1AggregationFeatures();
  private static final PreviousTag1Level2 __PreviousTag1Level2 = new PreviousTag1Level2();
  private static final PreviousTag2Level2 __PreviousTag2Level2 = new PreviousTag2Level2();
  private static final prevTagsForContextLevel2 __prevTagsForContextLevel2 = new prevTagsForContextLevel2();
  private static final FeaturesLevel2$$5 __FeaturesLevel2$$5 = new FeaturesLevel2$$5();

  public FeaturesLevel2()
  {
    containingPackage = "edu.illinois.cs.cogcomp.ner.LbjFeatures";
    name = "FeaturesLevel2";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord"; }
  public String getOutputType() { return "mixed%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof NEWord))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'FeaturesLevel2(NEWord)' defined on line 521 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    FeatureVector __result;
    __result = new FeatureVector();
    __result.addFeatures(__PreviousTagPatternLevel2.classify(__example));
    __result.addFeatures(__Level1AggregationFeatures.classify(__example));
    __result.addFeatures(__PreviousTag1Level2.classify(__example));
    __result.addFeatures(__PreviousTag2Level2.classify(__example));
    __result.addFeatures(__prevTagsForContextLevel2.classify(__example));
    __result.addFeatures(__FeaturesLevel2$$5.classify(__example));
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof NEWord[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'FeaturesLevel2(NEWord)' defined on line 521 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "FeaturesLevel2".hashCode(); }
  public boolean equals(Object o) { return o instanceof FeaturesLevel2; }

  public java.util.LinkedList getCompositeChildren()
  {
    java.util.LinkedList result = new java.util.LinkedList();
    result.add(__PreviousTagPatternLevel2);
    result.add(__Level1AggregationFeatures);
    result.add(__PreviousTag1Level2);
    result.add(__PreviousTag2Level2);
    result.add(__prevTagsForContextLevel2);
    result.add(__FeaturesLevel2$$5);
    return result;
  }
}

