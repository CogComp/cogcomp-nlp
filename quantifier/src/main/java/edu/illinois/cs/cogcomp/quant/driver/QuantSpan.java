/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.quant.driver;

import java.io.*;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.quant.standardize.Date;
import edu.illinois.cs.cogcomp.quant.standardize.DateRange;
import edu.illinois.cs.cogcomp.quant.standardize.Quantity;
import edu.illinois.cs.cogcomp.quant.standardize.Range;
import edu.illinois.cs.cogcomp.quant.standardize.Ratio;


public class QuantSpan implements Serializable {

    private static final long serialVersionUID = -5787092712439863265L;
    public int start, end;
    public Object object;

    public QuantSpan(Object object, int start, int end) {
        this.object = object;
        this.start = start;
        this.end = end;
    }

    public String toString() {
        IntPair ip = new IntPair(start, end);
        if (object instanceof Date) {
            return ((Date) object).toString() + ":" + ip;
        }
        if (object instanceof DateRange) {
            return ((DateRange) object).toString() + ":" + ip;
        }
        if (object instanceof Ratio) {
            return ((Ratio) object).toString() + ":" + ip;
        }
        if (object instanceof Range) {
            return ((Range) object).toString() + ":" + ip;
        }
        if (object instanceof Quantity) {
            return ((Quantity) object).toString() + ":" + ip;
        }
        return "";
    }
}
