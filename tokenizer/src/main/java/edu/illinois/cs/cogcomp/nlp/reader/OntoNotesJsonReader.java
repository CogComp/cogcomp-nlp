package edu.illinois.cs.cogcomp.nlp.reader;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class OntoNotesJsonReader {

    private ArrayList<OntoNotesDataModel> sentences;
    private ArrayList<String> rawTexts;

    public OntoNotesJsonReader(String fileName) {
        JSONParser parser = new JSONParser();

        sentences = new ArrayList<>();

        try {
            Object obj = parser.parse(new FileReader(new File(fileName)));

            JSONObject jsonObject = (JSONObject) obj;

            JSONArray sentencesArray = (JSONArray) jsonObject.get("sentences");

            for (JSONObject sentenceArray : (Iterable<JSONObject>) sentencesArray) {
                OntoNotesDataModel currentDataModel = new OntoNotesDataModel();
                currentDataModel.setPlainSentence((String) sentenceArray.get("sentence_text"));
                currentDataModel.setSentenceStartOffset(Integer.parseInt(sentenceArray.get(
                        "sentence_start_offset").toString()));
                currentDataModel.setSentenceEndOffset(Integer.parseInt(sentenceArray.get(
                        "sentence_end_offset").toString()));
                JSONArray eachSentenceArray = (JSONArray) sentenceArray.get("tokens");
                for (JSONObject eachTokenObject : (Iterable<JSONObject>) eachSentenceArray) {

                    currentDataModel.addAToken((String) eachTokenObject.get("token_text"));
                    currentDataModel.addAStartOffset(Integer.parseInt(eachTokenObject.get(
                            "token_start_offset").toString()));
                    currentDataModel.addAnEndOffset(Integer.parseInt(eachTokenObject.get(
                            "token_end_offset").toString()));
                }
                sentences.add(currentDataModel);
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        rawTexts = new ArrayList<>();
        for (OntoNotesDataModel eachEntry : sentences) {
            rawTexts.add(eachEntry.getPlainSentence());
        }
    }

    public ArrayList<OntoNotesDataModel> getSentences() {
        return sentences;
    }

    public ArrayList<String> getRawTexts() {
        return rawTexts;
    }
}
