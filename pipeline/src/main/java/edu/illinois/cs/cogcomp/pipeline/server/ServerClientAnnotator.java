/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.pipeline.server;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.io.caches.TextAnnotationMapDBHandler;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import org.apache.commons.codec.digest.DigestUtils;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.concurrent.ConcurrentMap;

/**
 * Client to make calls to the remote pipeline server
 * 
 * @author khashab2
 *
 */
public class ServerClientAnnotator extends Annotator {

    private String url = "";
    private String port = "";
    private DB db = null;
    private String[] viewsToAdd = new String[] {ViewNames.LEMMA};

    private static Logger logger = LoggerFactory.getLogger(ServerClientAnnotator.class);

    public ServerClientAnnotator() {
        super("PipelineServerView", new String[] {});
    }

    public ServerClientAnnotator(String viewName) {
        super(viewName, new String[] {});
    }

    public ServerClientAnnotator(String viewName, String url, String port, String[] viewsToAdd) {
        super(viewName, new String[] {});
        this.url = url;
        this.port = port;
        this.viewsToAdd = viewsToAdd;
    }

    public ServerClientAnnotator(String viewName, String[] requiredViews) throws Exception {
        super(viewName, requiredViews);
        throw new Exception("Do not use this constructor.");
    }

    public void setViewsAll(String[] viewsToAdd) {
        this.viewsToAdd = viewsToAdd;
    }

    public void setViews(String... viewsToAdd) {
        this.viewsToAdd = viewsToAdd;
    }

    public void setUrl(String url, String port) {
        this.url = url;
        this.port = port;
    }

    public void useCaching() {
        useCaching("serverClientAnnotator.cache");
    }

    public void useCaching(String dbFile) {
        this.db = DBMaker.fileDB(dbFile).
                closeOnJvmShutdown().
                transactionEnable(). // with transaction enabled, the cache won't get corrupt if the program crashes (at the cost of losing a little speed)
                make();
    }

    /**
     * MapDB requires the database to be closed at the end of operations. This is usually handled by
     * the {@code closeOnJvmShutdown()} snippet in the initializer, but this method needs to be
     * called if multiple instances of the {@link TextAnnotationMapDBHandler} are used.
     */
    public void close() {
        db.close();
    }

    @Override
    public void initialize(ResourceManager rm) {
        // do nothing
    }

    public TextAnnotation annotate(String str) throws Exception {
        String viewsConnected = Arrays.toString(viewsToAdd);
        String views = viewsConnected.substring(1, viewsConnected.length() - 1).replace(" ", "");
        ConcurrentMap<String, byte[]> concurrentMap =
                (db != null) ? db.hashMap(viewName, Serializer.STRING, Serializer.BYTE_ARRAY)
                        .createOrOpen() : null;
        String key = DigestUtils.sha1Hex(str + views);
        if (concurrentMap != null && concurrentMap.containsKey(key)) {
            byte[] taByte = concurrentMap.get(key);
            return SerializationHelper.deserializeTextAnnotationFromBytes(taByte);
        } else {
            URL obj =
                    new URL(url + ":" + port + "/annotate");
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("charset", "utf-8");
            con.setRequestProperty("Content-Type", "text/plain; charset=utf-8");

            con.setDoOutput(true);
            con.setUseCaches(false);

            OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
            wr.write("text=" + URLEncoder.encode(str, "UTF-8") + "&views=" + views);
            wr.flush();

            int responseCode = con.getResponseCode();
            logger.debug("\nSending '" + con.getRequestMethod() + "' request to URL : " + url);
            logger.debug("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            TextAnnotation ta = SerializationHelper.deserializeFromJson(response.toString());
            if (concurrentMap != null) {
                concurrentMap.put(key, SerializationHelper.serializeTextAnnotationToBytes(ta));
                this.db.commit();
            }
            return ta;
        }
    }

    @Override
    public void addView(TextAnnotation ta) {
        TextAnnotation newTA = null;
        try {
            newTA = annotate(ta.getText());
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (String vu : viewsToAdd) {
            ta.addView(vu, newTA.getView(vu));
        }
    }

    public static void main(String[] args) {
        ServerClientAnnotator annotator = new ServerClientAnnotator();
        String sentA = "This is the best sentence ever.";
        annotator.setUrl("http://austen.cs.illinois.edu", "8080");
        annotator.setViews(ViewNames.POS, ViewNames.LEMMA);
        annotator.useCaching();
        try {
            TextAnnotation ta = annotator.annotate(sentA);
            logger.info(ta.getAvailableViews().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
