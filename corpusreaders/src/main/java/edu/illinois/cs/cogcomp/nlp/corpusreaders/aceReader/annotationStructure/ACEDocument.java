/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.annotationStructure;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.Paragraph;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class ACEDocument implements Serializable {

    public ACEDocumentAnnotation aceAnnotation;

    public String orginalContent;

    public String contentRemovingTags;

    public List<String> originalLines;

    public Map<String, String> metadata;

    public List<Pair<String, Paragraph>> paragraphs;
}
