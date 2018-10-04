/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.curator;

import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.tokenizer.Tokenizer;
import edu.illinois.cs.cogcomp.thrift.base.AnnotationFailedException;
import edu.illinois.cs.cogcomp.thrift.base.ServiceUnavailableException;
import org.apache.thrift.TException;

import java.net.SocketException;

/**
 * A simple {@link edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation}
 * builder, that uses {@link edu.illinois.cs.cogcomp.curator.CuratorClient} to create a
 * TextAnnotation and populate it with
 * {@link edu.illinois.cs.cogcomp.core.datastructures.ViewNames#TOKENS} and
 * {@link edu.illinois.cs.cogcomp.core.datastructures.ViewNames#SENTENCE} views.
 *
 * @author Christos Christodoulopoulos
 * @author Narender Gupta
 */
public class CuratorTextAnnotationBuilder implements TextAnnotationBuilder {
    private static final String NAME = "CuratorTextAnnotationBuilder";

    private CuratorClient curatorClient;

    public CuratorTextAnnotationBuilder(CuratorClient curatorClient) {
        this.curatorClient = curatorClient;
    }

    @Override
    public TextAnnotation createTextAnnotation(String text) throws IllegalArgumentException {
        return this.createTextAnnotation("", "", text);
    }

    @Override
    public TextAnnotation createTextAnnotation(String corpusId, String textId, String text)
            throws IllegalArgumentException {
        try {
            return curatorClient.getTextAnnotation(corpusId, textId, text);
        } catch (ServiceUnavailableException | AnnotationFailedException | SocketException
                | TException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public TextAnnotation createTextAnnotation(String corpusId, String textId, String text,
            Tokenizer.Tokenization tokenization) throws IllegalArgumentException {
        return new TextAnnotation(corpusId, textId, text, tokenization.getCharacterOffsets(),
                tokenization.getTokens(), tokenization.getSentenceEndTokenIndexes());
    }

    @Override
    public String getName() {
        return NAME;
    }
}
