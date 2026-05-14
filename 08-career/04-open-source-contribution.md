# Career — Lesson 4: Open Source Contribution

## Why Open Source?

```
Benefits of Contributing:
┌─────────────────────────────────────────────────────────────┐
│                                                               │
│  🚀 Portfolio       — Real code used by real companies       │
│  📚 Learning        — Read production code from experts      │
│  🤝 Networking      — Connect with industry leaders          │
│  💼 Job             — Top companies hire contributors        │
│  🎯 Interview prep  — You understand large codebases         │
│  🌟 Recognition     — Become known in the community          │
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

## Finding Your First Project

### Java Projects That Welcome Beginners

| Project | Description | Good First Issue |
|---------|-------------|-----------------|
| **Spring Boot** | Most popular Java framework | `good-first-issue` label |
| **Hibernate** | JPA/ORM implementation | `good first issue` label |
| **Testcontainers** | Integration testing | `good first issue` label |
| **Apache Kafka** | Event streaming | `newbie` label |
| **Elasticsearch** | Search engine | `good first issue` label |
| **JUnit 5** | Testing framework | `good first issue` label |
| **Micrometer** | Metrics collection | `good-first-issue` label |
| **OpenRewrite** | Automated refactoring | `good first issue` label |
| **Netty** | Network framework | `beginner friendly` label |
| **Guava** | Google core libraries | `good first issue` label |

## Contribution Workflow

```
┌─────────┐   ┌─────────┐   ┌─────────┐   ┌─────────┐
│ 1. FIND  │──▶│ 2. READ │──▶│ 3. CODE │──▶│ 4. PR   │
│ Issue    │   │ CONTRIB │   │ & TEST  │   │ & REVIEW│
└─────────┘   └─────────┘   └─────────┘   └────┬────┘
                                                │
                                         ┌──────▼──────┐
                                         │ 5. MERGED!  │
                                         └─────────────┘
```

### Step 1: Find an Issue

```bash
# Search for beginner-friendly issues
# GitHub label: "good first issue" + language:Java
# GitHub label: "help wanted" + language:Java
# GitHub label: "beginner friendly" + language:Java

# Or filter by size
# GitHub label: "size:small" or "effort:small"
```

### Step 2: Understand the Project

```bash
# Fork the repository
git clone https://github.com/YOUR_USER/project-name.git
cd project-name

# Read contribution guide
cat CONTRIBUTING.md
cat CODE_OF_CONDUCT.md

# Build the project
./mvnw install -DskipTests  # or ./gradlew build

# Run tests
./mvnw test
```

### Step 3: Make Your Changes

```java
// Example: Fixing a bug in a test utility
// BEFORE — doesn't handle null
public static String capitalize(String input) {
    return input.substring(0, 1).toUpperCase() + input.substring(1);
}

// AFTER — null-safe
public static String capitalize(String input) {
    if (input == null || input.isEmpty()) {
        return input;
    }
    return input.substring(0, 1).toUpperCase() + input.substring(1);
}
```

### Step 4: Write Tests

```java
// ALWAYS include tests with your change
@Test
void shouldHandleNull() {
    assertNull(StringUtils.capitalize(null));
}

@Test
void shouldHandleEmpty() {
    assertEquals("", StringUtils.capitalize(""));
}

@Test
void shouldCapitalizeFirstLetter() {
    assertEquals("Hello", StringUtils.capitalize("hello"));
}
```

### Step 5: Submit PR

```bash
# Commit with descriptive message
git add -A
git commit -m "fix: handle null input in StringUtils.capitalize()"
git push origin my-fix

# Open PR on GitHub with:
# - What the problem is
# - How you fixed it
# - How to test it
# - Screenshots if UI change
# - Related issue number
```

## PR Best Practices

```markdown
## Summary
Fixed NullPointerException in `StringUtils.capitalize()` when input is null.

## Changes
- Added null check at the beginning of `capitalize()`
- Added empty string check for consistency
- Added 3 unit tests for null, empty, and normal cases

## Testing
- `mvn test` passes
- All 3 new tests pass
- Existing tests not affected

Closes #1234
```

## Types of Contributions

| Type | Difficulty | Impact | Example |
|------|-----------|--------|---------|
| **Documentation** | ⭐ | Medium | Fix typo, improve Javadoc |
| **Bug fix** | ⭐⭐ | High | Fix NPE, fix edge case |
| **Test improvement** | ⭐⭐ | Medium | Add missing tests |
| **Refactoring** | ⭐⭐⭐ | Medium | Improve code quality |
| **New feature** | ⭐⭐⭐⭐ | High | Add new functionality |
| **Performance** | ⭐⭐⭐⭐⭐ | High | Optimize critical path |

## Building Your Reputation

```
Start Small → Be Consistent → Get Known

Month 1-3:    Documentation fixes, small bug fixes
Month 3-6:    Bug fixes with tests, review others' PRs
Month 6-12:   Feature contributions, help triage issues
Month 12+:    Become a maintainer/committer
```

## Exercises

1. Find a "good first issue" in a Java open-source project.
2. Set up the project locally and run the tests.
3. Leave a helpful comment on an issue suggesting a fix.
4. Submit a documentation improvement PR (typo fix, better example).
5. Review an open PR and leave constructive feedback.
