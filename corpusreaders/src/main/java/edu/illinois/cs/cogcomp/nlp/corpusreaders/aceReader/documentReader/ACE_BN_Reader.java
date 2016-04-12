package edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.documentReader;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.Paragraph;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ACE_BN_Reader {
	
	static boolean isDebug = false;

	public static void main (String[] args) throws FileNotFoundException {
		String file = "/shared/shelley/yqsong/eventData/ace2005Modify/data/English/bn/adj/CNN_ENG_20030304_173120.16.sgm";
		List<String> lines = LineIO.read(file);
		String content = "";
		for (int i = 0; i < lines.size(); ++i) {
			content += lines.get(i) + " ";
		}
		String contentRemovingTags = content;
	 	while (contentRemovingTags.contains("<")) {
	 		int p = contentRemovingTags.indexOf('<');
	 		int q = contentRemovingTags.indexOf('>');
	 		contentRemovingTags = contentRemovingTags.substring(0,p)
	 				+ contentRemovingTags.substring(q+1, contentRemovingTags.length());
	 	}
		parse(content, contentRemovingTags, false);

	}

	public static List<Pair<String, Paragraph>> parse (String content, String contentRemovingTags, boolean is2004) {
		List<Pair<String, Paragraph>> paragraphs = new ArrayList<Pair<String, Paragraph>>();
		
		Pattern pattern = null;
		Matcher matcher = null;
		
		String docID = "";
		String dateTime = "";
		String headLine = "";
		String text = "";
		
		pattern = is2004 ? Pattern.compile("<DOCNO>(.*?)</DOCNO>") : Pattern.compile("<DOCID>(.*?)</DOCID>");
		matcher = pattern.matcher(content);
		while (matcher.find()) {
			docID = (matcher.group(1)).trim();
	    }
		int index1 = content.indexOf(docID);
		Paragraph para1 = new Paragraph(index1, docID);
		Pair<String, Paragraph> pair1 = new Pair<String, Paragraph>("docID", para1);
		paragraphs.add(pair1);
		
		pattern = is2004 ? Pattern.compile("<DATE_TIME>(.*?)</DATE_TIME>") : Pattern.compile("<DATETIME>(.*?)</DATETIME>");
		matcher = pattern.matcher(content);
		while (matcher.find()) {
			dateTime = (matcher.group(1)).trim();
	    }
		int index2 = content.indexOf(dateTime);
		Paragraph para2 = new Paragraph(index2, dateTime);
		Pair<String, Paragraph> pair2 = new Pair<String, Paragraph>("dateTime", para2);
		paragraphs.add(pair2);

		if (is2004) {
			pattern = Pattern.compile("<TEXT>(.*?)<TURN>|<TURN>(.*?)<TURN>|<TURN>(.*?)</TEXT>|<TEXT>(.*?)</TEXT>");
		} else {
			pattern = Pattern.compile("<TURN>(.*?)</TURN>");
		}

		matcher = pattern.matcher(content);
		int regionStart = 0;
		while (matcher.find(regionStart)) {
			// Pick the first non-empty group.
			for (int i = 1; i <= matcher.groupCount(); ++i) {
				if (matcher.group(i) != null) {
					text = (matcher.group(i)).trim();
					break;
				}
			}

			int index4 = content.indexOf(text);
			Paragraph para4 = new Paragraph(index4, text);
			Pair<String, Paragraph> pair4 = new Pair<String, Paragraph>("text", para4);
			paragraphs.add(pair4);

			if (is2004) {
				regionStart = matcher.end() - 6; // Hack to move back to the overlapping <TURN> tag
			} else {
				regionStart = matcher.end();
			}
		}

		int index = 0;
		for (int i = 0; i < paragraphs.size(); ++i) {
			String paraContent = paragraphs.get(i).getSecond().content;
			int offsetWithFiltering = contentRemovingTags.indexOf(paraContent, index);
			paragraphs.get(i).getSecond().offsetFilterTags = offsetWithFiltering;
			
			index += paraContent.length();
		}
		
		if (isDebug) {
			for (int i = 0; i < paragraphs.size(); ++i) {
				System.out.println(paragraphs.get(i).getFirst() + "--> " + paragraphs.get(i).getSecond().content);
				System.out.println(content.substring(paragraphs.get(i).getSecond().offset, 
						paragraphs.get(i).getSecond().offset + paragraphs.get(i).getSecond().content.length()));
				System.out.println(contentRemovingTags.substring(paragraphs.get(i).getSecond().offsetFilterTags, 
						paragraphs.get(i).getSecond().offsetFilterTags + paragraphs.get(i).getSecond().content.length()));
				System.out.println();
			}
		}

//		if (isDebug) {
//			for (int i = 0; i < paragraphs.size(); ++i) {
//				System.out.println(paragraphs.get(i).getFirst() + "--> " + paragraphs.get(i).getSecond().content);
//				System.out.println(content.substring(paragraphs.get(i).getSecond().offset, 
//						paragraphs.get(i).getSecond().offset + paragraphs.get(i).getSecond().content.length()));
//				System.out.println();
//			}
//		}

		return paragraphs;
	}
	
}
