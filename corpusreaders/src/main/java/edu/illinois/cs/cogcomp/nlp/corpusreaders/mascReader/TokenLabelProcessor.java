/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.mascReader;

import org.w3c.dom.Element;

import java.util.function.Function;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;

/**
 * A TokenLabelProcessor captures a rule of producing a token label from a token XML element
 * It has a viewName to indicate the target TextAnnotation view (e.g. ViewNames.POS),
 * and a processor function on a token XML element that returns a token label String
 *
 * @author Xiaotian Le
 */
class TokenLabelProcessor extends Pair<String, Function<Element, String>> {
    public TokenLabelProcessor(String viewName, Function<Element, String> processor) {
        super(viewName, processor);
    }

    public String getViewName() {
        return getFirst();
    }

    public Function<Element, String> getProcessor() {
        return getSecond();
    }
}
