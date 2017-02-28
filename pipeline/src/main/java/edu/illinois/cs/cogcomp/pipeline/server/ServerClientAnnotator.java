package edu.illinois.cs.cogcomp.pipeline.server;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;

/**
 * Client to make calls to the remote pipeline server
 * @author khashab2
 *
 */
public class ServerClientAnnotator extends Annotator {

    String url = "";
    String port = "";

    String[] viewsToAdd = new String[]{ViewNames.LEMMA};

    public ServerClientAnnotator() {
        super("PipelineServerView", new String[]{});
    }

    public ServerClientAnnotator(String viewName) {
        super(viewName, new String[]{});
    }

    public ServerClientAnnotator(String viewName, String url, String port, String[] viewsToAdd) {
        super(viewName, new String[]{});
        this.url = url;
        this.port = port;
        this.viewsToAdd = viewsToAdd;
    }

    public ServerClientAnnotator(String viewName, String[] requiredViews) throws Exception {
        super(viewName, requiredViews);
        throw new Exception("Do not use this constructor.");
    }

    public void setViews(String... viewsToAdd) {
        this.viewsToAdd = viewsToAdd;
    }

    public void setUrl(String url, String port) {
        this.url = url;
        this.port = port;
    }

    @Override
    public void initialize(ResourceManager rm) {
        // do nothing
    }

    protected TextAnnotation annotate(String str) throws Exception {
        String viewsConnected = Arrays.toString(viewsToAdd);
        String views = viewsConnected.substring(1, viewsConnected.length() - 1).replace(" ", "");
        URL obj = new URL(url + ":" + port + "/annotate?text=\"" + URLEncoder.encode(str, "UTF-8") + "\"&views=" + views);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("charset", "utf-8");
        con.setRequestProperty("Content-Type", "text/plain; charset=utf-8");

        int responseCode = con.getResponseCode();
        System.out.println("\nSending '" + con.getRequestMethod() + "' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return SerializationHelper.deserializeFromJson(response.toString());
    }

    @Override
    protected void addView(TextAnnotation ta) {
        TextAnnotation newTA = null;
        try {
            newTA = annotate(ta.getText());
        } catch (Exception e) {
            e.printStackTrace();
        }
        for(String vu: viewsToAdd) {
            ta.addView(vu, newTA.getView(vu));
        }
    }

    public static void main(String[] args) {
        ServerClientAnnotator annotator = new ServerClientAnnotator();
        String sentA = "This is the best sentence ever.";
        annotator.setUrl("http://austen.cs.illinois.edu", "8080");
        annotator.setViews(ViewNames.POS, ViewNames.LEMMA);
        try {
            TextAnnotation ta = annotator.annotate(sentA);
            System.out.println(ta.getAvailableViews());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

