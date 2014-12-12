package edu.illinois.cs.cogcomp.nlp.curator;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;

import edu.illinois.cs.cogcomp.thrift.curator.MultiRecord;
import edu.illinois.cs.cogcomp.thrift.curator.Record;

/**
 * Static class to create identifiers.
 * 
 * @author James Clarke
 * 
 */
public class Identifier {

	private static final MessageDigest md;
	private static final String ALGORITHM = "SHA";
	static {
		try {
			md = MessageDigest.getInstance(ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Error cannot find digest algorithm "
					+ ALGORITHM, e);
		}
	}

	/**
	 * Converts a byte array into a hexidecimal string.
	 * 
	 * @param bArr
	 * @return
	 */
	private static String hexDigest(byte[] bArr) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < bArr.length; i++) {
			int unsigned = bArr[i] & 0xff;
			if (unsigned < 0x10)
				sb.append("0");
			sb.append(Integer.toHexString((unsigned)));
		}
		return sb.toString();
	}

	/**
	 * @param text
	 * @return
	 */
	private static synchronized byte[] performDigest(String text) {
		return md.digest(text.getBytes());
	}

	/**
	 * Create an identifer for a text.
	 * 
	 * @param text
	 * @return
	 */
	private static String getId(String text) {
		return hexDigest(performDigest(text));
	}
	
	public static String getId(String text, boolean flag) {
		String t2 = "FLAG:" + flag + ":" + text;
		return getId(t2);
	}

	public static String getId(Record record) {
		if (!record.isSetIdentifier()) {
			record.setIdentifier(getId(record.getRawText(), record.isWhitespaced()));
		}
		return record.getIdentifier();
	}

	public static String getId(MultiRecord record) {
//		if (!record.isSetIdentifier()) {
//			record
//					.setIdentifier(getId(StringUtil.join(record.getRecords(),
//							"")));
//		}
//		return record.getIdentifier();
		return null;
	}
	
	public static String getId(List<String> text) {
//		StringBuffer result = new StringBuffer();
//		for (String item : text)
//			result.append(getId(item));
//		return result.toString();
		return null;
	}

	public static String getId(TBase datum) throws Exception {
		if (datum instanceof Record) {
			return getId((Record) datum);
		} else if (datum instanceof MultiRecord) {
			return getId((MultiRecord) datum);
		} else {
			throw new Exception("Unexpected data type!");
		}
	}

}
