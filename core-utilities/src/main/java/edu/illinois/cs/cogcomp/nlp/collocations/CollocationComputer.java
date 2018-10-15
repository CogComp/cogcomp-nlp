/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.collocations;

/**
 * @author vivek
 */
public interface CollocationComputer {
    double getCount(String str) throws Exception;

    double getCollocationScore(String left, String right) throws Exception;
}
