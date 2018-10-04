/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
//package edu.illinois.cs.cogcomp.wikifier.utils.freebase;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileReader;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.util.List;
//import java.util.Map;
//
//import org.apache.commons.io.FileUtils;
//import org.apache.commons.lang.StringUtils;
//import org.json.simple.JSONArray;
//import org.json.simple.JSONObject;
//import org.json.simple.parser.JSONParser;
//import org.json.simple.parser.ParseException;
//import org.mapdb.DB;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.google.common.collect.Lists;
//
//import edu.illinois.cs.cogcomp.wikifier.utils.io.MapDB;
//
///**
// * TODO Make this an index instead of a MapDB
// * 
// * @author upadhya3
// *
// */
//public class FineGrainNER {
//
//	private static final Logger logger = LoggerFactory
//			.getLogger(FineGrainNER.class);
//	private Map<String, String> wikiTitle2freebaseMid;
//	private DB mydb;
//
//	static final String defaultCacheLocation = "/shared/bronte/tac2014/data/freebaseRawResponseCache/wikititle2mid";
//
//	public FineGrainNER(String wikititle2freebaseMidcacheLocation) {
//		// Provide read-only access to the map
//		mydb = MapDB.DBConfig.READ_ONLY.configure(
//				MapDB.newDefaultDb(wikititle2freebaseMidcacheLocation,
//						"freebase_cache")).make();
//		// this.mydb =
//		// MapDB.newDefaultDb("/shared/bronte/tac2014/data/freebaseRawResponseCache/wikititle2mid",
//		// "freebase_cache").make();
//		wikiTitle2freebaseMid = mydb.getHashMap("freebase_cache");
//	}
//
//	public FineGrainNER() {
//		this(defaultCacheLocation);
//	}
//
//	/**
//	 * 
//	 * @param wikiTitle
//	 * @return
//	 * @throws IOException
//	 * @throws ParseException
//	 */
//	// public List<String> getWikiPageTypes(String wikiTitle) throws
//	// IOException, ParseException
//	// {
//	// String pagetitle;
//	// if(wikiTitle.contains("/"))
//	// pagetitle = StringUtils.substringAfterLast(wikiTitle, "/");
//	// String mid=this.getWikipageMid(pagetitle);
//	// if(mid!=null)
//	// System.out.println(pagetitle+" "+mql.lookupType(mql.buildQuery(mid)));
//	// return null;
//	//
//	// // MQLquery mql = new
//	// MQLquery("AIzaSyAclVmmn2FbIc6PiN9poGfNTt2CcyU6x48");
//	// // String mid = mql.lookupMid(mql.buildQuery(null, "/wikipedia/en",
//	// wikiTitle));
//	// // return mql.lookupType(mql.buildQuery(mid));
//	//
//	//
//	// }
//
//	public String getWikipageMid(String wikiTitle) {
//		if (wikiTitle2freebaseMid.containsKey(wikiTitle)) {
//			String mid = wikiTitle2freebaseMid.get(wikiTitle);
//			return mid;
//		} else {
//			logger.info("WikiTitle "
//					+ wikiTitle
//					+ " is not in cache, please try again with a redirecting title to the same page");
//			return null;
//		}
//	}
//
//	public static void main(String[] args) throws IOException, ParseException {
//		// List<String> titles = FileUtils.readLines(new
//		// File("some_titles"),"utf-8");
//		// for(String title:titles)
//		// {
//		// System.out.println(title);
//		// // System.out.println(getWikiPageTypes("Barack_Obama")); // This
//		// works, because no unicode
//		// // System.out.println(getWikiPageTypes(title)); // This works,
//		// because no unicode
//		// System.out.println(StringEscapeUtils.escapeJava(title));
//		// break;
//		// }
//		// for(String t:getWikiPageTypes("Barack_Obama"))
//		// System.out.println(t);
//		// getAllWikipages();
//		// readJsonResponse();
//		// DB mydb =
//		// MapDB.newDefaultDb("/shared/bronte/tac2014/data/freebaseRawResponseCache/wikititle2mid",
//		// "freebase_cache").make();
//		// // this.cache_of_names = db.getHashMap("freebase_cache");
//		// Map<String,String>wikiTitle2freebaseMid=mydb.getHashMap("freebase_cache");
//		// // populate(wikiTitle2freebaseMid);
//		// for(String key:wikiTitle2freebaseMid.keySet())
//		// System.out.println(wikiTitle2freebaseMid.get(key)+" "+key);
//		// mydb.close();
////
//		getTypeExamples("/people/person");
//		getTypeExamples("/organization/organization");
//		getTypeExamples("/location/location");
//		getTypeExamples("/music/album");
//		getTypeExamples("/music/musical_group");
//		getTypeExamples("/time/recurring_event");
//		getTypeExamples("/award/award_winning_work");
//		getTypeExamples("/education/field_of_study");
//		getTypeExamples("/tv/tv_genre");
//		getTypeExamples("/media_common/media_genre");
//		getTypeExamples("/law/invention");
//		getTypeExamples("/sports/sports_championship");
//		getTypeExamples("/soccer/football_award");
//		getTypeExamples("/people/ethnicity");
//		getTypeExamples("/finance/currency");
//		
//
//	}
//
//	private static void populate(Map<String, String> wikiTitle2freebaseType)
//			throws IOException {
//
//		BufferedReader br = new BufferedReader(new FileReader(
//				("/shared/bronte/tac2014/data/mids_freebase")));
//		String line;
//		String mid = null;
//		int c = 0;
//		while ((line = br.readLine()) != null) {
//			if (line.startsWith("==="))
//				continue;
//			if (line.startsWith("MID: ")) {
//				String[] parts = line.split(" ");
//				// System.out.println(parts[1]+parts[1].length());
//				mid = parts[1];
//			} else {
//				// System.out.println(line+line.length());
//				wikiTitle2freebaseType.put(line, mid);
//			}
//			// if(c++ ==10)
//			// break;
//			System.out.println(c++);
//		}
//		br.close();
//	}
//
//	public static void getTypeExamples(String type) throws IOException,
//			ParseException {
//		int LIMIT = 1000; // its times out above 1000!
//		String query = "[{\"type\":\""
//				+ type
//				+ "\",\"mid\":null,\"key\":[{\"limit\":1, \"namespace\":\"/wikipedia/en\", \"value\":null}],\"limit\": "
//				+ LIMIT + "}]";
//		System.out.println(query);
////		 System.exit(-1);
//		// String queryAndcursor = query+"&cursor";
//		String cursor = "";
//		QueryMQL mql = new QueryMQL("AIzaSyAclVmmn2FbIc6PiN9poGfNTt2CcyU6x48");
//		// JSONObject res = new JSONObject();
//		// res.put("cursor", false);
//		// System.out.println(res.toString());
//		// System.out.println((String)res.get("cursor"));
//		int count = 0;
//		System.out.println(type.replace("/", "_") + count + "_" + "_.txt");
//		PrintWriter writer = new PrintWriter(type.replace("/", "_") + count + "_" + "_.txt",
//				"UTF-8");
//		logger.info("Cursoring: " + count);
//		while (cursor != null) {
//	
//			count++;
//			try {
//				JSONObject response = mql.getCursorAndResponse(query, cursor);
//				System.out.println(response.toString());
//				List<String> wikititles = getExamples(response);
//				for (String title : wikititles) {
//					writer.println(title);
//				}
//				Object cursorObj = response.get("cursor");
//				cursor = (String) cursorObj; 
//				logger.info(cursor);
//				// writer.println(response.toString());
//				//
//			}
//			catch(ClassCastException e)
//			{
//				System.out.println("Finished");
//				writer.close();
//				return;
//			}
//			catch (Exception e) {
//				e.printStackTrace();
//			}
//			if (count == 100) {
//				writer.close();
//				return;
//			}
//		}
//		writer.close();
//	
//	}
//
//	public static void getAllWikipages() throws IOException, ParseException {
//		int LIMIT = 1000; // its times out above 1000!
//		String query = "[{\"mid\": null,\"key\": [{\"namespace\": \"/wikipedia/en\",\"value\": null}],\"limit\": "
//				+ LIMIT + "}]";
//		// String queryAndcursor = query+"&cursor";
//		String cursor = "";
//		QueryMQL mql = new QueryMQL("AIzaSyAclVmmn2FbIc6PiN9poGfNTt2CcyU6x48");
//		// JSONObject res = new JSONObject();
//		// res.put("cursor", false);
//		// System.out.println(res.toString());
//		// System.out.println((String)res.get("cursor"));
//		int count = 0;
//
//		while (cursor != null) {
//			PrintWriter writer = new PrintWriter(
//					"/shared/bronte/tac2014/data/freebaseNER/file" + count
//							+ "_" + cursor + "_.txt", "UTF-8");
//			logger.info("Cursoring: " + count);
//
//			count++;
//			try {
//				JSONObject response = mql.getCursorAndResponse(query, cursor);
//				cursor = (String) response.get("cursor");
//				logger.info(cursor);
//				writer.println(response.toString());
//				//
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			writer.close();
//			if (count == 10) {
//				break;
//			}
//		}
//
//	}
//
//	private static List<String> getExamples(JSONObject response) {
//		JSONArray results = (JSONArray) response.get("result");
//		List<String> ans = Lists.newArrayList();
//		for (Object result : results) {
//			JSONObject tmp = (JSONObject) result;
//			JSONArray keys = (JSONArray) tmp.get("key");
//			String mid = (String) tmp.get("mid");
//			JSONObject key = (JSONObject) keys.get(0);
//			System.out.println(QueryMQL.decodeMQL(key.get("value").toString()));
//			// System.out.println(mid);
//			ans.add(QueryMQL.decodeMQL(key.get("value").toString()));
//		}
//		return ans;
//	}
//
//	public static void readJsonResponse() throws FileNotFoundException,
//			IOException, ParseException {
//		int count = 0;
//		int midcount = 0;
//		List<String> lines = FileUtils.readLines(new File("filelist"));
//		// PrintStream writer = System.out;
//		List<String> badfiles = Lists.newArrayList();
//		PrintWriter writer = new PrintWriter(
//				"/shared/bronte/tac2014/data/mids_freebase", "UTF-8");
//		for (String filename : lines) {
//
//			System.out.println("Doing file: " + filename);
//			JSONParser parser = new JSONParser();
//
//			Object obj;
//			try {
//				obj = parser.parse(new FileReader(
//						"/shared/bronte/tac2014/data/freebaseNER/" + filename));
//			} catch (Exception e) {
//				System.out.println("Something wrong with " + filename);
//				badfiles.add(filename);
//				count++;
//				e.printStackTrace();
//				continue;
//			}
//			JSONObject response = (JSONObject) obj;
//			JSONArray results = (JSONArray) response.get("result");
//			for (Object result : results) {
//				JSONObject tmp = (JSONObject) result;
//				JSONArray keys = (JSONArray) tmp.get("key");
//				String mid = (String) tmp.get("mid");
//
//				writer.println("================");
//				writer.println("MID: " + mid);
//				midcount++;
//				for (Object key : keys) {
//					JSONObject k = (JSONObject) key;
//					writer.println(QueryMQL
//							.decodeMQL(k.get("value").toString()));
//				}
//				// writer.println("TYPE:");
//				// writer.println(mql.lookupType(mql.buildQuery(mid)));
//				writer.println("=================");
//			}
//			count++;
//			if (count == 4641)
//				break;
//		}
//		writer.close();
//		System.out.println("TOTAL COUNT:" + midcount);
//		System.out.println("BAD FILES");
//		for (String f : badfiles)
//			System.out.println(f);
//	}
//
//}
