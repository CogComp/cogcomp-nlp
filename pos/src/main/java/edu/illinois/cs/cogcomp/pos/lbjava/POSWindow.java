/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D5091DA028040154F75E6F0982B921D36B9DF241424FC2939C09CE6CEEAD601EFB7EAA468F8776ECC9B0351BDB91274B6C9F479B2BAA4BF8B0DF0250FAD4590E092E3065E0C213DC19F0BBD0A92DAB9284CD5B14CCD7CDA403E09367882A11955E05D63D84801C94FAB9EE6CE94688992A7B398E61A9322D143E3B7A1A71BE6DEC2B43D146EFFC4336F359CE18EF42478C50B1A2381C292B406120BF0F482ACAB6231BF4283ABF20F90CC93602100000

package edu.illinois.cs.cogcomp.pos.lbjava;

import edu.illinois.cs.cogcomp.lbjava.classify.*;
import edu.illinois.cs.cogcomp.lbjava.infer.*;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.lbjava.learn.*;
import edu.illinois.cs.cogcomp.lbjava.nlp.*;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;
import edu.illinois.cs.cogcomp.lbjava.parse.*;
import edu.illinois.cs.cogcomp.pos.*;


/**
 * Feature generator that senses the parts of speech of the four context words immediately
 * surrounding the target word (two before and two after).
 *
 * @author Nick Rizzolo
 **/
public class POSWindow extends Classifier {
    private static final POSTagger __POSTagger = new POSTagger();

    public POSWindow() {
        containingPackage = "edu.illinois.cs.cogcomp.pos.lbjava";
        name = "POSWindow";
    }

    public String getInputType() {
        return "edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token";
    }

    public String getOutputType() {
        return "discrete%";
    }

    public FeatureVector classify(Object __example) {
        if (!(__example instanceof Token)) {
            String type = __example == null ? "null" : __example.getClass().getName();
            System.err
                    .println("Classifier 'POSWindow(Token)' defined on line 31 of POS.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        Token word = (Token) __example;

        FeatureVector __result;
        __result = new FeatureVector();
        String __id;
        String __value;

        int i;
        Token w = word, last = word;
        for (i = 0; i <= 2 && last != null; ++i) {
            last = (Token) last.next;
        }
        for (i = 0; i > -2 && w.previous != null; --i) {
            w = (Token) w.previous;
        }
        for (; w != last; w = (Token) w.next) {
            __id = "" + (i++);
            __value = "" + (__POSTagger.discreteValue(w));
            __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage,
                    this.name, __id, __value, valueIndexOf(__value), (short) 0));
        }
        return __result;
    }

    public FeatureVector[] classify(Object[] examples) {
        if (!(examples instanceof Token[])) {
            String type = examples == null ? "null" : examples.getClass().getName();
            System.err
                    .println("Classifier 'POSWindow(Token)' defined on line 31 of POS.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        return super.classify(examples);
    }

    public int hashCode() {
        return "POSWindow".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof POSWindow;
    }
}
