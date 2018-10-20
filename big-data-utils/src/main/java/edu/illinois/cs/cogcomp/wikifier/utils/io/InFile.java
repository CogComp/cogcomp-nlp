/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.wikifier.utils.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
  * This project was started by Nicholas Rizzolo (rizzolo@uiuc.edu) .
  * Most of design, development, modeling and
  * coding was done by Lev Ratinov (ratinov2@uiuc.edu).
  * For modeling details and citations, please refer
  * to the paper:
  * External Knowledge and Non-local Features in Named Entity Recognition
  * by Lev Ratinov and Dan Roth
  * submitted/to appear/published at NAACL 09.
  *
 **/

public class InFile {
	public static boolean convertToLowerCaseByDefault=false;
	public static boolean normalize=false;
	public static boolean pruneStopSymbols=false;
	public BufferedReader  in = null;
	public static String stopSymbols="@";
	private static final Logger logger = LoggerFactory.getLogger(InFile.class);

	public InFile(String filename){
		try{
			in= new BufferedReader(new FileReader(filename));
		}catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}
	}

	public String readLine(){
		try{
			String s=in.readLine();
			if(s==null)
				return null;
			if(convertToLowerCaseByDefault)
				return s.toLowerCase().trim();
			return s;
		}catch(Exception e)
		{
			e.printStackTrace();
			System.exit(0);
		}
		return null;
	}

	public List<String> readLineTokens(){
		return tokenize(readLine());
	}

	public static List<String> tokenize(String s){
		if(s==null)
			return null;
		List<String> res=new ArrayList<String>();
		StringTokenizer st=new StringTokenizer(s," \n\t\r");
		while(st.hasMoreTokens())
			res.add(st.nextToken());
		return res;
	}

	public static List<String> tokenize(String s, String delims){
		if(s==null)
			return null;
		List<String> res=new ArrayList<String>();
		StringTokenizer st=new StringTokenizer(s,delims);
		while(st.hasMoreTokens())
			res.add(st.nextToken());
		return res;
	}

	public static List<String> aggressiveTokenize(String s ){
        if (s == null)
            return null;
        return Arrays.asList(StringUtils.split(s, " \n\t,./<>?;':\"[]{}\\|`~!@#$%^&*()_+-="));
//		List<String> res= Lists.newArrayList();
//		StringTokenizer st=new StringTokenizer(s," \n\t,./<>?;':\"[]{}\\|`~!@#$%^&*()_+-=");
//		while(st.hasMoreTokens())
//			res.add(st.nextToken());
//		return res;
	}

//	public static String[] vec2arr(List<String> v){
//		String[] res=new String[v.size()];
//		for(int i=0;i<v.size();i++)
//			res[i]=v.get(i);
//		return res;
//	}
	public void close(){
		try{
			this.in.close();
		}catch(Exception E){}
	}

	public static String readFileText(String file) throws IOException  {
        File f = new File(file);
        InputStreamReader isr = new InputStreamReader(
                                new FileInputStream(f));
        logger.info("character encoding = " + isr.getEncoding());
        int c;
        StringBuffer res = new StringBuffer();
        while( (c = isr.read()) != -1 ) {
            res.append((char)c);
        }
        isr.close();
        return res.toString().replace('�', '\'');
	}

	public static String readFileText(String file, String encoding) throws IOException  {
        File f = new File(file);
        InputStreamReader isr = new InputStreamReader(
                                new FileInputStream(f), encoding);
		logger.info("character encoding = " + isr.getEncoding());
        int c;
        StringBuffer res = new StringBuffer();
        while( (c = isr.read()) != -1 ) {
            res.append((char)c);
        }
        isr.close();
        return res.toString();
	}

    public static String vec2str(List<String> surfaceFormsAttribs) {
        return StringUtils.join(surfaceFormsAttribs,',');
    }
    
}
