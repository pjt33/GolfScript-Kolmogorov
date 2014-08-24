package org.cheddarmonk.compression;

import java.util.*;
import org.cheddarmonk.util.Pair;

public class BisectionGrammarBuilder extends AbstractGrammarBuilder implements GrammarBuilder {
	@Override
	public NonTerminal build(String str) {
		// Build from the bottom up.
		List<CharSequence> tokens = new ArrayList<CharSequence>();
		for (int i = 0; i < str.length(); i++) tokens.add(str.substring(i, i+1));

		Map<Pair<CharSequence, CharSequence>, NonTerminal> existing = new HashMap<Pair<CharSequence, CharSequence>, NonTerminal>();

		while (tokens.size() > 1) {
			List<CharSequence> newTokens = new ArrayList<CharSequence>();
			Iterator<CharSequence> it = tokens.iterator();
			while (it.hasNext()) {
				CharSequence cs1 = it.next();
				if (it.hasNext()) {
					CharSequence cs2 = it.next();
					Pair<CharSequence, CharSequence> p = new Pair<CharSequence, CharSequence>(cs1, cs2);
					NonTerminal nt = existing.get(p);
					if (nt == null) {
						nt = new NonTerminal(cs1, cs2);
						existing.put(p, nt);
					}
					cs1 = nt;
				}
				newTokens.add(cs1);
			}
			tokens = newTokens;
		}

		if (tokens.isEmpty()) return new NonTerminal();

		CharSequence cs = tokens.get(0);
		return cs instanceof NonTerminal ? ((NonTerminal)cs) : new NonTerminal(cs);
	}
}
