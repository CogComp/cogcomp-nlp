/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package org.cogcomp.md;

import org.cogcomp.md.LbjGen.*;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;

import java.util.List;

/**
 * This is a Parser that reads a List of Objects and
 * returns all Objects in the form of Parser.
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
