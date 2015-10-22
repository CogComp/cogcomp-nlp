package tests;

import java.util.ArrayList;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import edu.illinois.cs.cogcomp.lbjava.nlp.Sentence;
import edu.illinois.cs.cogcomp.lbjava.nlp.Word;
import edu.illinois.cs.cogcomp.thrift.base.Labeling;
import edu.illinois.cs.cogcomp.thrift.curator.Record;
import edu.illinois.cs.cogcomp.nlp.reader.OntoNotesDataModel;
import edu.illinois.cs.cogcomp.nlp.utility.Utility;

public class UtilityTester {
	private ArrayList<OntoNotesDataModel> sentences;
	String[] testSentences = {"Mary had a little lamb.", "Its fleece was white as snow."};

	@Before
	public void setUp() throws Exception {
		// Initialize sentences with test data.
		sentences = new ArrayList<OntoNotesDataModel>();
		
		for (String sentence : testSentences){
			OntoNotesDataModel model = new OntoNotesDataModel();
			model.setPlainSentence(sentence);
			Sentence eachSentence = new Sentence(sentence);

            for (int i = 0; i < eachSentence.wordSplit().size(); i++) {
                Word eachToken = (Word) eachSentence.wordSplit().get(i);
                model.addAToken(eachToken.form);
            }
			
	        sentences.add(model);
		}
	}
	
	@Test
	public void parseRecordTest() {
		Record record = Utility.parseIntoCuratorRecord(sentences);
		
		assert(!record.equals(null));
		
		Map<String, Labeling> labelViews = record.getLabelViews();
		
		assert(labelViews.containsKey("sentences"));
		assert(labelViews.containsKey("tokens"));
		
		Labeling sentenceLabeling = labelViews.get("sentences");
		Labeling tokenLabeling = labelViews.get("token");
		
		assert(!sentenceLabeling.equals(null));
		assert(!tokenLabeling.equals(null));
	}

	@Test
	public void characterOffsetTest() {
		int startOffset, endOffset;
		
		// Check that offset are not set.
		for (int i = 0; i < sentences.size(); i++) {
			startOffset = sentences.get(i).getSentenceStartOffset();
			endOffset = sentences.get(i).getSentenceEndOffset();
			
			assert(startOffset == 0);
			assert(endOffset == 0);
		}
		
		Utility.computeCharacterOffsets(sentences);
		
		// Check that offsets are set.
		for (int i = 0; i < sentences.size(); i++) {
			startOffset = sentences.get(i).getSentenceStartOffset();
			endOffset = sentences.get(i).getSentenceEndOffset();
			if (i == 0){
				assert(startOffset == 0);
			} else {
				assert(startOffset != 0);
			}
			
			assert(endOffset != 0);
		}
	}
}
