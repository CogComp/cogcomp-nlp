package edu.illinois.cs.cogcomp.finetyper.finer.datastructure.types;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by haowu4 on 5/15/17.
 */
public class TypeSystem {

    // IO related helper class.
    private static class TypeInfo {
        String parent;
        boolean is_figer_type;
    }

    private static class TypeInfos {
        Map<String, TypeInfo> typeInfos;
    }

    public static TypeSystem getFromJson(InputStream is) throws IOException {


        Gson gson = new GsonBuilder().create();
        TypeInfos load = null;
        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(is))) {
            load = gson.fromJson(reader, TypeInfos.class);
        }
        Map<String, FinerType> typeCollection = new HashMap<>();
        Set<FinerType> invisiableTypes = new HashSet<>();
        for (Map.Entry<String, TypeInfo> entry : load.typeInfos.entrySet()) {
            String typeName = entry.getKey();
            TypeInfo info = entry.getValue();
            FinerType type = new FinerType(typeName, info.is_figer_type);
            typeCollection.put(typeName, type);
        }

        for (Map.Entry<String, TypeInfo> entry : load.typeInfos.entrySet()) {
            String typeName = entry.getKey();
            TypeInfo info = entry.getValue();

            FinerType currentType = typeCollection.get(typeName);
            if (!currentType.isVisible()) {
                invisiableTypes.add(currentType);
            }
            String parentName = info.parent;
            if (parentName != null) {
                FinerType parentType = typeCollection.get(parentName);
                parentType.addChildren(currentType);
                currentType.setParent(parentType);
            }
        }

        TypeSystem typeSystem = new TypeSystem(typeCollection, invisiableTypes);

        for (FinerType type : typeCollection.values()) {
            type.setTypeSystem(typeSystem);
        }

        return typeSystem;
    }

    public TypeSystem(Map<String, FinerType> typeCollection, Set<FinerType> invisiableTypes) {
        this.typeCollection = typeCollection;
        this.invisiableTypes = invisiableTypes;
    }

    private Map<String, FinerType> typeCollection;
    private Set<FinerType> invisiableTypes;

    public FinerType getTypeOrFail(String name) {
        if (typeCollection.containsKey(name)) {
            FinerType t = typeCollection.get(name);
            if (t != null) {
                return t;
            } else {
                throw new RuntimeException("Type [" + name + "] is NULL.");
            }
        } else {
            throw new RuntimeException("Type [" + name + "] Not Found");
        }
    }

    public Set<String> failedQueries = new HashSet<>();

    public Optional<FinerType> getType(String name) {
        FinerType t = typeCollection.get(name);
        if (t == null) {
            if (!failedQueries.contains(name)) {
                failedQueries.add(name);
                System.err.println("Type [" + name + "] Not Found");
            }
            return Optional.empty();
        } else {
            return Optional.of(t);
        }
    }

}
