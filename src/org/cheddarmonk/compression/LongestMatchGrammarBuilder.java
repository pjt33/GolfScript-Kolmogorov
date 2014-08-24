package org.cheddarmonk.compression;

import java.util.Set;

public class LongestMatchGrammarBuilder extends GlobalGrammarBuilder
{
	@Override
	protected Subsequence selectMaximalString(Set<Subsequence> maximalStrings) {
		int length = 0;
		Subsequence longest = null;
		// TODO Tiebreakers?
		for (Subsequence ss : maximalStrings) {
			if (ss.size() > length) {
				length = ss.size();
				longest = ss;
			}
		}
		return longest;
	}
}
