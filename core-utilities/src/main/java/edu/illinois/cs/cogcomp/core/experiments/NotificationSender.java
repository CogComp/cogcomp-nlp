/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.experiments;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * An API to send notifications to android devices via the Notify My Android app. You need to have
 * an account with <a href="https://www.notifymyandroid.com/register.jsp">Notify My Android</a>.
 * <p>
 * Code adapted from from Adriano Maia (adriano@usk.bz)
 *
 * @author Christos Christodoulopoulos
 * @see <a href="https://github.com/uskr/NMAClientLib">https://github.com/uskr/NMAClientLib</a>
 */
public class NotificationSender {
    private static Logger logger = LoggerFactory.getLogger(NotificationSender.class);

    private static String API_KEY;
    private static final String DEFAULT_URL = "https://www.notifymyandroid.com";
    private static String APP_NAME = "CogComp project";
    private static String EVENT_NAME = "Experiment Complete";

    /**
     * Basic constructor. You only need the file that contains the API key.
     *
     * @param apiKeyFile The file containing the 48-bit API key from <a
     *        href="https://www.notifymyandroid.com/account.jsp">Notify My Android</a>
     */
    public NotificationSender(String apiKeyFile) {
        try {
            BufferedReader in =
                    new BufferedReader(new InputStreamReader(new FileInputStream(new File(
                            apiKeyFile))));
            API_KEY = in.readLine();
        } catch (IOException e) {
            System.err.println("Cannot read API key file");
            API_KEY = null;
        }
    }

    /**
     * A extended constructor that allows the definition of the name of the program (the title of
     * the notification) and the event name (the subtitle of the notification).
     *
     * @param apiKeyFile The file containing the 48-bit API key from <a
     *        href="https://www.notifymyandroid.com/account.jsp">Notify My Android</a>
     * @param appName The name of the program sending the notification
     * @param eventName The event name (e.g. Experiment Complete!)
     */
    public NotificationSender(String apiKeyFile, String appName, String eventName) {
        this(apiKeyFile);
        APP_NAME = appName;
        EVENT_NAME = eventName;
    }

    public void notify(String message) {
        if (API_KEY == null) {
            logger.error("API key not set");
            return;
        }
        try {
            // Verify that the API key is correct
            sendRequest(null);
            // Sending a notification
            logger.info(sendRequest(message));
        } catch (IOException | SAXException | ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a notification using NMA public API.
     *
     * @param description Long description or message body (Up to 10000 characters)
     * @return result
     */
    private static String sendRequest(String description) throws IOException,
            ParserConfigurationException, SAXException {
        URL url;
        if (description == null)
            url = new URL(DEFAULT_URL + "/publicapi/verify");
        else
            url = new URL(DEFAULT_URL + "/publicapi/notify");

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setUseCaches(false);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        // Setup the POST data
        StringBuilder data = new StringBuilder();
        addEncodedParameter(data, "apikey", API_KEY);
        if (description != null) {
            addEncodedParameter(data, "application", APP_NAME);
            addEncodedParameter(data, "event", EVENT_NAME);
            addEncodedParameter(data, "description", description);
            addEncodedParameter(data, "priority", Integer.toString(0));
        }

        // Buffers and Writers to send the data
        OutputStreamWriter writer;
        writer = new OutputStreamWriter(connection.getOutputStream());

        writer.write(data.toString());
        writer.flush();
        writer.close();
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }

            boolean msgSent = false;

            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource inStream = new InputSource();
            inStream.setCharacterStream(new StringReader(response.toString()));
            Document doc = db.parse(inStream);

            Element root = doc.getDocumentElement();

            if (root.getTagName().equals("nma")) {
                Node item = root.getFirstChild();
                String childName = item.getNodeName();
                if (childName.equals("success"))
                    msgSent = true;
            }

            return (msgSent) ? "Message sent successfully" : "Message failed to send";
        } else {
            return "There was a problem contacting NMA Servers. "
                    + "HTTP Response code different than 200(OK). "
                    + "Try again or contact support@notifymyandroid.com if it persists.";
        }
    }

    /**
     * Dynamically adds a url-form-encoded key/value to a StringBuilder
     *
     * @param sb StringBuilder buffer used to build the final url-form-encoded data
     * @param name Key name
     * @param value Value
     */
    private static void addEncodedParameter(StringBuilder sb, String name, String value)
            throws IOException {
        if (sb.length() > 0) {
            sb.append("&");
        }
        sb.append(URLEncoder.encode(name, "UTF-8"));
        sb.append("=");
        if (value == null)
            throw new IOException("ERROR: " + name + " is null");
        else
            sb.append(URLEncoder.encode(value, "UTF-8"));
    }
}
