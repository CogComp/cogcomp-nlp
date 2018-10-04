/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.datastructures.textannotation;

import com.google.common.collect.Maps;
import edu.illinois.cs.cogcomp.core.datastructures.HasAttributes;
import edu.illinois.cs.cogcomp.core.math.ArgMax;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Vivek Srikumar
 *         <p>
 *         Aug 4, 2009
 */
public class Relation implements Serializable, HasAttributes {

    private static final long serialVersionUID = -1005341815252250162L;
    protected final int relationName;
    protected final Map<String, Double> labelsToScores;
    protected Constituent source;
    protected Constituent target;
    protected double score;
    protected Map<String, String> attributes;


    /**
     * Instantiates a Relation connecting two constituents with a set of labels and corresponding scores.
     * The 'main' label and score that will be returned by {@link #getRelationName()} and {@link #getScore()} will
     *    be the argmax and corresponding score from the labelsToScores argument.
     * @param labelsToScores map from labels to scores.
     * @param source constituent that is the source of the relation
     * @param target constituent that is the target of the relation
     */
    public Relation( Map<String, Double> labelsToScores, Constituent source, Constituent target ) {
        this(labelsToScores, new ArgMax<>(labelsToScores).getArgmax(), new ArgMax<>(labelsToScores).getMaxValue(),
                source, target);
    }


    /**
     * Instantiates a Relation connecting two Constituents with the name and score specified.
     *
     * @param relationName name of relation
     * @param source constituent that is the source of the relation
     * @param target constituent that is the target of the relation
     * @param score confidence score for relation
     */
    public Relation(String relationName, Constituent source, Constituent target, double score) {
        this( null, relationName, score, source, target );
    }


    /**
     * private constructor to enable immutable label to score map
     *
     * @param labelsToScores map from labels to scores.
     * @param relationName name of relation
     * @param source constituent that is the source of the relation
     * @param target constituent that is the target of the relation
     * @param score confidence score for relation
     */
    private Relation(Map<String, Double> labelsToScores, String relationName, double score, Constituent source, Constituent target) {

        if ( null != labelsToScores) {
            this.labelsToScores = Maps.newHashMap();
            this.labelsToScores.putAll(labelsToScores);
        }
        else
            this.labelsToScores = null;

        TextAnnotation ta = source.getTextAnnotation();
        assert ta == target.getTextAnnotation();

        if (relationName == null)
            relationName = "";
        int r = ta.symtab.getId(relationName);
        if (r == -1)
            r = ta.symtab.add(relationName);

        this.relationName = r;
        this.source = source;
        this.target = target;
        this.score = score;

        this.source.registerRelationSource(this);
        this.target.registerRelationTarget(this);
    }


    /**
     * return map of labels to scores. If not explicitly created, generates a trivial distribution
     *    using the label assigned at construction.
     *
     * @return map of labels to scores
     */
    public Map<String, Double> getLabelsToScores()
    {
        Map<String, Double> returnMap = null;
        if ( null != labelsToScores) {
            returnMap = new HashMap<>();
            returnMap.putAll(labelsToScores);
        }
        return returnMap;
    }



    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof Relation))
            return false;


        Relation r = (Relation) obj;

        if (this.attributes == null && r.attributes != null)
            return false;
        if (this.attributes != null && r.attributes == null)
            return false;

        if (this.attributes != null && r.attributes != null)
            if (!this.attributes.equals(r.attributes))
                return false;

        if (this.labelsToScores == null && r.labelsToScores != null)
            return false;
        if (this.labelsToScores != null && r.labelsToScores == null)
            return false;
        if (this.labelsToScores != null && r.labelsToScores != null)
            if (!this.labelsToScores.equals(r.labelsToScores))
                return false;

        return r.getRelationName().equals(this.relationName)
                && r.getSource().equals(this.getSource()) && r.getTarget().equals(this.getTarget())
                && r.getScore() == this.getScore();

    }

    public String getRelationName() {
        return source.getTextAnnotation().symtab.getLabel(relationName);
    }

    /**
     * @return the score
     */
    public double getScore() {
        return score;
    }

    /**
     * @return the source
     */
    public Constituent getSource() {
        return source;
    }

    /**
     * @return the target
     */
    public Constituent getTarget() {
        return target;
    }


    public void addAttribute(String key, String value )
    {
        if ( null == attributes )
            attributes = new HashMap<>();

        attributes.put( key, value );
    }


    public String getAttribute(String key) {
        if (attributes == null)
            return null;
        else
            return attributes.get(key);
    }

    public Set<String> getAttributeKeys() {

        if (this.attributes == null)
            return new HashSet<>();
        else
            return this.attributes.keySet();
    }

    public boolean hasAttribute(String key) {
        return this.attributes != null && this.attributes.containsKey(key);
    }

    /**
     * Removes all attributes from a Constituent.
     */
    public void removeAllAttributes() {
        this.attributes = null;
    }


    @Override
    public int hashCode() {
        int hashCode =  this.getRelationName().hashCode() * 79 + this.getSource().hashCode() * 7
                + this.getTarget().hashCode() * 13 + (new Double(this.getScore())).hashCode() * 17;
        hashCode += (this.attributes == null ? 0 : this.attributes.hashCode() * 13);
        hashCode += (this.labelsToScores == null ? 0 : this.labelsToScores.hashCode() * 23);

        return hashCode;
    }

    @Override
    public String toString() {
        StringBuilder bldr = new StringBuilder( source.toString() );
        bldr.append( "--" ).append( getRelationName() ).append("--> ");
        bldr.append( target ).append("(").append( getScore() ).append( ")" );
        return bldr.toString();
    }

}
