package edu.illinois.cs.cogcomp.core.utilities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class EnclosedObject {

    private String str;
    public int some_state = 10;

    public EnclosedObject() {

    }

    public EnclosedObject(String str) {
        this.str = str;
    }

}
