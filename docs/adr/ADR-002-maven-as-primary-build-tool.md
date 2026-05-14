# ADR-002: Maven as Primary Build Tool

## Status

Accepted

## Context

The course requires a build tool that is industry-standard, well-documented, and suitable for projects ranging from single-module exercises to multi-module enterprise systems. Both Maven and Gradle are widely used in the Java ecosystem.

## Decision

Use Maven as the primary build tool with `pom.xml` configuration throughout the course. Provide Gradle `build.gradle` equivalents as a secondary reference in advanced modules.

Rationale:
- Maven's convention-over-configuration model is easier for beginners to understand
- POM structure is declarative and self-documenting
- Industry standard for enterprise Java (Spring projects, government, banking)
- Maven Wrapper (`mvnw`) ensures reproducible builds without global Maven installation
- Maven's strict lifecycle phases align well with CI/CD pipeline education
- Multi-module builds with Maven are well-understood and documented

## Consequences

**Pros:**
- Simplified onboarding for students new to build tools
- Extensive plugin ecosystem for testing, reporting, and deployment
- Native support in all major CI platforms
- Maven BOM (Bill of Materials) simplifies dependency management

**Cons:**
- Verbose XML configuration compared to Gradle's Groovy/Kotlin DSL
- Slower incremental builds than Gradle for large multi-module projects
- Students may encounter Gradle in some workplaces and need adaptation
