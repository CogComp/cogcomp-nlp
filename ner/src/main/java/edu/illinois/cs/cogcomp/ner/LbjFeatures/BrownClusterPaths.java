// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000560915B43C04C07CFBA4C183A5A6B88F8EDAE3834F541928A8F036F0D679E675AEDD84EA6722E77737D67E6E4E0EE842FFCF2FFCDA437948E0FC1E69C6766ED4BCE092BCDD68387ABB73B4B28E4EA016613C718EA028C27ACFD5A388FED2D3615FCDEA0392B52243EE05B4A2CCD5B48CF26F595AE6D8BC5B1E70CFC062FF62D42C050E7C96682A484FF6A2953AB02450AD830DA06FEE446ED1E504393BB130505952804B48792D003B41EA06A3D1427692896B964144196F377CEB17DD0BF83138B37A0EBF48373017FC9E29D21E786BD2F18617CD3DAB364D147FBC252A09E1F344D9A8DF05FC87674ADCA71B48DAFFD11D415AF5E332CA4FE0938E2CDF282F515F06EA689D02F4A143B67B19C44197813A1641FE7D349715F2DB71D154EF193F30DB1196BE20200000

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


public class BrownClusterPaths extends Classifier
{
  public BrownClusterPaths()
  {
    containingPackage = "edu.illinois.cs.cogcomp.ner.LbjFeatures";
    name = "BrownClusterPaths";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof NEWord))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'BrownClusterPaths(NEWord)' defined on line 108 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    NEWord word = (NEWord) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    if (ParametersForLbjCode.currentParameters.featuresToUse.containsKey("BrownClusterPaths"))
    {
      BrownClusters bc = BrownClusters.get();
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
        String[] paths = bc.getPrefixes(w);
        for (int j = 0; j < paths.length; j++)
        {
          __id = "" + (i);
          __value = "" + (paths[j]);
          __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
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
      System.err.println("Classifier 'BrownClusterPaths(NEWord)' defined on line 108 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "BrownClusterPaths".hashCode(); }
  public boolean equals(Object o) { return o instanceof BrownClusterPaths; }
}

