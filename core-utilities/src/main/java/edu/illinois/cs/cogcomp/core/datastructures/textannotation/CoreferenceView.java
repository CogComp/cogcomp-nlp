/**
 *
 */
package edu.illinois.cs.cogcomp.core.datastructures.textannotation;

import edu.illinois.cs.cogcomp.core.algorithms.Mappers;
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

    /**
     * Create a new CoreferenceView with default {@link #viewGenerator} and {@link #score}.
     */
    public CoreferenceView(String viewName, TextAnnotation text) {
        this(viewName, viewName+"-annotator", text, 1.0);
    }

    public CoreferenceView(String viewName, String viewGenerator, TextAnnotation text, double score) {
        super(viewName, viewGenerator, text, score);

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

    public void addCorefEdges(Constituent canonicalMention, List<Constituent> coreferentMentions, double[] scores) {
        this.addConstituent(canonicalMention);

        int canonicalMentionId = this.constituents.indexOf(canonicalMention);

        canonicalEntitiesMap.put(canonicalMentionId, canonicalMentionId);

        int i = 0;
        for (Constituent c : coreferentMentions) {
            if (c != canonicalMention) {

                this.addConstituent(c);

                Relation relation = new Relation("COREF", canonicalMention, c, scores[i]);

                this.addRelation(relation);
                int cId = this.constituents.indexOf(c);
                canonicalEntitiesMap.put(cId, canonicalMentionId);
            }
            i++;
        }
    }

    /**
     *
     */
    private void findCanonicalEntries() {

        if (this.canonicalEntitiesMap.size() > 0 || !this.modified)
            return;

        // The assumption is that all nodes with no parent are canonical
        // entities.
        for (Constituent cc : this.getConstituents()) {
            int ccId = this.constituents.indexOf(cc);

            if (cc.getIncomingRelations().size() == 0) {
                canonicalEntitiesMap.put(ccId, ccId);

                for (Relation r : cc.getOutgoingRelations()) {
                    int tId = this.constituents.indexOf(r.getTarget());
                    canonicalEntitiesMap.put(tId, ccId);
                }
            }
        }

        modified = false;
    }

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

    public Constituent getCanonicalEntity(Constituent c) {
        findCanonicalEntries();

        int cId = this.canonicalEntitiesMap.get(this.constituents.indexOf(c));

        return this.constituents.get(cId);

    }

    public List<Constituent> getCoreferentMentions(Constituent mention) {
        List<Constituent> myMentions = (List<Constituent>) this.where(Queries.sameSpanAsConstituent(mention));

        if (myMentions.size() == 0)
            return Collections.singletonList(mention);

        Constituent myMention = myMentions.get(0);

        Constituent canonicalEntity = getCanonicalEntity(myMention);

        return Mappers.map(canonicalEntity.getOutgoingRelations(), relationsToConstituents);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        Set<Constituent> entities = this.getCanonicalEntities();

        for (Constituent canonical : Sorters.sortSet(entities,
                TextAnnotationUtilities.constituentStartComparator)) {
            sb.append(canonical.toString()).append(" (").append(canonical.getStartSpan()).append(", ")
                    .append(canonical.getEndSpan()).append(")\n");

            for (Relation referant : canonical.getOutgoingRelations()) {
                sb.append("\t").append(referant.getTarget().toString()).append(" (").append(referant.getTarget()
                        .getStartSpan()).append(", ").append(referant.getTarget().getEndSpan()).append(") \n");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

}
