package experiments;

import org.cogcomp.Datastore;

import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

public class Minio {
	public void upload() throws Exception {
		String file = "config/datastore.properties";
		ResourceManager rm = new ResourceManager(file);
		String endpoint = rm.getString("ENDPOINT");
		String accessKey = rm.getString("ACCESS-KEY");
		String secretKey = rm.getString("SECRET-KEY");
		Datastore ds = new Datastore(endpoint, accessKey, secretKey);
		/**
		 * ds.publishFile("org.cogcomp.wordembedding", "word2vec.txt", 1.5,
		 * "/shared/bronte/sling3/data/vectors-enwikitext_vivek200.txt", false,
		 * true); ds.publishFile("org.cogcomp.wordembedding", "glove.txt", 1.5,
		 * "/shared/bronte/sling3/data/glove.6B.200d.txt", false, true);
		 * ds.publishFile("org.cogcomp.wordembedding", "phrase2vec.txt", 1.5,
		 * "/shared/bronte/sling3/data/phraseEmbedding.txt", false, true);
		 **/
		ds.publishFile("org.cogcomp.wordembedding", "memorybasedESA.txt", 1.5,
				"/shared/bronte/sling3/data/MemoryBasedESA.txt", false, true);
		ds.publishFile("org.cogcomp.wordembedding", "pageIDMapping.txt", 1.5,
				"/shared/bronte/sling3/data/wikiPageIDMapping.txt", false, true);

	}

	public static void main(String[] args) throws Exception {
		new Minio().upload();
	}

}
