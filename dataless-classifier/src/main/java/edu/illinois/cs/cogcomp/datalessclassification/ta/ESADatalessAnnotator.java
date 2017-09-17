/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.datalessclassification.ta;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.NotImplementedException;
import org.json.simple.JSONObject;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.datalessclassification.config.DatalessConfigurator;
import edu.illinois.cs.cogcomp.datalessclassification.config.ESADatalessConfigurator;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.datalessclassification.representation.esa.MemoryBasedESA;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;

/**
 * A wrapper of ESA-based Dataless Classifier for the cogcomp pipeline.
 * 
 * @author shashank
 */

public class ESADatalessAnnotator extends ADatalessAnnotator {
	
	protected static final String Class_Name = ESADatalessAnnotator.class.getCanonicalName();
	public static final String Annotator_Name = ViewNames.ESA_DATALESS;
	
	/**
	 * @return The name of the ESA Dataless Annotator
	 */
	@Override
	public String getName () {
		return Annotator_Name;
	}
	
	public ESADatalessAnnotator () {
		this(
				new ESADatalessConfigurator().getDefaultConfig()
			);
	}
	
	public ESADatalessAnnotator (ResourceManager config) {
		super(config);
		initializeEmbedding(config);
		initializeClassifier(config);
	}
	
	public ESADatalessAnnotator (ResourceManager config, JSONObject treeObj) throws NotImplementedException {
		super(treeObj);
		initializeEmbedding(config);
		initializeClassifier(config);
	}
	
	public ESADatalessAnnotator (ResourceManager config, String hierarchyPath, String labelNameFile, String labelDescFile) {
		super(hierarchyPath, labelNameFile, labelDescFile);
		initializeEmbedding(config);
		initializeClassifier(config);
	}
	
	public ESADatalessAnnotator (ResourceManager config, Set<String> topNodes, Map<String, Set<String>> childMap, 
			Map<String, String> labelNameMap, Map<String, String> labelDescMap) {
		
		super(topNodes, childMap, labelNameMap, labelDescMap);
		initializeEmbedding(config);
		initializeClassifier(config);
	}
	
	protected void initializeEmbedding (ResourceManager config) {
		conceptWeights = new HashMap<>();
		
		embedding_dim = config.getInt(ESADatalessConfigurator.ESA_DIM);
		
		embedding = new MemoryBasedESA(config);
	}
	
	public static CommandLine getCMDOpts (String[] args) {
		Options options = new Options();
		
		Option configOpt = new Option("c", "config", true, "config file path");
		configOpt.setRequired(false);
		options.addOption(configOpt);
		
		Option labelOption = new Option("k", "topK", true, "Max number of labels to select at every level (default: 1)");
		labelOption.setRequired(false);
		options.addOption(labelOption);
		
		Option testFileOption = new Option("f", "testFile", true, "File to annotate using Dataless");
		testFileOption.setRequired(false);
		options.addOption(testFileOption);
		
		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		
		CommandLine cmd = null;
		
		try {
		    cmd = parser.parse(options, args);
		} 
		catch (ParseException e) {
		    System.out.println(e.getMessage());
		    formatter.printHelp("utility-name", options);
		
		    System.exit(1);
		    return cmd;
		}
		
		return cmd;
	}
	
	
	/**
	 * @param args
	 * 		config: config file path
	 * 		topK: Max number of labels to select at every level (default: 1)
	 * 		testFile: Test File
	 * 
	 * @throws IOException 
	 * @throws AnnotatorException 
	 */
	public static void main (String[] args) throws IOException, AnnotatorException {
		CommandLine cmd = getCMDOpts(args);

		ResourceManager rm = null;
		
		try {
			String configFile = cmd.getOptionValue("config", "config/project.properties");
			ResourceManager nonDefaultRm = new ResourceManager(configFile);
			
			rm = new ESADatalessConfigurator().getConfig(nonDefaultRm);
		} 
		catch (Exception e) {
			rm = new ESADatalessConfigurator().getDefaultConfig(); 
		}
		
        int topK = Integer.parseInt(cmd.getOptionValue("topK", "1"));
		String testFile = cmd.getOptionValue("testFile", "data/graphicsTestDocument.txt");
		
		BufferedReader br = new BufferedReader(new FileReader(new File(testFile)));
		
		StringBuilder sb = new StringBuilder();
		
		String line;
		
		while ((line = br.readLine()) != null) {
			sb.append(line);
			sb.append(" ");
		}
		
		br.close();
		
		String text = sb.toString().trim();
		
		TokenizerTextAnnotationBuilder taBuilder = new TokenizerTextAnnotationBuilder(new StatefulTokenizer());
		
        TextAnnotation ta = taBuilder.createTextAnnotation(text);
        
        ESADatalessAnnotator datalessAnnotator = new ESADatalessAnnotator(rm);
        
        datalessAnnotator.labelText(ta, topK);
		
		List<Constituent> annots = ta.getView(ESADatalessAnnotator.Annotator_Name).getConstituents();
		
		System.out.println("Predicted LabelIDs :- ");
		
		for (Constituent annot : annots) {
			System.out.println(annot.getLabel());
		}
		
		Map<String, String> labelNameMap = DatalessAnnotatorUtils.getLabelNameMap(rm.getString(DatalessConfigurator.LabelName_Path.key));
		
		System.out.println("Predicted Labels :- ");
		
		for (Constituent annot : annots) {
			System.out.println(labelNameMap.get(annot.getLabel()));
		}
	}
}
