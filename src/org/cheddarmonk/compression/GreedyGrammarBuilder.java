package org.cheddarmonk.compression;

import java.util.Set;

public class GreedyGrammarBuilder extends GlobalGrammarBuilder
{
	private final int threshold;

	public GreedyGrammarBuilder(int threshold) {
		this.threshold = threshold;
	}

	@Override
	protected Subsequence selectMaximalString(Set<Subsequence> maximalStrings) {
		// Strict conformance to the Apostolico and Lonardi definition requires us to count gain as count * size - size - count = (count - 1) * (size - 1) - 1
		// In practice we really want an offset as well to account for the extra cost of separating out the rule - call it one character for convenience.
		int saving = Integer.MIN_VALUE;
		Subsequence best = null;
		// TODO Tiebreakers?
		for (Subsequence ss : maximalStrings) {
			int benefit = (ss.count() - 1) * (ss.size() - 1) - 2;
			if (benefit > saving) {
				saving = benefit;
				best = ss;
			}
		}
		return saving >= threshold ? best : null;
	}

	@Override
	public String toString() {
		return super.toString() + "(threshold=" + threshold + ")";
	}
}
