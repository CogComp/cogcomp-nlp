package edu.illinois.cs.cogcomp.comma.readers;

import edu.illinois.cs.cogcomp.comma.datastructures.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.IResetableIterator;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.TextAnnotationReader;


/**
 * A Sentence Reader that constructs sentences from the TextAnnotationReader
 */
public class TextAnnotationReaderToSentenceReader implements IResetableIterator<Sentence>{
	private TextAnnotationReader taReader;
	public TextAnnotationReaderToSentenceReader(TextAnnotationReader taReader) {
		this.taReader = taReader;
		reset();
	}

	@Override
	public boolean hasNext() {
		return taReader.hasNext();
	}
	
	@Override
	public Sentence next() {
		TextAnnotation ta = taReader.next();
		return new Sentence(ta, ta);
	}

	@Override
	public void remove() {
		taReader.remove();
	}

	@Override
	public void reset() {
		taReader.reset();
	}

}
