package org.cheddarmonk.compression;

import java.util.*;

public abstract class GlobalGrammarBuilder extends AbstractGrammarBuilder implements GrammarBuilder
{
	@Override
	public NonTerminal build(String str) {
		CharSequence[] chs = new CharSequence[str.length()];
		for (int i = 0; i < str.length(); i++) chs[i] = str.subSequence(i, i + 1);
		NonTerminal grammar = new NonTerminal(chs);

		while (true) {
			Set<Subsequence> maximalStrings = findMaximalStrings(grammar);
			if (maximalStrings.isEmpty()) break;

			Subsequence selected = selectMaximalString(maximalStrings);
			if (selected == null) break; // Allow to abort

			NonTerminal replacement = selected.createReplacement();
			grammar = grammar.substitute(replacement);
		}

		return grammar;
	}

	private Set<Subsequence> findMaximalStrings(NonTerminal grammar) {
		List<Map<String, Subsequence>> levels = new ArrayList<Map<String, Subsequence>>();

		// First level: pairs.
		Map<String, Subsequence> l0 = new HashMap<String, Subsequence>();
		for (NonTerminal nt : grammar.reachableNonTerminals()) {
			List<CharSequence> prod = nt.production();
			for (int i = 0; i < prod.size() - 1; i++) {
				String str = prod.get(i).toString() + prod.get(i + 1);
				Subsequence ss = l0.get(str);
				if (ss == null) {
					ss = new Subsequence(2);
					l0.put(str, ss);
				}
				ss.add(nt, i);
			}
		}
		for (Iterator<Map.Entry<String, Subsequence>> it = l0.entrySet().iterator(); it.hasNext(); ) {
			if (it.next().getValue().count() < 2) it.remove();
		}
		levels.add(l0);

		// Subsequence levels: extend previous level
		do
		{
			Map<String, Subsequence> lp = levels.get(levels.size() - 1);
			Map<String, Subsequence> ln = new HashMap<String, Subsequence>();
			for (Map.Entry<String, Subsequence> e : lp.entrySet()) {
				String prefix = e.getKey();
				Subsequence extending = e.getValue();
				for (Map.Entry<NonTerminal, Set<Integer>> pos : extending.startPositions.entrySet()) {
					NonTerminal nt = pos.getKey();
					List<CharSequence> prod = nt.production();
					for (Integer off : pos.getValue()) {
						if (off + extending.size() < prod.size()) {
							String str = prefix + prod.get(off + extending.size());
							Subsequence ss = ln.get(str);
							if (ss == null) {
								ss = new Subsequence(extending.size() + 1);
								ln.put(str, ss);
							}
							ss.add(nt, off);
						}
					}
				}
			}
			for (Iterator<Map.Entry<String, Subsequence>> it = ln.entrySet().iterator(); it.hasNext(); ) {
				if (it.next().getValue().count() < 2) it.remove();
			}
			levels.add(ln);
		} while (levels.get(levels.size() - 1).size() > 0);

		// The maximal strings are those for which "no strictly longer string appears at least as
		// many times on the right side without overlap".
		Set<Subsequence> maximal = new HashSet<Subsequence>();
		int requiredCount = 0;
		for (int i = levels.size() - 1; i >= 0; i--) {
			Collection<Subsequence> lvl = levels.get(i).values();
			boolean foundOne = false;
			for (Subsequence ss : lvl) {
				if (ss.count() >= requiredCount) {
					requiredCount = ss.count();
					foundOne = true;
				}
			}
			if (foundOne) {
				for (Subsequence ss : lvl) {
					if (ss.count() == requiredCount) maximal.add(ss);
				}
				// Shorter sequences must be more common.
				requiredCount++;
			}
		}
		return maximal;
	}

	protected abstract Subsequence selectMaximalString(Set<Subsequence> maximalStrings);

	protected static class Subsequence {
		private Map<NonTerminal, Set<Integer>> startPositions = new HashMap<NonTerminal, Set<Integer>>();
		private int size;
		private int count = 0;

		public Subsequence(int size) {
			this.size = size;
		}

		public int size() { return size; }
		public int count() { return count; }

		public void add(NonTerminal rule, int offset) {
			Set<Integer> relevantOffsets = startPositions.get(rule);
			if (relevantOffsets != null) {
				for (int i = offset - size + 1; i < offset; i++) {
					if (relevantOffsets.contains(i)) return;
				}
			}
			else {
				relevantOffsets = new HashSet<Integer>();
				startPositions.put(rule, relevantOffsets);
			}

			relevantOffsets.add(offset);
			count++;
		}

		public NonTerminal createReplacement() {
			for (Map.Entry<NonTerminal, Set<Integer>> e1 : startPositions.entrySet()) {
				for (Integer i : e1.getValue()) {
					return new NonTerminal(e1.getKey().production().subList(i, i + size));
				}
			}
			throw new IllegalStateException("Unreachable code");
		}

		@Override
		public String toString() {
			return "{" + createReplacement().toString() + ": len=" + size + ", count=" + count + "}";
		}
	}
}
