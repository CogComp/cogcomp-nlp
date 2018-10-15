/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.comma.datastructures;

import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Property;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

public class CommaSRLConfigurator extends Configurator {

    // Whether to use gold-standard annotations for features
    public static final Property useGold = new Property("USE_GOLD", Configurator.FALSE);

    // Whether a comma can have more than one label or not
    public static final Property allowMultiLabelCommas = new Property("ALLOW_MULTI_LABEL_COMMAS",
            Configurator.FALSE);

    // Whether to include commas that do not have annotations while reading annotated data
    // #If false, discard them
    // #If true, label them with Other
    public static final Property includeNullLabelCommas = new Property("INCLUDE_NULL_LABEL_COMMAS",
            Configurator.FALSE);

    public static final Property getBayraktarAnnotationsDir = new Property(
            "getBayraktarAnnotationsDir", Configurator.FALSE);

    // Locations of Penn Treebank, PropBank and NomBank; used if USE_GOLD is true to get the gold
    // parses and SRLs
    public static final Property getPTBHDir = new Property("PTB_DIR",
            "/shared/corpora/corporaWeb/treebanks/eng/pennTreebank/treebank-3/parsed/mrg/wsj");
    public static final Property getPropbankDir = new Property("PROPBANK_DIR",
            "/shared/corpora/corporaWeb/treebanks/eng/propbank_1/data");
    public static final Property getNombankDir = new Property("NOMBANK_DIR",
            "/shared/corpora/corporaWeb/treebanks/eng/nombank");

    // Whether to use NER labels for lexical nodes
    public static final Property lexicaliseNER = new Property("LEX_NER", Configurator.TRUE);

    // Whether to use POS labels for lexical nodes
    public static final Property lexicalisePOS = new Property("LEX_POS", Configurator.FALSE);

    // location of annotations required for training CommaSRL
    public static final Property dataLocation = new Property("COMMASRL_DIR", "data");

    // Whether to use Curator to get the non-gold annotations (the alternative is the standalone NLP
    // pipeline)
    public static final Property useCurator = new Property("USE_CURATOR", Configurator.FALSE);

    // Useful for structured prediction
    // A comma structure is a sequence
    // Set to true if the sequence should be all the commas in the sentence
    // Set to false if the sequence should be only the commas that are siblings in the parse tree
    public static final Property isCommaStructureFullSentence = new Property(
            "IS_COMMA_STRUCTURE_FULL_SENTENCE", Configurator.FALSE);

    // Whether to read the data from data-store, or the local data folder
    public static final Property useDatastoreToReadData = new Property("READ_DATA_FROM_DATASTORE",
            Configurator.TRUE);

    @Override
    public ResourceManager getDefaultConfig() {
        Property[] props =
                {useGold, allowMultiLabelCommas, includeNullLabelCommas,
                        isCommaStructureFullSentence, getBayraktarAnnotationsDir, getPTBHDir,
                        getPropbankDir, getNombankDir, lexicaliseNER, lexicalisePOS, useCurator,
                        dataLocation, useDatastoreToReadData};
        return new ResourceManager(generateProperties(props));
    }
}
