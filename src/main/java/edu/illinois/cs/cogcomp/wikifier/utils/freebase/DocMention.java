package main.java.edu.illinois.cs.cogcomp.wikifier.utils.freebase;

public class DocMention {
	String doc;
	String mention;
	int start;
	int end;

	public DocMention(String doc, String mention, int start, int end) {
		this.doc = doc;
		this.mention = mention;
		this.start = start;
		this.end = end;
	}
}
