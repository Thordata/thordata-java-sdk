# Changelog

All notable changes to this project will be documented in this file.

## [1.1.0] - 2026-01-06

### Added
- **OkHttp Integration**: Replaced legacy `Socket`-based proxy implementation with `OkHttp` for robust TLS-in-TLS tunneling and connection pooling.
- **Maven Artifacts**: Added `maven-source-plugin` and `maven-javadoc-plugin` to build process.
- **User-Agent**: Standardized User-Agent format.

### Fixed
- **Code Quality**: Cleaned up unused imports and suppressed unchecked warnings.
- **Documentation**: Full rewrite of `README.md`.

## [1.0.1] - 2026-01-05

### Added
- **OkHttp Integration**: Replaced raw Socket implementation with `OkHttp` for robust connection pooling and TLS handling in `ProxyNetwork`.
- **User-Agent**: Standardized User-Agent format to `thordata-java-sdk/{version} java/{ver} ({os})`.
- **Javadoc**: Added source and javadoc attachment plugins to `pom.xml`.

### Fixed
- **Cleanups**: Removed unused imports and suppressed unchecked warnings for cleaner build logs.