/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.utilities.protobuf;

import edu.illinois.cs.cogcomp.core.algorithms.Sorters;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.utilities.AbstractSerializer;
import edu.illinois.cs.cogcomp.core.utilities.TokenUtils;
import edu.illinois.cs.cogcomp.core.utilities.protobuf.TextAnnotationImpl.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * Class implementing methods to read/write TextAnnotation instance to/from Google's Protocol Buffers format.
 */
public class ProtobufSerializer extends AbstractSerializer {
    private static final Logger logger = LoggerFactory.getLogger(ProtobufSerializer.class);

    /** Helper methods to handle Protobuf implementation */

    private static void writeTokenOffsets(TextAnnotation ta, TextAnnotationProto.Builder taBuilder) {
        for (Constituent c : ta.getView(ViewNames.TOKENS).getConstituents()) {
            TokenOffsetsProto.Builder tokenOffsetBuilder = TokenOffsetsProto.newBuilder();

            tokenOffsetBuilder.setForm(c.getSurfaceForm());
            tokenOffsetBuilder.setStartCharOffset(c.getStartCharOffset());
            tokenOffsetBuilder.setEndCharOffset(c.getEndCharOffset());

            taBuilder.addTokenOffsets(tokenOffsetBuilder);
        }
    }

    private static void writeSentences(TextAnnotation ta, TextAnnotationProto.Builder taBuilder) {
        SentenceProto.Builder sentenceBuilder = SentenceProto.newBuilder();
        SpanLabelView sentenceView = (SpanLabelView) ta.getView(ViewNames.SENTENCE);

        sentenceBuilder.setGenerator(sentenceView.getViewGenerator());
        sentenceBuilder.setScore(sentenceView.getScore());
        for (Sentence sentence: ta.sentences()) {
            sentenceBuilder.addSentenceEndPositions(sentence.getEndSpan());
        }

        taBuilder.setSentences(sentenceBuilder);
    }

    private static Pair<Pair<String, Double>, int[]> readSentences(SentenceProto sentenceProto) {
        String generator = sentenceProto.getGenerator();
        double score = sentenceProto.getScore();
        int[] endPositions = sentenceProto.getSentenceEndPositionsList().stream().mapToInt(i -> i).toArray();

        return new Pair<>(new Pair<>(generator, score), endPositions);
    }

    private static void writeConstituent(Constituent constituent, ViewDataProto.Builder viewDataBuilder) {
        ConstituentProto.Builder constituentBuilder = ConstituentProto.newBuilder();

        constituentBuilder.setLabel(constituent.getLabel());
        constituentBuilder.setScore(constituent.getConstituentScore());
        constituentBuilder.setStart(constituent.getStartSpan());
        constituentBuilder.setEnd(constituent.getEndSpan());

        if (constituent.getAttributeKeys().size() > 0) {
            for (String key : Sorters.sortSet(constituent.getAttributeKeys())) {
                constituentBuilder.putProperties(key, constituent.getAttribute(key));
            }
        }

        Map<String, Double> labelsToScores = constituent.getLabelsToScores();
        if (labelsToScores != null) {
            constituentBuilder.putAllLabelScoreMap(labelsToScores);
        }

        viewDataBuilder.addConstituents(constituentBuilder);
    }

    private static Constituent readConstituent(ConstituentProto consProto, TextAnnotation ta, String viewName) {
        String label = consProto.getLabel();
        double score = consProto.getScore();
        int start = consProto.getStart();
        int end = consProto.getEnd();

        Map<String, Double> labelsToScores = consProto.getLabelScoreMapMap();
        if (labelsToScores.isEmpty()) {
            labelsToScores = null;
        }

        Constituent constituent;
        if (null == labelsToScores)
            constituent = new Constituent(label, score, viewName, ta, start, end);
        else
            constituent = new Constituent(labelsToScores, viewName, ta, start, end);

        for (Map.Entry<String, String> entry : consProto.getPropertiesMap().entrySet()) {
            constituent.addAttribute(entry.getKey(), entry.getValue());
        }

        return constituent;
    }

    private static void writeViewData(View view, ViewProto.Builder viewBuilder) {
        ViewDataProto.Builder viewDataBuilder = ViewDataProto.newBuilder();

        viewDataBuilder.setViewType(view.getClass().getCanonicalName());
        viewDataBuilder.setViewName(view.getViewName());
        viewDataBuilder.setGenerator(view.getViewGenerator());
        viewDataBuilder.setScore(view.getScore());

        for (Constituent constituent : view.getConstituents()) {
            writeConstituent(constituent, viewDataBuilder);
        }

        List<Constituent> constituents = view.getConstituents();
        for (Relation relation: view.getRelations()) {
            RelationProto.Builder relationBuilder = RelationProto.newBuilder();

            Constituent src = relation.getSource();
            Constituent tgt = relation.getTarget();

            int srcId = constituents.indexOf(src);
            int tgtId = constituents.indexOf(tgt);

            relationBuilder.setRelationName(relation.getRelationName());
            relationBuilder.setScore(relation.getScore());
            relationBuilder.setSrcConstituent(srcId);
            relationBuilder.setTargetConstituent(tgtId);

            if (!relation.getAttributeKeys().isEmpty()) {
                for (String key: Sorters.sortSet(relation.getAttributeKeys())) {
                    relationBuilder.putProperties(key, relation.getAttribute(key));
                }
            }

            Map<String, Double> labelsToScores = relation.getLabelsToScores();
            if (labelsToScores != null) {
                relationBuilder.putAllLabelScoreMap(labelsToScores);
            }

            viewDataBuilder.addRelations(relationBuilder);
        }

        viewBuilder.addViewData(viewDataBuilder);
    }

    private static View readViewData(ViewDataProto viewData, TextAnnotation ta) throws Exception {
        String viewClass = viewData.getViewType();
        String viewName = viewData.getViewName();
        String viewGenerator = viewData.getGenerator();
        double score = viewData.getScore();

        View view = createEmptyView(ta, viewClass, viewName, viewGenerator, score);

        List<Constituent> constituents = new ArrayList<>();
        for (ConstituentProto consProto : viewData.getConstituentsList()) {
            Constituent cons = readConstituent(consProto, ta, viewName);
            constituents.add(cons);

            if(viewName.contains("PARSE"))
                view.addConstituent(cons, true);
            else
                view.addConstituent(cons);
        }

        for (RelationProto relationProto : viewData.getRelationsList()) {
            String name = relationProto.getRelationName();
            double relScore = relationProto.getScore();
            int src = relationProto.getSrcConstituent();
            int tgt = relationProto.getTargetConstituent();

            Map<String, Double> labelsToScores = relationProto.getLabelScoreMapMap();
            if (labelsToScores.isEmpty()) {
                labelsToScores = null;
            }

            Relation relation;
            if (null == labelsToScores)
                relation = new Relation(name, constituents.get(src), constituents.get(tgt), relScore);
            else
                relation = new Relation(labelsToScores, constituents.get(src), constituents.get(tgt));

            for (Map.Entry<String, String> entry : relationProto.getPropertiesMap().entrySet()) {
                relation.addAttribute(entry.getKey(), entry.getValue());
            }

            view.addRelation(relation);
        }

        return view;
    }

    public static TextAnnotationProto writeTextAnnotation(TextAnnotation ta) {
        TextAnnotationProto.Builder taBuilder = TextAnnotationProto.newBuilder();

        // get rid of the views that are empty
        Set<String> viewNames = new HashSet<>(ta.getAvailableViews());
        for (String vu : viewNames) {
            if (ta.getView(vu) == null) {
                logger.warn("View " + vu + " is null");
                ta.removeView(vu);
            }
        }

        taBuilder.setCorpusId(ta.getCorpusId());
        taBuilder.setId(ta.getId());
        taBuilder.setText(ta.getText());
        taBuilder.addAllTokens(Arrays.asList(ta.getTokens()));

        writeTokenOffsets(ta, taBuilder);

        writeSentences(ta, taBuilder);

        for (String viewName : Sorters.sortSet(ta.getAvailableViews())) {

            ViewProto.Builder viewBuilder = ViewProto.newBuilder();

            viewBuilder.setViewName(viewName);

            List<View> topKViews = ta.getTopKViews(viewName);
            for (View topKView : topKViews) {
                writeViewData(topKView, viewBuilder);
            }

            taBuilder.addViews(viewBuilder);
        }

        if (ta.getAttributeKeys().size() > 0) {
            for (String key: Sorters.sortSet(ta.getAttributeKeys())) {
                taBuilder.putProperties(key, ta.getAttribute(key));
            }
        }

        return taBuilder.build();
    }

    public static TextAnnotation readTextAnnotation(TextAnnotationProto taImpl) throws Exception {
        String corpusId = taImpl.getCorpusId();
        String id = taImpl.getId();
        String text = taImpl.getText();
        String[] tokens = taImpl.getTokensList().toArray(new String[0]);

        Pair<Pair<String, Double>, int[]> sentences = readSentences(taImpl.getSentences());

        IntPair[] offsets = TokenUtils.getTokenOffsets(text, tokens);

        TextAnnotation ta =
                new TextAnnotation(corpusId, id, text, offsets, tokens, sentences.getSecond());

        for (ViewProto view : taImpl.getViewsList()) {
            String viewName = view.getViewName();

            List<View> topKViews = new ArrayList<>();
            for (ViewDataProto viewData : view.getViewDataList()) {
                topKViews.add(readViewData(viewData, ta));
            }


            if (viewName.equals(ViewNames.SENTENCE))
                ta.removeView(viewName);

            ta.addTopKView(viewName, topKViews);

            if (viewName.equals(ViewNames.SENTENCE))
                ta.setSentences();
        }

        for (Map.Entry<String, String> entry: taImpl.getPropertiesMap().entrySet()) {
            ta.addAttribute(entry.getKey(), entry.getValue());
        }

        return ta;
    }

    /** Public Methods to read/write Text Annotation */

    public static TextAnnotation parseFrom(String fileName) throws Exception {
        FileInputStream fileInputStream = new FileInputStream(fileName);
        return parseFrom(fileInputStream);
    }

    public static TextAnnotation parseFrom(byte[] byteData) throws Exception {
        TextAnnotationProto taProto = TextAnnotationProto.parseFrom(byteData);
        return readTextAnnotation(taProto);
    }

    public static TextAnnotation parseFrom(InputStream inputStream) throws Exception {
        TextAnnotationProto taProto = TextAnnotationProto.parseFrom(inputStream);
        return readTextAnnotation(taProto);
    }

    public static void writeToFile(TextAnnotation ta, String fileName) throws Exception {
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);
        writeToOutputStream(ta, fileOutputStream);
    }

    public static void writeToOutputStream(TextAnnotation ta, OutputStream outputStream) throws Exception {
        TextAnnotationProto taProto = writeTextAnnotation(ta);
        taProto.writeTo(outputStream);
    }

    public static byte[] writeAsBytes(TextAnnotation ta) throws Exception {
        TextAnnotationProto taProto = writeTextAnnotation(ta);
        return taProto.toByteArray();
    }
}
