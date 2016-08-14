/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.annotationStructure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ACERelationMention implements Serializable {

    private static final long serialVersionUID = 1L;

    public String id;
    public String lexicalCondition;

    public int extentStart;
    public int extentEnd;
    public String extent;

    public List<ACERelationArgumentMention> relationArgumentMentionList =
            new ArrayList<ACERelationArgumentMention>();

}
