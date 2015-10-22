package edu.illinois.cs.cogcomp.edison.annotators;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Annotator;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.utilities.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

/**
 * Use this class to create a brown cluster view for your text.
 *
 * @author Vivek Srikumar
 */
public class BrownClusterViewGenerator implements Annotator {
	public final static String file100 = "brown-clusters/brown-rcv1.clean.tokenized-CoNLL03.txt-c100-freq1.txt";
	public final static String file320 = "brown-clusters/brown-rcv1.clean.tokenized-CoNLL03.txt-c320-freq1.txt";
	public final static String file1000 = "brown-clusters/brown-rcv1.clean.tokenized-CoNLL03.txt-c1000-freq1.txt";
	public final static String file3200 = "brown-clusters/brown-rcv1.clean.tokenized-CoNLL03.txt-c3200-freq1.txt";
	public static final String ViewName = "BrownClustersView";
	private final Logger log = LoggerFactory.getLogger(BrownClusterViewGenerator.class);

	// private final Map<String, List<ListMatch<String>>> matchers;
	// private final Map<String, List<Integer>> lengths;
	private final Map<String, List<String>> clusterToStrings;

	// private final Map<String, int[]> stringLengths;

	private String name;

	private String file;

	private boolean gzip;

	public BrownClusterViewGenerator(String name, String file) throws Exception {
		this(name, file, false);
	}

	public BrownClusterViewGenerator(String name, String file, boolean gzip) throws Exception {
		this.name = name;
		this.file = file;
		this.gzip = gzip;
		clusterToStrings = new HashMap<>();
	}

	@SuppressWarnings("resource")
	private void loadClusters() throws Exception {
		log.debug("Loading brown clusters from {}", file);

		List<URL> resources = IOUtils.lsResources(BrownClusterViewGenerator.class, file);

		InputStream stream;
		if (resources.size() == 0) {
			stream = new FileInputStream(file);
		} else {
			URL url = resources.get(0);
			stream = url.openStream();
		}

		if (gzip) stream = new GZIPInputStream(stream);

		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

		String line;
		while ((line = reader.readLine()) != null) {

			String[] parts = line.split("\t");
			assert parts.length == 3 : "Error: " + line;

			String clusterId = parts[0];
			String s = parts[1];

			s = StringUtils.normalizeUnicodeDiacritics(s);

			s = s.replaceAll("&amp;", "&");

			s = s.replaceAll("'", " '");
			s = s.replaceAll(",", " ,");
			s = s.replaceAll(";", " ;");
			s = s.replaceAll("\\s+", " ");

			s = s.trim();

			// remove trailing slashes
			if (s.endsWith("/")) s = s.substring(0, s.length() - 1);

			// remove leading and trailing hyphens
			s = s.replaceFirst("\\-+", "");

			// s = SEPARATOR + s.replaceAll("\\s+", SEPARATOR) + SEPARATOR;

			if (!clusterToStrings.containsKey(clusterId)) clusterToStrings.put(clusterId, new ArrayList<String>());

			List<String> list = clusterToStrings.get(clusterId);
			list.add(s);

			if (s.indexOf('-') >= 0) {
				String s1 = s.replaceAll("-", " ").trim();
				list.add(s1);
			}
		}

		reader.close();

		log.info("Finished loading {} brown clusters from {}", clusterToStrings.size(), file);

	}

	@Override
	public String getViewName() {
		return ViewName + ":" + this.name;
	}

	;

	@Override
	public View getView(TextAnnotation ta) {

		lazyLoadClusters();

		SpanLabelView view = new SpanLabelView(ViewName, "BrownClusters", ta, 1.0, true);

		Map<String, List<IntPair>> m = getMatchingSpans(ta);

		for (Entry<String, List<IntPair>> entry : m.entrySet()) {

			String label = entry.getKey();

			Set<IntPair> added = new LinkedHashSet<>();

			for (IntPair p : entry.getValue()) {

				// don't add nested constituents of the same type
				boolean foundContainer = false;
				for (IntPair p1 : added) {
					if (p1 == p) continue;

					if (p1.getFirst() <= p.getFirst() && p1.getSecond() >= p.getSecond()) {
						foundContainer = true;
						break;
					}
				}

				if (!foundContainer) {
					view.addSpanLabel(p.getFirst(), p.getSecond(), label, 1.0);
					added.add(p);
				}
			}
		}

		return view;
	}

	@Override
	public String[] getRequiredViews() {
		return new String[0];
	}

	private void lazyLoadClusters() {
		if (clusterToStrings.size() == 0) {
			synchronized (BrownClusterViewGenerator.class) {
				if (clusterToStrings.size() == 0) {
					try {
						loadClusters();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
	}

	private Map<String, List<IntPair>> getMatchingSpans(TextAnnotation ta) {
		Map<String, List<IntPair>> map = new HashMap<>();

		for (Entry<String, List<String>> entry : this.clusterToStrings.entrySet()) {
			String clusterId = entry.getKey();
			List<String> patterns = entry.getValue();

			for (String pattern : patterns) {
				List<IntPair> list = ta.getSpansMatching(pattern);

				if (list.size() > 0) {
					if (!map.containsKey(clusterId)) map.put(clusterId, new ArrayList<IntPair>());

					map.get(clusterId).addAll(list);
				}
			}

		}
		return map;
	}
}
