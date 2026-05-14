FROM eclipse-temurin:21-jdk

ARG MAVEN_VERSION=3.9.6
ARG GRADLE_VERSION=8.7
ARG HELM_VERSION=3.14.3
ARG KUBECTL_VERSION=1.29.3

ENV MAVEN_HOME=/usr/share/maven \
    GRADLE_HOME=/opt/gradle \
    JAVA_HOME=/opt/java/openjdk

ENV PATH=$MAVEN_HOME/bin:$GRADLE_HOME/bin:/usr/local/bin:$PATH

RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    git \
    wget \
    gnupg \
    unzip \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /usr/share/maven /usr/share/maven/ref \
    && curl -fsSL https://dlcdn.apache.org/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz \
    | tar -xzC /usr/share/maven --strip-components=1 \
    && ln -s /usr/share/maven/bin/mvn /usr/local/bin/mvn

RUN curl -fsSL https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip -o gradle.zip \
    && unzip -q gradle.zip -d /opt \
    && mv /opt/gradle-$GRADLE_VERSION $GRADLE_HOME \
    && rm gradle.zip \
    && ln -s $GRADLE_HOME/bin/gradle /usr/local/bin/gradle

RUN curl -fsSL https://get.helm.sh/helm-v$HELM_VERSION-linux-amd64.tar.gz \
    | tar -xzC /usr/local/bin --strip-components=1 linux-amd64/helm

RUN curl -fsSL https://dl.k8s.io/release/v$KUBECTL_VERSION/bin/linux/amd64/kubectl -o /usr/local/bin/kubectl \
    && chmod +x /usr/local/bin/kubectl

RUN curl -fsSL https://download.docker.com/linux/static/stable/x86_64/docker-24.0.7.tgz \
    | tar -xzC /usr/local/bin --strip-components=1 docker/docker

RUN useradd -m -s /bin/bash vscode \
    && usermod -aG docker vscode

USER vscode
WORKDIR /workspace

CMD ["bash"]
