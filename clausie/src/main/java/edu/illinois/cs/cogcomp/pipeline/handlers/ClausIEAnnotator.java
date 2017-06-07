package edu.illinois.cs.cogcomp.pipeline.handlers;

import de.mpii.clausie.ClausIE;
import de.mpii.clausie.Proposition;
import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

import java.util.List;

public class ClausIEAnnotator extends Annotator {

    private ClausIE clausIE = new ClausIE();

    public ClausIEAnnotator() {
        super("CLAUSIE", new String[]{ViewNames.SENTENCE});
    }

    @Override
    public void initialize(ResourceManager rm) {
        this.clausIE = new ClausIE();
        clausIE.initParser();
    }

    @Override
    protected void addView(TextAnnotation ta) throws AnnotatorException {
        assert ta.hasView(ViewNames.SENTENCE): "Sentences view didn't find . . . ";
        List<Constituent> sentences = ta.getView(ViewNames.SENTENCE).getConstituents();
        System.out.println("sentences.size: " + sentences.size());
        View vu = new View(viewName, "ClausIEAnnotator", ta, 1.0);
        assert sentences.size() == ta.getNumberOfSentences();
        for(Constituent sent : sentences) {
            clausIE.parse(sent.getSurfaceForm());
            clausIE.detectClauses();
            clausIE.generatePropositions();
            System.out.println(sent.getSurfaceForm());
            Constituent sentenceCons = new Constituent("", viewName, ta, sent.getStartSpan(), sent.getEndSpan());
            int propId = 0;
            for(Proposition p : clausIE.getPropositions()) {
                String proposition = p.subject();
                proposition += " " + p.relation();
                for(int i = 0; i < p.noArguments(); i++)
                    proposition += " " + p.argument(i);
                sentenceCons.addAttribute("proposition-" + propId, proposition);
                propId++;
            }
            vu.addConstituent(sentenceCons);
        }
        ta.addView(viewName, vu);
    }
}
