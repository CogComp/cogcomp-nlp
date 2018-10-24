/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.transliteration;


/**
 * This is mainly intended to introduce clarity, not add any functionality.
 * Created by mayhew2 on 11/5/15.
 */
public class Production {

    /**
     * Source segment
     */
    String segS;

    /**
     * Target segment
     */
    String segT;

    public String getFirst(){
        return segS;
    }

    public String getSecond(){
        return segT;
    }

    public int getOrigin(){
        return origin;
    }

    public void setOrigin(int o){
        this.origin = o;
    }

    /**
     * Language of origin (will just be an ID, won't actually know the country)
     * -1 is the default id.
     */
    int origin;

    public Production(String segS, String segT) {
        this(segS, segT, -1);
    }

    public Production(String segS, String segT, int origin) {
        this.segS = segS;
        this.segT = segT;
        this.origin = origin;
    }

    @Override
    public int hashCode() {
        int result = segS.hashCode();
        result = 31 * result + segT.hashCode();
        result = 31 * result + origin;
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Production that = (Production) o;

        if (origin != that.origin) return false;
        if (segS != null ? !segS.equals(that.segS) : that.segS != null) return false;
        return !(segT != null ? !segT.equals(that.segT) : that.segT != null);

    }

    @Override
    public String toString() {
        return "Production{" + segS + " : " + segT  + ", " + origin + '}';
    }
}
