package edu.illinois.cs.cogcomp.edison.utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.TreeMap;

import edu.illinois.cs.cogcomp.edison.features.helpers.TestPosHelper;
import junit.framework.TestCase;

public class TestPOSBaseLineCounter extends TestCase {

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

        System.out.println(str);

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
