/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.utilities;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Static class to create identifiers.
 *
 * @author James Clarke
 */
public class Identifier {

    private static final MessageDigest md;
    private static final String ALGORITHM = "SHA";

    static {
        try {
            md = MessageDigest.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error cannot find digest algorithm " + ALGORITHM, e);
        }
    }

    /**
     * Converts a byte array into a hexidecimal string.
     */
    private static String hexDigest(byte[] bArr) {
        StringBuilder sb = new StringBuilder();
        for (byte aBArr : bArr) {
            int unsigned = aBArr & 0xff;
            if (unsigned < 0x10)
                sb.append("0");
            sb.append(Integer.toHexString((unsigned)));
        }
        return sb.toString();
    }

    private static synchronized byte[] performDigest(String text) {
        return md.digest(text.getBytes());
    }

    /**
     * Create an identifer for a text.
     */
    private static String getId(String text) {
        return hexDigest(performDigest(text));
    }

    public static String getId(String text, boolean flag) {
        String t2 = "FLAG:" + flag + ":" + text;
        return getId(t2);
    }

    public static String getId(List<String> text) {
        return null;
    }
}
