# Contributing to Temporal.io Plugin

Thank you for your interest in contributing!

## Prerequisites

- JDK 21+
- Gradle 9.2+
- [Temporal CLI](https://docs.temporal.io/cli#install) (for testing)

## Development

For build commands, architecture overview, and technical details, see [CLAUDE.md](CLAUDE.md).

Quick start:

```bash
# Run IDE with plugin installed
./gradlew runIde

# Run tests
./gradlew check
```

## GitHub Workflow

We use standard GitHub flow:

1. **Issues** — Report bugs or suggest features via [GitHub Issues](https://github.com/smolchanovsky/temporal-jetbrains-plugin/issues)
2. **Fork** — Fork the repository to your account
3. **Branch** — Create a feature branch from `main`
4. **Develop** — Make your changes and run tests locally
5. **Pull Request** — Open a PR against `main` with a clear description
6. **Review** — Address feedback and wait for approval
7. **Merge** — After approval, the PR will be merged

## Code Style

- Follow Kotlin coding conventions
- Use meaningful names
- Keep functions focused and small

For detailed guidelines, see [CLAUDE.md](CLAUDE.md).

## Using AI Agents

This project is configured for [Claude Code](https://claude.ai/code). The [CLAUDE.md](CLAUDE.md) file contains project context, architecture details, and guidelines that AI agents use to understand the codebase.
