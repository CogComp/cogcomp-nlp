/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.wikifier.utils.spelling;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xeustechnologies.googleapi.spelling.SpellChecker;
import org.xeustechnologies.googleapi.spelling.SpellCorrection;
import org.xeustechnologies.googleapi.spelling.SpellResponse;



public class SurfaceFormSpellChecker {

    public static String pathToSpellCheckCache = null;//"../Data/OtherData/SpellCheck.cache";
	private static SpellChecker checker = new SpellChecker();
	private static Properties correctionCache = new Properties();
	private static final boolean caching = false;
	private static OutputStream output;
	private static final Logger logger = LoggerFactory.getLogger(SurfaceFormSpellChecker.class);

	static{
		if(pathToSpellCheckCache != null &&
				new File(pathToSpellCheckCache).exists()){
			try {
				correctionCache.load(new FileInputStream(pathToSpellCheckCache));
			} catch (Exception e) {

			}
		}

		// Only if we need caching
		if(caching){
			try {
				output= new FileOutputStream(pathToSpellCheckCache);
			} catch (FileNotFoundException e) {
				logger.info("Error opening/creating the spell check cache for output");
			}
			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable(){
				@Override
                public void run(){
					try {
						correctionCache.store(output, "");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			},"store spell check"));
		}
	}

	public static String getCorrection(String text){



		// All uppercase normalization HONG KONG => Hong Kong
		String noPunc = text.replaceAll("[^A-Z0-9]*", "");

		if(StringUtils.isAllUpperCase(noPunc) && noPunc.length()>3){
			char[] letters = text.toLowerCase().toCharArray();
			for(int i = 0; i< letters.length;i++){
				if( i==0 || !Character.isLetter(letters[i-1]) && Character.isLetter(letters[i])){
					letters[i] = Character.toUpperCase(letters[i]);
				}
			}
			return new String(letters);
		}

		// Spell check
		if(correctionCache.get(text)!=null){
			return String.valueOf(correctionCache.get(text));
		}else{
			if(caching){
				String correction = getGoogleCorrection(text);
				correctionCache.put(text, correction);
				return correction;
			}
		}

		return text;
	}

	private static String getGoogleCorrection(String text){
		try{
			SpellResponse response = checker.check(text);
			if(response.getCorrections() == null )
				return text;
			else{
				StringBuilder sb = new StringBuilder();
				int prevStart = 0;
				for(SpellCorrection correction:response.getCorrections()){
					// Only considers corrections of full confidence
					if(correction.getConfidence()==1){
						sb.append(text.substring(prevStart,correction.getOffset()));
						String topCorrection = correction.getValue().split("\t")[0];
						sb.append(topCorrection);
						prevStart = correction.getOffset()+correction.getLength();
					}
				}
				sb.append(text.substring(prevStart));
				return sb.toString();
			}
		}catch(Exception e){
			return text;
		}
	}
	public static void main(String[] args)
	{
		System.out.println(getGoogleCorrection("hullo wurrld"));
	}
}
