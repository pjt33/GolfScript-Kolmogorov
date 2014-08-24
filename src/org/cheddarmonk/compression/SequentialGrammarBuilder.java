package org.cheddarmonk.compression;

import java.util.*;

public class SequentialGrammarBuilder extends AbstractGrammarBuilder implements GrammarBuilder
{
	@Override
	public NonTerminal build(String str) {
		// NB This is not an efficient implementation.

		int off = 0, len = str.length();
		NonTerminal processed = new NonTerminal();
		Map<String, NonTerminal> prefixes = new HashMap<String, NonTerminal>();
		while (off < len) {
			// Find the longest known prefix.
			int end = len;
			CharSequence prefixRule = null;
			while (end > off + 1 && prefixRule == null) {
				prefixRule = prefixes.get(str.substring(off, end--));
			}
			if (prefixRule == null) prefixRule = str.substring(off, off + 1);
			off += prefixRule.length();

			// Special case
			if (prefixRule == processed) {
				processed = new NonTerminal(processed, processed);
				prefixes.put(processed.toString(), processed);
				continue;
			}

			// Append to the root rule.
			List<CharSequence> newPrimaryProduction = new ArrayList<CharSequence>();
			for (CharSequence cs : processed.production()) newPrimaryProduction.add(cs);
			newPrimaryProduction.add(prefixRule);
			processed = new NonTerminal(newPrimaryProduction);

			// If the last two elements of the root rule are used elsewhere, substitute a new rule.
			if (newPrimaryProduction.size() > 1) {
				CharSequence[] candidateRule = new CharSequence[2];
				candidateRule[0] = newPrimaryProduction.get(newPrimaryProduction.size() - 2);
				candidateRule[1] = newPrimaryProduction.get(newPrimaryProduction.size() - 1);
				if (processed.countUsageWithoutOverlap(candidateRule) > 1) {
					processed = processed.substitute(new NonTerminal(candidateRule));

					// This could eliminate duplication of either of the elements of candidateRule if they're non-terminals.
					for (int i = 0; i < 2; i++) {
						if (candidateRule[i] instanceof NonTerminal && processed.countUsageWithoutOverlap(new CharSequence[]{candidateRule[i]}) == 1) {
							processed = processed.expand((NonTerminal)candidateRule[i]);
						}
					}

					// KISS
					prefixes.clear();
					for (NonTerminal nt : processed.reachableNonTerminals()) {
						if (nt == processed) continue;
						prefixes.put(nt.toString(), nt);
					}
				}
			}
		}

		return processed;
	}
}
