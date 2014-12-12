package edu.illinois.cs.cogcomp.srl.data;

import edu.illinois.cs.cogcomp.core.algorithms.Sorters;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.edison.data.IResetableIterator;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.PredicateArgumentView;
import edu.illinois.cs.cogcomp.edison.sentences.Relation;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.srl.core.SRLManager;
import edu.illinois.cs.cogcomp.srl.core.SRLType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.*;

public class LegalArguments {

	private final static Logger log = LoggerFactory.getLogger(LegalArguments.class);

	private final Map<String, Set<String>> legalArgs = new HashMap<String, Set<String>>();

	public LegalArguments(String file) throws Exception {
		List<URL> list = IOUtils.lsResources(LegalArguments.class, file);
		if (list.size() == 0) {
			log.error("Cannot find file " + file + " in the classpath. "
					+ "Using legal arguments constraints from frame files.");
		}
		else {
			URL url = list.get(0);
			Scanner scanner = new Scanner(url.openStream());

			log.info("Loading legal arguments from {}", file);
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine().trim();
				if (line.length() == 0) continue;

				String[] strings = line.split("\t");

				assert strings.length == 2 : line;

				String lemma = strings[0].trim();

				Set<String> set = new HashSet<String>(Arrays.asList(strings[1].split("\\s+")));

				set.add(SRLManager.NULL_LABEL);
				legalArgs.put(lemma, Collections.unmodifiableSet(set));
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

	public static void generateLegalArgumentsFile(
			IResetableIterator<TextAnnotation> data, String gold,
			SRLType SRLType) throws FileNotFoundException {

		String outputFile = SRLType + ".legal.arguments";
		Map<String, Set<String>> legalArgs = new HashMap<String, Set<String>>();

		while (data.hasNext()) {
			TextAnnotation ta = data.next();

			if (!ta.hasView(gold)) continue;

			PredicateArgumentView srl = (PredicateArgumentView) ta.getView(gold);

			for (Constituent predicate : srl.getPredicates()) {
				String lemma = srl.getPredicateLemma(predicate);

				Set<String> legal = new HashSet<String>();

				for (Relation r : srl.getArguments(predicate)) {
					legal.add(r.getRelationName());
				}

				if (!legalArgs.containsKey(lemma))
					legalArgs.put(lemma, legal);
				else
					legalArgs.get(lemma).addAll(legal);
			}
		}

		PrintWriter out = new PrintWriter(new File(outputFile));

		for (String lemma : Sorters.sortSet(legalArgs.keySet())) {
			StringBuilder legal = new StringBuilder();
			for (String l : Sorters.sortSet(legalArgs.get(lemma))) {
				legal.append(l).append(" ");
			}

			String trim = legal.toString().trim();
			if (trim.length() > 0)
				out.println(lemma + "\t" + trim);
		}
		out.close();

		System.out.println("Wrote legal arguments to " + outputFile);
	}
}
