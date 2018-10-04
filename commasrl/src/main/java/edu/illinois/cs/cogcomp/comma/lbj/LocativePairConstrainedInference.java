/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D3C8B4A03805C0547B2778FED4AB18E0D94511A247501E99A686D4026A5702EEDBF10D9DD3783C51DB3B3B6164B65824EDCD198756A37839827F5F11C8C4D3E6CA1F7A91B2A2B962249FE27E879B2AC960E8D3949FC8D085426482F28ABDEE83B4F317D68E938D5CCE19276C6F101FD2AEF1B8000000

package edu.illinois.cs.cogcomp.comma.lbj;

import edu.illinois.cs.cogcomp.comma.datastructures.Comma;
import edu.illinois.cs.cogcomp.comma.datastructures.CommaSRLSentence;
import edu.illinois.cs.cogcomp.infer.ilp.OJalgoHook;
import edu.illinois.cs.cogcomp.lbjava.infer.*;
import edu.illinois.cs.cogcomp.lbjava.learn.*;


public class LocativePairConstrainedInference extends ILPInference {
    public static CommaSRLSentence findHead(Comma c) {
        return c.getSentence();
    }


    public LocativePairConstrainedInference() {}

    public LocativePairConstrainedInference(CommaSRLSentence head) {
        super(head, new OJalgoHook());
        constraint = new LocativePairConstrainedInference$subjectto().makeConstraint(head);
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
