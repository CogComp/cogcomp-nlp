/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.illinois.cs.cogcomp.ner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.Properties;

import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.ner.config.NerBaseConfigurator;

/**
 * The factory, given a properties file reference will load the properties and
 * "merge" them with the defaults. In the case of properties that reference a 
 * file or directory, the property in the properties file is assumed to be a 
 * relative path. The path will be prefixed with the directory path included in 
 * the static path variable before actually being saved in the resource manager.
 * The resource manager is return after this refactoring is complete.
 * @author redman
 */
public class NERResourceManagerFactory {
                
    /**
     * check first if file in in resource fork (a jar file), if not check if it's in a file. If the
     * configuration file exists in either a jar file or on the file system return true.
     * @param configFile the file to find.
     * @throws IOException 
     */
    static private Properties checkIfExists(String configFile) throws IOException {
        InputStream is = NERResourceManagerFactory.class.getClassLoader().getResourceAsStream(configFile);
        if (is == null) {
            is = new FileInputStream(configFile);
        }
        try {
            Properties properties = new Properties();
            properties.load(is);
            return properties;
        } finally {
            try {
                is.close();
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * This method will return a resource manager that can be used by the NER
     * system, however, all paths are assumed to be relative, the  
     * resourcePath must be set to contain the folder on the system where all
     * property files, models, gazetteers and brown cluster reside.
     * 
     * @param propertiesFilename the name of the properties file.
     * @param modelsReplacementPattern pattern to replace with the model directory, or null to disable.
     * @param resourcesReplacementPattern pattern to replace with the resources directory, or null to disable.
     * @param modelsPath path where models are found, or null to disable.
     * @param resourcesPath path where resources are found, or null to disable.
     * @return the modified resources.
     * @throws FileNotFoundException if a required file was not found.
     * @throws IOException if a file was found but could not be read or parsed.
     */
    static public ResourceManager get(String propertiesFilename, String modelsReplacementPattern,
    		String resourcesReplacementPattern, String modelsPath, String resourcesPath) 
    				throws FileNotFoundException, IOException {
        
    	// check the models path.
        if (resourcesPath != null && resourcesPath.length() > 0) {
        	if (!resourcesPath.endsWith(File.separator)) {
        		resourcesPath = resourcesPath + File.separator;
        	}
        	File resourcesDirectory = new File(resourcesPath);
        	if (!resourcesDirectory.exists()) {
        		throw new FileNotFoundException("The resources directory did not exist.");
        	}
        	if (!resourcesDirectory.isDirectory()) {
        		throw new FileNotFoundException("The resources directory existed, but is not a directory.");
        	}
        }
    	Properties properties = null;
    	try {
			properties = checkIfExists(propertiesFilename);
		} catch (IOException e) {
			if (resourcesPath == null)
				throw e;
			// did not exist as presented in the argument, add the resourcePath, see if it's there.
            propertiesFilename = resourcesPath+propertiesFilename;
        	properties = checkIfExists(propertiesFilename);
		}
    	
    	// check the models path.
        if (modelsPath != null && modelsPath.length() > 0) {
        	if (modelsPath.length() > 0 && !modelsPath.endsWith(File.separator)) {
        		modelsPath = modelsPath + File.separator;
        	}
        	File modelsDirectory = new File(modelsPath);
        	if (!modelsDirectory.exists()) {
        		throw new FileNotFoundException("The models directory did not exist.");
        	}
        	if (!modelsDirectory.isDirectory()) {
        		throw new FileNotFoundException("The models directory existed, but is not a directory.");
        	}
        }
        
        // we now have the new properties, and we have the base default props,
        // merge them together modifying paths as necessary.
        ResourceManager rm = new NerBaseConfigurator().getDefaultConfig();
        for (Entry <Object, Object> entry : properties.entrySet()) {
            String name = (String)entry.getKey();
            String value = (String)entry.getValue();
            if (name.equals(NerBaseConfigurator.PATH_TO_MODEL)) {
            	if (modelsPath != null && modelsReplacementPattern != null)
            		value = value.replace(modelsReplacementPattern, modelsPath);
            } else if (name.equals(NerBaseConfigurator.PATH_TO_GAZETTEERS)) {
            	if (resourcesPath != null && resourcesReplacementPattern != null)
            		value = value.replace(resourcesReplacementPattern, resourcesPath);
            } else if (name.equals(NerBaseConfigurator.PATHS_TO_BROWN_CLUSTERS)) {
                
                // trickier since this is a list of paths, split on tabs or
                // spaces
            	if (resourcesPath != null && resourcesReplacementPattern != null) {
	                String [] paths = value.split("[\\t ]");
	                value = "";
	                for (int i = 0; i < paths.length; i++) {
	                    if (i > 0) 
	                        value += "\t";
	                    value += paths[i].replace(resourcesReplacementPattern, resourcesPath);
	                }
            	}
            } else if (name.equals(NerBaseConfigurator.PATH_TO_TOKEN_NORM_DATA)) {
                
                // trickier since this is a list of paths, split on tabs or
                // spaces
            	if (resourcesPath != null && resourcesReplacementPattern != null) {
	                String [] paths = value.split("[\\t ]");
	                value = "";
	                for (int i = 0; i < paths.length; i++) {
	                    if (i > 0) 
	                        value += "\t";
	                    value += paths[i].replace(resourcesReplacementPattern, resourcesPath);
	                }
            	}
            }
            rm.getProperties().setProperty(name, value);
        }
        return rm;
    }

	/** 
     * this is for testing. It also demonstrates the contract just in case
     * you were wondering how this guy works.
     */
    static public void main(String[] args) throws IOException {
        ResourceManager rm = NERResourceManagerFactory.get("reuters.config", "#MODELS_PATH#", 
        		"#RESOURCES_PATH#","/Users/redman/Desktop","/Users/redman/Desktop");
        for (Entry<Object, Object> entry : rm.getProperties().entrySet()) 
            System.out.println(entry.getKey()+":"+entry.getValue());
        System.out.println();
        try {
	        rm = NERResourceManagerFactory.get("reuters.config", null, null, null, null);
	        for (Entry<Object, Object> entry : rm.getProperties().entrySet()) 
	            System.out.println(entry.getKey()+":"+entry.getValue());
	        System.err.println("This configuration file should not have been found, should have thrown a FileNotFoundException.");
        } catch (FileNotFoundException fnfe) {
        	
        }
        
        try {
	        rm = NERResourceManagerFactory.get("/Users/redman/Desktop/reuters.config", null, null, null, null);
	        for (Entry<Object, Object> entry : rm.getProperties().entrySet()) 
	            System.out.println(entry.getKey()+":"+entry.getValue());
        } catch (FileNotFoundException fnfe) {
        	
        }

	}
}
