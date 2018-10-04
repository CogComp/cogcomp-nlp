/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.tokenizer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;

/**
 * Using an instance of a MySQL database containing the ontonotes data, validate the tokenizers
 * against one another. The database contains the full sentences, and the tokens extracted by the
 * OntoNotes project, this class will run our tokenizers (Stateful and Illinois) and see how they
 * compare, reporting any differences.
 * 
 * @author redman
 *
 */
public class TokenizerValidation {
    /** the account name. */
    static String account = null;

    /** the password. */
    static String password = null;

    /** the database url. */
    static String dburl = null;

    /**
     * parse the arguments, order is everything.
     * 
     * @param args the arguments.
     */
    private static void parseArgs(String[] args) {
        if (args.length != 3) {
            System.err
                    .println("You must pass <user account>, <password>, and <database URL> as arguments to this function.");
            System.exit(-1);
        }
        account = args[0];
        password = args[1];
        dburl = args[2];
    }

    /**
     * @return the database connection.
     * @throws SQLException if database issues arise.
     */
    private static Connection getConnection() throws SQLException {
        Connection con = null;
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            throw new SQLException("Could not instantiate the driver MySQL.", ex);
        }
        Properties connectionProps = new Properties();
        connectionProps.put("user", account);
        connectionProps.put("password", password);
        con = DriverManager.getConnection(dburl, connectionProps);
        con.setAutoCommit(false);
        return con;
    }

    /**
     * @param args
     * @throws SQLException
     */
    public static void main(String[] args) throws SQLException {
        parseArgs(args);

        // create both tokenizers
        TextAnnotationBuilder statefulBuilder =
                new TokenizerTextAnnotationBuilder(new StatefulTokenizer());
        TextAnnotationBuilder ilBuilder =
                new TokenizerTextAnnotationBuilder(new IllinoisTokenizer());
        String snt = "At .5 or 3.5 decibles.";
        TextAnnotation tr = statefulBuilder.createTextAnnotation("test1", "state", snt);
        List<Constituent> t = tr.getView(ViewNames.TOKENS).getConstituents();
        TextAnnotation tr2 = ilBuilder.createTextAnnotation("test1", "state", snt);
        List<Constituent> t2 = tr2.getView(ViewNames.TOKENS).getConstituents();
        int idx = 0;
        for (Constituent ntt : t)
            System.out.println(ntt.getSurfaceForm());
        System.err.println();
        for (Constituent ntt : t2)
            System.out.println(ntt.getSurfaceForm());

        Connection con = getConnection();
        String sentencequery =
                "SELECT s.id,s.no_trace_string FROM sentence s, document d, subcorpus c \n"
                        + "    where s.document_id = d.id AND d.subcorpus_id = c.id AND c.language_id = 'en'";

        // issue the query, process one string at a time.
        int counter = 0;
        int bad = 0;
        try (ResultSet rs1 = con.createStatement().executeQuery(sentencequery)) {
            while (rs1.next()) {
                counter++;
                // System.out.println("counter = "+counter);
                String id = rs1.getString(1);
                String sentence = rs1.getString(2);
                if (sentence.length() == 0)
                    continue;
                sentence = sentence.replaceAll(" 's ", "'s ");
                sentence = sentence.replaceAll(" 'S ", "'S ");
                sentence = sentence.replaceAll(" 'm ", "'m ");
                sentence = sentence.replaceAll(" 're ", "'re ");
                sentence = sentence.replaceAll(" 'nt ", "'nt ");
                sentence = sentence.replaceAll(" 've ", "'ve ");
                sentence = sentence.replaceAll(" 'd ", "'d ");
                sentence = sentence.replaceAll(" 'll ", "'ll ");
                sentence = sentence.replaceAll(" do n't ", " don't ");
                TextAnnotation stateful = null;
                try {
                    stateful = statefulBuilder.createTextAnnotation("test1", "state", sentence);
                } catch (ArrayIndexOutOfBoundsException aioobe) {
                    System.err.println("Bad Sentence : " + sentence);
                    System.exit(1);;
                }
                TextAnnotation il = ilBuilder.createTextAnnotation("test2", "il", sentence);
                List<Constituent> statefulToks =
                        stateful.getView(ViewNames.TOKENS).getConstituents();
                List<Constituent> ilToks = il.getView(ViewNames.TOKENS).getConstituents();

                // get the provided tokens.
                int sidx = 0;
                int iidx = 0;
                for (; true;) {
                    if (sidx < statefulToks.size() && iidx < ilToks.size()) {
                        String stok = statefulToks.get(sidx).getSurfaceForm();
                        String itok = ilToks.get(iidx).getSurfaceForm();
                        if (!stok.equals(itok)) {
                            System.out.println(sentence);
                            System.out.println("stateful:" + stok + " il:" + itok);
                            bad++;
                            break;
                        }
                    } else {
                        if (statefulToks.size() != ilToks.size()) {
                            System.out.println(sentence);
                            System.out.println("stateful size:" + statefulToks.size() + " il size:"
                                    + ilToks.size());
                            bad++;
                        }
                        break;
                    }
                    sidx++;
                    iidx++;
                }

                /**
                 * String tokequery ="select t.word from token t where t.id like '%"+id+
                 * "' AND part_of_speech!='-NONE-'"; try (ResultSet rs2 =
                 * con.createStatement().executeQuery(tokequery)) { int iindex = 0; int sindex = 0;
                 * while (rs2.next()) { String word = rs2.getString(1); if (sindex >=
                 * statefulToks.size()) { System.out.println("On token '"+word+
                 * ", stateful parsing revealed too few tokens."); } else { String stok =
                 * statefulToks.get(sindex).getSurfaceForm(); if (!word.equals(stok))
                 * System.out.println("On token '"+word+"', stateful parsing token was '"+stok+"'");
                 * } if (iindex >= ilToks.size()) { System.out.println("On token '"+word+
                 * "', illinois parsing revealed too few tokens."); } else { String stok =
                 * ilToks.get(iindex).getSurfaceForm(); if (!word.equals(stok))
                 * System.out.println("On token '"+word+"', illinois parsing token was '"+stok+"'");
                 * 
                 * } iindex++; sindex++; } }
                 */
            }
        }
        System.out.println("Done of " + counter + ", " + bad + " were bad.");
    }

}
