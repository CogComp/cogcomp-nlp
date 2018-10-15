/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.TreeMap;

import edu.illinois.cs.cogcomp.edison.features.helpers.TestPosHelper;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;

public class TestPOSBaseLineCounter {
    private static Logger logger = LoggerFactory.getLogger(TestPOSBaseLineCounter.class);

    @Test
    public final void test() throws Exception {

        POSBaseLineCounter posBaseLine = new POSBaseLineCounter("posBaseLine");
        posBaseLine.buildTable(TestPosHelper.corpus);


        String str = "Test Corpus: section0.br\n";

        for (String form : posBaseLine.table.keySet()) {
            str += "	" + form + ": \n";
            TreeMap<String, Integer> posSet = posBaseLine.table.get(form);

            for (String pos : posSet.keySet()) {
                str += "		" + pos + ": " + posSet.get(pos) + "\n";
            }
        }

        logger.info(str);

        try {
            File file = new File("src/test/resources/outputFiles/TestBaseLineCounterOutput");

            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(str);
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
