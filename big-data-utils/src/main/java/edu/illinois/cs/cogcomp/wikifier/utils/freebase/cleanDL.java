/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.wikifier.utils.freebase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.google.api.client.util.Maps;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class cleanDL {
	static Map<String, List<DocMention>> mentions = Maps.newLinkedHashMap();
	private static final Logger logger = LoggerFactory.getLogger(cleanDL.class);

	public static void main(String[] args) throws IOException {
		List<String> lines = FileUtils.readLines(new File(
				"/Users/Shyam/mention.eval.dl"));

		for (String line : lines) {
			String[] parts = line.split("\\s+");
			System.out.println(parts[0] + parts[1] + parts[2]);
			StringBuilder sb = new StringBuilder();
			for (int i = 3; i < parts.length; i++)
				sb.append(parts[i] + " ");
			if (mentionFilter(parts)) {
				System.out.println("removing " + Arrays.asList(parts));
				continue;
			}
			if (mentions.containsKey(parts[0])) {
				mentions.get(parts[0])
						.add(new DocMention(parts[0], sb.toString(), Integer
								.parseInt(parts[1]), Integer.parseInt(parts[2])));
			} else {
				mentions.put(parts[0], new ArrayList<DocMention>());
				mentions.get(parts[0])
						.add(new DocMention(parts[0], sb.toString(), Integer
								.parseInt(parts[1]), Integer.parseInt(parts[2])));
			}
		}
		for (String doc : mentions.keySet()) {
			handleDoc(mentions.get(doc));
		}

		outputMentions();
	}

	private static void outputMentions() throws FileNotFoundException {
		PrintWriter w = new PrintWriter("/Users/Shyam/mention.eval.dl.out");
		for (String doc : mentions.keySet()) {
			for (DocMention tmp : mentions.get(doc))
				w.println(doc + "\t" + tmp.start + "\t" + tmp.end + "\t"
						+ tmp.mention);
		}
		w.close();
	}

	private static boolean mentionFilter(String[] parts) {
		char charAt = parts[3].charAt(0);
		if (Character.isLowerCase(charAt))
			return true;
		return false;
	}

	static void handleDoc(List<DocMention> mdocs) {
		List<DocMention> toRemove = Lists.newArrayList();
		for (DocMention m1 : mdocs) {
			for (DocMention m2 : mdocs) {
				if (m1.equals(m2))
					continue;
				if (m1.mention.contains(m2.mention) && m1.start <= m2.start
						&& m1.end >= m2.end) {
					logger.info("removing substring mention "
							+ m2.mention + "," + m2.start + "," + m2.end
							+ " for " + m1.mention + "," + m1.start + ","
							+ m1.end);
					toRemove.add(m2);
				}
				if (m2.mention.contains(m1.mention) && m2.start <= m1.start
						&& m2.end >= m1.end) {
					logger.info("removing substring mention "
							+ m1.mention + " for " + m2.mention);
					toRemove.add(m1);
				}
			}
		}
		Iterator<DocMention> it = mdocs.iterator();
		while (it.hasNext()) {
			DocMention m = it.next();
			if (toRemove.contains(m))
				it.remove();
		}
	}
}
