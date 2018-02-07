package edu.illinois.cs.cogcomp.finetyper;

import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import org.cogcomp.Datastore;
import org.cogcomp.DatastoreException;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Created by haowu4 on 2/7/18.
 */
public class FinerResourceUpload {
    public static void main(String[] args) throws InvalidPortException, InvalidEndpointException, DatastoreException {
        String accessKey = "7BDP8NOBCLRHDENUDZ1H";
        String secretKey = "zkTS6t8OsmXuVHqClpDMlwaG6wKvLy/uKz3aDUbL";
        String serverAddress = "http://127.0.0.1:9000";

        String baseFolder = "/data/fine-tpying-package";

        Datastore datastore = new Datastore(serverAddress, accessKey, secretKey);
        String[] toupload = new String[]{
                FinerResource.WORD_EMBEDDING_TAR_GZ,
                FinerResource.SENSE_EMBEDDING_TAR_GZ,
                FinerResource.KB_BIAS_RESOURCE_TAR_GZ,
                FinerResource.WORD_POS_TO_SENSE_TAR_GZ,
                FinerResource.SYNSET2TYPE_TAR_GZ
        };

        for (String file : toupload) {
            datastore.publishFile(FinerResource.FINER_RESOURCE_GROUP_ID, file, 1.0, new File(baseFolder, file).getAbsolutePath());
        }

    }
}