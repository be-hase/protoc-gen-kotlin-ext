# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A protoc compiler plugin that generates useful extension code for Kotlin: `*OrNull` extension properties for optional fields and data-class-like factory functions. It does not generate its own message classes — it only generates additional code on top of the classes produced by the official protobuf-java. Only proto3 is targeted.

## Common Commands

```bash
# Build + all tests + lint (ktlint / detekt)
./gradlew check

# Unit tests for the plugin itself
./gradlew :protoc-gen-kotlin-ext:test

# Run a single test class
./gradlew :protoc-gen-kotlin-ext:test --tests "dev.hsbrysk.protoc.gen.CompileOptionTest"

# Functional tests (actually run protoc code generation and verify)
./gradlew :functional-test:test

# Functional tests with protobuf-kotlin enabled (also run in CI)
./gradlew :functional-test:clean :functional-test:test -PwithProtocGenKotlin

# Fix lint issues
./gradlew ktlintFormat
```

- The Kotlin code is compiled with a Java 8 toolchain (foojay-resolver auto-downloads the JDK). `allWarningsAsErrors = true`, so warnings fail the build.
- Tests use JUnit 5 + assertk.

## Architecture

### Module Layout

- `protoc-gen-kotlin-ext/` — The plugin itself. Distributed as a fat jar with `GeneratorRunner.main` as the Main-Class.
- `functional-test/` — Runs actual code generation on the protos in `src/main/proto/` using the locally built plugin (the output of `:protoc-gen-kotlin-ext:installDist`) and tests the behavior of the generated code. Also verifies coexistence with the grpc-java / grpc-kotlin plugins.
- `build-logic/` — Convention plugins (`conventions.kotlin` / `conventions.ktlint` / `conventions.detekt`). Included build.

### Plugin Processing Flow (protoc-gen-kotlin-ext)

Following the protoc plugin protocol, it reads a `CodeGeneratorRequest` from stdin and writes a `CodeGeneratorResponse` to stdout. The entry point is `GeneratorRunner`:

1. `CompileOption.parseOptions` parses the compile options (`factory` / `orNullGetter` / `messageOrNullGetter`, toggled on/off with a `+`/`-` suffix) and determines the list of enabled `Generator`s.
2. For each proto file, a KotlinPoet `FileSpec.Builder` is created (the output file name is `<PascalCaseProtoFileName>KtExtensions.kt`), and each `Generator.apply` adds extension code to it.

There are three `Generator` implementations:

- `FactoryGenerator` — Generates data-class-constructor-like factory functions (default: on)
- `OrNullGetterGenerator` — Generates `*OrNull` extension properties for optional scalar fields (default: on)
- `MessageOrNullGetterGenerator` — Generates `*OrNull` for message-type fields (default: off, because protoc-gen-kotlin already generates the equivalent when protobuf-kotlin is used)

The latter two share `AbstractOrNullGetterGenerator`. The extension functions under `util/` resolve Java package names, class names, and nesting structure from descriptors; edge cases such as outer class name collisions and Kotlin keyword escaping are handled there (the corresponding tests are `outer_class_same_*.proto` and `keyword.proto` in functional-test).

### Release

Pushing a `vX.Y.Z` tag triggers `.github/workflows/publish.yml`, which runs `sonatypeCentralUpload` to publish to Maven Central. The version is injected via `-PpublishVersion` / the `PUBLISH_VERSION` environment variable, defaulting to `latest-SNAPSHOT`. The jar is renamed with a `-jdk8` classifier before publishing.
