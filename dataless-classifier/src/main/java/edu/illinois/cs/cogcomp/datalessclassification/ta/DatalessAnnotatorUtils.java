/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.datalessclassification.ta;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DatalessAnnotatorUtils {
	
	public static Map<String, String> getLabelNameMap (String filePath) {
		Map<String, String> labelNameMap = new HashMap<>();
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			String line;
			
			while ((line = reader.readLine()) != null) {
				if (line.isEmpty()) 
					continue;
				
				String[] tokens = line.split("\t", 2);

				String labelId = tokens[0].trim();
				String labelName = tokens[1].trim();
				
				labelNameMap.put(labelId, labelName);
			}
			
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return labelNameMap;
	}
	
	public static Map<String, String> getLabelDescriptionMap (String filePath) {
		Map<String, String> labelDesriptionMap = new HashMap<>();
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			String line;
			
			while ((line = reader.readLine()) != null) {
				if (line.isEmpty()) 
					continue;
				
				String[] tokens = line.split("\t", 2);

				String labelId = tokens[0].trim();
				String labelDesc = tokens[1].trim();
				
				labelDesriptionMap.put(labelId, labelDesc);
			}
			
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return labelDesriptionMap;
	}
	
	public static Set<String> getTopNodes (String hierarchyPath) {
		Set<String> topNodes = new HashSet<>();
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(hierarchyPath));
			String line = reader.readLine();
			
			String[] nodes = line.split("\t");
			
			for (String node : nodes) {
				topNodes.add(node);
			}
			
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return topNodes;
	}
	
	public static Map<String, Set<String>> getParentChildMap (String hierarchyPath) {
		Map<String, Set<String>> parentChildMap = new HashMap<>();
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(hierarchyPath));
			
			reader.readLine();
			
			String line;
			String[] nodes;
			
			while ((line = reader.readLine()) != null) {
				if (line.isEmpty()) 
					continue;
				
				String[] tokens = line.split("\t", 2);

				String parentID = tokens[0].trim();
				String childIDs = tokens[1].trim();
				Set<String> childIDSet = new HashSet<>();
				
				nodes = childIDs.split("\t");
				
				for (String node : nodes) {
					childIDSet.add(node);
				}
				
				parentChildMap.put(parentID, childIDSet);
			}
			
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return parentChildMap;
	}
}
