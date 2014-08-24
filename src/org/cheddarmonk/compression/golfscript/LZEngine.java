package org.cheddarmonk.compression.golfscript;

import java.util.List;
import org.cheddarmonk.compression.LZBuilder.*;
import org.cheddarmonk.util.Function;

public class LZEngine extends GolfScriptEngine implements Function<List<LZToken>, String> {
	// Encode pairs as a character 128+ followed by a count character
	// :k'<<string>>'{k{{k$}*0:k;}{127.2$<{-:k}*;}if}/](+
	@Override
	public String eval(List<LZToken> t) {
		StringBuilder sb = new StringBuilder();
		for (LZToken tok : t) {
			if (tok.isChar()) {
				char ch = tok.toChar();
				if (ch > 127) throw new IllegalArgumentException();
				sb.append(ch);
			}
			else {
				sb.append((char)(tok.off() + 127));
				sb.append((char)tok.len());
			}
		}

		String plain = ":k" + escape(sb.toString()) + "{k{{k$}*0:k;}{127.2$<{-:k}*;}if}/](+";
		// Might do better with some offsets.
		String down = charAdd(sb.toString(), -1);
		if (down != null) {
			String downFull = ":k" + escape(down) + "{)k{{k$}*0:k;}{127.2$<{-:k}*;}if}/](+";
			if (downFull.length() < plain.length()) plain = downFull;
		}
		// Might do better with basification for entropy-encoding
		String basified = ":k." + basify(sb.toString()) + "+{k{{k$}*0:k;}{127.2$<{-:k}*;}if}/](+";
		if (basified.length() < plain.length()) plain = basified;

		return plain;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
