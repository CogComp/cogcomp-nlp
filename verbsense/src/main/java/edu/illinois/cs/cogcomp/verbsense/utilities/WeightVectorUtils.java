/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.verbsense.utilities;

import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class WeightVectorUtils {

    private final static Logger log = LoggerFactory.getLogger(WeightVectorUtils.class);

    public static void save(String fileName, WeightVector wv) throws IOException {
        BufferedOutputStream stream =
                new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(fileName)));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream));

        float[] w = wv.getWeightArray();

        writer.write("WeightVector");
        writer.newLine();

        writer.write(w.length + "");
        writer.newLine();

        int numNonZero = 0;
        for (int index = 0; index < w.length; index++) {
            if (w[index] != 0) {
                writer.write(index + ":" + w[index]);
                writer.newLine();
                numNonZero++;
            }
        }

        writer.close();
        log.info("Number of non zero weights: " + numNonZero);
    }

    public static WeightVector load(String fileName) {
        try {
            GZIPInputStream zipin = new GZIPInputStream(new FileInputStream(fileName));
            BufferedReader reader = new BufferedReader(new InputStreamReader(zipin));

            String line;

            line = reader.readLine().trim();
            if (!line.equals("WeightVector")) {
                reader.close();
                throw new IOException("Invalid model file.");
            }

            line = reader.readLine().trim();
            int size = Integer.parseInt(line);

            WeightVector w = new WeightVector(size);

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                String[] parts = line.split(":");
                int index = Integer.parseInt(parts[0]);
                float value = Float.parseFloat(parts[1]);
                w.setElement(index, value);
            }

            zipin.close();

            return w;
        } catch (Exception e) {
            log.error("Error loading model file {}", fileName);
            System.exit(-1);
        }
        return null;
    }

    public static WeightVector loadWeightVectorFromClassPath(String fileName) {
        try {
            Class<WeightVectorUtils> clazz = WeightVectorUtils.class;
            List<URL> list = IOUtils.lsResources(clazz, fileName);

            if (list.size() == 0) {
                log.error("File {} not found on the classpath", fileName);
                throw new Exception("File not found on classpath");
            }
            InputStream stream = list.get(0).openStream();
            GZIPInputStream zipin = new GZIPInputStream(stream);
            BufferedReader reader = new BufferedReader(new InputStreamReader(zipin));

            String line;

            line = reader.readLine().trim();
            if (!line.equals("WeightVector")) {
                reader.close();
                throw new IOException("Invalid model file.");
            }

            line = reader.readLine().trim();
            int size = Integer.parseInt(line);

            WeightVector w = new WeightVector(size);

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                String[] parts = line.split(":");
                int index = Integer.parseInt(parts[0]);
                float value = Float.parseFloat(parts[1]);
                w.setElement(index, value);
            }

            zipin.close();
            return w;
        } catch (Exception e) {
            log.error("Error loading model file {}", fileName);
            System.exit(-1);
        }
        return null;
    }

}
