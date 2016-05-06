package edu.illinois.cs.cogcomp.annotation.handler;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TreeView;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.ParserAnnotator;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class StanfordBreakTest {
    private TextAnnotation ta;
    private StanfordDepHandler depParser;

    @Before
    public void setUp() throws IOException, AnnotatorException {
        String text = "It 's a tough job hosting the Academy Awards .\n" +
                "It 's supposed to be entertaining , but it 's also a night for the stars to revel in the glory of an Oscar win .\n" +
                "And the host has to be edgy and funny , without going over the line and insulting the honorees .\n" +
                "But when it works , as it did in 1992 when tough-guy actor Jack Palance did push-ups in front of comedian and host Billy Crystal , only to become the butt of Crystal 's jokes for the rest of the night , it makes for memorable moments .\n" +
                "On Friday , the organization behind the Academy Awards named Australian actor Hugh Jackman as the host of February 's annual show .\n" +
                "Jackman is the first non-comedian to single-handedly host the show in recent years .\n" +
                "The last time an Australian hosted the show was in 1987 , when to `` Crocodile Dundee '' star Paul Hogan shared the stage with actress Goldie Hawn and comedian Chevy Chase .\n" +
                "Coming off starring in `` Australia , '' which has had only limited box office success since its Nov. 26 opening with a total box office take of $ 43 million , Jackman will rely on song and dance instead of just jokes to entertain a worldwide audience on Oscar night .\n" +
                "He has done well hosting the Tony Awards , the Broadway version of the Oscars , but the Oscars ' audience is used to comedians on the show .\n" +
                "On a night where hype and pomp rule , the comedian 's role is often to keep the show grounded by injecting a jester 's dose of realism .\n" +
                "Jackman 's naming as host prompted a `` love it or hate it '' reaction here on our news floor in Los Angeles .\n" +
                "There did n't seem to be much in between , so we thought we 'd leave it to readers to weigh in with their own thoughts .\n" +
                "Hugh Jackman : an Oscar hit or miss ?\n";
        ta = BasicTextAnnotationBuilder.createTextAnnotationFromTokens(getTokenisedText(text));

        Properties stanfordProps = new Properties();
        stanfordProps.put("annotators", "pos, parse");
        stanfordProps.put("parse.originalDependencies", true);
        POSTaggerAnnotator posAnnotator = new POSTaggerAnnotator("pos", stanfordProps);
        ParserAnnotator parseAnnotator = new ParserAnnotator("parse", stanfordProps);
        depParser = new StanfordDepHandler(posAnnotator, parseAnnotator);
    }

    private List<String[]> getTokenisedText(String rawtext) {
        List<String[]> sentences = new ArrayList<>();
        for (String sent : rawtext.split("\n")) sentences.add(sent.split(" "));
        return sentences;
    }

    @Test
    public void testBreak() throws AnnotatorException {
        depParser.addView(ta);
        TreeView parse = (TreeView) ta.getView(ViewNames.DEPENDENCY_STANFORD);
        List<Constituent> c = ta.getView(ViewNames.TOKENS).getConstituentsCoveringSpan(111, 113);
        for (Constituent con : c) { // c is list with "Academy","Awards"
            System.out.println("input "+ con);
            List<Constituent> parsecons = parse.getConstituentsCovering(con);
            System.out.println(parsecons.get(0));
        }
    }
}
