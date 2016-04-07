package edu.illinois.cs.cogcomp.corpusreaders;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.TextAnnotationReader;

/**
 * Corpus reader for the ACE data-set.
 * This reader currently only supports only the ACE-2004 and ACE-2005 data-sets.
 *
 * @author Bhargav Mangipudi
 */
public class ACEReader extends TextAnnotationReader {
    private String corpusId;
    private boolean is2004mode;
    private String[] sections;

    public ACEReader(String aceCorpusHome, String[] sections, boolean is2004mode) {
        super(aceCorpusHome);

        this.is2004mode = is2004mode;
        this.corpusId = is2004mode ? "ACE2004" : "ACE2005";
    }

    public ACEReader(String aceCorpusHome, boolean is2004mode) {
        this(aceCorpusHome, null, is2004mode);
    }

    @Override
    protected void initializeReader() {

    }

    @Override
    protected TextAnnotation makeTextAnnotation() throws Exception {
        return null;
    }

    /**
     * Returns {@code true} if the iteration has more elements.
     * (In other words, returns {@code true} if {@link #next} would
     * return an element rather than throwing an exception.)
     *
     * @return {@code true} if the iteration has more elements
     */
    @Override
    public boolean hasNext() {
        return false;
    }
}
