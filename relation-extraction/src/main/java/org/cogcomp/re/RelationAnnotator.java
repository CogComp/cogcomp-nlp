/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package org.cogcomp.re;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
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

import java.io.File;
import java.util.List;

public class RelationAnnotator extends Annotator {

    private relation_classifier relationClassifier;
    private ACERelationConstrainedClassifier constrainedClassifier;
    private Gazetteers gazetteers;
    private WordNetManager wordNet;
    private MentionAnnotator mentionAnnotator;

    public RelationAnnotator() {
        this(true);
    }


    public RelationAnnotator(boolean lazilyInitialize) {
        super("RELATION_EXTRACTION", new String[]{ViewNames.POS, ViewNames.DEPENDENCY_STANFORD, ViewNames.SHALLOW_PARSE}, lazilyInitialize);
        File modelFile = new File("models/ACE_GOLD_BI.lc");
        File lexFile = new File("models/ACE_GOLD_BI.lex");
        relationClassifier = new relation_classifier(modelFile.getAbsolutePath(), lexFile.getAbsolutePath());
        constrainedClassifier = new ACERelationConstrainedClassifier(relationClassifier);
    }

    @Override
    public void initialize(ResourceManager rm) {
        try {
            mentionAnnotator = new MentionAnnotator("ACE_TYPE");
            Datastore ds = new Datastore(new ResourceConfigurator().getDefaultConfig());
            File gazetteersResource = ds.getDirectory("org.cogcomp.gazetteers", "gazetteers", 1.6, false);
            GazetteersFactory.init(5, gazetteersResource.getPath() + File.separator + "gazetteers", true);
            WordNetManager.loadConfigAsClasspathResource(true);
            wordNet = WordNetManager.getInstance();
            gazetteers = GazetteersFactory.get();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void addView(TextAnnotation record) throws AnnotatorException {
        if (!isInitialized()){
            doInitialize();
        }
        if (!record.hasView(ViewNames.POS) ){
            throw new AnnotatorException("Missing required view POS");
        }
        if (!record.hasView(ViewNames.DEPENDENCY_STANFORD)){
            throw new AnnotatorException("Missing required view DEPENDENCY_STANFORD");
        }
        if (!record.hasView(ViewNames.SHALLOW_PARSE)){
            throw new AnnotatorException("Missing required view SHALLOW_PARSE");
        }
        mentionAnnotator.addView(record);
        View mentionView = record.getView(ViewNames.MENTION);
        View annotatedTokenView = new SpanLabelView("RE_ANNOTATED", record);
        for (Constituent co : record.getView(ViewNames.TOKENS).getConstituents()){
            Constituent c = co.cloneForNewView("RE_ANNOTATED");
            for (String s : co.getAttributeKeys()){
                c.addAttribute(s, co.getAttribute(s));
            }
            c.addAttribute("WORDNETTAG", BIOFeatureExtractor.getWordNetTags(wordNet, c));
            c.addAttribute("WORDNETHYM", BIOFeatureExtractor.getWordNetHyms(wordNet, c));
            annotatedTokenView.addConstituent(c);
        }
        record.addView("RE_ANNOTATED", annotatedTokenView);
        for (int i = 0; i < record.getNumberOfSentences(); i++){
            Sentence curSentence = record.getSentence(i);
            List<Constituent> cins = mentionView.getConstituentsCoveringSpan(curSentence.getStartSpan(), curSentence.getEndSpan());
            for (int j = 0; j < cins.size(); j++){
                for (int k = j + 1; k < cins.size(); k++){
                    if (k == j) continue;
                    Constituent source = cins.get(j);
                    Constituent target = cins.get(k);
                    Constituent sourceHead = MentionAnnotator.getHeadConstituent(source, "");
                    Constituent targetHead = MentionAnnotator.getHeadConstituent(target, "");
                    source.addAttribute("GAZ", ((FlatGazetteers) gazetteers).annotatePhrase(sourceHead));
                    target.addAttribute("GAZ", ((FlatGazetteers)gazetteers).annotatePhrase(targetHead));
                    Relation for_test_forward = new Relation("PredictedRE", source, target, 1.0f);
                    Relation for_test_backward = new Relation("PredictedRE", target, source, 1.0f);
                    //String tag_forward = constrainedClassifier.discreteValue(for_test_forward);
                    //String tag_backward = constrainedClassifier.discreteValue(for_test_backward);
                    String tag_forward = relationClassifier.discreteValue(for_test_forward);
                    String tag_backward = relationClassifier.discreteValue(for_test_backward);

                    if (tag_forward.equals(ACEMentionReader.getOppoName(tag_backward)) && !tag_forward.equals("NOT_RELATED")){
                        String tag = tag_forward;
                        Constituent first = source;
                        Constituent second = target;
                        if (tag_forward.length() > tag_backward.length()){
                            tag = tag_backward;
                            first = target;
                            second = source;
                        }
                        String coarseType = ACERelationTester.getCoarseType(tag);
                        Relation r = new Relation(coarseType, first, second, 1.0f);
                        r.addAttribute("RelationType", coarseType);
                        r.addAttribute("RelationSubtype", tag);
                        mentionView.addRelation(r);
                    }
                    if (!tag_forward.equals(ACEMentionReader.getOppoName(tag_backward)) &&
                            (!tag_forward.equals("NOT_RELATED") || !tag_backward.equals("NOT_RELATED"))){
                        double forward_score = 0.0;
                        double backward_score = 0.0;
                        ScoreSet scores = relationClassifier.scores(for_test_forward);
                        Score[] scoresArray = scores.toArray();
                        for (Score s : scoresArray){
                            if (s.value.equals(tag_forward)){
                                forward_score = s.score;
                            }
                        }
                        scores = relationClassifier.scores(for_test_backward);
                        scoresArray = scores.toArray();
                        for (Score s : scoresArray){
                            if (s.value.equals(tag_forward)){
                                backward_score = s.score;
                            }
                        }
                        String tag = tag_forward;
                        Constituent first = source;
                        Constituent second = target;
                        if (backward_score > forward_score){
                            tag = tag_backward;
                            first = target;
                            second = source;
                        }
                        if (!tag.equals("NOT_RELATED")){
                            String coarseType = ACERelationTester.getCoarseType(tag);
                            Relation r = new Relation(coarseType, first, second, 1.0f);
                            r.addAttribute("RelationType", coarseType);
                            r.addAttribute("RelationSubtype", tag);
                            mentionView.addRelation(r);
                        }
                    }
                }
            }
        }
        record.addView(ViewNames.MENTION, mentionView);
    }
}