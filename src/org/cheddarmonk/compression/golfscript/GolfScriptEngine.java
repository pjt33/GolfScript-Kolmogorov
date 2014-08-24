package org.cheddarmonk.compression.golfscript;

import java.math.BigInteger;

public class GolfScriptEngine {
	public static String escape(String str) {
		return "'" + str.replace("\\", "\\\\").replace("'", "\\'") + "'";
	}

	// Given a string generates a program to create an array holding that string's char values
	public static String basify(String str) {
		int N = 1;
		char[] chs = str.toCharArray();
		for (char ch : chs) {
			if (N <= ch) N = ch+1;
		}

		// No benefit is possible with base-256.
		if (N == 256) return "[" + escape(str) + "{}/]";

		// Interpret the string as a number in base-N, and base-convert to base 256
		BigInteger stringValue = BigInteger.ZERO;

		BigInteger _N = new BigInteger(Integer.toString(N));
		for (char ch : chs) stringValue = stringValue.multiply(_N).add(BigInteger.valueOf(ch));

		byte[] bytes = stringValue.toByteArray();
		int off = bytes[0] == 0 ? 1 : 0;
		try {
			return escape(new String(bytes, off, bytes.length - off, "ISO-8859-1")) + "256base " + N + "base";
		}
		catch (java.io.UnsupportedEncodingException ex) {
			// This should never happen in a conforming Java implementation
			throw new RuntimeException(ex);
		}
	}

	protected String charAdd(String str, int off)
	{
		char[] ch = str.toCharArray();
		for (int i = 0; i < ch.length; i++) {
			if (ch[i] + off < 0 || ch[i] + off > 255) return null;
			ch[i] += off;
		}
		return new String(ch);
	}
}
