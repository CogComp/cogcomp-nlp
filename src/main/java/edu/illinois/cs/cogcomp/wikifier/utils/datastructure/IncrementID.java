package main.java.edu.illinois.cs.cogcomp.wikifier.utils.datastructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IncrementID<T> {

    Map<T, Integer> idMap = new HashMap<T, Integer>();
    List<T> objectMap = new ArrayList<T>();

    public int id(T t) {
        Integer id = idMap.get(t);
        if (id == null) {
            int curId = idMap.size();
            idMap.put(t, curId);
            objectMap.add(t);
            return curId;
        }
        return id.intValue();
    }

    public String stringId(T t){
        return String.valueOf(id(t));
    }

    public T get(int id) {
        return objectMap.get(id);
    }

    public int size() {
        return idMap.size();
    }

    public void reset(){
        idMap.clear();
        objectMap.clear();
    }

}
