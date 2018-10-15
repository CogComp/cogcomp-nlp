/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.utilities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/***
 * Example on serializing classes to xml using XmlModel interface. This will serialize an object
 * with JAXB annotations to xml. Member objects are also serialized if they have jaxb annotations.
 *
 * @author upadhya3
 */
@XmlRootElement(name = "exampleClass")
@XmlAccessorType(XmlAccessType.FIELD)
// this will serialize all fields to xml,
// for more details on Xmlaccesstype see,
// http://blog.bdoughan.com/2011/06/using-jaxbs-xmlaccessortype-to.html
public class XMLModelExample {
    public String fieldA;
    public String fieldB;
    private String fieldC; // does not matter its private, a field is a
    // field
    private EnclosedObject obj;
    public List<EnclosedObject> lists;

    /**
     * Used for JAXB calls only. This is important. JAXB wont work if you do not have a empty
     * constructor.
     */
    public XMLModelExample() {}

    public XMLModelExample(String a, String b, String c) {
        fieldA = a;
        fieldB = b;
        fieldC = c;
    }

    public void setEnclosed(EnclosedObject s1) {
        this.obj = s1;
    }

}
