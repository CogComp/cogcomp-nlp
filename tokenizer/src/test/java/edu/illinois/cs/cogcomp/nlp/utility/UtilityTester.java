package edu.illinois.cs.cogcomp.nlp.utility;

import edu.illinois.cs.cogcomp.lbjava.nlp.Sentence;
import edu.illinois.cs.cogcomp.lbjava.nlp.Word;
import edu.illinois.cs.cogcomp.nlp.reader.OntoNotesDataModel;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class UtilityTester {
    private ArrayList<OntoNotesDataModel> sentences;
    String[] testSentences = {"Mary had a little lamb.", "Its fleece was white as snow."};

    @Before
    public void setUp() throws Exception {
        // Initialize sentences with test data.
        sentences = new ArrayList<>();

        for (String sentence : testSentences) {
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
    public void characterOffsetTest() {
        int startOffset, endOffset;

        // Check that offset are not set.
        for (OntoNotesDataModel sentence : sentences) {
            startOffset = sentence.getSentenceStartOffset();
            endOffset = sentence.getSentenceEndOffset();

            assert (startOffset == 0);
            assert (endOffset == 0);
        }

        Utility.computeCharacterOffsets(sentences);

        // Check that offsets are set.
        for (int i = 0; i < sentences.size(); i++) {
            startOffset = sentences.get(i).getSentenceStartOffset();
            endOffset = sentences.get(i).getSentenceEndOffset();
            if (i == 0) {
                assert (startOffset == 0);
            } else {
                assert (startOffset != 0);
            }

            assert (endOffset != 0);
        }
    }
}
