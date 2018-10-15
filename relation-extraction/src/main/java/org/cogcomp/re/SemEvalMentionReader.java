/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package org.cogcomp.re;

import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.chunker.main.ChunkerAnnotator;
import edu.illinois.cs.cogcomp.chunker.main.ChunkerConfigurator;
import edu.illinois.cs.cogcomp.core.constants.Language;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.resources.ResourceConfigurator;
import edu.illinois.cs.cogcomp.edison.utilities.WordNetManager;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.FlatGazetteers;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.GazetteersFactory;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.pipeline.common.Stanford331Configurator;
import edu.illinois.cs.cogcomp.pipeline.handlers.StanfordDepHandler;
import edu.illinois.cs.cogcomp.pos.POSAnnotator;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.ParserAnnotator;
import org.cogcomp.Datastore;
import org.cogcomp.md.BIOFeatureExtractor;
import org.cogcomp.md.MentionAnnotator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

public class SemEvalMentionReader implements Parser {

    private List<Relation> relations;
    private int counter = 0;
    private POSAnnotator _posAnnotator;
    private FlatGazetteers _gazetteers;
    private WordNetManager _wordnet;
    private ChunkerAnnotator __chunker;
    private StanfordDepHandler __stanfordDep;
    private MentionAnnotator __mentionAnnotator;

    public void initExternalTools(){
        try {
            _posAnnotator = new POSAnnotator();
            Datastore ds = new Datastore(new ResourceConfigurator().getDefaultConfig());
            File gazetteersResource = ds.getDirectory("org.cogcomp.gazetteers", "gazetteers", 1.3, false);
            _gazetteers = (FlatGazetteers)GazetteersFactory.get(5, gazetteersResource.getPath() + File.separator + 
                "gazetteers", true, Language.English);
            WordNetManager.loadConfigAsClasspathResource(true);
            _wordnet = WordNetManager.getInstance();
            __chunker  = new ChunkerAnnotator(true);
            __chunker.initialize(new ChunkerConfigurator().getDefaultConfig());

            Properties stanfordProps = new Properties();
            stanfordProps.put("annotators", "pos, parse");
            stanfordProps.put("parse.originalDependencies", true);
            stanfordProps.put("parse.maxlen", Stanford331Configurator.STFRD_MAX_SENTENCE_LENGTH);
            stanfordProps.put("parse.maxtime", Stanford331Configurator.STFRD_TIME_PER_SENTENCE);
            POSTaggerAnnotator posAnnotator = new POSTaggerAnnotator("pos", stanfordProps);
            ParserAnnotator parseAnnotator = new ParserAnnotator("parse", stanfordProps);
            __stanfordDep = new StanfordDepHandler(posAnnotator, parseAnnotator);
            __mentionAnnotator = new MentionAnnotator("ACE_TYPE");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public List<TextAnnotation> readTrainFile(String fileName, String mode){
        List<String> sentences = new ArrayList<>();
        List<String> types = new ArrayList<>();
        List<TextAnnotation> ret = new ArrayList<>();
        int counter = 0;
        if (mode.equals("TRAIN")) {
            try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (counter % 4 == 0) {
                        String curSentence = line.split("\t")[1];
                        if (curSentence.charAt(0) == '"'){
                            curSentence = curSentence.substring(1);
                        }
                        if (curSentence.charAt(curSentence.length() - 1) == '"'){
                            curSentence = curSentence.substring(0, curSentence.length() - 1);
                        }
                        sentences.add(curSentence);
                    }
                    if (counter % 4 == 1) {
                        types.add(line);
                    }
                    counter++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (mode.equals("TEST")){
            try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String curSentence = line.split("\t")[1];
                    if (curSentence.charAt(0) == '"'){
                        curSentence = curSentence.substring(1);
                    }
                    if (curSentence.charAt(curSentence.length() - 1) == '"'){
                        curSentence = curSentence.substring(0, curSentence.length() - 1);
                    }
                    sentences.add(curSentence);
                    types.add("UNKNOWN");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        StatefulTokenizer statefulTokenizer = new StatefulTokenizer();

        for (int i = 0; i < sentences.size(); i++){
            List<String[]> tokens = new ArrayList<>();
            String sentence = sentences.get(i);
            String type = types.get(i);
            Pair<String[], IntPair[]> tokenizedSentence = statefulTokenizer.tokenizeSentence(sentence);
            List<String> curTokens = new LinkedList<>(Arrays.asList(tokenizedSentence.getFirst()));
            int firstArgStart = 0;
            int firstArgEnd = 0;
            int secondArgStart = 0;
            int secondArgEnd = 0;
            for (int j = 0; j < curTokens.size(); j++){
                if (curTokens.get(j).equals("<") && curTokens.get(j + 1).equals("e1") && curTokens.get(j + 2).equals(">")){
                    firstArgStart = j;
                    for (int k = j; k < j + 3; k++) {
                        curTokens.remove(j);
                    }
                }
                if (curTokens.get(j).equals("<") && curTokens.get(j + 1).equals("/") && curTokens.get(j + 2).equals("e1") && curTokens.get(j + 3).equals(">")){
                    firstArgEnd = j;
                    for (int k = j; k < j + 4; k++) {
                        curTokens.remove(j);
                    }
                }
                if (curTokens.get(j).equals("<") && curTokens.get(j + 1).equals("e2") && curTokens.get(j + 2).equals(">")){
                    secondArgStart = j;
                    for (int k = j; k < j + 3; k++) {
                        curTokens.remove(j);
                    }
                }
                if (curTokens.get(j).equals("<") && curTokens.get(j + 1).equals("/") && curTokens.get(j + 2).equals("e2") && curTokens.get(j + 3).equals(">")){
                    secondArgEnd = j;
                    for (int k = j; k < j + 4; k++) {
                        curTokens.remove(j);
                    }
                }
            }
            tokens.add(curTokens.toArray(new String[curTokens.size()]));
            TextAnnotation ta = BasicTextAnnotationBuilder.createTextAnnotationFromTokens(tokens);
            try {
                ta.addView(_posAnnotator);
                __chunker.addView(ta);
                __stanfordDep.addView(ta);
                __mentionAnnotator.addView(ta);
                View annotatedTokenView = new SpanLabelView("RE_ANNOTATED", ta);
                for (Constituent co : ta.getView(ViewNames.TOKENS).getConstituents()){
                    Constituent c = co.cloneForNewView("RE_ANNOTATED");
                    for (String s : co.getAttributeKeys()){
                        c.addAttribute(s, co.getAttribute(s));
                    }
                    c.addAttribute("WORDNETTAG", BIOFeatureExtractor.getWordNetTags(_wordnet, c));
                    c.addAttribute("WORDNETHYM", BIOFeatureExtractor.getWordNetHyms(_wordnet, c));
                    annotatedTokenView.addConstituent(c);
                }
                ta.addView("RE_ANNOTATED", annotatedTokenView);
            }
            catch (Exception e){
                e.printStackTrace();
            }
            SpanLabelView mentionView = new SpanLabelView("MENTIONS", "MENTIONS", ta, 1.0f);
            Constituent firstArg = new Constituent("MENTION", 1.0f, "MENTIONS", ta, firstArgStart, firstArgEnd);
            Constituent secondArg = new Constituent("MENTION", 1.0f, "MENTIONS", ta, secondArgStart, secondArgEnd);
            firstArg.addAttribute("GAZ", _gazetteers.annotatePhrase(firstArg));
            secondArg.addAttribute("GAZ", _gazetteers.annotatePhrase(secondArg));

            View annotatedMentionView = ta.getView(ViewNames.MENTION);
            List<Constituent> firstMentions = annotatedMentionView.getConstituentsCoveringToken(firstArg.getStartSpan());
            List<Constituent> secondMentions = annotatedMentionView.getConstituentsCoveringToken(secondArg.getStartSpan());
            if (firstMentions.size() == 0){
                firstArg.addAttribute("EntityType", "UNKNOWN");
            }
            else {
                firstArg.addAttribute("EntityType", firstMentions.get(0).getAttribute("EntityType"));
            }
            if (secondMentions.size() == 0){
                secondArg.addAttribute("EntityType", "UNKNOWN");
            }
            else {
                secondArg.addAttribute("EntityType", secondMentions.get(0).getAttribute("EntityType"));
            }

            mentionView.addConstituent(firstArg);
            mentionView.addConstituent(secondArg);
            if (type.contains("e1,e2")){
                Relation relation = new Relation(type.split("[(]")[0], firstArg, secondArg, 1.0f);
                relation.addAttribute("RelationSubtype", relation.getRelationName());
                mentionView.addRelation(relation);
            }
            else if (type.contains("e2,e1")){
                Relation relation = new Relation(type.split("[(]")[0], secondArg, firstArg, 1.0f);
                relation.addAttribute("RelationSubtype", relation.getRelationName());
                mentionView.addRelation(relation);
            }
            else{
                Relation relationLeft = new Relation(type, secondArg, firstArg, 1.0f);
                Relation relationRight = new Relation(type, firstArg, secondArg, 1.0f);
                relationLeft.addAttribute("RelationSubtype", relationLeft.getRelationName());
                relationRight.addAttribute("RelationSubtype", relationRight.getRelationName());
                mentionView.addRelation(relationLeft);
                mentionView.addRelation(relationRight);
            }

            ta.addView("MENTIONS", mentionView);
            ret.add(ta);
        }
        return ret;
    }

    public SemEvalMentionReader(String file_path, String mode){
        initExternalTools();
        relations = new ArrayList<>();
        List<TextAnnotation> tas = readTrainFile(file_path, mode);
        System.out.println(tas.size());
        for (TextAnnotation ta : tas){
            for (Relation r : ta.getView("MENTIONS").getRelations()) {
                relations.add(r);
            }
        }
    }
    public void close(){}
    public Object next(){
        if (counter == relations.size()) {
            return null;
        } else {
            counter ++;
            return relations.get(counter - 1);
        }
    }

    public void reset(){
        counter = 0;
    }
}