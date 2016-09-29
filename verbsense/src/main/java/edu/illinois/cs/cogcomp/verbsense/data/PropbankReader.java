package edu.illinois.cs.cogcomp.verbsense.data;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.edison.data.CoNLLColumnFormatReader;
import edu.illinois.cs.cogcomp.edison.data.corpora.PennTreebankReader;
import edu.illinois.cs.cogcomp.edison.features.helpers.ParseHelper;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.TokenLabelView;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import edu.illinois.cs.cogcomp.edison.utilities.ParseUtils;
import edu.illinois.cs.cogcomp.verbsense.core.SenseManager;

import java.io.FileNotFoundException;
import java.util.*;

public class PropbankReader extends PennTreebankReader {
	public static final String LemmaIdentifier = CoNLLColumnFormatReader.LemmaIdentifier;

	private Iterator<TextAnnotation> wsjIterator;
	private final Map<String, List<PropbankFields>> goldFields;

	private final Set<String> sections;
	protected String dataHome;

	public PropbankReader(String treebankHome, String dataHome, String[] sections) throws Exception {
		super(treebankHome, sections);
		this.dataHome = dataHome;

		this.sections = new HashSet<>();
		if (sections != null) {
			this.sections.addAll(Arrays.asList(sections));
		}

		this.goldFields = new HashMap<>();
		readData();

		wsjIterator = null;
	}

	private void readData() throws NumberFormatException, FileNotFoundException {
		String dataFile = dataHome + "/prop.txt";
		for (String line : LineIO.read(dataFile)) {
			PropbankFields n = new PropbankFields(line);

			if (this.sections.contains(n.getSection())) {
				if (!this.goldFields.containsKey(n.getIdentifier())) {
					this.goldFields.put(n.getIdentifier(), new ArrayList<PropbankFields>());
				}
				this.goldFields.get(n.getIdentifier()).add(n);
			}
		}
	}

	public final boolean hasNext() {
		if (wsjIterator == null)
			return super.hasNext();
		else
			return this.wsjIterator.hasNext();
	}

	@Override
	public final TextAnnotation next() {
		TextAnnotation ta;
		if (wsjIterator == null)
			ta = super.next();
		else
			ta = wsjIterator.next();

		assert ta != null;

		if (this.goldFields.containsKey(ta.getId()))
			addAnnotation(ta);

		return ta;
	}

	private void addAnnotation(TextAnnotation ta) {
		String goldViewName = SenseManager.getGoldViewName();
		Tree<String> tree = ParseHelper.getParseTree(ViewNames.PARSE_GOLD, ta, 0);
		Tree<Pair<String, IntPair>> spanLabeledTree = ParseUtils.getSpanLabeledTree(tree);
		List<Tree<Pair<String, IntPair>>> yield = spanLabeledTree.getYield();

		TokenLabelView view = new TokenLabelView(goldViewName, "AnnotatedTreebank", ta, 1.0);

		Set<Integer> predicates = new HashSet<>();

		for (PropbankFields fields : goldFields.get(ta.getId())) {
			int start = fields.getPredicateStart(yield);
			if (predicates.contains(start))
				continue;

			predicates.add(start);
			view.addTokenLabel(start, fields.getSense(), 1.0);
			try {
				view.addTokenAttribute(start, LemmaIdentifier, fields.getLemma());
			} catch (EdisonException e) {
				//XXX Maybe log the exception?
				e.printStackTrace();
			}
		}

		if (view.getConstituents().size() > 0)
			ta.addView(goldViewName, view);
	}
}

