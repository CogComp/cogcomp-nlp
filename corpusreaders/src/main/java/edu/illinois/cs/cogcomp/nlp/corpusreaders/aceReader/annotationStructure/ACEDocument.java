/**
 * This software is released under the University of Illinois/Research and
 *  Academic Use License. See the LICENSE file in the root folder for details.
 * Copyright (c) 2016
 *
 * Developed by:
 * The Cognitive Computation Group
 * University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
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
