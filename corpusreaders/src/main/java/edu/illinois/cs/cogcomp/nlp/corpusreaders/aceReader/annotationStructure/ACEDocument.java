package edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.annotationStructure;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.Paragraph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ACEDocument implements Serializable {

	public ACEDocumentAnnotation aceAnnotation;

	public List<TextAnnotation> taList = new ArrayList<TextAnnotation>();

	public String orginalContent;

	public String contentRemovingTags;

	public List<String> originalLines;

	public List<Pair<String, Paragraph>> paragraphs;

}
