/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.annotationStructure;

import java.io.Serializable;

public class ACERelationArgument implements Serializable {

    public String id;

    // For ACE2004, only supported values of role are: (Arg-1|Arg-2)
    // For ACE2005, supported values of role are:
    // (Arg-1|Arg-2|Time-Within|Time-Starting|Time-Ending|Time-Before|Time-After|Time-Holds|Time-At-Beginning|Time-At-End)
    public String role;

}
