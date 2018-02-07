package edu.illinois.cs.cogcomp.finetyper;

import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.cogcomp.Datastore;
import org.cogcomp.DatastoreException;

import java.io.*;

/**
 * Created by haowu4 on 2/3/18.
 */
public class FinerResource {

    public static final String FINER_RESOURCE_GROUP_ID = "edu.cogcomp.cs.illinois.finetyper";

    public static final String WORD_EMBEDDING_TAR_GZ = "word_embedding.txt.tar.gz";
    public static final String SENSE_EMBEDDING_TAR_GZ = "synset_embeddings_300.txt.tar.gz";
    public static final String KB_BIAS_RESOURCE_TAR_GZ = "kbias.txt.tar.gz";
    public static final String WORD_POS_TO_SENSE_TAR_GZ = "word_pos_to_synsets.txt.tar.gz";
    public static final String SYNSET2TYPE_TAR_GZ = "synset2TypeMap.txt.tar.gz";

    public static InputStream getTarGZInputStrem(File file) throws IOException {
        return new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(file)));
    }

    public static Datastore getDefaultDatastore() throws DatastoreException {
        return new Datastore();
    }

    public static InputStream getResourceInputStream(Datastore dataStore, String name) throws IOException, DatastoreException {
        File file = dataStore.getFile(
                FinerResource.FINER_RESOURCE_GROUP_ID, name, 1.0);
        if (name.endsWith("tar.gz")) {
            return getTarGZInputStrem(file);
        }
        return new FileInputStream(file);
    }


}
