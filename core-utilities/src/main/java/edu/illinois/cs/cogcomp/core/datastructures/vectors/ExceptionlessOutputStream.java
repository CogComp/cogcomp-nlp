/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.datastructures.vectors;

import java.io.*;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/**
 * This class intends to operate just as a <code>DataOutputStream</code> with some additional
 * convenience methods and built-in exception handling.
 *
 * @author Nick Rizzolo
 **/
public class ExceptionlessOutputStream extends FilterOutputStream {
    /** This buffer is used internally by {@link #writeUTF(String)}. */
    private byte[] buffer = null;
    /** The underlying data output stream. */
    private DataOutputStream dos;


    /**
     * Opens a buffered (and uncompressed) stream for writing to the specified file.
     *
     * @param filename The file to write to.
     * @return The newly opened stream.
     **/
    public static ExceptionlessOutputStream openBufferedStream(String filename) {
        ExceptionlessOutputStream eos = null;

        try {
            eos =
                    new ExceptionlessOutputStream(new BufferedOutputStream(new FileOutputStream(
                            filename)));
        } catch (Exception e) {
            System.err.println("Can't open '" + filename + "' for output:");
            e.printStackTrace();
            System.exit(1);
        }

        return eos;
    }


    /**
     * Opens a compressed stream for writing to the specified file.
     *
     * @param filename The file to write to.
     * @return The newly opened stream.
     **/
    public static ExceptionlessOutputStream openCompressedStream(String filename) {
        ExceptionlessOutputStream eos = null;

        try {
            ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(filename));
            zip.putNextEntry(new ZipEntry(ExceptionlessInputStream.zipEntryName));
            eos = new ExceptionlessOutputStream(new BufferedOutputStream(zip));
        } catch (Exception e) {
            System.err.println("Can't open '" + filename + "' for output:");
            e.printStackTrace();
            System.exit(1);
        }

        return eos;
    }


    /**
     * Opens a buffered (and uncompressed) stream for writing to the specified file. If the
     * specified URL does not reference a file on the local file system, an error message will be
     * displayed, and the program will exit.
     *
     * @param url The location of the file to write to.
     * @return The newly opened stream.
     **/
    public static ExceptionlessOutputStream openBufferedStream(URL url) {
        if (!url.getProtocol().equals("file")) {
            System.err.println("Can't open URL with protocol '" + url.getProtocol()
                    + "' for output.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        return openBufferedStream(url.getFile());
    }


    /**
     * Opens a buffered stream for writing to the specified file. If the specified URL does not
     * reference a file on the local file system, an error message will be displayed, and the
     * program will exit.
     *
     * @param url The location of the file to write to.
     * @return The newly opened stream.
     **/
    public static ExceptionlessOutputStream openCompressedStream(URL url) {
        if (!url.getProtocol().equals("file")) {
            System.err.println("Can't open URL with protocol '" + url.getProtocol()
                    + "' for output.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        return openCompressedStream(url.getFile());
    }


    /**
     * Creates a new data output stream to write data to the specified underlying output stream.
     *
     * @param out The underlying output stream.
     **/
    public ExceptionlessOutputStream(OutputStream out) {
        super(new DataOutputStream(out));
        dos = (DataOutputStream) this.out;
    }


    /**
     * Whenever an exception is caught, this method attempts to close the stream and exit the
     * program.
     *
     * @param e The thrown exception.
     **/
    private void handleException(Exception e) {
        System.err.println("Can't write to output stream:");
        e.printStackTrace();
        close();
        System.exit(1);
    }


    /**
     * Closes this output stream and releases any system resources associated with the stream.
     **/
    public void close() {
        try {
            dos.close();
        } catch (Exception e) {
            System.err.println("Can't close output stream:");
            e.printStackTrace();
            System.exit(1);
        }
    }


    /**
     * Writes a <code>boolean</code> to the underlying output stream as a 1-byte value. The value
     * <code>true</code> is written out as the value <code>(byte)1</code>; the value
     * <code>false</code> is written out as the value <code>(byte)0</code>.
     *
     * @param v A <code>boolean</code> value to be written.
     **/
    public void writeBoolean(boolean v) {
        try {
            dos.writeBoolean(v);
        } catch (Exception e) {
            handleException(e);
        }
    }


    /**
     * Writes out a <code>byte</code> to the underlying output stream as a 1-byte value.
     *
     * @param v A <code>byte</code> value to be written.
     **/
    public void writeByte(int v) {
        try {
            dos.writeByte(v);
        } catch (Exception e) {
            handleException(e);
        }
    }


    /**
     * Writes an array of bytes to the underlying output stream. First, an integer representing the
     * number of bytes in the array is written, followed by the bytes in the array.
     *
     * @param ba The array of 8-bit bytes.
     **/
    public void writeBytes(byte[] ba) {
        try {
            if (ba == null) {
                dos.writeInt(-1);
                return;
            }

            int n = ba.length;
            dos.writeInt(n);
            for (int i = 0; i < n; ++i)
                dos.writeByte(ba[i]);
        } catch (Exception e) {
            handleException(e);
        }
    }


    /**
     * Writes a <code>short</code> to the underlying output stream as two bytes, high byte first.
     *
     * @param v A <code>short</code> to be written.
     **/
    public void writeShort(int v) {
        try {
            dos.writeShort(v);
        } catch (Exception e) {
            handleException(e);
        }
    }


    /**
     * Writes a <code>char</code> to the underlying output stream as a 2-byte value, high byte
     * first.
     *
     * @param v A <code>char</code> value to be written.
     **/
    public void writeChar(int v) {
        try {
            dos.writeChar(v);
        } catch (Exception e) {
            handleException(e);
        }
    }


    /**
     * Writes an <code>int</code> to the underlying output stream as four bytes, high byte first.
     *
     * @param v An <code>int</code> to be written.
     **/
    public void writeInt(int v) {
        try {
            dos.writeInt(v);
        } catch (Exception e) {
            handleException(e);
        }
    }


    /**
     * Writes a <code>long</code> to the underlying output stream as eight bytes, high byte first.
     *
     * @param v A <code>long</code> to be written.
     */
    public void writeLong(long v) {
        try {
            dos.writeLong(v);
        } catch (Exception e) {
            handleException(e);
        }
    }


    /**
     * Converts the float argument to an <code>int</code> using the <code>floatToIntBits</code>
     * method in class <code>Float</code>, and then writes that <code>int</code> value to the
     * underlying output stream as a 4-byte quantity, high byte first.
     *
     * @param v A <code>float</code> value to be written.
     **/
    public void writeFloat(float v) {
        try {
            dos.writeFloat(v);
        } catch (Exception e) {
            handleException(e);
        }
    }


    /**
     * Converts the double argument to a <code>long</code> using the <code>doubleToLongBits</code>
     * method in class <code>Double</code>, and then writes that <code>long</code> value to the
     * underlying output stream as an 8-byte quantity, high byte first.
     *
     * @param v A <code>double</code> value to be written.
     **/
    public void writeDouble(double v) {
        try {
            dos.writeDouble(v);
        } catch (Exception e) {
            handleException(e);
        }
    }


    /**
     * Writes a string to the underlying stream in such a way that it can be read back in. In
     * particular, the length of the string is written first.
     *
     * @param s The string to write.
     **/
    public void writeString(String s) {
        if (s == null)
            writeShort((short) -1);
        else
            writeUTF(s);
    }


    /**
     * Writes a string using <a
     * href="http://java.sun.com/javase/6/docs/api/java/io/DataInput.html#modified-utf-8">modified
     * UTF-8</a> encoding in a machine-independent manner.
     *
     * <p>
     * First, two bytes are written to out via the {@link #writeShort(int)} method giving the number
     * of bytes to follow. This value is the number of bytes actually written out, not the length of
     * the string. Following the length, each character of the string is output, in sequence, using
     * the modified UTF-8 encoding for the character.
     *
     * @param str A string to be written.
     * @return The number of bytes written out.
     **/
    public int writeUTF(String str) {
        int strlen = str.length();
        int utfLength = 0;
        int c, count = 0;

        /* use charAt instead of copying String to char array */
        for (int i = 0; i < strlen; i++) {
            c = str.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F))
                utfLength++;
            else if (c > 0x07FF)
                utfLength += 3;
            else
                utfLength += 2;
        }

        if (utfLength > 32767) {
            System.err.println("Error in ExceptionlessOutputStream: String too long");
            new Exception().printStackTrace();
            close();
            System.exit(1);
        }

        if (buffer == null || buffer.length < utfLength)
            buffer = new byte[utfLength * 2];

        writeShort((short) utfLength);

        int i = 0;
        for (i = 0; i < strlen; i++) {
            c = str.charAt(i);
            if (!((c >= 0x0001) && (c <= 0x007F)))
                break;
            buffer[count++] = (byte) c;
        }

        for (; i < strlen; i++) {
            c = str.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F))
                buffer[count++] = (byte) c;
            else if (c > 0x07FF) {
                buffer[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
                buffer[count++] = (byte) (0x80 | ((c >> 6) & 0x3F));
                buffer[count++] = (byte) (0x80 | ((c >> 0) & 0x3F));
            } else {
                buffer[count++] = (byte) (0xC0 | ((c >> 6) & 0x1F));
                buffer[count++] = (byte) (0x80 | ((c >> 0) & 0x3F));
            }
        }

        try {
            dos.write(buffer, 0, utfLength);
        } catch (Exception e) {
            handleException(e);
        }
        return utfLength + 2;
    }
}
