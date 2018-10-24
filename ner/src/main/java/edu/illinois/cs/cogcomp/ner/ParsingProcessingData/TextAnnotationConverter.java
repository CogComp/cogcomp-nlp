package edu.illinois.cs.cogcomp.ner.ParsingProcessingData;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.lbjava.nlp.Word;
import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;
import edu.illinois.cs.cogcomp.ner.LbjTagger.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TextAnnotationConverter {

    private static Logger logger = LoggerFactory.getLogger(TextAnnotationConverter.class);

    /**
     * NER Code uses the Data object to run. This converts TextAnnotations into a Data object.
     * Important: this creates data with BIO labeling.
     *
     * @param tas list of text annotations
     */
    public static Data loaddataFromTAs(List<TextAnnotation> tas, ParametersForLbjCode cp) throws Exception {

        Data data = new Data();
        for(TextAnnotation ta : tas) {
            NERDocument doc = getNerDocument(ta, cp);

            data.documents.add(doc);
        }

        return data;
    }

    /**
     * Convert a single TextAnnotation into an NERDocument, for use in a Data object.
     * @param ta a text annotation
     * @return NERDocument
     */
    public static NERDocument getNerDocument(TextAnnotation ta, ParametersForLbjCode cp) {
        // convert this data structure into one the NER package can deal with.
        ArrayList<LinkedVector> sentences = new ArrayList<>();
        String[] tokens = ta.getTokens();

        View ner;
        if(ta.hasView(ViewNames.NER_CONLL)) {
            ner = ta.getView(ViewNames.NER_CONLL);
        }else if(ta.hasView(ViewNames.NER_ONTONOTES)){
            ner = ta.getView(ViewNames.NER_ONTONOTES);
        }else{
            ner = new View(ViewNames.NER_CONLL, "Ltf2TextAnnotation",ta,1.0);
            ta.addView(ViewNames.NER_CONLL, ner);
        }

        int[] tokenindices = new int[tokens.length];
        int tokenIndex = 0;
        int neWordIndex = 0;
        for (int i = 0; i < ta.getNumberOfSentences(); i++) {
            Sentence sentence = ta.getSentence(i);
            int sentstart = sentence.getStartSpan();

            LinkedVector words = new LinkedVector();

            for(int k = 0; k < sentence.size(); k++){
                int tokenid = sentstart+k;

                String w = sentence.getToken(k);

                List<Constituent> cons = ner.getConstituentsCoveringToken(tokenid);
                if(cons.size() > 1){
                    logger.error("Doc: " + ta.getId() + ", Too many constituents for token " + tokenid + ", choosing just the first.");
                }

                String tag = "O";

                if(cons.size() > 0) {
                    Constituent c = cons.get(0);
                    if(tokenid == c.getSpan().getFirst())
                        tag = "B-" + c.getLabel();
                    else
                        tag = "I-" + c.getLabel();
                }

                if (w.length() > 0) {
                    NEWord.addTokenToSentence(words, w, tag, cp);

                    //NEWord word = new NEWord(new Word(w),null,tag);

                    //NEWord.addTokenToSentence(words, word);


                    tokenindices[neWordIndex] = tokenIndex;
                    neWordIndex++;
                } else {
                    logger.error("Bad (zero length) token.");
                }
                tokenIndex++;
            }
            if (words.size() > 0)
                sentences.add(words);
        }
        return new NERDocument(sentences, ta.getId());
    }

    /**
     * Assume data is annotated at this point. This will add an NER view to the TAs.
     * @param data
     * @param tas
     */
    public static void Data2TextAnnotation(Data data, List<TextAnnotation> tas) {

        HashMap<String, TextAnnotation> id2ta = new HashMap<>();
        for(TextAnnotation ta : tas){
            id2ta.put(ta.getId(), ta);
        }

        for(NERDocument doc : data.documents) {
            String docid = doc.docname;

            TextAnnotation ta = id2ta.get(docid);
            ArrayList<LinkedVector> nerSentences = doc.sentences;
            SpanLabelView nerView = new SpanLabelView(ViewNames.NER_CONLL, ta);

            // each LinkedVector in data corresponds to a sentence.
            int tokenoffset = 0;
            for (LinkedVector sentence : nerSentences) {
                boolean open = false;

                // there should be a 1:1 mapping btw sentence tokens in record and words/predictions
                // from NER.
                int startIndex = -1;
                String label = null;
                for (int j = 0; j < sentence.size(); j++, tokenoffset++) {
                    NEWord neWord = (NEWord) (sentence.get(j));
                    String prediction = neWord.neTypeLevel2;

                    // LAM-tlr this is not a great way to ascertain the entity type, it's a bit
                    // convoluted, and very
                    // inefficient, use enums, or nominalized indexes for this sort of thing.
                    if (prediction.startsWith("B-")) {
                        startIndex = tokenoffset;
                        label = prediction.substring(2);
                        open = true;
                    } else if (j > 0) {
                        String previous_prediction = ((NEWord) sentence.get(j - 1)).neTypeLevel2;
                        if (prediction.startsWith("I-")
                                && (!previous_prediction.endsWith(prediction.substring(2)))) {
                            startIndex = tokenoffset;
                            label = prediction.substring(2);
                            open = true;
                        }
                    }

                    if (open) {
                        boolean close = false;
                        if (j == sentence.size() - 1) {
                            close = true;
                        } else {
                            String next_prediction = ((NEWord) sentence.get(j + 1)).neTypeLevel2;
                            if (next_prediction.startsWith("B-"))
                                close = true;
                            if (next_prediction.equals("O"))
                                close = true;
                            if (next_prediction.indexOf('-') > -1
                                    && (!prediction.endsWith(next_prediction.substring(2))))
                                close = true;
                        }
                        if (close) {
                            nerView.addSpanLabel(startIndex, tokenoffset+1, label, 1d);
                            open = false;
                        }
                    }
                }
            }
            ta.addView(ViewNames.NER_CONLL, nerView);
        }
    }
}
