/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.wikifier.utils.examples;

import edu.illinois.cs.cogcomp.bigdata.lucene.Lucene;
import java.io.IOException;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryparser.classic.ParseException;


public class LuceneExample {
	public static void main(String[] args) throws IOException, ParseException {

		String pathToIndexDir = "testIndex";
		createIndex(pathToIndexDir);
		IndexReader reader = Lucene.reader(pathToIndexDir);
		Map<String, Float> idfs = Lucene.getIdfs(reader, "text");
		for (String k : idfs.keySet()) {
			System.out.println(k + " " + idfs.get(k));
		}
		System.out.println("TFS");
		for (int i = 0; i < reader.maxDoc(); i++) {
			System.out.println(reader.document(i).getField("title")
					.stringValue());
			Map<String, Float> tfs = Lucene.getTfs(reader, "text", i);
			for (String k : tfs.keySet()) {
				System.out.println(k + " " + tfs.get(k));
			}
		}

	}

	private static void createIndex(String pathToIndexDir) throws IOException {
		IndexWriter w = Lucene.writer(pathToIndexDir,
				Lucene.newConfig(Lucene.SIMPLE));
		addDoc(w, "Lucene in Action", "193398817",
				"This is a book on lucene and this book is awesome");
		addDoc(w, "Lucene for Dummies", "55320055Z",
				"Book for dummies. I read it to this day");
		addDoc(w, "Managing Gigabytes", "55063554A",
				"Book on managing gigabytes. Not very interesting");
		addDoc(w, "The Art of Computer Science", "9900333X",
				"Was this book written by Knuth?");
		addDoc(w, "Concrete Mathematics", "9900333X",
				"Cute book on combinatorics and other discrete maths. By Knuth and Graham");
		addDoc(w, "The Art of Zen", "9900333X", "Some book on Zen Buddhism");
		addDoc(w,
				"Hamlet",
				"12313123",
				"to be or not to be. Do be do be do.");
		addDoc(w, "Zen and Some Motorcycle Nonsense", "9900333X",
				"American take on Zen Buddhism. They mostly talk about motorcycles though");

		w.close();
	}

	private static void addDoc(IndexWriter w, String title, String isbn,
			String text) throws IOException {
		Document doc = new Document();
		doc.add(new TextField("title", title, Field.Store.YES));
		doc.add(new StringField("isbn", isbn, Field.Store.YES));
		doc.add(new Field("text", text, Lucene.FULL_INDEX));
		w.addDocument(doc);
	}
}