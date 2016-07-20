package edu.illinois.cs.cogcomp.nlp.corpusreaders;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotationUtilities;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * This reads column format CoNLL data for NER (from CoNLL 2002/2003 shared tasks).
 *
 * This only reads labels and words -- doesn't read POS tags, even if present.
 *
 * Created by mayhew2 on 7/20/16.
 */
public class CoNLLNerReader extends TextAnnotationReader {

    private List<TextAnnotation> textAnnotations;
    private int taCounter;

    private static Logger logger = LoggerFactory.getLogger(CoNLLNerReader.class);

    /**
     * This expects a directory that contains conll format files.
     * @param conlldirectory
     */
    public CoNLLNerReader(String conlldirectory){
        super(CorpusReaderConfigurator.buildResourceManager("NER_CONLL", conlldirectory));
        this.taCounter = 0;

    }

    @Override
    protected void initializeReader() {
        String[] files = new String[0];
        this.textAnnotations = new ArrayList<>();

        String corpusdirectory= this.resourceManager.getString(CorpusReaderConfigurator.CORPUS_DIRECTORY.key);

        // In case the input argument is a single file
        if (!IOUtils.isDirectory(corpusdirectory)) {
            files = new String[] {corpusdirectory};
        } else {
            try {
                files = IOUtils.ls(corpusdirectory);
            } catch (IOException e) {
                logger.error("Error listing directory.");
                logger.error(e.getMessage());
            }
        }
        try {
            for (String file : files) {
                textAnnotations.add(loadCoNLLfile(corpusdirectory + "/" + file));
            }
        } catch (IOException e) {
            logger.error("Error reading file.");
            logger.error(e.getMessage());
        }
    }

    /**
     * This loads filename into a textannotation.
     * @param filename
     * @return
     * @throws FileNotFoundException
     */
    private TextAnnotation loadCoNLLfile(String filename) throws FileNotFoundException {
        List<String> lines = LineIO.read(filename);

        List<IntPair> spans = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        StringBuilder text = new StringBuilder();

        int start = -1;
        String label = "";

        int i = 0;
        for(String line : lines){
            String[] sline = line.split("\t");


            if(line.startsWith("B-")){
                start = i;
                label = sline[0].split("-")[1];

            }else if(sline[0].startsWith("I-")){
                // don't do anything....
            }else{
                // it's O or it's empty
                if(start > -1){
                    // peel off a constituent if it exists.
                    spans.add(new IntPair(start, i));
                    labels.add(label);
                }

                label = "";
                start = -1;
            }

            // add the word form to the sentence.
            if(sline.length > 5 && !sline[5].equals("-DOCSTART-")) {
                text.append(sline[5] + " ");
                i++;
            }
        }

        // in case the very last line is an NE.
        if(start > -1){
            spans.add(new IntPair(start, i));
            labels.add(label);
        }

        TextAnnotation ta = TextAnnotationUtilities.createFromTokenizedString(text.toString());

        SpanLabelView emptyview = new SpanLabelView(ViewNames.NER_CONLL, "FromFile", ta, 1d);
        ta.addView(ViewNames.NER_CONLL, emptyview);

        for(int k = 0; k < labels.size(); k++){
            label = labels.get(k);
            IntPair span = spans.get(k);
            Constituent c = new Constituent(label, ViewNames.NER_CONLL, ta, span.getFirst(), span.getSecond());
            emptyview.addConstituent(c);
        }

        return ta;
    }

    @Override
    protected TextAnnotation makeTextAnnotation() throws Exception {
        if (!hasNext())
            return null;
        return textAnnotations.get(taCounter++);
    }

    @Override
    public boolean hasNext() {
        return textAnnotations.size() > taCounter;
    }

}
