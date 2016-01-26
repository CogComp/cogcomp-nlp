package edu.illinois.cs.cogcomp.core.datastructures.textannotation;

import java.io.Serializable;

/**
 * @author Vivek Srikumar
 *         <p/>
 *         Aug 4, 2009
 */
public class Relation implements Serializable {

    private static final long serialVersionUID = -1005341815252250162L;

    protected Constituent source;
    protected Constituent target;

    protected double score;
    protected final int relationName;

    public Relation(String relationName, Constituent source, Constituent target, double score) {

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

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof Relation))
            return false;

        Relation r = (Relation) obj;

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

    @Override
    public int hashCode() {
        return this.getRelationName().hashCode() * 79 + this.getSource().hashCode() * 7
                + this.getTarget().hashCode() * 13 + (new Double(this.getScore())).hashCode() * 17;
    }

    @Override
    public String toString() {
        return source + "--" + getRelationName() + "--> " + target + "(" + getScore() + ")";
    }
}
