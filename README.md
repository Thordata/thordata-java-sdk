# thordata-java-sdk

Official Java SDK for Thordata APIs.

## Development

This repository includes a git submodule (`sdk-spec`) for cross-SDK parity checks.

```bash
git submodule update --init --recursive
mvn -B test
```

## Examples

1) Copy `.env.example` to `.env` and fill in your credentials.
2) Run examples from the repository root.

Java:

```bash
mvn -q -DskipTests test-compile exec:java -Dexec.classpathScope=test -Dexec.mainClass=com.thordata.sdk.examples.SerpExample
```

## Git submodules

This repository uses a git submodule (`sdk-spec`) for cross-SDK parity checks.

After cloning:

```bash
git submodule update --init --recursive
```