/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nesim.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.wcohen.secondstring.JaroWinkler;

import edu.illinois.cs.cogcomp.nesim.io.MappingReader;

public class NickNameManager {
	// Variables
	HashMap<String, ArrayList<String>> mapNickName;
	HashMap<String, ArrayList<Float>> mapNickConf;
	String nicknameFileName;
	float confThres;
	JaroWinkler jrwk;

	/**
	 * Constructor to initialize a NickNameManager object. Utilized as a helper
	 * class in NameParser to score string of type PER whenever nicknames may
	 * apply.
	 * 
	 * @param nicknameFileName
	 *            File name to obtain list of nickname strings
	 * @param confThres
	 *            Threshold for confidence of whether a string is a nickname
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public NickNameManager(String nicknameFileName, float confThres) throws NumberFormatException, IOException {
		this.nicknameFileName = nicknameFileName;
		this.confThres = confThres;
		mapNickName = new HashMap<String, ArrayList<String>>();
		mapNickConf = new HashMap<String, ArrayList<Float>>();
		jrwk = new JaroWinkler();
		readNicknameFile();
	}

	/**
	 * Reads the given file and stores nicknames in a HashMap.
	 * 
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public void readNicknameFile() throws NumberFormatException, IOException {
		BufferedReader reader = MappingReader.getReader(nicknameFileName);
		String line;

		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (line.length() == 0)
				continue;
			if (line.charAt(0) == '#')
				continue;
			String[] parts = line.split("\t+");
			if (parts.length != 3)
				continue;
			String nick = parts[0];
			nick = nick.toLowerCase();
			String name = parts[1];
			name = name.toLowerCase();
			String stringConf = parts[2];
			Float conf = Float.valueOf(stringConf);
			if (conf < confThres)
				continue;
			if (!mapNickName.containsKey(nick)) {
				ArrayList<String> names = new ArrayList<String>();
				names.add(name);
				mapNickName.put(nick, names);
				ArrayList<Float> confs = new ArrayList<Float>();
				confs.add(conf);
				mapNickConf.put(nick, confs);
			} else {
				ArrayList<String> names = mapNickName.get(nick);
				ArrayList<Float> confs = mapNickConf.get(nick);
				names.add(name);
				confs.add(conf);
				mapNickName.put(nick, names);
				mapNickConf.put(nick, confs);
			}
		}
	}

	/**
	 * Checks if the string parameter is a valid nickname.
	 * 
	 * @param text
	 *            String to be checked for nickname
	 * @return A boolean value (true/false) determining whether the string is a
	 *         nickname
	 */
	public boolean isNickName(String text) {
		if (mapNickName.get(text.toLowerCase()) == null)
			return false;
		return true;
	}

	/**
	 * Finds all possible nicknames for the string parameter.
	 * 
	 * @param text
	 *            String to find the nickname mapping for from the HashMap
	 * @return An ArrayList of all possible nicknames for a certain name
	 */
	public ArrayList<String> getNickNameMapping(String text) {
		ArrayList<String> retArr;
		if ((retArr = mapNickName.get(text.toLowerCase())) == null)
			return null;
		return retArr;
	}

	/**
	 * Adds a new nickname or replaces an old one.
	 * 
	 * @param text
	 *            String for which the nickname will be replaced
	 * @return An ArrayList containing the new nickname for the given formal
	 *         name
	 */
	public ArrayList<String> replaceNickNames(String text) {
		ArrayList<String> arrNicknames = mapNickName.get(text);
		ArrayList<String> resNicknames = new ArrayList<String>();
		if (arrNicknames != null) {
			int n = arrNicknames.size();
			for (int i = 0; i < n; i++)
				resNicknames.add(arrNicknames.get(i));
		}
		resNicknames.add(text);
		return resNicknames;
	}
}
