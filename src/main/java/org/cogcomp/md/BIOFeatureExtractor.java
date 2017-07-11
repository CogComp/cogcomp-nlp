package org.cogcomp.md;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.edison.features.helpers.WordEmbeddings;
import edu.illinois.cs.cogcomp.ner.StringStatisticsUtils.MyString;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Xuanyu on 7/9/2017.
 */
public class BIOFeatureExtractor {
    public static List<String> getGazetteerFeatures(Constituent c){
        List<String> ret_features = new ArrayList<>();
        String label = "";
        TextAnnotation ta = c.getTextAnnotation();
        View gazetteerView = ta.getView(ViewNames.GAZETTEER);
        Constituent gazetteerConstituent = gazetteerView.getConstituentsCovering(c).get(0);
        if (gazetteerConstituent != null){
            label = gazetteerConstituent.getLabel();
        }
        return ret_features;
    }

    public static List<String> getWordEmbeddingsFeatures(Constituent c){
        List<String> ret_features = new ArrayList<>();
        try {
            WordEmbeddings.initWithDefaults();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        double[] we_0 = WordEmbeddings.getEmbedding(c);
        System.out.println(we_0);
        return ret_features;
    }

    public static String isSentenceStart (Constituent c){
        Sentence sentence = c.getTextAnnotation().getSentenceFromToken(c.getStartSpan());
        int sentenceStart = sentence.getStartSpan();
        if (c.getStartSpan() == sentenceStart){
            return "1";
        }
        return "0";
    }

    public static List<Pair<Integer, String>> getWordFormFeatures (Constituent c){
        List<Pair<Integer, String>> ret_features = new ArrayList<>();
        TextAnnotation ta = c.getTextAnnotation();
        Sentence sentence = ta.getSentenceFromToken(c.getStartSpan());
        int sentenceStart = sentence.getStartSpan();
        int sentenceEnd = sentence.getEndSpan();
        if (c.getStartSpan() > sentenceStart){
            ret_features.add(new Pair<>(-1, ta.getToken(c.getStartSpan() - 1)));
            ret_features.add(new Pair<>(-1, MyString.normalizeDigitsForFeatureExtraction(ta.getToken(c.getStartSpan() - 1))));
        }
        ret_features.add(new Pair<>(0, ta.getToken(c.getStartSpan())));
        ret_features.add(new Pair<>(0, MyString.normalizeDigitsForFeatureExtraction(ta.getToken(c.getStartSpan()))));
        if (c.getEndSpan() < sentenceEnd){
            ret_features.add(new Pair<>(1, ta.getToken(c.getStartSpan() + 1)));
            ret_features.add(new Pair<>(1, MyString.normalizeDigitsForFeatureExtraction(ta.getToken(c.getStartSpan() + 1))));
        }
        if (c.getEndSpan() < sentenceEnd - 1){
            ret_features.add(new Pair<>(2, ta.getToken(c.getStartSpan() + 2)));
            ret_features.add(new Pair<>(2, MyString.normalizeDigitsForFeatureExtraction(ta.getToken(c.getStartSpan() + 2))));
        }
        return ret_features;
    }
}