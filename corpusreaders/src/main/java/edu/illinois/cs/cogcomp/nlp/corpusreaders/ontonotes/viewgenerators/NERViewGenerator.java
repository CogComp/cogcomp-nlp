/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.ontonotes.viewgenerators;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;

/**
 * This class will read NER data from the OntoNotes 5.0 corpora stored in it's SQL representation 
 * in a mySQL database and produce a TextAnnotation containing the sentences, tokens and NER view.
 * @author redman
 * @AvoidUsing(reason = "This class has been deprecated in favor of mechanism reading the data from the original files.",
            alternative = "OntonotesNamedEntityReader")
 */
public class NERViewGenerator {

	/**
	 * get a connection to the database.
	 * @param dburl the database url.
	 * @param user the user name
	 * @param password the password
	 * @return the connection.
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	static private Connection getConnection(final String dburl, final String user, final String password)
			throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.jdbc.Driver");
		return DriverManager.getConnection(dburl, user, password);
	}
		
	/**
	 * Remove junk left by annotators.
	 * @param string string to clean.
	 * @return the clean sentence.
	 */
	static private String fs(String string) {
		StringBuffer workspace = new StringBuffer();
		
		// match *<whatever not space>-<number> at start of line, or within preceeded by a space
		final Pattern p = Pattern.compile("\\*[^\\s-]*-[\\d]+|\\*PRO\\*|\\*\\?\\*");
		Matcher m = p.matcher(string);
		int where = 0;
		while (m.find()) {
			workspace.append(string.substring(where, m.start()));
			where = m.end() + 1;
		}
		
		// get the end of the string.
		if (where < string.length())
			workspace.append(string.substring(where));
		String s = workspace.toString();
		return s;
	}
	
	/**
	 * get the text annotation for a single document containing the tokenization and the NER view.
	 * @param con the connection.
	 * @param documentId the document id.
	 * @return the text annotation containing the NER view.
	 * @throws SQLException 
	 */
	static public TextAnnotation getTextAnnotation(Connection con, String documentId) throws SQLException {
		StringBuffer completetext = new StringBuffer();
		ArrayList<IntPair> offsets = new ArrayList<IntPair>();
		ArrayList<Integer> sentenceEndPositions = new ArrayList<Integer>();
		ArrayList<String> tokens = new ArrayList<String>();
		
		// fetch each sentence try to produce sentences and tokens and their associated offsets.
		String query = "select string,sentence_index from sentence \n"
				+ "    where document_id = '"+documentId+"' \n"
				+ "    order by sentence_index asc";
		int sentindex = 0;
		try (ResultSet rs = con.createStatement().executeQuery(query)) {
			while(rs.next()) {
				String sent = fs(rs.getString(1));
				int index = rs.getInt(2);
				if (index != sentindex) {
					
					// this is an error worth reporting, we expect the sentences to be
					// contiguous and to come in order.
					System.err.println("There was a sentence out of order, expected "+sentindex+", got "+index);
				}
				sentindex = index+1;
				int whereInCanonicalText = completetext.length();
				if (whereInCanonicalText > 0)
					completetext.append(' ');
				completetext.append(sent);
				
				// compute the start and end of each token.
				String[] splittoks = sent.split(" ");
				for (String token : splittoks) {
					int x = completetext.indexOf(token, whereInCanonicalText);
					whereInCanonicalText = x + token.length();
					IntPair ip = new IntPair(x, whereInCanonicalText);
					tokens.add(token);
					offsets.add(ip);
				}
				sentenceEndPositions.add(tokens.size());
			}
		}
		// convert the ArrayList of sent end positions to an array of primitive ints.
		int[] sep = new int[sentenceEndPositions.size()];
		for (int i = 0; i < sep.length; i++) 
			sep[i] = sentenceEndPositions.get(i);
		
        TextAnnotation ta = new TextAnnotation("ontonotes", documentId, completetext.toString(), 
        		offsets.toArray(new IntPair[offsets.size()]), tokens.toArray(new String[tokens.size()]), 
        		sep);
        
        query = "select type, start_word_index, end_word_index, sentence_index from name_entity \n"
				+ "    where document_id = '"+documentId+"' \n"
				+ "    order by sentence_index,start_word_index asc";
        // generate the NER view;
        SpanLabelView nerView = new SpanLabelView(ViewNames.NER_ONTONOTES, ta);
		try (ResultSet rs = con.createStatement().executeQuery(query)) {
			while(rs.next()) {
				String type = rs.getString(1);
				int start_word = rs.getInt(2);
				int end_word = rs.getInt(3);
				int sentence_index = rs.getInt(4);
				Sentence fuckmunky = ta.getSentence(sentence_index);
				int sp = fuckmunky.getStartSpan();
				nerView.addSpanLabel(sp+start_word, sp+end_word+1, type, 1d);
			}
		}
		ta.addView(ViewNames.NER_ONTONOTES, nerView);
        return ta;
	}
	
	/**
	 * This is used primarily for quick testing. The bulk of this should be used as a
	 * starting point for a unit test, question is can we include some of the files for
	 * the test in the code repository?
	 * @param args
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 
	static public void main(String[] args) throws ClassNotFoundException, SQLException {
		String test = "*POS* dinky little tyke . Hughy did not *?* comply .";
		System.out.println(fs(test));
	}

	/**
	 * This is used primarily for quick testing. The bulk of this should be used as a
	 * starting point for a unit test, question is can we include some of the files for
	 * the test in the code repository?
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
    static public void main(String[] args) throws ClassNotFoundException, SQLException {
		String document = "bc/cctv/00/cctv_0000@0000@cctv@bc@en@on";
		Connection con = getConnection("jdbc:mysql://localhost:3306/ontonotes", "cline","~reallyhard~");
		TextAnnotation ta = getTextAnnotation(con, document);
		SpanLabelView slv = (SpanLabelView) ta.getView(ViewNames.NER_ONTONOTES);
		System.out.println("---------------"+ta.getId()+"---------------");
		for (int i = 0; i < ta.getNumberOfSentences();  i++) {
			Sentence sentence = ta.getSentence(i);
			System.out.println(i+") "+sentence+"@"+sentence.getStartSpan());
			List<Constituent> nerlabels = slv.getConstituentsCovering(sentence.getSentenceConstituent());
			for (Constituent c : nerlabels) {
				System.out.println("    "+c.getTokenizedSurfaceForm()+"-"+c.getLabel());
			}
		}
		System.out.println();
	}
}
