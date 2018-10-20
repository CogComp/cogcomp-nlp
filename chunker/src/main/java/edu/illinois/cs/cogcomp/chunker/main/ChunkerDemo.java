/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.chunker.main;

import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.curator.CuratorFactory;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by qning2 on 10/26/16.
 * Usage: args={input.txt, output.txt}
 * The chunker model used here is specified in ChunkerConfigurator by the two properties: MODEL_DIR_PATH and MODEL_NAME.
 */
public class ChunkerDemo {
    public static void main (String[] args) throws Exception {

        /*Load data*/
        byte[] encoded = Files.readAllBytes(Paths.get(args[0]));
        String text = new String(encoded, StandardCharsets.UTF_8);

        /*Create textannotation*/
        AnnotatorService annotator = CuratorFactory.buildCuratorClient();
        TextAnnotation ta = annotator.createBasicTextAnnotation("corpus", "id", text);

        /*Add part-of-speech*/
        annotator.addView(ta, ViewNames.POS);

        /*ChunkerAnnotator*/
        ChunkerAnnotator ca = new ChunkerAnnotator(true);
        ca.initialize(new ChunkerConfigurator().getDefaultConfig());
        ca.addView(ta);

        /*Output to file*/
        List<String> lines =  new ArrayList<>();
        lines.add(ta.getView(ViewNames.SHALLOW_PARSE).toString());
        lines.add(ta.getView(ViewNames.POS).toString());
        Path file = Paths.get(args[1]);
        Files.write(file,lines, Charset.forName("UTF-8"));
    }
}
