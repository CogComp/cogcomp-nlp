package edu.illinois.cs.cogcomp.edison.features.manifest;

import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.core.datastructures.trees.TreeParserFactory;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

class ManifestParser {

	private String featureSetName;
	private Tree<String> featuresList;

	private List<Tree<String>> definitions;
	private String contents;
	private HashMap<String, String> variables;

	public ManifestParser(InputStream file) throws EdisonException {
		Scanner scanner = new Scanner(file);

		StringBuilder content = new StringBuilder();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();

			int indexOf = line.indexOf(";");
			if (indexOf >= 0) line = line.substring(0, indexOf);

			if (line.trim().length() == 0) continue;

			content.append(line).append("\n");
		}

		initialize(content.toString());
	}

	public ManifestParser(String contents) throws EdisonException {
		initialize(contents);

	}

	private void initialize(String contents) throws EdisonException {
		this.contents = contents;
		Tree<String> parse = TreeParserFactory.getStringTreeParser().parse(contents);

		if (!parse.getLabel().equals("define")) {
			throw new EdisonException(
					"Invalid feature manifest. Expecting keyword 'define', found " + parse.getLabel() + " instead");
		}

		if (parse.getNumberOfChildren() == 0) {
			throw new EdisonException("A feature set needs a name and a list of features!");
		}

		featureSetName = parse.getChild(0).getLabel();

		featuresList = parse.getChild(parse.getNumberOfChildren() - 1);

		definitions = new ArrayList<>();
		variables = new HashMap<>();

		for (int i = 1; i < parse.getNumberOfChildren() - 1; i++) {
			Tree<String> child = parse.getChild(i);

			String label = child.getLabel();
			if (!label.equals("define") && !label.equals("defvar")) {
				throw new EdisonException(
						"Invalid feature definition. Only 'define' statements allowed here, found " + label + "\n" + child);
			}
			if (label.equals("define")) definitions.add(child);
			else if (label.equals("defvar")) {
				if (child.getNumberOfChildren() != 2) {
					throw new EdisonException("defvar needs exactly two parameters (defvar variable value)\n" + child);
				}

				String varName = child.getChild(0).getLabel();
				String value = child.getChild(1).getLabel();

				variables.put(varName, value);

			}

		}
	}

	public String getName() {
		return featureSetName;
	}

	public String getIncludedFeatures() {
		return this.contents;
	}

	public Tree<String> getFeatureDescriptor() {
		return featuresList;
	}

	public List<Tree<String>> getDefinitions() {
		return definitions;
	}

	public HashMap<String, String> getVariables() {
		return variables;
	}

	public void setVariable(String key, String value) {
		this.variables.put(key, value);
	}

}