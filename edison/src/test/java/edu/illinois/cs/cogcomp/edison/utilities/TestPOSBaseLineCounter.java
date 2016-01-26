package edu.illinois.cs.cogcomp.edison.utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.TreeMap;

import org.junit.Test;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.edison.features.factory.Constant;
import edu.illinois.cs.cogcomp.edison.features.factory.POSBaseLineFeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.factory.TestPOSBaseLineFeatureExtractor;
import junit.framework.TestCase;

public class TestPOSBaseLineCounter extends TestCase {
	
	public final void test()throws Exception{
		String prefix = Constant.prefix;
		String fileName = prefix + Constant.POSCorpus + Constant.POSCorpus01;
		
		POSBaseLineCounter posBaseLine = new POSBaseLineCounter("posBaseLine");
		posBaseLine.buildTable(fileName);
		
		
		String str = "Test Corpus: section0.br\n";
		
		for (String form : posBaseLine.table.keySet()){
			str += "	" + form + ": \n";
			TreeMap<String, Integer> posSet = posBaseLine.table.get(form);
			
			for (String pos : posSet.keySet()){
				str += "		" + pos + ": " + posSet.get(pos) + "\n";
			}
		}
		
		System.out.println(str);
		
		try {
			File file = new File(prefix + Constant.testResources + "\\outputFiles\\TestBaseLineCounterOutput");

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