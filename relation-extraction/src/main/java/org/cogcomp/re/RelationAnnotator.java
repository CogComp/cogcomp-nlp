/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package org.cogcomp.re;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.constants.Language;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.resources.ResourceConfigurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.edison.utilities.WordNetManager;
import edu.illinois.cs.cogcomp.lbjava.classify.Score;
import edu.illinois.cs.cogcomp.lbjava.classify.ScoreSet;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.FlatGazetteers;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.Gazetteers;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.GazetteersFactory;
import org.cogcomp.Datastore;
import org.cogcomp.md.BIOFeatureExtractor;
import org.cogcomp.md.MentionAnnotator;
import org.cogcomp.re.LbjGen.relation_classifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class RelationAnnotator extends Annotator {

    private static Logger logger = LoggerFactory.getLogger(RelationAnnotator.class);

    private relation_classifier relationClassifier;
    private ACERelationConstrainedClassifier constrainedClassifier;
    private Gazetteers gazetteers;
    private WordNetManager wordNet;

    public RelationAnnotator() {
        this(true);
    }

    public RelationAnnotator(boolean lazilyInitialize) {
        super(ViewNames.RELATION, new String[]{ViewNames.MENTION, ViewNames.POS, ViewNames.DEPENDENCY_STANFORD, ViewNames.SHALLOW_PARSE}, lazilyInitialize);
    }

    @Override
    public void initialize(ResourceManager rm) {
        try {
            Datastore ds = new Datastore(new ResourceConfigurator().getDefaultConfig());
            File modelDir = ds.getDirectory("org.cogcomp.re", "ACE_GOLD_BI", 1.0, false);
            String modelFile = modelDir.getPath() + File.separator + "ACE_GOLD_BI" + File.separator + "ACE_GOLD_BI.lc";
            String lexFile = modelDir.getPath() + File.separator + "ACE_GOLD_BI" + File.separator + "ACE_GOLD_BI.lex";
            relationClassifier = new relation_classifier();
            relationClassifier.readModel(modelFile);
            relationClassifier.readLexicon(lexFile);
            constrainedClassifier = new ACERelationConstrainedClassifier(relationClassifier);
            File gazetteersResource = ds.getDirectory("org.cogcomp.gazetteers", "gazetteers", 1.6, false);
            gazetteers = GazetteersFactory.get(5, gazetteersResource.getPath() + File.separator + "gazetteers", 
                true, Language.English);
            WordNetManager.loadConfigAsClasspathResource(true);
            wordNet = WordNetManager.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addView(TextAnnotation record) throws AnnotatorException {
        if (!isInitialized()) {
            doInitialize();
        }
        if (!record.hasView(ViewNames.POS)) {
            throw new AnnotatorException("Missing required view POS");
        }
        if (!record.hasView(ViewNames.DEPENDENCY_STANFORD)) {
            throw new AnnotatorException("Missing required view DEPENDENCY_STANFORD");
        }
        if (!record.hasView(ViewNames.SHALLOW_PARSE)) {
            throw new AnnotatorException("Missing required view SHALLOW_PARSE");
        }
        if (!record.hasView(ViewNames.MENTION)) {
            throw new AnnotatorException("Missing required view MENTION");
        }
        View mentionView = record.getView(ViewNames.MENTION);
        View relationView = new SpanLabelView(ViewNames.RELATION, record);
        //Add the original mention view if no mentions are predicted.
        if (mentionView.getConstituents().size() == 0){
            record.addView(ViewNames.RELATION, relationView);
            return;
        }
        if (mentionView.getConstituents().get(0).getAttribute("EntityType").equals("MENTION")) {
            logger.error("The mentions don't have types; this will cause poor performance in predictions.. . ");
        }
        View annotatedTokenView = new SpanLabelView("RE_ANNOTATED", record);
        for (Constituent co : record.getView(ViewNames.TOKENS).getConstituents()) {
            Constituent c = co.cloneForNewView("RE_ANNOTATED");
            for (String s : co.getAttributeKeys()) {
                c.addAttribute(s, co.getAttribute(s));
            }
            c.addAttribute("WORDNETTAG", BIOFeatureExtractor.getWordNetTags(wordNet, c));
            c.addAttribute("WORDNETHYM", BIOFeatureExtractor.getWordNetHyms(wordNet, c));
            annotatedTokenView.addConstituent(c);
        }
        record.addView("RE_ANNOTATED", annotatedTokenView);

        for (int i = 0; i < record.getNumberOfSentences(); i++) {
            Sentence curSentence = record.getSentence(i);
            List<Constituent> cins = mentionView.getConstituentsCoveringSpan(curSentence.getStartSpan(), curSentence.getEndSpan());
            for (int j = 0; j < cins.size(); j++) {
                for (int k = j + 1; k < cins.size(); k++) {
                    if (k == j) continue;
                    Constituent source = cins.get(j);
                    Constituent target = cins.get(k);
                    Constituent sourceHead = MentionAnnotator.getHeadConstituent(source, "");
                    Constituent targetHead = MentionAnnotator.getHeadConstituent(target, "");
                    source.addAttribute("GAZ", ((FlatGazetteers) gazetteers).annotatePhrase(sourceHead));
                    target.addAttribute("GAZ", ((FlatGazetteers) gazetteers).annotatePhrase(targetHead));
                    Relation for_test_forward = new Relation("PredictedRE", source, target, 1.0f);
                    Relation for_test_backward = new Relation("PredictedRE", target, source, 1.0f);
                    String tag_forward = constrainedClassifier.discreteValue(for_test_forward);
                    String tag_backward = constrainedClassifier.discreteValue(for_test_backward);

                    if (tag_forward.equals(ACEMentionReader.getOppoName(tag_backward)) && !tag_forward.equals("NOT_RELATED")) {
                        String tag = tag_forward;
                        Constituent first = source;
                        Constituent second = target;
                        if (tag_forward.length() > tag_backward.length()) {
                            tag = tag_backward;
                            first = target;
                            second = source;
                        }
                        String coarseType = ACERelationTester.getCoarseType(tag);
                        Constituent firstMention = first.cloneForNewView(ViewNames.RELATION);
                        Constituent secondMention = second.cloneForNewView(ViewNames.RELATION);
                        Relation r = new Relation(coarseType + "-" + tag, firstMention, secondMention, 1.0f);
                        r.addAttribute("RelationType", coarseType);
                        r.addAttribute("RelationSubtype", tag);
                        relationView.addConstituent(firstMention);
                        relationView.addConstituent(secondMention);
                        relationView.addRelation(r);
                    }
                    if (!tag_forward.equals(ACEMentionReader.getOppoName(tag_backward)) &&
                            (!tag_forward.equals("NOT_RELATED") || !tag_backward.equals("NOT_RELATED"))) {
                        double forward_score = 0.0;
                        double backward_score = 0.0;
                        ScoreSet scores = relationClassifier.scores(for_test_forward);
                        Score[] scoresArray = scores.toArray();
                        for (Score s : scoresArray) {
                            if (s.value.equals(tag_forward)) {
                                forward_score = s.score;
                            }
                        }
                        scores = relationClassifier.scores(for_test_backward);
                        scoresArray = scores.toArray();
                        for (Score s : scoresArray) {
                            if (s.value.equals(tag_forward)) {
                                backward_score = s.score;
                            }
                        }
                        String tag = tag_forward;
                        Constituent first = source;
                        Constituent second = target;
                        if (forward_score < backward_score && backward_score - forward_score > 0.005) {
                            tag = tag_backward;
                            first = target;
                            second = source;
                        }
                        if (!tag.equals("NOT_RELATED")) {
                            Constituent firstMention = first.cloneForNewView(ViewNames.RELATION);
                            Constituent secondMention = second.cloneForNewView(ViewNames.RELATION);
                            Relation r;
                            String coarseType = ACERelationTester.getCoarseType(tag);
                            if (tag.contains("_OP")){
                                tag = ACEMentionReader.getOppoName(tag);
                                r = new Relation(coarseType + "-" + tag, secondMention, firstMention, 1.0f);
                            }
                            else {
                                r = new Relation(coarseType + "-" + tag, firstMention, secondMention, 1.0f);
                            }
                            r.addAttribute("RelationType", coarseType);
                            r.addAttribute("RelationSubtype", tag);
                            relationView.addConstituent(firstMention);
                            relationView.addConstituent(secondMention);
                            relationView.addRelation(r);
                        }
                    }
                }
            }
        }
        record.addView(ViewNames.RELATION, relationView);
    }

}