/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.pipeline.handlers.clausie;

import de.mpii.clausie.ClausIE;
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
        super("CLAUSIE", new String[]{ViewNames.SENTENCE}, true);
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
        View vu = new View(viewName, "ClausIEAnnotator", ta, 1.0);
        assert sentences.size() == ta.getNumberOfSentences();
        for(Constituent sent : sentences) {
            String[] clausieResults = ClausieSplitter.split(sent.getSurfaceForm());
            Constituent sentenceCons = new Constituent("sent-" + sent.getSentenceId(), viewName, ta, sent.getStartSpan(), sent.getEndSpan());
            int propId = 0;
            for(String clausieSent : clausieResults) {
                sentenceCons.addAttribute("clauseIe:" + propId, clausieSent);
                propId++;
            }
            vu.addConstituent(sentenceCons);
        }
        ta.addView(viewName, vu);
    }
}
