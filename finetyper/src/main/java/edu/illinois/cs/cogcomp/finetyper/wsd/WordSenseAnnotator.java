package edu.illinois.cs.cogcomp.finetyper.wsd;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.finetyper.FinerResource;
import edu.illinois.cs.cogcomp.finetyper.wsd.datastructure.WordAndPOS;
import edu.illinois.cs.cogcomp.finetyper.wsd.embedding.EmbeddingSpace;
import edu.illinois.cs.cogcomp.finetyper.wsd.math.FloatDenseVector;
import edu.illinois.cs.cogcomp.finetyper.wsd.math.Similarity;
import org.cogcomp.Datastore;
import org.cogcomp.DatastoreException;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by haowu4 on 1/13/17.
 */

public class WordSenseAnnotator extends Annotator {

    public static final String ANNOTATOR_NAME = "EmbeddingWSDAnnotator";
    public final String VIEWNAME;

    private EmbeddingSpace wordEmbeddings;
    private EmbeddingSpace senseEmbeddings;
    private Map<WordAndPOS, List<String>> candidatesMaps;

    private List<String> wordToSense(String w, String pos) {
        return candidatesMaps.getOrDefault(new WordAndPOS(w, pos), new
                ArrayList<String>());
    }

    public WordSenseAnnotator(String viewName, ResourceManager rm) {
        super(viewName, new String[]{ViewNames.POS}, rm);
        VIEWNAME = rm.getString("fine_type_wsd_viewname", ViewNames.FINE_NER_TYPE_WSD);
    }


    @Override
    public void initialize(ResourceManager rm) {

        try {
            Datastore ds = FinerResource.getDefaultDatastore();

            // Read word embedding.

            try (InputStream input = FinerResource.getResourceInputStream(ds, FinerResource.WORD_EMBEDDING_TAR_GZ)) {
                wordEmbeddings = EmbeddingSpace.readEmbeddingStream(input);
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Having trouble reading important resources WORD_EMBEDDING_TAR_GZ.");
            }


            // Read sense embedding.
            try (InputStream input = FinerResource.getResourceInputStream(ds, FinerResource.SENSE_EMBEDDING_TAR_GZ)) {
                senseEmbeddings = EmbeddingSpace.readEmbeddingStream(input);
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Having trouble reading important resources SENSE_EMBEDDING_TAR_GZ.");
            }


            candidatesMaps = new HashMap<>();

            try (InputStream inputStream = FinerResource.getResourceInputStream(ds, FinerResource.WORD_POS_TO_SENSE_TAR_GZ)) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {

                    String line;

                    while ((line = br.readLine()) != null) {
                        String trimedLine = line.trim();
                        if (trimedLine.isEmpty()) {
                            continue;
                        }

                        String[] parts = trimedLine.split("\t");
                        String word = parts[0];
                        String pos = parts[1];
                        String[] senses = parts[2].split(" ");
                        WordAndPOS key = new WordAndPOS(word, pos);
                        List<String> candidates = candidatesMaps.getOrDefault(key, new
                                ArrayList<String>());
                        for (String s : senses) {
                            candidates.add(s);
                        }
                        candidatesMaps.put(key, candidates);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println("Having trouble reading important resources WORD_POS_TO_SENSE_TAR_GZ.");
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Having trouble reading important resources WORD_POS_TO_SENSE_TAR_GZ.");
            }


        } catch (DatastoreException e) {
            e.printStackTrace();
            System.err.println("Having trouble connecting to Datastore.");
        }


    }

    @Override
    public void addView(TextAnnotation ta) throws AnnotatorException {
        View v = new View(VIEWNAME, ANNOTATOR_NAME, ta, 1.0);
        View tokens = ta.getView(ViewNames.TOKENS);
        for (Sentence sentence : ta.sentences()) {
            int startTokenId = sentence.getStartSpan();
            int endTokenId = sentence.getStartSpan();
            for (int i = startTokenId; i < endTokenId; i++) {
                Constituent ct = tokens.getConstituents().get(i);
                Map<String, Double> result = predict(sentence, ct);
                if (result.isEmpty()) continue;
                v.addConstituent(new Constituent(result, VIEWNAME, ta, ct
                        .getStartSpan(), ct.getEndSpan()));
            }
        }
        ta.addView(VIEWNAME, v);
    }

    /**
     * Map Part of speech to the convention used in the sense embedding.
     *
     * @param pos String of POS tag, such as VB.
     * @return
     */
    public static String mapPOS(String pos) {
        switch (pos.toLowerCase().charAt(0)) {
            case 'v':
                return "v";
            case 'n':
                return "n";
            default:
                return "";
        }
    }

    /**
     * Predict the sense of a token based on the cosine similarity of sense embedding and average word embedding in
     * the sentence.
     *
     * @param sentence Sentence that contains the given constituent.
     * @param ct       The given constituent
     * @return
     */
    public Map<String, Double> predict(Sentence sentence, Constituent ct) {
        Map<String, Double> result = new HashMap<>();

        String query = ct.getSurfaceForm();
        String pos = ct.getTextAnnotation().getView(ViewNames.POS)
                .getConstituentsCovering(ct).get(0).getLabel();
        List<String> senses = wordToSense(query, mapPOS(pos));

        if (senses.isEmpty()) {
            return result;
        }

        FloatDenseVector sentenceEmbedding = wordEmbeddings.getEmbeddingOrNull(sentence);


        if (sentenceEmbedding == null) {
            return result;
        }

        double normOfSentence = Similarity.l2norm(sentenceEmbedding);
        for (String s : senses) {
            FloatDenseVector senseEmbedding = senseEmbeddings.getEmbeddingOrNull(s);
            if (senseEmbedding == null) continue;
            double sim = Similarity.cosine(sentenceEmbedding, senseEmbedding,
                    normOfSentence,
                    Similarity.l2norm(senseEmbedding));
            result.put(s, sim);
        }

        return result;
    }


}
