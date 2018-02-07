package edu.illinois.cs.cogcomp.finetyper.finer;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.finetyper.finer.components.typers.IFinerTyper;
import edu.illinois.cs.cogcomp.finetyper.finer.components.mention.MentionDetecter;
import edu.illinois.cs.cogcomp.finetyper.finer.datastructure.FineTypeConstituent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by haowu4 on 1/15/17.
 */
public class FinerAnnotator extends Annotator {
    private MentionDetecter mentionDetecter;
    private List<IFinerTyper> typers;


    public FinerAnnotator(MentionDetecter mentionDetecter, List<IFinerTyper> typers) {
        super(ViewNames.FINE_NER_TYPE, new String[]{ViewNames.POS, ViewNames.NER_ONTONOTES});
        this.mentionDetecter = mentionDetecter;
        this.typers = typers;
    }

    public void setMentionDetecter(MentionDetecter mentionDetecter) {
        this.mentionDetecter = mentionDetecter;
    }

    public void addTyper(IFinerTyper typer) {
        this.typers.add(typer);
    }

    @Override
    public void initialize(ResourceManager rm) {

    }

    public void addView(TextAnnotation ta) {
        List<FineTypeConstituent> fineTypes = this.getAllFineTypeConstituents(ta);
        View finalAnnotation = new SpanLabelView(ViewNames.FINE_NER_TYPE, ta);
        for (FineTypeConstituent c : fineTypes) {
            Optional<Constituent> ret = c.toConstituent(ViewNames.FINE_NER_TYPE);
            ret.ifPresent(finalAnnotation::addConstituent);
        }
        ta.addView(ViewNames.FINE_NER_TYPE, finalAnnotation);
    }

    public List<FineTypeConstituent> getAllFineTypeConstituents(TextAnnotation ta) {
        List<FineTypeConstituent> allCandidates = new ArrayList<>();
        for (int i = 0; i < ta.getNumberOfSentences(); i++) {
            Sentence sent = ta.getSentence(i);
            List<FineTypeConstituent> sentence_candidates = mentionDetecter.getMentionCandidates(ta, sent);
            for (IFinerTyper typer : this.typers) {
                typer.annotate(sentence_candidates, sent);
            }

            for (FineTypeConstituent c : sentence_candidates) {
                c.finish();
                allCandidates.add(c);
            }
        }
        return allCandidates;
    }

}
