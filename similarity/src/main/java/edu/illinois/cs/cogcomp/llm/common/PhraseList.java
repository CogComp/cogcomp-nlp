/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.llm.common;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashSet;

/**
 * load both continuous phrase and discontinuous phrase. Only supports for two
 * word phrase now. TODO: Mult-word phrase
 * 
 * @author shaoshi
 *
 */
public class PhraseList {
	// all phrases
	HashSet<String> dict = new HashSet<String>();
	// discontinuous verb phrase
	HashSet<String> verb = new HashSet<String>();
	HashSet<String> verbPhrase = new HashSet<String>();

	public PhraseList(String dictionary, String discontPhrase) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(dictionary));
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.length() > 0) {
					dict.add(line);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			reader = new BufferedReader(new FileReader(discontPhrase));
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.length() > 0) {
					String arr[] = line.split("\\s+");
					verb.add(arr[0]);
					verbPhrase.add(line);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
