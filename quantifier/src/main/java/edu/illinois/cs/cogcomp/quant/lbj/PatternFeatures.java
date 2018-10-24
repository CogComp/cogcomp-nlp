/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000DE6515F6BD83C0EFB264073C5CB6B10E0B7B57580346DE1C0C25C0840D67B6A8241B9E4438D25029CDC9B35FFBF8494E8D9B3067DB7851B7834A599229AF8F192AC2712C4EA1C2CBA4E317B614BCB00E6B1D066C3552D8516B109639D92D54A9CBB394EB5237BA58C5728A0129759C9523A13B7B3A4E8291D2795C11AC6CA39583D583BB94DBED59243A2D04EA429650ADA45D86BB14B4983A11F71D80F081A63E04CA738F09286269451050AA8E0677051D23242B0E55E1E2A88091422858E7D70C78E3640113E0C0DD01D5A510D1EB7901E957222CB7180D380E1794C8C390BDB5C347F68D432B0D0587465D81E294CA545590544465D949E8E4BBA279A97E61597F508455F0B8DB1E559E078708FFB2AFFE5153D79477A506F9BCBC6AE5186AA8252F8554F14D1AF8F53456AE3F3C4149FA887E8DA3F1DF114BAD7ADBF2D9B24D32E8A4248216BE3FB63411A5FB01130DD5304CD08E2D8D721D5A010D3C2AE6F301E09C6752EE674ABD3A849BDE257FB4D35B63CA90F800CD7D4A01B616D8F6F2A34C3C604C0B7F786EDFFD0CD0BE3FF4FAFB9DB20A7FB09DF6FDF9C79B3F34FE124A0B22CBF4CB569BC94F64EBA596870F0DED8CE69F9C3EDD11AFC0268655379ED766C8E787256A3373DF0EE30B9AFF5A8A9B99ABB9F8EE32BB1FF32D4673E2BB2775C2BC89BC2BC2D102AC6A60D227F898E9892D7BF3E8FDC3971F8966F6E92D7DEC75CCBE62E546304824596771F8DEC073471D0816EEA1A0C9B4039237B8D43A15571A58B93EB87503DCCDCB1940850F6DD16A853533748BFAC52E8D07DD22A490B2DCAF96DC53E42FD0A8C7B42A8A2561E8C4D80AF5B190C07CAA54B73B6A576C2AFA18DA52410A3A183759B5C99A0319AE16062F06504EDBA254AAC60993F6FA1FE80319D626515A3655DA4B65B37D281C660251A20B527DE87947BF82AEA7C6164DC02FEDA2364CAA28FC0C5740C1632AF9A97D45EC5E2F77670A466E0634CA8A160D2A4CA1D70D7151F63561661BD73C47C7BC7B4DD8535E7E86253E8E3FFDED5471A053663EF376B077E3F58BF48FF3D9F28C881C9973F5BBFD8864CB4639D6C7E957A994EAAEDA8A06C1E81D1FE5F3D7F3F3FBFCE27E7E793FC619D5D97A7A9CC8BDC738749AE8FC3BE0E6215336ABFA72B60B3F6479C378B05ABE17A8B25499C83AF44F538A7CB5DD08430874F244FBB1528B36AB183D4E9E70453E9F8F127DF90C1368DF568469246178C01F347C2712DCDFF73AA70753DB475536072AC81DF99C8F0FBF4E5A92F164F7625E8F1EAF2DC5368D76294FF2AF796A8E38E101C7AFE01C5FA182E1C00000

package edu.illinois.cs.cogcomp.quant.lbj;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.lbjava.classify.*;
import edu.illinois.cs.cogcomp.lbjava.infer.*;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.lbjava.learn.*;
import edu.illinois.cs.cogcomp.lbjava.parse.*;
import edu.illinois.cs.cogcomp.quant.features.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PatternFeatures extends Classifier {
    public PatternFeatures() {
        containingPackage = "edu.illinois.cs.cogcomp.quant.lbj";
        name = "PatternFeatures";
    }

    public String getInputType() {
        return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent";
    }

    public String getOutputType() {
        return "discrete%";
    }

    public FeatureVector classify(Object __example) {
        if (!(__example instanceof Constituent)) {
            String type = __example == null ? "null" : __example.getClass().getName();
            System.err
                    .println("Classifier 'PatternFeatures(Constituent)' defined on line 11 of chunk.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        Constituent word = (Constituent) __example;

        FeatureVector __result;
        __result = new FeatureVector();
        String __id;
        String __value;

        String ordinal =
                "(?:" + "\\d+(?:st|nd|rd|th)"
                        + "|first|second|third|fourth|fifth|sixth|seventh|eighth|ninth|tenth"
                        + "|eleventh|twelfth|thirteenth|fourteenth|fifteenth|sixteenth"
                        + "|seventeenth|eighteenth|nineteenth"
                        + "|twentieth|thirtieth|fou?rtieth|fiftieth|sixtieth|seventieth"
                        + "|eightieth|ninetieth" + "|hundredth|thousandth|millionth|billionth)";
        String fraction_denom =
                "(?:" + "half|halve|third|fourth|fifth|sixth|seventh|eighth|ninth|tenth"
                        + "|eleventh|twelfth|thirteenth|fourteenth|fifteenth|sixteenth"
                        + "|seventeenth|eighteenth|nineteenth"
                        + "|twentieth|thirtieth|fou?rtieth|fiftieth|sixtieth|seventieth"
                        + "|eightieth|ninetieth" + "|hundredth|thousandth|millionth|billionth)s";
        String writtenNumber =
                "twelve|seven|trillion|ten|seventeen|two|four|sixty|"
                        + "zero|eighteen|thirteen|dozen|one|fourty|fifty|twenty"
                        + "six|three|eleven|hundred|thousand|million|eighty"
                        + "fourteen|five|nineteen|sixteen|fifteen|seventy|billion"
                        + "thirty|ninety|nine|eight";
        String digits = "(\\d+)";
        String four_digits = "(\\d\\d\\d\\d)";
        String two_digits = "(\\d\\d)";
        String two_letter = "[A-Z][A-Z]";
        String initial = "[A-Z]\\.";
        String abbrev = "([A-Z]?[a-z]+\\.)";
        String roman = "(M?M?M?(?:CM|CD|D?C?C?C?)(?:XC|XL|L?X?X?X?)(?:IX|IV|V?II?|III))";
        String numeric = "((?:\\d{1,3}(?:\\,\\d{3})*|\\d+)(?:\\.\\d+)?)";
        String doftw = "(?:Mon|Tues?|Wed(?:nes)?|Thurs?|Fri|Satui?r?|Sun)(?:day|\\.)";
        String month =
                "(?:jan(?:uary)?|febr?(?:uary)?|mar(?:ch)?|apr(?:il)?"
                        + "|may|june?|july?|aug(?:ust)?|sept?(?:ember)?|oct(?:ober)?|nov(?:ember)?|"
                        + "dec(?:ember)?)\\.?";
        String dayWords = "(?: today|tomorrow|yesterday|morning|afternoon|evening)";
        String possibleYear = "(?:\\d\\d\\d\\d(?:\\s*s)?|\\'?\\d\\d(?:\\s*?s)?)";
        String time =
                "(\\d\\d?)\\s*?(?:(\\:)?\\s*?(\\d\\d))?\\s*([ap]\\.m\\.?|[ap]m|[a"
                        + "p])?\\s*(?:\\(?(GMT|EST|PST|CST)?\\)?)?(?:\\W|$)";
        Pattern pattern = Pattern.compile(digits, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(word.getSurfaceForm());
        if (matcher.matches()) {
            __id = "[digits]";
            __value = "true";
            __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage,
                    this.name, __id, __value, valueIndexOf(__value), (short) 0));
        }
        pattern = Pattern.compile(numeric, Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(word.getSurfaceForm());
        if (matcher.matches()) {
            __id = "[numeric]";
            __value = "true";
            __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage,
                    this.name, __id, __value, valueIndexOf(__value), (short) 0));
        }
        pattern = Pattern.compile(numeric, Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(word.getSurfaceForm());
        if (matcher.find()) {
            __id = "[contains_numeric]";
            __value = "true";
            __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage,
                    this.name, __id, __value, valueIndexOf(__value), (short) 0));
        }
        pattern = Pattern.compile(writtenNumber, Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(word.getSurfaceForm());
        if (matcher.matches()) {
            __id = "[written_number]";
            __value = "true";
            __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage,
                    this.name, __id, __value, valueIndexOf(__value), (short) 0));
        }
        pattern = Pattern.compile(fraction_denom, Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(word.getSurfaceForm());
        if (matcher.matches()) {
            __id = "[fraction_denom]";
            __value = "true";
            __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage,
                    this.name, __id, __value, valueIndexOf(__value), (short) 0));
        }
        pattern = Pattern.compile(ordinal, Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(word.getSurfaceForm());
        if (matcher.matches()) {
            __id = "[ordinal]";
            __value = "true";
            __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage,
                    this.name, __id, __value, valueIndexOf(__value), (short) 0));
        }
        pattern = Pattern.compile(month, Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(word.getSurfaceForm());
        if (matcher.matches()) {
            __id = "[month]";
            __value = "true";
            __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage,
                    this.name, __id, __value, valueIndexOf(__value), (short) 0));
        }
        return __result;
    }

    public FeatureVector[] classify(Object[] examples) {
        if (!(examples instanceof Constituent[])) {
            String type = examples == null ? "null" : examples.getClass().getName();
            System.err
                    .println("Classifier 'PatternFeatures(Constituent)' defined on line 11 of chunk.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        return super.classify(examples);
    }

    public int hashCode() {
        return "PatternFeatures".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof PatternFeatures;
    }
}
