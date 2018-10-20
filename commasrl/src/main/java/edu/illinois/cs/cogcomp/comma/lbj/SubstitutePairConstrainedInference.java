/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D3C814A02C040140FB2D7CDDB8F10F89B8190818CB06CDC89C0A99589D952709CFDD8289B575154B8E38D8531368A7F2E2E5D9B721B62B6173215E1BD32999964C0CAEF3A2873A9CBC2484BF236FA6A847A98DFF948876C60CA2E3349754BD5F7C958FA8BD59E535EB4ECFC01326BF00B7F16A4AD8000000

package edu.illinois.cs.cogcomp.comma.lbj;

import edu.illinois.cs.cogcomp.comma.datastructures.Comma;
import edu.illinois.cs.cogcomp.comma.datastructures.CommaSRLSentence;
import edu.illinois.cs.cogcomp.infer.ilp.OJalgoHook;
import edu.illinois.cs.cogcomp.lbjava.infer.*;
import edu.illinois.cs.cogcomp.lbjava.learn.*;


public class SubstitutePairConstrainedInference extends ILPInference {
    public static CommaSRLSentence findHead(Comma c) {
        return c.getSentence();
    }


    public SubstitutePairConstrainedInference() {}

    public SubstitutePairConstrainedInference(CommaSRLSentence head) {
        super(head, new OJalgoHook());
        constraint = new SubstitutePairConstrainedInference$subjectto().makeConstraint(head);
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
