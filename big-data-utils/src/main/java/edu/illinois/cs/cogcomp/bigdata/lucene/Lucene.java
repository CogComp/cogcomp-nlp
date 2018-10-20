/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.bigdata.lucene;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

import com.google.common.collect.Iterators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simplifed Lucene interface
 * 
 * @author cheng88
 * @modified upadhya3
 *
 */
public class Lucene {

	/**
	 * use this when you want to store term vectors etc.
	 */
	public static FieldType FULL_INDEX = new FieldType();

	static {
		FULL_INDEX.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
		FULL_INDEX.setTokenized(true);
		FULL_INDEX.setStored(true);
		FULL_INDEX.setStoreTermVectors(true);
		FULL_INDEX.setStoreTermVectorPositions(true);
		FULL_INDEX.setStoreTermVectorOffsets(true);
		FULL_INDEX.freeze();
	}

	public static FieldType JUST_INDEX = new FieldType();

	static {

		JUST_INDEX.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
		JUST_INDEX.setStored(true);
		JUST_INDEX.freeze();

	}

	public static final Version version = Version.LUCENE_6_0_0; // change this when
																// using a
																// different
																// version
	public static final Analyzer ENGLISH = new EnglishAnalyzer();
	public static final Analyzer STANDARD = new StandardAnalyzer();
	public static final Analyzer SIMPLE = new SimpleAnalyzer();
	public static final Analyzer KEYWORD = new KeywordAnalyzer();
	public static final Analyzer WHITESPACE = new WhitespaceAnalyzer();
	public static final Analyzer AGGRESSIVE_TRANSFORM = new ASCIIEnglishAnalyzer();
	public static final Analyzer MINIMAL = new MinimalAnalyzer(version);
	
	private static final IndexWriterConfig storeConfig = newConfig(new KeywordAnalyzer());

	private static final Logger logger = LoggerFactory.getLogger(Lucene.class);
	/**
	 * creates a config for a writer using a specified analyzer
	 * 
	 * @param analyzer
	 * @return
	 */
	public static IndexWriterConfig newConfig(Analyzer analyzer) {
		return new IndexWriterConfig(analyzer);
	}

	/**
	 * returns a index writer with the specified writer config.
	 * 
	 * @param pathToIndexDir
	 * @param config
	 * @return
	 * @throws IOException
	 */
	public static IndexWriter writer(String pathToIndexDir,
			IndexWriterConfig config) throws IOException{
		return new IndexWriter(new MMapDirectory(Paths.get(pathToIndexDir)),
				config);
	}

	/**
	 * returns a index writer configured to use a simple analyzer
	 * 
	 * @param pathToIndexDir
	 * @return
	 * @throws IOException
	 */
	public static IndexWriter simpleWriter(String pathToIndexDir)
			throws IOException {
		return writer(pathToIndexDir, newConfig(SIMPLE));
	}

	
	public static IndexWriter simpleStemmingWriter(String indexDir)
			throws IOException {
		return writer(indexDir, newConfig(MINIMAL));
	}

	/**
	 * checks if docId is deleted in the index
	 * 
	 * @param indexReader
	 * @param docID
	 * @return
	 */
	public static boolean isDeleted(IndexReader indexReader, int docID) {
		Bits liveDocs = MultiFields.getLiveDocs(indexReader);
		if (!liveDocs.get(docID)) {
			// document is deleted...
			return true;
		} else
			return false;
	}

	public static IndexWriter storeOnlyWriter(String pathToIndexDir)
			throws IOException {
		return new IndexWriter(new MMapDirectory(Paths.get(pathToIndexDir)),
				storeConfig);
	}

	public static IndexReader ramReader(String pathToIndex) throws IOException {
		return DirectoryReader.open(new RAMDirectory(new MMapDirectory(
				Paths.get(pathToIndex)), IOContext.READ));
	}

	public static IndexReader reader(String dir, String... children)
			throws IOException {
		return reader(Paths.get(dir, children).toString());
	}

	public static IndexReader reader(String pathToIndex) throws IOException {
		return DirectoryReader.open(new MMapDirectory(Paths.get(pathToIndex)));
	}

	public static IndexSearcher searcher(IndexReader reader) throws IOException {
		return new IndexSearcher(reader);
	}

	public static IndexSearcher searcher(Directory dir) throws IOException {
		return searcher(DirectoryReader.open(dir));
	}

	public static IndexSearcher searcher(String dir, String... children)
			throws IOException {
		return searcher(Paths.get(dir, children).toString());
	}

	public static IndexSearcher searcher(String path) throws IOException {
		return new IndexSearcher(reader(path));
	}

	/**
	 * 
	 * @param terms
	 * @return an iterable terms representation of the terms in the index
	 */
	public static Iterable<TermsEnum> terms(final Terms terms) {
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
						if (available)
							return true;
						loadNext();
						return available;
					}

					@Override
					public TermsEnum next() {
						if (!available)
							loadNext();
						available = false;
						return te;
					}

					private void loadNext() {
						try {
							available = te.next() != null;
						} catch (IOException e) {
						}
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException(
								"Can not remove terms from index");
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
	public static TermsEnum safeEnum(Terms terms) {
		try {
			return terms.iterator();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Uses default analyzer
	 * 
	 * @param string
	 * @return
	 */
	public static QueryParser newQueryParser(String string) {
		return new QueryParser(string, ENGLISH);
	}

	/**
	 * Searches the field with the exact integer value. Suitable for ID indexing
	 * 
	 * @param field
	 * @param id
	 * @return
	 */
//	public static Query intQuery(String field, int id) {
//		return NumericRangeQuery.newIntRange(field, id, id, true, true);
//	}

	/**
	 * Uses default query parser
	 * 
	 * @param field
	 * @param query
	 * @return
	 * @throws ParseException
	 */
	public static Query newQuery(String field, String query)
			throws ParseException {
		return newQueryParser(field).parse(query);
	}

	public static boolean indexExists(String pageIndex) {
		try {
			return DirectoryReader.indexExists(new NIOFSDirectory(Paths.get(
					pageIndex)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static QueryParser newQueryParser(String field, Analyzer analyzer) {
		return new QueryParser(field, analyzer);
	}

	/**
	 * uses defaultSimilarity to compute idf. DefaultSimilarity computes idf as
	 * 1 + log (numDocs/ docFreq + 1)
	 * 
	 * @param reader
	 * @param field
	 * @return
	 * @throws IOException
	 */
	public static Map<String, Float> getIdfs(IndexReader reader, String field)
			throws IOException {
		// DefaultSimilarity computes idf as 1 + log (numDocs/ docFreq + 1)
		return getIdfs(reader, field, new ClassicSimilarity());
	}

	/**
	 * uses custom similarity to compute idf, use this if you want to implement
	 * IDF(numDocs,docFreq)
	 * 
	 * @param reader
	 * @param field
	 * @param tfidfSIM
	 * @return
	 * @throws IOException
	 */
	public static Map<String, Float> getIdfs(IndexReader reader, String field,
			TFIDFSimilarity tfidfSIM) throws IOException {
		Map<String, Float> docFrequencies = new HashMap<>();

		TermsEnum termEnum = MultiFields.getTerms(reader, field).iterator();
		BytesRef bytesRef;
		while ((bytesRef = termEnum.next()) != null) {
			if (termEnum.seekExact(bytesRef)) {
				String term = bytesRef.utf8ToString();

				float idf = tfidfSIM.idf(termEnum.docFreq(), reader.numDocs());
				docFrequencies.put(term, idf);
			}
		}

		return docFrequencies;
	}

	/**
	 * returns term freq for a given doc.
	 * 
	 * @param reader
	 * @param field
	 * @return
	 * @throws IOException
	 */
	public static Map<String, Float> getTfs(IndexReader reader, String field,
			int docID) throws IOException {
		Map<String, Float> termFrequencies = new HashMap<>();
		Terms terms = reader.getTermVector(docID, field);
		TermsEnum itr = terms.iterator();
		BytesRef term = null;
		while ((term = itr.next()) != null) {
			String termText = term.utf8ToString();
			long termFreq = itr.totalTermFreq(); // term freq in doc with docID
			termFrequencies.put(termText, (float) termFreq);
		}
		return termFrequencies;
	}


	/**
	 * returns tf-idf = (term_freq/inversve_doc_freq) for a given doc and a term. 
	 *
	 * @param reader
	 * @param docIdField
	 * @param docId
	 * @return
	 * @throws IOException
	 */
	public static int getLuceneDocId(IndexReader reader, String docIdField, String docId) throws IOException {
		int luceneDocId = -1;
		IndexSearcher searcher = new IndexSearcher(reader);
		QueryParser parser = new QueryParser(docIdField, KEYWORD);
		Query q = new TermQuery(new Term(docIdField, docId));

		ScoreDoc[] docs = searcher.search(q, 1).scoreDocs;
		if (docs.length == 0) {
			logger.error("Document with docId : " + docId + "  not found!");
			System.exit(0);
			return -1;
		} else {
			luceneDocId = docs[0].doc;  // Lucene DocId
			return luceneDocId;
		}
	}

	/**
	 * returns tf-idf = (term_freq/inversve_doc_freq) for a given doc and a term.
	 *
	 * @param reader
	 * @param docIdField
	 * @param docId
	 * @param textField
	 * @param term
	 * @return
	 * @throws IOException
	 */
	public static double getTfIdf(IndexReader reader, String docIdField, String docId, String textField, String term) throws IOException {
		int luceneDocId = getLuceneDocId(reader, docIdField, docId);  // Lucene DocId
		return getTfIdf(reader, luceneDocId, textField, term);
	}

	/**
	 * returns tf-idf = (term_freq/inversve_doc_freq) for a given doc and a term.
	 *
	 * @param reader
	 * @param luceneDocId
	 * @param textField
	 * @param term
	 * @return
	 * @throws IOException
	 */
	public static double getTfIdf(IndexReader reader, int luceneDocId, String textField, String term) throws IOException {
		double tf = getTf(reader, luceneDocId, textField, term);
		double idf = getIdf(reader, textField, term);
		return tf*idf;
	}

	/**
	 * returns tf for a given term in a given doc
	 *
	 * @param reader
	 * @param luceneDocId
	 * @param textField
	 * @param term
	 * @return
	 * @throws IOException
	 */
	public static double getTf(IndexReader reader, int luceneDocId, String textField, String term) throws IOException {
		Map<String, Float> tfs = getTfs(reader, textField, luceneDocId);
		term = term.trim();
		if(tfs.containsKey(term))
			return tfs.get(term);

		else if(tfs.containsKey(term.toLowerCase()))
			return tfs.get(term.toLowerCase());

		else
			return 0.0;
	}

	/**
	 * returns tf for a given term in a given doc
	 *
	 * @param reader
	 * @param docIdField
	 * @param docId
	 * @param textField
	 * @param term
	 * @return
	 * @throws IOException
	 */
	public static double getTf(IndexReader reader, String docIdField, String docId, String textField, String term) throws IOException {
		int luceneDocId = getLuceneDocId(reader, docIdField, docId);
		return getTf(reader, luceneDocId, textField, term);
	}

	/**
	 * returns idf for a given term
	 *
	 * @param reader
	 * @param textField
	 * @param term
	 * @return
	 * @throws IOException
	 */
	public static double getIdf(IndexReader reader, String textField, String term) throws IOException {
		Map<String, Float> idfs = getIdfs(reader, textField);
		term = term.trim();
		if(idfs.containsKey(term))
			return idfs.get(term);

		else if(idfs.containsKey(term.toLowerCase()))
			return idfs.get(term.toLowerCase());

		else
			return 0.0;
	}

}
