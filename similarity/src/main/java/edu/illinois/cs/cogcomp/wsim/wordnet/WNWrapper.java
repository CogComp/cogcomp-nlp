/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.wsim.wordnet;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import edu.mit.jwi.IRAMDictionary;
import edu.mit.jwi.RAMDictionaryFromJar;
import edu.mit.jwi.data.ILoadPolicy;
import edu.mit.jwi.RAMDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.IPointer;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;
import edu.mit.jwi.morph.WordnetStemmer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WNWrapper {

	private static final String NAME = WNWrapper.class.getCanonicalName();
	public IRAMDictionary dict = null;
	public WordnetStemmer wstem = null;

	private static WNWrapper instance;
	private Logger logger = LoggerFactory.getLogger(WNWrapper.class);

	public synchronized static WNWrapper getWNWrapper(String wnhome, boolean useJar) throws IOException {
		if (null == instance)
			instance = new WNWrapper(wnhome, useJar);

		return instance;
	}

	private WNWrapper(String wnhome, boolean useJar) throws IOException {
		try {
			if (useJar) {
				RAMDictionaryFromJar rd = new RAMDictionaryFromJar();
				dict = rd.getRAMDictionaryFromJar(wnhome);
			} else {
				URL url = new URL("file", null, wnhome);
				dict = new RAMDictionary(url, ILoadPolicy.NO_LOAD);
			}
			dict.open();
			/*
			 * PLEASE NEVER DO THIS while(dict.isOpen()==false);
			 */
			Thread.sleep(500);
			wstem = new WordnetStemmer(dict);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/*
	 * @param args: path to the local Wordnet resource folder
	 */
	public static void main(String[] args) {
		WnsimUtils.checkArgsOrDie(args, 1, NAME, "wnPath");
		WNWrapper wrap = null;
		try {
			wrap = WNWrapper.getWNWrapper(args[0], false);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}

		ArrayList<String> lis = wrap.getStemsList("go");
		System.out.println(Arrays.toString(lis.toArray()));
	}

	public int getSenseCount(String w, POS p) {
		ArrayList<IWord> iwords = getIWords(w, p);

		return iwords.size();
	}

	public boolean inWN(String w) {
		boolean in = false;
		try {
			in = dict.getIndexWord(w, POS.NOUN) != null;
		} catch (Exception e) {
			return false;
		}
		in = in || dict.getIndexWord(w, POS.ADJECTIVE) != null;
		in = in || dict.getIndexWord(w, POS.ADVERB) != null;
		in = in || dict.getIndexWord(w, POS.VERB) != null;
		return in;
	}

	public void getSynsetId(String word) {
		List<String> stems = wstem.findStems(word, null);
		if (stems.size() > 0)
			word = stems.get(0);
		IIndexWord idxWord = dict.getIndexWord(word, POS.NOUN);
		IWordID wordID = idxWord.getWordIDs().get(0); // 1 st meaning
		IWord iword = dict.getWord(wordID);
		ISynset synset = iword.getSynset();
		System.out.println(synset.getID());
	}

	public void getStems(String word) {
		List<String> stems = wstem.findStems(word, null);
		for (String s : stems) {
			System.out.println(s);
		}
	}

	public String getTopStem(String word) {
		return getStemsList(word).get(0);
	}

	public ArrayList<String> getStemsList(String word) { // experimental
															// change!!!!!!!!!!!!!!!!!!!!!!!
		return getStemsList(word, null);
	}

	public ArrayList<String> getStemsList(String word, POS pos) { // experimental
																	// change!!!!!!!!!!!!!!!!!!!!!!!
		List<String> stems = new ArrayList<String>();
		if (pos != null) {
			List<String> stems1 = wstem.findStems(word, pos);
			stems = fix(stems1, word.substring(0, 1), word);
		} else {
			for (POS p : POS.values()) {
				try {
					List<String> stems1 = wstem.findStems(word, p);
					stems.addAll(fix(stems1, word.substring(0, 1), word));
				} catch (Exception e) {
				}
			}
		}
		stems = new ArrayList<String>((new HashSet<String>(stems)));
		if (stems.size() == 0) {
			stems.add(word);
			return (ArrayList<String>) stems;
		}
		// System.out.println(Arrays.toString(stems.toArray(new String[0]))+"
		// "+word);
		return (ArrayList<String>) stems;
	}

	private List<String> fix(List<String> stems, String substring, String word) {
		ArrayList<String> stms = new ArrayList<String>();

		for (String s : stems) {
			// System.out.println(s);
			if (s.equals("ha") && word.equals("has"))
				s = "has";
			if (s.equals("doe") && word.equals("does"))
				s = "does";
			stms.add(s);
			break;
		}

		return stms;
	}

	public void printSynsets(String word) {
		IIndexWord idxWord = dict.getIndexWord(word, POS.NOUN);
		IWordID wordID = idxWord.getWordIDs().get(0); // 1 st meaning
		IWord iword = dict.getWord(wordID);
		ISynset synset = iword.getSynset();
		List<ISynsetID> sets = synset.getRelatedSynsets(Pointer.HYPERNYM);
		for (ISynsetID is : sets) {
			System.out.println(synset);
			recurseSynset(dict.getSynset(is));
		}
	}

	public void getAllSemanticRelations(String lemma) {
		// for each stem, for each sense in stemn\,print lemma, sensem,
		// probability, sr and lr
		List<String> stems = wstem.findStems(lemma, null);
		for (String s : stems) {
			for (POS pos : POS.values()) {
				IIndexWord idxWord = dict.getIndexWord(lemma, pos);
				if (idxWord == null)
					continue;
				List<IWordID> senses = idxWord.getWordIDs(); // 1 st meaning
				for (IWordID iw : senses) {
					ISynset is = dict.getSynset(iw.getSynsetID());
					System.out.println(is.getGloss());
					getAllSemanticRelations(is);
				}
			}
		}
	}

	public ArrayList<IWord> getAntonyms(String st) {
		return getAntonyms(st, null);
	}

	public ArrayList<IWord> getAntonyms(String st, POS p) {
		ArrayList<IWord> iwds = getIWords(st, p);
		ArrayList<IWord> antonyms = new ArrayList<IWord>();
		for (IWord iw : iwds) {
			List<IWordID> ants = iw.getRelatedMap().get(Pointer.ANTONYM);
			if (ants == null)
				continue;
			for (IWordID id : ants) {
				// System.out.println(dict.getWord(id));
				antonyms.add(dict.getWord(id));
			}
		}

		return antonyms;
	}

	public boolean isAntonym(String w1, POS p1, String w2, POS p2) {
		ArrayList<IWord> ants = getAntonyms(w1, p1);
		ArrayList<IWord> wds = getIWords(w2, p2);

		for (IWord ant : ants) {
			for (IWord iw : wds) {
				if (ant.equals(iw))
					return true;
			}
		}

		return false;
	}

	public boolean isAntonym(String w1, String w2) {
		return isAntonym(w1, null, w2, null);
	}

	/* uncomment to let capital letterered words be converted to lowercase */
	public ArrayList<ISynset> getAllSynset(String word) {
		ArrayList<ISynset> syns = getAllSynset(word, false, null);
		if (syns.size() == 0)
			return getAllSynset(word, true, null);
		return syns;
	}

	public ArrayList<ISynset> getAllSynset(String word, POS pos) {
		try {
			ArrayList<ISynset> syns = getAllSynset(word, false, pos);
			if (syns.size() == 0)
				return getAllSynset(word, true, pos);
			return syns;
		} catch (Exception e) {
			logger.warn("caught exception in getAllSynset(): {}", e.getMessage());
		}
		return new ArrayList<ISynset>();
	}

	// if word starts with cap, only let those synsets through that also start
	// with cap.
	public ArrayList<ISynset> getAllSynset(String word, boolean flag, POS pos_) {
		ArrayList<ISynset> syns = new ArrayList<ISynset>();
		List<String> stems = getStemsList(word, pos_);
		for (String s : stems) {
			// System.out.println(s);
			for (POS pos : POS.values()) {
				if (pos_ != null && !pos.equals(pos_))
					continue;
				// System.out.println(pos);
				IIndexWord idxWord = dict.getIndexWord(s, pos);
				if (idxWord == null)
					continue;
				List<IWordID> senses = idxWord.getWordIDs(); // 1 st meaning
				// HashMap<IWordID,Double> probs = getProbs(senses);
				for (IWordID iw : senses) {
					// if(probs.get(iw) > pTHRESH) {
					// System.out.println(probs.get(iw));
					ISynset is = dict.getSynset(iw.getSynsetID());
					List<IWord> words = is.getWords();
					if (isAcceptable(words, word) || flag) {
						// System.out.println(is);
						// if(true) {
						syns.add(is);
						// for(IWord iword: words) {
						// // System.out.println(iword);
						// }
					}
				}
			}
		}

		return syns;
	}

	// only acceptable if word is not capitalized or is capitalized and a
	// capitaled word in synset.
	private boolean isAcceptable(List<IWord> words, String word) {
		if (containsCapital(word)) {
			for (IWord iword : words) {
				if (containsCapital(iword.getLemma()))
					return true;
			}
			return false;
		}
		return true;
	}

	private boolean containsCapital(String word) {

		for (int i = 0; i < word.length(); i++) {
			if (Character.isUpperCase(word.charAt(i))) {
				return true;
			}
		}
		return false;
	}

	private HashMap<IWordID, Double> getProbs(List<IWordID> senses) {
		HashMap<IWordID, Double> probs = new HashMap<IWordID, Double>();
		double total = 0;
		for (IWordID iw : senses) {
			total += dict.getSenseEntry(dict.getWord(iw).getSenseKey()).getTagCount();
		}
		for (IWordID iw : senses) {
			double val = dict.getSenseEntry(dict.getWord(iw).getSenseKey()).getTagCount() / total;
			if (Double.isNaN(val)) {
				val = 0.;
			}
			probs.put(iw, val);
		}

		return probs;
	}

	public void getAllSemanticRelations(ISynset s) {
		Map<IPointer, List<ISynsetID>> map = s.getRelatedMap();
		Set<Entry<IPointer, List<ISynsetID>>> x = map.entrySet();
		for (Entry<IPointer, List<ISynsetID>> y : x) {
			System.out.println(y.getKey());
			List<ISynsetID> lis = y.getValue();
			for (ISynsetID id : lis) {
				ISynset syn = dict.getSynset(id);
				System.out.println("\t" + syn);
			}

		}
	}

	public void getAllLexicalRelations(String lemma) {
		// for each stem, for each sense in stemn\,print lemma, sensem,
		// probability, sr and lr
		List<String> stems = wstem.findStems(lemma, null);
		for (String s : stems) {
			for (POS pos : POS.values()) {
				IIndexWord idxWord = dict.getIndexWord(lemma, pos);
				if (idxWord == null)
					continue;
				List<IWordID> senses = idxWord.getWordIDs(); // 1 st meaning
				for (IWordID iw : senses) {
					ISynset is = dict.getSynset(iw.getSynsetID());
					System.out.println(is.getGloss());
					getAllLexicalRelations(dict.getWord(iw));
				}
			}
		}
	}

	public ArrayList<IWord> getIWords(String st) {
		return getIWords(st, null);
	}

	public ArrayList<IWord> getIWords(String st, POS pos_) {
		List<String> stems = getStemsList(st, pos_);
		ArrayList<IWord> iwds = new ArrayList<IWord>();
		for (String s : stems) {
			// System.out.println(s);
			for (POS pos : POS.values()) {
				if (pos_ != null && !pos.equals(pos_))
					continue;
				IIndexWord idxWord = dict.getIndexWord(s, pos);
				// System.out.println(idxWord);
				if (idxWord == null)
					continue;
				List<IWordID> senses = idxWord.getWordIDs(); // 1 st meaning
				for (IWordID iw : senses) {
					// System.out.println(iw);
					iwds.add(dict.getWord(iw));
				}
			}
		}

		return iwds;
	}

	public void getAllLexicalRelations(IWord s) {
		Map<IPointer, List<IWordID>> map = s.getRelatedMap();
		Set<Entry<IPointer, List<IWordID>>> x = map.entrySet();
		for (Entry<IPointer, List<IWordID>> y : x) {
			System.out.println(y.getKey());
			List<IWordID> lis = y.getValue();
			for (IWordID id : lis) {
				IWord syn = dict.getWord(id);
				System.out.println("\t" + syn);
			}

		}
	}

	public void recurseSynset(ISynset s) {
		System.out.println(s);
		List<ISynsetID> lis = s.getRelatedSynsets(Pointer.HYPERNYM);
		for (ISynsetID id : lis) {
			recurseSynset(dict.getSynset(id));
			dict.getSynset(id);
		}
	}
}
