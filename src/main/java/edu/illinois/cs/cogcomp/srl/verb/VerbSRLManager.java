package edu.illinois.cs.cogcomp.srl.verb;

import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;
import edu.illinois.cs.cogcomp.srl.core.*;
import edu.illinois.cs.cogcomp.srl.data.FramesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public class VerbSRLManager extends SRLManager {
	private final static Logger log = LoggerFactory.getLogger(VerbSRLManager.class);

	// XXX: Don't change these arrays unless you know what you are doing. If you
	// change the order of elements, or adding or removing elements, then you
	// have to re-train the Classifier.
	private static final String[] coreArguments = { "A0", "A1", "A2", "A3",
			"A4", "A5", "AA" };

	private static final String[] modifierArguments = { "AM-ADV", "AM-CAU",
			"AM-DIR", "AM-DIS", "AM-EXT", "AM-LOC", "AM-MNR", "AM-MOD",
			"AM-NEG", "AM-PNC", "AM-PRD", "AM-REC", "AM-TMP" };

	private static final String[] allArguments = { NULL_LABEL, "A0", "A1",
			"A2", "A3", "A4", "A5", "AA", "AM-ADV", "AM-CAU", "AM-DIR",
			"AM-DIS", "AM-EXT", "AM-LOC", "AM-MNR", "AM-MOD", "AM-NEG",
			"AM-PNC", "AM-PRD", "AM-REC", "AM-TMP", "C-A0", "C-A1", "C-A2",
			"C-A3", "C-AM-ADV", "C-AM-CAU", "C-AM-DIS", "C-AM-EXT", "C-AM-LOC",
			"C-AM-MNR", "R-A0", "R-A1", "R-A2", "R-A3", "R-AA", "R-AM-ADV",
			"R-AM-LOC", "R-AM-MNR", "R-AM-PNC", "R-AM-TMP", "C-V", "C-A4",
			"C-AM-DIR", "C-AM-NEG", "C-AM-PNC", "C-AM-TMP", "R-A4", "R-AM-CAU",
			"R-AM-EXT" };

	// XXX: Ignoring the sense "XX".
	private static final String[] allSenses = { "01", "02", "03", "04", "05",
			"06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16",
			"17", "18", "19", "20", "21" };

	public final static Set<String> coreArgumentSet = Collections
			.unmodifiableSet(new TreeSet<String>(Arrays.asList(coreArguments)));

	public static final Set<String> modifierArgumentSet = Collections
			.unmodifiableSet(new TreeSet<String>(Arrays
					.asList(modifierArguments)));

	private ArgumentCandidateGenerator candidateGenerator;

	private final AbstractPredicateDetector heuristicPredicateDetector;

	public VerbSRLManager(boolean trainingMode, String defaultParser) throws Exception {
		super(trainingMode, defaultParser);
		candidateGenerator = new XuePalmerCandidateGenerator(this);
		heuristicPredicateDetector = new VerbPredicateDetector(this);

		for (int i = 0; i < this.getNumLabels(Models.Classifier); i++) {
			assert i == this.getArgumentId(this.getArgument(i));
		}
	}

	public AbstractPredicateDetector getHeuristicPredicateDetector() {
		return heuristicPredicateDetector;
	}

	@Override
	public SRLType getSRLType() {
		return SRLType.Verb;
	}

	@Override
	protected String[] getArgumentLabels() {
		return allArguments;
	}

	@Override
	protected String[] getSenseLabels() {
		return allSenses;
	}

	@Override
	public Set<String> getCoreArguments() {
		return coreArgumentSet;
	}

	@Override
	public Set<String> getModifierArguments() {
		return modifierArgumentSet;
	}

	@Override
	public int getNumArguments() {
		return allArguments.length;
	}

	@Override
	public int getNumSenses() {
		return allSenses.length;
	}

	@Override
	public String getArgument(int id) {
		return allArguments[id];
	}

	@Override
	public String getSense(int id) {
		return allSenses[id];
	}

	@Override
	public ArgumentCandidateGenerator getArgumentCandidateGenerator() {
		return this.candidateGenerator;
	}

	@Override
	public FramesManager getFrameManager() {
		return FramesManager.getPropbankInstance();
	}

	@Override
	public String getPredictedViewName() {
		return ViewNames.SRL_VERB;
	}

	@Override
	public AbstractPredicateDetector getLearnedPredicateDetector() {
		// for the verbs, we don't want a learned predicate detector because the
		// POS based heuristic works very well.
		// return getHeuristicPredicateDetector();
		if (this.getSRLType() == SRLType.Nom) {
			try {
				return new LearnedPredicateDetector(this);
			} catch (Exception e) {
				log.error("Unable to create learned predicate detector!", e);
				throw new RuntimeException(e);
			}
		} else {
			return getHeuristicPredicateDetector();
		}
	}

	@Override
	public int getPruneSize(Models model) {
		switch (model) {
		case Classifier:
			return 6;
		case Identifier:
			return 4;
		default:
			return 4;

		}
	}

}
