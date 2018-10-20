/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.wikifier.utils.io;

import java.io.File;
import java.io.FilenameFilter;

public class FileFilters {


	public static final FilenameFilter viewableFiles = new FilenameFilter(){
		@Override
		public boolean accept(File dir, String name) {
			return !name.startsWith(".")&&!name.endsWith("~");
		}

	};

	public static final FilenameFilter wikifierOutputFilter = new FilenameFilter(){
		@Override
		public boolean accept(File dir, String name) {
			return name.endsWith(".tagged.full.xml");
		}
	};
}
