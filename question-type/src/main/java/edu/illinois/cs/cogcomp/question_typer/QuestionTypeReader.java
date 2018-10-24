/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.question_typer;

import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;

import java.io.*;

import static edu.illinois.cs.cogcomp.question_typer.QuestionTyperFeatureExtractorsUtils.*;

/**
 * Created by daniel on 1/18/18.
 */
public class QuestionTypeReader implements Parser {

    BufferedReader bufferedReader = null;
    String line;

    // annotators
    public static TextAnnotationBuilder taBldr = new TokenizerTextAnnotationBuilder(new StatefulTokenizer(false, false));

    public QuestionTypeReader(String file) {
        // format "ISO-8859-1"
        try {
            bufferedReader = new BufferedReader(new FileReader(new File(file)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object next() {
        try {
            line = bufferedReader.readLine(); // read a new line
            if(line == null) {
                return null;
            }
            else {
                String[] split = line.split(" ");
                String[] splitLabel = split[0].split(":");
                assert splitLabel.length == 2 : "The label length is not 2, but it is instead" + splitLabel.length;
                String question = line.substring(split[0].length()).trim();
                TextAnnotation ta = taBldr.createTextAnnotation(question);
                SpanLabelView view = new SpanLabelView(QuestionTyperConfigurator.questionTypeViewName,
                        QuestionTyperConfigurator.questionTypeViewName, ta, 1.0);
                Constituent c = new Constituent(split[0], 1.0, QuestionTyperConfigurator.questionTypeViewName,
                        ta, 0, ta.getTokens().length);
                c.addAttribute(QuestionTyperConfigurator.goldCoarseLabelAttributeName, splitLabel[0]);
                c.addAttribute(QuestionTyperConfigurator.goldFineLabelAttributeName, split[0]);
                view.addConstituent(c);
                ta.addView(QuestionTyperConfigurator.questionTypeViewName, view);
                try {
                    QuestionTyperFeatureExtractorsUtils.serverClient.addView(ta);
                }
                catch (Exception e){
                    // if we're not able to annotate this instance, just ignore it
                    System.out.println(ta);
                    e.printStackTrace();
                    return null;
                }
                return ta;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void reset() {

    }

    @Override
    public void close() {
        try {
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
