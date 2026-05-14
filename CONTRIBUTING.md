# Contributing to the Bro Code Java Course

Thank you for considering contributing! This document outlines the process for contributing to the course content, code examples, and projects.

## Table of Contents

1. [Code of Conduct](#code-of-conduct)
2. [Getting Started](#getting-started)
3. [How to Contribute](#how-to-contribute)
4. [Code Style](#code-style)
5. [Testing Requirements](#testing-requirements)
6. [Pull Request Process](#pull-request-process)
7. [Reporting Issues](#reporting-issues)

## Code of Conduct

All contributors must adhere to our [Code of Conduct](CODE_OF_CONDUCT.md). Please read it before participating.

## Getting Started

1. **Fork** the repository to your GitHub account.
2. **Clone** your fork locally:
   ```bash
   git clone https://github.com/YOUR_USERNAME/java-by-BroCode.git
   ```
3. **Create a branch** for your changes:
   ```bash
   git checkout -b feat/your-feature-name
   ```
4. Make your changes following the guidelines below.

## How to Contribute

### Types of Contributions

- **Bug fixes**: Correcting errors in code examples, project code, or documentation.
- **New content**: Adding lessons, exercises, or project modules.
- **Improvements**: Enhancing existing content, refactoring code, or improving test coverage.
- **Documentation**: Fixing typos, clarifying explanations, or translating content.

### Branch Naming

- `feat/` — new features or content (e.g., `feat/add-io-streams-lesson`)
- `fix/` — bug fixes (e.g., `fix/wrong-import-in-auth-service`)
- `docs/` — documentation changes (e.g., `docs/fix-typo-in-readme`)
- `refactor/` — code restructuring (e.g., `refactor/extract-validator-utility`)

## Code Style

### Java Conventions

- Follow **Oracle Java Code Conventions** and **Google Java Style Guide**.
- Use 4-space indentation (no tabs).
- Maximum line length of 120 characters.
- One blank line between class members (methods, fields, inner classes).
- Organize imports: no wildcard imports, groups in order: static, Java, third-party, project.

### Naming

- **Classes**: PascalCase nouns (`UserService`, `OrderRepository`)
- **Methods**: camelCase verbs (`findById()`, `createOrder()`)
- **Constants**: UPPER_SNAKE_CASE (`MAX_RETRY_COUNT`)
- **Packages**: lowercase, no underscores (`com.brocode.ecommerce.service`)
- **Tests**: `{ClassUnderTest}Test` (e.g., `UserServiceTest`)

### Formatting

- Opening braces on the same line (`K&R style`).
- No trailing whitespace.
- Use `@Override` on all overriding methods.
- Prefer `var` only when the right-hand side makes the type obvious.

## Testing Requirements

- All new code must include tests.
- Unit tests must use **JUnit 5** and **Mockito**.
- Integration tests must use **Testcontainers** for database/Kafka dependencies.
- Aim for at least 70% code coverage on new code.
- Test naming: `should{ExpectedBehavior}_when{Condition}` (e.g., `shouldReturnUser_whenValidId()`).

## Pull Request Process

1. Ensure your branch is up to date with `main`:
   ```bash
   git fetch upstream
   git rebase upstream/main
   ```
2. Run all tests before committing:
   ```bash
   mvn clean test
   ```
3. Ensure no checkstyle violations:
   ```bash
   mvn checkstyle:check
   ```
4. Commit with a descriptive message:
   ```bash
   git commit -m "feat: add input/output streams lesson with exercises"
   ```
5. Push to your fork and open a PR against the `main` branch.
6. In the PR description, reference any related issues and describe your changes.
7. A maintainer will review your PR. Address any feedback by pushing additional commits.

### Commit Message Format

Follow [Conventional Commits](https://www.conventionalcommits.org/):
```
<type>: <short description>

[optional body]
```

Types: `feat`, `fix`, `docs`, `refactor`, `test`, `chore`.

## Reporting Issues

Use the GitHub issue tracker to report bugs or request features. Include:
- A clear description of the issue
- Steps to reproduce (for bugs)
- Java version and OS information
- Relevant code snippets or error logs
