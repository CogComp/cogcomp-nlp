/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.utilities;

import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.core.datastructures.trees.TreeParserFactory;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.illinois.cs.cogcomp.edison.utilities.NomLexEntry.ADJECTIVAL;
import static edu.illinois.cs.cogcomp.edison.utilities.NomLexEntry.NomLexClasses;

/**
 * Read NomLex from S-expressions
 *
 * @author Vivek Srikumar
 */
public class NomLexReader {

    private final static Logger logger = LoggerFactory.getLogger(NomLexReader.class);
    public static String nomLexFile = "NOMLEX-plus-clean.1.0";
    private static NomLexReader INSTANCE;
    private static boolean initialized = false;
    private HashMap<String, List<NomLexEntry>> nomLex;
    private Map<String, String> pluralToSingularMap;

    public NomLexReader(String nomLexFile) throws Exception {
        InputStream stream;
        if (!new File(nomLexFile).exists()) {
            logger.info("Loading {} from classpath", nomLexFile);
            List<URL> list = IOUtils.lsResources(NomLexReader.class, nomLexFile);
            if (list.isEmpty()) {
                logger.error("Could not load " + nomLexFile);
                System.exit(-1);
            }
            stream = list.get(0).openStream();
        } else {
            logger.info("Loading {} from local directory", nomLexFile);
            stream = new FileInputStream(nomLexFile);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

        nomLex = new HashMap<>();
        pluralToSingularMap = new HashMap<>();

        readNomLexLisp(reader);
    }

    public synchronized static NomLexReader getInstance() throws EdisonException {
        if (!initialized) {
            if (nomLexFile == null) {
                logger.error("Cannot initialize NomLex." + "Please set NomLexReader.nomLexFile "
                        + "to the correct value.");
                throw new EdisonException("NomLexReader.nomLexFile is not set");

            }

            logger.info("Loading NOMLEX. Looking for file {}", nomLexFile);
            try {
                INSTANCE = new NomLexReader(nomLexFile);

                initialized = true;
            } catch (Exception e) {
                throw new EdisonException(e);
            }
        }
        return INSTANCE;
    }

    private void readNomLexLisp(BufferedReader in) throws IOException {

        String line;
        int numParens = 0;
        StringBuffer current = new StringBuffer();
        while ((line = in.readLine()) != null) {

            boolean inQuotes = false;
            String prevChar = "";
            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                String c1 = c + "";

                if (c == '"') {
                    if (i > 0 && line.charAt(i - 1) != '\\')
                        inQuotes = !inQuotes;
                } else if (c == '(') {
                    if (!inQuotes) {
                        numParens++;
                    } else
                        c1 = "[";
                } else if (c == ')') {
                    if (!inQuotes) {
                        numParens--;
                    } else
                        c1 = "]";
                }

                if (prevChar.equals(c1) && c1.equals("("))
                    current.append("DummyNode");

                current.append(c1);

                prevChar = c1;

            }

            if (numParens == 0) {

                NomLexEntry entry = createNewRecord(current.toString());

                // logger.info(entry.nomClass + "\t" + entry.orth + "\t"
                // + entry.plural + "\t" + entry.verb + "\t" + entry.adj);

                if (!this.nomLex.containsKey(entry.orth))
                    this.nomLex.put(entry.orth, new ArrayList<NomLexEntry>());

                this.nomLex.get(entry.orth).add(entry);

                current = new StringBuffer();
                numParens = 0;

            }

        }
        in.close();
    }

    private NomLexEntry createNewRecord(String string) {
        Tree<String> tree = TreeParserFactory.getStringTreeParser().parse(string);

        NomLexEntry entry = new NomLexEntry();

        entry.nomClass = NomLexClasses.valueOf(tree.getLabel().replaceAll("-", "_"));

        // for (Tree<String> key : tree.getChildren()) {

        for (int childId = 0; childId < tree.getNumberOfChildren(); childId++) {
            Tree<String> key = tree.getChild(childId);
            String label = key.getLabel();

            if (!label.startsWith(":"))
                continue;

            Tree<String> value;
            if (key.isLeaf()) {
                if (childId == tree.getNumberOfChildren() - 1)
                    break;
                value = tree.getChild(childId + 1);
            } else
                value = key.getChild(0);

            String valueLabel = value.getLabel().replaceAll("\"", "");

            switch (label) {
                case ":ORTH":
                    entry.orth = valueLabel;
                    break;
                case ":VERB":
                    entry.verb = valueLabel;
                    break;
                case ":ADJ":
                    entry.adj = valueLabel;
                    break;
                case ":PLURAL":
                    if (!valueLabel.equals("*NONE*"))
                        entry.plural = valueLabel;
                    else
                        entry.plural = null;
                    break;
            }
        }

        if (entry.plural == null)
            entry.plural = entry.orth;
        else
            pluralToSingularMap.put(entry.plural, entry.orth);

        // assert NomLexEntry.VERBAL.contains(entry.nomClass) == (entry.verb !=
        // null) : "Nomclass = "
        // + entry.nomClass.name()
        // + ", verb = "
        // + entry.verb
        // + " for ORTH= " + entry.orth;

        if (ADJECTIVAL.contains(entry.nomClass) && entry.adj == null) {
            entry.adj = entry.orth;
        }

        //
        // assert NomLexEntry.ADJECTIVAL.contains(entry.nomClass) == (entry.adj
        // != null) : "NomClass = "
        // + entry.nomClass
        // + ", adj = "
        // + entry.adj
        // + " for orth = "
        // + entry.orth;

        return entry;
    }

    public boolean containsEntry(String token) {
        return this.nomLex.containsKey(token) || isPlural(token);
    }

    public List<NomLexEntry> getNomLexEntry(String token) {
        if (this.nomLex.containsKey(token))
            return this.nomLex.get(token);
        else if (isPlural(token)) {
            String orth = getSingular(token);
            return this.nomLex.get(orth);
        } else
            return null;
    }

    public String getSingular(String token) {
        return this.pluralToSingularMap.get(token);
    }

    public boolean isPlural(String token) {
        return this.pluralToSingularMap.containsKey(token);
    }

    public List<NomLexEntry> getNomLexEntries(String predicateWord, String predicateLemma) {

        if (this.containsEntry(predicateWord))
            return this.getNomLexEntry(predicateWord);
        else if (this.containsEntry(predicateLemma))
            return this.getNomLexEntry(predicateLemma);
        else
            return new ArrayList<>();
    }

}
