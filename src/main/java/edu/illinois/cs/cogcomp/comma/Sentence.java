package edu.illinois.cs.cogcomp.comma;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.illinois.cs.cogcomp.comma.sl.CommaIOManager;
import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.FeatureVectorBuffer;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;

/**
 * Used to group all the commas in a sentence. The commas can then be accessed based on relations to each other based on their position within the sentence
 * @author navari
 *
 */
public class Sentence implements Serializable{
	Collection<Comma> commas;
	private static final long serialVersionUID = 2522617554768671153l;
	public Sentence(){
		commas = new ArrayList<Comma>();
	}
	
	public void addComma(Comma c){
		commas.add(c);
	}
	
	public Collection<Comma> getCommas(){
		return commas;
	}
	
	public Collection<Comma> getFirstCommasWhichAreNotLast(){
		Collection<Comma> firstCommasWhichAreNotLast = new ArrayList<Comma>();
		for (Comma c : commas)
			if(getNextComma(c)!=null && getPreviousComma(c)==null)
				firstCommasWhichAreNotLast.add(c);
		return firstCommasWhichAreNotLast;
	}
	
	public Collection<Comma> getMiddleCommas(){
		Collection<Comma> middleCommas = new ArrayList<Comma>();
		for (Comma c : commas)
			if(getNextComma(c)!=null && getPreviousComma(c)!=null)
				middleCommas.add(c);
		return middleCommas;
	}
	
	public Comma getNextComma(Comma curr_c){
		Comma next_c = null;
		int curr_diff = Integer.MAX_VALUE;
		for(Comma c: commas){
			if(c.commaPosition > curr_c.commaPosition && (c.commaPosition - curr_c.commaPosition < curr_diff)){
				next_c = c;
				curr_diff = next_c.commaPosition - curr_c.commaPosition;
			}
		}
		return next_c;
	}
	
	public Comma getPreviousComma(Comma curr_c){
		Comma prev_c = null;
		int curr_diff = Integer.MAX_VALUE;
		for(Comma c: commas){
			if(c.commaPosition < curr_c.commaPosition && (curr_c.commaPosition - c.commaPosition < curr_diff)){
				prev_c = c;
				curr_diff = curr_c.commaPosition - prev_c.commaPosition;
			}
		}
		return prev_c;
	}
	
	public Collection<Comma> getFirstSiblingCommasWhichAreNotLast(){
		Collection<Comma> firstCommasWhichAreNotLast = new ArrayList<Comma>();
		for (Comma c : commas)
			if(getNextSiblingComma(c)!=null && getPreviousSiblingComma(c)==null)
				firstCommasWhichAreNotLast.add(c);

		return firstCommasWhichAreNotLast;
	}
	
	public Collection<Comma> getMiddleSiblingCommas(){
		Collection<Comma> middleCommas = new ArrayList<Comma>();
		for (Comma c : commas)
			if(getNextSiblingComma(c)!=null && getPreviousSiblingComma(c)!=null)
				middleCommas.add(c);
		return middleCommas;
	}
	
	public Comma getNextSiblingComma(Comma curr_c){
		Comma next_c = null;
		int curr_diff = Integer.MAX_VALUE;
		for(Comma c: curr_c.getSiblingCommas()){
			if(c.commaPosition > curr_c.commaPosition && (c.commaPosition - curr_c.commaPosition < curr_diff)){
				next_c = c;
				curr_diff = next_c.commaPosition - curr_c.commaPosition;
			}
		}
		return next_c;
	}
	
	public Comma getPreviousSiblingComma(Comma curr_c){
		Comma prev_c = null;
		int curr_diff = Integer.MAX_VALUE;
		for(Comma c: curr_c.getSiblingCommas()){
			if(c.commaPosition < curr_c.commaPosition && (curr_c.commaPosition - c.commaPosition < curr_diff)){
				prev_c = c;
				curr_diff = curr_c.commaPosition - prev_c.commaPosition;
			}
		}
		return prev_c;
	}
	
	

	/**
	 * 
	 * @return String representation of the sentence with all the commas embedded into the string
	 */
	public String getAnnotatedText(){
		String text = commas.iterator().next().getText();
		String[] tokens = text.split("\\s+");
		List<Comma> sortedCommas = new ArrayList<Comma>(commas);
		Collections.sort(sortedCommas, new Comparator<Comma>(){
			@Override
			public int compare(Comma o1, Comma o2) {
				return o1.getPosition() - o2.getPosition();
			}
		});
		int commaIdx = 0;
		String annotatedText = tokens[0];
		for (int tokenIdx = 1; tokenIdx < tokens.length; tokenIdx++) {
			annotatedText += " " + tokens[tokenIdx];
			if(commaIdx < sortedCommas.size() && sortedCommas.get(commaIdx).commaPosition == tokenIdx){
				annotatedText += "[" + sortedCommas.get(commaIdx).getVivekNaveenRole() + "]";
				commaIdx++;
			}
		}
		return annotatedText;
	}
	
	public String getId(){
		return commas.iterator().next().getTextAnnotation(true).getId();
	}
	
	public static class CommaSequence implements IInstance{
		public final List<Comma> sortedCommas;
		public final IFeatureVector baseFeatures[];
		public CommaSequence(List<Comma> commas, Lexiconer lexicon, List<Classifier> lbjExtractors){
			Collections.sort(commas, new Comparator<Comma>(){
				@Override
				public int compare(Comma o1, Comma o2) {
					return o1.getPosition() - o2.getPosition();
				}
			});
			this.sortedCommas = commas;
			
			baseFeatures = new IFeatureVector[sortedCommas.size()];
			for(int i=0; i<sortedCommas.size(); i++){
				FeatureVector lbjFeatureVector = new FeatureVector();
				for(Classifier lbjExtractor : lbjExtractors)
					lbjFeatureVector.addFeatures(lbjExtractor.classify(sortedCommas.get(i)));
				FeatureVectorBuffer slFeatureVectorBuffer = new FeatureVectorBuffer();
				for(int j=0; j<lbjFeatureVector.featuresSize(); j++){
					String featureString = lbjFeatureVector.getFeature(j).toString();
					if(lexicon.isAllowNewFeatures())
						lexicon.addFeature(featureString);
					if(lexicon.containFeature(featureString))
						slFeatureVectorBuffer.addFeature(lexicon.getFeatureId(featureString), 1);
					else 
						slFeatureVectorBuffer.addFeature(lexicon.getFeatureId(CommaIOManager.unknownFeature), 1);
				}
				baseFeatures[i] = slFeatureVectorBuffer.toFeatureVector();
			}
		}
	}
	
	public static class CommaLabelSequence implements IStructure{
		public final List<String> labels;
		public final int[] labelIds;
		
		/**
		 * This constructor is used while constructing and SLProblem
		 * @param commaSequence
		 * @param lexicon
		 */
		public CommaLabelSequence(CommaSequence commaSequence, Lexiconer lexicon, Classifier lbjLabeler){
			labels = new ArrayList<String>();
			for(Comma comma : commaSequence.sortedCommas)
				labels.add(lbjLabeler.discreteValue(comma));
			
			labelIds = new int[labels.size()];
			for(int i=0; i<labels.size(); i++){
				String label = labels.get(i);
				if(lexicon.isAllowNewFeatures()){
					lexicon.addLabel(label);
				}
				labelIds[i] = lexicon.getLabelId(labels.get(i));
			}
		}
		
		/**
		 * This constructor is used by the inference solver which is only uses label-ids
		 * @param labelIds the label-ids corresponding to the labels in the lexicon such that the labels
		 * @param lexicon THe lexicon used to obtain the label-ids
		 */
		public CommaLabelSequence(int[] labelIds, Lexiconer lexicon){
			this.labelIds = labelIds;
			
			labels = new ArrayList<String>();
			for(int i = 0; i<labelIds.length; i++){
				labels.add(lexicon.getLabelString(labelIds[i]));
			}
		}
		
		@Override
		public boolean equals(Object aThat) {
			
			// check for self-comparison
			if (this == aThat)
				return true;

			if (!(aThat instanceof CommaLabelSequence))
				return false;		

			// cast to native object is now safe
			CommaLabelSequence that = (CommaLabelSequence) aThat;

			//check if their tags are the same
			return this.labels.equals(that.labels) && Arrays.equals(labelIds, that.labelIds);
		}
		
		@Override
		public int hashCode(){
			return labels.hashCode()*37 + labelIds.hashCode();
		}
	}
}


