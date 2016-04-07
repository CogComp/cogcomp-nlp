package edu.illinois.cs.cogcomp.corpusreaders.aceReader.documentReader;

//import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.corpusreaders.aceReader.annotationStructure.ACEDocument;
import edu.illinois.cs.cogcomp.corpusreaders.aceReader.annotationStructure.ACEDocumentAnnotation;
import edu.illinois.cs.cogcomp.corpusreaders.aceReader.XMLException;
import edu.illinois.cs.cogcomp.nlp.tokenizer.IllinoisTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;

import java.io.*;
import java.util.HashSet;

public class ReadACEDocuments {
	
	static boolean isDebug = false;


	static String[] failureFileList = new String[] {
		"MARKBACKER_20050105.1526",
		"FLOPPINGACES_20050203.1953.038",
		"FLOPPINGACES_20041228.0927.010",
		"MARKETVIEW_20050209.1923",
		"fsh_29786",
		"fsh_29195",
		"fsh_29303",
		"APW_ENG_20030610.0554",
		"APW_ENG_20030422.0469", 
		"NYT_ENG_20030630.0079",
		"APW_ENG_20030519.0548",
		"CNN_ENG_20030526_183538.3",
		"CNN_ENG_20030602_105829.2",
		"CNN_ENG_20030415_103039.0",
		"CNN_ENG_20030327_163556.20",
		"CNNHL_ENG_20030312_150218.13",
		"CNN_ENG_20030428_193655.2",
		"CNN_ENG_20030428_193655.2",
		"CNNHL_ENG_20030304_142751.10",
		"CNNHL_ENG_20030611_133445.24",
		"CNN_ENG_20030527_215946.12",
		"CNN_ENG_20030507_170539.0",
		"CNN_ENG_20030424_070008.15",
		"CNN_ENG_20030602_133012.9",
		"CNN_ENG_20030529_130011.6",
		"CNN_ENG_20030430_093016.0",
		"CNN_ENG_20030607_170312.6",
		"CNN_ENG_20030622_173306.9",
		"CNN_ENG_20030611_102832.4",
		"CNN_ENG_20030416_180808.15",
		"CNN_ENG_20030528_125956.8",
		"marcellapr_20050211.2013",
		"rec.travel.usa-canada_20050128.0121",
		"soc.culture.china_20050203.0639",
		"alt.atheism_20041104.2428",
		"alt.vacation.las-vegas_20050109.0133",
	};

    public static void main (String[] args) throws Exception {

	    String docDirInput = "/shared/shelley/yqsong/eventData/ace2005Modify/data/English/";
		String docDirOuput = "/shared/experiments/mssammon/aceNer/testEventAce/";
		String docDirOuputWithGlobalCoref = "/shared/shelley/yqsong/eventData/ace2005_output_2015/taCacheWithGlobalCoref/";
		
		File outputDir = new File(docDirOuput);
		if (outputDir.exists() == false) {
			outputDir.mkdir();
		}
		
		outputDir = new File(docDirOuputWithGlobalCoref);
		if (outputDir.exists() == false) {
			outputDir.mkdir();
		}
		TokenizerTextAnnotationBuilder taBuilder = new TokenizerTextAnnotationBuilder( new IllinoisTokenizer() );
		AceFileProcessor fileProcessor = new AceFileProcessor( taBuilder );
		annotateAllDocument(fileProcessor, docDirInput, docDirOuput);
	}
	

	public static void annotateAllDocument (AceFileProcessor functor, String inputFolderStr, String outputFolderStr) {
		HashSet<String> failureFileSet = new HashSet<String>();
		for (int i = 0; i < failureFileList.length; ++i) {
			failureFileSet.add(failureFileList[i]);
		}
		
		File inputFolder = new File (inputFolderStr);
		File[] subFolderList = inputFolder.listFiles();

		for (int folderIndex = 0; folderIndex < subFolderList.length; ++folderIndex) {

			FilenameFilter filter = new FilenameFilter() {
				public boolean accept(File directory, String fileName) {
					return fileName.endsWith(".apf.xml");
				}
			};
            File subFolderEntry = subFolderList[folderIndex];
			File labelFolder = new File(subFolderEntry.getAbsolutePath()+"/adj");
			File[] fileList = labelFolder.listFiles(filter);
			for (int fileID = 0; fileID < fileList.length; ++fileID) {

                String annotationFile = fileList[fileID].getAbsolutePath();


                System.err.println( "reading ace annotation from '" + annotationFile + "'..." );
                ACEDocumentAnnotation annotationACE = null;
                try {
                    annotationACE = ReadACEAnnotation.readDocument(annotationFile);
                } catch (XMLException e) {
                    e.printStackTrace();
                    continue;
                }

                File outputFile = new File (outputFolderStr + annotationACE.id +".ta");
                if (outputFile.exists() || failureFileSet.contains(annotationACE.id)) {
                    continue;
                }


                if (annotationFile.contains("rec.games.chess.politics_20041216.1047")) {
                    System.out.println("[DEBUG]");
                }

                System.out.println("[File]" + annotationFile);



                ACEDocument aceDoc = functor.processAceEntry(subFolderEntry, annotationACE, annotationFile);

				FileOutputStream f;
				try {
					f = new FileOutputStream(outputFile);
				    ObjectOutputStream s = new ObjectOutputStream(f);
				    s.writeObject(aceDoc);
				    s.flush();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
//	private static void checkAlign(String content, ACEDocumentAnnotation annotation) {
//		List<ACEEntity> entities = annotation.entityList;
//		for (ACEEntity entity : entities) {
//			for (ACEEntityMention mention : entity.entityMentionList) {
//				String str1 = content.substring(mention.extentStart, mention.extentEnd+1); 
//				str1 = str1.replaceAll("&amp;", "&"); // To be noticed !!!
//				
//				String str2 = mention.extent;
//				if (!str1.equals(str2)) {
//					System.out.println(str2+"\n"+str1+"\n"+content);
//					System.exit(1);
//				}
//			}
//		}
//	}

	

}
