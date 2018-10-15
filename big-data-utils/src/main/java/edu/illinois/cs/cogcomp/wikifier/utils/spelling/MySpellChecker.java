/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
//package edu.illinois.cs.cogcomp.wikifier.utils.spelling;
//
//import java.io.File;
//import java.io.IOException;
//import java.io.PrintStream;
//import java.io.PrintWriter;
//import java.util.List;
//import java.util.Map;
//
//import org.apache.lucene.search.spell.PlainTextDictionary;
//import org.apache.lucene.search.spell.SpellChecker;
//import org.apache.lucene.store.MMapDirectory;
//
//import com.google.common.collect.Lists;
//import com.google.common.collect.Maps;
//
//import edu.illinois.cs.cogcomp.wikifier.common.GlobalParameters;
//import edu.illinois.cs.cogcomp.wikifier.utils.WikiTitleUtils;
//import edu.illinois.cs.cogcomp.wikifier.utils.lucene.Lucene;
//import AshwinSpellChecker;
//
//public class MySpellChecker extends AbstractSurfaceQueryEngine
//{
//
//	private SpellChecker spellChecker;
//	
//	public static void main(String[] args) throws Exception
//	{
//		String dictPath = "titles_canonical.txt";
//		String spellIndexPath = "data/WikiData/SpellIndex2";
//		
////		MySpellChecker sc = new MySpellChecker(dictPath,spellIndexPath,false);
////		MyDirectSpellChecker sc = new MyDirectSpellChecker(spellIndexPath,1);
//		GoogleSpellChecker sc = new GoogleSpellChecker();
////		AshwinSpellChecker sc = new AshwinSpellChecker();
//		Map<String, String> q_and_a = getTestCases();
//		int c=0;
//		List<String> missed = Lists.newArrayList();
//		for(String q:q_and_a.keySet())
//		{
//			if(sc.queryAndCheck(q, q_and_a.get(q)))
//				c++;
//			else
//				missed.add(q);
//		}
//		System.out.println("=====");
//		System.out.println("Correct on "+c+"/"+q_and_a.size());
//		System.out.println("Missed");
//		for(String m:missed)
//			System.out.println(m);
//		sc.close();
//	}
//	
//	public String[] query(String q) throws IOException
//	{
//		String[] suggestions = spellChecker.suggestSimilar(q,150);
//		for(String s:suggestions)
//			System.out.println(s);
//		return suggestions;
//	}
//	
//	public void close() throws IOException
//	{
//		spellChecker.close();
//	}
//	
//	public MySpellChecker(String dictPath,String spellIndexPath, boolean createNewIndex) throws IOException
//	{
//		spellChecker = new SpellChecker(new MMapDirectory(new File(spellIndexPath)));
//		 
////		spellChecker.setStringDistance(new NGramDistance());
////		SpellChecker spellChecker = new SpellChecker(new RAMDirectory());
//		
//		// create a new index
//		if(createNewIndex)
//		{
//			long startTime = System.currentTimeMillis();
//			System.out.println("Creating New Index");
//			spellChecker.indexDictionary(
//					new PlainTextDictionary(new File(dictPath)),
//					Lucene.newConfig(Lucene.ENGLISH), true);
//			System.out.println("Index Created");
//			long estimatedTime = System.currentTimeMillis() - startTime;
//			System.out.println("Time elapsed " + estimatedTime);
//		}
//	}
//
//	
//	public static void dumpTitles(String[] args) throws Exception
//	{
//		loadGlobal(args);
//		PrintWriter pw = new PrintWriter(new File("titles_canonical.txt"));
//		int c = 0;
//		for (int titleId : GlobalParameters.wikiAccess)
//		{
//			String title = GlobalParameters.wikiAccess.getTitle(Integer
//					.toString(titleId));
//			pw.println(WikiTitleUtils.canonicalizeNoComma(title)); // William_Clinton_(disambiguation)
//																	// =>
//																	// William
//																	// Clinton
//																	//
//																	// Champaign,_Illinois
//																	// =>
//																	// Champaign
//			if (c++ % 100 == 0)
//				System.out.println("Done " + c);
//		}
//		pw.close();
//		System.out.println("Finished " + c);
//
//	}
//	public static void loadGlobal(String[] args) throws Exception
//	{
//
//		PrintStream originalOut = System.out;
//		// Disables extra outputs
//		// Console.silence();
//
//		originalOut
//				.println("Initialization will take a few minutes. Please be patient.");
//		originalOut.println("Loading...");
//
//		originalOut.println("arguments: ");
//		for (String arg : args)
//			originalOut.println(arg);
//
//		originalOut.println("finished printing args.");
//		if (args.length == 2)
//		{
//			GlobalParameters.loadConfig(args[0]);
//			originalOut.println("loading config '" + args[0] + "'...");
//		}
//	}
//	public static Map<String,String> getTestCases()
//	{
//		Map<String,String> q_and_a= Maps.newLinkedHashMap();
//		q_and_a.put("Chine","China");
//		q_and_a.put("Crotia","Croatia");
//		q_and_a.put("Barcalona","Barcelona");
//		q_and_a.put("Mann Coulter","Ann Coulter");
//		q_and_a.put("Engerland","England");
//		q_and_a.put("Packistan","Pakistan");
//		q_and_a.put("Chilie","Chile");
//		q_and_a.put("Swedin","Sweden");
//		q_and_a.put("Astralia","Australia");
//		q_and_a.put("Holand","Holland");
//		q_and_a.put("Netharlands","Netherlands");
//		q_and_a.put("Roawn","Rowan");
//		q_and_a.put("Mogodishu","Mogadishu");
//		q_and_a.put("Bagdad","Baghdad");
//		q_and_a.put("San Antone","San Antonio");
//		q_and_a.put("Mattherws","Matthews");
//		q_and_a.put("Greec","Greece");
//		q_and_a.put("Collin Ferrell","Colin Farrell");
//		q_and_a.put("Athen","Athens");
//		q_and_a.put("Venezuella","Venezuela");
//		q_and_a.put("Damascas","Damascus");
//		q_and_a.put("Dubia","Dubai");
//		q_and_a.put("Vienne","Vienna");
//		q_and_a.put("Viena","Vienna");
//		q_and_a.put("Narobi","Nairobi");
//		q_and_a.put("Cypris","Cyprus");
//		q_and_a.put("Soctland","Scotland");
//		q_and_a.put("Ciaro","Cairo");
//		q_and_a.put("Vietname","Vietnam");
//		q_and_a.put("Lebenon","Lebanon");
//		q_and_a.put("Saudie","Saudi");
//		q_and_a.put("Michicgan","Michigan");
//		q_and_a.put("Isral","Israel");
//		q_and_a.put("Ayres","Ayers");
//		q_and_a.put("Qutar","Qatar");
//		q_and_a.put("Ethipia","Ethiopia");
//		q_and_a.put("Ehtiopia","Ethiopia");
//		q_and_a.put("Sidney","Sydney");
//		q_and_a.put("Tailand","Thailand");
//		q_and_a.put("Liverpol","Liverpool");
//		q_and_a.put("Kanya","Kenya");
//		q_and_a.put("Pyonyang","Pyongyang");
//		q_and_a.put("Swedan","Sweden");
//		q_and_a.put("Liverpol","Liverpool");
//		q_and_a.put("Suadi","Saudi");
//		q_and_a.put("Kuwaite","Kuwait");
//		q_and_a.put("Schwarzenegge","Schwarzenegger");
//		q_and_a.put("Bagdahd","Baghdad");
//		q_and_a.put("Czech Republick","Czech Republic");
//		q_and_a.put("Jurusalem","Jerusalem");
//		q_and_a.put("Floria","Florida");
//		q_and_a.put("Sri Lanke","Sri Lanka");
//		q_and_a.put("Arazona","Arizona");
//		q_and_a.put("Jerusalim","Jerusalem");
//		q_and_a.put("Schwarzennegger","Schwarzenegger");
//		q_and_a.put("Liverpoole","Liverpool");
//		q_and_a.put("Channing Tandum","Channing Tatum");
//		q_and_a.put("Nirobi","Nairobi");
//		return q_and_a;
//	
//	}
//}