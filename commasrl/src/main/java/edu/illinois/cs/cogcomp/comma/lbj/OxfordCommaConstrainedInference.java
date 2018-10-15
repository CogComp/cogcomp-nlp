/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D3C814A02C040140FB2D7CDDB8F10F89B8111228F28567B39C2A99189C84409CFD5D089B577751D55A7A15231DDBA75B2D8E435A645667B45585ADD9F8C45073A8F6D66CB1B9B8CFD464F7A902F160AFF5901F885185AAF801E286F27DDFC2CF68EEC9E138E945FE1264CAF10B8F7622A8000000

package edu.illinois.cs.cogcomp.comma.lbj;

import edu.illinois.cs.cogcomp.comma.datastructures.Comma;
import edu.illinois.cs.cogcomp.comma.datastructures.CommaSRLSentence;
import edu.illinois.cs.cogcomp.infer.ilp.OJalgoHook;
import edu.illinois.cs.cogcomp.lbjava.infer.*;
import edu.illinois.cs.cogcomp.lbjava.learn.*;


public class OxfordCommaConstrainedInference extends ILPInference {
    public static CommaSRLSentence findHead(Comma c) {
        return c.getSentence();
    }


    public OxfordCommaConstrainedInference() {}

    public OxfordCommaConstrainedInference(CommaSRLSentence head) {
        super(head, new OJalgoHook());
        constraint = new OxfordCommaConstrainedInference$subjectto().makeConstraint(head);
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
