package edu.illinois.cs.cogcomp.wikifier.utils.lucene;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.store.RAMDirectory;

import java.io.File;
import java.io.IOException;

public class HelloLucene {
	
  public static void main(String[] args) throws IOException, ParseException {
 
	Analyzer analyzer = Lucene.STANDARD; // use the same analyser for indexing and querying
	String spellIndexPath = "data/WikiData/SpellIndex2";
    Directory index = new MMapDirectory(new File(spellIndexPath));

//    IndexWriterConfig config = new IndexWriterConfig(Lucene.version, analyzer);
//
//    IndexWriter w = new IndexWriter(index, config);
//    LineIterator it = FileUtils.lineIterator(new File("titles_canonical.txt"));
//    while(it.hasNext())
//    {
//    	addDoc(w, it.next());
//    }
//    w.close();

    String querystr = "Chine";

    QueryParser qp = Lucene.newQueryParser("title", analyzer);
    Query q = qp.parse(querystr);

    Query fuzzyQuery = new FuzzyQuery(new Term("title", querystr), 1);
    int hitsPerPage = 20;
    
    
    IndexSearcher searcher = Lucene.searcher(index);
    TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
//    searcher.search(q, collector);
    searcher.search(fuzzyQuery, collector);
    ScoreDoc[] hits = collector.topDocs().scoreDocs;
    

    System.out.println("Found " + hits.length + " hits.");
    for(int i=0;i<hits.length;++i) {
      int docId = hits[i].doc;
      Document d = searcher.doc(docId);
      System.out.println((i + 1) + ". " + "\t" + d.get("title"));
    }

    index.close();
  }
  
  private static void addDoc(IndexWriter w, String title) throws IOException {
    Document doc = new Document();
    
    doc.add(new Field("title", title, Lucene.FULL_INDEX));
    w.addDocument(doc);
  }
}