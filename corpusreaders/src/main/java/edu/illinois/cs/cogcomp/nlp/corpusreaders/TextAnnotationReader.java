/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.IResetableIterator;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;

/**
 * A fairly generic iterator that generates TextAnnotation objects from some input source.
 *
 * @author Vivek Srikumar
 *         <p>
 *         Sep 4, 2009
 * @author mssammon
 */
public abstract class TextAnnotationReader implements Iterable<TextAnnotation>,
        IResetableIterator<TextAnnotation> {

    private static Logger log = LoggerFactory.getLogger(TextAnnotationReader.class);

    protected final String corpusName;
    protected final ResourceManager resourceManager;
    protected int currentAnnotationId;

    /**
     * ResourceManager must provide a value for {@link CorpusReaderConfigurator}.CORPUS_NAME, plus
     * whatever is required by derived class for its initializeReader() and reset() methods.
     * 
     * @param rm ResourceManager with constructor arguments.
     */
    public TextAnnotationReader(ResourceManager rm) {
        this.resourceManager = rm;
        this.corpusName = rm.getString(CorpusReaderConfigurator.CORPUS_NAME.key);
        initializeReader();
        reset();
    }

    /**
     * called by constructor to perform subclass-specific initialization.
     */
    protected abstract void initializeReader();


    /**
     * override this to conform to whatever the derived class's state mechanism requires.
     */
    public void reset() {
        this.currentAnnotationId = 0;
    }

    public Iterator<TextAnnotation> iterator() {
        return this;
    }

    public TextAnnotation next() {
        try {
            TextAnnotation ta = makeTextAnnotation();
            currentAnnotationId++;

            return ta;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error creating TextAnnotation", e);
            return null;
        }
    }

    protected abstract TextAnnotation makeTextAnnotation() throws Exception;

    /**
     * @throws {@link UnsupportedOperationException} to let user know nothing happens.
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }


    /**
     * generate a human-readable report of annotations read from the source file (plus whatever
     * other relevant statistics the user should know about).
     */

    abstract public String generateReport();

}
