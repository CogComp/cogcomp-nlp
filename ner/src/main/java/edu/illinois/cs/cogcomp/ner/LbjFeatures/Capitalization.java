// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000590914F43C03C058FFA8994C4DAAB658832959BC4BD5043170017E0DAB291579C41724189AFFD17A47416AD5054A44E8F9FB72FBA98B278E1F0D8E691710ED50CEF2165A7F4E57B4F9AD39539C67DFC6D5D0D9C592C23783005309C3867A7723BE8736DDDFBCBEAC6D85451C9343E7AE51D0A6F1C12F3AD7269EB53E5391EB3CF8466F7D66696A19C6C309283A524938DE20A5DCE7C241436D142425E59202856907D03F9F7B4E2A4031AD651469154ED8373EE09E057160FDDB28EF4837B09F0C9EA8DB3C732B1872A5E9F04BEE73A62DDF0B49824662A98A351743D8C0643C8267732F75D131049DAEF11A606CDCA7285C45A8ADEE429DDA8402B378FE182BC2E327EB00C2426E9F8F100000

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
      System.err.println("Classifier 'Capitalization(NEWord)' defined on line 196 of LbjTagger.lbj received '" + type + "' as input.");
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
        if (ParametersForLbjCode.currentParameters.useFE)
        {
          __id = "" + (word.domainName + i);
          __value = "" + (w.capitalized);
          __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 2));
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
      System.err.println("Classifier 'Capitalization(NEWord)' defined on line 196 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "Capitalization".hashCode(); }
  public boolean equals(Object o) { return o instanceof Capitalization; }
}

