/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.question_typer;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.resources.ResourceConfigurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.question_typer.lbjava.QuestionCoarseTyper;
import edu.illinois.cs.cogcomp.question_typer.lbjava.QuestionFineTyper;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import org.cogcomp.Datastore;
import org.cogcomp.DatastoreException;

import java.io.File;

/**
 * Created by daniel on 1/24/18.
 */
public class QuestionTypeAnnotator extends Annotator {
    QuestionFineTyper fine = null;
    QuestionCoarseTyper coarse = null;

    String modelsFolder = null;

    public QuestionTypeAnnotator() {
        super(ViewNames.QUESTION_TYPE, new String[]{ViewNames.LEMMA, ViewNames.POS, ViewNames.NER_ONTONOTES, ViewNames.NER_CONLL, ViewNames.SHALLOW_PARSE}, true);
    }

    @Override
    public void initialize(ResourceManager rm) {
        System.out.println("loading . . . ");
        try {
            Datastore dsNoCredentials = new Datastore(new ResourceConfigurator().getDefaultConfig());
            File f = dsNoCredentials.getDirectory("org.cogcomp.question-typer", "question-typer-models", 1.0, false);
            this.modelsFolder = f.getPath() + "/question-typer-models/";
            System.out.println(modelsFolder + "QuestionFineTyper.lc");
        } catch (InvalidPortException | DatastoreException | InvalidEndpointException e) {
            e.printStackTrace();
        }
        fine = new QuestionFineTyper(modelsFolder + "QuestionFineTyper.lc", modelsFolder + "QuestionFineTyper.lex");
        coarse = new QuestionCoarseTyper(modelsFolder + "QuestionCoarseTyper.lc", modelsFolder + "QuestionCoarseTyper.lex");
    }

    @Override
    protected void addView(TextAnnotation ta) throws AnnotatorException {
        SpanLabelView view = new SpanLabelView(ViewNames.QUESTION_TYPE, ViewNames.QUESTION_TYPE, ta, 1.0);
        assert ta.getAvailableViews().contains(ViewNames.SHALLOW_PARSE) && ta.getAvailableViews().contains(ViewNames.NER_CONLL) &&
                ta.getAvailableViews().contains(ViewNames.NER_ONTONOTES): "the annotator does not have the required views ";
        String fineLabel = fine.discreteValue(ta);
        Double fineLabelScore = fine.scores(ta).getScore(fineLabel).score;
        String coarseLabel = coarse.discreteValue(ta);
        Double coarseLabelScore = coarse.scores(ta).getScore(coarseLabel).score;
        Constituent cFine = new Constituent(fineLabel, fineLabelScore, ViewNames.QUESTION_TYPE,
                ta, 0, ta.getTokens().length);
        Constituent cCoarse = new Constituent(coarseLabel, coarseLabelScore, ViewNames.QUESTION_TYPE, ta, 0, ta.getTokens().length);
        view.addConstituent(cCoarse);
        view.addConstituent(cFine);
        ta.addView(ViewNames.QUESTION_TYPE, view);
    }
}
