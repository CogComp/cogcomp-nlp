package edu.illinois.cs.cogcomp.nlp.corpusreaders;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;

/**
 * MASC is by default stored in GrAF XML format.
 * Use ANC Tool to convert to CoNLL column format.
 *
 * This class reads MASC CoNLL column format data,
 *   converted by ANC Tool v3.0.2 with all Annotation types, "Penn POS tags" Token Type, and "Penn" Sentence Type
 * See the class constants about annotations available and their column numbers.
 * Created by xle2 on 11/1/2017.
 */
public class CoNLLMASCReader extends CoNLLGenericReader {
    public static final int COLUMN_TOKEN_NUMBER_OF_LINE = 0;  // Unprocessed
    public static final int COLUMN_TOKEN_BEGIN_OFFSET = 1;  // Unprocessed
    public static final int COLUMN_TOKEN_END_OFFSET = 2;  // Unprocessed
    public static final int COLUMN_TOKEN = 3;
    public static final int COLUMN_LEMMA = 4;
    public static final int COLUMN_PART_OF_SPEECH = 5;
    public static final int COLUMN_NOUN_CHUNK = 6;  // "nchunk" (IOB)
    public static final int COLUMN_VERB_CHUNK = 7;  // "vchunk" (IOB)
    public static final int COLUMN_CES_DOC = 8;  // Unprocessed, "cesDoc" (IOB)
    public static final int COLUMN_TEXT = 9;  // Unprocessed, "text" (IOB)
    public static final int COLUMN_BODY = 10;  // Unprocessed, "body" (IOB)
    public static final int COLUMN_PARAGRAPH = 11;  // Unprocessed, "p" (IOB)
    public static final int COLUMN_DATE = 12;  // "date" (IOB)
    public static final int COLUMN_LOCATION = 13;  // "location" (IOB)
    public static final int COLUMN_ORGANIZATION = 14;  // "org" (IOB)
    public static final int COLUMN_PERSON = 15;  // "person" (IOB)

    public CoNLLMASCReader(String mascDirectory) {
        super("MASC-3.0.0", mascDirectory);
    }

    @Override
    protected CoNLLColumnConfig getColumnConfig() {
        return new CoNLLColumnConfig(
                COLUMN_TOKEN,  // tokenColumn
                Arrays.asList(COLUMN_LEMMA, COLUMN_PART_OF_SPEECH),  // tokenLabelColumns
                Arrays.asList(COLUMN_NOUN_CHUNK, COLUMN_VERB_CHUNK, COLUMN_DATE, COLUMN_LOCATION, COLUMN_ORGANIZATION, COLUMN_PERSON)  // iobColumns
        );
    }

    @Override
    protected void processViews(
            TextAnnotation ta,
            Map<Integer, List<Pair<Integer, String>>> tokenLabelStreams,
            Map<Integer, SpanCollector> spanStreams,
            String filename) {
        int labelCount = 0;
        String logHeader = "[" + shortenPath(filename) + "] ";

        labelCount = createTokenLabelView(tokenLabelStreams.get(COLUMN_LEMMA).stream(), ta, ViewNames.LEMMA);
        logger.info(logHeader +
                "Processed column " + COLUMN_LEMMA + " (Lemma) " +
                "into " + labelCount + " LEMMA constituents.");

        labelCount = createTokenLabelView(tokenLabelStreams.get(COLUMN_PART_OF_SPEECH).stream(), ta, ViewNames.POS);
        logger.info(logHeader +
                "Processed column " + COLUMN_PART_OF_SPEECH + " (POS) " +
                "into " + labelCount + " POS constituents.");

        // MASC have NPs and VPs in separate columns and might overlap
        labelCount = createSpanLabelView(concat(
                mapSecond(spanStreams.get(COLUMN_NOUN_CHUNK).stream(), label -> "NP"),
                mapSecond(spanStreams.get(COLUMN_VERB_CHUNK).stream(), label -> "VP")
        ), ta, ViewNames.SHALLOW_PARSE, true);
        logger.info(logHeader +
                "Merged " +
                "column " + COLUMN_NOUN_CHUNK + "(Noun chunk) " +
                "column " + COLUMN_VERB_CHUNK + "(Verb chunk) " +
                "into " + labelCount + " SHALLOW_PARSE constituents.");

        // MASC have different NER types in separate columns and might overlap
        labelCount = createSpanLabelView(concat(
                mapSecond(spanStreams.get(COLUMN_LOCATION).stream(), label -> "LOC"),
                mapSecond(spanStreams.get(COLUMN_ORGANIZATION).stream(), label -> "ORG"),
                mapSecond(spanStreams.get(COLUMN_PERSON).stream(), label -> "PER")
        ), ta, ViewNames.NER_CONLL, true);
        logger.info(logHeader +
                "Merged " +
                "column " + COLUMN_LOCATION + " (Location) " +
                "column " + COLUMN_ORGANIZATION + " (Organization) " +
                "column " + COLUMN_PERSON + " (Person) " +
                "into " + labelCount + " NER_CONLL constituents.");

        // MASC have different NER types in separate columns and might overlap
        labelCount = createSpanLabelView(concat(
                mapSecond(spanStreams.get(COLUMN_DATE).stream(), label -> "DATE"),
                mapSecond(spanStreams.get(COLUMN_LOCATION).stream(), label -> "LOCATION"),
                mapSecond(spanStreams.get(COLUMN_ORGANIZATION).stream(), label -> "ORGANIZATION"),
                mapSecond(spanStreams.get(COLUMN_PERSON).stream(), label -> "PERSON")
        ), ta, ViewNames.NER_ONTONOTES, true);
        logger.info(logHeader +
                "Merged " +
                "column " + COLUMN_DATE + " (Date) " +
                "column " + COLUMN_LOCATION + " (Location) " +
                "column " + COLUMN_ORGANIZATION + " (Organization) " +
                "column " + COLUMN_PERSON + " (Person) " +
                "into " + labelCount + " NER_ONTONOTES constituents.");
    }

    // TODO Move to Pair class
    public static <S, T, R> Stream<Pair<S, R>> mapSecond(Stream<Pair<S, T>> stream, Function<? super T, ? extends R> mapper) {
        return stream.map(pair -> new Pair<>(pair.getFirst(), mapper.apply(pair.getSecond())));
    }

    // TODO Move to utilities
    @SafeVarargs
    public static <T> Stream<T> concat(Stream<T>... streams) {
        return Stream.of(streams).flatMap(Function.identity());
    }

    // TODO Move to utilities
    public static String shortenPath(String path) {
        return path.replaceAll("^(\\w+:|)([\\\\|/][^\\\\|/]+[\\\\|/][^\\\\|/]+[\\\\|/]).*([\\\\|/][^\\\\|/]+[\\\\|/][^\\\\|/]+)$", "$1$2...$3");
    }
}
