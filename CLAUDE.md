# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## プロジェクト概要

protoc コンパイラプラグイン。Kotlin 向けの拡張コード(optional フィールド用の `*OrNull` 拡張プロパティ、data class 風のファクトリ関数)を生成する。独自のメッセージクラスは生成せず、公式の protobuf-java が生成するクラスに対する追加コードのみを生成する。proto3 のみ対象。

## よく使うコマンド

```bash
# ビルド + 全テスト + lint(ktlint / detekt)
./gradlew check

# プラグイン本体のユニットテストのみ
./gradlew :protoc-gen-kotlin-ext:test

# 単一テストクラスの実行
./gradlew :protoc-gen-kotlin-ext:test --tests "dev.hsbrysk.protoc.gen.CompileOptionTest"

# functional-test(実際に protoc でコード生成して検証)
./gradlew :functional-test:test

# protobuf-kotlin 併用モードでの functional-test(CI でも実行される)
./gradlew :functional-test:clean :functional-test:test -PwithProtocGenKotlin

# lint 修正
./gradlew ktlintFormat
```

- Kotlin 本体は Java 8 toolchain でコンパイルされる(foojay-resolver が JDK を自動ダウンロード)。`allWarningsAsErrors = true` なので警告もビルドエラーになる。
- テストは JUnit 5 + assertk。

## アーキテクチャ

### モジュール構成

- `protoc-gen-kotlin-ext/` — プラグイン本体。`GeneratorRunner.main` を Main-Class とする fat jar として配布される。
- `functional-test/` — `src/main/proto/` の proto を、ローカルビルドしたプラグイン(`:protoc-gen-kotlin-ext:installDist` の成果物)で実際にコード生成し、生成コードの挙動をテストするモジュール。grpc-java / grpc-kotlin プラグインとの共存も検証している。
- `build-logic/` — convention plugin(`conventions.kotlin` / `conventions.ktlint` / `conventions.detekt`)。included build。

### プラグインの処理フロー(protoc-gen-kotlin-ext)

protoc プラグインプロトコルに従い、stdin から `CodeGeneratorRequest` を読み、stdout に `CodeGeneratorResponse` を書く。エントリポイントは `GeneratorRunner`:

1. `CompileOption.parseOptions` がコンパイルオプション(`factory` / `orNullGetter` / `messageOrNullGetter`、`+`/`-` サフィックスで on/off)をパースし、有効な `Generator` のリストを決める。
2. proto ファイルごとに KotlinPoet の `FileSpec.Builder` を作り(出力ファイル名は `<Protoファイル名のPascalCase>KtExtensions.kt`)、各 `Generator.apply` が拡張コードを追加していく。

`Generator` の実装は 3 つ:

- `FactoryGenerator` — data class コンストラクタ風のファクトリ関数を生成(デフォルト on)
- `OrNullGetterGenerator` — optional スカラーフィールド向け `*OrNull` 拡張プロパティ(デフォルト on)
- `MessageOrNullGetterGenerator` — メッセージ型フィールド向け `*OrNull`(デフォルト off。protobuf-kotlin 使用時は protoc-gen-kotlin 側が同等のものを生成するため)

後者 2 つは `AbstractOrNullGetterGenerator` を共有する。`util/` 配下に Descriptor から Java パッケージ名・クラス名・ネスト構造を解決する拡張関数群があり、outer class 名の衝突や Kotlin キーワードのエスケープなどのエッジケースはここで処理される(functional-test の `outer_class_same_*.proto` や `keyword.proto` が対応するテスト)。

### リリース

`v X.Y.Z` 形式のタグ push で `.github/workflows/publish.yml` が `sonatypeCentralUpload` を実行し Maven Central に公開される。バージョンは `-PpublishVersion` / `PUBLISH_VERSION` 環境変数で注入され、未指定時は `latest-SNAPSHOT`。jar は `-jdk8` classifier 付きにリネームして公開される。
