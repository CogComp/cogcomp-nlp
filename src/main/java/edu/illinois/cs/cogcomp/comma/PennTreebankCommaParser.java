package edu.illinois.cs.cogcomp.comma;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.illinois.cs.cogcomp.comma.bayraktar.BayraktarPatternLabeler;
import edu.illinois.cs.cogcomp.edison.data.TextAnnotationReader;
import edu.illinois.cs.cogcomp.edison.data.corpora.PennTreebankReader;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;

public class PennTreebankCommaParser implements Parser {
	private TextAnnotationReader taReader;
	private Iterator<Comma> currTAsCommaIterator;
	private static String treebankHome;
	static{
		CommaProperties properties = CommaProperties.getInstance();
        treebankHome = properties.getPTBHDir();
	}
	
	public PennTreebankCommaParser(TextAnnotationReader taReader) {
		this.taReader = taReader;
		reset();
	}
	
	public PennTreebankCommaParser() {
		try {
			String[] sections = { "00", "01", "02" , "03", "04", "05", "06", "07", "08", "09", "10", "11", "12" , "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24"};
			this.taReader = new PennTreebankReader(treebankHome, sections);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		reset();
	}
	
	@Override
	public Object next() {
		while(hasNext()){
			Comma comma = currTAsCommaIterator.next();
			String bayraktarPattern = comma.getBayraktarPattern();
			if(BayraktarPatternLabeler.isLabelAvailable(bayraktarPattern))
				return comma;
		}
		return null;
	/*	if(hasNext())
			return currTAsCommaIterator.next();
		else
			return null;*/
	}
	
	public boolean hasNext(){
		
		while(!currTAsCommaIterator.hasNext()){
			TextAnnotation nextTA = taReader.next();
			if(nextTA==null)
				break;
			List<Comma> commasInTA = CommaLabeler.getCommas(nextTA);
			currTAsCommaIterator = commasInTA.iterator();
		}
		return currTAsCommaIterator.hasNext();
	}

	@Override
	public void reset() {
		taReader.reset();
		currTAsCommaIterator = new ArrayList<Comma>().iterator();
	}

	@Override
	public void close() {}
	
	
	public static void main(String[] args){
		Parser cp = new PennTreebankCommaParser();
		while(true){
			Comma comma = (Comma) cp.next();
			System.out.println(comma.getTextAnnotation(false).getId());
		}
	}
}
