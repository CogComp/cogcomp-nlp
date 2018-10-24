/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.wikifier.utils.freebase;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * 
 * TODO add other fields when need arises.
 *
 * @author upadhya3
 *
 */

public class FreebaseAnswer {
	private String name;
	private String mid;
//	private String id;
	public FreebaseAnswer(JsonElement js) {
//		System.out.println(js.toString());
		JsonObject jobject = js.getAsJsonObject();
		String name = jobject.get("name").toString();
		String mid = jobject.get("mid").toString();
		this.setName(name.substring(1,name.length()-1));
		this.setMid(mid.substring(1,mid.length()-1));
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getMid() {
		return mid;
	}
	public void setMid(String mid) {
		this.mid = mid;
	}
}
