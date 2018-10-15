/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.verbsense.caches;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.core.StructuredProblem;
import edu.illinois.cs.cogcomp.sl.util.FeatureVector;
import edu.illinois.cs.cogcomp.verbsense.core.SenseManager;
import edu.illinois.cs.cogcomp.verbsense.jlis.SenseInstance;
import edu.illinois.cs.cogcomp.verbsense.jlis.SenseStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class FeatureVectorCacheFile implements Closeable,
        Iterator<Pair<SenseInstance, SenseStructure>> {

    private final static Logger log = LoggerFactory.getLogger(FeatureVectorCacheFile.class);

    private BufferedWriter writer;
    private BufferedReader reader;
    private String file;
    private SenseManager manager;

    private String nextLine = null;

    public FeatureVectorCacheFile(String file, SenseManager manager) throws IOException {
        this.file = file;
        this.manager = manager;
    }

    private void openWriter(String file) throws IOException {
        BufferedOutputStream stream =
                new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(file)));
        writer = new BufferedWriter(new OutputStreamWriter(stream));
    }

    public synchronized void put(String lemma, int label, FeatureVector features) throws Exception {
        if (writer == null) {
            openWriter(file);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(lemma).append("\t").append(label).append("\t");
        int[] idx = features.getIdx();
        float[] value = features.getValue();
        for (int i = 0; i < idx.length; i++) {
            sb.append(idx[i]).append(":").append(value[i]).append(" ");
        }

        writer.write(sb.toString().trim());
        writer.newLine();
    }

    public void close() {
        try {
            if (writer != null)
                writer.close();

            if (reader != null)
                reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized Pair<SenseInstance, SenseStructure> next() {
        try {
            assert reader != null;

            if (nextLine == null)
                hasNext();

            String[] parts = nextLine.split("\t");
            String lemma = parts[0].trim();
            int label = Integer.parseInt(parts[1]);

            String features = parts[2];

            SenseInstance x = new SenseInstance(lemma, features);
            SenseStructure y = new SenseStructure(x, label, manager);

            return new Pair<>(x, y);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void openReader() throws IOException {
        GZIPInputStream zipin = new GZIPInputStream(new FileInputStream(file));
        reader = new BufferedReader(new InputStreamReader(zipin));
    }

    @Override
    public boolean hasNext() {
        try {
            if (reader == null)
                openReader();

            nextLine = reader.readLine();

            if (nextLine == null)
                return false;

            nextLine = nextLine.trim();

            return true;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void remove() {}

    public StructuredProblem getStructuredProblem() {
        return getStructuredProblem(-1);
    }

    public StructuredProblem getStructuredProblem(int sizeLimit) {
        int count = 0;
        StructuredProblem problem = new StructuredProblem();

        log.info("Creating structured problem");
        while (hasNext()) {
            Pair<SenseInstance, SenseStructure> pair = next();
            problem.input_list.add(pair.getFirst());
            problem.output_list.add(pair.getSecond());

            count++;
            if (sizeLimit >= 0 && count >= sizeLimit)
                break;

            if (count % 10000 == 0) {
                log.info("{} examples loaded", count);
            }
        }

        log.info("{} examples loaded. Finished creating structured problem", count);

        return problem;
    }

}
