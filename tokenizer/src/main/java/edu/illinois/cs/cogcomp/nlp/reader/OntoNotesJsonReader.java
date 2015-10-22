package edu.illinois.cs.cogcomp.nlp.reader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import edu.illinois.cs.cogcomp.thrift.curator.Record;
import edu.illinois.cs.cogcomp.nlp.utility.Utility;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class OntoNotesJsonReader {

    private ArrayList<OntoNotesDataModel> sentences;
    private ArrayList<String> rawTexts;

    public OntoNotesJsonReader(String fileName) {
        JSONParser parser = new JSONParser();

        sentences = new ArrayList<OntoNotesDataModel>();

        try {
            Object obj = parser.parse(new FileReader("./data/"+fileName));

            JSONObject jsonObject = (JSONObject) obj;

            JSONArray sentencesArray = (JSONArray) jsonObject.get("sentences");
            //System.out.println(sentencesArray.toString());

            Iterator<JSONObject> iteratorSentencesArray = sentencesArray.iterator();

            while (iteratorSentencesArray.hasNext()) {
                OntoNotesDataModel currentDataModel = new OntoNotesDataModel();
                JSONObject currentObject = iteratorSentencesArray.next();
                currentDataModel.setPlainSentence((String)currentObject.get("sentence_text"));
                currentDataModel.setSentenceStartOffset(Integer.parseInt(currentObject.get("sentence_start_offset").toString()));
                currentDataModel.setSentenceEndOffset(Integer.parseInt(currentObject.get("sentence_end_offset").toString()));
                //System.out.println(iterator.next().get("plain sentence")+"\n");
                JSONArray eachSentenceArray = (JSONArray) currentObject.get("tokens");
                Iterator<JSONObject> iteratorEachSentenceArray = eachSentenceArray.iterator();
                while (iteratorEachSentenceArray.hasNext()) {
                    //System.out.println(iteratorEachSentenceTokensArray.next());
                    //currentDataModel.addAToken(iteratorEachSentenceArray.next());
                    JSONObject eachTokenObject = iteratorEachSentenceArray.next();
                    currentDataModel.addAToken((String) eachTokenObject.get("token_text"));
                    currentDataModel.addAStartOffset(Integer.parseInt(eachTokenObject.get("token_start_offset").toString()));
                    currentDataModel.addAnEndOffset(Integer.parseInt(eachTokenObject.get("token_end_offset").toString()));
                }
                sentences.add(currentDataModel);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        rawTexts = new ArrayList<String>();
        for (OntoNotesDataModel eachEntry : sentences) {
            rawTexts.add(eachEntry.getPlainSentence());
        }

//        for (OntoNotesDataModel eachEntry : sentences) {
//            System.out.println(eachEntry.getPlainSentence()+"\n");
//            System.out.println(eachEntry.getTokens().toString()+"\n");
//            System.out.println(eachEntry.getStartOffsets().toString()+"\n");
//            System.out.println(eachEntry.getEndOffsets().toString()+"\n");
//            System.out.println("start: "+eachEntry.getSentenceStartOffset()+", end: "+eachEntry.getSentenceEndOffset()+"\n\n");
//        }
    }

    public Record parseIntoCuratorRecord() {
        return Utility.parseIntoCuratorRecord(sentences);
    }

    public ArrayList<String> getRawTexts() {
        return rawTexts;
    }
}
