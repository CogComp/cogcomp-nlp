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
