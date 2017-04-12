package edu.illinois.cs.cogcomp.wsim.esa;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.StopWords;

/**
 * Shaoshi Ling
 * sling3@illinois.edu
 */

public class ResourcesConfig {
	public static String word2vec;
	public static String paragram;
	public static String truncated;
	public static String phrase2vec;
	public static String memorybasedESA; 
	public static int dimension;
	public static String pageIDMapping;
	
	public ResourcesConfig(){
		initialization();
	}
	
	public static void initialization () {
		initialization("conf/configurations.properties");
	}
	
	public static void initialization (String configFile) {
		// Read the configuration file
		PropertiesConfiguration config = null;
		try {
			config = new PropertiesConfiguration(configFile);
		} catch (ConfigurationException e1) {
			e1.printStackTrace();
		}
		word2vec = config.getString("cogcomp.word2vec", "data/MemoryBasedESA.txt");
		paragram = config.getString("cogcomp.paragram", "data/MemoryBasedESA.txt");
		truncated = config.getString("cogcomp.truncated", "data/MemoryBasedESA.txt");
		phrase2vec = config.getString("cogcomp.phrase2vec", "data/MemoryBasedESA.txt");
		pageIDMapping = config.getString("cogcomp.esa.complex.pageIDMapping", "data/wikipedia/wiki_structured/wikiPageIDMapping.txt");
		memorybasedESA = config.getString("cogcomp.esa.memory.wordIndex", "data/MemoryBasedESA.txt");
		dimension = config.getInt("cogcomp.esa.memory.wordIndex", 200);

		
		System.out.println("Configuration Done.");
	}
	
}	