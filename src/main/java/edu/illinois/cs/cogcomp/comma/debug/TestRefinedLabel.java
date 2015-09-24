package edu.illinois.cs.cogcomp.comma.debug;

import java.util.List;

import edu.illinois.cs.cogcomp.comma.datastructures.Comma;
import edu.illinois.cs.cogcomp.comma.datastructures.CommaProperties;
import edu.illinois.cs.cogcomp.comma.readers.RefinedLabelHelper;
import edu.illinois.cs.cogcomp.comma.readers.VivekAnnotationReader;

public class TestRefinedLabel {
	public static void main(String[] args){
		String id1 = "15 wsj/00/wsj_0015.mrg:4";
		String id2 = "2 wsj/00/wsj_0015.mrg:8";
		String navennLabel1 = RefinedLabelHelper.getRefinedLabel(id1);
		String navennLabel2 = RefinedLabelHelper.getRefinedLabel(id2);
		System.out.println(navennLabel1 + " should be " + "Quotation " + navennLabel1.equals("Quotation"));
		System.out.println(navennLabel2 + " should be " + "Introductory " + navennLabel2.equals("Introductory"));
		VivekAnnotationReader reader = new VivekAnnotationReader(CommaProperties.getInstance().getOriginalVivekAnnotationFile());
		List<Comma> commas = reader.getCommas();
		for(Comma comma : commas){
			String naveenLabel = RefinedLabelHelper.getRefinedLabel(comma);
			if(naveenLabel!=null && naveenLabel.trim().isEmpty()){
				System.out.println(comma.getCommaID());
				System.out.println(naveenLabel);
			}
		}
	}
}
