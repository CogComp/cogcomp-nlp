/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader;

import java.io.Serializable;

public class Paragraph implements Serializable {

    public Paragraph() {}

    public Paragraph(int offset, String content) {
        this.offset = offset;
        this.content = content;
    }

    public int offset = -1;

    public int offsetFilterTags = -1;

    public String content;
}
