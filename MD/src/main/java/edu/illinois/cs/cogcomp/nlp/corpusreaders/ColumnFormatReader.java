package edu.illinois.cs.cogcomp.nlp.corpusreaders;

import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.lbjava.nlp.ColumnFormat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Xuanyu on 7/19/2017.
 */
public class ColumnFormatReader extends AnnotationReader<TextAnnotation>{
    String _path;
    List<String> _filePaths;
    List<TextAnnotation> _tas;
    private int _tas_idx;
    public ColumnFormatReader(String path){
        super(CorpusReaderConfigurator.buildResourceManager(path));
        _path = path;
        fillPaths();
        readTextAnnotations();
    }
    public void initializeReader(){
        _path = "INVALID";
        _filePaths = new ArrayList<>();
        _tas = new ArrayList<>();
        _tas_idx = 0;
    }
    public void fillPaths(){
        File directory = new File(_path);
        File[] subFiles = directory.listFiles();
        for (File f : subFiles){
            _filePaths.add(f.getAbsolutePath());
        }
    }
    public void readTextAnnotations(){
        for (String s : _filePaths){
            _tas.add(readSingleFile(s));
        }
    }
    public TextAnnotation readSingleFile(String file){
        System.out.println(file);
        ColumnFormat columnFormat = new ColumnFormat(file);
        List<String[]> tokens = new ArrayList<>();
        List<Pair<Integer, Integer>> mentions = new ArrayList<>();
        List<String> mentionTypes = new ArrayList<>();
        List<String> curSentence = new ArrayList<>();
        List<Integer> curMention = new ArrayList<>();
        int tokenIdx = 0;
        columnFormat.reset();
        boolean prevNull = false;
        for (Object lineObject  = columnFormat.next(); lineObject != null; lineObject = columnFormat.next()){
            if (lineObject == null || ((String[])lineObject).length == 0){
                if (prevNull){
                    break;
                }
                String[] curSentenceArr = new String[curSentence.size()];
                curSentenceArr = curSentence.toArray(curSentenceArr);
                tokens.add(curSentenceArr);
                curSentence = new ArrayList<>();
                prevNull = true;
                continue;
            }
            prevNull = false;
            String[] line = (String[])lineObject;
            String word = line[5];

            curSentence.add(word);
            String mentionType = line[0];
            if (mentionType.startsWith("B-")){
                if (curMention.size() > 0) {
                    mentions.add(new Pair<>(curMention.get(0), curMention.get(curMention.size() - 1) + 1));
                    curMention = new ArrayList<>();
                }
                curMention.add(tokenIdx);
                String[] group = mentionType.split("-");
                mentionTypes.add(group[1]);
            }
            if (mentionType.startsWith("I-")){
                curMention.add(tokenIdx);
            }
            if (mentionType.equals("O")){
                if (curMention.size() > 0) {
                    mentions.add(new Pair<>(curMention.get(0), curMention.get(curMention.size() - 1) + 1));
                    curMention = new ArrayList<>();
                }
            }
            tokenIdx ++;
        }
        columnFormat.reset();
        if (curMention.size() > 0){
            mentions.add(new Pair<>(curMention.get(0), curMention.get(curMention.size() - 1)));
        }
        if (curSentence.size() > 0){
            String[] curSentenceArr = new String[curSentence.size()];
            curSentenceArr = curSentence.toArray(curSentenceArr);
            tokens.add(curSentenceArr);
        }
        TextAnnotation ta = BasicTextAnnotationBuilder.createTextAnnotationFromTokens(tokens);
        SpanLabelView mentionView = new SpanLabelView("MENTIONS", this.getClass().getCanonicalName(), ta, 1.0f);
        if (mentionTypes.size() != mentions.size()){
            System.out.println("ERROR");
        }
        for (int i = 0; i < mentions.size();i ++){
            Pair<Integer, Integer> curBound = mentions.get(i);
            String curType = mentionTypes.get(i);
            Constituent constituent = new Constituent("MENTION", 1.0f, "MENTIONS", ta, curBound.getFirst(), curBound.getSecond());
            constituent.addAttribute("EntityType", curType);
            constituent.addAttribute(ACEReader.EntityHeadStartCharOffset, "HEAD");
            constituent.addAttribute(ACEReader.EntityHeadEndCharOffset, "HEAD");
            if (_path.contains("nom")){
                constituent.addAttribute("EntityMentionType", "NOM");
            }
            else{
                constituent.addAttribute("EntityMentionType", "NAM");
            }
            mentionView.addConstituent(constituent);
        }
        ta.addView("MENTIONS", mentionView);
        return ta;
    }
    public boolean hasNext(){
        return _tas_idx < _tas.size();
    }
    public TextAnnotation next(){
        if (_tas_idx == _tas.size()){
            return null;
        }
        else{
            _tas_idx ++;
            return _tas.get(_tas_idx - 1);
        }
    }
    public String generateReport(){
        return null;
    }
}
