package edu.illinois.cs.cogcomp.nlp.curator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.cs.cogcomp.thrift.base.Node;
import edu.illinois.cs.cogcomp.thrift.base.Span;

/**
 * Useful String Utilities
 * 
 * @author James Clarke
 * 
 */
public class StringUtil {

	private static final Logger logger = LoggerFactory.getLogger(StringUtil.class);
	/**
	 * Replicates Python's str.join function but allows for any Object to be
	 * joined.
	 * 
	 * Given a collection of Objects create a String representation of the
	 * Objects with the delimiter between. join(["apples", "bananas", "pears",
	 * "oranges"], ", ") outputs "apples, bananas, pears, oranges".
	 * 
	 * @param objs
	 * @param delimiter
	 * @return
	 */
	public static <T> String join(final Collection<T> objs,
			final String delimiter) {
		if (objs == null || objs.isEmpty())
			return "";
		Iterator<T> iter = objs.iterator();
		StringBuffer buffer = new StringBuffer(iter.next().toString());
		while (iter.hasNext())
			buffer.append(delimiter).append(iter.next().toString());
		return buffer.toString();
	}

	/**
	 * Normalizes a string from Penn Treebank format.
	 * 
	 * @param string
	 * @return
	 */
	public static String normalize(String string) {
		string = string.replaceAll("` ", "'");
		string = string.replaceAll("`` ", "\"");
		string = string.replaceAll(" ''", "\"");

		string = string.replaceAll("-LRB-", "(");
		string = string.replaceAll("-RRB-", ")");

		string = string.replaceAll("-LCB-", "{");
		string = string.replaceAll("-RCB-", "}");

		string = string.replaceAll("-LSB-", "[");
		string = string.replaceAll("-RSB-", "]");

		string = string.replaceAll("\\s+", " ");

		string = string.replaceAll(" '", "'");

		string = string.replaceAll(" ,", ",");
		string = string.replaceAll(" \\.", "\\.");
		return string;
	}

	/**
	 * Retrieve the text representing the span.
	 * 
	 * @param span
	 * @param rawText
	 * @return
	 */
	public static String spanToString(Span span, String rawText) {
		if (span.getEnding() > rawText.length()) {
			logger.error("Span end is greater than rawText length");
			logger.error("Raw Text: {}", rawText);
			logger.error("Span: {}", span);
		}
		return rawText.substring(span.getStart(), span.getEnding());
	}

	/**
	 * Converts a Node and all its children to a nicely formatted string
	 * 
	 * @param node
	 *            the Node
	 * @param nodes
	 *            all the nodes
	 * @param tokens
	 *            the tokens for these nodes
	 * @param padding
	 *            padding to apply
	 * @return
	 */
	public static String nodeToString(Node node, List<Node> nodes,
			String rawText, int padding) {
		StringBuffer s = new StringBuffer();
		for (int i = 0; i < padding; i++) {
			s.append("  ");
		}
		s.append(node.getLabel());
		s.append(" : [");
		s.append(StringUtil.spanToString(node.getSpan(), rawText));
		s.append("]");
		if (node.isSetChildren()) {
			padding += 1;
			for (int index : node.getChildren().keySet()) {
				s.append("\n");
				String label = node.getChildren().get(index);
				if (label != "") {
					for (int i = 0; i < padding; i++) {
						s.append(" ");
					}
					s.append(">edge label: " + label + "\n");
				}
				s
						.append(nodeToString(nodes.get(index), nodes, rawText,
								padding));
			}
		}
		return s.toString();
	}

	/**
	 * Converts sentences and tokens represented as spans into a list of lists
	 * of string.
	 * 
	 * @param tokens
	 * @param sentences
	 * @param rawText
	 * @return
	 */
	public static List<List<String>> tokensAsStrings(List<Span> tokens,
			List<Span> sentences, String rawText) {
		List<List<String>> strTokens = new ArrayList<List<String>>();
		int sentNum = 0;
		Span sentence = sentences.get(sentNum);
		strTokens.add(new ArrayList<String>());
		for (Span token : tokens) {
			if (token.getStart() >= sentence.getEnding()) {
				strTokens.add(new ArrayList<String>());
				sentNum++;
				sentence = sentences.get(sentNum);
			}
			strTokens.get(sentNum).add(
					rawText.substring(token.getStart(), token.getEnding()));
		}
		return strTokens;
	}


	/**
	 * Finds the span (as start and end indices) where the word occurs in the
	 * rawText starting at from.
	 * 
	 * @param from
	 * @param rawText
	 * @param word
	 * @return
	 */
	public static int[] findSpan(int from, String rawText, String word) {
		int start = rawText.indexOf(word, from);
		int end = start + word.length();
		return new int[] { start, end };
	}
}
