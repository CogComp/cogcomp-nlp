// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000BCDCCA84D415550F37D094C4F4F4D22F94D2B4DC134515134D0F37D0FCF2A415827021A9A063ABA0E69A98525A549A5C01511C9198549A921E99529106EB19E0A9CBF7E5E45AE82000EE857C97D5000000

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


public class NETaggerLevel1$$1 extends Classifier
{
  private static final FeaturesLevel1SharedWithLevel2 __FeaturesLevel1SharedWithLevel2 = new FeaturesLevel1SharedWithLevel2();
  private static final FeaturesLevel1Only __FeaturesLevel1Only = new FeaturesLevel1Only();

  public NETaggerLevel1$$1()
  {
    containingPackage = "edu.illinois.cs.cogcomp.ner.LbjFeatures";
    name = "NETaggerLevel1$$1";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord"; }
  public String getOutputType() { return "mixed%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof NEWord))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'NETaggerLevel1$$1(NEWord)' defined on line 374 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    FeatureVector __result;
    __result = new FeatureVector();
    __result.addFeatures(__FeaturesLevel1SharedWithLevel2.classify(__example));
    __result.addFeatures(__FeaturesLevel1Only.classify(__example));
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof NEWord[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'NETaggerLevel1$$1(NEWord)' defined on line 374 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "NETaggerLevel1$$1".hashCode(); }
  public boolean equals(Object o) { return o instanceof NETaggerLevel1$$1; }

  public java.util.LinkedList getCompositeChildren()
  {
    java.util.LinkedList result = new java.util.LinkedList();
    result.add(__FeaturesLevel1SharedWithLevel2);
    result.add(__FeaturesLevel1Only);
    return result;
  }
}

