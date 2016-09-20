/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.srl.data;

import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.srl.core.SRLManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;

public class LegalArguments {

	private final static Logger log = LoggerFactory.getLogger(LegalArguments.class);

	private final Map<String, Set<String>> legalArgs = new HashMap<>();
    private final Map<String, Set<String>> legalArgsForSense = new HashMap<>();
    private final Map<String, Set<String>> legalSenses = new HashMap<>();

	public LegalArguments(String file) throws Exception {
		List<URL> list = IOUtils.lsResources(LegalArguments.class, file);
		if (list.size() == 0) {
			log.error("Cannot find file " + file + " in the classpath.");
		}
		else {
			URL url = list.get(0);
			Scanner scanner = new Scanner(url.openStream());

			log.info("Loading legal arguments from {}", file);
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine().trim();
				if (line.length() == 0) continue;

				String[] strings = line.split("\t");

				String lemma = strings[0].trim();

				Set<String> set = new HashSet<>();
				Set<String> sensesSet = new HashSet<>();

                if (strings.length == 2) {
                    for (String argsForSense : strings[1].split("\\s+")) {
                        String sense = argsForSense.split("#")[0];

                        List<String> args = Arrays.asList(argsForSense.split("#")[1].split(","));
                        legalArgsForSense.put(lemma + "." + sense, new HashSet<>(args));
                        set.addAll(args);
                        sensesSet.add(sense);
                    }
                }

				set.add(SRLManager.NULL_LABEL);
				legalArgs.put(lemma, set);
                legalSenses.put(lemma, sensesSet);
			}
			scanner.close();
		}
	}

	public boolean hasLegalArguments(String lemma) {
		return this.legalArgs.containsKey(lemma);
	}

	public Set<String> getLegalArguments(String lemma) {
		return this.legalArgs.get(lemma);
	}

    public boolean hasLegalSenses(String lemma) {
        return this.legalSenses.containsKey(lemma);
    }

    public Set<String> getLegalSenses(String lemma) {
        return this.legalSenses.get(lemma);
    }

    public Set<String> getLegalArgsForSense(String lemma, String sense) {
        return this.legalArgsForSense.get(lemma + "." + sense);
    }
}
