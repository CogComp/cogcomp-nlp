// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D6091CA43C040168F55E7B069484314C3ABD8792A715A8705C3FA9C40642DDD2BBB13A69EBBB3B96A64B49040667EF9FE366A66F593A04BB647BE96180EA3ADF5265A7BC147BCFDA30B539CAFEE5DABA1DBCF25C237CE0CD02972DE4F6466D9FB7BEE1FDED756B6A2AAEC932316AE51D09E0D932FFC6F5CB4FDA90A9D8F70AFA466FF533B43D846310CA07452AC14BB04BAD781B0586CA342C25E592036952EA13F9F1227152C47D6BA0956C1973ECD8B34A34D581AFC0A0BF31ECD22F183D71B574F1C6B3F31D2FC70A5FF71535EE795A421233152AE43C15A191E9C8721DDD8CB557CB009C657864CD0B8ADE64EA4B69B122B3715EC2B8E49FE70076838C902C100000

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


public class Capitalization extends Classifier
{
  public Capitalization()
  {
    containingPackage = "edu.illinois.cs.cogcomp.ner.LbjFeatures";
    name = "Capitalization";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord"; }
  public String getOutputType() { return "discrete%"; }

  private static String[] __allowableValues = DiscreteFeature.BooleanValues;
  public static String[] getAllowableValues() { return __allowableValues; }
  public String[] allowableValues() { return __allowableValues; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof NEWord))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'Capitalization(NEWord)' defined on line 178 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    NEWord word = (NEWord) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    if (ParametersForLbjCode.currentParameters.featuresToUse.containsKey("Capitalization"))
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
        __id = "" + (i);
        __value = "" + (w.capitalized);
        __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 2));
        __id = "" + (word.domainName + i);
        __value = "" + (w.capitalized);
        __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 2));
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
      System.err.println("Classifier 'Capitalization(NEWord)' defined on line 178 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "Capitalization".hashCode(); }
  public boolean equals(Object o) { return o instanceof Capitalization; }
}

