package org.cogcomp.md;

import edu.illinois.cs.cogcomp.lbjava.parse.Parser;

import java.util.List;

/**
 * Created by xuanyu on 7/15/2017.
 */
public class ArrayToParser implements Parser {
    private List readings;
    private int idx = 0;
    public ArrayToParser(List input){
        readings = input;
    }
    public void close(){}
    public Object next(){
        if (idx == readings.size()) {
            return null;
        } else {
            idx++;
            return readings.get(idx - 1);
        }
    }
    public void reset(){
        idx = 0;
    }
}
