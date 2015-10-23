package edu.illinois.cs.cogcomp.comma.annotators;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.comma.datastructures.Comma;
import edu.illinois.cs.cogcomp.comma.datastructures.CommaProperties;
import edu.illinois.cs.cogcomp.comma.datastructures.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.CoNLLColumnFormatReader;

/**
 * An interface for providing a comma {@link PredicateArgumentView}
 */
public class CommaLabeler implements Annotator{
    public static final String VIEW_NAME = "SRL_COMMA";
    private static String[] requiredViews;
    private LocalCommaClassifier classifier;

    
    public CommaLabeler () {
        classifier = new LocalCommaClassifier();
        CommaProperties properties = CommaProperties.getInstance();
        String CONSTITUENT_PARSER;
        boolean GOLD = properties.useGold();
        if(GOLD)
        	CONSTITUENT_PARSER = ViewNames.PARSE_GOLD;
        else
        	CONSTITUENT_PARSER = properties.getConstituentParser();
        requiredViews = new String[]{CONSTITUENT_PARSER, ViewNames.POS, ViewNames.SHALLOW_PARSE};
    }

    @Override
	public View getView(TextAnnotation ta) throws AnnotatorException {
        // Check that we have the required views
        for (String requiredView : requiredViews) {
            if (!ta.hasView(requiredView))
                throw new AnnotatorException("Missing required view " + requiredView);
        }
        // Create the Comma structure
        Sentence sentence = new Sentence(ta, ta);
        
        PredicateArgumentView srlView = new PredicateArgumentView(VIEW_NAME, "illinois-comma", ta, 1.0d);
        for (Comma comma : sentence.getCommas()) {
            String label = classifier.discreteValue(comma);
            int position = comma.getPosition();
            Constituent predicate = new Constituent(label, VIEW_NAME, ta, position, position+1);
            predicate.addAttribute(CoNLLColumnFormatReader.SenseIdentifer, label);
            srlView.addConstituent(predicate);
            Constituent leftArg = comma.getPhraseToLeftOfComma(1);
            if (leftArg != null) {
                Constituent leftArgConst = new Constituent(leftArg.getLabel(), VIEW_NAME, ta,
                        leftArg.getStartSpan(), leftArg.getEndSpan());
                srlView.addConstituent(leftArgConst);
                srlView.addRelation(new Relation("LeftOf" + label, predicate, leftArgConst, 1.0d));
            }
            Constituent rightArg = comma.getPhraseToRightOfComma(1);
            if (rightArg != null) {
                Constituent rightArgConst = new Constituent(rightArg.getLabel(), VIEW_NAME, ta,
                        rightArg.getStartSpan(), rightArg.getEndSpan());
                srlView.addConstituent(rightArgConst);
                srlView.addRelation(new Relation("RightOf" + label, predicate, rightArgConst, 1.0d));
            }
        }
        return srlView;
    }

	@Override
	public String getViewName() {
		return VIEW_NAME;
	}

    public String[] getRequiredViews() {
        return requiredViews;
    }
}
