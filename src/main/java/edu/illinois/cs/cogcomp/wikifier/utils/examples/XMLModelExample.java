package edu.illinois.cs.cogcomp.wikifier.utils.examples;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import edu.illinois.cs.cogcomp.wikifier.utils.XmlModel;

/***
 * Example on serializing classes to xml using XmlModel interface. This will serialize an
 * object with JAXB annotations to xml. Member objects are also serialized if they have jaxb annotations.
 * 
 * @author upadhya3
 *
 */
@XmlRootElement(name = "exampleClass")
@XmlAccessorType(XmlAccessType.FIELD)
// this will serialize all fields to xml, 
// for more details on Xmlaccesstype see,
// http://blog.bdoughan.com/2011/06/using-jaxbs-xmlaccessortype-to.html
public class XMLModelExample {
	public String fieldA;
	public String fieldB;
	private String fieldC; // does not matter its private, a field is a field
	private EnclosedObject obj;
	public List<EnclosedObject> lists;
	
	/**
	 * Used for JAXB calls only. This is important. JAXB wont work if you do not
	 * have a empty constructor.
	 */
	public XMLModelExample() {
	}

	public XMLModelExample(String a, String b, String c) {
		fieldA = a;
		fieldB = b;
		fieldC = c;
	}

	public void setEnclosed(EnclosedObject s1)
	{
		this.obj=s1;
	}
	public static void main(String[] args) {
		XMLModelExample ob = new XMLModelExample("1", "2", "3");
		ob.setEnclosed(new EnclosedObject("xyz"));
		ob.lists=new ArrayList<EnclosedObject>();
		ob.lists.add(new EnclosedObject("ijk"));
		ob.lists.add(new EnclosedObject("pqr"));
		ob.lists.add(new EnclosedObject("mno"));
		try {
			XmlModel.write(ob, "somefile.xml");
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
}
