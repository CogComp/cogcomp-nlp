package edu.illinois.cs.cogcomp.comma;


import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.comma.lbj.CommaClassifier;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.CoNLLColumnFormatReader;

/**
 * An interface for providing a comma {@link PredicateArgumentView}
 */
public class CommaLabeler implements Annotator {
    public static final String VIEW_NAME = "SRL_COMMA";
    private static String[] requiredViews = new String[]{ViewNames.PARSE_STANFORD, ViewNames.POS};
    private CommaClassifier classifier;

    public CommaLabeler () {
        classifier = new CommaClassifier();
    }

    @Override
    public View getView(TextAnnotation ta) throws AnnotatorException {
        // Check that we have the required views
        for (String requiredView : requiredViews) {
            if (!ta.hasView(requiredView))
                throw new AnnotatorException("Missing required view " + requiredView);
        }
        // Create the Comma structure
        PredicateArgumentView srlView = new PredicateArgumentView(VIEW_NAME, "illinois-comma", ta, 1.0d);
        String[] tokenizedText = getTokenizedText(ta.getView(ViewNames.TOKENS));
        for (Constituent comma : ta.getView(ViewNames.POS).getConstituents()) {
            if (!comma.getLabel().equals(",")) continue;
            Comma commaStruct = new Comma(comma.getStartSpan(), tokenizedText, ta);
            String label = classifier.discreteValue(commaStruct);
            Constituent predicate = new Constituent(label, VIEW_NAME, ta, comma.getStartSpan(), comma.getEndSpan());
            predicate.addAttribute(CoNLLColumnFormatReader.SenseIdentifer, label);
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

    private String[] getTokenizedText(View tokenView) {
        String[] text = new String[tokenView.getNumberOfConstituents()];
        java.util.List<Constituent> constituents = tokenView.getConstituents();
        for (int i = 0; i < constituents.size(); i++) {
            Constituent c = constituents.get(i);
            text[i] = c.getSurfaceString();
        }
        return text;
    }

    @Override
    public String getViewName() {
        return VIEW_NAME;
    }

    public String[] getRequiredViews() {
        return requiredViews;
    }
}
