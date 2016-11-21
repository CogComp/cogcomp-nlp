/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.core.utilities;

import java.util.*;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import edu.illinois.cs.cogcomp.core.algorithms.Sorters;
import edu.illinois.cs.cogcomp.core.datastructures.HasAttributes;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonSerializer extends AbstractSerializer {

    public static final String FORM = "form";
    public static final String STARTCHAROFFSET = "startCharOffset";
    public static final String ENDCHAROFFSET = "endCharOffset";
    public static final String TOKENOFFSETS = "tokenOffsets";
    public static final String LABEL_SCORE_MAP = "labelScoreMap";
    public static final String PROPERTIES = "properties";
    private final Logger logger = LoggerFactory.getLogger(JsonSerializer.class);

    /**
     * Add an array of objects reporting View's Constituents' surface form and character offsets.
     * May make deserialization to TextAnnotation problematic, as the relevant methods deduce token
     * character offsets directly from list of token strings and raw text.
     *
     * @param fieldName name to give to this field
     * @param view view whose character offsets will be serialized
     * @param json Json object to which resulting array will be added
     */
    private static void writeTokenOffsets(String fieldName, View view, JsonObject json) {
        JsonArray offsetArray = new JsonArray();
        for (Constituent c : view.getConstituents()) {
            JsonObject cJ = new JsonObject();
            writeString(FORM, c.getSurfaceForm(), cJ);
            writeInt(STARTCHAROFFSET, c.getStartCharOffset(), cJ);
            writeInt(ENDCHAROFFSET, c.getEndCharOffset(), cJ);
            offsetArray.add(cJ);
        }
        json.add(fieldName, offsetArray);
    }

    private static void writeView(View view, JsonObject json) {
        writeString("viewType", view.getClass().getCanonicalName(), json);

        writeString("viewName", view.getViewName(), json);

        writeString("generator", view.getViewGenerator(), json);

        if (view.getScore() != 0)
            writeDouble("score", view.getScore(), json);

        List<Constituent> constituents = view.getConstituents();

        if (constituents.size() > 0) {
            JsonArray cJson = new JsonArray();
            for (int i = 0; i < view.getNumberOfConstituents(); i++) {

                Constituent constituent = constituents.get(i);
                JsonObject c = new JsonObject();
                writeConstituent(constituent, c);

                cJson.add(c);
            }

            json.add("constituents", cJson);
        }

        List<Relation> relations = view.getRelations();

        if (relations.size() > 0) {

            JsonArray rJson = new JsonArray();

            for (Relation r : relations) {
                Constituent src = r.getSource();
                Constituent tgt = r.getTarget();

                int srcId = constituents.indexOf(src);
                int tgtId = constituents.indexOf(tgt);

                JsonObject rJ = new JsonObject();

                writeString("relationName", r.getRelationName(), rJ);

                if (r.getScore() != 0)
                    writeDouble("score", r.getScore(), rJ);
                writeInt("srcConstituent", srcId, rJ);
                writeInt("targetConstituent", tgtId, rJ);
                writeAttributes(r, rJ);

                Map<String, Double> labelsToScores = r.getLabelsToScores();

                if (null != labelsToScores)
                    writeLabelsToScores(labelsToScores, rJ);

                rJson.add(rJ);
            }

            json.add("relations", rJson);
        }
    }

    private static View readView(JsonObject json, TextAnnotation ta) throws Exception {

        String viewClass = readString("viewType", json);

        String viewName = readString("viewName", json);

        String viewGenerator = readString("generator", json);

        double score = 0;
        if (json.has("score"))
            score = readDouble("score", json);

        View view = createEmptyView(ta, viewClass, viewName, viewGenerator, score);

        List<Constituent> constituents = new ArrayList<>();

        if (json.has("constituents")) {

            JsonArray cJson = json.getAsJsonArray("constituents");

            for (int i = 0; i < cJson.size(); i++) {
                JsonObject cJ = (JsonObject) cJson.get(i);
                Constituent c = readConstituent(cJ, ta, viewName);
                constituents.add(c);
                view.addConstituent(c);
            }
        }

        if (json.has("relations")) {
            JsonArray rJson = json.getAsJsonArray("relations");
            for (int i = 0; i < rJson.size(); i++) {
                JsonObject rJ = (JsonObject) rJson.get(i);

                String name = readString("relationName", rJ);

                double s = 0;
                if (rJ.has("score"))
                    s = readDouble("score", rJ);

                int src = readInt("srcConstituent", rJ);
                int tgt = readInt("targetConstituent", rJ);

                Map<String, Double> labelsToScores = null;
                if (rJ.has(LABEL_SCORE_MAP))
                {
                    labelsToScores = new HashMap<>();
                    readLabelsToScores(labelsToScores, rJ);
                }

                Relation rel = null;

                if (null == labelsToScores)
                    rel = new Relation(name, constituents.get(src), constituents.get(tgt), s);
                else
                    rel = new Relation(labelsToScores, constituents.get(src), constituents.get(tgt));

                readAttributes(rel, rJ);

                view.addRelation(rel);
            }
        }
        return view;
    }

    private static void writeConstituent(Constituent c, JsonObject cJ) {
        writeString("label", c.getLabel(), cJ);

        if (c.getConstituentScore() != 0)
            writeDouble("score", c.getConstituentScore(), cJ);
        writeInt("start", c.getStartSpan(), cJ);
        writeInt("end", c.getEndSpan(), cJ);

        writeAttributes(c, cJ);
        Map<String, Double> labelsToScores = c.getLabelsToScores();

        if ( null != labelsToScores )
            writeLabelsToScores(labelsToScores, cJ);
    }

    private static Constituent readConstituent(JsonObject cJ, TextAnnotation ta, String viewName) {
        String label = readString("label", cJ);
        double score = 0;
        if (cJ.has("score"))
            score = readDouble("score", cJ);
        int start = readInt("start", cJ);
        int end = readInt("end", cJ);

        Map<String, Double> labelsToScores = null;

        if (cJ.has(LABEL_SCORE_MAP)) {
            labelsToScores = new HashMap<>();
            readLabelsToScores(labelsToScores, cJ );
        }

        Constituent c = null;
        if (null == labelsToScores)
            c = new Constituent(label, score, viewName, ta, start, end);
        else
            c = new Constituent(labelsToScores, viewName, ta, start, end);

        readAttributes(c, cJ);

        return c;
    }

    private static void writeSentences(TextAnnotation ta, JsonObject json) {

        JsonObject object = new JsonObject();

        SpanLabelView sentenceView = (SpanLabelView) ta.getView(ViewNames.SENTENCE);
        writeString("generator", sentenceView.getViewGenerator(), object);

        writeDouble("score", sentenceView.getScore(), object);
        int numSentences = sentenceView.getNumberOfConstituents();
        int[] sentenceEndPositions = new int[numSentences];

        int id = 0;
        for (Sentence sentence : ta.sentences()) {
            sentenceEndPositions[id++] = sentence.getEndSpan();
        }
        writeIntArray("sentenceEndPositions", sentenceEndPositions, object);

        json.add("sentences", object);
    }

    private static Pair<Pair<String, Double>, int[]> readSentences(JsonObject json) {
        JsonObject object = json.getAsJsonObject("sentences");

        String generator = readString("generator", object);
        double score = readDouble("score", object);
        int[] endPositions = readIntArray("sentenceEndPositions", object);

        return new Pair<>(new Pair<>(generator, score), endPositions);

    }

    private static void writeIntArray(String name, int[] is, JsonObject object) {

        JsonArray array = new JsonArray();

        for (int i : is) {
            array.add(new JsonPrimitive(i));
        }

        object.add(name, array);
    }

    private static int[] readIntArray(String name, JsonObject object) {

        JsonArray array = object.get(name).getAsJsonArray();
        int[] s = new int[array.size()];

        for (int i = 0; i < array.size(); i++)
            s[i] = array.get(i).getAsInt();

        return s;
    }

    private static void writeStringArray(String name, String[] strings, JsonObject object) {

        JsonArray array = new JsonArray();

        for (String s : strings) {
            array.add(new JsonPrimitive(s));
        }

        object.add(name, array);
    }

    private static String[] readStringArray(String name, JsonObject object) {

        JsonArray array = object.get(name).getAsJsonArray();
        String[] s = new String[array.size()];

        for (int i = 0; i < array.size(); i++)
            s[i] = array.get(i).getAsString();
        return s;
    }

    private static void writeString(String name, String value, JsonObject out) {
        out.add(name, new JsonPrimitive(value));
    }

    private static String readString(String name, JsonObject obj) {
        return obj.getAsJsonPrimitive(name).getAsString();
        // return obj.get(name).getAsString();
    }

    private static void writeInt(String name, int value, JsonObject out) {
        out.add(name, new JsonPrimitive(value));
    }

    private static int readInt(String name, JsonObject obj) {
        return obj.get(name).getAsInt();
    }

    private static void writeDouble(String name, double value, JsonObject out) {
        out.add(name, new JsonPrimitive(value));
    }

    private static double readDouble(String name, JsonObject obj) {
        return obj.get(name).getAsDouble();
    }

    private static void writeAttributes(HasAttributes obj, JsonObject out) {
        if (obj.getAttributeKeys().size() > 0) {
            JsonObject properties = new JsonObject();

            for (String key : Sorters.sortSet(obj.getAttributeKeys())) {
                writeString(key, obj.getAttribute(key), properties);
            }

            out.add("properties", properties);
        }
    }

    private static void readAttributes(HasAttributes obj, JsonObject json) {
        if (json.has("properties")) {
            JsonObject properties = json.getAsJsonObject("properties");

            for (Entry<String, JsonElement> entry : properties.entrySet()) {
                obj.addAttribute(entry.getKey(), entry.getValue().getAsString());
            }
        }
    }

    private static void readLabelsToScores(Map<String, Double> obj, JsonObject json)
    {
        if (json.has(LABEL_SCORE_MAP)) {
            JsonObject map = json.getAsJsonObject(LABEL_SCORE_MAP);
            for(Entry<String, JsonElement> e : map.entrySet()) {
                obj.put( e.getKey(), e.getValue().getAsDouble() );
            }
        }
    }

    private static void writeLabelsToScores(Map<String, Double> obj, JsonObject out)
    {
        JsonObject labelScoreMap = new JsonObject();
        for (String key : Sorters.sortSet(obj.keySet()))
            writeDouble( key, obj.get(key), out);
        out.add(LABEL_SCORE_MAP, labelScoreMap);
    }

    JsonObject writeTextAnnotation(TextAnnotation ta) {
        return writeTextAnnotation(ta, false);
    }

    JsonObject writeTextAnnotation(TextAnnotation ta, boolean doWriteTokenOffsets) {

        // get rid of the views that are empty
        Set<String> viewNames = new HashSet<>(ta.getAvailableViews());
        for (String vu : viewNames) {
            if (ta.getView(vu) == null) {
                logger.warn("View " + vu + " is null");
                ta.removeView(vu);
            }
        }

        JsonObject json = new JsonObject();

        writeString("corpusId", ta.getCorpusId(), json);
        writeString("id", ta.getId(), json);
        writeString("text", ta.getText(), json);
        writeStringArray("tokens", ta.getTokens(), json);
        if (doWriteTokenOffsets)
            writeTokenOffsets(TOKENOFFSETS, ta.getView(ViewNames.TOKENS), json);

        writeSentences(ta, json);

        JsonArray views = new JsonArray();
        for (String viewName : Sorters.sortSet(ta.getAvailableViews())) {
            if (viewName.equals(ViewNames.SENTENCE))
                continue;

            JsonObject view = new JsonObject();

            writeString("viewName", viewName, view);
            views.add(view);

            JsonArray viewData = new JsonArray();
            List<View> topKViews = ta.getTopKViews(viewName);

            for (View topKView : topKViews) {
                JsonObject kView = new JsonObject();
                writeView(topKView, kView);
                viewData.add(kView);
            }

            view.add("viewData", viewData);
        }

        json.add("views", views);

        writeAttributes(ta, json);


        return json;
    }

    TextAnnotation readTextAnnotation(String string) throws Exception {
        JsonObject json = (JsonObject) new JsonParser().parse(string);

        String corpusId = readString("corpusId", json);
        String id = readString("id", json);
        String text = readString("text", json);
        String[] tokens = readStringArray("tokens", json);

        Pair<Pair<String, Double>, int[]> sentences = readSentences(json);

        IntPair[] offsets = TokenUtils.getTokenOffsets(text, tokens);

        TextAnnotation ta =
                new TextAnnotation(corpusId, id, text, offsets, tokens, sentences.getSecond());

        JsonArray views = json.getAsJsonArray("views");
        for (int i = 0; i < views.size(); i++) {
            JsonObject view = (JsonObject) views.get(i);
            String viewName = readString("viewName", view);

            JsonArray viewData = view.getAsJsonArray("viewData");
            List<View> topKViews = new ArrayList<>();

            for (int k = 0; k < viewData.size(); k++) {
                JsonObject kView = (JsonObject) viewData.get(k);

                topKViews.add(readView(kView, ta));
            }

            ta.addTopKView(viewName, topKViews);
        }

        readAttributes(ta, json);

        return ta;
    }
}
