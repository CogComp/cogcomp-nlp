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

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.IResetableIterator;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

/**
 * @author Vivek Srikumar
 *         <p>
 *         Sep 4, 2009
 */
public abstract class TextAnnotationReader implements Iterable<TextAnnotation>,
        IResetableIterator<TextAnnotation> {

    private static Logger log = LoggerFactory.getLogger(TextAnnotationReader.class);

    protected final String corpusName;
    protected int currentAnnotationId;

    public TextAnnotationReader(String corpusName) {
        this.corpusName = corpusName;
        initializeReader();
        reset();
    }

    protected abstract void initializeReader();

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

    public void remove() {}

}
