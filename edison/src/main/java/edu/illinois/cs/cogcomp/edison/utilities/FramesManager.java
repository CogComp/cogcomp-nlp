/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.utilities;

import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.resources.ResourceConfigurator;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import org.cogcomp.Datastore;
import org.cogcomp.DatastoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * A set of utility functions to read frame-related resources, like propBank, nomBank, etc.
 *
 * @author Vivek Srikumar
 */
public class FramesManager {

    private final static Logger log = LoggerFactory.getLogger(FramesManager.class);

    private final Set<String> prepositions = new HashSet<>(
            Arrays.asList(new String[]{"about", "above", "across", "after",
                    "against", "along", "among", "around", "as", "at",
                    "before", "behind", "beneath", "beside", "between", "by",
                    "down", "during", "for", "from", "in", "inside", "into",
                    "like", "of", "off", "on", "onto", "over", "round",
                    "through", "to", "towards", "with"}));

    public final String UNKNOWN_VERB_CLASS = "UNKNOWN";

    public FramesManager(String dir) {
        log.info("Loading frames from {}", dir);

        try {
            readFrameData(dir);
        } catch (Exception e) {
            log.error("Unable to load frames from {}", dir);
            throw new RuntimeException(e);
        }
    }

    /**
     * @param withPropBankData if false, it will use nomBank data
     */
    public FramesManager(Boolean withPropBankData) throws InvalidPortException, InvalidEndpointException, DatastoreException {
        Datastore dsNoCredentials = new Datastore(new ResourceConfigurator().getDefaultConfig());
        String dir = "";
        if(withPropBankData) {
            File f = dsNoCredentials.getDirectory("org.cogcomp.propbank", "propbank-frames", 3.1, false);
            dir = f.getAbsolutePath() + File.separator + "propbank-frames-3.1" + File.separator + "frames";
        }
        else {
            File f = dsNoCredentials.getDirectory("org.cogcomp.nombank", "nombank-frames", 1.0, false);
            dir = f.getAbsolutePath() + File.separator + "nombank.1.0" + File.separator + "frames";
        }

        log.info("Loading frames from {}", dir);

        try {
            readFrameData(dir);
        } catch (Exception e) {
            log.error("Unable to load frames from {}", dir);
            throw new RuntimeException(e);
        }
    }

    private void readFrameData(String dir) throws Exception {
        frameData = new HashMap<>();

        File framesetFile = new File(dir + File.separator + "frameset.dtd");

        if (!framesetFile.exists())
            throw new EdisonException("Cannot read frames. 'frameset.dtd' not found in " + dir);

        final URL dtd = framesetFile.toURI().toURL();

        for (File file : new File(dir).listFiles()) {
            if (!file.getName().endsWith("xml"))
                continue;
            URL url = file.toURI().toURL();
            String fileName = file.getName();

            // A hack to deal with percent-sign in nombank. There is another
            // file called perc-sign that will fill this void.
            if (fileName.contains("percent-sign.xml"))
                continue;

            // I HATE IT THAT JAVA XML IS SO PAINFUL! ALL THIS JUNK TO READ A
            // BUNCH OF FILES!!!!
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(false);
            dbf.setValidating(false);

            DocumentBuilder db = dbf.newDocumentBuilder();

            db.setEntityResolver((publicId, systemId) -> new InputSource(dtd.openStream()));

            Document doc = db.parse(url.openStream());

            NodeList predicateElements = doc.getElementsByTagName("predicate");

            for (int i = 0; i < predicateElements.getLength(); i++) {
                String lemma = IOUtils.stripFileExtension(fileName);

                FrameData fData = new FrameData(lemma);
                frameData.put(lemma, fData);

                NodeList roleSets = doc.getElementsByTagName("roleset");
                addRoleSets(fileName, lemma, fData, roleSets);
            }
        }
    }

    private void addRoleSets(String file, String lemma, FrameData fData, NodeList roleSets) {
        for (int i = 0; i < roleSets.getLength(); i++) {
            Element roleSet = (Element) roleSets.item(i);

            String sense = roleSet.getAttribute("id");
            String senseName = roleSet.getAttribute("name");

            // WTF frame makers?
            if (sense.equals("lionise.01"))
                sense = "lionize.01";

            if (sense.equals("oneslashonezeroth.01"))
                sense = "1-slash-10th.01";

            // danielkh: commenting this out since many files don't satisfy
            /*
            assert sense.startsWith(IOUtils.stripFileExtension(file)) || sense.startsWith(lemma) :
                    "lemma: " + lemma + "\t sense:" + sense + "\t file: " + file +
                            "\tIOUtils.stripFileExtension(file): " + IOUtils.stripFileExtension(file);
            */

            String verbClass;
            if (roleSet.hasAttribute("vncls")) {
                verbClass = roleSet.getAttribute("vncls");
                if (verbClass.equals("-") || verbClass.length() == 0)
                    verbClass = UNKNOWN_VERB_CLASS;
            } else
                verbClass = UNKNOWN_VERB_CLASS;

            sense = sense.replaceAll(lemma + ".", "");

            fData.addSense(sense, senseName, verbClass);

            NodeList roles = roleSet.getElementsByTagName("role");

            for (int j = 0; j < roles.getLength(); j++) {
                Element role = (Element) roles.item(j);

                String label = role.getAttribute("n");
                // danielkh: commented out the attribute, since it messes up description retriaval when using it inside our srl annotators
//                if (role.hasAttribute("f"))
//                    label += "-" + role.getAttribute("f");
                String argLabel = "A" + label;
                fData.addArgument(sense, argLabel);

                if (role.hasAttribute("descr")) {
                    String descr = role.getAttribute("descr");
                    fData.addArgumentDescription(sense, argLabel, descr);
                }

                NodeList elementsByTagName = role.getElementsByTagName("vnrole");

                for (int roleId = 0; roleId < elementsByTagName.getLength(); roleId++) {
                    String vntheta = ((Element) (elementsByTagName.item(roleId))).getAttribute("vntheta");
                    fData.addArgumentVNTheta(sense, argLabel, vntheta);
                }

            } // end of arguments
            addExamples(fData, roleSet, sense);
        }
    }

    private void addExamples(FrameData fData, Element roleSet, String sense) {
        NodeList examples = roleSet.getElementsByTagName("example");

        for (int exampleId = 0; exampleId < examples.getLength(); exampleId++) {
            Element example = (Element) examples.item(exampleId);

            String name = "";
            if (example.hasAttribute("name"))
                name = example.getAttribute("name");

            String text = example.getElementsByTagName("text").item(0).getTextContent();

            NodeList args = example.getElementsByTagName("arg");

            Map<String, String> argDescriptions = new HashMap<>();
            Map<String, String> argExamples = new HashMap<>();

            for (int argId = 0; argId < args.getLength(); argId++) {
                Element arg = (Element) args.item(argId);

                String n = "";
                if (arg.hasAttribute("n"))
                    n = "A" + arg.getAttribute("n");

                if (n.length() <= 1 || n.toUpperCase().equals("AM"))
                    continue;

                if (arg.hasAttribute("f")) {
                    String f = arg.getAttribute("f").toLowerCase();
                    if (prepositions.contains(f)) {
                        argDescriptions.put(n, f);
                        argExamples.put(n, arg.getTextContent());
                    }
                } else {
                    String t = arg.getTextContent();
                    String[] parts = t.split(" ");
                    if (parts.length > 0) {
                        t = parts[0].toLowerCase();
                        if (prepositions.contains(t)) {
                            argDescriptions.put(n, t);
                            argExamples.put(n, arg.getTextContent());

                        }
                    }
                }
            }
            fData.addExample(sense, name, text, argDescriptions, argExamples);
        }
    }

    public HashMap<String, FrameData> frameData;

    public Set<String> getPredicates() {
        return frameData.keySet();
    }

    public FrameData getFrame(String lemma) {
        return frameData.get(lemma);
    }

    public FrameData.SenseFrameData getFrameWithSense(String lemma, String sense) {
        FrameData f = getFrame(lemma);
        return (f != null)? f.getArgInfoForSense(sense) : null;
    }

    /** Example input/output values:
     * argLabel: A1-PPT --> expected output: commodity
     * argLabel: A2-LOC --> expected output: location
     * argLabel: A0-PAG --> expected output: storer
     */
    public static String getArgDcrp(String argLabel, FrameData.SenseFrameData arguments) {
        System.out.println(arguments.argDescription.keySet());
        return (arguments.argDescription.containsKey(argLabel)) ? arguments.argDescription.get(argLabel).description : "";
    }

    /**
     * Use this to create the compact files used during inference.
     * NB: You need to have access to the Propbank and Nombank frame files
     */
    public static void main(String[] args) throws IOException, InvalidPortException, DatastoreException, InvalidEndpointException {
        FramesManager manager = new FramesManager(true);
//        for (String predicate : manager.getPredicates()) {
//            System.out.println("--> predicate: " + predicate);
//            FrameData frame = manager.getFrame(predicate);
//            System.out.println("--> frame: " + frame);
//            Set<String> senses = frame.getSenses();
//            System.out.println("--> senses: " + senses);
//            String senseStr = "";
//            for (String sense : senses) {
//                Set<String> argsForSense = frame.getArgsForSense(sense);
//                if (argsForSense.isEmpty()) continue;
//                System.out.println("---------> argsForSense: " + argsForSense);
//                senseStr += sense + "#";
//                for (String arg : argsForSense)
//                    senseStr += arg + ",";
//                senseStr = senseStr.substring(0, senseStr.length()-1) + " ";
//            }
//        }

/*
        FrameData.SenseFrameData arguments = manager.getFrameWithSense("buy", "01");
        System.out.println(arguments);
        System.out.println(arguments.senseName);
        System.out.println(arguments.verbClass);
        System.out.println(arguments.argDescription);
        for(String k : arguments.argDescription.keySet()) {
            System.out.println("\t\t\tkey: " + k);
            System.out.println("\t\t\tdescription: " + arguments.argDescription.get(k).description);
            System.out.println("\t\t\tvnTheta: " + arguments.argDescription.get(k).vnTheta);
        }
*/

        FrameData.SenseFrameData frameData = manager.getFrameWithSense("buy", "01");
        System.out.println("frameData: " + frameData);
        System.out.println(FramesManager.getArgDcrp("A1", frameData));
    }
}