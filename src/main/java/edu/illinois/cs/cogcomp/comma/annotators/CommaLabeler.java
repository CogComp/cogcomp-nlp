package edu.illinois.cs.cogcomp.comma.annotators;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.comma.datastructures.Comma;
import edu.illinois.cs.cogcomp.comma.datastructures.CommaProperties;
import edu.illinois.cs.cogcomp.comma.datastructures.Sentence;
import edu.illinois.cs.cogcomp.comma.lbj.LocalCommaClassifier;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.PredicateArgumentView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.CoNLLColumnFormatReader;
import edu.illinois.cs.cogcomp.nlp.tokenizer.IllinoisTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.CcgTextAnnotationBuilder;

/**
 * An interface for providing a comma {@link PredicateArgumentView}
 */
public class CommaLabeler extends Annotator{
    private LocalCommaClassifier classifier;
    
    public static final String viewName = "SRL_COMMA";
    
    public CommaLabeler () {
    	super(viewName, new String[]{CommaProperties.getInstance().useGold()?ViewNames.PARSE_GOLD :  CommaProperties.getInstance().getConstituentParser() , ViewNames.POS, ViewNames.SHALLOW_PARSE});
    	classifier = new LocalCommaClassifier();
    }

    @Override
	public void addView(TextAnnotation ta) throws AnnotatorException {
//    	if(ta.hasView(viewName))
//    		return;
    	
        // Check that we have the required views
        for (String requiredView : requiredViews) {
            if (!ta.hasView(requiredView))
                throw new AnnotatorException("Missing required view " + requiredView);
        }
        // Create the Comma structure
        Sentence sentence = new Sentence(ta, ta);
        
        PredicateArgumentView srlView = new PredicateArgumentView(viewName, "illinois-comma", ta, 1.0d);
        for (Comma comma : sentence.getCommas()) {
            String label = classifier.discreteValue(comma);
            int position = comma.getPosition();
            Constituent predicate = new Constituent(label, viewName, ta, position, position+1);
            predicate.addAttribute(CoNLLColumnFormatReader.SenseIdentifer, label);
            srlView.addConstituent(predicate);
            Constituent leftArg = comma.getPhraseToLeftOfComma(1);
            if (leftArg != null) {
                Constituent leftArgConst = new Constituent(leftArg.getLabel(), viewName, ta,
                        leftArg.getStartSpan(), leftArg.getEndSpan());
                srlView.addConstituent(leftArgConst);
                srlView.addRelation(new Relation("LeftOf" + label, predicate, leftArgConst, 1.0d));
            }
            Constituent rightArg = comma.getPhraseToRightOfComma(1);
            if (rightArg != null) {
                Constituent rightArgConst = new Constituent(rightArg.getLabel(), viewName, ta,
                        rightArg.getStartSpan(), rightArg.getEndSpan());
                srlView.addConstituent(rightArgConst);
                srlView.addRelation(new Relation("RightOf" + label, predicate, rightArgConst, 1.0d));
            }
        }
        ta.addView(viewName, srlView);
    }
    
    public void annotate(String inFileName, String outFileName) throws Exception{
    	PreProcessor preProcessor = new PreProcessor();
        BufferedReader reader = new BufferedReader(new FileReader(inFileName));
        PrintWriter writer = new PrintWriter(outFileName, "UTF-8");
        String line = null;
        while ((line = reader.readLine()) != null) {
            if(line.length() > 0) {
            	TextAnnotation ta = preProcessor.preProcess(line);
            	addView(ta);
            	writer.format("%s\n\n", commaViewToString(ta));
            }
        }
        reader.close();
        writer.close();
	}
    
    public static String commaViewToString(TextAnnotation ta){
    	Sentence sentence = new Sentence(ta);
    	return sentence.getAnnotatedText();
    }

    public static void main(String args[]) throws Exception{
    	if(args.length != 2)
        {
            System.out.println("Proper Usage is: java CommaLabeler infile outfile");
            System.exit(0);
        }
    	System.out.format("infile:%s\noutfile%s\n", args[0],args[1]);
    	CommaLabeler annotator = new CommaLabeler();
    	annotator.annotate(args[0], args[1]);
    }

}
