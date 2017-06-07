package edu.illinois.cs.cogcomp.pipeline.handlers;

import de.mpii.clausie.ClausIE;
import de.mpii.clausie.Clause;
import de.mpii.clausie.Proposition;
import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

public class ClausIEAnnotator extends Annotator {

    ClausIE clausIE = new ClausIE();

    public ClausIEAnnotator(String[] requiredViews) {
        super("CLAUS_IE", new String[]{});
    }

    @Override
    public void initialize(ResourceManager rm) {
        this.clausIE = new ClausIE();
        clausIE.initParser();
    }

    @Override
    protected void addView(TextAnnotation ta) throws AnnotatorException {
/*        for(int i = 0; i < ta.getNumberOfSentences(); i++) {
            clausIE.parse(ta.sen(i));
            System.out.println(clauseIE.getSemanticGraph().toFormattedString().replaceAll("\n", "\n#                ").trim());
            clauseIE.detectClauses();
            System.out.println(clauseIE.getClauses().size());
            for(Clause c : clauseIE.getClauses()) {
                System.out.println(c.toString());
            }
            clauseIE.generatePropositions();
            System.out.println("clausIE.getPropositions(): ");
            System.out.println(clauseIE.getPropositions());
            View vu = new View("", "", ta, 1.0);
            Constituent sentenceCons = new Constituent("", viewName, ta, 0, ta.getTokens().length);
            sentenceCons.addAttribute();
            for(Proposition p : clauseIE.getPropositions()) {
            }
            System.out.println(clauseIE.getOptions());
            System.out.println(clauseIE.getDepTree());
        }*/
    }

    public static void main(String[] args) {
//        ClausIE clauseIE = new ClausIE();
//        clauseIE.initParser();
//        clauseIE.parse("The boy gave the frog to the girl. The boy's gift was to the girl. The girl was given a frog. A squirrel is storing a lot of nuts to prepare for a seasonal change in the environment. The construction of the John Smith library finished on time.  Amanda found herself in the Winnebago with her ex-boyfriend, an herbalist and a pet detective. Rome is in Lazio province and Naples in Campania. The region at the end of 2014 had a population of around 5,869,000 people, making it the third-most-populous region of Italy. ");
//        System.out.println(clauseIE.getSemanticGraph().toFormattedString().replaceAll("\n", "\n#                ").trim());
//        clauseIE.detectClauses();
//        System.out.println(clauseIE.getClauses().size());
//        for(Clause c : clauseIE.getClauses()) {
//            System.out.println(c.toString());
//        }
//        clauseIE.generatePropositions();
//        System.out.println("clausIE.getPropositions(): ");
//        System.out.println(clauseIE.getPropositions());
//        for(Proposition p : clauseIE.getPropositions()) {
//
//        }
//        System.out.println(clauseIE.getOptions());
//        System.out.println(clauseIE.getDepTree());

        TextAnnotation ta = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(false, 3);
        int begin = 0;
        int end = 0;
        int currentSentId = 0;
        int tokenId = 0;
        for(Constituent c : ta.getView(ViewNames.TOKENS).getConstituents()) {
            if( c.getSentenceId() > currentSentId ) {
                currentSentId = c.getSentenceId();
                end = tokenId - 1; // the previous token was the end of the previous sentence
                Constituent sentCons = new Constituent("")

                begin == tokenId;
            }
            tokenId++;
        }
        System.out.println(ta.getSentence(0).text);
        System.out.println(ta.getSentence(1).text);
        System.out.println(ta.getSentence(2).text);
    }
}
