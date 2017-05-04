// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000DAF8F5B43C03415CFBAC520E848EA54D74BBD044491825C70548DB56BED4719631DCD40FFEEBBB775B031446B709B0901ECFE4EC9B5B5A550C887407E6C8D754A5CC56579F0E34D0F2C782894E0F106D08CB5D1477C460AB2F1E669F871EB6C265924047177A516057C4109EECFD31BEED54D6D1D53EB94134280503A11C18FD5BAD539473850E372DD2941FEB61A4D6B29B8051C4BF69581F1AB2A547D4C5B45520F314E610BC0C949C7D46A07A524699553C63FBD60338E552474802613FCF9E350C9DF800A4B4A81CAB64E1F899D2704B70A6FD1FE9517D68CE033F687EF9A82DF995514EB7B9EE77F6EB7EB0F3808E9DC2200000

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


public class AffixesZH extends Classifier
{
  public AffixesZH()
  {
    containingPackage = "edu.illinois.cs.cogcomp.ner.LbjFeatures";
    name = "AffixesZH";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof NEWord))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'AffixesZH(NEWord)' defined on line 250 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    NEWord word = (NEWord) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    if (ParametersForLbjCode.currentParameters.featuresToUse.containsKey("Affixes") && ParametersForLbjCode.currentParameters.language.equals("zh"))
    {
      int N = word.form.length();
      for (int i = 1; i <= 2; ++i)
      {
        if (word.form.length() > i)
        {
          __id = "ZH-p|";
          __value = "" + (word.form.substring(0, i));
          __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
          __id = "" + (word.domainName + "ZH-p|");
          __value = "" + (word.form.substring(0, i));
          __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
        }
      }
      for (int i = 1; i <= 2; ++i)
      {
        if (word.form.length() > i)
        {
          __id = "ZH-s|";
          __value = "" + (word.form.substring(N - i));
          __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
          __id = "" + (word.domainName + "ZH-s|");
          __value = "" + (word.form.substring(N - i));
          __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
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
      System.err.println("Classifier 'AffixesZH(NEWord)' defined on line 250 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "AffixesZH".hashCode(); }
  public boolean equals(Object o) { return o instanceof AffixesZH; }
}

