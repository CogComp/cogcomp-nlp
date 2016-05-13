/**
 * This software is released under the University of Illinois/Research and
 *  Academic Use License. See the LICENSE file in the root folder for details.
 * Copyright (c) 2016
 *
 * Developed by:
 * The Cognitive Computation Group
 * University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000058E4BCA02C04C0CF59DB80A05FF0CB86D7028411B287E8DD9AEA4B9D29D4D7D7DBB245A0E14F297C4666235BBB3CE0CC214ADA024E76218DD3AE6689D2E0E5CA9B5C2323391B9578C1CA0E209B29862625A6C9255EE94AEC37268D3F6C7145D7CB213B2A72451090D36B4F25F63AEBB136B7FB9DF3A1CA9BC8047663D2BC89C2E5762EF6C965D6058C694FCD90615F116DA3E35FEC417F7B8BF403771A018223FC9A7EB4BC58ABB2E713FF0D670AAEBDCE50A1C8FCF724100000

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
  private static final additionalFeaturesDiscreteNonConjunctive __additionalFeaturesDiscreteNonConjunctive = new additionalFeaturesDiscreteNonConjunctive();
  private static final additionalFeaturesDiscreteConjunctive __additionalFeaturesDiscreteConjunctive = new additionalFeaturesDiscreteConjunctive();
  private static final additionalFeaturesRealNonConjunctive __additionalFeaturesRealNonConjunctive = new additionalFeaturesRealNonConjunctive();

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
      System.err.println("Classifier 'FeaturesSharedTemp(NEWord)' defined on line 440 of LbjTagger.lbj received '" + type + "' as input.");
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
    __result.addFeatures(__additionalFeaturesDiscreteNonConjunctive.classify(__example));
    __result.addFeatures(__additionalFeaturesDiscreteConjunctive.classify(__example));
    __result.addFeatures(__additionalFeaturesRealNonConjunctive.classify(__example));
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof NEWord[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'FeaturesSharedTemp(NEWord)' defined on line 440 of LbjTagger.lbj received '" + type + "' as input.");
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
    result.add(__additionalFeaturesDiscreteNonConjunctive);
    result.add(__additionalFeaturesDiscreteConjunctive);
    result.add(__additionalFeaturesRealNonConjunctive);
    return result;
  }
}

