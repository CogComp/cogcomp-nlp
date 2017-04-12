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
	public static int paragram_dimension;
	public static String pageIDMapping;
	public static int embedding_dimension;
	
	public ResourcesConfig(){
		initialization();
	}
	
	public static void initialization () {
		initialization("config/configurations.properties");
	}
	
	public static void initialization (String configFile) {
		// Read the configuration file
		PropertiesConfiguration config = null;
		try {
			config = new PropertiesConfiguration(configFile);
		} catch (ConfigurationException e1) {
			e1.printStackTrace();
		}
		word2vec = config.getString("cogcomp.word2vec");
		paragram = config.getString("cogcomp.paragram");
		truncated = config.getString("cogcomp.truncated");
		phrase2vec = config.getString("cogcomp.phrase2vec");
		pageIDMapping = config.getString("cogcomp.esa.complex.pageIDMapping");
		memorybasedESA = config.getString("cogcomp.esa.memory.wordIndex");
		paragram_dimension = config.getInt("cogcomp.paragram.dimension", 25);
		embedding_dimension = config.getInt("cogcomp.embedding.dimension", 200);
		
		System.out.println("Configuration Done.");
	}
	
}	