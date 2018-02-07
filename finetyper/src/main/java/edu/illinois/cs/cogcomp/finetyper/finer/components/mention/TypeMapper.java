package edu.illinois.cs.cogcomp.finetyper.finer.components.mention;

import edu.illinois.cs.cogcomp.finetyper.finer.datastructure.types.FinerType;
import edu.illinois.cs.cogcomp.finetyper.finer.datastructure.types.TypeSystem;

import java.util.Map;

/**
 * Created by haowu4 on 5/17/17.
 * <p>
 * Map a given string to one of the type in a fine type taxonomy.
 */
public class TypeMapper {
    TypeSystem types;
    Map<String, String> mapping;

    public TypeMapper(TypeSystem types, Map<String, String> mapping) {
        this.types = types;
        this.mapping = mapping;
    }

    public FinerType getType(String label) {
        String tname = mapping.getOrDefault(label, null);
        if (tname != null)
            return types.getTypeOrFail(tname);
        else {
            return null;
        }
    }

}
