/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.wikifier.utils.freebase;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class MQLQueryWrapper {

//	@Deprecated
//	public String mid;
	public String MQLquery;
	public String value;

	// public MQLQuery(String mid, String namespace, String value)
	// {
	// this.mid = mid;
	// JSONObject obj = new JSONObject();
	// JSONArray key = new JSONArray();
	// obj.put("mid", mid);
	// JSONObject contents = new JSONObject();
	// contents.put("namespace", namespace);
	// contents.put("value", value);
	// key.add(contents);
	// obj.put("key", key);
	// }
//	@Deprecated
//	public MQLQueryWrapper(String mid) {
//		this.mid = mid;
//		JSONObject obj = new JSONObject();
//		JSONArray type = new JSONArray();
//		obj.put("mid", mid);
//		obj.put("type", type);
//		this.MQLquery = StringEscapeUtils
//				.unescapeJavaScript(obj.toJSONString());
//	}
//	@SuppressWarnings("unchecked")
//	@Deprecated
//	public MQLQueryWrapper(String mid, String namespace, String value) {
//		
//		this.value=value;
//		JSONObject obj = new JSONObject();
//		JSONArray key = new JSONArray();
//		obj.put("mid", mid);
//		JSONObject contents = new JSONObject();
//		contents.put("namespace", namespace);
//		contents.put("value", value);
//		key.add(contents);
//		obj.put("key", key);
//		this.MQLquery = StringEscapeUtils
//				.unescapeJavaScript(obj.toJSONString());
//	}

	@SuppressWarnings("unchecked")
	public MQLQueryWrapper(String namespace, String value) {
		this.value=value;
		JSONObject obj = new JSONObject();
		JSONArray key = new JSONArray();
		JSONArray type = new JSONArray();
		obj.put("mid", null);
		obj.put("type", type);
		JSONObject contents = new JSONObject();
		contents.put("namespace", namespace);
		contents.put("value", QueryMQL.encodeMQL(value));
		key.add(contents);
		obj.put("key", key);
		this.MQLquery = StringEscapeUtils
				.unescapeJavaScript(obj.toJSONString());
	}
}
