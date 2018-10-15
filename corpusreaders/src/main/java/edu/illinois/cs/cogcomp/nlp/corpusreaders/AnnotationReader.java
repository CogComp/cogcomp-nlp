/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.IResetableIterator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

/**
 * A fairly generic iterator that generates some kind of Annotation objects from some input source.
 *
 * @author Vivek Srikumar
 * @author mssammon
 */
public abstract class AnnotationReader<T> implements Iterable<T>,
        IResetableIterator<T> {

    private static Logger log = LoggerFactory.getLogger(AnnotationReader.class);

    protected final String corpusName;
    protected final ResourceManager resourceManager;
    protected int currentAnnotationId;

    public AnnotationReader(){
        this.resourceManager = null;
        this.corpusName = null;
        log.error("Cannot use default constructor in AnnotationReader() class. Exiting...");
    }

    /**
     * ResourceManager must provide a value for {@link CorpusReaderConfigurator}.CORPUS_NAME, plus
     * whatever is required by derived class for its initializeReader() and reset() methods.
     * 
     * @param rm ResourceManager with constructor arguments.
     */
    public AnnotationReader(ResourceManager rm) {
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


    public Iterator<T> iterator() {
        return this;
    }


    /**
     * is there another annotation object to return?
     * @return 'true' if there is at least one more annotation, 'false' otherwise
     */
    public abstract boolean hasNext();


    /**
     * return the next annotation object. Don't forget to increment currentAnnotationId.
     * @return an annotation object.
     */
    public abstract T next();


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
