package org.cheddarmonk.compression.golfscript;

import java.util.*;
import org.cheddarmonk.compression.GrammarBuilder.NonTerminal;

public class RemapEngine extends AbstractGrammarEngine {
	@Override
	public String generate(NonTerminal grammar) {
		// We use dummy rules to handle small terminals.
		List<NonTerminal> rules = grammar.topologicalSort();

		String expansion = grammar.toString();
		Set<Integer> codePoints = new HashSet<Integer>();
		for (char ch : expansion.toCharArray()) codePoints.add((int)ch);

		Map<NonTerminal, Integer> index = new HashMap<NonTerminal, Integer>();
		List<NonTerminal> expandedRules = new ArrayList<NonTerminal>();
		int ruleNo = 0;
		for (NonTerminal nt : rules) {
			while (codePoints.contains(ruleNo)) {
				NonTerminal dummy = new NonTerminal(Character.toString((char)ruleNo));
				index.put(dummy, ruleNo);
				expandedRules.add(dummy);
				ruleNo++;
			}

			index.put(nt, ruleNo);
			expandedRules.add(nt);
			ruleNo++;
		}

		if (expandedRules.size() > 256) throw new IndexOutOfBoundsException(rules.size() + " rules with dummies => " + expandedRules.size());

		StringBuilder rulesNullTerminated = new StringBuilder();
		for (NonTerminal rule : expandedRules) {
			if (rulesNullTerminated.length() > 0) rulesNullTerminated.append('\u0000');
			for (CharSequence cs : rule.production()) {
				if (cs instanceof NonTerminal) rulesNullTerminated.append((char)index.get(cs).intValue());
				else rulesNullTerminated.append(cs);
			}
		}

		// #'^0'XX,{\[1$]''+/\'rules''^0'/=*}/
		return "'\u0000'" + expandedRules.size()+",{\\[1$]''+/\\" + escape(rulesNullTerminated.toString()) + "'\u0000'/=*}/";
	}
}
