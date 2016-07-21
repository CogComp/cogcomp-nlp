/**
 * This software is released under the University of Illinois/Research and
 *  Academic Use License. See the LICENSE file in the root folder for details.
 * Copyright (c) 2016
 *
 * Developed by:
 * The Cognitive Computation Group
 * University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * A corpus reader that allows incremental processing of a corpus (so not assuming all
 *    documents are read and instantiated on instantiating the reader).
 * Goal is to return one TextAnnotation object at a time, where that TextAnnotation
 *    corresponds to a document or some other natural unit of text from the corpus.
 * In addition, some non-body fields may be recorded as properties stored in the TextAnnotation
 *    object. These field names are arbitrary for now, but a CorpusReader must at least
 *    provide a list of names of these fields so that the user can retrieve them.
 * The idea is that these will be somewhat corpus-specific, but will contain information
 *    useful for tasks related to the corpus -- e.g. article title/headline, byline,
 *    timestamp.
 * TODO: establish some common fields/tags for this additional information.
 * TODO: currently, assumes that each file in the corpus list will generate at least one
 *    TextAnnotation. Can avoid by pre-loading next file's annotations as soon as last
 *    TextAnnotation from current file is returned.
 */
public abstract class AbstractIncrementalCorpusReader extends TextAnnotationReader
{
    // list of all files that comprise corpus
    private List<Path> fileList;

    // holds TextAnnotations extracted from most recently accessed file
    private List<TextAnnotation> stack;

    private int fileIndex;  // points to index of file corresponding to current non-exhausted stack
    private int stackIndex; // points to index of stack that will be returned by iterator next()
    private String sourceDirectory;

    /**
     * ResourceManager must specify the fields {@link CorpusReaderConfigurator}.CORPUS_NAME and .CORPUS_DIRECTORY,
     *   plus whatever is required by the derived class for initializeReader().
     *
     * @param rm ResourceManager
     * @throws Exception
     */
    public AbstractIncrementalCorpusReader(ResourceManager rm) throws Exception {
        super(rm);
    }

    /**
     * this method is called by the base class constructor, so all subclass-specific object initialization
     *    must be done here.
     */
    protected void initializeReader() {
        this.sourceDirectory = resourceManager.getString(CorpusReaderConfigurator.CORPUS_DIRECTORY);
        try {
            fileList = getFileListing();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException( e.getMessage() );
        }
    }


    public void reset()
    {
        fileIndex = 0;
        stackIndex = 0;
        stack = new ArrayList<>(fileList.size());
    }

    public String getSourceDirectory() {
        return sourceDirectory;
    }


    public boolean hasNext() {
            return stackIndex < stack.size() || fileIndex < fileList.size();
        }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration
     * @throws NoSuchElementException if the iteration has no more elements
     */
    @Override
    public TextAnnotation next() {
        if ( stack.isEmpty() && fileIndex >= fileList.size() )
            throw new NoSuchElementException();

        if ( stackIndex >= stack.size() )
        {
            stackIndex = 0;

            do {
                try {
                    stack = getTextAnnotationsFromFile( fileList.get( fileIndex++ ) );
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new NoSuchElementException( e.getMessage() );
                }
            }
            while( stack.isEmpty() && fileIndex < fileList.size() );
            // at this point, either stack has one or more elements or it is emtpy
            // because we didn't find any new TextAnnotations
        }

        TextAnnotation returnTa = stack.get( stackIndex++ );

        return returnTa;
    }


    /**
     * generate a list of files comprising the corpus. Each is expected to generate
     *    one or more TextAnnotation objects, though the way the iterator is implemented
     *    allows for corpus files to generate zero TextAnnotations if you are feeling
     *    picky.
     * Note that the corpus directory is specified as a constructor argument.
     *
     * @return a list of Path objects corresponding to files containing corpus documents to process.
     */
    abstract public List<Path> getFileListing() throws IOException;

    /**
     * given an entry from the corpus file list generated by {@link #getFileListing()}},
     *    parse its contents and get zero or more TextAnnotation objects.
     *
     * @param corpusFileListEntry corpus file containing content to be processed
     * @return List of TextAnnotation objects extracted from the corpus file
     */
    public abstract List<TextAnnotation> getTextAnnotationsFromFile(Path corpusFileListEntry) throws Exception;



}
