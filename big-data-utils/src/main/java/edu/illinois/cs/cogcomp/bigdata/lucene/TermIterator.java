/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.bigdata.lucene;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.SlowCompositeReaderWrapper;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;

import java.io.IOException;

/**
 * Iterates over all the terms 
 * @author cheng88
 *
 */
public abstract class TermIterator implements Runnable {

    private final Terms terms;

    public TermIterator(IndexReader reader, String fieldName) throws IOException {
        terms = SlowCompositeReaderWrapper.wrap(reader).terms(fieldName);
    }

    public void run() {
        TermsEnum te;
        try {
            te = terms.iterator();
            int i = 0;
            while (te.next() != null) {
                int freq = te.docFreq();
                String termString = te.term().utf8ToString();
                hasTerm(i++, termString, freq);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public long totalTermFreq() {
        try {
            return terms.getSumTotalTermFreq();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public abstract void hasTerm(int id, String surface, int docFreq);

}
