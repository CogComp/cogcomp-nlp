package edu.illinois.cs.cogcomp.corpusreaders.aceReader.annotationStructure;

import java.io.Serializable;

public class ACERelationArgument implements Serializable {

	public String id;

    // For ACE2004, only supported values of role are: (Arg-1|Arg-2)
    // For ACE2005, supported values of role are: (Arg-1|Arg-2|Time-Within|Time-Starting|Time-Ending|Time-Before|Time-After|Time-Holds|Time-At-Beginning|Time-At-End)
	public String role;
	
}
