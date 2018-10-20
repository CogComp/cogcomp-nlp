/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Vivek Srikumar, Ming-Wei Chang
 */
public class ShellInterface {
    private static Logger logger = LoggerFactory.getLogger(ShellInterface.class);

    public static int executeCommand(String command) throws IOException, InterruptedException {
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(command);

        return process.waitFor();
    }

    public static String executeCommandWithOutput(String command) throws IOException {
        try {
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(command);

            StreamGobbler err = new StreamGobbler(proc.getErrorStream(), "ERR:");
            StreamGobbler out = new StreamGobbler(proc.getInputStream(), "");

            err.start();
            out.start();
            proc.waitFor();

            return out.getOutput();

        } catch (Throwable t) {
            t.printStackTrace();
            return "";
        }
    }
}


class StreamGobbler extends Thread {
    private static Logger logger = LoggerFactory.getLogger(StreamGobbler.class);

    InputStream is;
    String prefix;
    private StringBuilder sb;

    StreamGobbler(InputStream is, String prefix) {
        this.is = is;
        this.prefix = prefix;
        this.sb = new StringBuilder();
    }

    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                logger.info(prefix + " " + line);
                sb.append(line);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public String getOutput() {
        return sb.toString();
    }
}
