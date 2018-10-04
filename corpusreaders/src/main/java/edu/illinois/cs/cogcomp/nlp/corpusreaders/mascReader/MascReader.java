/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.mascReader;

import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.AbstractIncrementalCorpusReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.CorpusReaderConfigurator;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import gnu.trove.map.hash.TIntIntHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * A reader for the MASC Open National Corpus newswire section (and possibly others).
 * This part of the MASC corpus has plain text files and separate files with stand-off
 *     annotation in xml format.
 * Initial implementation: segmentation only
 *
 * ...-seg.xml file:
 *     <region xml:id="seg-r0" anchors="4 10"/>
 *     <region xml:id="seg-r2" anchors="11 17"/>
 *     ...
 *
 * ...-s.xml file:
 *
 *  <a xml:id="s-N65573" label="s" ref="s-n0" as="anc"/>
 *  <region xml:id="s-r1" anchors="140 161"/>
 *  <node xml:id="s-n1">
 *     <link targets="s-r1"/>
 *  </node>
 *  <a xml:id="s-N65589" label="q" ref="s-n1" as="anc"/>
 *  <region xml:id="s-r2" anchors="345 598"/>
 *  <node xml:id="s-n2">
 *     <link targets="s-r2"/>
 *  </node>
 *
 * -- 'region' element is the lowest level: specifies an id and a pair of char offsets.
 * -- 'node' element groups regions, may potentially assign attributes
 * -- 'a' element assigns an id with a label 'q' or 's' (quote vs stmt?) to a separate 'node' element
 *
 * Specify desired annotations as a comma-separated list of values for Property "ANNOTATIONS" defined in
 *    {@link MascReaderConfigurator}.
 *
 * @author mssammon
 */
public class MascReader extends AbstractIncrementalCorpusReader<TextAnnotation> {

    private static final String NAME = MascReader.class.getCanonicalName();
    private static final Logger logger = LoggerFactory.getLogger(MascReader.class);


    public enum FileType {TEXT, SENTENCE, TOKENS, POS, NE, NC, VC, PENN, LOGICAL};
    public static Map<FileType, String> fileTypeExtensions;

    static {
        fileTypeExtensions = new HashMap<>();
        fileTypeExtensions.put(FileType.TEXT, ".txt");
        fileTypeExtensions.put(FileType.SENTENCE, "-s.xml");
        fileTypeExtensions.put(FileType.TOKENS, "-seg.xml");
        fileTypeExtensions.put(FileType.POS, "-penn.xml");
        fileTypeExtensions.put(FileType.NE, "-ne.xml");
        fileTypeExtensions.put(FileType.NC, "-nc.xml");
        fileTypeExtensions.put(FileType.VC, "-vc.xml");
        fileTypeExtensions.put(FileType.LOGICAL, "-logical.xml");
    }

    private SentenceStaxParser sentenceParser;
    private TokenStaxParser tokenParser;
    private PennStaxParser pennParser;
    private boolean readPenn;

    /** if set to 'true', prints out a crude format marking sentence boundaries read from documents.*/
    private final boolean DEBUG = false;

    /**
     * ResourceManager must specify the fields {@link CorpusReaderConfigurator}.CORPUS_NAME and
     * .CORPUS_DIRECTORY, plus whatever is required by the derived class for initializeReader().
     *
     * @param rm ResourceManager
     * @throws Exception
     */
    public MascReader(ResourceManager rm) throws Exception {
        super(new MascReaderConfigurator().getConfig(rm));
    }

    /**
     * specify annotation sources.
     */
    @Override
    public void initializeReader() {
        super.initializeReader();
        sentenceParser = new SentenceStaxParser();
        tokenParser = new TokenStaxParser();
        readPenn = resourceManager.getBoolean(MascReaderConfigurator.READ_PENN.key);

        if (readPenn)
            pennParser = new PennStaxParser();
    }

    /**
     * MASC directory structure:
     *    .../written/genre/source/
     *       id.txt (source text)
     *       id-s.xml (sentences)
     *       id-seg.xml (token boundaries)
     *       id-penn.xml (pos and lemma)
     *
     * CURRENT LIMITATION: expects every file name (independent of directory) to be unique.
     *
     * @return a list of lists of paths: each member list is a set of files containing source and
     *         standoff annotations. Annotation files will have suffixes from the map defined
     *         in field {@link #fileTypeExtensions}.
     * @throws IOException if the paths are not specified correctly, causing failure to read
     *         files expected to be present
     */
    @Override
    public List<List<Path>> getFileListing() throws IOException {
        String sourceDir = resourceManager.getString(CorpusReaderConfigurator.SOURCE_DIRECTORY);

        Set<String> genres = new HashSet<>(Arrays.asList(resourceManager.getCommaSeparatedValues(
                MascReaderConfigurator.GENRES.key)));
        List<String> annotations = Arrays.asList(resourceManager.getCommaSeparatedValues(
                MascReaderConfigurator.ANNOTATIONS.key));
        List<List<Path>> corpusPaths = new ArrayList<>();
        Map<String, List<Path>> stemToAnnotationPaths = new HashMap<>();
        Map<String, Path> stemToSourcePath = new HashMap<>();

        for (String genre : genres) {

            String dir = Paths.get(sourceDir, genre).toString();

            String[] sourceFiles = IOUtils.lsFilesRecursive(dir, file ->
                    file.isDirectory() || file.getAbsolutePath().endsWith(fileTypeExtensions.get(FileType.TEXT)));
            // A directory should always be accepted

            for (String sourceFile : sourceFiles) {
                Path sourcePath = Paths.get(sourceFile);
                String stem = IOUtils.getFileStem(sourceFile);
                List<Path> corpusFilePaths = stemToAnnotationPaths.get(stem);
                if (null == corpusFilePaths) {
                    corpusFilePaths = new ArrayList<>();
                    stemToAnnotationPaths.put(stem, corpusFilePaths);
                }
                stemToSourcePath.put(stem, sourcePath);

//                    check only text files are listed: seems like it is not true
                String path = sourcePath.getParent().toString();

                for (String annotation : annotations) {

                    String extension = MascReader.fileTypeExtensions.get(FileType.valueOf(annotation));
                    Path annotationPath = Paths.get(path, stem + extension);

                    if (FileType.valueOf(annotation).equals(FileType.TEXT))
                        stemToSourcePath.put(stem, annotationPath);

                    corpusFilePaths.add(annotationPath);
                }
            }
        }

        for (String stem : stemToSourcePath.keySet()) {
            List<Path> filePaths = new ArrayList<>();
            filePaths.add(stemToSourcePath.get(stem));
            filePaths.addAll(stemToAnnotationPaths.get(stem));
            corpusPaths.add(filePaths);
        }
        return corpusPaths;
    }

    /**
     * parse the set of files representing the source document text and annotations. Currently, only populates
     *      SENTENCE and TOKENS views.
     * NOTE: constraints imposed by superclasses make using the files harder than necessary: not all files have
     *      the full set of annotations in this corpus
     * @param corpusFileListEntry corpus file containing content to be processed
     * @return a list containing one TextAnnotation file, corresponding to one source text file plus
     *    annotations
     * @throws Exception if files can't be found, or if parser fails to read annotation format
     */
    @Override
    public List<TextAnnotation> getAnnotationsFromFile(List<Path> corpusFileListEntry) throws Exception {

        Path sentPath = null;
        Path tokPath = null;
        Path textPath = null;
        Path pennPath = null;

        for (Path p : corpusFileListEntry) {
            String fileName = p.getFileName().toString();
            if (fileName.endsWith(fileTypeExtensions.get(FileType.SENTENCE)))
                sentPath = p;
            else if (fileName.endsWith(fileTypeExtensions.get(FileType.TOKENS)))
                tokPath = p;
            else if (fileName.endsWith(fileTypeExtensions.get(FileType.TEXT)))
                textPath = p;
            else if (readPenn && fileName.endsWith(fileTypeExtensions.get(FileType.PENN)))
                pennPath = p;
            // else ignore
        }
        if (!Files.exists(sentPath))
            throw new IllegalArgumentException("sentence file '" + sentPath.getFileName() + "' does not exist.");

        if (!Files.exists(tokPath))
            throw new IllegalArgumentException("tokens file '" + tokPath.getFileName() + "' does not exist.");

        if (!Files.exists(textPath))
            throw new IllegalArgumentException("text file '" + textPath.getFileName() + "' does not exist.");

        if (readPenn && !Files.exists(textPath))
            throw new IllegalArgumentException("text file '" + textPath.getFileName() + "' does not exist.");


        return buildTextAnnotation(textPath, sentPath, tokPath, pennPath);
    }

    private List<TextAnnotation> buildTextAnnotation(Path textPath, Path sentPath, Path tokPath, Path pennPath) throws FileNotFoundException, XMLStreamException {

        String fileStem = IOUtils.getFileStem(textPath.toFile().getName());
        String text = LineIO.slurp(textPath.toFile().getAbsolutePath());
        List<Pair<String, IntPair>> tokenInfo = tokenParser.parseFile(tokPath.toFile().getAbsolutePath());
        Pair<List<SentenceStaxParser.MascSentence>, List<SentenceStaxParser.MascSentenceGroup>> sentenceInfo =
                sentenceParser.parseFile(sentPath.toFile().getAbsolutePath());

        String[] tokens = new String[tokenInfo.size()];
        int[] sentEndTokOffsets = new int[sentenceInfo.getFirst().size()];
        IntPair[] tokOffsets = new IntPair[tokens.length];
        TIntIntHashMap tokEndTotokIndex = new TIntIntHashMap();
        int index = 0;

        for (Pair<String, IntPair> tok : tokenInfo) {
            //TODO: check indexing is one-past-the-end
            tokens[index] = text.substring(tok.getSecond().getFirst(), tok.getSecond().getSecond());
            tokOffsets[index] = tok.getSecond();
            tokEndTotokIndex.put(tok.getSecond().getSecond(), index);
            index++;
        }

        List<SentenceStaxParser.MascSentence> sentences = sentenceInfo.getFirst();

        if (DEBUG) {
            printSentences(System.err, text, sentences);
        }

        removeOverlappingSentences(sentences);

        int lastIndex = -1;
        for (int i = 0; i < sentences.size(); ++i) {
            int newIndex = tokEndTotokIndex.get(sentences.get(i).end) + 1;
            if (newIndex < lastIndex)
                throw new IllegalStateException("sentence end before beginning -- doc '" + sentPath.toString() +
                "', sent id '" + sentences.get(i).regionId + "', start " + sentences.get(i).start +
                ", end " + sentences.get(i).end);
            sentEndTokOffsets[i] = newIndex; //one-past-the-end index.
            lastIndex = newIndex;
        }
        if (sentEndTokOffsets[sentences.size() -1] == tokens.length -1)
            sentEndTokOffsets[sentences.size() -1] = tokens.length;

        // FIX case where trailing content not part of sentnce in MASC -- fake final sentence
        // needed due to constraints imposed by TextAnnotation
        if (sentEndTokOffsets[sentences.size() -1] != tokens.length) {
            int[] modSentEndTokOffsets = new int[sentences.size() + 1];
            for (int i = 0; i < sentEndTokOffsets.length; ++i)
                modSentEndTokOffsets[i] = sentEndTokOffsets[i];
            modSentEndTokOffsets[modSentEndTokOffsets.length - 1] = tokens.length;
            sentEndTokOffsets = modSentEndTokOffsets;
        }

        TextAnnotation ta = new TextAnnotation(super.corpusName, fileStem, text, tokOffsets, tokens, sentEndTokOffsets);

        if (readPenn) {
            addLemmaAndPos(ta, tokenInfo, pennPath);
        }

        return Collections.singletonList(ta);
    }

    private void printSentences(PrintStream out, String text, List<SentenceStaxParser.MascSentence> sentences) {
        out.println("TEXT:\n" + text);
        out.println("\n\nSENTENCES:\n");
        for (SentenceStaxParser.MascSentence sent : sentences) {
            out.println("###" + text.substring(sent.start, sent.end) + "###");
        }
        out.println("____ENDOFDOC____");
    }

    /**
     * This method may be redundant at this point
     * @param sentences
     */
    private void removeOverlappingSentences(List<SentenceStaxParser.MascSentence> sentences) {

        Set<IntPair> offsetsToRemove = new HashSet<>();
        Map<IntPair, SentenceStaxParser.MascSentence> offsetsToSentences = new HashMap<>();
        Set<SentenceStaxParser.MascSentence> sentsToRemove = new HashSet<>();

        for (SentenceStaxParser.MascSentence sent : sentences) {
            IntPair sentOffset = new IntPair(sent.start, sent.end);
            for (IntPair offset : offsetsToSentences.keySet()) {
                if (isInside(sentOffset, offset)) {
                    sentsToRemove.add(sent);
                    break;
                }
                else if (isInside(offset, sentOffset))
                    offsetsToRemove.add(offset);
                else if (isOverlap(offset, sentOffset)) {
                    if (isLarger(offset, sentOffset)) {
                        sentsToRemove.add(sent);
                        break;
                    }
                    else
                        offsetsToRemove.add(offset);
                }
            }
        }
        logger.debug("## removing at least {}, and at most {}, sentences...",sentsToRemove.size(),
                (sentsToRemove.size() + offsetsToRemove.size()));

        for (SentenceStaxParser.MascSentence sent : sentsToRemove) {
            sentences.remove(sent);
        }
        for (IntPair offset : offsetsToRemove)
            sentences.remove(offsetsToSentences.get(offset));
    }

    /**
     * is A a larger span than B
     * @param a
     * @param b
     * @return
     */
    private boolean isLarger(IntPair a, IntPair b) {
        return (a.getSecond() - a.getFirst()) - (b.getSecond() - b.getFirst()) > 0;
    }

    private boolean isOverlap(IntPair offset, IntPair otherOffset) {
        int firstStart = offset.getFirst();
        int firstEnd = offset.getSecond();
        int secondStart = otherOffset.getFirst();
        int secondEnd = otherOffset.getSecond();

        return (firstStart >= secondStart && firstStart <= secondEnd) ||
                (firstEnd >= secondStart && firstEnd <= secondEnd);
    }


    private boolean isInside(IntPair sentOffset, IntPair offset) {
        return (sentOffset.getFirst() >= offset.getFirst()) && (sentOffset.getSecond() <= offset.getSecond());
    }


    private void addLemmaAndPos(TextAnnotation ta, List<Pair<String, IntPair>> tokenInfo, Path pennPath) throws FileNotFoundException, XMLStreamException {

        Map<String, IntPair> mascTokIdToOffset = new HashMap<>();

        for (Pair<String, IntPair> tokenSpan : tokenInfo)
            mascTokIdToOffset.put(tokenSpan.getFirst(), tokenSpan.getSecond());

        Map<String, PennStaxParser.PosLemma> lemmaAndPos = pennParser.parseFile(pennPath.toFile().getName());
        throw new IllegalStateException("NOT YET IMPLEMENTED");

    }

    /**
     * Read sections of corpus into TextAnnotations, write out TextAnnotations in json format.
     * Specify MASC root dir of written files, e.g. /home/mssammon/work/data/masc-ccg/written/
     * @param args
     */
    public static void main(String[] args) {


        if (args.length != 2) {
            System.err.println("Usage: " + NAME + " mascCorpusDir outDir");
            System.exit(-1);
        }

        String corpusDir = args[0];
        String outDirGold = args[1];
        String outDirPred = outDirGold + "_PRED";

        Properties props = new Properties();

        props.setProperty(CorpusReaderConfigurator.CORPUS_DIRECTORY.key, corpusDir);
        props.setProperty(CorpusReaderConfigurator.SOURCE_DIRECTORY.key, corpusDir);

        IOUtils.mkdir(outDirGold);
        IOUtils.mkdir(outDirPred);

        ResourceManager rm = new ResourceManager(props);

        MascReader reader = null;
        try {
            reader = new MascReader(rm);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        TextAnnotationBuilder taBldr = new TokenizerTextAnnotationBuilder(new StatefulTokenizer(true, false));

        int numGoldTokCorrect = 0;
        int numGoldTokTotal = 0;
        int numGoldSentCorrect = 0;
        int numGoldSentTotal = 0;

        while (reader.hasNext()) {
            TextAnnotation goldTa = reader.next();
            String text = goldTa.getText();
//            Tokenizer.Tokenization tknz = tokenizer.tokenizeTextSpan(text);
            TextAnnotation predTa = taBldr.createTextAnnotation(goldTa.getCorpusId() + "_PREDICTED", goldTa.getId(), text);

            IntPair[] goldTokCharOffsets = getCharacterOffsets(goldTa.getView(ViewNames.TOKENS));

            numGoldTokTotal += goldTokCharOffsets.length;
            numGoldTokCorrect += countCorrectSpans(predTa.getView(ViewNames.TOKENS), goldTokCharOffsets);

            IntPair[] goldSentCharOffsets = getCharacterOffsets(goldTa.getView(ViewNames.SENTENCE));

            numGoldSentTotal += goldSentCharOffsets.length;
            numGoldSentCorrect += countCorrectSpans(predTa.getView(ViewNames.SENTENCE), goldSentCharOffsets);


            String taJson = SerializationHelper.serializeToJson(goldTa, true);

            String outFile = Paths.get(outDirGold, goldTa.getId() + ".json").toString();

            try {
                logger.trace("Writing file out to '{}'...", outFile);
                LineIO.write(outFile, Collections.singletonList(taJson));
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }
            outFile = Paths.get(outDirPred, predTa.getId() + ".json").toString();
            String predTaJson = SerializationHelper.serializeToJson(predTa, true);

            try {
                logger.debug("writing file '{}'...", outFile);
                LineIO.write(outFile, Collections.singletonList(predTaJson));
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }
            logger.debug("## finished processing file '{}'.", goldTa.getId());
        }

        System.out.println(reader.generateReport());

        System.out.print("TOKEN PERFORMANCE:");
        computeAndPrintAcc(numGoldTokCorrect, numGoldTokTotal);

        System.out.print("SENTENCE PERFORMANCE:");
        computeAndPrintAcc(numGoldSentCorrect, numGoldSentTotal);
    }

    private static void computeAndPrintAcc(int numGoldCorrect, int numGoldTotal) {

        double acc = (double) numGoldCorrect / (double) numGoldTotal;
        System.out.println("num gold correct: " + numGoldCorrect + "; num gold total: " + numGoldTotal);
        System.out.println("Accuracy: " + String.format("%.3f", acc));

    }

    /**
     * assumption: each constituent has unique character offsets
     * @param view
     * @param goldCharOffsets
     * @return
     */
    private static int countCorrectSpans(View view, IntPair[] goldCharOffsets) {
        List<Constituent> constituents = view.getConstituents();
        Set<IntPair> predOffsets = new HashSet<>();

        for (Constituent c : constituents)
            predOffsets.add(new IntPair(c.getStartCharOffset(), c.getEndCharOffset()));

        int numCorrect = 0;
        for (IntPair goldSpan : goldCharOffsets)
            if (predOffsets.contains(goldSpan))
                numCorrect++;

        return numCorrect;
    }




    private static int[] findSentenceEndTokenIndexes(IntPair[] goldTokCharOffsets, IntPair[] goldSentCharOffsets) {

        int[] goldSentTokIndexes = new int[goldSentCharOffsets.length];

        TIntIntHashMap tokEndCharToIndex = new TIntIntHashMap();

        for (int index = 0; index < goldTokCharOffsets.length; ++index)
            tokEndCharToIndex.put(goldTokCharOffsets[index].getSecond(), index);

        for (int index = 0; index < goldSentCharOffsets.length; ++index) {
            int sentEndCharOffset = goldSentCharOffsets[index].getSecond();
            if (!tokEndCharToIndex.containsKey(sentEndCharOffset)) {
                throw new IllegalArgumentException("saw gold sent end char offset '" + sentEndCharOffset +
                        "' but there was no corresponding gold tok end offset.");
            }
            goldSentTokIndexes[index] = tokEndCharToIndex.get(sentEndCharOffset);
        }

        return goldSentTokIndexes;
    }


    private static IntPair[] getCharacterOffsets(View view) {
        List<Constituent> constituents = view.getConstituents();
        IntPair[] offsets = new IntPair[constituents.size()];
        int index = 0;

        for (Constituent c : constituents)
            offsets[index++] = new IntPair(c.getStartCharOffset(), c.getEndCharOffset());

        return offsets;
    }
}
