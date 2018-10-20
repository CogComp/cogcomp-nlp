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

import edu.illinois.cs.cogcomp.core.datastructures.Triple;

/**
 * A SpanLabelProcessor captures a rule of producing span labels from a particular kind of XML elements
 * It has an elementName to indicate the XML tag name for the span element (e.g. nchunk),
 * a viewName to indicate the target TextAnnotation view (e.g. ViewNames.SHALLOW_PARSE),
 * and a processor function on one such element that returns a span label String
 *
 * @author Xiaotian Le
 */
class SpanLabelProcessor extends Triple<String, String, Function<Element, String>> {
    public SpanLabelProcessor(String elementName, String viewName, Function<Element, String> processor) {
        super(elementName, viewName, processor);
    }

    public String getElementName() {
        return getFirst();
    }

    public String getViewName() {
        return getSecond();
    }

    public Function<Element, String> getProcessor() {
        return getThird();
    }
}
