package org.cheddarmonk.compression.golfscript;

import java.io.*;
import java.util.*;
import org.cheddarmonk.compression.*;
import org.cheddarmonk.util.*;

public class Kolmogorov {
	private Set<Function<String, Collection<Program>>> compilers = new HashSet<Function<String, Collection<Program>>>();

	public static void main(String[] args) throws Exception {
		StringBuilder sb = new StringBuilder();
		InputStreamReader isr = new InputStreamReader(System.in, "ISO-8859-1");
		char[] buf = new char[4096];
		int len;
		while ((len = isr.read(buf, 0, buf.length)) > 0) {
			sb.append(buf, 0, len);
		}
		isr.close();

		new Kolmogorov().optimise(sb.toString());
	}

	private Kolmogorov()
	{
		// Lempel-Ziv-based approaches
		BuilderCollection<List<LZBuilder.LZToken>> lzBuilders = new BuilderCollection<List<LZBuilder.LZToken>>();
		lzBuilders.registerBuilder(new LZBuilder());
		lzBuilders.registerEngine(new LZEngine());
		lzBuilders.registerEngine(new LZBasified());
		compilers.add(lzBuilders);

		// General grammar-based approaches
		BuilderCollection<GrammarBuilder.NonTerminal> grammarBuilders = new BuilderCollection<GrammarBuilder.NonTerminal>();
		grammarBuilders.registerBuilder(new SequentialGrammarBuilder());
		grammarBuilders.registerBuilder(new LongestMatchGrammarBuilder());
		grammarBuilders.registerBuilder(new RePairGrammarBuilder());
		grammarBuilders.registerBuilder(new SubstringSuperstringGrammarBuilder());
		for (int i = 0; i < 4; i++) grammarBuilders.registerBuilder(new GreedyGrammarBuilder(i));
		grammarBuilders.registerEngine(new OffsetEngine());
		grammarBuilders.registerEngine(new RemapEngine());
		compilers.add(grammarBuilders);

		// Trivial approaches
		BuilderCollection<String> basicBuilders = new BuilderCollection<String>();
		basicBuilders.registerBuilder(new Function<String, String>() {
			@Override public String eval(String t) { return t; }
			@Override public String toString() { return "Raw string"; }
		});
		basicBuilders.registerEngine(new Function<String, String>() {
			@Override public String eval(String t) { return GolfScriptEngine.escape(t); }
			@Override public String toString() { return "String literal"; }
		});
		basicBuilders.registerEngine(new Function<String, String>() {
			@Override public String eval(String t) { return GolfScriptEngine.basify(t) + "+"; }
			@Override public String toString() { return "Simple base conversion"; }
		});
		basicBuilders.registerEngine(new Function<String, String>() {
			@Override public String eval(String t) {
				char[] chs = t.toCharArray();
				int min = 256;
				for (char ch : chs) if (ch < min) min = ch;
				for (int i = 0; i < chs.length; i++) chs[i] -= min;
				return GolfScriptEngine.basify(new String(chs)) + "{" + min + "+}%+";
			}
			@Override public String toString() { return "Base conversion with offset"; }
		});
		basicBuilders.registerEngine(new RunlengthEncoder());
		compilers.add(basicBuilders);
	}

	@SuppressWarnings("unchecked")
	private void optimise(String str)
	{
		PriorityQueue<Program> q = new PriorityQueue<Program>();
		for (Function<String, Collection<Program>> compiler : compilers) {
			for (Program prog : compiler.eval(str)) {
				q.add(prog);
			}
		}

		for (int i = 0; ; i++) {
			Program program = q.poll();
			if (program == null) break;

			System.out.println(program.representationBuilder + " + " + program.programEngine + " => " + program.program.length());
			// Only dump the top 5 programs to disk - disk IO is the slowest part of the program, so skip the unnecessary stuff
			if (i > 5) continue;

			try {
				PrintWriter pw = new PrintWriter("/tmp/kolmogorov-" + program.program.length() + ".gs", "ISO-8859-1");
				pw.write(program.program);
				pw.close();
			}
			catch (IOException ex) {
				System.out.println(ex);
			}
		}
	}

	static class BuilderCollection<Intermediate> implements Function<String, Collection<Program>>
	{
		private Set<Function<String, Intermediate>> builders = new HashSet<Function<String, Intermediate>>();
		private Set<Function<Intermediate, String>> engines = new HashSet<Function<Intermediate, String>>();

		public void registerBuilder(Function<String, Intermediate> builder) {
			builders.add(builder);
		}

		public void registerEngine(Function<Intermediate, String> engine) {
			engines.add(engine);
		}

		@Override
		public Collection<Program> eval(String str) {
			List<Program> programs = new ArrayList<Program>();
			for (Function<String, Intermediate> builder : builders) {
				Intermediate i;
				try {
					i = builder.eval(str);
				}
				catch (RuntimeException re) {
					System.err.println("\t" + builder + " failed: " + re);
					continue;
				}

				for (Function<Intermediate, String> engine : engines) {
					try {
						programs.add(new Program(builder, engine, engine.eval(i)));
					}
					catch (RuntimeException re) {
						System.err.println("\t" + engine + " failed: " + re);
						continue;
					}
				}
			}

			return programs;
		}
	}

	static class Program implements Comparable<Program>
	{
		private final Object representationBuilder;
		private final Object programEngine;
		private final String program;

		Program(Object builder, Object engine, String program) {
			this.representationBuilder = builder;
			this.programEngine = engine;
			this.program = program;
		}

		@Override
		public int compareTo(Program o) {
			return program.length() - o.program.length();
		}
	}
}
