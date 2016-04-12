package edu.illinois.cs.cogcomp.core.datastructures.textannotation;

import edu.illinois.cs.cogcomp.core.algorithms.Mappers;
import edu.illinois.cs.cogcomp.core.algorithms.ProducerConsumer;
import edu.illinois.cs.cogcomp.core.algorithms.Sorters;
import edu.illinois.cs.cogcomp.core.transformers.ITransformer;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.procedure.TIntProcedure;

import java.util.*;

/**
 * @author Vivek Srikumar
 */

public class CoreferenceView extends View {

    private static final long serialVersionUID = -5490913231260663181L;

    // TODO: remove this
    protected TIntIntHashMap canonicalEntitiesMap;

    private static ITransformer<Relation, Constituent> relationsToConstituents;

    boolean modified = false;

    static {
        relationsToConstituents = new ITransformer<Relation, Constituent>() {

            @Override
            public Constituent transform(Relation arg0) {
                return arg0.getTarget();
            }
        };
    }

    private List<Constituent> canonicalMentions;

    /**
     * Create a new CoreferenceView with default {@link #viewGenerator} and {@link #score}.
     */
    public CoreferenceView(String viewName, TextAnnotation text) {
        this(viewName, viewName + "-annotator", text, 1.0);
    }

    public CoreferenceView(String viewName, String viewGenerator, TextAnnotation text, double score) {
        super(viewName, viewGenerator, text, score);
        canonicalMentions = new ArrayList<>();
        canonicalEntitiesMap = new TIntIntHashMap();
    }

    @Override
    public void addConstituent(Constituent constituent) {
        this.modified = true;
        super.addConstituent(constituent);
    }

    @Override
    public void addRelation(Relation relation) {
        this.modified = true;
        super.addRelation(relation);
    }

    /**
     * @param canonicalMention: The assumption is that all nodes with no parent are canonical entities.
     * @param coreferentMentions
     */
    public void addCorefEdges(Constituent canonicalMention, List<Constituent> coreferentMentions) {
        double[] scores = new double[coreferentMentions.size()];
        Arrays.fill(scores, 1.0);
        addCorefEdges(canonicalMention, coreferentMentions, scores);
    }

    public void addCorefEdges(Constituent canonicalMention, List<Constituent> coreferentMentions,
            double[] scores) {
        this.addConstituent(canonicalMention);

        int canonicalMentionId = this.constituents.indexOf(canonicalMention);

        canonicalEntitiesMap.put(canonicalMentionId, canonicalMentionId);
        canonicalMentions.add(canonicalMention);

        int i = 0;
        for (Constituent c : coreferentMentions) {
            if (c != canonicalMention) {

                this.addConstituent(c);

                Relation relation = new Relation(viewName, canonicalMention, c, scores[i]);

                this.addRelation(relation);
                int cId = this.constituents.indexOf(c);
                canonicalEntitiesMap.put(cId, canonicalMentionId);
            }
            i++;
        }
    }

    /**
     * Finds the representative elements for each cluster
     */
    @Deprecated
    private void findCanonicalEntries() {

        if (this.canonicalEntitiesMap.size() > 0 || !this.modified)
            return;

        // The assumption is that all nodes with no parent are canonical entities.
        for (Constituent cc : this.getConstituents()) {
            int ccId = this.constituents.indexOf(cc);

            if (getFilteredIncomingRelations(cc).size() == 0) {
                canonicalEntitiesMap.put(ccId, ccId);
                for (Relation r : getFilteredOutgoingRelations(cc)) {
                    int tId = this.constituents.indexOf(r.getTarget());
                    canonicalEntitiesMap.put(tId, ccId);
                }
            }
        }

        modified = false;
    }

    @Deprecated
    public Set<Constituent> getCanonicalEntities() {

        findCanonicalEntries();

        final Set<Constituent> cc = new HashSet<>();

        this.canonicalEntitiesMap.forEachKey(new TIntProcedure() {
            @Override
            public boolean execute(int value) {
                return cc.add(constituents.get(value));
            }
        });

        return cc;
    }

    public List<Constituent> getCanonicalEntitiesViaRelations() {
        return canonicalMentions;
    }

    /**
     * Given a constituent, it returns the canonical constituent which represents the cluster.
     */
    @Deprecated
    public Constituent getCanonicalEntity(Constituent c) {
        findCanonicalEntries();

        int cId = this.canonicalEntitiesMap.get(this.constituents.indexOf(c));

        return this.constituents.get(cId);
    }

    /**
     * Given a constituent, it returns the canonical constituent which represents the cluster.
     */
    public Constituent getCanonicalEntityViaRelation(Constituent c) {
        List<Relation> incomingRelations = getFilteredIncomingRelations(c);

        // The assumption is that all nodes with no parent are canonical entities.
        if(incomingRelations.isEmpty())
            return c;
        if(incomingRelations.size() > 1)
            System.out.println("Warning: constituent belongs to more than one cluster; we returned only one of them.\n" +
                    "If you are deadline with overlapping clusters, and want to get all of the canonical elements, " +
                    "\"getCanonicalEntitySetViaRelation\" function. ");
        return incomingRelations.get(0).source;
    }

    /**
     * Given a constituent, it returns the canonical constituents which represents the cluster.
     */
    public Set<Constituent> getCanonicalEntitySetViaRelation(Constituent c) {
        List<Relation> incomingRelations = getFilteredIncomingRelations(c);

        // The assumption is that all nodes with no parent are canonical entities.
        if(incomingRelations.isEmpty())
            return new HashSet(Arrays.asList(c));
        Set<Constituent> canonical = new HashSet<>();
        for(Relation r: incomingRelations)
            canonical.add(r.source);
        return canonical;
    }

    /**
     * Given a mention, it returns the list of canonical mentions of the coref chains which
     * overlap with the input constituent
     * @param c the input constituent
     * @return canonical consittuents of the overlalpping chains with the input constituent
     */
    public HashSet<Constituent> getOverlappingChainsCanonicalMentions(Constituent c) {
        List<Constituent> overlappingCons = c.getView().getConstituentsCovering(c);
        HashSet<Constituent> canonicalCons = new HashSet<>();
        for(Constituent cc : overlappingCons)
            canonicalCons.addAll(getCanonicalEntitySetViaRelation(cc));
        return canonicalCons;
    }

    /** given the canonical constituent, returns back the constitunts connected to it.
     * @param mention canonical mention
     * @return the connected constituents
     */
    @Deprecated
    public List<Constituent> getCoreferentMentions(Constituent mention) {
        List<Constituent> myMentions =
                (List<Constituent>) this.where(Queries.sameSpanAsConstituent(mention));

        if (myMentions.size() == 0)
            return Collections.singletonList(mention);

        // TODO: this would create problem in overlapping mentions
        Constituent myMention = myMentions.get(0);

        Constituent canonicalEntity = getCanonicalEntity(myMention);

        return Mappers.map(getFilteredOutgoingRelations(canonicalEntity), relationsToConstituents);
    }

    /**
     * Returns ALL the constituents in a single chain characterized by its canonical constituent
     */
    public Set<Constituent> getCoreferentMentionsViaRelations(Constituent mention) {
        Set<Constituent> canonicalMentionSet = getCanonicalEntitySetViaRelation(mention);
        Set<Constituent> coreferentMentions = new HashSet<>();
        for(Constituent c : canonicalMentionSet) {
            for(Relation r: getFilteredOutgoingRelations(c)) {
                coreferentMentions.add(r.target);
            }
        }
        coreferentMentions.addAll(canonicalMentionSet);
        return coreferentMentions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        Set<Constituent> entities = this.getCanonicalEntities();

        for (Constituent canonical : Sorters.sortSet(entities,
                TextAnnotationUtilities.constituentStartComparator)) {
            sb.append(canonical.toString()).append(" (").append(canonical.getStartSpan())
                    .append(", ").append(canonical.getEndSpan()).append(")\n");

            for (Relation referant : getFilteredOutgoingRelations(canonical)) {
                sb.append("\t").append(referant.getTarget().toString()).append(" (")
                        .append(referant.getTarget().getStartSpan()).append(", ")
                        .append(referant.getTarget().getEndSpan()).append(") \n");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /** Since all the relations are being populated inside the View class, we need to filter out the relations which
     * are irrelevant to coreference. This function, given a constituent, returns all of the outgoing coreference
     * relations.
     */
    public List<Relation> getFilteredOutgoingRelations(Constituent c) {
        List<Relation> filteredOutgoingRelations = new ArrayList<>();
        for(Relation r : c.getOutgoingRelations() ) {
            if (!Objects.equals(r.getRelationName(), viewName))
                continue;
            filteredOutgoingRelations.add(r);
        }
        return filteredOutgoingRelations;
    }

    public List<Relation> getFilteredIncomingRelations(Constituent c) {
        List<Relation> filteredIncomingRelations = new ArrayList<>();
        for(Relation r : c.getIncomingRelations() ) {
            if (!Objects.equals(r.getRelationName(), viewName))
                continue;
            filteredIncomingRelations.add(r);
        }
        return filteredIncomingRelations;
    }
}
