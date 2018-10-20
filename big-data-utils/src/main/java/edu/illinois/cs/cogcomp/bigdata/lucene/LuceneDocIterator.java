/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.bigdata.lucene;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;

public class LuceneDocIterator implements Iterator<Document> {

    private IndexReader reader;
    private int pointer;
    private int max;
    private final Set<String> fieldsToLoad;
    
    public LuceneDocIterator(IndexReader reader){
        this(reader,null);
    }

    public LuceneDocIterator(IndexReader reader,Set<String> fieldsToLoad) {
        this.reader = reader;
        this.fieldsToLoad = fieldsToLoad;
        pointer = 0;
        max = reader.numDocs();
    }

    @Override
    public boolean hasNext() {
        return pointer < max;
    }

    @Override
    public Document next() {
        Document doc = null;
        try {
            if(fieldsToLoad!=null)
                doc = reader.document(pointer, fieldsToLoad);
            else
                doc = reader.document(pointer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        pointer++;
        return doc;
    }

    @Override
    public void remove() {

    }
}
