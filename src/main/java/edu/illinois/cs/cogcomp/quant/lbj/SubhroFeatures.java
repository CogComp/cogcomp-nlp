// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000DE6515F6BD6301EFB2281B1665398181AF6DC202A073D1404D1410B1946B838086BE463B3984384A2EA256EFBFEEE44952F6B7841B12F438212F8777CFEEBB3E1527EACDAD2878F13997DBADA53F114AFAD28B1F2CCF10A39DB1B97A9CF2769C7B46EEDA2DB94051A4BC239B8464361FE649C9423A5E23F3149D9F0A3F063F0E7B92BE341AC2A2D1CAD862DA24B516A6BE7B86920747AEB2D80F80A17605B9D2E4A51D2C39AA541823A38FD3494BD809C30BA81E2A8809DA440B0DFEF61F1AF81DA8817068E688EADB28E0F9524871D9880FC201A7B203E2981972163B581AB73C6B6D9B58C919D4DE4A621B25569AC011955721B77A3A3FEA4D5859B6F86978C14B9A6851FDAC2B8083C32CFF56D71695D5F57DDB55E1B0175575B20B4565AA00692932704F1ED845BAE394CC3C0795898631814F90CA934EE78AF58CDC31E86434B57C6839C6ADA443CB11130DDD204C358E2793462D5E1D2A7BB9AB34148342BD5D9BB369EE0A225E6ABCF97B235B6ADD3DE4D207F539C5D64977CD3647D978D58816F1E8DC78FB18B16D7EFEE5F71B7908577EA8FDDBF3BFDFE978ED34965E55BF6616BC27939ED8275B2B0F8C0DC641772FCE9EEF40D76013CA9A4A66F9990A70FABC47616AF12C70135E7254DCDE43CDE7A0F94CD2F34A9CE63467D1EA546998095695A3044D575065DA9131D3135AF6F3F9EBD7661F4965F6F93D7318B26CAB90B8260809B92CFE3EDDD91E18E2A60722CD04E8B6D0E251161BDAD2AAE3A5516E8F2F552CA803FA53106E2B907C92654D1D11EEB84D836D2D6388250CACA8E79594B8B85F61549B3215599A86FED4580AF5A6D0207C2B14B7DB1AD7EC3AFA38D972410ABA18B63B6F8B23DE24B97C18981C27857FAA419AA8146E2B9B1CFA58B8C631F682D1F6A236DA9D7860C163309A0518D29B9021B18DA634D5F8D2C8A914EDB33EC9A5590FB184B11078D88CB47F68AC9BC5EF42E8492CD1D1887551C0A549853AF0AF2A2ED5A2AD55C6FD6793EB39B3AE6CA82FB04B8A87C865A823613EF576B80793F548FC8FF3D9F28C881750B9F62CF04C0E3B4AEF7D9CE2EC71D9662B635DE45903E8DF67A703C4FDFCF2F12BBA9F5E5DC3B5467D799E972339E7DB5CB6455C9F2A3CB944DC89EBAD392C8DA0DB5519C83AE3ADDD83D41FB7EE04B30CBA6134EDF821C322B53C972FCFF290E7F31B249ECF8951EF460F25967F0FD74FE8E5DEFF139CF7CAE5C08443132EB095E1F78DF5386E174C712DC8F35FE538F5C05F2426CF21C7D0A5C18E884D3FF9F37396C813B00000

package edu.illinois.cs.cogcomp.quant.lbj;

import edu.illinois.cs.cogcomp.lbj.pos.POSTagger;
import edu.illinois.cs.cogcomp.lbj.pos.POSWindow;
import edu.illinois.cs.cogcomp.lbjava.classify.*;
import edu.illinois.cs.cogcomp.lbjava.infer.*;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.lbjava.learn.*;
import edu.illinois.cs.cogcomp.lbjava.nlp.*;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;
import edu.illinois.cs.cogcomp.lbjava.parse.*;
import java.util.*;
import java.util.regex.*;


public class SubhroFeatures extends Classifier
{
  public SubhroFeatures()
  {
    containingPackage = "edu.illinois.cs.cogcomp.quant.lbj";
    name = "SubhroFeatures";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Token))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'SubhroFeatures(Token)' defined on line 160 of chunk.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Token word = (Token) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    String ordinal = "(?:" + "\\d+(?:st|nd|rd|th)" + "|first|second|third|fourth|fifth|sixth|seventh|eighth|ninth|tenth" + "|eleventh|twelfth|thirteenth|fourteenth|fifteenth|sixteenth" + "|seventeenth|eighteenth|nineteenth" + "|twentieth|thirtieth|fou?rtieth|fiftieth|sixtieth|seventieth" + "|eightieth|ninetieth" + "|hundredth|thousandth|millionth|billionth" + ")";
    String fraction_denom = "(?:" + "half|halve|third|fourth|fifth|sixth|seventh|eighth|ninth|tenth" + "|eleventh|twelfth|thirteenth|fourteenth|fifteenth|sixteenth" + "|seventeenth|eighteenth|nineteenth" + "|twentieth|thirtieth|fou?rtieth|fiftieth|sixtieth|seventieth" + "|eightieth|ninetieth" + "|hundredth|thousandth|millionth|billionth" + ")s";
    String writtenNumber = "twelve|seven|trillion|ten|seventeen|two|four|sixty|" + "zero|eighteen|thirteen|dozen|one|fourty|fifty|twenty" + "six|three|eleven|hundred|thousand|million|eighty" + "fourteen|five|nineteen|sixteen|fifteen|seventy|billion" + "thirty|ninety|nine|eight";
    String digits = "(\\d+)";
    String four_digits = "(\\d\\d\\d\\d)";
    String two_digits = "(\\d\\d)";
    String two_letter = "[A-Z][A-Z]";
    String initial = "[A-Z]\\.";
    String abbrev = "([A-Z]?[a-z]+\\.)";
    String roman = "(M?M?M?(?:CM|CD|D?C?C?C?)(?:XC|XL|L?X?X?X?)(?:IX|IV|V?II?|III))";
    String numeric = "((?:\\d{1,3}(?:\\,\\d{3})*|\\d+)(?:\\.\\d+)?)";
    String doftw = "(?:Mon|Tues?|Wed(?:nes)?|Thurs?|Fri|Satui?r?|Sun)(?:day|\\.)";
    String month = "(?:jan(?:uary)?|febr?(?:uary)?|mar(?:ch)?|apr(?:il)?" + "|may|june?|july?|aug(?:ust)?|sept?(?:ember)?|oct(?:ober)?|nov(?:ember)?|" + "dec(?:ember)?)\\.?";
    String dayWords = "(?: today|tomorrow|yesterday|morning|afternoon|evening)";
    String possibleYear = "(?:\\d\\d\\d\\d(?:\\s*s)?|\\'?\\d\\d(?:\\s*?s)?)";
    String time = "(\\d\\d?)\\s*?(?:(\\:)?\\s*?(\\d\\d))?\\s*([ap]\\.m\\.?|[ap]m|[ap])?\\s*(?:\\(?(GMT|EST|PST|CST)?\\)?)?(?:\\W|$)";
    Pattern pattern = Pattern.compile(numeric, Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(word.form);
    if (matcher.matches())
    {
      __id = "[numeric]";
      __value = "true";
      __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
    }
    pattern = Pattern.compile(numeric, Pattern.CASE_INSENSITIVE);
    matcher = pattern.matcher(word.form);
    if (matcher.find())
    {
      __id = "[contains_numeric]";
      __value = "true";
      __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
    }
    pattern = Pattern.compile(writtenNumber, Pattern.CASE_INSENSITIVE);
    matcher = pattern.matcher(word.form);
    if (matcher.matches())
    {
      __id = "[written_number]";
      __value = "true";
      __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
    }
    pattern = Pattern.compile(fraction_denom, Pattern.CASE_INSENSITIVE);
    matcher = pattern.matcher(word.form);
    if (matcher.matches())
    {
      __id = "[fraction_denom]";
      __value = "true";
      __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
    }
    pattern = Pattern.compile(ordinal, Pattern.CASE_INSENSITIVE);
    matcher = pattern.matcher(word.form);
    if (matcher.matches())
    {
      __id = "[ordinal]";
      __value = "true";
      __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
    }
    pattern = Pattern.compile(month, Pattern.CASE_INSENSITIVE);
    matcher = pattern.matcher(word.form);
    if (matcher.matches())
    {
      __id = "[month]";
      __value = "true";
      __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
    }
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Token[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'SubhroFeatures(Token)' defined on line 160 of chunk.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "SubhroFeatures".hashCode(); }
  public boolean equals(Object o) { return o instanceof SubhroFeatures; }
}

