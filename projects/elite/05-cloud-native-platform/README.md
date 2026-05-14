# Project 5: Cloud-Native Platform

**Concepts:** Kubernetes Operators, Service Mesh (Istio), Canary Deployments, GitOps (ArgoCD), KEDA Auto-scaling, OpenTelemetry, Cert-Manager, Helm Charts, Multi-cluster

## Architecture

```
                     ┌─────────────────────────────────────┐
                     │         Kubernetes Cluster           │
                     │                                     │
                     │  ┌───────────────────────────────┐  │
                     │  │      Service Mesh (Istio)     │  │
                     │  │  ┌────┐ ┌────┐ ┌────┐        │  │
                     │  │  │ A  │ │ B  │ │ C  │        │  │
                     │  │  └──┬─┘ └──┬─┘ └──┬─┘        │  │
                     │  │     │      │      │           │  │
                     │  │  ┌──▼──────▼──────▼──┐       │  │
                     │  │  │   Ingress Gateway  │       │  │
                     │  │  └────────┬──────────┘       │  │
                     │  └───────────┼───────────────────┘  │
                     │               │                     │
                     │  ┌────────────▼────────────┐        │
                     │  │   GitOps (ArgoCD)        │        │
                     │  │   + Helm Charts          │        │
                     │  └─────────────────────────┘        │
                     │                                     │
                     │  ┌──────────┐ ┌────────────────┐   │
                     │  │ KEDA     │ │ OpenTelemetry   │   │
                     │  │ Scaler   │ │ Collector       │   │
                     │  └──────────┘ └────────────────┘   │
                     └─────────────────────────────────────┘
```

## Table of Contents
1. [Helm Charts](#1-helm-charts)
2. [Kubernetes Manifests](#2-kubernetes-manifests)
3. [Istio Service Mesh](#3-istio-service-mesh)
4. [KEDA Auto-Scaler](#4-keda-auto-scaler)
5. [Java Operator SDK](#5-java-operator-sdk)
6. [GitOps with ArgoCD](#6-gitops-with-argocd)
7. [OpenTelemetry Observability](#7-opentelemetry-observability)
8. [Canary Deployments](#8-canary-deployments)
9. [Cert-Manager & TLS](#9-cert-manager--tls)
10. [Multi-Environment Overlays](#10-multi-environment-overlays)

---

## 1. Helm Charts

### Chart Structure

```
helm/service-a/
├── Chart.yaml
├── values.yaml
├── values-dev.yaml
├── values-prod.yaml
└── templates/
    ├── _helpers.tpl
    ├── deployment.yaml
    ├── service.yaml
    ├── ingress.yaml
    ├── serviceaccount.yaml
    ├── configmap.yaml
    ├── hpa.yaml
    ├── pdb.yaml
    └── tests/
        └── test-connection.yaml
```

### Chart.yaml

```yaml
apiVersion: v2
name: service-a
description: Cloud-native microservice A
type: application
version: 1.0.0
appVersion: "1.0.0"
dependencies:
  - name: postgresql
    version: 12.x.x
    repository: https://charts.bitnami.com/bitnami
    condition: postgresql.enabled
  - name: redis
    version: 18.x.x
    repository: https://charts.bitnami.com/bitnami
    condition: redis.enabled
```

### values.yaml

```yaml
replicaCount: 3

image:
  repository: ghcr.io/myorg/service-a
  tag: latest
  pullPolicy: Always

imagePullSecrets: []
nameOverride: ""
fullnameOverride: ""

serviceAccount:
  create: true
  annotations: {}
  name: ""

podAnnotations:
  sidecar.istio.io/inject: "true"
  instrumentation.opentelemetry.io/inject-java: "true"

podSecurityContext:
  runAsNonRoot: true
  runAsUser: 1000
  fsGroup: 2000

securityContext:
  capabilities:
    drop: ["ALL"]
  readOnlyRootFilesystem: true
  runAsNonRoot: true
  runAsUser: 1000

service:
  type: ClusterIP
  port: 8080
  targetPort: 8080

ingress:
  enabled: true
  className: istio
  annotations:
    cert-manager.io/cluster-issuer: letsencrypt-prod
  hosts:
    - host: api.example.com
      paths:
        - path: /api/v1/a
          pathType: Prefix
  tls:
    - secretName: service-a-tls
      hosts:
        - api.example.com

resources:
  limits:
    cpu: 1000m
    memory: 1Gi
  requests:
    cpu: 200m
    memory: 256Mi

autoscaling:
  enabled: true
  minReplicas: 3
  maxReplicas: 20
  targetCPUUtilizationPercentage: 70
  targetMemoryUtilizationPercentage: 80
  # KEDA scalers
  keda:
    enabled: true
    redis:
      enabled: true
      listLength: "10"
    kafka:
      enabled: false
      lagThreshold: "100"

postgresql:
  enabled: true
  auth:
    database: servicea
    username: app

redis:
  enabled: true
  architecture: standalone

nodeSelector: {}
tolerations: []
affinity:
  podAntiAffinity:
    preferredDuringSchedulingIgnoredDuringExecution:
      - weight: 100
        podAffinityTerm:
          labelSelector:
            matchExpressions:
              - key: app.kubernetes.io/name
                operator: In
                values:
                  - service-a
          topologyKey: kubernetes.io/hostzone

probes:
  liveness:
    path: /actuator/health/liveness
    initialDelaySeconds: 30
    periodSeconds: 10
  readiness:
    path: /actuator/health/readiness
    initialDelaySeconds: 20
    periodSeconds: 5

podDisruptionBudget:
  enabled: true
  minAvailable: 2

env:
  - name: SPRING_PROFILES_ACTIVE
    value: "k8s"
  - name: JAVA_OPTS
    value: "-Xms256m -Xmx512m -XX:+UseZGC -XX:MaxRAMPercentage=75.0"
```

### deployment.yaml

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: {{ include "service-a.fullname" . }}
  labels:
    {{- include "service-a.labels" . | nindent 4 }}
spec:
  {{- if not .Values.autoscaling.enabled }}
  replicas: {{ .Values.replicaCount }}
  {{- end }}
  selector:
    matchLabels:
      {{- include "service-a.selectorLabels" . | nindent 6 }}
  template:
    metadata:
      {{- with .Values.podAnnotations }}
      annotations:
        {{- toYaml . | nindent 8 }}
      {{- end }}
      labels:
        {{- include "service-a.selectorLabels" . | nindent 8 }}
    spec:
      {{- with .Values.imagePullSecrets }}
      imagePullSecrets:
        {{- toYaml . | nindent 8 }}
      {{- end }}
      serviceAccountName: {{ include "service-a.serviceAccountName" . }}
      securityContext:
        {{- toYaml .Values.podSecurityContext | nindent 8 }}
      containers:
        - name: {{ .Chart.Name }}
          securityContext:
            {{- toYaml .Values.securityContext | nindent 12 }}
          image: "{{ .Values.image.repository }}:{{ .Values.image.tag | default .Chart.AppVersion }}"
          imagePullPolicy: {{ .Values.image.pullPolicy }}
          ports:
            - name: http
              containerPort: {{ .Values.service.targetPort }}
              protocol: TCP
          env:
            {{- toYaml .Values.env | nindent 12 }}
          resources:
            {{- toYaml .Values.resources | nindent 12 }}
          livenessProbe:
            httpGet:
              path: {{ .Values.probes.liveness.path }}
              port: http
            initialDelaySeconds: {{ .Values.probes.liveness.initialDelaySeconds }}
            periodSeconds: {{ .Values.probes.liveness.periodSeconds }}
          readinessProbe:
            httpGet:
              path: {{ .Values.probes.readiness.path }}
              port: http
            initialDelaySeconds: {{ .Values.probes.readiness.initialDelaySeconds }}
            periodSeconds: {{ .Values.probes.readiness.periodSeconds }}
          volumeMounts:
            - name: tmp
              mountPath: /tmp
          {{- if .Values.postgresql.enabled }}
            - name: postgres-credentials
              mountPath: /etc/secrets/postgres
              readOnly: true
          {{- end }}
      volumes:
        - name: tmp
          emptyDir: {}
      {{- with .Values.nodeSelector }}
      nodeSelector:
        {{- toYaml . | nindent 8 }}
      {{- end }}
      {{- with .Values.affinity }}
      affinity:
        {{- toYaml . | nindent 8 }}
      {{- end }}
      {{- with .Values.tolerations }}
      tolerations:
        {{- toYaml . | nindent 8 }}
      {{- end }}
```

---

## 2. Kustomize Overlays

### base/kustomization.yaml

```yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

resources:
  - namespace.yaml
  - service-account.yaml
  - deployments/service-a.yaml
  - deployments/service-b.yaml
  - deployments/service-c.yaml
  - services/service-a.yaml
  - services/service-b.yaml
  - services/service-c.yaml
  - configmaps/app-config.yaml
  - configmaps/logging-config.yaml
  - secrets/database-credentials.yaml
  - network-policies/default-deny.yaml
  - network-policies/allow-ingress.yaml
  - network-policies/allow-interservice.yaml
  - pod-disruption-budgets/service-a-pdb.yaml
  - pod-disruption-budgets/service-b-pdb.yaml

commonLabels:
  environment: base
  managed-by: kustomize

namespace: cloud-native

images:
  - name: service-a
    newName: ghcr.io/myorg/service-a
  - name: service-b
    newName: ghcr.io/myorg/service-b
  - name: service-c
    newName: ghcr.io/myorg/service-c
```

### overlays/dev/kustomization.yaml

```yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

bases:
  - ../../base

namePrefix: dev-

namespace: dev-cloud-native

patches:
  - target:
      kind: Deployment
    patch: |-
      - op: replace
        path: /spec/replicas
        value: 1
      - op: replace
        path: /spec/template/spec/containers/0/resources/requests/cpu
        value: 100m
      - op: replace
        path: /spec/template/spec/containers/0/resources/requests/memory
        value: 128Mi

configMapGenerator:
  - name: app-config
    behavior: merge
    literals:
      - SPRING_PROFILES_ACTIVE=dev
      - LOG_LEVEL=DEBUG

replicas:
  - name: service-a
    count: 1
  - name: service-b
    count: 1
  - name: service-c
    count: 1
```

### overlays/prod/kustomization.yaml

```yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

bases:
  - ../../base

namePrefix: prod-

namespace: prod-cloud-native

patchesStrategicMerge:
  - resource-limits.yaml
  - hpa-settings.yaml
  - pdb-settings.yaml

configMapGenerator:
  - name: app-config
    behavior: merge
    literals:
      - SPRING_PROFILES_ACTIVE=prod
      - LOG_LEVEL=WARN

replicas:
  - name: service-a
    count: 5
  - name: service-b
    count: 3
  - name: service-c
    count: 3

images:
  - name: service-a
    digest: sha256:abc123def456
  - name: service-b
    digest: sha256:789ghi012jkl
  - name: service-c
    digest: sha256:345mno678pqr
```

---

## 3. Istio Service Mesh

### istio/gateway.yaml

```yaml
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: cloud-native-gateway
spec:
  selector:
    istio: ingressgateway
  servers:
    - port:
        number: 80
        name: http
        protocol: HTTP
      hosts:
        - "api.example.com"
      tls:
        httpsRedirect: true
    - port:
        number: 443
        name: https
        protocol: HTTPS
      hosts:
        - "api.example.com"
      tls:
        mode: SIMPLE
        credentialName: cloud-native-tls
```

### istio/virtual-service.yaml

```yaml
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: cloud-native-routing
spec:
  hosts:
    - "api.example.com"
  gateways:
    - cloud-native-gateway
  http:
    - match:
        - uri:
            prefix: /api/v1/a
      route:
        - destination:
            host: service-a
            port:
              number: 8080
          weight: 90
        - destination:
            host: service-a-canary
            port:
              number: 8080
          weight: 10

    - match:
        - uri:
            prefix: /api/v1/b
      route:
        - destination:
            host: service-b
            port:
              number: 8080

    - match:
        - uri:
            prefix: /api/v1/c
      route:
        - destination:
            host: service-c
            port:
              number: 8080

  # Retry and timeout configuration
    - match:
        - uri:
            prefix: /api/v1
      timeout: 30s
      retries:
        attempts: 3
        perTryTimeout: 2s
        retryOn: connect-failure,refused-stream,unavailable,cancelled
```

### istio/destination-rule.yaml

```yaml
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: service-a-dr
spec:
  host: service-a
  trafficPolicy:
    connectionPool:
      tcp:
        maxConnections: 100
        connectTimeout: 3s
      http:
        http1MaxPendingRequests: 1024
        http2MaxRequests: 1024
        maxRequestsPerConnection: 100
    loadBalancer:
      simple: LEAST_CONN
    outlierDetection:
      consecutive5xxErrors: 5
      interval: 30s
      baseEjectionTime: 60s
      maxEjectionPercent: 50
  subsets:
    - name: stable
      labels:
        version: stable
    - name: canary
      labels:
        version: canary
---
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: service-b-dr
spec:
  host: service-b
  trafficPolicy:
    loadBalancer:
      simple: ROUND_ROBIN
    outlierDetection:
      consecutive5xxErrors: 3
      interval: 10s
      baseEjectionTime: 30s
      maxEjectionPercent: 100
```

### istio/service-entry.yaml

```yaml
apiVersion: networking.istio.io/v1beta1
kind: ServiceEntry
metadata:
  name: external-services
spec:
  hosts:
    - "api.stripe.com"
    - "api.sendgrid.com"
    - "s3.amazonaws.com"
  ports:
    - number: 443
      name: https
      protocol: TLS
  resolution: DNS
  location: MESH_EXTERNAL
---
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: external-routing
spec:
  hosts:
    - "api.stripe.com"
  tls:
    - match:
        - port: 443
          sniHosts:
            - "api.stripe.com"
      route:
        - destination:
            host: "api.stripe.com"
            port:
              number: 443
```

---

## 4. KEDA Auto-Scaler

### keda/scaled-object.yaml

```yaml
apiVersion: keda.sh/v1alpha1
kind: ScaledObject
metadata:
  name: service-a-scaler
spec:
  scaleTargetRef:
    name: service-a
  minReplicaCount: 3
  maxReplicaCount: 50
  pollingInterval: 10
  cooldownPeriod: 60
  advanced:
    horizontalPodAutoscalerConfig:
      behavior:
        scaleDown:
          stabilizationWindowSeconds: 300
          policies:
            - type: Percent
              value: 10
              periodSeconds: 60
        scaleUp:
          stabilizationWindowSeconds: 0
          policies:
            - type: Pods
              value: 5
              periodSeconds: 10
  triggers:
    - type: cpu
      metricType: Utilization
      metadata:
        value: "70"
    - type: memory
      metricType: Utilization
      metadata:
        value: "80"
    - type: prometheus
      metadata:
        serverAddress: http://prometheus.monitoring:9090
        metricName: http_requests_per_second
        query: |
          sum(rate(
            gateway_requests_total{route="service-a"}[2m]
          ))
        threshold: "100"
    - type: redis
      metadata:
        address: redis-master:6379
        listName: service-a-queue
        listLength: "10"
        activationListLength: "5"
    - type: kafka
      metadata:
        bootstrapServer: kafka-cluster:9092
        topic: service-a-commands
        consumerGroup: service-a-group
        lagThreshold: "100"
---
apiVersion: keda.sh/v1alpha1
kind: TriggerAuthentication
metadata:
  name: keda-redis-auth
spec:
  secretTargetRef:
    - parameter: password
      name: redis-secret
      key: redis-password
```

---

## 5. Java Operator SDK — Custom Controller

```java
package com.cloudnative.operator;

import io.javaoperatorsdk.operator.api.*;
import io.javaoperatorsdk.operator.api.reconciler.*;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Dependent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ControllerConfiguration(
    name = "app-deployment-controller",
    namespaces = Constants.WATCH_CURRENT_NAMESPACE,
    dependents = {
        @Dependent(name = "deployment", type = DeploymentDependentResource.class),
        @Dependent(name = "service", type = ServiceDependentResource.class),
        @Dependent(name = "ingress", type = IngressDependentResource.class),
        @Dependent(name = "pdb", type = PodDisruptionBudgetDependentResource.class)
    }
)
public class AppDeploymentReconciler
        implements Reconciler<AppDeployment>,
                   ErrorStatusHandler<AppDeployment> {

    private static final Logger log = LoggerFactory.getLogger(AppDeploymentReconciler.class);

    @Override
    public UpdateControl<AppDeployment> reconcile(
            AppDeployment resource, Context<AppDeployment> context) {

        log.info("Reconciling AppDeployment: {}/{}",
            resource.getMetadata().getNamespace(),
            resource.getMetadata().getName());

        AppDeploymentStatus status = new AppDeploymentStatus();
        status.setObservedGeneration(resource.getMetadata().getGeneration());

        try {
            // Dependent resources are managed automatically via @Dependent
            status.setStatus("RECONCILED");
            status.setMessage("All resources created/updated successfully");
        } catch (Exception e) {
            status.setStatus("ERROR");
            status.setMessage("Reconciliation failed: " + e.getMessage());
        }

        resource.setStatus(status);
        return UpdateControl.updateStatus(resource);
    }

    @Override
    public ErrorStatusUpdateControl<AppDeployment> updateErrorStatus(
            AppDeployment resource, Context<AppDeployment> context, Exception e) {

        AppDeploymentStatus status = new AppDeploymentStatus();
        status.setStatus("ERROR");
        status.setMessage(e.getMessage());
        resource.setStatus(status);

        return ErrorStatusUpdateControl.updateStatus(resource);
    }
}
```

### Custom Resource Definition

```java
package com.cloudnative.operator;

import io.javaoperatorsdk.operator.api.ObservedGenerationAware;
import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.*;

@Group("cloudnative.example.com")
@Version("v1")
@Kind("AppDeployment")
@Plural("appdeployments")
@ShortNames("appdep")
public class AppDeployment
        extends CustomResource<AppDeploymentSpec, AppDeploymentStatus>
        implements Namespaced, ObservedGenerationAware {

    @Override
    protected AppDeploymentStatus initStatus() {
        return new AppDeploymentStatus();
    }
}

// Spec
class AppDeploymentSpec {
    private int replicas = 3;
    private String image;
    private String tag = "latest";
    private ResourceRequirements resources;
    private ProbeConfig livenessProbe;
    private ProbeConfig readinessProbe;
    private AutoscalingConfig autoscaling;
    private CanaryConfig canary;
    private java.util.Map<String, String> env;
    private java.util.List<SecretRef> secrets;
    private ServiceConfig service;
    private IngressConfig ingress;
    private boolean istioInjection = true;

    // getters/setters
    public int getReplicas() { return replicas; }
    public void setReplicas(int replicas) { this.replicas = replicas; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }
    public ResourceRequirements getResources() { return resources; }
    public void setResources(ResourceRequirements resources) { this.resources = resources; }
    public ProbeConfig getLivenessProbe() { return livenessProbe; }
    public ProbeConfig getReadinessProbe() { return readinessProbe; }
    public AutoscalingConfig getAutoscaling() { return autoscaling; }
    public CanaryConfig getCanary() { return canary; }
    public java.util.Map<String, String> getEnv() { return env; }
    public java.util.List<SecretRef> getSecrets() { return secrets; }
    public ServiceConfig getService() { return service; }
    public IngressConfig getIngress() { return ingress; }
    public boolean isIstioInjection() { return istioInjection; }
}

class AppDeploymentStatus {
    private String status;
    private String message;
    private long observedGeneration;
    private java.util.List<String> resourceNames;
    private String deployedVersion;
    private java.util.Map<String, Integer> canaryStatus;

    // getters/setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public long getObservedGeneration() { return observedGeneration; }
    public void setObservedGeneration(long observedGeneration) { this.observedGeneration = observedGeneration; }
    public java.util.List<String> getResourceNames() { return resourceNames; }
    public void setResourceNames(java.util.List<String> resourceNames) { this.resourceNames = resourceNames; }
    public String getDeployedVersion() { return deployedVersion; }
    public void setDeployedVersion(String deployedVersion) { this.deployedVersion = deployedVersion; }
    public java.util.Map<String, Integer> getCanaryStatus() { return canaryStatus; }
    public void setCanaryStatus(java.util.Map<String, Integer> canaryStatus) { this.canaryStatus = canaryStatus; }
}

// Supporting types
record ResourceRequirements(CpuMemory limits, CpuMemory requests) {}
record CpuMemory(String cpu, String memory) {}
record ProbeConfig(String path, int initialDelay, int periodSeconds, int timeoutSeconds) {}
record AutoscalingConfig(boolean enabled, int minReplicas, int maxReplicas, int cpuTarget) {}
record CanaryConfig(boolean enabled, int weight, String header, String headerValue) {}
record SecretRef(String name, String key) {}
record ServiceConfig(int port, int targetPort, String type) {}
record IngressConfig(boolean enabled, String host, String path, boolean tls) {}
```

### Dependent Resource — Deployment

```java
package com.cloudnative.operator;

import io.javaoperatorsdk.operator.api.reconciler.dependent.*;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;

@KubernetesDependent(
    labelSelector = "app.kubernetes.io/managed-by=app-deployment-operator"
)
public class DeploymentDependentResource
        extends CRUDKubernetesDependentResource<Deployment, AppDeployment> {

    public DeploymentDependentResource() {
        super(Deployment.class);
    }

    @Override
    protected Deployment desired(AppDeployment primary,
                                  Context<AppDeployment> context) {
        AppDeploymentSpec spec = primary.getSpec();
        String name = primary.getMetadata().getName();

        return new DeploymentBuilder()
            .withNewMetadata()
                .withName(name)
                .withNamespace(primary.getMetadata().getNamespace())
                .addToLabels("app", name)
                .addToLabels("version", spec.getTag())
                .addToAnnotations("sidecar.istio.io/inject",
                    String.valueOf(spec.isIstioInjection()))
            .endMetadata()
            .withNewSpec()
                .withReplicas(spec.getReplicas())
                .withNewSelector()
                    .addToMatchLabels("app", name)
                .endSelector()
                .withNewTemplate()
                    .withNewMetadata()
                        .addToLabels("app", name)
                        .addToLabels("version", spec.getTag())
                    .endMetadata()
                    .withNewSpec()
                        .addNewContainer()
                            .withName(name)
                            .withImage(spec.getImage() + ":" + spec.getTag())
                            .withImagePullPolicy("Always")
                            .addNewPort()
                                .withContainerPort(8080)
                                .withName("http")
                            .endPort()
                            .withNewResources()
                                .addToLimits(java.util.Map.of(
                                    "cpu", new io.fabric8.kubernetes.api.model.Quantity(
                                        spec.getResources().limits().cpu()),
                                    "memory", new io.fabric8.kubernetes.api.model.Quantity(
                                        spec.getResources().limits().memory())
                                ))
                                .addToRequests(java.util.Map.of(
                                    "cpu", new io.fabric8.kubernetes.api.model.Quantity(
                                        spec.getResources().requests().cpu()),
                                    "memory", new io.fabric8.kubernetes.api.model.Quantity(
                                        spec.getResources().requests().memory())
                                ))
                            .endResources()
                            .withEnv(spec.getEnv().entrySet().stream()
                                .map(e -> new io.fabric8.kubernetes.api.model.EnvVar(
                                    e.getKey(), e.getValue(), null))
                                .toList())
                        .endContainer()
                        .withRestartPolicy("Always")
                    .endSpec()
                .endTemplate()
            .endSpec()
            .build();
    }
}
```

### Operator Main

```java
package com.cloudnative.operator;

import io.javaoperatorsdk.operator.Operator;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloudNativeOperator {

    private static final Logger log = LoggerFactory.getLogger(CloudNativeOperator.class);

    public static void main(String[] args) {
        log.info("Starting CloudNative Operator...");

        KubernetesClient client = new KubernetesClientBuilder()
            .withConfig(new ConfigBuilder().build())
            .build();

        Operator operator = new Operator(client);

        operator.register(new AppDeploymentReconciler());
        operator.register(new ServiceMeshConfigReconciler());
        operator.register(new CanaryDeploymentReconciler());

        Runtime.getRuntime().addShutdownHook(new Thread(operator::close));

        operator.start();
    }
}
```

---

## 6. GitOps with ArgoCD

### argo/project.yaml

```yaml
apiVersion: argoproj.io/v1alpha1
kind: AppProject
metadata:
  name: cloud-native
  namespace: argocd
spec:
  description: Cloud Native Platform
  sourceRepos:
    - 'https://github.com/myorg/cloud-native-config.git'
  destinations:
    - namespace: 'prod-*'
      server: 'https://kubernetes.default.svc'
    - namespace: 'staging-*'
      server: 'https://kubernetes.default.svc'
    - namespace: 'dev-*'
      server: 'https://kubernetes.default.svc'
  clusterResourceWhitelist:
    - group: '*'
      kind: '*'
  namespaceResourceWhitelist:
    - group: '*'
      kind: '*'
  roles:
    - name: admin
      policies:
        - p, proj:cloud-native:admin, applications, *, cloud-native/*, allow
    - name: read-only
      policies:
        - p, proj:cloud-native:read-only, applications, get, cloud-native/*, allow
```

### argo/application.yaml

```yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: cloud-native-platform
  namespace: argocd
  finalizers:
    - resources-finalizer.argocd.argoproj.io
spec:
  project: cloud-native
  source:
    repoURL: https://github.com/myorg/cloud-native-config.git
    targetRevision: HEAD
    path: k8s/overlays/prod
    helm:
      valueFiles:
        - values-prod.yaml
      parameters:
        - name: image.tag
          value: v1.2.3
  destination:
    server: https://kubernetes.default.svc
    namespace: prod-cloud-native
  syncPolicy:
    automated:
      prune: true
      selfHeal: true
      allowEmpty: false
    syncOptions:
      - Validate=true
      - CreateNamespace=true
      - PrunePropagationPolicy=foreground
      - PruneLast=true
      - ApplyOutOfSyncOnly=true
    retry:
      limit: 5
      backoff:
        duration: 5s
        factor: 2
        maxDuration: 3m
  ignoreDifferences:
    - group: apps
      kind: Deployment
      jsonPointers:
        - /spec/replicas
    - group: autoscaling
      kind: HorizontalPodAutoscaler
      jsonPointers:
        - /spec/metrics
```

---

## 7. OpenTelemetry Observability

```yaml
# OpenTelemetry Collector Configuration
# monitoring/otel-collector.yaml
apiVersion: opentelemetry.io/v1alpha1
kind: OpenTelemetryCollector
metadata:
  name: cloud-native-otel
spec:
  mode: deployment
  replicas: 2
  config: |
    receivers:
      otlp:
        protocols:
          grpc:
            endpoint: 0.0.0.0:4317
          http:
            endpoint: 0.0.0.0:4318

    processors:
      batch:
        timeout: 1s
        send_batch_size: 1024
      memory_limiter:
        check_interval: 1s
        limit_mib: 512
      attributes:
        actions:
          - key: environment
            value: production
            action: upsert
      resourcedetection:
        detectors: [gcp, ec2, kubernetes]
        timeout: 10s

    exporters:
      otlp:
        endpoint: jaeger-collector:4317
        tls:
          insecure: true
      prometheus:
        endpoint: 0.0.0.0:8889
        namespace: cloudnative
      debug:
        verbosity: detailed

    service:
      pipelines:
        traces:
          receivers: [otlp]
          processors: [memory_limiter, batch, attributes, resourcedetection]
          exporters: [otlp, debug]
        metrics:
          receivers: [otlp]
          processors: [memory_limiter, batch]
          exporters: [prometheus, debug]
        logs:
          receivers: [otlp]
          processors: [memory_limiter, batch]
          exporters: [debug]
```

### Java OpenTelemetry Agent Configuration

```yaml
# ConfigMap for OTEL Java agent
apiVersion: v1
kind: ConfigMap
metadata:
  name: otel-agent-config
data:
  otel-java-agent.properties: |
    otel.service.name=${spring.application.name:unknown}
    otel.resource.attributes=deployment.environment=production
    otel.traces.exporter=otlp
    otel.metrics.exporter=otlp
    otel.logs.exporter=otlp
    otel.exporter.otlp.endpoint=http://otel-collector:4317
    otel.exporter.otlp.protocol=grpc
    otel.instrumentation.spring-webflux.enabled=true
    otel.instrumentation.spring-webmvc.enabled=true
    otel.instrumentation.spring-data.enabled=true
    otel.instrumentation.jdbc.enabled=true
    otel.instrumentation.redis.enabled=true
    otel.instrumentation.kafka.enabled=true
    otel.instrumentation.r2dbc.enabled=true
    otel.instrumentation.reactor.enabled=true
    otel.instrumentation.log4j.enabled=true
    otel.instrumentation.logback.enabled=true
    otel.instrumentation.executor.enabled=true
```

---

## 8. Canary Deployments

### Canary Analysis — Flagger-style

```yaml
# istio/canary.yaml
apiVersion: flagger.app/v1beta1
kind: Canary
metadata:
  name: service-a
spec:
  targetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: service-a
  service:
    port: 8080
    targetPort: 8080
    portName: http
    gateways:
      - cloud-native-gateway
      - mesh
    hosts:
      - api.example.com
    match:
      - uri:
          prefix: /api/v1/a
  analysis:
    interval: 60s
    threshold: 5
    maxWeight: 50
    stepWeight: 10
    metrics:
      - name: request-success-rate
        thresholdRange:
          min: 99
        interval: 1m
      - name: request-duration
        thresholdRange:
          max: 500
        interval: 1m
      - name: "istio_requests_total"
        templateRef:
          name: istio-error-rate
        thresholdRange:
          max: 1
        interval: 1m
    webhooks:
      - name: load-test
        url: http://load-tester:8080/
        timeout: 5s
        metadata:
          cmd: "hey -z 2m -q 10 http://service-a-canary:8080/health"
    alerting:
      annotations:
        summary: "Canary deployment for service-a"
      provider: slack
```

---

## 9. Cert-Manager

```yaml
# certmanager/cluster-issuer.yaml
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: letsencrypt-prod
spec:
  acme:
    server: https://acme-v02.api.letsencrypt.org/directory
    email: infra@example.com
    privateKeySecretRef:
      name: letsencrypt-prod-account-key
    solvers:
      - http01:
          ingress:
            class: istio
---
# certmanager/certificate.yaml
apiVersion: cert-manager.io/v1
kind: Certificate
metadata:
  name: cloud-native-tls
  namespace: istio-system
spec:
  secretName: cloud-native-tls
  duration: 2160h  # 90 days
  renewBefore: 360h  # 15 days before expiry
  subject:
    organizations:
      - Example Corp
  isCA: false
  privateKey:
    algorithm: RSA
    encoding: PKCS1
    size: 2048
  usages:
    - server auth
    - client auth
  dnsNames:
    - api.example.com
    - "*.example.com"
  issuerRef:
    name: letsencrypt-prod
    kind: ClusterIssuer
```

---

## 10. Horizontal Pod Autoscaler

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: service-a-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: service-a
  minReplicas: 3
  maxReplicas: 50
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
    - type: Resource
      resource:
        name: memory
        target:
          type: Utilization
          averageUtilization: 80
    - type: Pods
      pods:
        metric:
          name: requests_per_second
        target:
          type: AverageValue
          averageValue: 100
  behavior:
    scaleDown:
      stabilizationWindowSeconds: 300
      policies:
        - type: Percent
          value: 10
          periodSeconds: 60
    scaleUp:
      stabilizationWindowSeconds: 0
      policies:
        - type: Pods
          value: 5
          periodSeconds: 10
        - type: Percent
          value: 100
          periodSeconds: 10
      selectPolicy: Max
```

---

## Summary of Cloud-Native Patterns

| Pattern | Implementation |
|---------|---------------|
| **GitOps** | ArgoCD with Kustomize overlays for multi-env |
| **Service Mesh** | Istio with mTLS, traffic splitting, retries |
| **Canary Deployments** | Flagger + Istio progressive delivery |
| **Auto-scaling** | KEDA with CPU/memory/Prometheus/Redis triggers |
| **Operators** | Java Operator SDK for custom controllers |
| **Observability** | OpenTelemetry + Jaeger + Prometheus + Grafana |
| **Cert Management** | cert-manager with Let's Encrypt |
| **Multi-env** | Kustomize overlays (dev/staging/prod) |
| **Network Policies** | Default-deny with allow rules |
| **Pod Disruption Budgets** | minAvailable: 2 for HA |
| **Pod Anti-Affinity** | Spread across nodes/zones |
