/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.datastructures.vectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;


/**
 * This class intends to operate just as a <code>DataInputStream</code> with some additional
 * convenience methods and built-in exception handling.
 *
 * @author Nick Rizzolo
 **/
public class ExceptionlessInputStream extends FilterInputStream {
    private static Logger logger = LoggerFactory.getLogger(ExceptionlessInputStream.class);

    /** The entry inside any compressed file has this name. */
    public static final String zipEntryName = "LBJFile";

    /** This buffer is used internally by {@link #readUTF(int)}. */
    private byte[] buffer = null;
    /** This buffer is used internally by {@link #readUTF(int)}. */
    private char[] chars = null;
    /** The underlying data input stream. */
    private DataInputStream dis;


    /**
     * Opens a buffered (and uncompressed) stream for reading from the specified file.
     *
     * @param filename The file to read from.
     * @return The newly opened stream.
     **/
    public static ExceptionlessInputStream openBufferedStream(String filename) {
        ExceptionlessInputStream eis = null;

        try {
            eis =
                    new ExceptionlessInputStream(new BufferedInputStream(new FileInputStream(
                            filename)));
        } catch (Exception e) {
            System.err.println("Can't open '" + filename + "' for input:");
            e.printStackTrace();
            System.exit(1);
        }

        return eis;
    }


    /**
     * Opens a compressed stream for reading from the specified file.
     *
     * @param filename The file to read from.
     * @return The newly opened stream.
     **/
    public static ExceptionlessInputStream openCompressedStream(String filename) {
        ExceptionlessInputStream eis = null;

        try {
            ZipFile zip = new ZipFile(filename);
            eis =
                    new ExceptionlessInputStream(new BufferedInputStream(zip.getInputStream(zip
                            .getEntry(zipEntryName))));
        } catch (Exception e) {
            System.err.println("Can't open '" + filename + "' for input:");
            e.printStackTrace();
            System.exit(1);
        }

        return eis;
    }


    /**
     * Opens a buffered (and uncompressed) stream for reading from the specified location.
     *
     * @param url The location to read from.
     * @return The newly opened stream.
     **/
    public static ExceptionlessInputStream openBufferedStream(URL url) {
        ExceptionlessInputStream eis = null;

        try {
            eis = new ExceptionlessInputStream(new BufferedInputStream(url.openStream()));
        } catch (Exception e) {
            System.err.println("Can't open '" + url + "' for input:");
            e.printStackTrace();
            System.exit(1);
        }

        return eis;
    }


    /**
     * Opens a compressed stream for reading from the specified location.
     *
     * @param url The location to read from.
     * @return The newly opened stream.
     **/
    public static ExceptionlessInputStream openCompressedStream(URL url) {
        if (url.getProtocol().equals("file"))
            return openCompressedStream(url.getFile());

        ExceptionlessInputStream eis = null;

        try {
            ZipInputStream zip = new ZipInputStream(url.openStream());
            zip.getNextEntry();
            eis = new ExceptionlessInputStream(new BufferedInputStream(zip));
        } catch (Exception e) {
            System.err.println("Can't open '" + url + "' for input:");
            e.printStackTrace();
            System.exit(1);
        }

        return eis;
    }


    /**
     * Creates a new data input stream to read data from the specified underlying input stream.
     *
     * @param in The underlying input stream.
     **/
    public ExceptionlessInputStream(InputStream in) {
        super(new DataInputStream(in));
        dis = (DataInputStream) this.in;
    }


    /**
     * Whenever an exception is caught, this method attempts to close the stream and exit the
     * program.
     *
     * @param e The thrown exception.
     **/
    private void handleException(Exception e) {
        System.err.println("Can't read from input stream:");
        e.printStackTrace();
        close();
        System.exit(1);
    }


    /**
     * Closes this input stream and releases any system resources associated with the stream.
     **/
    public void close() {
        try {
            dis.close();
        } catch (Exception e) {
            System.err.println("Can't close input stream:");
            e.printStackTrace();
            System.exit(1);
        }
    }


    /**
     * Reads one input byte and returns <code>true</code> if that byte is nonzero,
     * <code>false</code> if that byte is zero. This method is suitable for reading the byte written
     * by {@link ExceptionlessOutputStream#writeBoolean(boolean)}.
     *
     * @return The <code>boolean</code> value read.
     **/
    public boolean readBoolean() {
        try {
            return dis.readBoolean();
        } catch (Exception e) {
            handleException(e);
        }
        return false;
    }


    /**
     * Reads and returns one input byte. The byte is treated as a signed value in the range
     * <code>-128</code> through <code>127</code>, inclusive. This method is suitable for reading
     * the byte written by {@link ExceptionlessOutputStream#writeByte(int)}.
     *
     * @return The 8-bit value read.
     **/
    public byte readByte() {
        try {
            return dis.readByte();
        } catch (Exception e) {
            handleException(e);
        }
        return (byte) 0;
    }


    /**
     * Reads and returns an array of bytes from the input. The input stream is expected to contain
     * an integer representing the number of bytes in the array first, followed by the bytes in the
     * array. Each byte is treated as a signed value in the range <code>-128</code> through
     * <code>127</code>, inclusive. This method is suitable for reading the byte array written by
     * {@link ExceptionlessOutputStream#writeBytes(byte[])}.
     *
     * @return The array of 8-bit bytes read.
     **/
    public byte[] readBytes() {
        try {
            int n = dis.readInt();
            if (n < 0)
                return null;
            byte[] result = new byte[n];
            for (int i = 0; i < n; ++i)
                result[i] = dis.readByte();
            return result;
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }


    /**
     * Reads one input byte, zero-extends it to type <code>int</code>, and returns the result, which
     * is therefore in the range <code>0</code> through <code>255</code>. This method is suitable
     * for reading the byte written by {@link ExceptionlessOutputStream#writeByte(int)} if the
     * argument to <code>writeByte</code> was intended to be a value in the range <code>0</code>
     * through <code>255</code>.
     *
     * @return The unsigned 8-bit value read.
     **/
    public int readUnsignedByte() {
        try {
            return dis.readByte();
        } catch (Exception e) {
            handleException(e);
        }
        return 0;
    }


    /**
     * Reads two input bytes and returns a <code>short</code> value. Let <code>a</code> be the first
     * byte read and <code>b</code> be the second byte. The value returned is:
     * <p>
     * 
     * <pre>
     * <code>(short)((a &lt;&lt; 8) | (b &amp; 0xff))
     * </code>
     * </pre>
     * 
     * This method is suitable for reading the bytes written by
     * {@link ExceptionlessOutputStream#writeShort(int)}.
     *
     * @return The 16-bit value read.
     **/
    public short readShort() {
        try {
            return dis.readShort();
        } catch (Exception e) {
            handleException(e);
        }
        return (short) 0;
    }


    /**
     * Reads two input bytes and returns an <code>int</code> value in the range <code>0</code>
     * through <code>65535</code>. Let <code>a</code> be the first byte read and <code>b</code> be
     * the second byte. The value returned is:
     * <p>
     * 
     * <pre>
     * <code>(((a &amp; 0xff) &lt;&lt; 8) | (b &amp; 0xff))
     * </code>
     * </pre>
     * 
     * This method is suitable for reading the bytes written by
     * {@link ExceptionlessOutputStream#writeShort(int)} if the argument to <code>writeShort</code>
     * was intended to be a value in the range <code>0</code> through <code>65535</code>.
     *
     * @return The unsigned 16-bit value read.
     **/
    public int readUnsignedShort() {
        try {
            return dis.readUnsignedShort();
        } catch (Exception e) {
            handleException(e);
        }
        return 0;
    }


    /**
     * Reads an input <code>char</code> and returns the <code>char</code> value. A Unicode
     * <code>char</code> is made up of two bytes. Let <code>a</code> be the first byte read and
     * <code>b</code> be the second byte. The value returned is:
     * <p>
     * 
     * <pre>
     * <code>(char)((a &lt;&lt; 8) | (b &amp; 0xff))
     * </code>
     * </pre>
     * 
     * This method is suitable for reading the bytes written by
     * {@link ExceptionlessOutputStream#writeChar(int)}.
     *
     * @return The Unicode <code>char</code> read.
     **/
    public char readChar() {
        try {
            return dis.readChar();
        } catch (Exception e) {
            handleException(e);
        }
        return 0;
    }


    /**
     * Reads four input bytes and returns an <code>int</code> value. Let <code>a</code> be the first
     * byte read, <code>b</code> be the second byte, <code>c</code> be the third byte, and
     * <code>d</code> be the fourth byte. The value returned is:
     * <p>
     * 
     * <pre>
     * <code>
     * (((a &amp; 0xff) &lt;&lt; 24) | ((b &amp; 0xff) &lt;&lt; 16) |
     * &#32;((c &amp; 0xff) &lt;&lt; 8) | (d &amp; 0xff))
     * </code>
     * </pre>
     * 
     * This method is suitable for reading the bytes written by
     * {@link ExceptionlessOutputStream#writeInt(int)}.
     *
     * @return The <code>int</code> value read.
     **/
    public int readInt() {
        try {
            return dis.readInt();
        } catch (Exception e) {
            handleException(e);
        }
        return 0;
    }


    /**
     * Reads eight input bytes and returns a <code>long</code> value. Let <code>a</code> be the
     * first byte read, <code>b</code> be the second byte, <code>c</code> be the third byte,
     * <code>d</code> be the fourth byte, <code>e</code> be the fifth byte, <code>f</code> be the
     * sixth byte, <code>g</code> be the seventh byte, and <code>h</code> be the eighth byte. The
     * value returned is:
     * <p>
     * 
     * <pre>
     * <code>
     * (((long)(a &amp; 0xff) &lt;&lt; 56) |
     *  ((long)(b &amp; 0xff) &lt;&lt; 48) |
     *  ((long)(c &amp; 0xff) &lt;&lt; 40) |
     *  ((long)(d &amp; 0xff) &lt;&lt; 32) |
     *  ((long)(e &amp; 0xff) &lt;&lt; 24) |
     *  ((long)(f &amp; 0xff) &lt;&lt; 16) |
     *  ((long)(g &amp; 0xff) &lt;&lt;  8) |
     *  ((long)(h &amp; 0xff)))
     * </code>
     * </pre>
     * <p>
     * This method is suitable for reading the bytes written by
     * {@link ExceptionlessOutputStream#writeLong(long)}.
     *
     * @return The <code>long</code> value read.
     **/
    public long readLong() {
        try {
            return dis.readLong();
        } catch (Exception e) {
            handleException(e);
        }
        return 0;
    }


    /**
     * Reads four input bytes and returns a <code>float</code> value. It does this by first
     * constructing an <code>int</code> value in exactly the manner of the <code>readInt</code>
     * method, then converting this <code>int</code> value to a <code>float</code> in exactly the
     * manner of the method <code>Float.intBitsToFloat</code>. This method is suitable for reading
     * the bytes written by {@link ExceptionlessOutputStream#writeFloat(float)}.
     *
     * @return The <code>float</code> value read.
     **/
    public float readFloat() {
        try {
            return dis.readFloat();
        } catch (Exception e) {
            handleException(e);
        }
        return 0;
    }


    /**
     * Reads eight input bytes and returns a <code>double</code> value. It does this by first
     * constructing a <code>long</code> value in exactly the manner of the <code>readlong</code>
     * method, then converting this <code>long</code> value to a <code>double</code> in exactly the
     * manner of the method <code>Double.longBitsToDouble</code>. This method is suitable for
     * reading the bytes written by {@link ExceptionlessOutputStream#writeDouble(double)}.
     *
     * @return The <code>double</code> value read.
     **/
    public double readDouble() {
        try {
            return dis.readDouble();
        } catch (Exception e) {
            handleException(e);
        }
        return 0;
    }


    /**
     * Reads a string from the underlying stream.
     *
     * @return The string.
     **/
    public String readString() {
        short utfLength = readShort();
        if (utfLength == -1)
            return null;
        String result = readUTF(utfLength);
        return result;
    }


    /**
     * Reads in a string that has been encoded using a <a
     * href="http://java.sun.com/javase/6/docs/api/java/io/DataInput.html#modified-utf-8">modified
     * UTF-8</a> format. This method is suitable for reading the bytes written by
     * {@link ExceptionlessOutputStream#writeUTF(String)}.
     *
     * @param utfLength The number of bytes expected in the encoding of the string to read.
     * @return A Unicode string.
     **/
    public String readUTF(int utfLength) {
        if (buffer == null || buffer.length < utfLength) {
            buffer = new byte[utfLength * 2];
            chars = new char[utfLength * 2];
        }

        int c, char2, char3;
        int count = 0;
        int charsCount = 0;

        try {
            dis.readFully(buffer, 0, utfLength);
        } catch (Exception e) {
            handleException(e);
        }

        while (count < utfLength) {
            c = (int) buffer[count] & 0xff;
            if (c > 127)
                break;
            count++;
            chars[charsCount++] = (char) c;
        }

        while (count < utfLength) {
            c = (int) buffer[count] & 0xff;
            switch (c >> 4) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    /* 0xxxxxxx */
                    count++;
                    chars[charsCount++] = (char) c;
                    break;
                case 12:
                case 13:
                    /* 110x xxxx 10xx xxxx */
                    count += 2;
                    if (count > utfLength) {
                        System.err.println("Error in UTF formatting: partial character at end");
                        new Exception().printStackTrace();
                        close();
                        System.exit(1);
                    }

                    char2 = (int) buffer[count - 1];
                    if ((char2 & 0xC0) != 0x80) {
                        System.err.println("Error in UTF formatting: malformed input around byte "
                                + count);
                        new Exception().printStackTrace();
                        close();
                        System.exit(1);
                    }

                    chars[charsCount++] = (char) (((c & 0x1F) << 6) | (char2 & 0x3F));
                    break;
                case 14:
                    /* 1110 xxxx 10xx xxxx 10xx xxxx */
                    count += 3;
                    if (count > utfLength) {
                        System.err.println("Error in UTF formatting: partial character at end");
                        new Exception().printStackTrace();
                        close();
                        System.exit(1);
                    }

                    char2 = (int) buffer[count - 2];
                    char3 = (int) buffer[count - 1];
                    if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80)) {
                        System.err.println("Error in UTF formatting: malformed input around byte "
                                + (count - 1));
                        new Exception().printStackTrace();
                        close();
                        System.exit(1);
                    }

                    chars[charsCount++] =
                            (char) (((c & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
                    break;
                default:
                    /* 10xx xxxx, 1111 xxxx */
                    System.err.println("Error in UTF formatting: malformed input around byte "
                            + count);
                    new Exception().printStackTrace();
                    close();
                    System.exit(1);
            }
        }

        // The number of chars produced may be less than utfLength
        return new String(chars, 0, charsCount);
    }
}
