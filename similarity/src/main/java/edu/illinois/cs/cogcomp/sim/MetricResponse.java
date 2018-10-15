/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.sim;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @author mssammon
 * @author sgupta96
 * @author ngupta18
 */
public class MetricResponse implements Serializable {
	private static final long serialVersionUID = 1L;
	public final double score;
	public final String reason;

	public MetricResponse(double score, String reason) {
		this.score = score;
		this.reason = reason;
	}

	public String toString() {
		StringBuilder bldr = new StringBuilder("Score: ");
		bldr.append(score).append("; Reason: ").append(reason).append(System.lineSeparator());

		return bldr.toString();
	}

	// public void writeObject(ObjectOutputStream out) throws IOException {
	// out.writeDouble(score);
	// out.writeObject(reason);
	// }
	//
	// public void readObject(ObjectInputStream in) throws IOException,
	// ClassNotFoundException {
	// score = in.readDouble();
	// reason = (String) in.readObject();
	// }

}
