package edu.illinois.cs.cogcomp.finetyper.wsd.embedding;


import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.finetyper.wsd.math.FloatDenseVector;

import java.io.*;
import java.util.*;

/**
 * Created by haowu4 on 1/14/17.
 * A Embedding manager.
 */
public class EmbeddingSpace {

    Map<String, Integer> entryToId;
    FloatDenseVector[] embeddings;

    /**
     * Initialize a EmbeddingSpace from a file.
     *
     * @param inputStream Stream that contains the Embedding Table.
     * @return
     */
    public static EmbeddingSpace readEmbeddingStream(InputStream inputStream) {
        List<FloatDenseVector> vectors = new ArrayList<>();
        Map<String, Integer> toId = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            int counter = 0;
            while ((line = br.readLine()) != null) {
                String trimedLine = line.trim();
                if (trimedLine.isEmpty()) {
                    continue;
                }

                String[] parts = trimedLine.split("\t");
                if (parts.length != 2) {
                    continue;
                }
                String entry = parts[0];
                String[] vecParts = parts[1].split(" ");
                float[] vec = new float[vecParts.length];
                for (int i = 0; i < vecParts.length; i++) {
                    vec[i] = Float.valueOf(vecParts[i]);
                }
                vectors.add(new FloatDenseVector(vec));
                toId.put(entry, counter);
                counter++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new EmbeddingSpace(toId, vectors);
    }

    /**
     * Default constructor.
     *
     * @param entryToId  Lookup table for entry to index of the embeddings array.
     * @param embeddings Array of Embeddings.
     */
    public EmbeddingSpace(Map<String, Integer> entryToId, FloatDenseVector[]
            embeddings) {
        this.entryToId = entryToId;
        this.embeddings = embeddings;
    }

    public EmbeddingSpace(Map<String, Integer> toId, List<FloatDenseVector>
            vectors) {
        this(toId, vectors.toArray(new FloatDenseVector[vectors.size()]));
    }

    /**
     * Get the embedding of a word or return null.
     *
     * @param word Query.
     * @return Embedding of word query.
     */
    public FloatDenseVector getEmbeddingOrNull(String word) {
        if (entryToId.containsKey(word)) {
            return embeddings[entryToId.get(word)];
        }
        return null;
    }

    public FloatDenseVector getEmbeddingOrNull(Sentence sentence) {
        View tokens = sentence.getView(ViewNames.TOKENS);
        List<Constituent> constituents = tokens.getConstituents();
        FloatDenseVector sum = null;
        for (Constituent c : constituents) {
            String token = c.getSurfaceForm();

            FloatDenseVector dv = getEmbeddingOrNull(token);
            if (dv == null) continue;
            if (sum == null) {
                sum = new FloatDenseVector(dv);
            } else {
                sum.iadd(dv);
            }
        }

        return sum;
    }

}
