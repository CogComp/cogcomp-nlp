package edu.illinois.cs.cogcomp.edison.annotators;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.edison.utilities.ParseTreeProperties;
import edu.illinois.cs.cogcomp.nlp.utilities.ParseUtils;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Creates a {@link SpanLabelView} with clause information using the parse tree.
 * This view generator follows the specification in "Tjong Kim Sang, E. F., &
 * D'ejean, H. (2001): Introduction to the CoNLL-2001 Shared Task: Clause
 * Identification" and picks out all parse tree nodes that starts with an S to
 * be a clause. In particular, FRAG and RRC are ignored.
 *
 * @author Vivek Srikumar
 */
public class ClauseViewGenerator implements Annotator {

	public static ClauseViewGenerator CHARNIAK =
			new ClauseViewGenerator(ViewNames.PARSE_CHARNIAK, ViewNames.CLAUSES_CHARNIAK);
	public static ClauseViewGenerator STANFORD =
			new ClauseViewGenerator(ViewNames.PARSE_STANFORD, ViewNames.CLAUSES_STANFORD);
	public static ClauseViewGenerator BERKELEY =
			new ClauseViewGenerator(ViewNames.PARSE_BERKELEY, ViewNames.CLAUSES_BERKELEY);

	private final String parseViewName;
	private final String clauseViewName;

	public ClauseViewGenerator(String parseViewName, String clauseViewName) {
		this.parseViewName = parseViewName;
		this.clauseViewName = clauseViewName;
	}

	@Override
	public String getViewName() {
		return clauseViewName;
	}

	@Override
	public View getView(TextAnnotation ta) {
		SpanLabelView view = new SpanLabelView(getViewName(), "From " + parseViewName, ta, 1.0, true);

		TreeView parse = (TreeView) ta.getView(parseViewName);

		Set<IntPair> set = new LinkedHashSet<>();
		for (Constituent c : parse) {
			if (TreeView.isLeaf(c)) continue;

			if (ParseTreeProperties.isPreTerminal(c)) continue;

			String label = c.getLabel();

			label = ParseUtils.stripFunctionTags(label);
			label = ParseUtils.stripIndexReferences(label);

			// This is the definition used in
			// Introduction to the CoNLL-2001 Shared Task:
			// Clause Identification

			if (label.startsWith("S") && !label.equals("S1")) {
				int start = c.getStartSpan();
				int end = c.getEndSpan();

				if (start >= 0 && end > start) {
					set.add(new IntPair(start, end));
				}
			}
		}

		for (IntPair span : set) {
			view.addSpanLabel(span.getFirst(), span.getSecond(), "S", 1.0);

		}

		return view;
	}

	@Override
	public String[] getRequiredViews() {
		return new String[]{parseViewName};
	}
}
