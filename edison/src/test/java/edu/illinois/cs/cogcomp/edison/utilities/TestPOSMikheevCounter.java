package edu.illinois.cs.cogcomp.edison.utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
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

public class TestPOSMikheevCounter extends TestCase {
	
	public final void test()throws Exception{
		String prefix = Constant.prefix;
		String fileName = prefix + Constant.POSCorpus + Constant.POSCorpus01;
		
		POSMikheevCounter posMikheev = new POSMikheevCounter("posMikheev");
		posMikheev.buildTable(fileName);
		
		HashMap<String,HashMap<String, TreeMap<String, Integer>>> tables = 
				new HashMap<String,HashMap<String, TreeMap<String, Integer>>>();
		tables.put("firstCapitalized", posMikheev.firstCapitalized);
		tables.put("notFirstCapitalized", posMikheev.notFirstCapitalized);
		tables.put("table", posMikheev.table);
		
		String str = "Test Corpus: section0.br\n" + "\n";
		for (String tableName : tables.keySet()){
			str += tableName + "\n";
			HashMap<String, TreeMap<String, Integer>> table = tables.get(tableName);
			
			for (String form : table.keySet()){
				str += "		" + form + ": \n";
				TreeMap<String, Integer> posSet = table.get(form);

				for (String pos : posSet.keySet()){
					str += "			" + pos + ": " + posSet.get(pos) + "\n";
				}
			}
			str += "\n";
			
		}
		
		System.out.println(str);
		
		try {
			File file = new File(prefix + Constant.testResources + "\\outputFiles\\TestMikheevCounterOutput");

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