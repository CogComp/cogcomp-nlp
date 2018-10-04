/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
//package edu.illinois.cs.cogcomp.wikifier.utils.spelling;
//
//import java.io.IOException;
//
//import org.apache.lucene.index.IndexReader;
//import org.apache.lucene.index.Term;
//import org.apache.lucene.search.spell.DirectSpellChecker;
//import org.apache.lucene.search.spell.SuggestMode;
//import org.apache.lucene.search.spell.SuggestWord;
//
//import edu.illinois.cs.cogcomp.wikifier.utils.lucene.Lucene;
//
//public class MyDirectSpellChecker extends AbstractSurfaceQueryEngine
//{
//	private DirectSpellChecker ds;
//	private IndexReader ir;
//	private SuggestMode mode;
//	public int MAX_CANDIDATES=3;
//	
//	/**
//	 * default constructor
//	 * @throws IOException
//	 */
//	public MyDirectSpellChecker(String indexPath, int level)
//	{
//		ds= new DirectSpellChecker();
//		try {
//			ir = Lucene.reader(indexPath);
//		} catch (IOException e) {
//			e.printStackTrace();
//			System.err.println("Cannot load direct spell checker!");
//		}
//		switch (level)
//		{
//			case 1:
//				mode = SuggestMode.SUGGEST_MORE_POPULAR;		
//				break;
//			case 2:
//				mode = SuggestMode.SUGGEST_ALWAYS;
//				break;
//			case 3:
//				mode = SuggestMode.SUGGEST_WHEN_NOT_IN_INDEX;
//				break;
//		}
//	}
//	@Override
//	public String[] query(String q) throws IOException
//	{
//		System.out.println("Query: "+q);
//		SuggestWord[] suggestSimilar = ds.suggestSimilar(new Term("title",q), MAX_CANDIDATES, ir,mode);
//		String[] ans = new String[suggestSimilar.length];
//		for(int i=0; i < suggestSimilar.length ;i++)
//		{
//			SuggestWord w = suggestSimilar[i];
//			System.out.println(w.string+" "+w.score);
//			ans[i]=w.string;
//		}
//		return ans;
//	}
//
//	@Override
//	public void close() throws IOException
//	{
//		ir.close();
//	}
//	
//	public static void main(String[] args) throws IOException
//	{
//		String spellIndexPath="data/WikiData/SpellIndex2";
//		MyDirectSpellChecker sc = new MyDirectSpellChecker(spellIndexPath,2);
////		sc.query("Sri Lanke");
////		sc.query("Mann Coulter");
////		sc.query("Engerland");
////		sc.query("Chilie");
////		sc.query("San Antone");
////		sc.query("Chris Mattherws");
////		sc.query("Collin Ferrell");
////		sc.query("Crotia");
////		
////		sc.query("Saudie");
////		sc.query("Billy Ayres");
//		sc.query("Suadi");
////		sc.query("Arnold");
////		sc.query("Czech Republick");
////		sc.query("Sri Lanke");
////		sc.query("Channing Tandum");
//
//		sc.close();
//	}
//}
