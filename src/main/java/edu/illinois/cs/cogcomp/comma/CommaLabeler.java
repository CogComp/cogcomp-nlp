package edu.illinois.cs.cogcomp.comma;

import java.util.ArrayList;
import java.util.List;

import edu.illinois.cs.cogcomp.comma.lbj.LocalCommaClassifier;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.PredicateArgumentView;
import edu.illinois.cs.cogcomp.edison.sentences.Relation;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;
import edu.illinois.cs.cogcomp.thrift.base.AnnotationFailedException;

/**
 * An interface for providing a comma {@link PredicateArgumentView}
 */
public class CommaLabeler {
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
        requiredViews = new String[]{CONSTITUENT_PARSER, ViewNames.POS};
    }

    public PredicateArgumentView getCommaSRL(TextAnnotation ta) throws AnnotationFailedException {
        // Check that we have the required views
        for (String requiredView : requiredViews) {
            if (!ta.hasView(requiredView))
                throw new AnnotationFailedException("Missing required view " + requiredView);
        }
        // Create the Comma structure
        List<Comma> commas = getCommas(ta);
        PredicateArgumentView srlView = new PredicateArgumentView(VIEW_NAME, "illinois-comma", ta, 1.0d);
        for (Comma comma : commas) {
            String label = classifier.discreteValue(comma);
            int position = comma.getPosition();
            Constituent predicate = new Constituent(label, VIEW_NAME, ta, position, position+1);
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
    
    /**
     * 
     * @param ta the input TextAnnotation for whose sentence we want to create Commas
     * @return a list of Commas. Each comma in the list corresponds to a comma in the sentence represented by ta
     */
    public static List<Comma> getCommas(TextAnnotation ta){
    	List<Comma> commas = new ArrayList<Comma>();
    	Sentence sentenceStruct = new Sentence();
    	String text = ta.getText();
    	for (Constituent comma : ta.getView(ViewNames.POS).getConstituents()) {
            if (!comma.getLabel().equals(",")) continue;
            Comma commaStruct = new Comma(comma.getStartSpan(), text, ta, sentenceStruct);
            sentenceStruct.addComma(commaStruct);
            commas.add(commaStruct);
    	}
    	return commas;
    }

    public String[] getRequiredViews() {
        return requiredViews;
    }
}
