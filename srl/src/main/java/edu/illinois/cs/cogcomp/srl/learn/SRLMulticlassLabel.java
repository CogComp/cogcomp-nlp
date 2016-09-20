/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.srl.learn;

import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.srl.core.Models;
import edu.illinois.cs.cogcomp.srl.core.SRLManager;

public class SRLMulticlassLabel implements IStructure {
	private int label;
	private Models type;
	private SRLManager manager;

	public SRLMulticlassLabel(int label, Models type, SRLManager manager) {
		this.label = label;
		this.type = type;
		this.manager = manager;
	}
	public Models getType(){
		return type;
	}
	public SRLManager getManager(){
		return manager;
	}

	public int getLabel() {
		return label;
	}

}
