package org.cheddarmonk.compression;

import java.util.*;
import org.cheddarmonk.util.Function;

public interface GrammarBuilder extends Function<String, GrammarBuilder.NonTerminal>
{
	public NonTerminal build(String str);

	// Note: by being immutable, we avoid cycles.
	public static class NonTerminal implements CharSequence
	{
		private final CharSequence[] production;

		public NonTerminal(CharSequence... production) {
			this.production = production.clone();

			for (int i = 0; i < production.length; i++) {
				if (production[i].length() == 0) throw new IllegalArgumentException();
			}
		}

		public NonTerminal(List<? extends CharSequence> production) {
			this(production.toArray(new CharSequence[0]));
		}

		@Override
		public char charAt(int idx) {
			if (idx < 0) throw new IllegalArgumentException("idx = " + idx + " < 0");
			for (CharSequence cs : production) {
				int len = cs.length();
				if (idx < len) return cs.charAt(idx);
				idx -= len;
			}

			throw new IllegalArgumentException("idx = " + idx + " > length = " + length());
		}

		private int cachedLength = -1;
		@Override
		public int length() {
			if (cachedLength < 0) {
				int length = 0;
				for (CharSequence cs : production) length += cs.length();
				cachedLength = length;
			}
			return cachedLength;
		}

		@Override
		public CharSequence subSequence(int off, int len) {
			// TODO Generate a new non-terminal?
			return toString().subSequence(off, len);
		}

		public List<CharSequence> production() {
			return Collections.unmodifiableList(Arrays.asList(production));
		}

		public Set<NonTerminal> reachableNonTerminals() {
			return addReachableNonTerminals(new HashSet<NonTerminal>());
		}

		private Set<NonTerminal> addReachableNonTerminals(Set<NonTerminal> s) {
			if (s.add(this)) {
				for (CharSequence cs : production) {
					if (cs instanceof NonTerminal) ((NonTerminal)cs).addReachableNonTerminals(s);
				}
			}
			return s;
		}

		public List<NonTerminal> topologicalSort() {
			LinkedList<NonTerminal> sorted = new LinkedList<NonTerminal>();
			topologicalSort(this, sorted, new HashSet<NonTerminal>());
			return sorted;
		}

		private void topologicalSort(NonTerminal nt, LinkedList<NonTerminal> sorted, Set<NonTerminal> seen) {
			seen.add(nt);
			for (CharSequence cs : nt.production()) {
				if ((cs instanceof NonTerminal) && !seen.contains(cs)) {
					topologicalSort((NonTerminal)cs, sorted, seen);
				}
			}
			sorted.addFirst(nt);
		}

		private int indexOf(CharSequence[] subsequence, int off) {
			// Inefficient.
			for (int i = off; i <= production.length - subsequence.length; i++) {
				for (int j = 0; j < subsequence.length; j++) {
					if (!production[i+j].equals(subsequence[j])) break;
					if (j == subsequence.length - 1) return i;
				}
			}
			return -1;
		}

		public int countUsageWithoutOverlap(CharSequence[] subsequence) {
			int count = 0;
			for (NonTerminal nt : reachableNonTerminals()) {
				for (int off = 0; off < nt.production.length; ) {
					int idx = nt.indexOf(subsequence, off);
					if (idx < 0) break;
					count++;
					off = idx + subsequence.length;
				}
			}
			return count;
		}

		public NonTerminal substitute(NonTerminal rule) {
			return substitute(rule, new HashMap<NonTerminal, NonTerminal>());
		}

		private NonTerminal substitute(NonTerminal rule, Map<NonTerminal, NonTerminal> substitutions) {
			NonTerminal selfSubst = substitutions.get(this);
			if (selfSubst != null) return selfSubst;

			boolean changed = false;
			List<CharSequence> prod = Arrays.asList(production);

			int idx = indexOf(rule.production, 0);
			if (idx >= 0) {
				changed = true;
				List<CharSequence> directSubst = new ArrayList<CharSequence>();
				int off = 0;
				do
				{
					// Copy and substitute.
					for (int i = off; i < idx; i++) directSubst.add(production[i]);
					directSubst.add(rule);
					off = idx + rule.production.length;
					idx = indexOf(rule.production, off);
				}
				while (idx > 0);
				for (int i = off; i < production.length; i++) directSubst.add(production[i]);
				prod = directSubst;
			}

			CharSequence[] subst = new CharSequence[prod.size()];
			for (int i = 0; i < subst.length; i++) {
				CharSequence cs = prod.get(i);
				if (cs instanceof NonTerminal && cs != rule) {
					NonTerminal substituted = ((NonTerminal)cs).substitute(rule, substitutions);
					changed |= cs != substituted;
					subst[i] = substituted;
				}
				else subst[i] = cs;
			}

			selfSubst = changed ? new NonTerminal(subst) : this;
			substitutions.put(this, selfSubst);
			return selfSubst;
		}

		public NonTerminal expand(NonTerminal rule) {
			return expand(rule, new HashMap<NonTerminal, NonTerminal>());
		}

		private NonTerminal expand(NonTerminal rule, Map<NonTerminal, NonTerminal> expansions) {
			NonTerminal selfExpansion = expansions.get(this);
			if (selfExpansion != null) return selfExpansion;

			List<CharSequence> expanded = new ArrayList<CharSequence>();
			boolean changed = false;
			for (CharSequence cs : production) {
				if (cs == rule) {
					changed = true;
					// No need to recurse because rules can't be circular.
					for (CharSequence inner : rule.production) expanded.add(inner);
				}
				else if (cs instanceof NonTerminal) {
					NonTerminal subexpand = ((NonTerminal)cs).expand(rule, expansions);
					changed |= cs != subexpand;
					expanded.add(subexpand);
				}
				else {
					expanded.add(cs);
				}
			}

			selfExpansion = changed ? new NonTerminal(expanded) : this;
			expansions.put(this, selfExpansion);
			return selfExpansion;
		}

		// For debugging
		public String toGrammarString() {
			Set<NonTerminal> nonTerminals = reachableNonTerminals();
			Map<NonTerminal, String> representation = new HashMap<NonTerminal, String>();
			List<NonTerminal> ordered = new ArrayList<NonTerminal>();

			representation.put(this, "<0>");
			ordered.add(this);

			for (NonTerminal nt : nonTerminals) {
				if (nt == this) continue;
				representation.put(nt, "<" + ordered.size() + ">");
				ordered.add(nt);
			}

			StringBuilder sb = new StringBuilder();
			for (NonTerminal nt : ordered) {
				sb.append(representation.get(nt)).append(" -> ");
				for (CharSequence cs : nt.production) {
					CharSequence renderAs = representation.get(cs);
					sb.append(renderAs == null ? cs : renderAs);
				}
				sb.append("\n");
			}
			return sb.toString();
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(length());
			appendTo(sb);
			return sb.toString();
		}

		private void appendTo(StringBuilder sb) {
			for (CharSequence cs : production) {
				if (cs instanceof NonTerminal) ((NonTerminal)cs).appendTo(sb);
				else sb.append(cs);
			}
		}
	}
}
