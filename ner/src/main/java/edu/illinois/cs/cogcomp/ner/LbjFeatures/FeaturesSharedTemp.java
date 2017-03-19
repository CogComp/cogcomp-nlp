// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000054E81CA02C03C068F55A71141AEB1871D1B9E04460B1C0CB5C53575C5BD1966C47F4F658ACE2948F2CFF526DCB15D644180C31168AB70245D0AD17B7BCB5F4A4CC1BCE4C16F2AC053A3647D16DC04C254603A1681CC20C6CB392C97775FD1C0F7794176850991192CACA0F46BA89F436C6FB33DC764C2D9E8029CE8A57CFC2E6F44E7679D035064AA08BF418CDE305923EE9BA9B53F23AD02DA4294EE7192EB0EE131C988E000000

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


public class FeaturesSharedTemp extends Classifier
{
  private static final IsSentenceStart __IsSentenceStart = new IsSentenceStart();
  private static final Capitalization __Capitalization = new Capitalization();
  private static final nonLocalFeatures __nonLocalFeatures = new nonLocalFeatures();
  private static final GazetteersFeatures __GazetteersFeatures = new GazetteersFeatures();
  private static final FormParts __FormParts = new FormParts();
  private static final Forms __Forms = new Forms();
  private static final WordTypeInformation __WordTypeInformation = new WordTypeInformation();
  private static final Affixes __Affixes = new Affixes();
  private static final BrownClusterPaths __BrownClusterPaths = new BrownClusterPaths();
  private static final WordEmbeddingFeatures __WordEmbeddingFeatures = new WordEmbeddingFeatures();
  private static final WikifierFeatures __WikifierFeatures = new WikifierFeatures();
  private static final AffixesZH __AffixesZH = new AffixesZH();

  public FeaturesSharedTemp()
  {
    containingPackage = "edu.illinois.cs.cogcomp.ner.LbjFeatures";
    name = "FeaturesSharedTemp";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord"; }
  public String getOutputType() { return "mixed%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof NEWord))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'FeaturesSharedTemp(NEWord)' defined on line 366 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    FeatureVector __result;
    __result = new FeatureVector();
    __result.addFeatures(__IsSentenceStart.classify(__example));
    __result.addFeatures(__Capitalization.classify(__example));
    __result.addFeatures(__nonLocalFeatures.classify(__example));
    __result.addFeatures(__GazetteersFeatures.classify(__example));
    __result.addFeatures(__FormParts.classify(__example));
    __result.addFeatures(__Forms.classify(__example));
    __result.addFeatures(__WordTypeInformation.classify(__example));
    __result.addFeatures(__Affixes.classify(__example));
    __result.addFeatures(__BrownClusterPaths.classify(__example));
    __result.addFeatures(__WordEmbeddingFeatures.classify(__example));
    __result.addFeatures(__WikifierFeatures.classify(__example));
    __result.addFeatures(__AffixesZH.classify(__example));
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof NEWord[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'FeaturesSharedTemp(NEWord)' defined on line 366 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "FeaturesSharedTemp".hashCode(); }
  public boolean equals(Object o) { return o instanceof FeaturesSharedTemp; }

  public java.util.LinkedList getCompositeChildren()
  {
    java.util.LinkedList result = new java.util.LinkedList();
    result.add(__IsSentenceStart);
    result.add(__Capitalization);
    result.add(__nonLocalFeatures);
    result.add(__GazetteersFeatures);
    result.add(__FormParts);
    result.add(__Forms);
    result.add(__WordTypeInformation);
    result.add(__Affixes);
    result.add(__BrownClusterPaths);
    result.add(__WordEmbeddingFeatures);
    result.add(__WikifierFeatures);
    result.add(__AffixesZH);
    return result;
  }
}

