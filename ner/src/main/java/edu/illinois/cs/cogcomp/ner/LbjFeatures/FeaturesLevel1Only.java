// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000BCDCCA84D4155507B4D4C292D2A4D26F94D2B4DC134FFCBC9A4D0F37D0FCF2A415827021A9A063ABA010549A56999F5A5C12989E109852529A5497015CA38C2368854CC8062650041302FB8DD2FB8C93F3FA425B2A40623896DBA8A898E82000A9BC72F3D9000000

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


public class FeaturesLevel1Only extends Classifier
{
  private static final PreviousTagPatternLevel1 __PreviousTagPatternLevel1 = new PreviousTagPatternLevel1();
  private static final PreviousTag1Level1 __PreviousTag1Level1 = new PreviousTag1Level1();
  private static final PreviousTag2Level1 __PreviousTag2Level1 = new PreviousTag2Level1();
  private static final prevTagsForContextLevel1 __prevTagsForContextLevel1 = new prevTagsForContextLevel1();
  private static final FeaturesLevel1Only$$4 __FeaturesLevel1Only$$4 = new FeaturesLevel1Only$$4();

  public FeaturesLevel1Only()
  {
    containingPackage = "edu.illinois.cs.cogcomp.ner.LbjFeatures";
    name = "FeaturesLevel1Only";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord"; }
  public String getOutputType() { return "mixed%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof NEWord))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'FeaturesLevel1Only(NEWord)' defined on line 370 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    FeatureVector __result;
    __result = new FeatureVector();
    __result.addFeatures(__PreviousTagPatternLevel1.classify(__example));
    __result.addFeatures(__PreviousTag1Level1.classify(__example));
    __result.addFeatures(__PreviousTag2Level1.classify(__example));
    __result.addFeatures(__prevTagsForContextLevel1.classify(__example));
    __result.addFeatures(__FeaturesLevel1Only$$4.classify(__example));
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof NEWord[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'FeaturesLevel1Only(NEWord)' defined on line 370 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "FeaturesLevel1Only".hashCode(); }
  public boolean equals(Object o) { return o instanceof FeaturesLevel1Only; }

  public java.util.LinkedList getCompositeChildren()
  {
    java.util.LinkedList result = new java.util.LinkedList();
    result.add(__PreviousTagPatternLevel1);
    result.add(__PreviousTag1Level1);
    result.add(__PreviousTag2Level1);
    result.add(__prevTagsForContextLevel1);
    result.add(__FeaturesLevel1Only$$4);
    return result;
  }
}

