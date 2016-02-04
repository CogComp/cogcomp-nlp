package edu.illinois.cs.cogcomp.core.utilities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SerializationUtils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class is useful for serializing {@link TextAnnotation} objects to byte arrays.
 */
public class SerializationHelper {
    private static final String NAME = SerializationHelper.class.getCanonicalName();

    /**
     * Use Thrift's serializer to serialize record and then write to file. If forceOverwrite_ is set
     * to false and file already exists, this method throws an exception.
     *
     * @param ta The text annotation to be serialized
     * @param fileName Name of file to write to
     * @param forceOverwrite Whether or not to overwrite existing file.
     */
    public static void serializeTextAnnotationToFile(TextAnnotation ta, String fileName,
            boolean forceOverwrite) throws IOException {
        File outFile = new File(fileName);
        if (outFile.exists() && !forceOverwrite)
            throw new IOException("ERROR: " + NAME + ".serializeTextAnnotationToFile(): file '"
                    + fileName + "' already exists.");
        FileUtils.writeByteArrayToFile(outFile, serializeTextAnnotationToBytes(ta));
    }

    /**
     * Serialize a text annotation into a byte array. This can be useful for writing into a file or
     * a database record. Uses Apache's {@link SerializationUtils}.
     *
     * @param ta The text annotation to be serialized
     * @return A byte array
     */
    public static byte[] serializeTextAnnotationToBytes(TextAnnotation ta) throws IOException {
        return SerializationUtils.serialize(ta);
    }

    /**
     * Read serialized record from file and deserialize it. Expects Thrift serialization format, one
     * record in a single file.
     *
     * @param fileName Name of file to read from
     * @return A text annotation
     */
    public static TextAnnotation deserializeTextAnnotationFromFile(String fileName)
            throws IOException {
        File file = new File(fileName);
        if (!file.exists())
            throw new IOException("ERROR: " + NAME + ".deserializeTextAnnotationFromFile(): file '"
                    + fileName + "' does not exist.");

        return deserializeTextAnnotationFromBytes(FileUtils.readFileToByteArray(file));
    }

    /**
     * Read a text annotation from a byte array. The byte array must be one that is generated by the
     * {@link #serializeTextAnnotationToBytes(TextAnnotation)} function. Uses Apache's
     * {@link SerializationUtils}.
     *
     * @param obj The byte array
     * @return A text annotation
     */
    public static TextAnnotation deserializeTextAnnotationFromBytes(byte[] obj) {
        return (TextAnnotation) SerializationUtils.deserialize(obj);
    }

    /**
     * Serialize a text annotation into a json string. This can be useful for writing into a file or
     * a database record
     *
     * @param ta The text annotation to be serialized
     * @return A json string
     */
    public static String serializeToJson(TextAnnotation ta) {

        JsonSerializer serializer = new JsonSerializer();

        JsonElement json = serializer.writeTextAnnotation(ta);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        return gson.toJson(json);
    }

    /**
     * Read a text annotation from a json string. The input string must be one that is generated by
     * the {@link SerializationHelper#serializeToJson(TextAnnotation)} function
     *
     * @param jsonString The json string representation for the text
     * @return A text annotation
     * @throws Exception
     */
    public static TextAnnotation deserializeFromJson(String jsonString) throws Exception {

        JsonSerializer serializer = new JsonSerializer();
        return serializer.readTextAnnotation(jsonString);
    }
}
