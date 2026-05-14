# DevOps & Cloud — Lesson 4: CI/CD with GitHub Actions

## What is CI/CD?

**CI** (Continuous Integration) — automatically build and test every code change
**CD** (Continuous Delivery/Deployment) — automatically deploy to production

```
Developer Push → GitHub → CI Build → Tests → Deploy to Staging → Deploy to Prod
     │               │          │         │            │                │
     ▼               ▼          ▼         ▼            ▼                ▼
   Commit      Triggers     Compile    Run Tests    Deploy to     Automatic
                Pipeline    + Package    + Lint     Staging ENV    Deploy
```

## GitHub Actions Basics

```yaml
# .github/workflows/ci.yml
name: Java CI

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest

    services:
      postgres:
        image: postgres:16
        env:
          POSTGRES_DB: testdb
          POSTGRES_USER: testuser
          POSTGRES_PASSWORD: testpass
        ports:
          - 5432:5432
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5

    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: maven

      - name: Build and Test
        run: mvn verify -B
        env:
          SPRING_DATASOURCE_URL: jdbc:postgresql://localhost:5432/testdb

      - name: Upload Test Results
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: test-results
          path: target/surefire-reports/

      - name: Build Docker Image
        if: github.ref == 'refs/heads/main'
        run: docker build -t myapp:${{ github.sha }} .

      - name: SonarQube Analysis
        if: always()
        run: mvn sonar:sonar -Dsonar.token=${{ secrets.SONAR_TOKEN }}
```

## Deployment Pipeline

```yaml
# .github/workflows/deploy.yml
name: Deploy to Production

on:
  push:
    branches: [ main ]

jobs:
  deploy:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Build
        run: mvn package -DskipTests

      - name: Build Docker Image
        run: |
          docker build -t registry.example.com/myapp:${{ github.sha }} .
          docker tag registry.example.com/myapp:${{ github.sha }} \
                   registry.example.com/myapp:latest

      - name: Push to Registry
        run: |
          echo "${{ secrets.REGISTRY_PASSWORD }}" | docker login \
            registry.example.com -u ${{ secrets.REGISTRY_USERNAME }} \
            --password-stdin
          docker push registry.example.com/myapp:${{ github.sha }}
          docker push registry.example.com/myapp:latest

      - name: Deploy to Kubernetes
        run: |
          kubectl set image deployment/myapp \
            myapp=registry.example.com/myapp:${{ github.sha }} \
            --record

      - name: Verify Deployment
        run: |
          kubectl rollout status deployment/myapp --timeout=5m
          curl -sf https://api.example.com/actuator/health
```

## Quality Gates

```yaml
# Add quality checks to CI
- name: Check Test Coverage
  run: |
    mvn jacoco:report
    # Fail if coverage < 80%
    coverage=$(grep -oP 'Total.*?(\d+)%' target/site/jacoco/index.html | grep -oP '\d+')
    if [ "$coverage" -lt 80 ]; then
      echo "Coverage $coverage% is below 80% threshold!"
      exit 1
    fi

- name: Checkstyle
  run: mvn checkstyle:check

- name: SpotBugs (FindBugs successor)
  run: mvn spotbugs:check
```

## GitHub Actions Workflow Syntax Deep Dive

```yaml
name: Full CI/CD Pipeline

on:
  push:
    branches: [main, develop, 'feature/**']
    tags: ['v*.*.*']
    paths-ignore: ['*.md', 'docs/**']
  pull_request:
    branches: [main]
    types: [opened, synchronize, reopened]
  schedule:
    - cron: '0 6 * * 1'  # Every Monday at 6 AM
  workflow_dispatch:       # Manual trigger
    inputs:
      environment:
        description: 'Deploy environment'
        required: true
        default: 'staging'
        type: choice
        options:
          - staging
          - production

# Environment variables at workflow level
env:
  REGISTRY: ghcr.io
  IMAGE_NAME: ${{ github.repository }}

jobs:
  # ─── Lint & Test ───
  lint:
    runs-on: ubuntu-latest
    timeout-minutes: 10
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: maven
      - run: mvn checkstyle:check spotbugs:check

  test:
    needs: lint
    runs-on: ubuntu-latest
    timeout-minutes: 30
    services:
      postgres:
        image: postgres:16
        env:
          POSTGRES_DB: testdb
          POSTGRES_USER: testuser
          POSTGRES_PASSWORD: testpass
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
        ports:
          - 5432:5432
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: maven
      - name: Run tests with coverage
        run: mvn verify -Pcoverage
        env:
          SPRING_DATASOURCE_URL: jdbc:postgresql://localhost:5432/testdb
      - name: Upload coverage report
        uses: actions/upload-artifact@v4
        with:
          name: coverage-report
          path: target/site/jacoco/
      - name: Publish test results
        uses: EnricoMi/publish-unit-test-result-action@v2
        if: always()
        with:
          files: 'target/surefire-reports/*.xml'

  # ─── Build & Package ───
  build:
    needs: test
    runs-on: ubuntu-latest
    outputs:
      image-tag: ${{ steps.meta.outputs.tags }}
    steps:
      - uses: actions/checkout@v4
      - uses: docker/setup-buildx-action@v3
      - uses: docker/login-action@v3
        with:
          registry: ${{ env.REGISTRY }}
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}
      - uses: docker/metadata-action@v5
        id: meta
        with:
          images: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}
          tags: |
            type=sha,prefix=,format=short
            type=ref,event=branch
            type=semver,pattern={{version}}
            type=raw,value=latest,enable={{is_default_branch}}
      - uses: docker/build-push-action@v5
        with:
          context: .
          push: true
          tags: ${{ steps.meta.outputs.tags }}
          labels: ${{ steps.meta.outputs.labels }}
          cache-from: type=gha
          cache-to: type=gha,mode=max
          sbom: true  # Software Bill of Materials

  # ─── Security Scan ───
  security:
    needs: build
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: OWASP Dependency Check
        uses: dependency-check/Dependency-Check_Action@main
        with:
          project: 'myapp'
          path: '.'
          format: 'HTML'
      - name: Trivy scan
        uses: aquasecurity/trivy-action@master
        with:
          image-ref: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:${{ github.sha }}
          format: 'sarif'
          output: 'trivy-results.sarif'
      - name: Upload Trivy results
        uses: github/codeql-action/upload-sarif@v3
        with:
          sarif_file: 'trivy-results.sarif'

  # ─── Deploy to Environment ───
  deploy:
    needs: [build, security]
    runs-on: ubuntu-latest
    environment:
      name: ${{ github.ref == 'refs/heads/main' && 'production' || 'staging' }}
      url: https://api.${{ github.ref == 'refs/heads/main' && 'myapp.com' || 'staging.myapp.com' }}
    concurrency:
      group: ${{ github.ref == 'refs/heads/main' && 'production' || 'staging' }}
      cancel-in-progress: false
    steps:
      - uses: actions/checkout@v4
      - name: Deploy to Kubernetes
        run: |
          kubectl set image deployment/myapp \
            myapp=${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:${{ github.sha }} \
            --record
      - name: Verify
        run: |
          kubectl rollout status deployment/myapp --timeout=5m
```

### Matrix Builds

```yaml
# Run tests across multiple Java versions and OS
strategy:
  matrix:
    java: [17, 21]
    os: [ubuntu-latest, windows-latest]
    include:
      - java: 21
        os: ubuntu-latest
        coverage: true   # Extra dimension for one combination
    exclude:
      - java: 17
        os: windows-latest  # Skip this combination

runs-on: ${{ matrix.os }}
steps:
  - uses: actions/setup-java@v4
    with:
      java-version: ${{ matrix.java }}
      distribution: 'temurin'
      cache: maven
  - run: mvn verify
  - if: matrix.coverage
    run: mvn jacoco:report
```

### Caching Strategies

```yaml
# Maven cache
- uses: actions/cache@v4
  with:
    path: ~/.m2/repository
    key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}
    restore-keys: |
      ${{ runner.os }}-maven-

# Docker layer cache (via BuildKit)
- uses: docker/build-push-action@v5
  with:
    cache-from: type=gha
    cache-to: type=gha,mode=max

# Gradle cache
- uses: actions/cache@v4
  with:
    path: |
      ~/.gradle/caches
      ~/.gradle/wrapper
    key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
    restore-keys: |
      ${{ runner.os }}-gradle-
```

## Jenkins Pipeline as Code (Declarative)

```groovy
// Jenkinsfile
pipeline {
    agent any

    tools {
        jdk 'jdk21'
        maven 'maven3'
    }

    environment {
        DOCKER_REGISTRY = 'registry.example.com'
        DOCKER_IMAGE = "${DOCKER_REGISTRY}/myapp:${BUILD_NUMBER}"
    }

    parameters {
        choice(name: 'ENV', choices: ['dev', 'staging', 'prod'], description: 'Deploy environment')
        booleanParam(name: 'SKIP_TESTS', defaultValue: false)
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean compile -q'
            }
        }

        stage('Test') {
            when {
                expression { !params.SKIP_TESTS }
            }
            parallel {
                stage('Unit Tests') {
                    steps { sh 'mvn test' }
                }
                stage('Integration Tests') {
                    steps {
                        sh 'mvn verify -Pintegration'
                    }
                }
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                    jacoco(
                        execPattern: 'target/jacoco.exec',
                        classPattern: 'target/classes',
                        sourcePattern: 'src/main/java'
                    )
                }
            }
        }

        stage('Quality Gate') {
            steps {
                sh 'mvn sonar:sonar -Dsonar.token=${SONAR_TOKEN}'
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Package') {
            steps {
                sh 'mvn package -DskipTests'
            }
        }

        stage('Docker Build & Push') {
            steps {
                sh """
                    docker build -t ${DOCKER_IMAGE} .
                    docker push ${DOCKER_IMAGE}
                """
            }
        }

        stage('Security Scan') {
            steps {
                sh "trivy image --severity HIGH,CRITICAL --exit-code 1 ${DOCKER_IMAGE}"
            }
        }

        stage('Deploy') {
            when {
                branch 'main'
            }
            steps {
                sh "kubectl set image deployment/myapp myapp=${DOCKER_IMAGE}"
                sh "kubectl rollout status deployment/myapp --timeout=5m"
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        success {
            emailext(
                subject: "SUCCESS: ${env.JOB_NAME} - ${env.BUILD_NUMBER}",
                body: "Pipeline completed successfully. See: ${env.BUILD_URL}"
            )
        }
        failure {
            emailext(
                subject: "FAILED: ${env.JOB_NAME} - ${env.BUILD_NUMBER}",
                body: "Pipeline failed. See: ${env.BUILD_URL}"
            )
        }
    }
}
```

## GitLab CI/CD

```yaml
# .gitlab-ci.yml
image: maven:3.9-eclipse-temurin-21

variables:
  MAVEN_OPTS: "-Dmaven.repo.local=$CI_PROJECT_DIR/.m2/repository"
  DOCKER_IMAGE: $CI_REGISTRY_IMAGE:$CI_COMMIT_SHORT_SHA

cache:
  key: ${CI_COMMIT_REF_SLUG}
  paths:
    - .m2/repository

stages:
  - validate
  - test
  - build
  - security
  - deploy

validate:
  stage: validate
  script:
    - mvn checkstyle:check spotbugs:check

test:
  stage: test
  services:
    - postgres:16-alpine
  variables:
    POSTGRES_DB: testdb
    POSTGRES_USER: testuser
    POSTGRES_PASSWORD: testpass
  script:
    - mvn verify
    - mvn jacoco:report
  artifacts:
    reports:
      junit: target/surefire-reports/*.xml
      coverage_report: target/site/jacoco/
    paths:
      - target/*.jar
    expire_in: 30 days
  coverage: '/Total.*?(\d+)%/'

build:
  stage: build
  script:
    - docker build -t $DOCKER_IMAGE .
    - docker push $DOCKER_IMAGE

security:
  stage: security
  script:
    - mvn org.owasp:dependency-check-maven:check
    - trivy image --severity HIGH,CRITICAL $DOCKER_IMAGE

deploy_staging:
  stage: deploy
  environment: staging
  script:
    - kubectl set image deployment/myapp myapp=$DOCKER_IMAGE -n staging
    - kubectl rollout status deployment/myapp -n staging
  only:
    - develop

deploy_production:
  stage: deploy
  environment:
    name: production
    url: https://api.myapp.com
  script:
    - kubectl set image deployment/myapp myapp=$DOCKER_IMAGE -n prod
    - kubectl rollout status deployment/myapp -n prod
  only:
    - main
  when: manual  # Requires manual approval
  needs:
    - build
    - security
```

## ArgoCD — GitOps

```yaml
# Application.yaml — ArgoCD app definition
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: my-java-app
  namespace: argocd
spec:
  project: default
  source:
    repoURL: https://github.com/myorg/myapp-gitops.git
    targetRevision: HEAD
    path: k8s/overlays/production
    helm:
      valueFiles:
        - values-prod.yaml
  destination:
    server: https://kubernetes.default.svc
    namespace: production
  syncPolicy:
    automated:
      prune: true
      selfHeal: true
      allowEmpty: false
    syncOptions:
      - CreateNamespace=true
      - PruneLast=true
      - ApplyOutOfSyncOnly=true
    retry:
      limit: 5
      backoff:
        duration: 5s
        factor: 2
        maxDuration: 3m
```

```bash
# ArgoCD CLI
argocd login argocd.example.com
argocd app list
argocd app get my-java-app
argocd app sync my-java-app
argocd app diff my-java-app
argocd app rollback my-java-app 1
argocd app set my-java-app --sync-policy automated
```

## Security Scanning in CI

```yaml
# SAST (Static Application Security Testing) — CodeQL
- name: Initialize CodeQL
  uses: github/codeql-action/init@v3
  with:
    languages: java
    queries: security-and-quality
- name: Autobuild
  uses: github/codeql-action/autobuild@v3
- name: Perform CodeQL Analysis
  uses: github/codeql-action/analyze@v3

# DAST (Dynamic Application Security Testing) — OWASP ZAP
- name: Start app
  run: |
    docker run -d -p 8080:8080 myapp:latest
    sleep 10
- name: ZAP Scan
  uses: zaproxy/action-full-scan@v0.10.0
  with:
    target: 'http://localhost:8080/'
    rules_file_name: '.zap/rules.tsv'
    cmd_options: '-a -j'

# Dependency scanning — Dependabot (GitHub native)
# .github/dependabot.yml
version: 2
updates:
  - package-ecosystem: "maven"
    directory: "/"
    schedule:
      interval: "weekly"
    open-pull-requests-limit: 10
    labels:
      - "dependencies"
      - "security"
    reviewers:
      - "team/security"
  - package-ecosystem: "docker"
    directory: "/"
    schedule:
      interval: "weekly"

# Container scanning with Trivy in CI
- name: Trivy vulnerability scan
  uses: aquasecurity/trivy-action@master
  with:
    image-ref: ${{ env.IMAGE_NAME }}:${{ github.sha }}
    format: 'table'
    exit-code: '1'
    severity: 'CRITICAL,HIGH'
    vuln-type: 'os,library'

# License compliance with Fossa or similar
- name: Check licenses
  run: |
    mvn license:check
    # Fail on GPL/AGPL dependencies
```

## Best Practices

1. **Fast feedback** — CI should complete in < 10 minutes
2. **Fail fast** — run fastest tests first
3. **Cache dependencies** — Maven/Gradle, Docker layers
4. **Immutable artifacts** — tag with commit hash, never overwrite
5. **Secret management** — use GitHub Secrets, never hardcode
6. **Idempotent deployments** — deploying twice = same result

```yaml
# Cache Maven dependencies
- name: Cache Maven
  uses: actions/cache@v3
  with:
    path: ~/.m2/repository
    key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}
    restore-keys: |
      ${{ runner.os }}-maven-
```

---

1. Create a GitHub Actions workflow that builds and tests your Java project on every push.
2. Add a PostgreSQL service container to your CI pipeline.
3. Add code quality checks (Checkstyle, SpotBugs) to the build.
4. Create a deployment workflow that builds a Docker image and pushes to Docker Hub.
5. Add a health check verification step after deployment.
