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
			System.out.println(s);
		}
		return false;
	}
	public abstract String[] query(String q) throws IOException;
	public abstract void close() throws IOException;
}