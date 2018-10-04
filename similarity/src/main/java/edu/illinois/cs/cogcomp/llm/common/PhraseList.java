/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.llm.common;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashSet;

/**
 * load both continuous phrase and discontinuous phrase from the resource file. See /src/main/resources/phrases.txt
 * "0" means discontinuous phrase. "1" means continuous phrase.
 * 
 * 
 * Currently only supports for two word phrase now. TODO: Mult-word phrase
 * 
 * @author shaoshi
 *
 */
public class PhraseList {
	// all phrases
	HashSet<String> dict = new HashSet<String>();
	
	// discontinuous phrase
	HashSet<String> firstWord = new HashSet<String>();
	HashSet<String> discPhrase = new HashSet<String>();

	public PhraseList(String dictionary) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(dictionary));
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				String[] tokens = line.split("\\s+");
				if (tokens.length == 3) {
					dict.add(tokens[0] + " " + tokens[1]);
				}
				if (tokens[2].equals("0")) {
					discPhrase.add(tokens[0] + " " + tokens[1]);
					// System.out.println(tokens[0]+" "+tokens[1]);
					firstWord.add(tokens[0]);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
