package edu.illinois.cs.cogcomp.srl.data;

import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import edu.illinois.cs.cogcomp.srl.SRLProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * @author Vivek Srikumar
 */
public class FramesManager {

	private final static Logger log = LoggerFactory.getLogger(FramesManager.class);

	public static String propFramesDir = SRLProperties.getInstance().getPropbankHome() + "/frames";
	public static String nomFramesDir = SRLProperties.getInstance().getNombankHome() + "/frames";

	private static FramesManager PROP_INSTANCE = null, NOM_INSTANCE = null;

	public static FramesManager getPropbankInstance() {
		if (PROP_INSTANCE == null) {
			synchronized (log) {
				if (PROP_INSTANCE == null) {
					PROP_INSTANCE = new FramesManager(propFramesDir);
				}
			}
		}
		return PROP_INSTANCE;
	}

	public static FramesManager getNombankInstance() {
		if (NOM_INSTANCE == null) {
			synchronized (log) {
				if (NOM_INSTANCE == null) {

					NOM_INSTANCE = new FramesManager(nomFramesDir);
				}
			}
		}
		return NOM_INSTANCE;
	}

	private static final Set<String> prepositions = new HashSet<>(
            Arrays.asList(new String[]{"about", "above", "across", "after",
                    "against", "along", "among", "around", "as", "at",
                    "before", "behind", "beneath", "beside", "between", "by",
                    "down", "during", "for", "from", "in", "inside", "into",
                    "like", "of", "off", "on", "onto", "over", "round",
                    "through", "to", "towards", "with"}));

	public static final String UNKNOWN_VERB_CLASS = "UNKNOWN";

	public FramesManager(String dir) {
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

			db.setEntityResolver(new EntityResolver() {
				public InputSource resolveEntity(String publicId,
												 String systemId) throws SAXException, IOException {
					return new InputSource(dtd.openStream());
				}
			});

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

			assert sense.startsWith(IOUtils.stripFileExtension(file)) || sense.startsWith(lemma) : lemma + "\t" + sense;

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
				if (role.hasAttribute("f"))
					label += "-" + role.getAttribute("f");
				String argLabel = "A" + label;
				fData.addArgument(sense, argLabel);

				if (role.hasAttribute("descr")) {
					String descr = role.getAttribute("descr");
					fData.addArgumentDescription(sense, argLabel, descr);
				}

				NodeList elementsByTagName = role
						.getElementsByTagName("vnrole");

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

    /**
     * Use this to create the compact files used during inference.
     * NB: You need to have access to the Propbank and Nombank frame files
     */
    public static void main(String[] args) throws IOException {
        List<String> outLines = new ArrayList<>();
        FramesManager manager = new FramesManager(propFramesDir);
//        FramesManager manager = new FramesManager(nomFramesDir);
        for (String predicate : manager.getPredicates()) {
            FrameData frame = manager.getFrame(predicate);
            Set<String> senses = frame.getSenses();
            String senseStr = "";
            for (String sense : senses) {
                Set<String> argsForSense = frame.getArgsForSense(sense);
                if (argsForSense.isEmpty()) continue;
                senseStr += sense + "#";
                for (String arg : argsForSense)
                    senseStr += arg + ",";
                senseStr = senseStr.substring(0, senseStr.length()-1) + " ";
            }
            outLines.add(predicate + "\t" + senseStr.trim());
        }
        Collections.sort(outLines);
        LineIO.write("src/main/resources/Verb.legal.arguments", outLines);
//        LineIO.write("src/main/resources/Nom.legal.arguments", outLines);
    }
}
