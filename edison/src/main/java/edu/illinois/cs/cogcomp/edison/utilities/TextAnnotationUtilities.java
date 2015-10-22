package edu.illinois.cs.cogcomp.edison.utilities;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.utilities.BasicTextAnnotationBuilder;

import java.util.Collections;
import java.util.Comparator;

/**
 * @author Vivek Srikumar
 */
public class TextAnnotationUtilities {

	public final static Comparator<Constituent> constituentStartComparator = new Comparator<Constituent>() {
		public int compare(Constituent arg0, Constituent arg1) {
			int start0 = arg0.getStartSpan();
			int start1 = arg1.getStartSpan();
			if (start0 < start1) return -1;
			else if (start0 == start1) return 0;
			else return 1;
		}
	};
	public final static Comparator<Sentence> sentenceStartComparator = new Comparator<Sentence>() {

		@Override
		public int compare(Sentence o1, Sentence o2) {
			return constituentStartComparator.compare(o1.getSentenceConstituent(), o2.getSentenceConstituent());
		}
	};
	public final static Comparator<Constituent> constituentEndComparator = new Comparator<Constituent>() {

		@Override
		public int compare(Constituent arg0, Constituent arg1) {
			int end0 = arg0.getEndSpan();
			int end1 = arg1.getEndSpan();

			if (end0 < end1) return -1;
			else if (end0 > end1) return 1;
			else return 0;

		}
	};
	public final static Comparator<Constituent> constituentLengthComparator = new Comparator<Constituent>() {

		@Override
		public int compare(Constituent arg0, Constituent arg1) {
			int size0 = arg0.size();
			int size1 = arg1.size();

			if (size0 < size1) return -1;
			else if (size0 > size1) return 1;
			else return 0;

		}
	};

	public static TextAnnotation createFromTokenizedString(String text) {
		return BasicTextAnnotationBuilder.createTextAnnotationFromTokens(Collections.singletonList(text.split(" ")));
	}

	public static String getTokenSequence(TextAnnotation ta, int start, int end) {
		return new Constituent("", "", ta, start, end).toString();
	}

}
