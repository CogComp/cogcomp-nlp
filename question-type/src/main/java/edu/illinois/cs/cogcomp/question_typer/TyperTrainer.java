package edu.illinois.cs.cogcomp.question_typer;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by daniel on 1/18/18.
 */
public class TyperTrainer {
    public static void main(String[] args) throws IOException {
        QuestionTypeReader reader = new QuestionTypeReader("question-type/data/TREC_10.label.txt");
        Object o = null;
        while((o = reader.next()) != null) {
            TextAnnotation s = (TextAnnotation)o;
            System.out.println(s);
            System.out.println(s.getAvailableViews());
            System.out.println(QuestionTyperFeatureExtractorsUtils.getFineLabel(s));
            System.out.println(QuestionTyperFeatureExtractorsUtils.getCoarseLabel(s));
        }
        QuestionTyperFeatureExtractorsUtils.readLists();
        for(Object key : QuestionTyperFeatureExtractorsUtils.list.keySet()) {
            System.out.println("----> " + key);
            System.out.println(QuestionTyperFeatureExtractorsUtils.list.get(key));
        }
    }
}
