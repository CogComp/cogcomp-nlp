package edu.illinois.cs.cogcomp.comma;

import edu.illinois.cs.cogcomp.comma.lbj.CommaClassifier;
import edu.illinois.cs.cogcomp.edison.sentences.*;
import edu.illinois.cs.cogcomp.thrift.base.AnnotationFailedException;

/**
 * An interface for providing a comma {@link PredicateArgumentView}
 */
public class CommaLabeler {
    public static final String VIEW_NAME = "SRL_COMMA";
    private static String[] requiredViews = new String[]{ViewNames.PARSE_STANFORD, ViewNames.POS};
    private CommaClassifier classifier;

    public CommaLabeler () {
        classifier = new CommaClassifier();
    }

    public PredicateArgumentView getCommaSRL(TextAnnotation ta) throws AnnotationFailedException {
        // Check that we have the required views
        for (String requiredView : requiredViews) {
            if (!ta.hasView(requiredView))
                throw new AnnotationFailedException("Missing required view " + requiredView);
        }
        // Create the Comma structure
        PredicateArgumentView srlView = new PredicateArgumentView(VIEW_NAME, "illinois-comma", ta, 1.0d);
        String text = ta.getText();
        for (Constituent comma : ta.getView(ViewNames.POS).getConstituents()) {
            if (!comma.getLabel().equals(",")) continue;
            Comma commaStruct = new Comma(comma.getStartSpan(), text, ta);
            String label = classifier.discreteValue(commaStruct);
            Constituent predicate = new Constituent(label, VIEW_NAME, ta, comma.getStartSpan(), comma.getEndSpan());
            srlView.addConstituent(predicate);
            Constituent leftArg = commaStruct.getPhraseToLeftOfComma(1);
            if (leftArg != null) {
                Constituent leftArgConst = new Constituent(leftArg.getLabel(), VIEW_NAME, ta,
                        leftArg.getStartSpan(), leftArg.getEndSpan());
                srlView.addConstituent(leftArgConst);
                srlView.addRelation(new Relation("LeftOf" + label, predicate, leftArgConst, 1.0d));
            }
            Constituent rightArg = commaStruct.getPhraseToRightOfComma(1);
            if (rightArg != null) {
                Constituent rightArgConst = new Constituent(rightArg.getLabel(), VIEW_NAME, ta,
                        rightArg.getStartSpan(), rightArg.getEndSpan());
                srlView.addConstituent(rightArgConst);
                srlView.addRelation(new Relation("RightOf" + label, predicate, rightArgConst, 1.0d));
            }
        }
        return srlView;
    }

    public String[] getRequiredViews() {
        return requiredViews;
    }
}
