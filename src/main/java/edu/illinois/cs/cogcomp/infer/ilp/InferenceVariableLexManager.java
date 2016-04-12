package edu.illinois.cs.cogcomp.infer.ilp;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Vivek Srikumar
 * 
 */
public class InferenceVariableLexManager {
	private ConcurrentHashMap<String, Integer> variables;

	public InferenceVariableLexManager() {
		variables = new ConcurrentHashMap<String, Integer>();
	}

	public void addVariable(String identifier, int variableId) {
		assert !variables.containsKey(identifier) : identifier + " found!";

		variables.put(identifier, variableId);
	}

	public int getVariable(String identifier) {
		if (variables.containsKey(identifier))
			return variables.get(identifier);
		else
			return -1;
	}

	/**
	 * This is slow. Don't use this for anything except debugging
	 * 
	 * @param var
	 * @return
	 */
	public String getVariableName(int var) {

		for (Entry<String, Integer> entry : variables.entrySet())
			if (entry.getValue() == var)
				return entry.getKey();

		return null;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		for (String s : variables.keySet()) {
			sb.append("\t" + s + "\t" + variables.get(s) + "\n");
		}

		return sb.toString();
	}

	public int size() {
		return variables.size();
	}
}
