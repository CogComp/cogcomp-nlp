package edu.illinois.cs.cogcomp.nlp.reader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import edu.illinois.cs.cogcomp.nlp.utility.Utility;
import org.json.simple.JSONArray;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.simple.JSONValue;

public class OntoNotesParser {

    private ArrayList<OntoNotesDataModel> sentences;

    public OntoNotesParser(String fileName) {

        BufferedReader bufferedReader = null;
        try {
            boolean plainSentenceFlag = false, leavesFlag = false;

            int plainSentenceCount = 0, leavesCount = 0;

            String currentLine, eachPlainSentence = "";

            OntoNotesDataModel currentDataModel = new OntoNotesDataModel();
            sentences = new ArrayList<OntoNotesDataModel>();

            bufferedReader = new BufferedReader(new FileReader("./data/"+fileName));

            while ((currentLine = bufferedReader.readLine()) != null) {
                if (currentLine.equals("Plain sentence:")) {
                    plainSentenceFlag = true;
                    eachPlainSentence = "";
                }
                else if (currentLine.equals("Treebanked sentence:")) {
                    plainSentenceFlag = false;
                    plainSentenceCount = 0;
                    currentDataModel.setPlainSentence(eachPlainSentence);
                    sentences.add(currentDataModel);
                    //System.out.println(eachPlainSentence+"\n");
                }
                else if (currentLine.equals("Leaves:")) {
                    leavesFlag = true;
                }
                if (plainSentenceFlag) {
                    plainSentenceCount ++;
                    if (plainSentenceCount == 3 && ! currentLine.equals("")) {
                        eachPlainSentence += currentLine.substring(4);
                    }
                    else if (plainSentenceCount > 3 && ! currentLine.equals("")) {
                        eachPlainSentence += currentLine.substring(3);
                    }
                }
                if (leavesFlag) {
                    leavesCount ++;
                    if (leavesCount > 2) {
                        if (! currentLine.equals("")) {
                            if (!currentLine.substring(0,9).equals("         ")) {
                                if (currentLine.charAt(8) != '*') {
                                    currentDataModel.addAToken(currentLine.substring(8));
                                    //System.out.println("[" + currentLine.substring(8) + "]");
                                }
                            }
                        }
                        else {
                            leavesFlag = false;
                            leavesCount = 0;
                            currentDataModel = new OntoNotesDataModel();
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedReader != null)
                    bufferedReader.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        Utility.computeCharacterOffsets(sentences);

        for (OntoNotesDataModel eachEntry : sentences) {
            System.out.println(eachEntry.getPlainSentence()+"\n");
            System.out.println(eachEntry.getTokens().toString()+"\n");
            System.out.println(eachEntry.getStartOffsets().toString()+"\n");
            System.out.println(eachEntry.getEndOffsets().toString()+"\n");
            System.out.println("start: "+eachEntry.getSentenceStartOffset()+", end: "+eachEntry.getSentenceEndOffset()+"\n\n");
        }
    }

    public void writeToFileInJson(String fileName) {
        LinkedHashMap jsonObject = new LinkedHashMap();
        JSONArray sentencesArray = new JSONArray();


        for (OntoNotesDataModel eachEntry : sentences) {
            LinkedHashMap eachJsonObject = new LinkedHashMap();
            eachJsonObject.put("sentence_text", eachEntry.getPlainSentence());
            eachJsonObject.put("sentence_start_offset", eachEntry.getSentenceStartOffset());
            eachJsonObject.put("sentence_end_offset", eachEntry.getSentenceEndOffset());

            JSONArray eachTokensJsonArray = new JSONArray();
            for (int i = 0; i < eachEntry.getTokens().size(); i++) {
                LinkedHashMap eachTokenObject = new LinkedHashMap();
                eachTokenObject.put("token_text", eachEntry.getTokens().get(i));
                eachTokenObject.put("token_start_offset", eachEntry.getStartOffsets().get(i));
                eachTokenObject.put("token_end_offset", eachEntry.getEndOffsets().get(i));
                eachTokensJsonArray.add(eachTokenObject);
            }
            eachJsonObject.put("tokens", eachTokensJsonArray);
            sentencesArray.add(eachJsonObject);
        }

        jsonObject.put("sentences", sentencesArray);

        try {
            FileWriter file = new FileWriter("./data/"+fileName);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonParser jp = new JsonParser();
            JsonElement je = jp.parse(JSONValue.toJSONString(jsonObject));
            file.write(gson.toJson(je));
            file.flush();
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
