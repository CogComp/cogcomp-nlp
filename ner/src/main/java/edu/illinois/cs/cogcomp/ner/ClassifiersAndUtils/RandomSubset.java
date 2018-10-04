/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.ClassifiersAndUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Vector;

public class RandomSubset {

    private Logger logger = LoggerFactory.getLogger(RandomSubset.class);

    private Vector<Document> docs = null;
    private boolean[] mask;// mask[i]=false means that the current element was not allocated yet and
                           // is free for subsampling
    private int remainingCapacity = -1;

    public RandomSubset(DocumentCollection _docs) {
        docs = _docs.docs;
        mask = new boolean[docs.size()];
        remainingCapacity = mask.length;
    }

    public DocumentCollection getRandomSubset(int setSize) {
        // logger.info("Building random sample");
        DocumentCollection res = new DocumentCollection();
        if (setSize > remainingCapacity) {
            System.err
                    .println("Error-requested random subset size exceeds the available set capacity");
            System.exit(0);
        }
        while (res.docs.size() < setSize) {
            int i = (int) (Math.random() * ((double) mask.length));
            if (i == mask.length)
                i--;
            if (isAvailable(i)) {
                mask[i] = true;
                remainingCapacity--;
                res.docs.add(docs.elementAt(i));
            }
        }
        // logger.info("Done building random sample");
        return res;
    }

    public DocumentCollection getBalancedRandomSubset(int classesNum, int numSamplesPerClass) {
        logger.info("Building random sample");
        int[] availableCounts = new int[classesNum];
        for (int i = 0; i < this.docs.size(); i++)
            if (isAvailable(i))
                availableCounts[docs.elementAt(i).classID]++;
        for (int i = 0; i < classesNum; i++) {
            if (availableCounts[i] < numSamplesPerClass) {
                System.err.println("Cannot build a balances sample- missing enough elements for one of the classes");
                System.exit(0);
            }
            availableCounts[i] = numSamplesPerClass;
        }
        DocumentCollection res = new DocumentCollection();
        while (res.docs.size() < classesNum * numSamplesPerClass) {
            int i = (int) (Math.random() * ((double) mask.length));
            if (i == mask.length)
                i--;
            if (isAvailable(i) && (availableCounts[docs.elementAt(i).classID] > 0)) {
                mask[i] = true;
                availableCounts[docs.elementAt(i).classID]--;
                remainingCapacity--;
                res.docs.add(docs.elementAt(i));
            }
        }
        logger.info("Done building random sample");
        return res;
    }

    public boolean isAvailable(int i) {
        return (!mask[i]);
    }

    public void reset() {
        mask = new boolean[docs.size()];
        remainingCapacity = mask.length;
    }

}
