package edu.illinois.cs.cogcomp.finetyper.finer.datastructure.types;

import net.sf.extjwnl.data.Synset;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by haowu4 on 5/15/17.
 */
public class FinerType {
    private TypeSystem typeSystem;
    private String type;
    private boolean isVisible;
    private FinerType parent;
    private List<FinerType> children;
    private List<Synset> wordnetIds;

    FinerType(String name, boolean isVisible) {
        this.typeSystem = null;
        this.type = name;
        this.isVisible = isVisible;
        this.parent = null;
        this.children = new ArrayList<>();
        this.wordnetIds = new ArrayList<>();
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public TypeSystem getTypeSystem() {
        return typeSystem;
    }

    public void setTypeSystem(TypeSystem typeSystem) {
        this.typeSystem = typeSystem;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public FinerType getParent() {
        return parent;
    }

    public void setParent(FinerType parent) {
        this.parent = parent;
    }

    public List<FinerType> getChildren() {
        return children;
    }

    public void addChildren(FinerType child) {
        this.children.add(child);
    }

    public boolean isParentOf(FinerType t) {
        FinerType it = t.parent;
        while (it != null) {
            if (it.equals(this)) {
                return true;
            }
            it = it.parent;
        }
        return false;
    }

    public boolean isParentOfOrEqual(FinerType t) {
        FinerType it = t;
        while (it != null) {
            if (it.equals(this)) {
                return true;
            }
            it = it.parent;
        }
        return false;
    }

    public boolean isChildOf(FinerType t) {
        return t.isParentOf(this);
    }

    public boolean isChildOfOrEqual(FinerType t) {
        return t.isParentOfOrEqual(this);
    }

    public boolean isCoarseType(FinerType t) {
        return this.parent == null;
    }

    public List<Synset> wordNetSenseIds() {
        return this.wordnetIds;
    }

    public void addWordNetSenseId(Synset senseId) {
        this.wordnetIds.add(senseId);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FinerType finerType = (FinerType) o;

        return type.equals(finerType.type);
    }


    @Override
    public int hashCode() {
        return type.hashCode();
    }
}
