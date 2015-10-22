package tests;

import java.util.ArrayList;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import edu.illinois.cs.cogcomp.thrift.base.Labeling;
import edu.illinois.cs.cogcomp.thrift.base.Span;
import edu.illinois.cs.cogcomp.thrift.curator.Record;
import edu.illinois.cs.cogcomp.nlp.reader.OntoNotesJsonReader;
import edu.illinois.cs.cogcomp.nlp.tokenizer.IllinoisTokenizerPrev;

public class PrevTokenizerTester {
	OntoNotesJsonReader reader = new OntoNotesJsonReader("json_output.txt");
    ArrayList<String> rawTexts = reader.getRawTexts();
    IllinoisTokenizerPrev illinoisTokenizerPrev;
    Record illinoisRecord;

	@Before
	public void setUp() throws Exception {
		illinoisTokenizerPrev = new IllinoisTokenizerPrev(rawTexts);
		illinoisRecord = illinoisTokenizerPrev.parseIntoCuratorRecord();
	}

	@Test
	public void nullTest() {
		// Check for null values.
		assert(!illinoisTokenizerPrev.equals(null));
		assert(!illinoisRecord.equals(null));
		
		Map<String, Labeling> labelViews = illinoisRecord.getLabelViews();
		
		// Check that we have valid labels.
		assert(labelViews.containsKey("sentences"));
		assert(labelViews.containsKey("tokens"));
		
		Labeling sentenceLabeling = labelViews.get("sentences");
		Labeling tokenLabeling = labelViews.get("token");
		
		assert(!sentenceLabeling.equals(null));
		assert(!tokenLabeling.equals(null));
	}
	
	@Test
	public void labelTest() {
		Map<String, Labeling> labelViews = illinoisRecord.getLabelViews();
		Labeling sentenceLabeling = labelViews.get("sentences");
		Labeling tokenLabeling = labelViews.get("token");
		
		// Check that the number of labels is valid.
		int numSentences = rawTexts.size();
		assert(labelViews.get("sentences").getLabelsSize() == numSentences);
		assert(labelViews.get("tokens").getLabelsSize() >= numSentences);
		
		// Check that offsets are set.
		for (Span s : sentenceLabeling.getLabels()){
			assert(s.getStart() >= 0);
			assert(s.getEnding() >= 0);
		}
	}

}
