/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.utilities;

import edu.illinois.cs.cogcomp.core.algorithms.ProducerConsumer;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.stats.Counter;
import edu.illinois.cs.cogcomp.core.transformers.ITransformer;
import edu.illinois.cs.cogcomp.core.utilities.Table;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class TextStatistics extends ProducerConsumer<TextAnnotation> {

    private final FeatureExtractor fex;
    private final ITransformer<TextAnnotation, List<Constituent>> constituentGenerator;
    private AtomicInteger textCounter = new AtomicInteger(0);
    private AtomicInteger constituentCounter = new AtomicInteger(0);
    private Counter<String> counter = new Counter<>();

    public TextStatistics(Iterator<TextAnnotation> data, int numThreads, FeatureExtractor fex,
            ITransformer<TextAnnotation, List<Constituent>> constituentGenerator) {
        super(data, numThreads);
        this.fex = fex;
        this.constituentGenerator = constituentGenerator;
    }

    @Override
    protected void initialize() {}

    @Override
    protected boolean prerequisiteCheck(TextAnnotation ta) {
        return true;
    }

    @Override
    protected void consume(TextAnnotation ta) {

        for (Constituent c : constituentGenerator.transform(ta)) {
            try {
                Set<Feature> feats = fex.getFeatures(c);

                for (Feature feat : feats) {
                    count(feat);
                }
                constituentCounter.incrementAndGet();
            } catch (EdisonException e) {
                e.printStackTrace();
            }
        }
        textCounter.incrementAndGet();
    }

    private void count(Feature feat) {
        synchronized (counter) {
            counter.incrementCount(feat.toString());
        }
    }

    @Override
    protected String getStatus() {
        return "Seen " + textCounter.get() + " TextAnnotations, " + constituentCounter.get()
                + " constituents";
    }

    @Override
    protected List<TextAnnotation> process(TextAnnotation ta) {
        return Arrays.asList(ta);
    }

    public Table getResults() {
        Table table = new Table();

        table.addColumn("Item");
        table.addColumn("Count");

        for (String s : counter.getSortedItems())
            table.addRow(new String[] {s, "" + (int) (counter.getCount(s))});
        return table;
    }
}
