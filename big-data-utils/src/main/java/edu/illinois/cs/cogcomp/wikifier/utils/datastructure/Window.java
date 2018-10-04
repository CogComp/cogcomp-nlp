/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.wikifier.utils.datastructure;

import java.util.ArrayList;
import java.util.List;

public class Window <T>{

	public static class Neighbor<T>{
		public final int distance;
		public final T object;

		public Neighbor(T reference,int distance){
			object= reference;
			this.distance = distance;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + distance;
			result = prime * result + ((object == null) ? 0 : object.hashCode());
			return result;
		}

		@SuppressWarnings("unchecked")
        @Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Neighbor<T> other = (Neighbor<T>) obj;
			if (distance != other.distance)
				return false;
			if (object == null) {
				if (other.object != null)
					return false;
			} else if (!object.equals(other.object))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Neighbor [distance=" + distance + ", object=" + object + "]";
		}


	}

	final T center;

	final List<Neighbor<T>> window = new ArrayList<Neighbor<T>>();

	public Window(List<T> allEntities, int center, int windowSize){
		this.center = allEntities.get(center);
		int safeMax = Math.min(allEntities.size(),center+windowSize+1);
		int safeMin = Math.max(0, center-windowSize);
		for(int i=safeMin;i<safeMax;i++){
			if(i!=center)
				window.add(new Neighbor<T>(allEntities.get(i), Math.abs(center-i)));
		}
	}

	public T getCenter(){
		return center;
	}

	public List<Neighbor<T>> getNeigbors(){
		return window;
	}

	@Deprecated
    public List<Neighbor<T>> getRightNeigbors(){
        if(window.size() <= 1)
            return window;

        int rightStartPos = window.size()-2;
        while(rightStartPos >0 && window.get(rightStartPos).distance < window.get(rightStartPos+1).distance){
            rightStartPos --;
        }

        return window.subList(rightStartPos+1, window.size());
    }


}
