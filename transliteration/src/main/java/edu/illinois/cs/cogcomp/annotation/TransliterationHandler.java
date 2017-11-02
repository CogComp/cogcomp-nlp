/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.annotation;

import edu.illinois.cs.cogcomp.annotation.handler.IllinoisAbstractHandler;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.thrift.base.AnnotationFailedException;
import edu.illinois.cs.cogcomp.thrift.base.Labeling;
import edu.illinois.cs.cogcomp.thrift.base.Span;
import edu.illinois.cs.cogcomp.thrift.curator.Record;
import edu.illinois.cs.cogcomp.thrift.labeler.Labeler;
import edu.illinois.cs.cogcomp.transliteration.Example;
import edu.illinois.cs.cogcomp.transliteration.SPModel;
import edu.illinois.cs.cogcomp.utils.TopList;
import edu.illinois.cs.cogcomp.utils.Utils;
import org.apache.thrift.TException;
import edu.illinois.cs.cogcomp.curator.RecordGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by mayhew2 on 1/14/16.
 */
public class TransliterationHandler extends IllinoisAbstractHandler implements Labeler.Iface {
    private static Logger logger = LoggerFactory.getLogger(TransliterationHandler.class);
    private static final String ARTIFACTNAME = "illinois-transliteration";
    private static final String VERSION = "1.0.0";
    private static final String PACKAGENAME = "Illinois Transliteration";

    private SPModel model;

    public TransliterationHandler(String trainfilename) throws FileNotFoundException {
        super(PACKAGENAME, VERSION, ARTIFACTNAME);

        boolean fix = true;

        List<Example> training = Utils.readWikiData(trainfilename, fix);

        model = new SPModel(training.subList(0,200));
        model.Train(2);


    }

    @Override
    public Labeling labelRecord(Record record) throws AnnotationFailedException, TException {

        String text = record.getRawText().toLowerCase();
        List<String> words = Arrays.asList(text.split(" "));

        int limit = 5;
        if (words.size() > limit){
            logger.info("Transliteration handler does not handle text with more than {} tokens. Just annotating the first {}...", limit, limit);
            words = words.subList(0,limit);
        }

        List<Span> labels = new ArrayList<>();

        int i = 0;
        for(String name : words) {

            if(name.length() == 0){
                i += 1;
                continue;
            }

            logger.debug("[" + name + "]");

            TopList<Double, String> res = null;

            Span span = new Span();
            span.setStart(i);
            span.setEnding(i + name.length());
            try {
                res = model.Generate(name);
                Pair<Double, String> best = res.getFirst();
                span.setLabel(best.getSecond());

            } catch (Exception e) {
                e.printStackTrace();
                span.setLabel("UNLABELED");
            }

            labels.add(span);

            i += name.length()+1; // extra 1 is for spaces...
        }

        Labeling labeling = new Labeling();
        labeling.setSource(getSourceIdentifier());
        labeling.setLabels(labels);
        return labeling;
    }

    public static void main(String[] args) throws Exception {
        String text  = "whatever";

        String wikidata = "/shared/corpora/transliteration/wikidata/wikidata.Hindi";

        TransliterationHandler handler = new TransliterationHandler(wikidata);
        Record input = RecordGenerator.generateTokenRecord( text, false );

        Labeling labels = handler.labelRecord(input);
        for(Iterator<Span> label = labels.getLabelsIterator(); label.hasNext() ; ) {
            Span span = label.next();
            System.out.println("["+span.start+"-"+span.ending+"]");
            System.out.println(text.substring(span.start, span.ending)+"\t:\t"+span.getLabel());
        }
    }

}
