/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.nlp.tokenizer;

import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.tokenizer.TokenizerStateMachine.State;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;

/**
 * This is the entry point to the tokenizer state machine. This class is thread-safe, the
 * {@link TokenizerStateMachine} is not.
 * 
 * @author redman
 */
public class StatefulTokenizer implements Tokenizer {
    @Override
    public Pair<String[], IntPair[]> tokenizeSentence(String sentence) {
        // parse the test
        TokenizerStateMachine tsm = new TokenizerStateMachine();
        tsm.parseText(sentence);

        // construct the data needed for the tokenization.
        int words = 0;
        for (State s : tsm.completed) {
            int idx = s.stateIndex();
            if (idx != TokenizerState.IN_SENTENCE.ordinal())
                words++;
        }
        IntPair[] wordOffsets = new IntPair[words];
        String[] tokens = new String[words];
        int wordIndex = 0;
        for (State s : tsm.completed) {
            State ms = (State) s;
            if (s.stateIndex() != TokenizerState.IN_SENTENCE.ordinal()) {
                tokens[wordIndex] = new String(tsm.text, ms.start, ms.end - ms.start);
                wordOffsets[wordIndex++] = new IntPair(ms.start, ms.end);
            }
        }
        return new Pair<>(tokens, wordOffsets);
    }

    @Override
    public Tokenization tokenizeTextSpan(String textSpan) {

        // parse the text
        TokenizerStateMachine tsm = new TokenizerStateMachine();
        tsm.parseText(textSpan);

        // construct the data needed for the tokenization.
        int sentences = 0;
        int words = 0;
        for (State s : tsm.completed) {
            int idx = s.stateIndex();
            if (idx == TokenizerState.IN_SENTENCE.ordinal())
                sentences++;
            else
                words++;
        }
        IntPair[] wordOffsets = new IntPair[words];
        int[] sentenceEnds = new int[sentences];
        String[] tokens = new String[words];
        int sentenceIndex = 0;
        int wordIndex = 0;
        for (State s : tsm.completed) {
            State ms = (State) s;
            if (s.stateIndex() == TokenizerState.IN_SENTENCE.ordinal())
                sentenceEnds[sentenceIndex++] = wordIndex;
            else {
                tokens[wordIndex] = new String(tsm.text, ms.start, ms.end - ms.start);
                wordOffsets[wordIndex++] = new IntPair(ms.start, ms.end);
            }
        }
        return new Tokenization(tokens, wordOffsets, sentenceEnds);
    }

    /**
     * Just for testing.
     * 
     * @param args
     */
    static public void main(String[] args) {
        String issue1 =
                "5 careers: big demand, big pay if you're in one of the jobs listed here, you may be able to negotiate a sweet pay hike for yourself when changing employers. by jeanne sahadi, cnnmoney.com senior writer february 3, 2006: 4:42 pm est \n"
                        + "new york (cnnmoney.com) – recent surveys show that a lot of people are itching to find new jobs and human resource managers are expecting a lot of movement   both signs that employers may need to sweeten the pot.\n"
                        + " there also have been predictions that the labor market may start to tilt in favor of job seekers due to a shortage of skilled workers.\n"
                        + " cnnmoney.com talked with specialists at national staffing and recruiting firm spherion to find out which job hunting workers today are sitting in the catbird seat when it comes to negotiating better pay.\n"
                        + " below is a list of in demand workers in five arenas.\n"
                        + " accounting thanks to enron and the sarbanes oxley act of 2002, those who have a few years of corporate auditing experience working for a large public accounting firm can negotiate a sweet package for themselves when they change jobs.\n"
                        + " that applies whether they're leaving the accounting firm to go work for a corporation or if they're seeking to return to the public accounting firm from an auditing job at an individual company.\n"
                        + " college graduates with an accounting degree but not yet a cpa designation might make between $35,000 and $45,000 a year, or up to $50,000 in large cities like new york. after a couple of years they can command a substantial pay hike if they move to large company as an internal staff auditor or to a smaller company as controller. at that point, their salary can jump to anywhere from $50,000 to $75,000.\n"
                        + " the expectation is that they will obtain their cpa designation.\n"
                        + " if they choose to return to a public accounting firm as an audit manager after a couple of years at a corporation they can earn a salary of $70,000 to $85,000.\n"
                        + " sales and marketing the health care and biomedical fields offer some handsome earnings opportunities for those on the business side.\n"
                        + " business development directors, product managers and associate product managers working for medical device makers, for instance, can do quite well for themselves if they develop a successful track record managing the concept, execution and sales strategy for a medical device before jumping ship.\n"
                        + " typically, they have an mba in marketing plus at least two to three years' experience on the junior end to between five and eight years' experience at the more senior levels. that experience ideally will be in the industry where they're seeking work.\n"
                        + " an associate product manager might make a base salary of $55,000 to $75,000. a product manager can make a base of $75,000 to $95,000, while a business development director may make $120,000 to $160,000. those salaries don't include bonuses.\n"
                        + " the business development director seeking a vice president position could boost his base to between $150,000 to $200,000    depending on whether the new company is a risky start up or established device maker.\n"
                        + " legal intellectual property attorneys specialising in patent law and the legal secretaries who have experience helping to prepare patent applications are highly desirable these days.\n"
                        + " the most in demand are those lawyers with not only a j.d. but also an advanced degree in electrical and mechanical engineering, chemical engineering, biotechnology, pharmacology or computer science.\n"
                        + " even those patent lawyers who just have an undergraduate degree in those fields have a leg up.\n"
                        + " patent lawyers working for a law firm might make $125,000 to $135,000 to start or about $90,000 if they work for a corporation that trying to get a patent or to protect one they already have. with a couple of years' experience, they can expect a 10 percent jump or better when they get another job.\n"
                        + " legal secretaries, meanwhile, might make $65,000 at a law firm or $55,000 at a corporation. should they choose to move to a new employer, they can command close to a 10 percent bump in pay.\n"
                        + " technology two tech jobs in high demand these days are .net (dot net) developers and quality assurance analysts.\n"
                        + " developers who are expert users of microsoft software programming language .net can make between $75,000 and $85,000 a year in major cities when they're starting out. if they pursue a job at a company that seeks someone with a background in a given field (say, a firm looking for a .net developer experienced in using software related to derivatives) they might snag a salary hike of 15 percent or more when they switch jobs.\n"
                        + " those who work in software quality management, meanwhile, might make $65,000 to $75,000 a year and be able to negotiate a 10 percent to 15 percent jump in pay if they switch jobs.\n"
                        + " manufacturing and engineering despite all the announced job cuts in the automotive industry, quality and process engineers, as well as plant managers certified in what known as lean manufacturing techniques, are hot commodities.\n"
                        + " the same applies to professionals in similar positions at other types of manufacturers.\n"
                        + " one lean manufacturing technique is to use video cameras to capture the manufacturing process. a quality engineer will analyse the tapes to identify areas in the process that create inefficiencies or excess waste, both in terms of materials and workers' time.\n"
                        + " process and manufacturing engineers might make between $65,000 and $75,000. with an lm certification and a few years' experience, they can command pay hikes of between 15 percent and 20 percent if they choose to switch jobs.\n"
                        + " a plant manager making between $90,000 and $120,000 may expect to get a 10 percent raise or more.        \n";
        String issue2 = "John is 'a good boy' and he is 'not a dog' or a dogs' or a dog's eye.";
        String issue3 =
                "At 1:30 a.m. Sunday, the troops moved out: 40 U.S. soldiers in a convoy of Humvees mounted with heavy machine guns, and 60 Afghan National Army troops in pickup trucks.";
        String issue4 = " There is one.     ";
        String issue5 =
                "\"I said, 'what're you? Crazy?'\" said Sandowsky. \"I can't afford to do that.";
        String[] ss = {issue1, issue2, issue3, issue4, issue5};
        for (String issue : ss) {
            TextAnnotation ta;
            final TextAnnotationBuilder tab =
                    new TokenizerTextAnnotationBuilder(new IllinoisTokenizer());
            ta = tab.createTextAnnotation(issue);
            for (int i = 0; i < ta.getNumberOfSentences(); i++)
                System.out.println(ta.getSentence(i));
            System.out.println("\n");
            final TextAnnotationBuilder stab =
                    new TokenizerTextAnnotationBuilder(new StatefulTokenizer());
            ta = stab.createTextAnnotation(issue);
            System.out.println(ta);
            for (int i = 0; i < ta.getNumberOfSentences(); i++)
                System.out.println(ta.getSentence(i));

        }
    }
}
