package edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.documentReader;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.Paragraph;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ACE_CTS_Reader {
	
	static boolean isDebug = false;

	public static void main (String[] args) throws FileNotFoundException {
		String file = "/shared/shelley/yqsong/eventData/ace2005Modify/data/English/cts/adj/fsh_29097.sgm";
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
		parse(content, contentRemovingTags);
	}
	
	public static List<Pair<String, Paragraph>> parse (String content, String contentRemovingTags) {
		List<Pair<String, Paragraph>> paragraphs = new ArrayList<>();
		
		Pattern pattern = null;
		Matcher matcher = null;
		
		String docID = "";
		String dateTime = "";
		String headLine = "";
		String text = "";
		
		pattern = Pattern.compile("<DOCID>(.*?)</DOCID>");
		matcher = pattern.matcher(content);
		while (matcher.find()) {
			docID = (matcher.group(1)).trim();
	    }
		int index1 = content.indexOf(docID);
		Paragraph para1 = new Paragraph(index1, docID);
		Pair<String, Paragraph> pair1 = new Pair<String, Paragraph>("docID", para1);
		paragraphs.add(pair1);
		
		pattern = Pattern.compile("<DATETIME>(.*?)</DATETIME>");
		matcher = pattern.matcher(content);
		while (matcher.find()) {
			dateTime = (matcher.group(1)).trim();
	    }
		int index2 = content.indexOf(dateTime);
		Paragraph para2 = new Paragraph(index2, dateTime);
		Pair<String, Paragraph> pair2 = new Pair<String, Paragraph>("dateTime", para2);
		paragraphs.add(pair2);
		
		pattern = Pattern.compile("<TURN>(.*?)</TURN>");
		matcher = pattern.matcher(content);
		while (matcher.find()) {
			text = (matcher.group(1)).trim();
			
			text = text.substring(text.indexOf("</SPEAKER>") + "</SPEAKER>".length()).trim();
			int index4 = content.indexOf(text);
			Paragraph para4 = new Paragraph(index4, text);
			Pair<String, Paragraph> pair4 = new Pair<String, Paragraph>("text", para4);
			paragraphs.add(pair4);
	    }

		int index = 0;
		for (int i = 0; i < paragraphs.size(); ++i) {
			int offsetWithFiltering = contentRemovingTags.indexOf(paragraphs.get(i).getSecond().content, index);
			paragraphs.get(i).getSecond().offsetFilterTags = offsetWithFiltering;
			
			index += paragraphs.get(i).getSecond().content.length();
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
