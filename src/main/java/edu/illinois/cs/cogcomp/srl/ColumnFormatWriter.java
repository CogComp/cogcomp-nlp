package edu.illinois.cs.cogcomp.srl;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.PredicateArgumentView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotationUtilities;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.edison.features.helpers.ParseHelper;
import edu.illinois.cs.cogcomp.edison.features.helpers.WordHelpers;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.CoNLLColumnFormatReader;
import edu.illinois.cs.cogcomp.nlp.utilities.ParseUtils;

/**
 * Prints text annotation formatted with one word per line as follows.
 * <blockquote> <code>
 *     form  POS  full-parse  chunk  NE  verb-sense  verb-lemma  [verb1-args
 *     [verb2-args ... ]]
 *   </code> </blockquote>
 * 
 * @author Vivek Srikumar
 */
public class ColumnFormatWriter {

	private final String predicateArgumentViewName;
	private final String parseViewName;

	public ColumnFormatWriter(String parseViewName,
			String predicateArgumentViewName) {
		this.parseViewName = parseViewName;
		this.predicateArgumentViewName = predicateArgumentViewName;
	}

	public ColumnFormatWriter() {
		this(ViewNames.PARSE_CHARNIAK, ViewNames.SRL_VERB);
	}

	public void transform(Iterable<TextAnnotation> reader, PrintWriter out)
			throws Exception {

		for (TextAnnotation ta : reader) {
			transform(ta, out);
		}
	}

	public void transform(TextAnnotation ta, PrintWriter out) throws Exception {
		String[][] columns = transformToColumns(ta);
		printFormatted(columns, out, ta);
	}

	public void printPredicateArgumentView(PredicateArgumentView pav,
			PrintWriter out) {
		// System.out.println("*" + pav + "*");

		List<String[]> columns = new ArrayList<>();
		convertPredicateArgView(pav.getTextAnnotation(), pav, columns, false);

		String[][] tr = transpose(columns, pav.getTextAnnotation().size());

		printFormatted(tr, out, pav.getTextAnnotation());
	}

	private void printFormatted(String[][] columns, PrintWriter out, TextAnnotation ta) {
		// leftOfStar: length of everything before the asterisk.
		// rightOfStar: length of asterisk and what comes after.

		int[] leftOfStar = new int[columns[0].length];
		int[] rightOfStart = new int[columns[0].length];

        for (String[] rowData : columns) {
            for (int col = 0; col < rowData.length; col++) {

                String word = rowData[col];

                int starPos = word.indexOf("*");

                int lenLeft, lenRight;
                if (starPos < 0) {
                    lenLeft = word.length();
                    lenRight = -1;
                } else {
                    lenLeft = starPos + 1;
                    lenRight = word.length() - starPos + 1;
                }

                if (leftOfStar[col] < lenLeft)
                    leftOfStar[col] = lenLeft;

                if (rightOfStart[col] < lenRight)
                    rightOfStart[col] = lenRight;
            }
        }

		// System.out.println("here");

		assert ta.size() == columns.length;

		for (int sentenceId = 0; sentenceId < ta.getNumberOfSentences(); sentenceId++) {

			int start = ta.getSentence(sentenceId).getStartSpan();

			for (int row = start; row < ta.getSentence(sentenceId).getEndSpan(); row++) {
				String[] rowData = columns[row];

				out.print(rowData[0]);

				// print the spaces
				for (int spCount = rowData[0].length(); spCount < leftOfStar[0]; spCount++)
					out.print(" ");

				out.print("  " + rowData[1]);

				// print the spaces
				for (int spCount = rowData[1].length(); spCount < leftOfStar[1]; spCount++)
					out.print(" ");

				out.print("  ");

				for (int colId = 2; colId < rowData.length; colId++) {

					String word = rowData[colId];

					int starPos = word.indexOf("*");

					int leftSpaces, rightSpaces;
					leftSpaces = leftOfStar[colId];
					rightSpaces = rightOfStart[colId];

					if (rightSpaces == 0)
						leftSpaces = 0;
					else
						leftSpaces -= starPos;

					if (rightSpaces == 0) {
						rightSpaces = leftOfStar[colId] - word.length();
					} else {
						rightSpaces -= (word.length() - starPos);
					}

					for (int i = 0; i < leftSpaces - 1; i++)
						out.print(" ");

					out.print(word + "  ");

					for (int i = 0; i < rightSpaces; i++)
						out.print(" ");

				}

				out.println();
			}

			out.println();
		}
	}

	private String[][] transpose(List<String[]> columns, int size) {
		String[][] output = new String[size][];

		for (int i = 0; i < size; i++) {
			output[i] = new String[columns.size()];
		}

		for (int row = 0; row < size; row++) {
			for (int col = 0; col < columns.size(); col++) {
				output[row][col] = columns.get(col)[row];
			}
		}

		return output;
	}

	/**
	 * Return a table. Numrows = number of words. Num Cols depends on how many
	 * predicate arg relations we have
	 */
	private String[][] transformToColumns(TextAnnotation ta) {
		List<String[]> columns = new ArrayList<>();

		// first the words
		String[] form = new String[ta.size()];
		String[] pos = new String[ta.size()];
		for (int i = 0; i < ta.size(); i++) {
			form[i] = WordHelpers.getWord(ta, i);
			pos[i] = WordHelpers.getPOS(ta, i);
		}

		columns.add(form);
		columns.add(pos);

		// now the parse
		String[] parse = getParse(ta);
		columns.add(parse);

		// add the chunks
		String[] chunk = getChunkData(ta);
		columns.add(chunk);

		// add the ner. For now, we don't have ner annotation
		String[] ne = getNEData(ta);
		columns.add(ne);

		// if (ta.hasView(ViewNames.SRL_NOM))
		// // add the predicate argument column information
		// addPredicateArgs(columns, ta);

		// add the predicate argument column information
		addPredicateArgs(columns, ta);

		return transpose(columns, ta.size());
	}

	private static String[] getNEData(TextAnnotation ta) {

		if (!ta.hasView(ViewNames.NER_CONLL)) {
			String[] chunk = new String[ta.size()];

			for (int i = 0; i < ta.size(); i++) {
				chunk[i] = "*";
			}

			return chunk;
		}
		SpanLabelView nerView = (SpanLabelView) ta.getView(ViewNames.NER_CONLL);

		List<Constituent> nerConstituents = nerView.getConstituents();

		Collections.sort(nerConstituents,
				TextAnnotationUtilities.constituentStartComparator);

		Map<Integer, String> cc = new HashMap<>();
		for (Constituent c : nerConstituents) {
			for (int i = c.getStartSpan(); i < c.getEndSpan(); i++) {
				if (i == c.getStartSpan())
					cc.put(i, "(" + c.getLabel());
				else
					cc.put(i, "");

				cc.put(i, cc.get(i) + "*");

				if (i == c.getEndSpan() - 1)
					cc.put(i, cc.get(i) + ")");
			}
		}

		String[] ner = new String[ta.size()];
		for (int i = 0; i < ta.size(); i++) {
			if (cc.containsKey(i)) {
				ner[i] = cc.get(i);
			} else
				ner[i] = "*";
		}
		return ner;
	}

	private static String[] getChunkData(TextAnnotation ta) {

		if (!ta.hasView(ViewNames.SHALLOW_PARSE)) {
			String[] chunk = new String[ta.size()];

			for (int i = 0; i < ta.size(); i++) {
				chunk[i] = "*";
			}

			return chunk;
		}

		SpanLabelView chunkView = (SpanLabelView) ta.getView(ViewNames.SHALLOW_PARSE);

		List<Constituent> chunkConstituents = chunkView.getConstituents();

		Collections.sort(chunkConstituents, TextAnnotationUtilities.constituentStartComparator);

		Map<Integer, String> cc = new HashMap<>();
		for (Constituent c : chunkConstituents) {
			for (int i = c.getStartSpan(); i < c.getEndSpan(); i++) {
				if (i == c.getStartSpan())
					cc.put(i, "(" + c.getLabel());
				else
					cc.put(i, "");

				cc.put(i, cc.get(i) + "*");

				if (i == c.getEndSpan() - 1)
					cc.put(i, cc.get(i) + ")");
			}
		}

		String[] chunk = new String[ta.size()];
		for (int i = 0; i < ta.size(); i++) {
			if (cc.containsKey(i)) {
				chunk[i] = cc.get(i);
			} else
				chunk[i] = "*";
		}
		return chunk;
	}

	private void addPredicateArgs(List<String[]> columns, TextAnnotation ta) {
		PredicateArgumentView predArgView = null;

		if (ta.hasView(predicateArgumentViewName))
			predArgView = (PredicateArgumentView) ta.getView(predicateArgumentViewName);

		convertPredicateArgView(ta, predArgView, columns, true);

	}

	private void convertPredicateArgView(TextAnnotation ta,
			PredicateArgumentView pav, List<String[]> columns, boolean addSense) {

		List<Constituent> predicates = new ArrayList<>();
		if (pav != null)
			predicates = pav.getPredicates();

		Collections.sort(predicates, TextAnnotationUtilities.constituentStartComparator);

		int size = ta.size();

		addPredicateInfo(columns, predicates, size, addSense);

		for (Constituent predicate : predicates) {
            assert pav != null;
            List<Relation> args = pav.getArguments(predicate);

			String[] paInfo = addPredicateArgInfo(predicate, args, size);

			columns.add(paInfo);
		}
	}

	private void addPredicateInfo(List<String[]> columns,
			List<Constituent> predicates, int size, boolean addSense) {
		Map<Integer, String> senseMap = new HashMap<>();
		Map<Integer, String> lemmaMap = new HashMap<>();

		for (Constituent c : predicates) {
			senseMap.put(c.getStartSpan(), c.getAttribute(CoNLLColumnFormatReader.SenseIdentifer));
			lemmaMap.put(c.getStartSpan(), c.getAttribute(CoNLLColumnFormatReader.LemmaIdentifier));
		}

		String[] sense = new String[size];
		String[] lemma = new String[size];

		for (int i = 0; i < size; i++) {
			if (lemmaMap.containsKey(i)) {

				sense[i] = senseMap.get(i);
				lemma[i] = lemmaMap.get(i);
			} else {
				sense[i] = "-";
				lemma[i] = "-";
			}
		}

		if (addSense)
			columns.add(sense);
		columns.add(lemma);
	}

	private String[] addPredicateArgInfo(Constituent predicate,
			List<Relation> args, int size) {
		Map<Integer, String> paInfo = new HashMap<>();

		paInfo.put(predicate.getStartSpan(), "(V*)");
		for (Relation r : args) {
			String argPredicate = r.getRelationName();

			argPredicate = argPredicate.replaceAll("ARG", "A");
			argPredicate = argPredicate.replaceAll("Support", "SUP");

			for (int i = r.getTarget().getStartSpan(); i < r.getTarget().getEndSpan(); i++) {
				paInfo.put(i, "*");
				if (i == r.getTarget().getStartSpan())
					paInfo.put(i, "(" + argPredicate + paInfo.get(i));
				if (i == r.getTarget().getEndSpan() - 1)
					paInfo.put(i, paInfo.get(i) + ")");

			}
		}

		String[] paColumn = new String[size];
		for (int i = 0; i < size; i++) {
			if (paInfo.containsKey(i))
				paColumn[i] = paInfo.get(i);
			else
				paColumn[i] = "*";

		}

		return paColumn;
	}

	private String[] getParse(TextAnnotation ta) {
		String[] parse = new String[ta.size()];

		for (int sentenceId = 0; sentenceId < ta.getNumberOfSentences(); sentenceId++) {

			Tree<String> tree = ParseHelper.getParseTree(parseViewName, ta, sentenceId);

			Sentence sentence = ta.getSentence(sentenceId);

			tree = ParseUtils.snipNullNodes(tree);
			tree = ParseUtils.stripFunctionTags(tree);

			String[] treeLines = tree.toString().split("\n");

			if (treeLines.length != sentence.size()) {

				System.out.println(ta);
				System.out.println(ta.getView(parseViewName));

				System.out.println("Sentence: " + sentence);

				throw new IllegalStateException("Expected " + sentence.size()
						+ " tokens, but found " + treeLines.length
						+ " tokens in the tree");
			}

			for (int i = 0; i < treeLines.length; i++) {

				String t = treeLines[i].replaceAll(" ", "");

				// get rid of the word
				t = t.replaceAll("\\([^\\(\\)]*\\)", "*");

				// get rid of the pos and replace with a "*"
				parse[sentence.getStartSpan() + i] = t;
			}
		}
		return parse;
	}
}