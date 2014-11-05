package edu.illinois.cs.cogcomp.wikifier.utils.examples;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "enclosedObject")
@XmlAccessorType(XmlAccessType.FIELD)
public class EnclosedObject {
	
	private String str;
	public int some_state=10;
	public EnclosedObject(){
		
	}
	public EnclosedObject(String str) {
		this.str= str;
	}

}
