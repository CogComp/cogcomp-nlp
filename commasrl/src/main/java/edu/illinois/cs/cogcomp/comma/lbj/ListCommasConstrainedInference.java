/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D3C814A038034144FA23BC463DB0479E6AA8051C314883A686DFF129F5C541FEE5B28EE666ED3629C8CC49844B9A855ABC21A45A25C27842C1AEB0FCC030A7A8D1DA0EB83464CD356AD72B02ED62AD9A83F77C60CA9C6680754DD677D99BFF0FC62CB72D78AEBC97F8DE70C0FB3E2198000000

package edu.illinois.cs.cogcomp.comma.lbj;

import edu.illinois.cs.cogcomp.comma.datastructures.Comma;
import edu.illinois.cs.cogcomp.comma.datastructures.CommaSRLSentence;
import edu.illinois.cs.cogcomp.infer.ilp.OJalgoHook;
import edu.illinois.cs.cogcomp.lbjava.infer.*;
import edu.illinois.cs.cogcomp.lbjava.learn.*;


public class ListCommasConstrainedInference extends ILPInference {
    public static CommaSRLSentence findHead(Comma c) {
        return c.getSentence();
    }


    public ListCommasConstrainedInference() {}

    public ListCommasConstrainedInference(CommaSRLSentence head) {
        super(head, new OJalgoHook());
        constraint = new ListCommasConstrainedInference$subjectto().makeConstraint(head);
    }

    public String getHeadType() {
        return "edu.illinois.cs.cogcomp.comma.datastructures.CommaSRLSentence";
    }

    public String[] getHeadFinderTypes() {
        return new String[] {"edu.illinois.cs.cogcomp.comma.datastructures.Comma"};
    }

    public Normalizer getNormalizer(Learner c) {
        return new IdentityNormalizer();
    }
}
