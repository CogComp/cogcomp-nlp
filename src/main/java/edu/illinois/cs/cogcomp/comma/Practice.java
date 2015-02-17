package edu.illinois.cs.cogcomp.comma;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.illinois.cs.cogcomp.edison.data.corpora.PennTreebankReader;
import edu.illinois.cs.cogcomp.edison.data.corpora.TreebankChunkReader;
import edu.illinois.cs.cogcomp.edison.data.curator.CuratorClient;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;
import edu.illinois.cs.cogcomp.srl.data.PropbankReader;

public class Practice {
	public static void main(String[] args) throws Exception{
		String[] sections = { "00" };
		List<TextAnnotation> taList = new ArrayList<TextAnnotation>();
		PennTreebankReader ptbr;
		ptbr = new PennTreebankReader("data/pennTreeBank/treebank-3/parsed/mrg/wsj", sections);
		
		for (TextAnnotation ta : ptbr) {
			if(ta!=null){
				taList.add(ta);
			}
		}
		taList = taList.subList(5, 15);
		Collections.shuffle(taList);
		pbr(taList);
		for(TextAnnotation ta: taList)
			System.out.println(ta.getView(ViewNames.SRL_VERB));
	}
	
	
	public static void pbr(List<TextAnnotation> taList) throws Exception{
		String[] sections = { "00" };
		boolean mergeContiguousCArgs = false;
		String treebankHome = "data/pennTreeBank/treebank-3/parsed/mrg/wsj";
		String propbankHome = "data/propbank_1/data";
		PropbankReader pbr = new PropbankReader(taList, treebankHome, propbankHome, sections, ViewNames.SRL_VERB, mergeContiguousCArgs);
		while(pbr.hasNext())
			pbr.next();
		TextAnnotation ta;
		int hasView = 0, noView = 0;
		while(pbr.hasNext()){
			ta = pbr.next();
			System.out.println(ta);
			if(ta.hasView(ViewNames.SRL_VERB)){
				System.out.println(ta.getView(ViewNames.SRL_VERB));
				hasView++;
			}
			else{
				noView++;
				String curatorHost = "trollope.cs.illinois.edu";
				int curatorPort = 9010;
				boolean respoectTokenization = true;
				CuratorClient client = new CuratorClient(curatorHost, curatorPort, respoectTokenization);
				boolean forceUpdate = false;
				client.addSRLVerbView(ta, forceUpdate);
				System.out.println(ta.getView(ViewNames.SRL_VERB));
			}
		}
		System.out.println("hasView = "  + hasView);
		System.out.println("noView = "  + noView);
	}
	
	public static void tcr() throws Exception{
		String[] sections = { "00" };
		boolean mergeContiguousCArgs = false;
		String treebankHome = "data/pennTreeBank/treebank-3/parsed/mrg/wsj";
		//String treebankHome = "data/pennTreeBank";
		TreebankChunkReader tcr = new TreebankChunkReader(treebankHome, sections);
		TextAnnotation ta;
		int hasView = 0, noView = 0;
		while(tcr.hasNext()){
			ta = tcr.next();
			if(ta.hasView(ViewNames.SHALLOW_PARSE)){
				System.out.println(ta.getView(ViewNames.SRL_VERB));
				hasView++;
			}
			else{
				noView++;
				String curatorHost = "trollope.cs.illinois.edu";
				int curatorPort = 9010;
				boolean respoectTokenization = true;
				CuratorClient client = new CuratorClient(curatorHost, curatorPort, respoectTokenization);
				boolean forceUpdate = false;
				client.addChunkView(ta, forceUpdate);
				System.out.println(ta.getText());
				System.out.println(ta.getView(ViewNames.SHALLOW_PARSE));
			}
		}
		System.out.println("hasView = "  + hasView);
		System.out.println("noView = "  + noView);
	}
}
