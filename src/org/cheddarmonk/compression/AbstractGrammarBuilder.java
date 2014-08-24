package org.cheddarmonk.compression;

public abstract class AbstractGrammarBuilder implements GrammarBuilder {
	@Override
	public NonTerminal eval(String t) {
		NonTerminal nt = build(t);
		System.out.println("\t" + toString() + " => " + nt.reachableNonTerminals().size() + " rules");
		return nt;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
