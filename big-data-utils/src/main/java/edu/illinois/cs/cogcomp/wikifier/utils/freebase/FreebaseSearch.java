/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.wikifier.utils.freebase;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;


/**
 * Takes a string (e.g., "obama") and asks the Freebase Search API. Caches if
 * necessary. Outputs a set of entities.
 *
 * @author Percy Liang
 * @modified upadhya3
 */
public class FreebaseSearch {
	static int counter = 0;
	private String apikey;
	private String cacheLocation;
	private String rdfCacheLocation;
	static final String defaultCacheLocation = "/shared/bronte/tac2014/data/freebaseRawResponseCache/SearchResponse";
	static final String defaultRdfCacheLocation = "/shared/bronte/tac2014/data/freebaseRawResponseCache/RdfResponse";

	static final String defaultApikey = "AIzaSyAclVmmn2FbIc6PiN9poGfNTt2CcyU6x48"; // has
																					// 10k
																					// queries
																					// per
																					// day
																					// limit
																					// (Shyam)
	// static final String defaultApikey =
	// "AIzaSyD4X-Y5JK4ONiCrxp_rbyo54VgKFFXcon0"; // has 10k queries per day
	// limit (Chen-Tse)

	private static final Logger logger = LoggerFactory
			.getLogger(FreebaseSearch.class);

	public FreebaseSearch() {
		this.apikey = defaultApikey;
		this.cacheLocation = defaultCacheLocation;
		this.rdfCacheLocation = defaultRdfCacheLocation;
	}

	public FreebaseSearch(String cacheLocation, String key) {
		this.apikey = key;
		this.cacheLocation = cacheLocation;
	}

	public FreebaseSearch(String cacheLocation) {
		this(cacheLocation, defaultApikey);
	}

	public List<FreebaseAnswer> lookup(String query) throws Exception {
		// First, try the cache.
		String checksum = QueryMQL.getMD5Checksum(query);
		if (IOUtils.exists(cacheLocation + "/" + checksum + ".cached")) {
//			logger.info("Found!");
			return parseJson(FileUtils.readFileToString(new File(cacheLocation
					+ "/" + checksum + ".cached"), "UTF-8"));
		} else {
			logger.info("Caching");
			String tmp = getQueryResponse(query);
			FileUtils.writeStringToFile(new File(cacheLocation + "/" + checksum
					+ ".cached"), tmp, "UTF-8");
			return parseJson(tmp);

		}
	}

	/**
	 * Query Freebase by ID
	 * @param query: in the form of m/09c7w0
	 * @return
	 * @throws Exception
	 */
	public List<String> lookupRdf(String query){
		// First, try the cache.
		String checksum = QueryMQL.getMD5Checksum(query);
		String filename = rdfCacheLocation + "/" + checksum + ".cached";
		if (IOUtils.exists(filename)) {
//			logger.info("Found!");
			try {
				return LineIO.read(filename);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			logger.info("Caching");
			List<String> tmp = queryByID(query);
			try {
				FileUtils.writeStringToFile(new File(filename), tmp.stream().collect(joining("\n")), "UTF-8");
			} catch (IOException e) {
				e.printStackTrace();
			}
			return tmp;
		}
		return null;
	}

	private List<FreebaseAnswer> parseJson(String ans) {
		if (ans == null)
			return null;
		List<FreebaseAnswer> output = Lists.newArrayList();
		JsonElement parse = new JsonParser().parse(ans);
		JsonObject asJsonObject = parse.getAsJsonObject();
		JsonArray jarray = asJsonObject.getAsJsonArray("result");
		for (JsonElement js : jarray) {
			output.add(new FreebaseAnswer(js));
		}
		return output;
	}

	private List<String> queryByID(String query) {
		counter++;
		logger.info("NOT IN FREEBASE CACHE, QUERYING ... " + counter + " times");
		String url = null;
		try {
			url = String.format("https://www.googleapis.com/freebase/v1/rdf/" + query + "?key=" + apikey, URLEncoder.encode(query, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		logger.info("QUERY URL: " + url);
		URLConnection conn = null;
		InputStream in;
		try {
			conn = new URL(url).openConnection();
			in = conn.getInputStream();
		} catch (IOException e) {
			return new ArrayList<>();
		}

		// Read the response
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		List<String> ret = new ArrayList<>();
		String line;
		try {
			while ((line = reader.readLine()) != null)
				ret.add(line.trim());
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ret;
	}
	private String getQueryResponse(String query) throws MalformedURLException,
			IOException {
		counter++;
		logger.info("NOT IN FREEBASE CACHE, QUERYING ... " + counter + " times");
		String url = String.format(
				"https://www.googleapis.com/freebase/v1/search?query=%s&key="
						+ apikey, URLEncoder.encode(query, "UTF-8"));
		logger.info("QUERY URL: " + url);
		URLConnection conn = new URL(url).openConnection();
		InputStream in = conn.getInputStream();

		// Read the response
		StringBuilder buf = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line;
		while ((line = reader.readLine()) != null)
			buf.append(line);
		reader.close();
		// logger.info(buf.toString());
		return buf.toString();
	}

	// /en/barack_obama => fb:en.barack_obama, FOR LATER USE
	private String toRDF(String s) {
		if (s == null)
			return s;
		return "fb:" + s.substring(1).replaceAll("/", ".");
	}

	public String[] query(String q) throws IOException {
		logger.info("in query:" + q);
		List<String> ans = new ArrayList<String>();
		try {
			List<FreebaseAnswer> tmp = lookup(q);
			for (FreebaseAnswer t : tmp) {
				ans.add(t.getName());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ans.toArray(new String[ans.size()]);
	}

	public void close() throws IOException {
		// TODO Auto-generated method stub

	}

	public static void main(String[] args) throws Exception {
		FreebaseSearch fb = new FreebaseSearch();
		List<FreebaseAnswer> answers = fb.lookup("Barack_Obama");
		String mid = answers.get(0).getMid().substring(1);
//		Arrays.asList(answers).forEach(x -> logger.info(x));

		// for(FreebaseAnswer term:fb.lookup("Obama"))
		// {
		// logger.info(term.getName()+" ");
		// }
		// Map<String, String> map = MapDB
		// .newDefaultDb("data/freebaseRawResponseCache/",
		// "freebase_cache").make().getHashMap("freebase_cache");
		// logger.info(map.size());
		// int i = 0;
		// for (String key : map.keySet()) {
		// // logger.info(map.get(key));
		// fb.lookup(key);
		// if (i++ % 1000 == 0) {
		// logger.info("Done " + i);
		// }
		// }
		// query = "Obomber"; //
		// query = "Saint Ronnie"; // NO!
		// query = "Kstewart"; // NO!
		// query = "Shrub"; /////
		// query = "Mufc"; ///

		// query = "Hogtown"; /////
		// query = "Arnie"; //
		// query = "Owe Bama"; // NO!
		// query = "Belgie"; //

		// query = "MAN U"; //
		// query = "Brittania";

		// query = "heels"; //
		// query = "dprk"; //
		// query = "CFF"; // NO!
		// query = "Hayluh Bawbuh"; // NO!
		// query = "T Dot"; //
		// query = "Chitown"; // NO!
		// query = "Rpattz"; ///
		// query = "Hiltery"; // NO!
		// query = "Baby Bush"; ///
		// query = "Mclame"; // NO!
		// query = "Rob Pattinson";
		// query = "Cigar City"; //
		// query = "Nobama";
		// String[] querys = { "Obomber", "MAN U", "Arnie", "heels", "dprk",
		// "CFF", "Hayluh Bawbuh", "Rob Pattinson", "Cigar City",
		// "Baby Bush", "Hogtown" }; // 11 queries

		// FreebaseSearch search = new FreebaseSearch(
		// "/shared/bronte/tac2014/data/freebaseCache");
		// FreebaseSearch search = new FreebaseSearch();
		// logger.info(search.lookup("Popolo Delle Liberta'"));
		// FreebaseSearch search = new FreebaseSearch("freebase_cache");
		// logger.info(search.cache.size());
		// populate(search);
		// logger.info(search.cache.size());

		// for(String key:search.cache.keySet())
		// {
		// logger.info(key+" : "+search.cache.get(key));
		// logger.info(search.cache.get(key).size());
		//
		// // logger.info(remove_duplicates(search.cache.get(key)));
		// // search.cache.put(key, remove_duplicates(search.cache.get(key)));
		// //
		// logger.info(remove_duplicates(search.cache.get(key)).size());
		// }
		// int c=0;
		// while(true)
		// {
		// try {
		// search.lookup(query);
		// } catch (Exception e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// c++;
		// logger.info(c);
		// }
		// for (String query : querys)
		// for (String ans : search.lookup(query)) {
		// logger.info(ans);
		// }
	}
}
