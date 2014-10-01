package edu.illinois.cs.cogcomp.wikifier.utils.lucene;

import org.apache.lucene.document.FieldType;

public class Fields {
    
    public static FieldType FULL_INDEX = new FieldType();
    
    static{
        FULL_INDEX.setIndexed(true);
        FULL_INDEX.setTokenized(true);
        FULL_INDEX.setStored(false);
        FULL_INDEX.setStoreTermVectors(true);
        FULL_INDEX.setStoreTermVectorPositions(true);
        FULL_INDEX.freeze();
    }
    
}
