/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.PredicateArgumentView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.core.utilities.ArrayUtilities;
import edu.illinois.cs.cogcomp.nlp.utilities.ParseUtils;

import java.io.FileNotFoundException;
import java.util.*;

public abstract class AbstractSRLAnnotationReader extends PennTreebankReader {
    public static final String LemmaIdentifier = PredicateArgumentView.LemmaIdentifier;
    public static final String SenseIdentifier = PredicateArgumentView.SenseIdentifer;
    public static final String HyphenTagInfo = "HyphenTagInfo";

    static abstract class Fields {
        protected String section, wsjFileName, lemma, sense, identifier;

        int sentence, predicateTerminal;

        Fields(String line) {}

        String getSection() {
            return section;
        }

        String getIdentifier() {
            return identifier;
        }

        String getLemma() {
            return lemma;
        }

        String getWSJFileName() {
            return wsjFileName;
        }

        String getSense() {
            return sense;
        }

        int getSentence() {
            return sentence;
        }

        int getPredicateTerminal() {
            return predicateTerminal;
        }

        public abstract Constituent createPredicate(TextAnnotation ta, String viewName,
                List<Tree<Pair<String, IntPair>>> yield);

        public abstract List<? extends GoldLabel> getGoldLabels();

    }

    protected String srlViewName;
    protected boolean mergeContiguousCArgs;

    private Iterator<TextAnnotation> wsjIterator;

    private final Map<String, List<Fields>> goldFields;

    private final Set<String> sections;
    protected String dataHome;

    public AbstractSRLAnnotationReader(String treebankHome) throws Exception {
        super(treebankHome);
        throw new Exception("Invalid constructor.");
    }

    public AbstractSRLAnnotationReader(String treebankHome, String nombankHome, String srlViewName,
            boolean mergeContiguousCArgs) throws Exception {
        this(treebankHome, nombankHome, null, srlViewName, mergeContiguousCArgs);
    }

    public AbstractSRLAnnotationReader(String treebankHome, String nombankHome, String[] sections,
            String srlViewName, boolean mergeContiguousCArgs) throws Exception {
        super(treebankHome, sections);
        this.dataHome = nombankHome;
        this.srlViewName = srlViewName;
        this.mergeContiguousCArgs = mergeContiguousCArgs;

        this.sections = new HashSet<>();
        if (sections != null) {
            this.sections.addAll(Arrays.asList(sections));
        }

        this.goldFields = new HashMap<>();
        readData();

        wsjIterator = null;
    }

    public AbstractSRLAnnotationReader(Iterable<TextAnnotation> list, String treebankHome,
            String nombankHome, String[] sections, String srlViewName, boolean mergeContiguousCArgs)
            throws Exception {
        super(treebankHome);
        this.srlViewName = srlViewName;
        this.mergeContiguousCArgs = mergeContiguousCArgs;

        this.dataHome = nombankHome;

        this.sections = new HashSet<>();
        if (sections != null) {
            this.sections.addAll(Arrays.asList(sections));
        }

        this.goldFields = new HashMap<>();
        readData();
        this.wsjIterator = list.iterator();
    }

    private void readData() throws NumberFormatException, FileNotFoundException {

        String nombankFile = this.getDataFile(dataHome);
        for (String line : LineIO.read(nombankFile)) {

            Fields n = readFields(line);

            if (this.sections.contains(n.getSection())) {
                if (!this.goldFields.containsKey(n.getIdentifier())) {
                    this.goldFields.put(n.getIdentifier(), new ArrayList<Fields>());
                }

                this.goldFields.get(n.getIdentifier()).add(n);
            }
        }
    }

    public final boolean hasNext() {
        if (wsjIterator == null)
            return super.hasNext();
        else
            return this.wsjIterator.hasNext();
    }

    @Override
    public final TextAnnotation next() {
        TextAnnotation ta;
        if (wsjIterator == null)
            ta = super.next();
        else
            ta = wsjIterator.next();

        assert ta != null;

        if (this.goldFields.containsKey(ta.getId()))
            addAnnotation(ta);

        return ta;
    }

    private void addAnnotation(TextAnnotation ta) {
        Tree<String> tree = ParseUtils.getParseTree(ViewNames.PARSE_GOLD, ta, 0);
        Tree<Pair<String, IntPair>> spanLabeledTree = ParseUtils.getSpanLabeledTree(tree);

        List<Tree<Pair<String, IntPair>>> yield = spanLabeledTree.getYield();

        PredicateArgumentView pav =
                new PredicateArgumentView(srlViewName, "AnnotatedTreebank", ta, 1.0);

        Set<Integer> predicates = new HashSet<>();

        for (Fields fields : goldFields.get(ta.getId())) {
            Constituent predicate = fields.createPredicate(ta, srlViewName, yield);

            if (predicates.contains(predicate.getStartSpan()))
                continue;

            predicates.add(predicate.getStartSpan());

            List<Constituent> args = new ArrayList<>();
            List<String> labels = new ArrayList<>();
            List<Double> scores = new ArrayList<>();

            // We need to make sure that the One-Argument-Per-Span constraint is
            // respected. Yes sir, we do, even if the data says otherwise!
            Set<IntPair> seenSpans = new HashSet<>();

            for (GoldLabel arg : fields.getGoldLabels()) {

                List<Constituent> aa =
                        arg.getArgument(ta, srlViewName, yield, mergeContiguousCArgs);

                List<Constituent> filtered = new ArrayList<>();
                for (Constituent possibleArg : aa) {
                    if (seenSpans.contains(possibleArg.getSpan()))
                        continue;

                    seenSpans.add(possibleArg.getSpan());
                    filtered.add(possibleArg);
                }

                addArguments(ta, predicate, args, labels, scores, arg, filtered);
            }// for each arg

            pav.addPredicateArguments(predicate, args, labels.toArray(new String[labels.size()]),
                    ArrayUtilities.asDoubleArray(scores));

        }

        if (pav.getPredicates().size() > 0)
            ta.addView(srlViewName, pav);
    }

    private void addArguments(TextAnnotation ta, Constituent predicate, List<Constituent> args,
            List<String> labels, List<Double> scores, GoldLabel arg, List<Constituent> aa) {
        String label = arg.label;

        label = convertToCoNLL(label);

        if (label.equals("rel")) {
            for (Constituent c : aa) {
                if (c.getStartSpan() == predicate.getStartSpan()
                        && c.getEndSpan() == predicate.getEndSpan())
                    continue;
                else if (c.getStartSpan() <= predicate.getStartSpan()
                        && c.getEndSpan() <= predicate.getEndSpan()) {
                    int c1Start = c.getStartSpan();
                    int c1End = predicate.getStartSpan();

                    if (c1Start != c1End) {
                        args.add(new Constituent("C-V", srlViewName, ta, c1Start, c1End));
                        labels.add("C-V");
                        scores.add(1.0);
                    }

                    int c2Start = predicate.getEndSpan();
                    int c2End = c.getEndSpan();

                    if (c2Start != c2End) {
                        args.add(new Constituent("C-V", srlViewName, ta, c2Start, c2End));
                        labels.add("C-V");
                        scores.add(1.0);
                    }
                } else if (c.getStartSpan() == predicate.getStartSpan()
                        && c.getEndSpan() > predicate.getEndSpan()) {
                    int start = predicate.getEndSpan();
                    int end = c.getEndSpan();
                    args.add(new Constituent("C-V", srlViewName, ta, start, end));
                    labels.add("C-V");
                    scores.add(1.0);
                } else {
                    args.add(new Constituent("C-V", srlViewName, ta, c.getStartSpan(), c
                            .getEndSpan()));
                    labels.add("C-V");
                    scores.add(1.0);
                }
            }

        } else {
            for (Constituent c : aa) {
                args.add(c);
                labels.add(convertToCoNLL(c.getLabel()));
                scores.add(1.0);
            }
        }
    }

    private String convertToCoNLL(String label) {
        label = label.replaceAll("ARG", "A");
        label = label.replace("Support", "SUP");
        return label;
    }

    protected abstract Fields readFields(String line);

    protected abstract String getDataFile(String dataHome);

}


class GoldLabel {
    final String propSpanInfo;
    final String label;

    // Applicable only for Nombank
    final String h;
    final String field;

    GoldLabel(String field) {
        this.field = field;
        int firstDash = field.indexOf('-');

        propSpanInfo = field.substring(0, firstDash);
        String l =
                field.substring(firstDash + 1).replaceAll("ARG", "A").replaceAll("Support", "SUP");

        int hIndex = l.indexOf("-H");
        if (hIndex > 0) {
            h = l.substring(hIndex);
            l = l.substring(0, hIndex);
        } else {
            h = null;
        }

        if (l.startsWith("AM")) {
            firstDash = l.indexOf('-');
            int lastDash = l.lastIndexOf('-');

            if (firstDash != lastDash) {
                label = l.substring(0, lastDash);
            } else
                label = l;
        } else {
            firstDash = l.indexOf('-');
            if (firstDash > 0)
                label = l.substring(0, firstDash);
            else
                label = l;
        }

    }

    List<Constituent> getArgument(TextAnnotation ta, String viewName,
            List<Tree<Pair<String, IntPair>>> yield, boolean mergeContiguousCArgs) {

        String[] parts = propSpanInfo.split("\\*");

        List<Pair<IntPair, Boolean>> spans = new ArrayList<>();

        boolean someR = false;
        for (String part : parts) {

            if (part.length() == 0)
                continue;

            for (String s : part.split(",")) {
                if (s.length() == 0)
                    continue;

                Pair<String, IntPair> info = getSpan(s, yield);

                String nonTerminal = info.getFirst();
                IntPair span = info.getSecond();

                if (span.getFirst() < 0 || span.getFirst() >= span.getSecond())
                    continue;

                boolean r = false;
                if (nonTerminal.startsWith("WH")) {
                    r = true;
                    someR = true;
                }
                spans.add(new Pair<>(span, r));

            }

        }

        Collections.sort(spans, new Comparator<Pair<IntPair, Boolean>>() {

            @Override
            public int compare(Pair<IntPair, Boolean> arg0, Pair<IntPair, Boolean> arg1) {
                if (arg0.getFirst().getFirst() < arg1.getFirst().getFirst())
                    return -1;
                else if (arg0.getFirst().getFirst() == arg1.getFirst().getFirst())
                    return 0;
                else
                    return 1;
            }
        });

        if (!someR && mergeContiguousCArgs) {
            spans = mergeCArgs(spans);
        }

        boolean first = true;
        List<Constituent> arg = new ArrayList<>();
        for (Pair<IntPair, Boolean> item : spans) {
            String label = this.label;

            if (item.getSecond() && spans.size() > 1) {
                label = "R-" + label;
            } else {
                if (first) {
                    first = false;
                } else {
                    label = "C-" + label;
                }
            }

            Constituent constituent =
                    new Constituent(label, viewName, ta, item.getFirst().getFirst(), item
                            .getFirst().getSecond());

            if (h != null) {
                constituent.addAttribute(AbstractSRLAnnotationReader.HyphenTagInfo, h);
            }
            arg.add(constituent);

        }

        return arg;
    }

    protected List<Pair<IntPair, Boolean>> mergeCArgs(List<Pair<IntPair, Boolean>> spans) {

        if (spans.size() <= 1)
            return spans;

        List<Pair<IntPair, Boolean>> list = new ArrayList<>();

        IntPair prev = null;

        boolean r = true;

        for (Pair<IntPair, Boolean> p : spans) {
            if (prev == null) {
                prev = p.getFirst();
                r = p.getSecond();
            } else {

                if (p.getFirst().getFirst() == prev.getSecond()) {
                    prev = new IntPair(prev.getFirst(), p.getFirst().getSecond());
                    r &= p.getSecond();
                } else {
                    list.add(new Pair<>(prev, r));
                    prev = p.getFirst();
                    r = p.getSecond();
                }
            }
        }

        list.add(new Pair<>(prev, r));

        assert list.size() <= spans.size();

        if (spans.size() > 0)
            assert list.size() > 0;

        return list;
    }

    private Pair<String, IntPair> getSpan(String s, List<Tree<Pair<String, IntPair>>> yield) {

        String[] parts = s.split(":");

        assert parts.length == 2;

        int terminalNumber = Integer.parseInt(parts[0]);
        int height = Integer.parseInt(parts[1]);

        Tree<Pair<String, IntPair>> leaf = yield.get(terminalNumber);
        Tree<Pair<String, IntPair>> node = leaf.getParent();

        while (height > 0) {
            node = node.getParent();
            height--;
        }

        return node.getLabel();

    }

    @Override
    public String toString() {
        return "GoldLabel [propSpanInfo=" + propSpanInfo + ", label=" + label + ", h=" + h + "]";
    }

}
