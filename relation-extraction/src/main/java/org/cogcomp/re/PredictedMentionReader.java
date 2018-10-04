/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package org.cogcomp.re;

import edu.illinois.cs.cogcomp.chunker.main.ChunkerAnnotator;
import edu.illinois.cs.cogcomp.chunker.main.ChunkerConfigurator;
import edu.illinois.cs.cogcomp.core.constants.Language;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.resources.ResourceConfigurator;
import edu.illinois.cs.cogcomp.edison.utilities.WordNetManager;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.FlatGazetteers;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.Gazetteers;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.GazetteersFactory;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;
import edu.illinois.cs.cogcomp.pipeline.common.Stanford331Configurator;
import edu.illinois.cs.cogcomp.pipeline.handlers.StanfordDepHandler;
import edu.illinois.cs.cogcomp.pos.POSAnnotator;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.ParserAnnotator;
import org.cogcomp.Datastore;
import org.cogcomp.md.BIOFeatureExtractor;
import org.cogcomp.md.MentionAnnotator;

import java.io.File;
import java.util.*;

public class PredictedMentionReader implements Parser{
    public List<Relation> relations;
    public int size_of_gold_relations = 0;
    private int currentRelationIndex;

    public PredictedMentionReader(String path){
        relations = new ArrayList<>();
        try {
            ACEReader aceReader = new ACEReader(path, false);
            POSAnnotator pos_annotator = new POSAnnotator();
            ChunkerAnnotator chunker  = new ChunkerAnnotator(true);
            chunker.initialize(new ChunkerConfigurator().getDefaultConfig());
            MentionAnnotator mentionAnnotator = new MentionAnnotator("ACE_TYPE");

            Datastore ds = new Datastore(new ResourceConfigurator().getDefaultConfig());
            File gazetteersResource = ds.getDirectory("org.cogcomp.gazetteers", "gazetteers", 1.6, false);
            Gazetteers gazetteers = GazetteersFactory.get(5, gazetteersResource.getPath() + File.separator + "gazetteers", 
                true, Language.English);
            WordNetManager.loadConfigAsClasspathResource(true);
            WordNetManager wordNet = WordNetManager.getInstance();
            Properties stanfordProps = new Properties();
            stanfordProps.put("annotators", "pos, parse");
            stanfordProps.put("parse.originalDependencies", true);
            stanfordProps.put("parse.maxlen", Stanford331Configurator.STFRD_MAX_SENTENCE_LENGTH);
            stanfordProps.put("parse.maxtime", Stanford331Configurator.STFRD_TIME_PER_SENTENCE);
            POSTaggerAnnotator posAnnotator = new POSTaggerAnnotator("pos", stanfordProps);
            ParserAnnotator parseAnnotator = new ParserAnnotator("parse", stanfordProps);
            StanfordDepHandler stanfordDepHandler = new StanfordDepHandler(posAnnotator, parseAnnotator);
            for (TextAnnotation ta : aceReader){
                if (ta.getId().equals("bn\\CNN_ENG_20030424_070008.15.apf.xml")){
                    continue;
                }
                ta.addView(pos_annotator);
                mentionAnnotator.addView(ta);
                stanfordDepHandler.addView(ta);
                chunker.addView(ta);

                View annotatedTokenView = new SpanLabelView("RE_ANNOTATED", ta);
                for (Constituent co : ta.getView(ViewNames.TOKENS).getConstituents()){
                    Constituent c = co.cloneForNewView("RE_ANNOTATED");
                    for (String s : co.getAttributeKeys()){
                        c.addAttribute(s, co.getAttribute(s));
                    }
                    c.addAttribute("WORDNETTAG", BIOFeatureExtractor.getWordNetTags(wordNet, c));
                    c.addAttribute("WORDNETHYM", BIOFeatureExtractor.getWordNetHyms(wordNet, c));
                    annotatedTokenView.addConstituent(c);
                }
                ta.addView("RE_ANNOTATED", annotatedTokenView);

                View goldView = ta.getView(ViewNames.MENTION_ACE);
                View predictedView = ta.getView(ViewNames.MENTION);
                Map<Constituent, Constituent> consMap = new HashMap<Constituent, Constituent>();
                for (Constituent c : goldView.getConstituents()){
                    consMap.put(c,null);
                    Constituent ch = RelationFeatureExtractor.getEntityHeadForConstituent(c, ta, "");
                    for (Constituent pc : predictedView.getConstituents()){
                        Constituent pch = MentionAnnotator.getHeadConstituent(pc, "");
                        if (ch.getStartSpan() == pch.getStartSpan() && ch.getEndSpan() == pch.getEndSpan()){
                            consMap.put(c, pc);
                            break;
                        }
                    }
                }
                size_of_gold_relations += goldView.getRelations().size();
                for (int i = 0; i < ta.getNumberOfSentences(); i++){
                    Sentence curSentence = ta.getSentence(i);
                    List<Constituent> in_cur_sentence = predictedView.getConstituentsCoveringSpan(curSentence.getStartSpan(), curSentence.getEndSpan());
                    for (int j = 0; j < in_cur_sentence.size(); j++){
                        for (int k = j + 1; k < in_cur_sentence.size(); k++){
                            Constituent source = in_cur_sentence.get(j);
                            Constituent target = in_cur_sentence.get(k);
                            Constituent sourceHead = MentionAnnotator.getHeadConstituent(source, "");
                            Constituent targetHead = MentionAnnotator.getHeadConstituent(target, "");
                            source.addAttribute("GAZ", ((FlatGazetteers) gazetteers).annotatePhrase(sourceHead));
                            target.addAttribute("GAZ", ((FlatGazetteers)gazetteers).annotatePhrase(targetHead));

                            boolean found_tag = false;
                            for (Relation r : goldView.getRelations()){

                                if (consMap.get(r.getSource()) == null || consMap.get(r.getTarget()) == null){
                                    continue;
                                }

                                Constituent gsh = ACEReader.getEntityHeadForConstituent(r.getSource(), ta, "A");
                                Constituent gth = ACEReader.getEntityHeadForConstituent(r.getTarget(), ta, "A");
                                Constituent psh = MentionAnnotator.getHeadConstituent(source, "B");
                                Constituent pth = MentionAnnotator.getHeadConstituent(target, "B");

                                if (gsh.getStartSpan() == psh.getStartSpan() && gsh.getEndSpan() == psh.getEndSpan()
                                        && gth.getStartSpan() == pth.getStartSpan() && gth.getEndSpan() == pth.getEndSpan()){
                                    Relation newRelation = new Relation(r.getAttribute("RelationSubtype"), source, target, 1.0f);
                                    newRelation.addAttribute("RelationType", r.getAttribute("RelationType"));
                                    newRelation.addAttribute("RelationSubtype", r.getAttribute("RelationSubtype"));
                                    newRelation.addAttribute("IsGoldRelation", "True");
                                    relations.add(newRelation);
                                    found_tag = true;
                                    break;
                                }
                                if (gsh.getStartSpan() == pth.getStartSpan() && gsh.getEndSpan() == pth.getEndSpan()
                                        && gth.getStartSpan() == psh.getStartSpan() && gth.getEndSpan() == psh.getEndSpan()){
                                    Relation newRelation = new Relation(r.getAttribute("RelationSubtype"), target, source, 1.0f);
                                    newRelation.addAttribute("RelationType", r.getAttribute("RelationType"));
                                    newRelation.addAttribute("RelationSubtype", r.getAttribute("RelationSubtype"));
                                    newRelation.addAttribute("IsGoldRelation", "True");
                                    relations.add(newRelation);
                                    found_tag = true;
                                    break;
                                }
                            }
                            if (!found_tag){
                                Relation newRelation = new Relation("NOT_RELATED", source, target, 1.0f);
                                newRelation.addAttribute("RelationType", "NOT_RELATED");
                                newRelation.addAttribute("RelationSubtype", "NOT_RELATED");
                                newRelation.addAttribute("IsGoldRelation", "False");
                                relations.add(newRelation);
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void close(){

    }
    public Object next(){
        if (currentRelationIndex == relations.size()) {
            return null;
        } else {
            currentRelationIndex++;
            return relations.get(currentRelationIndex - 1);
        }
    }

    public void reset(){
        currentRelationIndex = 0;
    }
}
