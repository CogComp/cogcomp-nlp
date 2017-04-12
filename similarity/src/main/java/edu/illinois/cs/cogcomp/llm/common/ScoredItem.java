package edu.illinois.cs.cogcomp.llm.common;

public class ScoredItem implements Comparable< ScoredItem >
{
	protected Object m_item;
	protected Double m_score;
	
	public ScoredItem( Object item_, double score_ )
	{
		m_item = item_;
		m_score = score_;
	}

	public Object getItem() 
	{
		return m_item;
	}

	public Double getScore()
	{
		return m_score;
	}
	
	public int compareTo( ScoredItem other_ )
	{
		return m_score.compareTo( other_.m_score );
	}


}
