/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.annotationStructure.ACEDocument;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.documentReader.ReadACEAnnotation;
import edu.illinois.cs.cogcomp.pipeline.handlers.StanfordTrueCaseHandler;

import java.io.File;
import java.util.List;

public class ACEReaderWithTrueCaseFixer extends ACEReader {

    private StanfordTrueCaseHandler stanfordTrueCaseHandler = null;


    public ACEReaderWithTrueCaseFixer(String aceCorpusHome, boolean is2004mode) throws Exception {
        super(aceCorpusHome, is2004mode);
        stanfordTrueCaseHandler = new StanfordTrueCaseHandler();
        stanfordTrueCaseHandler.initialize(null);
    }

    public ACEReaderWithTrueCaseFixer(String aceCorpusHome, String[] sections, boolean is2004mode) throws Exception {
        super(aceCorpusHome, sections, is2004mode);
        stanfordTrueCaseHandler = new StanfordTrueCaseHandler();
        stanfordTrueCaseHandler.initialize(null);
    }
    /**
     * Parse a single ACE Document.
     *
     * @param section Section that the document belongs to.
     * @param fileName Name of the annotation file.
     * @return TextAnnotation instance.
     */
    @Override
    protected TextAnnotation parseSingleACEFile(String section, String fileName) {
        ACEDocument doc;

        // TODO: Static field might cause issue if we try to parse both versions in parallel.
        ReadACEAnnotation.is2004mode = this.is2004mode;

        try {
            File sectionDir = new File(this.aceCorpusHome + File.separator + section);
            doc = ACEReader.fileProcessor.processAceEntry(sectionDir, fileName);
        } catch (Exception ex) {
            ACEReader.logger.warn("Error while reading document - " + fileName, ex);
            return null;
        }

        //logger.info("Parsing file - " + fileName);

        // Adding `section/fileName` as textId for annotation.
        String textId = fileName.substring(fileName.indexOf(section + File.separator));
        TextAnnotation ta =
                ACEReader.taBuilder.createTextAnnotation(
                        this.corpusId,
                        textId,
                        doc.contentRemovingTags);
        try {
            stanfordTrueCaseHandler.addView(ta);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        View trueCaseView = ta.getView("STANFORD_TRUE_CASE");
        String resText = doc.contentRemovingTags;
        char[] resTextChar = resText.toCharArray();
        int consIdx = 0;
        List<Constituent> tokens = ta.getView(ViewNames.TOKENS).getConstituents();
        for (int i = 0; i < resText.length(); i++){
            if (resTextChar[i] == ' ' || resTextChar[i] == '\t' || resTextChar[i] == '\r'){
                continue;
            }
            String curToken = tokens.get(consIdx).toString();
            if (trueCaseView.getConstituentsCovering(tokens.get(consIdx)).size() > 0){
                String trueCaseCurToken = trueCaseView.getConstituentsCovering(tokens.get(consIdx)).get(0).getLabel();
                if (curToken.length() == trueCaseCurToken.length()){
                    curToken = trueCaseCurToken;
                }
                else{
                    if (trueCaseCurToken.equals("U.S.") && curToken.equals(".")){
                        i = i - 3;
                        curToken = trueCaseCurToken;
                    }
                }
            }
            int curTokenLength = curToken.length();
            for (int j = i; j < i + curTokenLength - 1; j++){
                if (j == resText.length()){
                    //break;
                }
                resTextChar[j] = curToken.charAt(j - i);
            }
            consIdx++;
            i = i + curTokenLength - 1;
        }
        resText = new String(resTextChar);
        String fileNameTransformed = fileName.replace(File.separator, "/");
        String[] fileNameGroup = fileNameTransformed.split("/");
        String groupName = fileNameGroup[fileNameGroup.length - 2];
        if (groupName.equals("bn")) {
            ta =
                    ACEReader.taBuilder.createTextAnnotation(
                            this.corpusId,
                            textId,
                            resText);
        }
        // Add metadata attributes to the generated Text Annotation.
        if (doc.metadata != null) {
            for (String metadataKey : doc.metadata.keySet()) {
                String value = doc.metadata.get(metadataKey);
                if (!value.isEmpty()) {
                    ta.addAttribute(metadataKey, value);
                }
            }
        }

        File file = new File( fileName );
        this.addEntityViews(ta, doc.aceAnnotation, file);
        this.addEntityRelations(ta, doc.aceAnnotation, file);

        // TODO: Pending Event, TimeEx and Value Views

        return ta;
    }
}
