package edu.illinois.cs.cogcomp.llm.common;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.io.FileUtils;
import org.cogcomp.DatastoreException;
import org.xmlpull.v1.XmlPullParserException;

import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidArgumentException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import io.minio.errors.NoResponseException;


public class Datastore {
    private MinioClient minioClient = null;
    private static final String CONFIG_FILE = "src/main/resources/datastore-config-sample.properties";

    // this is where we keep the files locally
    private String DATASTORE_FOLDER = "src/main/resources/";

    public Datastore() throws DatastoreException {
        // Create a minioClient with the information read from configuration file
        ResourceManager rm = null;
        try {
            rm = new ResourceManager(CONFIG_FILE);
        } catch (IOException e) {
            e.printStackTrace();
            throw new DatastoreException("invalid resource keys . . . ");
        }

        String endpoint = rm.getString("ENDPOINT");
        System.out.println(endpoint);
        if(rm.containsKey("ACCESS-KEY") && rm.containsKey("SECRET-KEY")) {
            String accessKey = rm.getString("ACCESS-KEY");
            String secretKey = rm.getString("SECRET-KEY");
            System.out.println("Reading config informtion from file . . . \n");
            System.out.println("\t\tEndpoint: " + endpoint);
            System.out.println("\t\tAccessKey: " + accessKey);
            System.out.println("\t\tSecretKey: " + secretKey);
            try {
                minioClient = new MinioClient(endpoint, accessKey, secretKey);
            } catch (InvalidEndpointException e) {
                e.printStackTrace();
                throw new DatastoreException("Invalid end-point url . . .");
            } catch (InvalidPortException e) {
                e.printStackTrace();
                throw new DatastoreException("Invalid end-point port . . .");
            }
        }
        else {
            try {
                minioClient = new MinioClient(endpoint);
            } catch (InvalidEndpointException e) {
                e.printStackTrace();
                throw new DatastoreException("Invalid end-point url . . .");
            } catch (InvalidPortException e) {
                e.printStackTrace();
                throw new DatastoreException("Invalid end-point port . . .");
            }
        }
    }

 

    public File getFile(String groupId, String artifactId, Double version) throws DatastoreException {
        return getFile(groupId, artifactId, version, false);
    }

    public File getFile(String groupId, String artifactId, Double version, Boolean isPrivate) throws DatastoreException {
        String versionedFileName = getNormalizedArtifactId(artifactId, version);
        String augmentedGroupId = (isPrivate? "private.": "readonly.") + groupId;
        System.out.println("Downloading the file from datastore . . . ");
        System.out.println("\t\tGroupId: " + augmentedGroupId);
        System.out.println("\t\tArtifactId: " + versionedFileName);
        String fileFolder = DATASTORE_FOLDER + File.separator + augmentedGroupId;
        IOUtils.mkdir(fileFolder);
        if(versionedFileName.contains("/")) {
            int idx = versionedFileName.lastIndexOf("/");
            String location = fileFolder + File.separator + versionedFileName.substring(0, idx);
            try {
                FileUtils.forceMkdir(new File(location));
            } catch (IOException e) {
                e.printStackTrace();
                throw new DatastoreException("Unable to create folder in your local machine " + location + " . . .");
            }
        }
        try {
            minioClient.getObject(augmentedGroupId, versionedFileName, fileFolder + File.separator + versionedFileName);
        } catch (InvalidBucketNameException e) {
            e.printStackTrace();
            throw new DatastoreException("Invalid bucket name . . . ");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InsufficientDataException e) {
            e.printStackTrace();
            throw new DatastoreException("Insufficient data . . . ");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            throw new DatastoreException("Invalid key . . . ");
        } catch (NoResponseException e) {
            e.printStackTrace();
            throw new DatastoreException("No server response . . . ");
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (ErrorResponseException e) {
            e.printStackTrace();
        } catch (InternalException e) {
            e.printStackTrace();
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
        return new File(fileFolder + File.separator + versionedFileName);
    }

 

    private String getNormalizedArtifactId(String artifactId, Double version) {
        // old way: put the version number between the file name and its extension
        /*
        String extension = FilenameUtils.getExtension(artifactId);
        String fileNameWithoutExtension = artifactId.replace(extension, "");
        fileNameWithoutExtension + "-" + Double.toString(version) + (extension.equals("")? "": "." + extension);
        */
        return version + File.separator + artifactId;
    }

    /**
     * Download resource file from cogcom server
     * Simply comment out the file that you don't want to download
     * Notice: It may take hours to download all the files
     * @param args
     * @throws DatastoreException
     */
    public static void main(String[] args) throws DatastoreException{
    	Datastore ds=new Datastore();
    	
    	ds.getFile("org.cogcomp.wordembedding", "word2vec.txt", 1.5);
    	
    	ds.getFile("org.cogcomp.wordembedding", "glove.txt", 1.5);
    	
    	ds.getFile("org.cogcomp.wordembedding", "phrase2vec.txt", 1.5);
    	
    	ds.getFile("org.cogcomp.wordembedding", "memorybasedESA.txt", 1.5);
    	
    	ds.getFile("org.cogcomp.wordembedding", "pageIDMapping.txt", 1.5);
    }
 
}
