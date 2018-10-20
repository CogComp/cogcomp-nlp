/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.wikifier.utils.spelling;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author upadhya3
 *
 */
public abstract class AbstractSurfaceQueryEngine
{
	private final Logger logger = LoggerFactory.getLogger(AbstractSurfaceQueryEngine.class);

	public boolean queryAndCheck(String q, String ans) throws IOException
	{
		
		
		for(String s:query(q))
		{
//			if(ans.contains(" "))
//			{
//				ans=ans.replaceAll("\\s+","");
//			}
			
			if(s.equals(ans) || s.toLowerCase().equals(ans.toLowerCase()))
			{
				return true; // correct
			}
			logger.info(s);
		}
		return false;
	}
	public abstract String[] query(String q) throws IOException;
	public abstract void close() throws IOException;
}
