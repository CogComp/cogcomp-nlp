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

public class ACEDocumentAnnotation implements Serializable {

    public String id;

    public List<ACEEntity> entityList = new ArrayList<ACEEntity>();
    public List<ACEValue> valueList = new ArrayList<ACEValue>();
    public List<ACETimeEx> timeExList = new ArrayList<ACETimeEx>();
    public List<ACERelation> relationList = new ArrayList<ACERelation>();
    public List<ACEEvent> eventList = new ArrayList<ACEEvent>();
}
