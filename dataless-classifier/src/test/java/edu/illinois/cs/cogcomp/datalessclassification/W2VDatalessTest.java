package edu.illinois.cs.cogcomp.datalessclassification;

import org.junit.Test;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.datalessclassification.config.W2VDatalessConfigurator;
import edu.illinois.cs.cogcomp.datalessclassification.ta.ADatalessAnnotator;
import edu.illinois.cs.cogcomp.datalessclassification.ta.W2VDatalessAnnotator;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * @author shashank
 */
public class W2VDatalessTest {

    @Test
    public void test () throws AnnotatorException, IOException {
    	String configFile = "config/project.properties";
    	ResourceManager nonDefaultRm = new ResourceManager(configFile);
		
		ResourceManager rm = new W2VDatalessConfigurator().getConfig(nonDefaultRm);
    	
        W2VDatalessAnnotator dataless = new W2VDatalessAnnotator(rm);

        String doc1_path = "data/graphicsTestDocument.txt";
        String doc2_path = "data/electronicsTestDocument.txt";

        Set<String> doc1_labels = new HashSet<>(Arrays.asList("computer", "comp.os.ms.windows.misc"));
        Set<String> doc2_labels = new HashSet<>(Arrays.asList("computer", "comp.windows.x"));
        
        String doc1_text = getText(doc1_path);
        String doc2_text = getText(doc2_path);

        Set<String> doc1_predictions = getPredictions(getTextAnnotation(doc1_text), dataless);
        Set<String> doc2_predictions = getPredictions(getTextAnnotation(doc2_text), dataless);
        
        System.out.println("Doc1: Gold LabelIDs:");
        
        for (String goldLabel : doc1_labels) {
        	System.out.println(goldLabel);
        }
        
        System.out.println();
        System.out.println("Doc1: Predicted LabelIDs:");
        
        for (String predictedLabel : doc1_predictions) {
        	System.out.println(predictedLabel);
        }
        
        System.out.println();
        System.out.println("Doc2: Gold LabelIDs:");
        
        for (String goldLabel : doc2_labels) {
        	System.out.println(goldLabel);
        }
        
        System.out.println();
        System.out.println("Doc2: Predicted LabelIDs:");
        
        for (String predictedLabel : doc2_predictions) {
        	System.out.println(predictedLabel);
        }
        
        assertTrue(isEqual(doc1_labels, doc1_predictions));
        assertTrue(isEqual(doc2_labels, doc2_predictions));
    }
    
    private static boolean isEqual (Set<String> goldLabels, Set<String> predictedLabels) {
    	if (goldLabels.size() != predictedLabels.size())
    		return false;
    	
    	for (String goldLabel : goldLabels) {
    		if (predictedLabels.contains(goldLabel) == false)
    			return false;
    	}
    	
    	return true;
    }

    private static String getText (String testFile) throws IOException {
    	BufferedReader br = new BufferedReader(new FileReader(new File(testFile)));
		
		StringBuilder sb = new StringBuilder();
		
		String line;
		
		while ((line = br.readLine()) != null) {
			sb.append(line);
			sb.append(" ");
		}
		
		br.close();
		
		String text = sb.toString().trim();
		
		return text;
    }
    
    private static TextAnnotation getTextAnnotation (String text) {
    	TokenizerTextAnnotationBuilder taBuilder = new TokenizerTextAnnotationBuilder(new StatefulTokenizer());
        TextAnnotation ta = taBuilder.createTextAnnotation(text);
        
        return ta;
    }
    
    private static Set<String> getPredictions (TextAnnotation ta, ADatalessAnnotator annotator) throws AnnotatorException {
    	annotator.labelText(ta, 1);
		
		List<Constituent> annots = ta.getView(annotator.getName()).getConstituents();
		
		Set<String> predictedLabels = new HashSet<>();
		
		for (Constituent annot : annots) {
			String label = annot.getLabel();
			predictedLabels.add(label);
		}
		
		return predictedLabels;
    }
}
