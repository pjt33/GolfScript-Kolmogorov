package org.cheddarmonk.compression;

import java.util.Set;

public class RePairGrammarBuilder extends GlobalGrammarBuilder
{
	@Override
	protected Subsequence selectMaximalString(Set<Subsequence> maximalStrings) {
		int count = 0;
		Subsequence mostFrequent = null;
		// TODO Tiebreakers?
		for (Subsequence ss : maximalStrings) {
			if (ss.count() > count) {
				count = ss.count();
				mostFrequent = ss;
			}
		}
		return mostFrequent;
	}
}
