/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.utilities;

import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.curator.CuratorConfigurator;
import edu.illinois.cs.cogcomp.curator.CuratorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Prepares the test.ta file used for the Maven Tests
 *
 * @author Christos Christodoulopoulos
 */
public class CreateTestTAResource {
    private AnnotatorService annotator;
    private List<String> testInputs;
    private List<TextAnnotation> tas;
    private static Logger logger = LoggerFactory.getLogger(CreateTestTAResource.class);

    public CreateTestTAResource() {
        testInputs = new ArrayList<>();
        tas = new ArrayList<>();
        populateTestInputs();


        try {
            // Respecting tokenization causes all sorts of problems
            // since the sentence below are not properly tokenized
            Map<String, String> nonDefaultValues = new HashMap<>();
            nonDefaultValues.put(CuratorConfigurator.RESPECT_TOKENIZATION.key,
                    CuratorConfigurator.FALSE);
            ResourceManager curatorConfig = (new CuratorConfigurator()).getConfig(nonDefaultValues);
            annotator = CuratorFactory.buildCuratorClient(curatorConfig);
            // Populate the annotation list
            getTextAnnotations();

            // Serialise and write the file
            File file = new File("src/test/resources/test.ta");
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(tas);
            oos.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new CreateTestTAResource();
    }

    private void populateTestInputs() {

        testInputs.add("My mother-in-law, " + "the Queen of Egypt, "
                + "left me some pearls in October.");

        testInputs.add("I give John $900 today.");
        testInputs.add("This is a test." + " This is another one. "
                + "My mother-in-law has arrived. ");

        testInputs.add("It is a technique widely used in " + "natural language processing."
                + " It is similar to the concept of " + "lexical analysis for computer languages.");

        testInputs.add("Models of this kind have recently "
                + "attracted much attention within the NLP community.");

        testInputs.add("  We have three kinds of pastries. "
                + "One of them has chocolate and the other 2 don't. ");

        testInputs.add("Most customers think that our pastries"
                + " are the best in the world. Some come here " + "from hundreds of miles away.");

        testInputs.add("Three people want more.");
        testInputs.add("This\tsentence has a tab. This does not.");

        testInputs.add("By winning the National Football League (NFL) playoff game, "
                + "the 49ers will host the winner of Sunday's Dallas-Green Bay "
                + "game on January 15 to decide a berth in the January 29 championship "
                + "game at Miami.");

        testInputs.add("The construction of the library took time.");

        String text = "John said that he will quit next week.";
        testInputs.add(text);

        text =
                "It wasn't a long walk to Lisa Cochran's car "
                        + "in the Costco parking lot from the store."
                        + " But by the time she got there, her infant "
                        + "was near death. Cochran blames the Infantino "
                        + "baby sling -- now recalled -- for her baby's death.";
        testInputs.add(text);

        text =
                "They won seven straight on their way to Super Bowl titles in 1997 and '98, then didn't make the playoffs last year. ";
        testInputs.add(text);

        text = "By the time the second half started, I was pretty much O.K.''";
        testInputs.add(text);

        text =
                "New England rallied to maintain  first place; the Giants and Falcons got wake-up calls and Buffalo  toppled the Chiefs as the second half of the National Football  League season was in full swing on Sunday. ";
        testInputs.add(text);

        text =
                "but - (( but I 'd love to have his hair)) something just came off the ~ TV about the Scott trial. ";
        testInputs.add(text);

        text =
                "Joseph Conrad Parkhurst , who founded the motorcycle magazine Cycle 	World in 1962 , has died.";
        testInputs.add(text);

        text =
                "Mr. Dahl, a registered representative in the insurance business, said he \"screwed up\" because he didn't realize he was breaking securities laws. ";
        testInputs.add(text);

        text = "mike von fremd , abc news , jacksonville .";
        testInputs.add(text);

        text = "jackie judd , abc news .";
        testInputs.add(text);

        text =
                "For the appeals court ruling in Duncan v. Northwest Airlines : http://www.uscourts.gov/links.html and click on 9th Circuit .";
        testInputs.add(text);

        text = "natalie pawelski , cnn .";
        testInputs.add(text);

        text =
                "`` The first thing overseas traders want to know when they come in in the morning is , ` What did the Nasdaq and the Dow do ? ' '' said Howard Klein , an institutional broker specializing in emerging markets at SG Cowen Securities in New York .";
        testInputs.add(text);

        text = "Sheryl Corley , NPR news , Chicago .";
        testInputs.add(text);

        text = "The car costs $900.";
        testInputs.add(text);

        text =
                "Steelers Quarterback Jim Brady sneaked a backward pass to running back Joe Corduroy for a last-second "
                        + "denial of Florida that will surely echo down the ages. "
                        + "His first completion of the 2011 season, this pass represents the pinnacle of "
                        + "Jim's terrible season to date, and "
                        + "it may mean redemption of his $13M contract with Pittsburgh, according to Joe's mother.";
        testInputs.add(text);

        text =
                "The investigation focused on the hangman's cell, where 141 French prisoners-of-war were said to have been executed, as well at the 18th-century Master Ropemaker's House.";
        testInputs.add(text);

        text =
                "The South Side store, part of a redevelopment of an abandoned steel plant, fell by a 25-21 vote. Passage required a majority of 26 votes in the 50-member council.";
        testInputs.add(text);

        text =
                "The newspaper added that regardless of the Israeli challenges, Lahoud would still be able to deliver on his duties, supported by Syria and a united Lebanon.";
        testInputs.add(text);

        text =
                "Seven Egyptian human rights organizations issued a plea today, Monday, to Egyptian President Hosni Mubarak to hold accountable those responsible for the acts of torture that targeted residents of a village in Egypt's countryside while investigating two capital murders last August.";
        testInputs.add(text);

        text =
                "GUS on Friday disposed of its remaining home shopping business and last non-UK retail operation with the 390m (265m) sale of the Dutch home shopping company, Wehkamp, to Industri Kapital, a private equity firm.";
        testInputs.add(text);

        text =
                "Tintin, the Smurfs, Boule et Bill, Asterix, Natacha, Spirou and other legendary characters were created by talented artists like Herge, Morris, Franquin and Walthery.";
        testInputs.add(text);

        text = "Spirou was created by Rob-Vel.";
        testInputs.add(text);

        text =
                "In an Oct. 19 review of `` The Misanthrope '' at Chicago 's Goodman Theatre ( `` Revitalized Classics Take the Stage in Windy City , '' Leisure & Arts ) , the role of Celimene , played by Kim Cattrall , was mistakenly attributed to Christina Haag . ";
        testInputs.add(text);

        text =
                "A three-man German delegation, accompanied by a Somali expert, arrived in north Mogadishu on Friday to start a new peace initiative to reconcile warring Somali clan factions. The delegation, headed by Hans Sterken, has since arrival held talks with Somali Salvation Alliance (SSA) factions supporting north Mogadishu warlord, self-styled interim Somali president Ali Mahdi Mohamed and those of his south Mogadishu ally, Osman Hassan Ali \"Atto\". Atto, a former financier-turned political enemy of south Mogadishu warlord General Mohamed Farah Aidid, heads a faction opposed to Aidid in the United Somali Congress/Somali National Alliance (USC/SNA) which controls the southern half of the war-torn Somali capital. Addressing newsmen here on Saturday, Sterken said that his delegation intended to meet all faction leaders who are agreeable to dialogue, as \"there is no way one can obtain peace, except through dialogue.\" \"We also intend to extend our invitation for dialogue to all representatives of the majority, as we believe that the majority should rule, that is democracy. That's the concrete message we have for Somalis,\" Sterken said. Asked whether they would invite General Aidid for talks on the peace initiative, Sterken said they had already held talks with Aidid's representative in the Egyptian capital Cairo and at Nairobi in neighbouring Kenya. The official was Abdurahman Ahmed Ali \"Tuur\", the deputy president in Aidid's yet to be recognised government, who welcomed the new German peace efforts. The German delegation was sent by Germany's Asocication for the Promotion of International Understanding, an organisation that consists of the country's leading former parliamentarians.";
        testInputs.add(text);

        text =
                "Houston," + "Monday, July 21 -- Men have landed and walked on the moon.  Two "
                        + "Americans, astronauts of Apollo 11, steered their fragile four-legged "
                        + "lunar module safely and smoothly to the historic landing yesterday at "
                        + "4:17:40 P.M., Eastern daylight time.  Neil A. Armstrong, the "
                        + "38-year-old civilian commander, radioed to earth and the mission "
                        + "control room here: \"Houston, Tranquility Base here; the Eagle has "
                        + "landed.\"\n\nThe first men to reach the moon -- Mr. Armstrong and his "
                        + "co-pilot, Col. Edwin E. Aldrin, Jr. of the Air Force -- brought their "
                        + "ship to rest on a level, rock-strewn plain near the southwestern shore "
                        + "of the arid Sea of Tranquility.  About six and a half hours later, Mr. "
                        + "Armstrong opened the landing craft's hatch, stepped slowly down the "
                        + "ladder and declared as he planted the first human footprint on the "
                        + "lunar crust: \"That's one small step for man, one giant leap for "
                        + "mankind.\"";

        testInputs.add(text);

        text =
                "Houston," + "Monday, July 21 -- Men have landed and walked on the moon.  Two"
                        + "Americans, astronauts of Apollo 11, steered their fragile four-legged"
                        + "lunar module safely and smoothly to the historic landing yesterday at"
                        + "4:17:40 P.M., Eastern daylight time.  Neil A. Armstrong, the"
                        + "38-year-old civilian commander, radioed to earth and the mission"
                        + "control room here: \"Houston, Tranquility Base here; the Eagle has"
                        + "landed.\"\n\n The first men to reach the moon -- Mr. Armstrong and his"
                        + "co-pilot, Col. Edwin E. Aldrin, Jr. of the Air Force -- brought their"
                        + "ship to rest on a level, rock-strewn plain near the southwestern shore"
                        + "of the arid Sea of Tranquility.  About six and a half hours later, Mr."
                        + "Armstrong opened the landing craft's hatch, stepped slowly down the"
                        + "ladder and declared as he planted the first human footprint on the"
                        + "lunar crust: \"That's one small step for man, one giant leap for"
                        + "mankind.\"";

        testInputs.add(text);
    }

    private void getTextAnnotations() throws Exception {
        String textId = "textId";
        String corpusId = "corpus";
        int count = 0;
        int total = testInputs.size();
        for (String text : testInputs) {
            TextAnnotation ta = annotator.createBasicTextAnnotation(corpusId, textId, text);
            annotator.addView(ta, ViewNames.PARSE_CHARNIAK);
            annotator.addView(ta, ViewNames.PARSE_STANFORD);
            annotator.addView(ta, ViewNames.DEPENDENCY);
            annotator.addView(ta, ViewNames.DEPENDENCY_STANFORD);
            annotator.addView(ta, ViewNames.POS);
            annotator.addView(ta, ViewNames.LEMMA);
            annotator.addView(ta, ViewNames.SHALLOW_PARSE);
            annotator.addView(ta, ViewNames.NER_CONLL);
            annotator.addView(ta, ViewNames.COREF);
            annotator.addView(ta, ViewNames.SRL_VERB);
            annotator.addView(ta, ViewNames.SRL_NOM);
            annotator.addView(ta, ViewNames.QUANTITIES);
            tas.add(ta);
            count++;
            if (count % 10 == 0)
                logger.info("Finished " + count + "/" + total + " sentences");
        }
    }
}
