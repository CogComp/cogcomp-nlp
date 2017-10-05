package edu.illinois.cs.cogcomp.annotation;

import edu.illinois.cs.cogcomp.annotation.server.IllinoisAbstractServer;
import edu.illinois.cs.cogcomp.thrift.labeler.Labeler;
import org.apache.commons.cli.Options;

/**
 * Created by mayhew2 on 1/14/16.
 */
public class TransliterationServer extends IllinoisAbstractServer {
    public TransliterationServer(Class c) {
        super(c);
    }

    public static void main(String[] args) {

        TransliterationServer s = new TransliterationServer(TransliterationServer.class);

        Options options = createOptions();

        s.parseCommandLine(options, args);

        Labeler.Iface handler = null;
        Labeler.Processor processor = null;

        String trainingfile = "/shared/corpora/transliteration/wikidata/wikidata.Hindi";
        try {
            handler = new TransliterationHandler(trainingfile);
            processor = new Labeler.Processor(handler);
        } catch (Exception e) {
            s.logger.error("Couldn't start the handler.... the exception was\n"+e.toString(), e.toString());
            System.exit(0);
        }

        s.runServer(processor);
    }

}
