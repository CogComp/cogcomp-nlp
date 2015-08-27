package main.java.edu.illinois.cs.cogcomp.wikifier.utils.io;

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
