package org.cheddarmonk.compression.golfscript;

import org.cheddarmonk.compression.GrammarBuilder.NonTerminal;
import org.cheddarmonk.util.Function;

/**
 * A (generator for a) GolfScript grammar expansion engine.
 */
public interface GrammarEngine extends Function<NonTerminal, String> {
	public String generate(NonTerminal grammar);
}

abstract class AbstractGrammarEngine extends GolfScriptEngine implements GrammarEngine {
	@Override
	public abstract String generate(NonTerminal grammar);

	@Override
	public String eval(NonTerminal grammar) {
		return generate(grammar);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}