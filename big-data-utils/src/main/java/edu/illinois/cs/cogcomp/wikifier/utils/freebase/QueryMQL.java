/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.wikifier.utils.freebase;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.net.SocketTimeoutException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.jayway.jsonpath.JsonPath;

import edu.illinois.cs.cogcomp.core.io.IOUtils;

/**
 * 
 * For querying the fine grained types and/or mids from freebase. Some of this
 * data is cached in FineGrainedNER class TODO make query response parsing
 * generic For querying the MQL api for freebase
 * 
 * 
 * @author upadhya3
 *
 */
public class QueryMQL {
	final static private Pattern quotedCharPattern = Pattern
			.compile("\\$([0-9A-Fa-f]{4})");
	static String mqlkey_start = "A-Za-z0-9";
	static String mqlkey_char = "A-Za-z0-9_-";

	static Pattern MQLKEY_VALID = Pattern.compile("^[" + mqlkey_start + "]["
			+ mqlkey_char + "]*$");

	static Pattern MQLKEY_CHAR_MUSTQUOTE = Pattern
			.compile(("[^" + mqlkey_char + "]"));

    static final String defaultApikey = "AIzaSyAOYVjHDzk4VNFqz9oWw1qht02vCyIqq5s";//"AIzaSyD4X-Y5JK4ONiCrxp_rbyo54VgKFFXcon0"; //"AIzaSyAclVmmn2FbIc6PiN9poGfNTt2CcyU6x48";
	static final String defaultTypeCacheLocation = "/shared/bronte/tac2014/data/freebaseRawResponseCache/TypeResponse";
	static final String defaultMidCacheLocation = "/shared/bronte/tac2014/data/freebaseRawResponseCache/MidResponse";

	public HashMap<String, List<String>> title_types;
	public HashMap<String, List<String>> new_title;
	public String typeCacheFile = "freebase_type_cache";
	private static final Logger logger = LoggerFactory
			.getLogger(QueryMQL.class);

	private String apikey; // use the same key as FreebaseSearch
//	private boolean cacheOn = false;
	private String typeCacheLocation;
	private String midCacheLocation;

	public QueryMQL(String typeCacheLocation, String midCacheLocation,
			String apikey) {
		this.midCacheLocation = midCacheLocation;
		this.typeCacheLocation = typeCacheLocation;
		this.apikey = apikey;
	}

	public QueryMQL() {
		this.apikey = defaultApikey;
		this.typeCacheLocation = defaultTypeCacheLocation;
		this.midCacheLocation = defaultMidCacheLocation;
	}

	public QueryMQL(String apikey) {
		this.apikey = apikey;
		this.typeCacheLocation = defaultTypeCacheLocation;
		this.midCacheLocation = defaultMidCacheLocation;
	}
	
	public void loadTypeCache(){
		BufferedReader br = null;
		this.title_types = new HashMap<String, List<String>>();
		this.new_title = new HashMap<String, List<String>>();
		
		try {
			br = new BufferedReader(new FileReader(this.typeCacheFile));
			
			String line = br.readLine();
			while(line != null){
				String[] tokens = line.trim().split("\t");
				List<String> types = new ArrayList<String>();
				for(int i=1; i<tokens.length; i++)
					types.add(tokens[i]);
				this.title_types.put(tokens[0], types);
				line = br.readLine();
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		logger.debug("Finished reading freebase type cache, "+title_types.size()+" entries");
	}
	
	public void writeNewTitleToCache() throws Exception{
		BufferedWriter bw = null;
		bw = new BufferedWriter(new FileWriter(this.typeCacheFile,true));
		for(String title: this.new_title.keySet()){
			String output = title;
			for(String type: new_title.get(title))
				output += "\t"+type;
			
			bw.write(output+"\n");
		}
		bw.close();
		
		logger.debug("Written "+this.new_title.size()+" new entries to cache");
		this.new_title.clear();
	}

	public List<String> lookupQuery(String query) throws ParseException,
			IOException {
		List<String> ans = new ArrayList<String>();
		JSONObject response = getResponse(query);

		JSONObject result = (JSONObject) response.get("result");

		JSONArray results = (JSONArray) result.get("key");
		for (Object value : results) {
			logger.info(JsonPath.read(value, "$.value").toString());
			ans.add(decodeMQL(JsonPath.read(value, "$.value").toString()));
		}
		return ans;
	}

	public JSONObject getCursorAndResponse(String mqlQuery, String cursor)
			throws IOException, ParseException {
		HttpTransport httpTransport = new NetHttpTransport();
		HttpRequestFactory requestFactory = httpTransport
				.createRequestFactory();
		JSONParser parser = new JSONParser();

		GenericUrl url = new GenericUrl(
				"https://www.googleapis.com/freebase/v1/mqlread");
		url.put("query", mqlQuery);
		url.put("key", apikey);
		url.put("cursor", cursor);
		logger.debug("QUERY URL: " + url.toString());
		HttpRequest request = requestFactory.buildGetRequest(url);
		HttpResponse httpResponse = request.execute();
		JSONObject response = (JSONObject) parser.parse(httpResponse
				.parseAsString());
		return response;
	}

	public JSONObject getResponse(String mqlQuery) throws IOException,
			ParseException {
		HttpTransport httpTransport = new NetHttpTransport();
		HttpRequestFactory requestFactory = httpTransport
				.createRequestFactory();
		JSONParser parser = new JSONParser();

		GenericUrl url = new GenericUrl(
				"https://www.googleapis.com/freebase/v1/mqlread");
		url.put("query", mqlQuery);
		url.put("key", apikey);
		logger.debug("Querying Freebase QUERY URL: " + url.toString());
		HttpRequest request = requestFactory.buildGetRequest(url);
		HttpResponse httpResponse;
		try {
			httpResponse = request.execute();
		} catch (HttpResponseException e) {
			e.printStackTrace();
			
			int statusCode = e.getStatusCode();
			logger.error("StatusCode "+statusCode);
			logger.error("Query URL was "+url.toString());
			logger.error("Query was "+mqlQuery);
			
			if(statusCode==403)	// max limit reached for a day
			{
				System.exit(-1);
			}
			return null;
		} 
		catch(SocketTimeoutException e)
		{
			e.printStackTrace();
			return null;
		}
		JSONObject response = (JSONObject) parser.parse(httpResponse
				.parseAsString());
		return response;
	}

	/**
	 * Returns the list of types like "/film/film_location" for a given mid
	 * 
	 * @param mqlQuery
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
//	@Deprecated
//	public List<String> lookupType(MQLQueryWrapper mql) throws IOException,
//			ParseException {
//		List<String> ans = new ArrayList<String>();
//		JSONObject response;
//		String mid = mql.mid;
//		mid = mid.replace("/", "_");
//		String mqlQuery = mql.MQLquery;
//		// first check mid in cache
//		if (IOUtils.exists(typeCacheLocation + "/" + mid + ".cached")) {
//			logger.info("Found!");
//			JSONParser jsonParser = new JSONParser();
//			response = (JSONObject) jsonParser.parse(FileUtils
//					.readFileToString(new File(typeCacheLocation + "/" + mid
//							+ ".cached"), "UTF-8"));
//		} else {
//			logger.info("Caching");
//			response = getResponse(mqlQuery);
//			FileUtils.writeStringToFile(new File(typeCacheLocation + "/" + mid
//					+ ".cached"), response.toString(), "UTF-8");
//		}
//
//		JSONObject result = (JSONObject) response.get("result");
//
//		JSONArray types = (JSONArray) result.get("type");
//		for (Object value : types) {
//			ans.add(value.toString());
//		}
//		return ans;
//	}

	static int found=0;
	static int cacheMiss=0;
	
//	public List<String> lookupType(String title) throws Exception{
//		List<String> ans = new ArrayList<String>();
//		
//		if (this.title_types.containsKey(title)) {
//			ans = title_types.get(title);
//			return ans;
//		} else {
//			try{
//				String mid = this.lookupMid(this.buildQuery(null, "/wikipedia/en", QueryMQL.encodeMQL(title)));
//				MQLQueryWrapper mql = this.buildQuery(mid);
//				JSONObject response;
//				String mqlQuery = mql.MQLquery;
//				response = getResponse(mqlQuery);
//				JSONObject result = (JSONObject) response.get("result");
//				if (result != null) {
//					JSONArray types = (JSONArray) result.get("type");
//					for (Object value : types) {
//						ans.add(value.toString());
//					}
//				}
//			} catch (HttpResponseException e){
//				logger.info("title: "+title);
//				e.printStackTrace();
//				if(e.getStatusCode() == 403)
//					System.exit(0);
//			}
//		}
//		return ans;
//	}

	public String lookupMidFromTitle(MQLQueryWrapper mql)
			throws Exception {
		JSONObject response;
		String mqlQuery = mql.MQLquery;
		logger.debug("QUERY IS " + mqlQuery);
		String title = mql.value;
		String checksum = getMD5Checksum(title);
		// first check mid in cache
		if (IOUtils.exists(typeCacheLocation + "/" + checksum + ".cached")) {
			found++;
			logger.info("Found! "+found);
			JSONParser jsonParser = new JSONParser();
			response = (JSONObject) jsonParser.parse(FileUtils
					.readFileToString(new File(typeCacheLocation + "/"
							+ checksum + ".cached"), "UTF-8"));
		} else {
			response = getResponse(mqlQuery);
			if (response == null)
				return null;
			cacheMiss++;
			logger.info("Caching "+cacheMiss);
			FileUtils.writeStringToFile(new File(typeCacheLocation + "/"
					+ checksum + ".cached"), response.toString(), "UTF-8");
		}

		JSONObject result = (JSONObject) response.get("result");

		if (result != null) {
			return (String) result.get("mid");
		}
		return null;
	}

	public List<String> lookupTypeFromTitle(MQLQueryWrapper mql)
			throws Exception {
		List<String> ans = new ArrayList<String>();
		JSONObject response;
		String mqlQuery = mql.MQLquery;
		logger.debug("QUERY IS " + mqlQuery);
		String title = mql.value;
		String checksum = getMD5Checksum(title);
		// first check mid in cache
		if (IOUtils.exists(typeCacheLocation + "/" + checksum + ".cached")) {
			found++;
			logger.info("Found! "+found);
			JSONParser jsonParser = new JSONParser();
			response = (JSONObject) jsonParser.parse(FileUtils
					.readFileToString(new File(typeCacheLocation + "/"
							+ checksum + ".cached"), "UTF-8"));
		} else {
			response = getResponse(mqlQuery);
			if (response == null)
				return ans;
			cacheMiss++;
			logger.info("Caching "+cacheMiss);
			FileUtils.writeStringToFile(new File(typeCacheLocation + "/"
					+ checksum + ".cached"), response.toString(), "UTF-8");
		}

		JSONObject result = (JSONObject) response.get("result");

		if (result != null) {
			JSONArray types = (JSONArray) result.get("type");
			for (Object value : types) {
				ans.add(value.toString());
			}
		}
		return ans;
	}

	/**
	 * returns the mid for a wiki title
	 * 
	 * @param mql
	 * @return
	 * @throws Exception
	 */
	@Deprecated
	public String lookupMid(MQLQueryWrapper mql) throws Exception {
		String mqlQuery = mql.MQLquery;
		String title = mql.value;
		String checksum = getMD5Checksum(title);

		logger.debug("MQLQUERY is " + mqlQuery);
		JSONObject response;

		if (IOUtils.exists(midCacheLocation + "/" + checksum + ".cached")) {
			logger.info("Found!");
			JSONParser jsonParser = new JSONParser();
			response = (JSONObject) jsonParser.parse(FileUtils
					.readFileToString(new File(midCacheLocation + "/"
							+ checksum + ".cached"), "UTF-8"));
		} else {
			logger.info("Caching");
			response = getResponse(mqlQuery);
			FileUtils.writeStringToFile(new File(midCacheLocation + "/"
					+ checksum + ".cached"), response.toString(), "UTF-8");
		}

		JSONObject result = (JSONObject) response.get("result");
		if (result != null)
			return (String) result.get("mid");
		else
			return null;
	}

	public static String getMD5Checksum(String query){
		MessageDigest complete = null;
		try {
			complete = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		complete.update(query.getBytes(), 0, query.getBytes().length);
		byte[] b = complete.digest();
		String result = "";
		for (int i = 0; i < b.length; i++)
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		return result;
	}

	/*
	 * MQL Unicode to normal Unicode mapping Changes things like $0028band$0029
	 * to (band) Code lifted from the web
	 */
	public static String decodeMQL(String s) {
		StringBuffer sb = new StringBuffer();
		int last = 0;
		Matcher m = quotedCharPattern.matcher(s);
		while (m.find()) {
			int start = m.start();
			int end = m.end();
			if (start > last) {
				sb.append(s.substring(last, start));
			}
			last = end;
			sb.append((char) Integer.parseInt(s.substring(start + 1, end), 16));
		}

		if (last < s.length()) {
			sb.append(s.substring(last));
		}

		return sb.toString();
	}

	public MQLQueryWrapper buildQuery(String namespace, String value) {
		return new MQLQueryWrapper(namespace, value);
	}

	/**
	 * TODO add more fields when need arises. for getting the wikipage_title
	 * from mid i.e. wikipage -> mid USE THE FINE GRAINED NER Class to get
	 * these. I have already queried all the data.
	 * 
	 * @param mid
	 * @param namespace
	 * @param value
	 * @return
	 */
//	@Deprecated
//	public MQLQueryWrapper buildQuery(String mid, String namespace, String value) {
//		return new MQLQueryWrapper(mid, namespace, value);
//	}

	/**
	 * Create queries with mid for type i.e. mid -> type
	 * 
	 * @return
	 */
//	@Deprecated
//	public MQLQueryWrapper buildQuery(String mid) {
//		return new MQLQueryWrapper(mid);
//	}

	public static String toHex(String arg) {
		return String.format("%04X", new BigInteger(1, arg.getBytes()));
	}

	public static String encodeMQL(String str) {
		StringBuilder retStr = new StringBuilder();

		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
//			int cp = Character.codePointAt(str, i);
			Matcher matcher = MQLKEY_CHAR_MUSTQUOTE.matcher("" + c);
			if (matcher.find()) {
				retStr.append("$" + toHex("" + c));
			} else
				retStr.append(c);
		}
		if (retStr.toString().endsWith("-")) {
			String tmp = retStr.toString();
			int idx = tmp.lastIndexOf("-");
			str = new StringBuilder(tmp).replace(idx, idx + 1, "$002D").toString();
			return str.toString();
		}
		if (retStr.toString().startsWith("-")) {
			String tmp = retStr.toString();
			int idx = tmp.indexOf("-");
			str = new StringBuilder(tmp).replace(idx, idx + 1, "$002D").toString();
			return str.toString();
		}
		return retStr.toString();
	}

	public static void main(String[] args) throws Exception {
		QueryMQL mql = new QueryMQL();
//		logger.info(encodeMQL("Darker_than_darkness-style93-"));
//		logger.info(encodeMQL("Frank_Black_93-03"));
//		System.exit(-1);
		// Example 1: query for mid from freebase of wiki title
		// Washington_(U.S._state)
		// logger.info(mql.lookupMid(mql.buildQuery(null,
		// "/wikipedia/en",
		// encodeMQL("Washington_(U.S._state)"))));
		// Example 2: to lookup fine grained types from freebase using mid
		// logger.info(mql.lookupType(mql.buildQuery("/m/01nf0c")));
		// Example 3: To get the fine grained type, first get the mid using
		// logger.info(mql.lookupTypeFromTitle(mql.buildQuery(
		// "/wikipedia/en", "Barack_Obama")));
		// Code for generating training data for Fine2CoarseNER classifier
//		List<String> lines = FileUtils.readLines(new File("sortedTarget"),
//				"UTF-8");
//		int c = 0;
//		Collections.shuffle(lines);
//		for (String title : lines) {
//			c++;
//			List<String> type = mql.lookupTypeFromTitle(mql.buildQuery(
//					"/wikipedia/en", QueryMQL.encodeMQL(title)));
//			logger.info(type);
//		}
		String mid = mql.lookupMidFromTitle(mql.buildQuery(
				"/wikipedia/en", QueryMQL.encodeMQL("Benjamin_Franklin")));
//		String mid = mql.lookupMid(mql.buildQuery(
//				"/wikipedia/en", QueryMQL.encodeMQL("xyzabc")));
		logger.info(mid);
//		logger.info(mid);
		// for (String title : lines) {
		// title=title.split("\\t")[0];
		// logger.info(title);
		// String mid = mql.lookupMid(mql.buildQuery(null, "/wikipedia/en",
		// encodeMQL(title)));
		// if (mid == null)
		// continue;
		// // w.print(title+"\t");
		// List<String> type = mql.lookupType(mql.buildQuery(mid));
		// logger.info(type);
		// }
		// w.close();
		// }
		// for (String title : titles.keySet()) {
		// logger.info(title + "\t" + titles.get(title));
		//
		// String mid;
		// try {
		// mid = mql.lookupMid(mql.buildQuery(null, "/wikipedia/en",
		// encodeMQL(title)));
		// } catch (Exception e) {
		// e.printStackTrace();
		// continue;
		// }
		// List<String> type = mql.lookupType(mql.buildQuery(mid));
		// if (type != null) {
		// logger.info(type);
		// w.print(title + "\t" + titles.get(title));
		// for (String t : type) {
		// w.print("\t" + t);
		// }
		// w.println();
		// }
		// }
		// w.close();
	}
}