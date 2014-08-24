package org.cheddarmonk.compression.golfscript;

import java.util.List;
import org.cheddarmonk.compression.LZBuilder.*;
import org.cheddarmonk.util.Function;

public class LZBasified implements Function<List<LZToken>, String> {
	@Override
	public String eval(List<LZToken> t) {
		int min = 256, max = -1;
		for (LZToken tok : t) {
			if (tok.isChar()) {
				char ch = tok.toChar();
				if (ch < min) min = ch;
				if (ch > max) max = ch;
			}
		}

		// We offset literal characters by min, so max goes to max-min and we can use max-min+1 as the starting point for offsets.
		// The aim is to play nicely with basify.
		StringBuilder sb = new StringBuilder();
		for (LZToken tok : t) {
			if (tok.isChar()) {
				char ch = tok.toChar();
				sb.append((char)(ch - min));
			}
			else {
				if (tok.off() == 0) throw new IllegalArgumentException("TODO Think about how to support offsets of 0");
				sb.append((char)(tok.off() + max - min));
				sb.append((char)tok.len());
			}
		}

		return ":k." + GolfScriptEngine.basify(sb.toString()) + "+{k{{k$}*0:k;}{" + (max-min) + ".2$<{-:k}*;}if}/](+{" + min + "+}%";
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
