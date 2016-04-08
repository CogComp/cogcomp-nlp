package edu.illinois.cs.cogcomp.corpusreaders;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.corpusreaders.aceReader.annotationStructure.ACEDocument;
import edu.illinois.cs.cogcomp.corpusreaders.aceReader.documentReader.AceFileProcessor;
import edu.illinois.cs.cogcomp.corpusreaders.aceReader.documentReader.ReadACEAnnotation;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.TextAnnotationReader;
import edu.illinois.cs.cogcomp.nlp.tokenizer.IllinoisTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Corpus reader for the ACE data-set.
 * This reader currently only supports only the ACE-2004 and ACE-2005 data-sets.
 *
 * @author Bhargav Mangipudi
 */
public class ACEReader extends TextAnnotationReader {
    private String aceCorpusHome;
    private boolean is2004mode;
    private String[] sections;

    private List<TextAnnotation> documents;

    public ACEReader(String aceCorpusHome, String[] sections, boolean is2004mode) throws Exception {
        super(aceCorpusHome);

        this.aceCorpusHome = aceCorpusHome;
        this.documents = new ArrayList<>();
        this.is2004mode = is2004mode;
        this.sections = sections;

        if (this.sections == null) {
            this.sections = IOUtils.lsDirectories(this.aceCorpusHome);
        }

        // TODO:Ideally constructor should'nt be reading and processing I/O.
        this.updateCurrentFiles();
    }

    public ACEReader(String aceCorpusHome, boolean is2004mode) throws Exception {
        this(aceCorpusHome, null, is2004mode);
    }


    @Override
    protected void initializeReader() {
        // This is called even before our class's constructor. Useless method.
    }

    // Find out all files and read text annotations here.
    protected void updateCurrentFiles() {
        File corpusHomeDir = new File(this.aceCorpusHome);
        assert corpusHomeDir.isDirectory();

        FilenameFilter apfFileFilter = new FilenameFilter() {
            public boolean accept(File directory, String fileName) {
                return fileName.endsWith(".apf.xml");
            }
        };

        ReadACEAnnotation.is2004mode = this.is2004mode;
        AceFileProcessor fileProcessor = new AceFileProcessor(new TokenizerTextAnnotationBuilder(new IllinoisTokenizer()));

        for (String section : this.sections) {
            File sectionDir = new File(corpusHomeDir.getAbsolutePath() + "/" + section);

            for (File f : sectionDir.listFiles(apfFileFilter)) {
                ACEDocument doc = fileProcessor.processAceEntry(sectionDir, f.getAbsolutePath());

                // TODO: Fix this. Unify TAs
                this.documents.add(doc.taList.get(0));
            }
        }
    }

    @Override
    protected TextAnnotation makeTextAnnotation() throws Exception {
        return this.documents.get(this.currentAnnotationId);
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
        return this.documents.size() > this.currentAnnotationId;
    }
}
