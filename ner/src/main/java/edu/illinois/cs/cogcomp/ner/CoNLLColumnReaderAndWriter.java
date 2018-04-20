/**
 * 
 */
package edu.illinois.cs.cogcomp.ner;

import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.sequences.DocumentReaderAndWriter;
import edu.stanford.nlp.sequences.IOBUtils;
import edu.stanford.nlp.sequences.SeqClassifierFlags;
import edu.stanford.nlp.util.AbstractIterator;

/**
 * This class reads CoNLL 05 column format. The existing readers were not sufficient
 * documentented, and access modifiers not sufficiently open to subclass the existing 
 * classes that did most all this work, so I had to start from scratch.
 * @author redman
 */
public class CoNLLColumnReaderAndWriter  implements DocumentReaderAndWriter<CoreLabel> {
    
    /** default. */
    private static final long serialVersionUID = 1L;

    /** the sentecne boundary. */
    public static final String BOUNDARY = "*BOUNDARY*";
    
    /** these flags are allow us to split docs on -DOCSTART- tag or not, and some
     * label remapping needed by stanford.
     */
    private SeqClassifierFlags flags; // = null;
    
    /** matches white space. */
    private static final Pattern space = Pattern.compile("^\\s*$");

    @Override
    public Iterator<List<CoreLabel>> getIterator(Reader r) {
        
        /**
         * Iterator produces only one document per file in this case.
         * @author redman
         */
        class CoNLLIterator extends AbstractIterator<List<CoreLabel>> {
            
            /** the split lines. */
            private String data;
                        
            /**
             * need a reader to do anything.
             * @param r the reader.
             */
            public CoNLLIterator(Reader r) {
                data = IOUtils.slurpReader(r);
            }

            @Override
            public boolean hasNext() {
                if (data != null)
                    return true;
                else 
                    return false;
            }

            @Override
            public List<CoreLabel> next() {
                String tmp = data;
                data = null;
                return processDocument(tmp);
            }
        }
        return new CoNLLIterator(r);
    }
    
    /**
     * process the document, we don't split on -DOCSTART- tags, not necessary for scoring
     * the stanford stuff.
     * @param doc the data from the document.
     * @return the list of core labels.
     */
    private List<CoreLabel> processDocument(String doc) {
        List<CoreLabel> list = new ArrayList<>();
        
        // split lines, iterate over them.
        String[] lines = doc.split("\n");
        for (String line : lines) {
            if (line.contains("-DOCSTART-"))
                continue;
            if (!flags.deleteBlankLines || !space.matcher(line).matches()) {
                list.add(generateLabel(line));
            }
        }
        IOBUtils.entitySubclassify(list, CoreAnnotations.AnswerAnnotation.class, flags.backgroundSymbol, flags.entitySubclassification, flags.intern);
        return list;
    }

    /** 
     * Parse a line from the CoNLL format file producing a single core label annotation.
     *  @param line a single line from a CoNLL 05 file.
     *  @return the newly created labeled data object.
     */
    protected CoreLabel generateLabel(String line) {
        CoreLabel wi = new CoreLabel();
        // wi.line = line;
        String[] columns = line.split("\\s+");
        switch (columns.length) {
            case 0:
            case 1:
                wi.setWord(BOUNDARY);
                wi.set(CoreAnnotations.AnswerAnnotation.class, flags.backgroundSymbol);
                break;
            default:
                wi.setWord(columns[5]);
                wi.setTag(columns[4]);
                wi.set(CoreAnnotations.AnswerAnnotation.class, columns[0]);
                break;
        }

        // Value annotation is used in a lot of place in corenlp so setting here as the word itself
        wi.set(CoreAnnotations.ValueAnnotation.class, wi.word());
        wi.set(CoreAnnotations.GoldAnswerAnnotation.class, wi.get(CoreAnnotations.AnswerAnnotation.class));
        return wi;
    }

    @Override
    public void init(SeqClassifierFlags flags) {
        this.flags = flags;
    }

    @Override
    public void printAnswers(List<CoreLabel> doc, PrintWriter out) {        
    }
}
