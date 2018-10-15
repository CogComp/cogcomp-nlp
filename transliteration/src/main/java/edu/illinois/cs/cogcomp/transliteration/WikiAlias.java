/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.transliteration;

import java.io.Serializable;

/**
 * Created by stephen on 9/24/15.
 */
public class WikiAlias implements Serializable {
    public String alias;
    public WikiTransliteration.AliasType type;
    public int count;

    public WikiAlias(String alias, WikiTransliteration.AliasType type, int count) {
        this.alias = alias;
        this.type = type;
        this.count = count;
    }
}
