package org.cheddarmonk.compression;

import java.util.*;
import org.cheddarmonk.util.Function;

public class LZBuilder implements Function<String, List<LZBuilder.LZToken>>
{
	// These values are somewhat dependent on the engine used...
	private final int minLen = 3;
	private final int maxLen = 256;
	private final int maxOff = 128;

	public List<LZToken> eval(String str) {
		List<LZToken> toks = new ArrayList<LZToken>();
		for (int i = 0; i < str.length(); ) {
			// Find the longest prefix of str[i..] which exists (at a suitable offset)
			int bestLen = 1;
			for (int len = minLen; i + len <= str.length() && len < maxLen; len++) {
				int off = str.indexOf(str.substring(i, i + len), i - maxOff);
				if (off >= 0 && off < i - 1) bestLen = len;
				else break;
			}
			if (bestLen == 1) toks.add(new LZToken(str.charAt(i)));
			else {
				int off = str.indexOf(str.substring(i, i + bestLen), i - maxOff);
				toks.add(new LZToken(i - off - 1, bestLen));
			}

			i += bestLen;
		}

		return toks;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(maxOff=" + maxOff + ",len=" + minLen + ".." + maxLen + ")";
	}

	public static class LZToken {
		private final char ch;
		// The offset counting the previous character as 0, the one before that as 1, etc.
		private final int off;
		private final int len;

		LZToken(char ch) {
			this.ch = ch;
			this.off = 0;
			this.len = 0;
		}

		LZToken(int off, int len) {
			this.ch = '\u0000';
			this.off = off;
			this.len = len;
		}

		@Override
		public String toString() {
			return len == 0 ? Character.toString(ch) : ("<" + off + "," + len + ">");
		}

		public boolean isChar() {
			return len == 0;
		}

		public char toChar() {
			if (len > 0) throw new UnsupportedOperationException();
			return ch;
		}

		public int off() {
			if (len == 0) throw new UnsupportedOperationException();
			return off;
		}

		public int len() {
			if (len == 0) throw new UnsupportedOperationException();
			return len;
		}
	}
}
