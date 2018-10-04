/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.annotationStructure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ACERelation implements Serializable {

    public String id; // ID;
    public String type;// TYPE;
    public String subtype;// SUBTYPE;
    public String modality;// MODALITY;
    public String tense;// TENSE;

    public List<ACERelationArgument> relationArgumentList = new ArrayList<ACERelationArgument>();
    public List<ACERelationMention> relationMentionList = new ArrayList<ACERelationMention>();

}
