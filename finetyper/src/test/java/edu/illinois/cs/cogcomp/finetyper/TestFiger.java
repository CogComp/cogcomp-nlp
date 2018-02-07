package edu.illinois.cs.cogcomp.finetyper;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.BasicAnnotatorService;
import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.finetyper.finer.FinerAnnotator;
import edu.illinois.cs.cogcomp.finetyper.finer.FinerTyperFactory;
import edu.illinois.cs.cogcomp.finetyper.finer.datastructure.types.TypeSystem;
import edu.illinois.cs.cogcomp.finetyper.wsd.WordSenseAnnotator;
import edu.illinois.cs.cogcomp.finetyper.wsd.math.FloatDenseVector;
import edu.illinois.cs.cogcomp.pipeline.main.PipelineFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.cogcomp.DatastoreException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


/**
 * Created by haowu4 on 1/31/17.
 */
public class TestFiger {

    public static void printColumn(TextAnnotation ta) {
        View view = ta.getView(ViewNames.FINE_NER_TYPE);
        String[] tokens = ta.getTokens();
        String[] bios = new String[tokens.length];
        String[] types = new String[tokens.length];

        for (int i = 0; i < tokens.length; i++) {
            bios[i] = "O";
            types[i] = null;
        }
        for (Constituent c : view.getConstituents()) {
            for (int j = c.getStartSpan(); j < c.getEndSpan(); j++) {
                bios[j] = "I";
                types[j] = String.join(",", c.getLabelsToScores().keySet());
            }

            bios[c.getStartSpan()] = "B";
        }

        for (int i = 0; i < tokens.length; i++) {
            String label = "O";
            if (null != types[i]) {
                label = String.format("%s-%s", bios[i], types[i]);
            }
            System.out.println(String.format("%s\t%s", tokens[i], label));
        }
    }

    public static List<TextAnnotation> loadFigers() throws
            IOException, AnnotatorException {
        List<String> sentences = new ArrayList<>();
        List<TextAnnotation> tas = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        ClassLoader.getSystemResourceAsStream("finer_resource/figer.xiang.label")))) {
            String line;
            BasicAnnotatorService bas = PipelineFactory.buildPipeline(ViewNames.POS, ViewNames.NER_ONTONOTES);
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    List<String[]> doc = new ArrayList<>();
                    String[] x = new String[sentences.size()];
                    for (int i = 0; i < sentences.size(); i++) {
                        x[i] = sentences.get(i);
                    }
                    doc.add(x);
                    TextAnnotation ta = BasicTextAnnotationBuilder
                            .createTextAnnotationFromTokens(doc);
                    ta = bas.annotateTextAnnotation(ta, false);
                    ta.getView(ViewNames.POS);
                    ta.getView(ViewNames.NER_ONTONOTES);
                    tas.add(ta);
                    sentences = new ArrayList<>();
                } else {
                    String w = line.split("\t")[0];
                    sentences.add(w);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        return tas;
    }

    public static void main(String[] args) throws IOException,
            AnnotatorException, DatastoreException {
        List<TextAnnotation> inputs = loadFigers();
        WordSenseAnnotator wsd = new WordSenseAnnotator(ViewNames.FINE_NER_TYPE_WSD, new ResourceManager(new Properties()));
        FinerTyperFactory factory = new FinerTyperFactory();
        FinerAnnotator finer = factory.getAnnotator();
        for (TextAnnotation ta : inputs) {
            wsd.addView(ta);
            finer.addView(ta);
            printColumn(ta);
            System.out.println();
        }

    }
}
