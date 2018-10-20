/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.utilities;

import edu.illinois.cs.cogcomp.core.io.IOUtils;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class XMLSerializationTest {
    private static Logger logger = LoggerFactory.getLogger(XMLSerializationTest.class);

    @Test
    public void testSanity() {
        logger.info("Example Usage");
        XMLModelExample ob = new XMLModelExample("1", "2", "3");
        ob.setEnclosed(new EnclosedObject("xyz"));
        ob.lists = new ArrayList<>();
        ob.lists.add(new EnclosedObject("ijk"));
        ob.lists.add(new EnclosedObject("pqr"));
        ob.lists.add(new EnclosedObject("mno"));
        try {
            XmlModel.write(ob, "somefile.xml");
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        XMLModelExample ob1 = XmlModel.load(XMLModelExample.class, "somefile.xml");
        // not complete test
        assert ob1 != null;
        assertEquals(ob1.fieldA, ob.fieldA);
        assertEquals(ob1.fieldB, ob.fieldB);
    }

    @After
    public void tearDown() throws IOException {
        IOUtils.rm("somefile.xml");
    }
}
