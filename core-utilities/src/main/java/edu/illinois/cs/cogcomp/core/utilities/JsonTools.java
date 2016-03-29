package edu.illinois.cs.cogcomp.core.utilities;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;

public class JsonTools {

    private static JsonParser parser = new JsonParser();

    public static JsonArray createJsonArrayFromArray(TextAnnotation[] annotations){
        JsonConverter<TextAnnotation> converter = new JsonConverter<TextAnnotation>() {
            @Override
            public JsonElement convertToJson(TextAnnotation object) {
                String jsonTextAnnotation = SerializationHelper.serializeToJson(object);
                return parser.parse(jsonTextAnnotation);
            }
        };
        return createJsonArrayFromArray(annotations, converter);
    }

    public static JsonArray createJsonArrayFromArray(String[] array){
        JsonConverter<String> converter = new JsonConverter<String>(){
            public JsonElement convertToJson(String object){
                return new JsonPrimitive(object);
            }
        };

        return createJsonArrayFromArray(array, converter);
    }

    private static <T> JsonArray createJsonArrayFromArray(T[] array, JsonConverter<T> converter){
        JsonArray jsonArray = new JsonArray();
        for(int i=0;i<array.length;i++){
            jsonArray.add(converter.convertToJson(array[i]));
        }
        return jsonArray;
    }

    public abstract static class JsonConverter<T> {
        public abstract JsonElement convertToJson(T object);
    }

}
