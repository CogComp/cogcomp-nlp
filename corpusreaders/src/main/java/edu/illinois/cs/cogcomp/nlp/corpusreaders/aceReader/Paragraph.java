package edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader;

import java.io.Serializable;

public class Paragraph implements Serializable {
	
	public Paragraph() {
	}
	
	public Paragraph(int offset, String content) {
		this.offset = offset;
		this.content = content;
	}

	public int offset = -1;

	public int offsetFilterTags = -1;
	
	public String content;
}
