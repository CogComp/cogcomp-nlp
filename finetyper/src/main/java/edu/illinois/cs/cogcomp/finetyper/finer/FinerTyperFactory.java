package edu.illinois.cs.cogcomp.finetyper.finer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.illinois.cs.cogcomp.finetyper.FinerResource;
import edu.illinois.cs.cogcomp.finetyper.finer.components.typers.IFinerTyper;
import edu.illinois.cs.cogcomp.finetyper.finer.components.mention.MentionDetecter;
import edu.illinois.cs.cogcomp.finetyper.finer.components.typers.HypernymTyper;
import edu.illinois.cs.cogcomp.finetyper.finer.components.typers.KBBiasTyper;
import edu.illinois.cs.cogcomp.finetyper.finer.components.mention.BasicMentionDetection;
import edu.illinois.cs.cogcomp.finetyper.finer.components.mention.TypeMapper;
import edu.illinois.cs.cogcomp.finetyper.finer.components.typers.NGramPattern;
import edu.illinois.cs.cogcomp.finetyper.finer.components.typers.NGramPatternBasedTyper;
import edu.illinois.cs.cogcomp.finetyper.finer.datastructure.types.FinerType;
import edu.illinois.cs.cogcomp.finetyper.finer.datastructure.types.TypeSystem;
import org.cogcomp.Datastore;
import org.cogcomp.DatastoreException;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by haowu4 on 5/16/17.
 */
public class FinerTyperFactory {
    public FinerTyperFactory() throws DatastoreException {
        this(true);
    }

    public FinerTyperFactory(boolean lazyInit) throws DatastoreException {

        this.typers = new ArrayList<>();
        if (!lazyInit) {
            this.init();
        }
    }

    private void init() throws DatastoreException {

        try (InputStream is = ClassLoader.getSystemResourceAsStream("finer_resource/figer_hier.json")) {
            this.typeSystem = TypeSystem.getFromJson(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (InputStream is = ClassLoader.getSystemResourceAsStream("finer_resource/ontonote_type_mapping.json")) {
            this.mentionDetecter = this.getMentionDetecter(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (InputStream is = ClassLoader.getSystemResourceAsStream("finer_resource/patterndb.txt")) {
            this.typers.add(this.getPatternTyper(is));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Now load larger components from Datastore.
        Datastore ds = FinerResource.getDefaultDatastore();
        try (InputStream is = FinerResource.getResourceInputStream(ds, FinerResource.SYNSET2TYPE_TAR_GZ)) {
            this.typers.add(this.getHypTyper(is));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (InputStream is = FinerResource.getResourceInputStream(ds, FinerResource.KB_BIAS_RESOURCE_TAR_GZ)) {
            this.typers.add(this.getKBBiasTyper(is));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public FinerAnnotator getAnnotator() throws DatastoreException {
        if (this.mentionDetecter == null || this.typers.isEmpty()) {
            this.init();
        }
        return new FinerAnnotator(this.mentionDetecter, this.typers);
    }

    private MentionDetecter getMentionDetecter(InputStream is) {
        Gson gson = new GsonBuilder().create();
        Map<String, String> ret = new HashMap<>();

        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(is))) {
            ret = gson.fromJson(reader, ret.getClass());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new BasicMentionDetection(new TypeMapper(this.typeSystem, ret));
    }

    private IFinerTyper getKBBiasTyper(InputStream is) throws IOException {
        Map<String, Map<FinerType, Double>> map = new HashMap<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split("\\t");
            String pattern = parts[0];
            Map<FinerType, Double> scoreMap = new HashMap<>();
            try {
                for (String typeAndScore : parts[1].split(" ")) {
                    FinerType type = getTypeOrFail(typeAndScore.split(":")[0]);
                    double score = Double.parseDouble(typeAndScore.split(":")[1]);
                    scoreMap.put(type, score);
                }
                map.put(pattern, scoreMap);
            } catch (RuntimeException exp) {
                System.err.println("[" + line + "] failed to process..");
            }

        }
        return new KBBiasTyper(map);
    }

    private IFinerTyper getHypTyper(InputStream is) throws IOException {
        Map<String, List<FinerType>> map = new HashMap<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split("\t");
            String synsetId = parts[0];
            List<FinerType> types = Arrays.stream(parts[1].split(" "))
                    .map(this::getType)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
            map.put(synsetId, types);
        }
        return new HypernymTyper(map);
    }

    private IFinerTyper getPatternTyper(InputStream is) throws IOException {
        Map<NGramPattern, List<FinerType>> map = new HashMap<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split("\t");

            int before = Integer.parseInt(parts[0]);
            String[] tokens = parts[1].split(" ");
            int after = Integer.parseInt(parts[2]);

            NGramPattern pattern = new NGramPattern(before, after, tokens);

            List<FinerType> types = Arrays.stream(parts[3].split(" "))
                    .map(this::getType)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
            map.put(pattern, types);
        }
        return new NGramPatternBasedTyper(map);

    }

    private FinerType getTypeOrFail(String name) {
        return this.typeSystem.getTypeOrFail(name);
    }

    private Optional<FinerType> getType(String name) {
        return this.typeSystem.getType(name);
    }


    private TypeSystem typeSystem = null;
    private MentionDetecter mentionDetecter = null;
    private List<IFinerTyper> typers = null;

    public void setMentionDetecter(MentionDetecter mentionDetecter) {
        this.mentionDetecter = mentionDetecter;
    }

    public void setTypers(List<IFinerTyper> typers) {
        this.typers = typers;
    }

    public List<IFinerTyper> getTypers() {
        return typers;
    }
}
