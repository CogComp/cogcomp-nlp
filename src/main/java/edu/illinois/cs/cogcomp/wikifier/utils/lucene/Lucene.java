package edu.illinois.cs.cogcomp.wikifier.utils.lucene;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Iterator;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.Version;

import com.google.common.collect.Iterators;

/**
 * Simplifed Lucene interface
 * @author cheng88 
 * @modified upadhya3
 *
 */
public class Lucene {

	
	public static FieldType FULL_INDEX = new FieldType();

    static{

        FULL_INDEX.setIndexed(true);

        FULL_INDEX.setTokenized(true);

        FULL_INDEX.setStored(true);

        FULL_INDEX.setStoreTermVectors(true);

        FULL_INDEX.setStoreTermVectorPositions(true);

        FULL_INDEX.setStoreTermVectorOffsets(true);

        FULL_INDEX.freeze();

    }
    
    public static FieldType JUST_INDEX = new FieldType();
    
    static{

    	JUST_INDEX.setIndexed(true);
    	JUST_INDEX.setStored(true);
    	JUST_INDEX.freeze();

    }
	
    public static final Version version = Version.LUCENE_43;
    public static final Analyzer ENGLISH = new EnglishAnalyzer(version);
    public static final Analyzer STANDARD = new StandardAnalyzer(version);
    public static final Analyzer SIMPLE = new SimpleAnalyzer(version);
    public static final Analyzer KEYWORD = new KeywordAnalyzer();
    public static final Analyzer WHITESPACE = new WhitespaceAnalyzer(version);
    public static final Analyzer AGGRESSIVE_TRANSFORM = new ASCIIEnglishAnalyzer(version);
    
    private static final IndexWriterConfig storeConfig = newConfig(new KeywordAnalyzer());
    

    public static IndexWriterConfig newConfig(Analyzer analyzer){
        return new IndexWriterConfig(version, analyzer);
    }
    
    public static IndexWriter writer(String pathToIndexDir,IndexWriterConfig config) throws IOException{
        return new IndexWriter(new MMapDirectory(new File(pathToIndexDir)),config);
    }
    
    public static IndexWriter simpleWriter(String pathToIndexDir) throws IOException{
        return writer(pathToIndexDir, newConfig(SIMPLE));
    }
//    public static IndexWriter simpleStemmingWriter(String indexDir) throws IOException{
//        return writer(indexDir, newConfig(new MinimalAnalyzer(version)));
//    }
    
    public static boolean isDeleted(IndexReader indexReader, int docID)
    {
    	Bits liveDocs = MultiFields.getLiveDocs(indexReader);
    	  if (!liveDocs.get(docID)) {
    	    // document is deleted...
    		  return true;
    	  }
    	  else
    		  return false;
    }
    
    public static IndexWriter storeOnlyWriter(String pathToIndexDir) throws IOException{
        return new IndexWriter(new MMapDirectory(new File(pathToIndexDir)),storeConfig);
    }

    public static IndexReader ramReader(String pathToIndex) throws IOException{
        return DirectoryReader.open(new RAMDirectory(new MMapDirectory(new File(pathToIndex)),IOContext.READ));
    }

    public static IndexReader reader(String dir,String... children) throws IOException{
        return reader(Paths.get(dir, children).toString());
    }

    public static IndexReader reader(String pathToIndex) throws IOException{
        return DirectoryReader.open(new MMapDirectory(new File(pathToIndex)));
    }
    
    public static IndexSearcher searcher(IndexReader reader) throws IOException{
        return new IndexSearcher(reader);
    }
    
    public static IndexSearcher searcher(Directory dir) throws IOException{
        return searcher(DirectoryReader.open(dir));
    }

    public static IndexSearcher searcher(String dir,String... children) throws IOException{
        return searcher(Paths.get(dir, children).toString());
    }

    public static IndexSearcher searcher(String path) throws IOException{
        return new IndexSearcher(reader(path));
    }
    
    /**
     * 
     * @param terms
     * @return an iterable terms represetnation of the terms in the index
     */
    public static Iterable<TermsEnum> terms(final Terms terms){
        return new Iterable<TermsEnum>() {
            @Override
            public Iterator<TermsEnum> iterator() {
                
                if (terms == null)
                    return Iterators.emptyIterator();
                
                return new Iterator<TermsEnum>() {
                    
                    private final TermsEnum te = safeEnum(terms);
                    private boolean available = false;
                    
                    @Override
                    public boolean hasNext() {
                        if(available)
                            return true;
                        loadNext();
                        return available;
                    }

                    @Override
                    public TermsEnum next() {
                        if(!available)
                            loadNext();
                        available = false;
                        return te;
                    }
                    
                    private void loadNext(){
                        try {
                            available = te.next() != null;
                        } catch (IOException e) {
                        }
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException("Can not remove terms from index");
                    }
                };
            }
        };
    }

    /**
     * 
     * @param terms
     * @return termsEnum without throwing exceptions
     */
    public static TermsEnum safeEnum(Terms terms){
        try {
            return terms.iterator(TermsEnum.EMPTY);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * Uses default analyzer
     * @param string
     * @return
     */
    public static QueryParser newQueryParser(String string) {
        return new QueryParser(version,string,ENGLISH);
    }
    
    /**
     * Searches the field with the exact integer value.
     * Suitable for ID indexing
     * @param field
     * @param id
     * @return
     */
    public static Query intQuery(String field, int id) {
        return NumericRangeQuery.newIntRange(field, id, id, true, true);
    }
    
    /**
     * Uses default query parser
     * @param field
     * @param query
     * @return
     * @throws ParseException 
     */
    public static Query newQuery(String field,String query) throws ParseException{
        return newQueryParser(field).parse(query);
    }

    public static boolean indexExists(String pageIndex) {
        try {
            return DirectoryReader.indexExists(new NIOFSDirectory(new File(pageIndex)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

	public static QueryParser newQueryParser(String field, Analyzer analyzer)
	{
		return new QueryParser(version, field, analyzer);
	}
}
