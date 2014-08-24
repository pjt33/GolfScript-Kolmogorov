package org.cheddarmonk.compression.golfscript;

import java.util.*;
import org.cheddarmonk.compression.GrammarBuilder.NonTerminal;

public class OffsetEngine extends AbstractGrammarEngine {
	@Override
	public String generate(NonTerminal grammar) {
		List<NonTerminal> rules = grammar.topologicalSort();
		int offset = rules.size();
		int maxPos = offset;
		{
			String expansion = grammar.toString();
			for (char ch : expansion.toCharArray()) {
				if (ch + offset > maxPos) maxPos = ch + offset;
			}
		}

		Map<NonTerminal, Integer> index = new HashMap<NonTerminal, Integer>();
		for (int i = 0; i < offset; i++) index.put(rules.get(i), i);

		StringBuilder rulesNullTerminated = new StringBuilder();
		for (NonTerminal rule : rules) {
			if (rulesNullTerminated.length() > 0) rulesNullTerminated.append('\u0000');
			for (CharSequence cs : rule.production()) {
				if (cs instanceof NonTerminal) rulesNullTerminated.append((char)index.get(cs).intValue());
				else {
					for (char ch : cs.toString().toCharArray()) {
						rulesNullTerminated.append((char)(ch + offset));
					}
				}
			}
		}

		// Consider a couple of alternatives

		// Route 1: direct string encoding (only valid if maxPos <= 255)
		// 'rules''^0'/( # [rules] starting-string
		// 1$,  # === offset-1
		// ,{ # [rules] string n
		//     \[1$)]''+ # [rules] n string 'n+1'
		//     /\2$=*
		// }/
		// \,){-}+%
		String direct = maxPos >= 256 ? "" : escape(rulesNullTerminated.toString()) + "'\u0000'/(" + (offset-1) + ",{\\[1$)]''+/\\2$=*}/\\,){-}+%";

		// Route 2: same but with base encoding
		String basified = basify(rulesNullTerminated.toString()) +    "[0]/(" + (offset-1) + ",{\\[1$)]/\\2$=*}/\\,){-}+%+";

		return maxPos < 256 && direct.length() < basified.length() ? direct : basified;
	}
}
