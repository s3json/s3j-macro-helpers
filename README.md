s3j-macro-helpers
=================

This is a support library for the **s3j** project, consolidating macro-level functionality that requires access to Scala 3 (Dotty) compiler internals, which are not exposed via the stable `Quotes` API. Its purpose is to isolate low-level functionality, allowing the main **s3j** codebase to remain within the boundaries of the public API.

Currently implemented features
------------------------------

* **Access to `-Xmacro-settings:` compiler flag**
  - Provides project-wide configuration options such as naming conventions (fields, classes, enum variants) and encoding preferences (e.g., for binary data or timestamps).
  - This is necessary because the standard `Quotes` method remains `@experimental` for years already, despite its simplicity.

* **A workaround for [Dotty issue #16147](https://github.com/lampepfl/dotty/issues/16147)**

* **`TypeRepr.dealias`, but keeping annotations**
  - Normal `dealias` method is explicitly documented to remove all annotations from the type
  - Dotty has `dealiasKeepAnnots` method which performs de-aliasing only and keeps annotations intact
  - **s3j** uses this method to resolve aliased types while keeping modifier annotations on them (such as `@jsonUnsigned`)

* **Flexible class generation**
  - The `Quotes` API is a one-shot method requiring full class structure at creation time, complicating scenarios involving forward (or circular) references or complex macro logic.
  - This library enables incremental, imperative class construction. Class components can be added or reordered dynamically, with finalization deferred until generation is complete.

* **Assisted implicit resolution with injected imports**
  - Scala's implicit resolution is all-or-nothing; it provides either a complete result or fails entirely.
  - This limitation hinders recursive derivation of serializers when not every class explicitly uses `derives`, e.g.:
    ```scala
    given seqFormat[T](using JsonFormat[T]): JsonFormat[Seq[T]] = { /* ... */ }
    case class Foo(/* ... */)
    case class Bar(foos: Seq[Foo]) derives JsonFormat
    ```
    The compiler cannot resolve `JsonFormat[Seq[Foo]]` even if `JsonFormat[Foo]` can be derived internally.
  - This library allows such assisted resolution mechanism to be built on top of the standard machinery by injecting additional catch-all implicit at the lowest priority level.
  - It also enables the injection of pre-defined well-known implicit search locations into the compiler resolution process.

Scala version compatibility
---------------------------

Due to the instability and the evolving nature of Dotty internals and its API, this library is sensitive to even minor Scala version changes. It is currently tested against all released compiler versions from **3.2.0** to **3.7.1**.
