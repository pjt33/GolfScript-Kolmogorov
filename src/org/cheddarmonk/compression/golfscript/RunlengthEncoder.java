package org.cheddarmonk.compression.golfscript;

import org.cheddarmonk.util.Function;

public class RunlengthEncoder implements Function<String, String> {
	@Override
	public String eval(String t) {
		char prevCh = '\ufeff';
		char prevLength = 0;
		StringBuilder lengths = new StringBuilder();
		StringBuilder chars = new StringBuilder();
		for (int i = 0; i < t.length(); i++) {
			char ch = t.charAt(i);
			if (ch == prevCh && prevLength < 255) {
				prevLength++;
				lengths.setCharAt(lengths.length() - 1, prevLength);
			}
			else {
				prevCh = ch;
				prevLength = 1;
				chars.append(prevCh);
				lengths.append(prevLength);
			}
		}

		return "[" + GolfScriptEngine.basify(lengths.toString()) + GolfScriptEngine.basify(chars.toString()) + "]zip{(*}%+";
	}

	@Override
	public String toString() {
		return "Run-length encoder";
	}
}
