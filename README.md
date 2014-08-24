## GolfScript-Kolmogorov

This is a fairly simple application which was developed to generate [GolfScript](http://www.golfscript.com/golfscript/) programs which evaluate to a fixed string.
Its intended use is for [kolmogorov-complexity](http://codegolf.stackexchange.com/tags/kolmogorov-complexity) questions on the [Programming Puzzles and Code Golf](http://codegolf.stackexchange.com/) Stack Exchange. For example, this program generated a [511-byte program which prints a rick-roll](http://codegolf.stackexchange.com/a/11549/194).

The main class is `org.cheddarmonk.compression.golfscript.Kolmogorov` and it reads the text to compress from stdin. It tries a number of different approaches and dumps the smallest few (because it has happened in the past that there's been a bug in the smallest program, so it's convenient to also have the second-smallest in a file) to `/tmp`. Note that some of the approaches are non-deterministic (mainly in the area of tie-breakers), so it's often worth running the program a few times.

Most of the approaches follow a two-phase process. In the first phase the string is converted into a grammar which generates it; in the second phase, the grammar is converted into a GolfScript program. The first phase implementations are largely based on Charikar, Lehman, Liu, Panigrahy, Prabhakaran, Sahai, & Shelat (2005) *[The smallest grammar problem](https://www.cs.virginia.edu/~shelat/papers/GrammarIEEE.pdf)*, Information Theory, IEEE Transactions on, 51(7), 2554-2576.

NB I make no claim that this is production-quality code, but I have received a request to make it public in whatever state it might be in.
