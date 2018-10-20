/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.ParsingProcessingData;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import edu.illinois.cs.cogcomp.ner.LbjTagger.NERDocument;
import edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord;
import edu.illinois.cs.cogcomp.ner.LbjTagger.ParametersForLbjCode;
import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

public class TaggedDataReader {
    private static Logger logger = LoggerFactory.getLogger(TaggedDataReader.class);

    public static NERDocument parseTextAnnotatedWithBrackets(String annotatedText,
            String documentName, ParametersForLbjCode cp) throws Exception {
        return BracketFileReader.parseTextWithBrackets(annotatedText, documentName, cp);
    }

    public static Vector<NERDocument> readFolder(String path, String format, ParametersForLbjCode cp) throws Exception {
        Vector<NERDocument> res = new Vector<>();
        String[] files = (new File(path)).list();

        // sort the files so we can get deterministic order.
        if (cp.sortLexicallyFilesInFolders) {
            Arrays.sort(files);
        }
        for (String file1 : files) {
            String file = path + "/" + file1;
            if ((new File(file)).isFile() && (!file1.equals(".DS_Store"))) {
                res.addElement(readFile(file, format, file1, cp));
            }
        }
        if (cp.treatAllFilesInFolderAsOneBigDocument) {
            // connecting sentence boundaries
            for (int i = 0; i < res.size() - 1; i++) {
                ArrayList<LinkedVector> ss1 = res.elementAt(i).sentences;
                ArrayList<LinkedVector> ss2 = res.elementAt(i + 1).sentences;
                if (ss1.size() > 0 && ss1.get(ss1.size() - 1).size() > 0 && ss2.size() > 0
                        && ss2.get(0).size() > 0) {
                    NEWord lastWord1 =
                            (NEWord) ss1.get(ss1.size() - 1)
                                    .get(ss1.get(ss1.size() - 1).size() - 1);
                    NEWord firstWord2 = (NEWord) ss2.get(0).get(0);
                    lastWord1.nextIgnoreSentenceBoundary = firstWord2;
                    firstWord2.previousIgnoreSentenceBoundary = lastWord1;
                }
            }
        }

        logger.info("Read " + files.length + " files from " + path);

        return res;
    }

    public static NERDocument readFile(String path, String format, String documentName, ParametersForLbjCode cp)
            throws Exception {
        NERDocument res = null;
        if (format.equals("-c")) {
            res = (new ColumnFileReader(path, cp)).read(documentName);
        } else if (format.equals("-r")) {
            res = BracketFileReader.read(path, documentName, cp);
        }else if (format.equals("-json")) {
            TextAnnotation ta = SerializationHelper.deserializeTextAnnotationFromFile(path, true);
            res = TextAnnotationConverter.getNerDocument(ta, cp);
        } else {
            System.err.println("Fatal error: unrecognized file format: " + format);
            System.exit(0);
        }
        connectSentenceBoundaries(res.sentences);
        return res;
    }

    public static void connectSentenceBoundaries(ArrayList<LinkedVector> sentences) {
        // connecting sentence boundaries
        for (int i = 0; i < sentences.size(); i++) {
            for (int j = 0; j < sentences.get(i).size(); j++) {
                NEWord w = (NEWord) sentences.get(i).get(j);
                w.previousIgnoreSentenceBoundary = (NEWord) w.previous;
                w.nextIgnoreSentenceBoundary = (NEWord) w.next;
            }
            if (i > 0 && sentences.get(i).size() > 0) {
                NEWord w = (NEWord) sentences.get(i).get(0);
                w.previousIgnoreSentenceBoundary =
                        (NEWord) sentences.get(i - 1).get(sentences.get(i - 1).size() - 1);
            }
            if (i < sentences.size() - 1 && sentences.get(i).size() > 0) {
                NEWord w = (NEWord) sentences.get(i).get(sentences.get(i).size() - 1);
                w.nextIgnoreSentenceBoundary = (NEWord) sentences.get(i + 1).get(0);
            }
        }
    }

    public static void sortFilesLexicographically(String[] files) {
        for (int i = 0; i < files.length; i++) {
            for (int j = i + 1; j < files.length; j++) {
                if (files[i].compareTo(files[j]) > 0) {
                    String s = files[i];
                    files[i] = files[j];
                    files[j] = s;
                }
            }
        }
    }
}
