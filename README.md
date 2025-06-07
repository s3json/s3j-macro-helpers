s3j-macro-helpers
=================

A helper library for the **s3j** project, doing everything that Scala 3 macro API designers didn't wanted me to do. This library encapsulates all accesses to the Dotty internals, allowing the rest of s3j code to stay within public API bounds. Assisted implicit resolver is available as a [separate library](https://github.com/makkarpov/explicits). Bits of code in this repository are not general-purpose enough to warrant a dedicated library, yet still make use of Dotty internals. While present situation is not nearly as bad as in the assisted implicits, every Scala release could easily change this status quo.
