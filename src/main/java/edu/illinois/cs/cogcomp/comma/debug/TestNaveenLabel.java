package edu.illinois.cs.cogcomp.comma.debug;

import java.util.List;

import edu.illinois.cs.cogcomp.comma.Comma;
import edu.illinois.cs.cogcomp.comma.VivekAnnotationCommaParser;
import edu.illinois.cs.cogcomp.comma.NaveenLabeler;

public class TestNaveenLabel {
	public static void main(String[] args){
		String id1 = "Other 15 wsj/00/wsj_0015.mrg:4";
		String id2 = "Other 2 wsj/00/wsj_0015.mrg:8";
		String navennLabel1 = NaveenLabeler.getNaveenLabel(id1);
		String navennLabel2 = NaveenLabeler.getNaveenLabel(id2);
		System.out.println(navennLabel1 + " should be " + "Quotation " + navennLabel1.equals("Quotation"));
		System.out.println(navennLabel2 + " should be " + "Introductory " + navennLabel2.equals("Introductory"));
		VivekAnnotationCommaParser cr = new VivekAnnotationCommaParser("data/comma_resolution_data.txt", "data/CommaFullView.ser", VivekAnnotationCommaParser.Ordering.ORDERED_SENTENCE);
		List<Comma> commas = cr.getCommas();
		for(Comma comma : commas){
			String naveenLabel = NaveenLabeler.getNaveenLabel(comma);
			if(naveenLabel!=null && naveenLabel.trim().isEmpty()){
				System.out.println(comma.getCommaID());
				System.out.println(naveenLabel);
			}
		}
	}
}
