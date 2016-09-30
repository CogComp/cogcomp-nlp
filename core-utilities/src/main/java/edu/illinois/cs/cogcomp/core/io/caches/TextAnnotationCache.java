package edu.illinois.cs.cogcomp.core.io.caches;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.IResetableIterator;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;

/**
 * An interface for caching {@link TextAnnotation}s. Can be implemented with different technologies:
 * DB's, simple file storage, MapDB, etc.
 *
 * @author Christos Christodoulopoulos
 */
public interface TextAnnotationCache {
    void addTextAnnotation(String dataset, TextAnnotation ta);

    void updateTextAnnotation(TextAnnotation ta);

    IResetableIterator<TextAnnotation> getDataset(String dataset);

    boolean contains(TextAnnotation ta);

    void removeTextAnnotation(TextAnnotation ta);
}
