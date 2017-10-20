package org.cogcomp.re;
import edu.illinois.cs.cogcomp.chunker.main.ChunkerAnnotator;
import edu.illinois.cs.cogcomp.chunker.main.ChunkerConfigurator;
import edu.illinois.cs.cogcomp.core.resources.ResourceConfigurator;
import edu.illinois.cs.cogcomp.edison.utilities.WordNetManager;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.BrownClusters;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.FlatGazetteers;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.Gazetteers;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.GazetteersFactory;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import edu.illinois.cs.cogcomp.pipeline.common.Stanford331Configurator;
import edu.illinois.cs.cogcomp.pipeline.handlers.StanfordDepHandler;
import edu.illinois.cs.cogcomp.edison.annotators.*;
import edu.illinois.cs.cogcomp.pos.POSAnnotator;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.ParserAnnotator;
import org.cogcomp.Datastore;
import org.cogcomp.md.BIOFeatureExtractor;

import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.lang.*;

public class ACEMentionReader implements Parser, Serializable
{
    private List<Relation> relations_mono;
    private List<Relation> relations_bi;
    private String readType;
    private int relationIdx;

    public static String getOppoName(String name){
        if (name.equals("Family") || name.equals("Lasting-Personal") || name.equals("Near") || name.equals("Business") || name.equals("NOT_RELATED")){
            return name;
        }
        if (name.contains("_OP")){
            return name.substring(0, name.length() - 3);
        }
        return name + "_OP";
    }

    public static List<String> getTypes(){
        String[] arr = new String[]{"Org-Location_OP", "Employment_OP", "Lasting-Personal", "Sports-Affiliation_OP", "Founder", "Investor-Shareholder", "Founder_OP", "Sports-Affiliation", "Employment", "Located", "Subsidiary", "Org-Location", "Membership", "Citizen-Resident-Religion-Ethnicity", "Geographical_OP", "Citizen-Resident-Religion-Ethnicity_OP", "User-Owner-Inventor-Manufacturer_OP", "Business", "Subsidiary_OP", "Membership_OP", "Near", "Geographical", "Investor-Shareholder_OP", "User-Owner-Inventor-Manufacturer", "Located_OP", "Family"};
        return new ArrayList<String>(Arrays.asList(arr));
    }

    public static Relation getOppoRelation(Relation r){
        return new Relation("TO_TEST", r.getTarget(), r.getSource(), 1.0f);
    }
    public ACEMentionReader(String file, String type) {
        readType = type;
        relations_mono = new ArrayList<>();
        relations_bi = new ArrayList<>();

        try {
            ACEReader reader = new ACEReader(file, new String[]{"bn", "nw"}, false);
            POSAnnotator pos_annotator = new POSAnnotator();
            ChunkerAnnotator chunker  = new ChunkerAnnotator(true);
            chunker.initialize(new ChunkerConfigurator().getDefaultConfig());
            Datastore ds = new Datastore(new ResourceConfigurator().getDefaultConfig());
            File gazetteersResource = ds.getDirectory("org.cogcomp.gazetteers", "gazetteers", 1.3, false);
            GazetteersFactory.init(5, gazetteersResource.getPath() + File.separator + "gazetteers", true);
            WordNetManager.loadConfigAsClasspathResource(true);
            WordNetManager wordNet = WordNetManager.getInstance();
            Gazetteers gazetteers = GazetteersFactory.get();
            Properties stanfordProps = new Properties();
            stanfordProps.put("annotators", "pos, parse");
            stanfordProps.put("parse.originalDependencies", true);
            stanfordProps.put("parse.maxlen", Stanford331Configurator.STFRD_MAX_SENTENCE_LENGTH);
            stanfordProps.put("parse.maxtime", Stanford331Configurator.STFRD_TIME_PER_SENTENCE);
            POSTaggerAnnotator posAnnotator = new POSTaggerAnnotator("pos", stanfordProps);
            ParserAnnotator parseAnnotator = new ParserAnnotator("parse", stanfordProps);
            StanfordDepHandler stanfordDepHandler = new StanfordDepHandler(posAnnotator, parseAnnotator);

            for (TextAnnotation ta : reader) {
                if (ta.getId().equals("bn\\CNN_ENG_20030424_070008.15.apf.xml")){
                    continue;
                }
                ta.addView(pos_annotator);
                stanfordDepHandler.addView(ta);
                chunker.addView(ta);

                View entityView = ta.getView(ViewNames.MENTION_ACE);
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

                List<Relation> existRelations = entityView.getRelations();
                for (int i = 0; i < ta.getNumberOfSentences(); i++){
                    Sentence curSentence= ta.getSentence(i);
                    List<Constituent> cins = entityView.getConstituentsCoveringSpan(curSentence.getStartSpan(), curSentence.getEndSpan());
                    for (int j = 0; j < cins.size(); j++){
                        for (int k = j + 1; k < cins.size(); k++){
                            Constituent firstArg = cins.get(j);
                            Constituent secondArg = cins.get(k);
                            Constituent firstArgHead = RelationFeatureExtractor.getEntityHeadForConstituent(firstArg, firstArg.getTextAnnotation(), "A");
                            Constituent secondArgHead = RelationFeatureExtractor.getEntityHeadForConstituent(secondArg, secondArg.getTextAnnotation(), "A");
                            firstArg.addAttribute("GAZ", ((FlatGazetteers) gazetteers).annotatePhrase(firstArgHead));
                            secondArg.addAttribute("GAZ", ((FlatGazetteers)gazetteers).annotatePhrase(secondArgHead));

                            boolean found_as_source = false;
                            boolean found_as_target = false;
                            for (Relation r : existRelations){
                                //if (r.getSource() == firstArg && r.getTarget() == secondArg){
                                if (r.getSource().getStartSpan() == firstArg.getStartSpan() && r.getSource().getEndSpan() == firstArg.getEndSpan()
                                        && r.getTarget().getStartSpan() == secondArg.getStartSpan() && r.getTarget().getEndSpan() == secondArg.getEndSpan()){
                                    relations_mono.add(r);
                                    Relation opdirNeg = new Relation("NOT_RELATED", secondArg, firstArg, 1.0f);
                                    opdirNeg.addAttribute("RelationType", "NOT_RELATED");
                                    found_as_source = true;
                                    String opTypeName = getOppoName(r.getAttribute("RelationSubtype"));
                                    Relation opdir = new Relation(opTypeName, secondArg, firstArg, 1.0f);
                                    opdir.addAttribute("RelationSubtype", opTypeName);
                                    opdir.addAttribute("RelationType", r.getAttribute("RelationType") + "_OP");
                                    relations_bi.add(r);
                                    relations_bi.add(opdir);
                                    break;
                                }
                                //if (r.getTarget() == firstArg && r.getSource() == secondArg){
                                if (r.getTarget().getStartSpan() == firstArg.getStartSpan() && r.getTarget().getEndSpan() == firstArg.getEndSpan()
                                        && r.getSource().getStartSpan() == secondArg.getStartSpan() && r.getSource().getEndSpan() == secondArg.getEndSpan()){
                                    relations_mono.add(r);
                                    Relation opdirNeg = new Relation("NOT_RELATED", firstArg, secondArg, 1.0f);
                                    opdirNeg.addAttribute("RelationType", "NOT_RELATED");
                                    found_as_target = true;
                                    String opTypeName = getOppoName(r.getAttribute("RelationSubtype"));
                                    Relation opdir = new Relation(opTypeName, firstArg, secondArg, 1.0f);
                                    opdir.addAttribute("RelationSubtype", opTypeName);
                                    opdir.addAttribute("RelationType", r.getAttribute("RelationType") + "_OP");
                                    relations_bi.add(r);
                                    relations_bi.add(opdir);
                                    break;
                                }
                            }
                            if (!found_as_source && !found_as_target){
                                Relation newRelation_1 = new Relation("NOT_RELATED", firstArg, secondArg, 1.0f);
                                newRelation_1.addAttribute("RelationSubtype", "NOT_RELATED");
                                newRelation_1.addAttribute("RelationType", "NOT_RELATED");
                                relations_mono.add(newRelation_1);
                                Relation newRelation_2 = new Relation("NOT_RELATED", secondArg, firstArg, 1.0f);
                                newRelation_2.addAttribute("RelationSubtype", "NOT_RELATED");
                                newRelation_2.addAttribute("RelationType", "NOT_RELATED");
                                relations_bi.add(newRelation_1);
                                relations_bi.add(newRelation_2);
                            }
                        }
                    }
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void close(){}
    public Object next(){
        if (readType == "relations_mono"){
            if (relationIdx == relations_mono.size()){
                return null;
            }
            else{
                relationIdx ++;
                return relations_mono.get(relationIdx- 1);
            }
        }
        else if (readType == "relations_bi"){
            if (relationIdx == relations_bi.size()){
                return null;
            }
            else{
                relationIdx ++;
                return relations_bi.get(relationIdx- 1);
            }
        }
        else{
            return null;
        }
    }

    public void reset(){
        relationIdx = 0;
    }
}
