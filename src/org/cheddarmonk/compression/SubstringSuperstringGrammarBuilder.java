package org.cheddarmonk.compression;

import java.util.*;
import org.cheddarmonk.util.*;

// The first new algorithm ("a simple O(log^3 n) approximation algorithm") from
// The Smallest Grammar Problem, Charikar et al
public class SubstringSuperstringGrammarBuilder extends AbstractGrammarBuilder
{
	@Override
	public NonTerminal build(String str) {
		// We have a series of layers, each of which has
		// 1. A list of strings
		// 2. An approximation to the shortest superstring containing that set (not necessarily in order)
		// 3. A division of that superstring into sections such that each string from 1 consists of a series and a prefix
		List<? extends CharSequence> g = build(Collections.singletonList(str), str.length(), new HashMap<String, CharSequence>());
		return (NonTerminal)g.get(0);
	}

	private static List<? extends CharSequence> build(List<String> strings, int maxLen, Map<String, CharSequence> normalise) {
		if (strings.contains("")) throw new IllegalArgumentException();

		if (maxLen == 1)
		{
			for (String str : strings) normalise.put(str, str);
			return strings;
		}

		maxLen = (maxLen + 1) / 2;

		String superstring = findSuperstring(strings);
		Set<Integer> cutPoints = new HashSet<Integer>();
		cutPoints.add(superstring.length());
		for (String str : strings) cutPoints.add(superstring.indexOf(str));
		List<Integer> cutPointsSorted = new ArrayList<Integer>(cutPoints);
		Collections.sort(cutPointsSorted);

		for (int i = 0; i < cutPointsSorted.size() - 1; i++) {
			int cp0 = cutPointsSorted.get(i), cp1 = cutPointsSorted.get(i + 1);
			if (cp1 - cp0 > maxLen) cutPoints.add((cp1 + cp0) / 2);
		}
		cutPointsSorted = new ArrayList<Integer>(cutPoints);
		Collections.sort(cutPointsSorted);

		List<String> nextLayer = new ArrayList<String>();
		for (int i = 0; i < cutPointsSorted.size() - 1; i++) {
			int cp0 = cutPointsSorted.get(i), cp1 = cutPointsSorted.get(i + 1);
			nextLayer.add(superstring.substring(cp0, cp1));
		}

		List<? extends CharSequence> nextLayerGrammar = build(nextLayer, maxLen, normalise);

		List<CharSequence> grammar = new ArrayList<CharSequence>();
		for (String str : strings) {
			CharSequence existing = normalise.get(str);
			if (existing != null) {
				grammar.add(existing);
				continue;
			}

			// Build non-terminal for str.
			int idx = cutPointsSorted.indexOf(superstring.indexOf(str));
			// We're looking at a sequence of elements from nextLayerGrammar starting at idx and ending with a prefix of one.
			List<CharSequence> prod = new ArrayList<CharSequence>();
			int len = str.length();
			while (nextLayerGrammar.get(idx).length() < len) {
				prod.add(nextLayerGrammar.get(idx));
				len -= nextLayerGrammar.get(idx).length();
				idx++;
			}
			// We want a production for the len-char prefix of nextLayerGrammar.get(idx). This is built recursively.
			if (len > 0) appendPrefix(prod, nextLayerGrammar.get(idx), len);

			grammar.add(new NonTerminal(prod));
			normalise.put(str, grammar.get(grammar.size() - 1));
		}

		return grammar;
	}

	private static void appendPrefix(List<CharSequence> prod, CharSequence cs, int len) {
		if (len < 0) throw new IllegalArgumentException();
		if (len == 0) return;

		if (cs instanceof NonTerminal) {
			NonTerminal nt = (NonTerminal)cs;
			for (CharSequence rhs : nt.production()) {
				if (rhs.length() < len) {
					prod.add(rhs);
					len -= rhs.length();
				}
				else {
					appendPrefix(prod, rhs, len);
					return;
				}
			}
		}
		else {
			prod.add(cs.subSequence(0, len));
		}
	}

	private static String findSuperstring(Collection<String> strings) {
		if (strings.size() == 0) throw new IllegalArgumentException();

		// Blum, Jiang, Li, Tromp and Yanakakis' 4-approximation
		// Provably within a factor of 4 of the optimum: conjectured to be within a factor of 2.
		Set<String> foo = new HashSet<String>(strings);
		BinaryHeap<Pair<String, String>, Integer> overlaps = new BinaryHeap<Pair<String, String>, Integer>();
		for (String s1 : foo) {
			for (String s2 : foo) {
				if (s1.equals(s2)) continue;
				overlaps.insert(new Pair<String, String>(s1, s2), findOverlap(s1, s2).length() - s1.length() - s2.length());
			}
		}

		while (foo.size() > 1) {
			Pair<String, String> pair = overlaps.pop();
			if (!foo.contains(pair.first) || !foo.contains(pair.second)) continue;

			foo.remove(pair.first);
			foo.remove(pair.second);
			String overlapped = findOverlap(pair.first, pair.second);

			if (!pair.first.equals(overlapped) && !pair.second.equals(overlapped)) {
				for (String s : foo) {
					overlaps.insert(new Pair<String, String>(s, overlapped), findOverlap(s, overlapped).length() - s.length() - overlapped.length());
					overlaps.insert(new Pair<String, String>(overlapped, s), findOverlap(overlapped, s).length() - overlapped.length() - s.length());
				}
			}

			foo.add(overlapped);
		}

		return foo.iterator().next();
	}

	private static String findOverlap(String s1, String s2) {
		for (int i = s2.length(); i > 0; i--) {
			if (s1.endsWith(s2.substring(0, i))) return s1 + s2.substring(i);
		}
		return s1 + s2;
	}
}
