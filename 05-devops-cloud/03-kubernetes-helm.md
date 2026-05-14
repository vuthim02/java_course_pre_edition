# DevOps & Cloud — Lesson 3: Kubernetes & Helm

## Why Kubernetes?

Docker runs containers. But when you have 50 containers across 10 servers, you need:
- **Scheduling**: Where does each container run?
- **Scaling**: Add more copies when load increases
- **Self-healing**: Restart failed containers
- **Service discovery**: How containers find each other
- **Load balancing**: Distribute traffic across containers
- **Rolling updates**: Update without downtime

Kubernetes (K8s) solves ALL of these.

```
┌─────────────────────────────────────────────────────────────┐
│                      KUBERNETES CLUSTER                      │
│                                                               │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐     │
│  │  Node 1  │  │  Node 2  │  │  Node 3  │  │  Node 4  │     │
│  │ ┌──────┐ │  │ ┌──────┐ │  │ ┌──────┐ │  │ ┌──────┐ │     │
│  │ │ Pod  │ │  │ │ Pod  │ │  │ │ Pod  │ │  │ │ Pod  │ │     │
│  │ │(User │ │  │ │(Order│ │  │ │(User │ │  │ │(Order│ │     │
│  │ │ Svc) │ │  │ │ Svc) │ │  │ │ Svc) │ │  │ │ Svc) │ │     │
│  │ └──────┘ │  │ └──────┘ │  │ └──────┘ │  │ └──────┘ │     │
│  │ ┌──────┐ │  │ ┌──────┐ │  │ ┌──────┐ │  │ ┌──────┐ │     │
│  │ │ Auth  │ │  │ │Redis │ │  │ │ Auth  │ │  │ │Redis │ │     │
│  │ │ Pod   │ │  │ │ Pod  │ │  │ │ Pod   │ │  │ │ Pod  │ │     │
│  │ └──────┘ │  │ └──────┘ │  │ └──────┘ │  │ └──────┘ │     │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘     │
│                                                               │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │                    CONTROL PLANE                         │ │
│  │  API Server → Scheduler → Controller Manager → etcd     │ │
│  └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## Core Concepts

| Concept | What It Is | Analogy |
|---------|-----------|---------|
| **Pod** | Smallest deployable unit (1+ containers) | A running process |
| **Deployment** | Manages replica Pods, rolling updates | Instructions for running |
| **Service** | Stable network endpoint for Pods | Load balancer |
| **Ingress** | External HTTP/HTTPS routing | API gateway |
| **ConfigMap** | Non-sensitive configuration | Properties file |
| **Secret** | Sensitive data (passwords, keys) | Vault |
| **PersistentVolume** | Storage that survives pod restarts | External disk |
| **Namespace** | Virtual cluster within a cluster | Project folder |

## Minikube (Local Development)

```bash
# Install minikube
curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64
sudo install minikube-linux-amd64 /usr/local/bin/minikube

# Start cluster
minikube start --cpus=4 --memory=8g

# Check status
kubectl get nodes
kubectl cluster-info
```

## Deploying a Java App to Kubernetes

### 1. Dockerfile

```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 2. Deployment

```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: user-service
  labels:
    app: user-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: user-service
  template:
    metadata:
      labels:
        app: user-service
    spec:
      containers:
      - name: user-service
        image: myregistry/user-service:1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "k8s"
        - name: DB_URL
          valueFrom:
            configMapKeyRef:
              name: app-config
              key: db-url
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "512Mi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 20
          periodSeconds: 5
```

### 3. Service

```yaml
# service.yaml
apiVersion: v1
kind: Service
metadata:
  name: user-service
spec:
  type: ClusterIP
  selector:
    app: user-service
  ports:
  - port: 80
    targetPort: 8080
```

### 4. ConfigMap & Secret

```yaml
# configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
data:
  db-url: "jdbc:postgresql://postgres-service:5432/mydb"
  cache-enabled: "true"
---
# secret.yaml
apiVersion: v1
kind: Secret
metadata:
  name: app-secrets
type: Opaque
stringData:
  db-password: "supersecret123"
  jwt-secret: "my-jwt-secret-key-here"
```

### 5. Apply to Cluster

```bash
kubectl apply -f configmap.yaml
kubectl apply -f secret.yaml
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml

# Check status
kubectl get pods
kubectl get deployments
kubectl get services
kubectl logs deployment/user-service
```

## Rolling Updates

```bash
# Update image with zero downtime
kubectl set image deployment/user-service user-service=myregistry/user-service:1.1.0

# Check rollout status
kubectl rollout status deployment/user-service

# Rollback if needed
kubectl rollout undo deployment/user-service
```

```yaml
# Rolling update configuration
spec:
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxUnavailable: 1        # Max pods unavailable during update
      maxSurge: 1              # Max extra pods during update
```

## Helm — Kubernetes Package Manager

Helm packages K8s resources into **charts** (like apt/yum for K8s).

```
Without Helm:                          With Helm:
Multiple YAML files                    One command:
deployment.yaml                         helm install myapp ./chart
service.yaml
configmap.yaml                         Values customizable:
ingress.yaml                           helm install myapp ./chart \
hpa.yaml                                     --set replicaCount=5
secret.yaml
```

### Chart Structure

```
my-java-app/
├── Chart.yaml              # Metadata (name, version)
├── values.yaml             # Default configuration values
├── templates/
│   ├── deployment.yaml     # Template with {{ .Values.* }}
│   ├── service.yaml
│   ├── ingress.yaml
│   ├── configmap.yaml
│   ├── _helpers.tpl        # Helper templates
│   └── tests/
│       └── test-connection.yaml
└── .helmignore
```

### values.yaml

```yaml
# Default values — overridden per environment
replicaCount: 3

image:
  repository: myregistry/user-service
  tag: "1.0.0"
  pullPolicy: Always

service:
  port: 80
  targetPort: 8080

resources:
  requests:
    memory: "256Mi"
    cpu: "250m"
  limits:
    memory: "512Mi"
    cpu: "500m"

config:
  dbUrl: "jdbc:postgresql://postgres:5432/mydb"
  cacheEnabled: "true"

ingress:
  enabled: true
  host: api.myapp.com
```

### Template with Values

```yaml
# templates/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: {{ include "myapp.fullname" . }}
spec:
  replicas: {{ .Values.replicaCount }}
  selector:
    matchLabels:
      app: {{ include "myapp.name" . }}
  template:
    metadata:
      labels:
        app: {{ include "myapp.name" . }}
    spec:
      containers:
      - name: {{ .Chart.Name }}
        image: "{{ .Values.image.repository }}:{{ .Values.image.tag }}"
        ports:
        - containerPort: {{ .Values.service.targetPort }}
        env:
        - name: DB_URL
          value: {{ .Values.config.dbUrl }}
```

### Helm Commands

```bash
# Install a chart
helm install my-release ./my-java-app

# Upgrade (with new values)
helm upgrade my-release ./my-java-app -f values-prod.yaml

# List releases
helm list

# Rollback
helm rollback my-release 1

# Uninstall
helm uninstall my-release

# Template locally (dry-run)
helm template ./my-java-app
```

## kubectl Cheat Sheet (50+ Commands)

```bash
# ─── Context & Cluster ───
kubectl config view                              # Show merged kubeconfig
kubectl config get-contexts                      # List all contexts
kubectl config current-context                   # Show active context
kubectl config use-context prod-cluster          # Switch context
kubectl config set-context --current --namespace=prod  # Set default ns
kubectl cluster-info                             # Cluster endpoint info
kubectl cluster-info dump                        # Full cluster diagnostics

# ─── Pods ───
kubectl get pods                                 # List pods in default ns
kubectl get pods -n my-namespace                 # List in namespace
kubectl get pods -o wide                         # With node/IP info
kubectl get pods --all-namespaces                # All namespaces
kubectl get pods --show-labels                   # Show pod labels
kubectl get pods -l app=myapp,version=v2         # Label selector
kubectl describe pod my-pod                      # Detailed pod info
kubectl logs my-pod                              # Container logs
kubectl logs -f my-pod                           # Stream logs
kubectl logs my-pod -c my-container              # Specific container
kubectl logs --tail=100 -f deployment/myapp      # Last 100 lines, follow
kubectl logs -l app=myapp                        # All pods matching label
kubectl exec -it my-pod -- /bin/sh               # Interactive shell
kubectl exec my-pod -- env                       # Run command, no TTY
kubectl port-forward pod/my-pod 8080:80          # Forward to local
kubectl cp my-pod:/tmp/file.txt ./local-file     # Copy from pod
kubectl top pod                                  # Pod resource usage
kubectl delete pod my-pod --grace-period=0 --force  # Force delete

# ─── Deployments ───
kubectl get deployments                          # List deployments
kubectl describe deployment myapp                # Details
kubectl scale deployment myapp --replicas=5      # Scale up/down
kubectl set image deployment/myapp myapp=myapp:v2  # Update image
kubectl rollout status deployment/myapp           # Track rollout
kubectl rollout history deployment/myapp          # Revision history
kubectl rollout undo deployment/myapp             # Rollback to previous
kubectl rollout undo deployment/myapp --to-revision=2  # Rollback to rev 2
kubectl rollout restart deployment/myapp          # Graceful restart
kubectl autoscale deployment myapp --min=3 --max=10 --cpu-percent=75

# ─── Services ───
kubectl get services                             # List services
kubectl describe svc my-service                  # Details
kubectl get endpoints my-service                 # Endpoint IPs
kubectl port-forward svc/my-service 8080:80      # Forward service

# ─── ConfigMap & Secret ───
kubectl create configmap app-config --from-file=application.properties
kubectl create configmap app-config --from-literal=env=prod
kubectl get configmap app-config -o yaml         # View as YAML
kubectl create secret generic db-secret --from-literal=password=secret
kubectl get secret db-secret -o jsonpath='{.data.password}' | base64 -d

# ─── Ingress ───
kubectl get ingress                              # List ingresses
kubectl describe ingress my-ingress              # Details
kubectl annotate ingress my-ingress nginx.ingress.kubernetes.io/rewrite-target=/

# ─── Storage ───
kubectl get pv                                   # PersistentVolumes
kubectl get pvc                                  # PersistentVolumeClaims
kubectl describe pvc my-pvc

# ─── Debugging ───
kubectl get events --sort-by='.lastTimestamp'    # Recent events
kubectl get events -w                            # Watch events live
kubectl top nodes                                # Node resource usage
kubectl top pods                                 # Pod resource usage
kubectl get endpointslices                       # Advanced endpoints
kubectl api-resources                            # Available resource types
kubectl explain deployment.spec                  # Docs for resource field
kubectl get all -n my-namespace                  # All resources in ns

# ─── Namespaces ───
kubectl get namespaces                           # List namespaces
kubectl create namespace staging                 # Create namespace
kubectl delete namespace staging                 # Delete namespace + all resources

# ─── Imperative Commands ───
kubectl run nginx --image=nginx                  # Quick pod creation
kubectl expose pod nginx --port=80               # Create service for pod
kubectl create deployment myapp --image=myapp:latest
kubectl expose deployment myapp --port=80 --type=LoadBalancer

# ─── Resource Management ───
kubectl apply -f deployment.yaml                 # Create/update resource
kubectl apply -f k8s/ -R                         # Apply directory recursively
kubectl delete -f deployment.yaml                # Delete resource
kubectl delete pod,svc -l app=myapp              # Delete by label
kubectl replace -f deployment.yaml               # Replace (force update)
kubectl patch deployment myapp -p '{"spec":{"replicas":5}}'  # Partial update
kubectl diff -f deployment.yaml                  # Diff vs cluster state
```

## Pod Lifecycle

```yaml
# Pod lifecycle phases: Pending → Running → Succeeded/Failed
# initContainers run before app containers, must succeed
apiVersion: v1
kind: Pod
metadata:
  name: myapp-pod
spec:
  initContainers:
  - name: init-db
    image: postgres:16-alpine
    command: ['sh', '-c', 'until pg_isready -h db-service; do echo waiting for db; sleep 2; done;']
  - name: init-migrations
    image: flyway/flyway
    command: ['flyway', 'migrate']
    env:
    - name: FLYWAY_URL
      valueFrom:
        configMapKeyRef:
          name: app-config
          key: db-url
  containers:
  - name: myapp
    image: myapp:latest
    ports:
    - containerPort: 8080
    startupProbe:           # For slow-starting apps
      httpGet:
        path: /actuator/health/startup
        port: 8080
      initialDelaySeconds: 5
      periodSeconds: 5
      failureThreshold: 30   # Up to 150s to start
    livenessProbe:           # Restart if unhealthy
      httpGet:
        path: /actuator/health/liveness
        port: 8080
      initialDelaySeconds: 30
      periodSeconds: 10
    readinessProbe:          # Remove from Service if unhealthy
      httpGet:
        path: /actuator/health/readiness
        port: 8080
      initialDelaySeconds: 20
      periodSeconds: 5
    lifecycle:
      preStop:               # Graceful shutdown
        exec:
          command: ["sh", "-c", "sleep 10 && kill -15 1"]
  terminationGracePeriodSeconds: 60
```

### Sidecar Pattern

```yaml
# Sidecar container — runs alongside the main container
spec:
  containers:
  - name: myapp
    image: myapp:latest
    ports:
    - containerPort: 8080
  - name: log-sidecar
    image: busybox
    command: ['sh', '-c', 'tail -f /var/log/myapp/app.log']
    volumeMounts:
    - name: shared-logs
      mountPath: /var/log/myapp
  volumes:
  - name: shared-logs
    emptyDir: {}
```

## ConfigMaps and Secrets Deep Dive

```yaml
# Immutable ConfigMap (more efficient, cannot be changed)
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
immutable: true
data:
  application.properties: |
    server.port=8080
    cache.enabled=true
    cache.ttl=300
  # Can also set individual keys
  env: production
  log-level: INFO

# Immutable Secret (recommended for production)
apiVersion: v1
kind: Secret
metadata:
  name: app-secrets
immutable: true
type: Opaque
stringData:
  db-password: supersecret123
  api-key: abcdef123456
```

```yaml
# Pod consuming ConfigMap as environment variables
apiVersion: apps/v1
kind: Deployment
metadata:
  name: myapp
spec:
  template:
    spec:
      containers:
      - name: myapp
        env:
        # From individual keys
        - name: DB_URL
          valueFrom:
            configMapKeyRef:
              name: app-config
              key: db-url
        # All keys as env vars
        envFrom:
        - configMapRef:
            name: app-config
        - secretRef:
            name: app-secrets
        # Mount as volume
        volumeMounts:
        - name: config-volume
          mountPath: /etc/config
          readOnly: true
      volumes:
      - name: config-volume
        configMap:
          name: app-config
          items:
          - key: application.properties
            path: application.properties
```

## Services Deep Dive

```yaml
# ClusterIP (default) — internal cluster access only
apiVersion: v1
kind: Service
metadata:
  name: myapp-clusterip
spec:
  type: ClusterIP
  clusterIP: 10.96.0.100    # Optional: specify IP
  selector:
    app: myapp
  ports:
  - port: 80
    targetPort: 8080
    protocol: TCP

---
# NodePort — expose on each node's IP at a static port (30000-32767)
apiVersion: v1
kind: Service
metadata:
  name: myapp-nodeport
spec:
  type: NodePort
  selector:
    app: myapp
  ports:
  - port: 80
    targetPort: 8080
    nodePort: 30080          # Optional: let k8s assign if omitted

---
# LoadBalancer — creates cloud LB (AWS ELB, GCP LB, etc.)
apiVersion: v1
kind: Service
metadata:
  name: myapp-lb
spec:
  type: LoadBalancer
  selector:
    app: myapp
  ports:
  - port: 80
    targetPort: 8080
  loadBalancerSourceRanges:  # Restrict access
  - "10.0.0.0/8"
  - "203.0.113.0/24"

---
# ExternalName — DNS alias for external services
apiVersion: v1
kind: Service
metadata:
  name: external-db
spec:
  type: ExternalName
  externalName: mydb.aws.com   # Returns CNAME record

---
# Headless Service — for StatefulSets, direct pod DNS
apiVersion: v1
kind: Service
metadata:
  name: myapp-headless
spec:
  clusterIP: None              # No load balancing
  selector:
    app: myapp
  ports:
  - port: 8080
--- 
# DNS entries for headless service:
# myapp-headless.default.svc.cluster.local → all pod IPs
# pod-0.myapp-headless.default.svc.cluster.local → specific pod
```

## Ingress Controllers

```yaml
# nginx-ingress controller installation
# helm install ingress-nginx ingress-nginx/ingress-nginx

apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: myapp-ingress
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /$2
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    nginx.ingress.kubernetes.io/limit-rps: "10"
    nginx.ingress.kubernetes.io/proxy-body-size: 8m
    nginx.ingress.kubernetes.io/cors-enabled: "true"
    nginx.ingress.kubernetes.io/cors-allow-origin: "https://myapp.com"
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
spec:
  ingressClassName: nginx
  tls:
  - hosts:
    - api.myapp.com
    secretName: myapp-tls-secret
  rules:
  - host: api.myapp.com
    http:
      paths:
      - path: /api/users(/|$)(.*)
        pathType: ImplementationSpecific
        backend:
          service:
            name: user-service
            port:
              number: 80
      - path: /api/orders(/|$)(.*)
        pathType: ImplementationSpecific
        backend:
          service:
            name: order-service
            port:
              number: 80
      - path: /static(/|$)(.*)
        pathType: ImplementationSpecific
        backend:
          service:
            name: static-service
            port:
              number: 80
```

## Helm Chart Structure Expanded

```yaml
# Chart.yaml
apiVersion: v2
name: my-java-app
description: A Helm chart for deploying a Java Spring Boot application
type: application
version: 1.2.0
appVersion: "2.5.0"
keywords:
  - java
  - spring-boot
  - microservice
home: https://github.com/myorg/myapp
sources:
  - https://github.com/myorg/myapp
maintainers:
  - name: DevOps Team
    email: devops@myorg.com
dependencies:
  - name: postgresql
    version: "~12.0"
    repository: "https://charts.bitnami.com/bitnami"
    condition: postgresql.enabled
  - name: redis
    version: "~18.0"
    repository: "https://charts.bitnami.com/bitnami"
    condition: redis.enabled
```

```yaml
# values.yaml
replicaCount: 3

image:
  repository: myregistry/myapp
  tag: "2.5.0"
  pullPolicy: IfNotPresent
  pullSecrets: []

nameOverride: ""
fullnameOverride: ""

service:
  type: ClusterIP
  port: 80
  targetPort: 8080

ingress:
  enabled: true
  className: nginx
  annotations:
    cert-manager.io/cluster-issuer: letsencrypt-prod
  hosts:
    - host: api.myapp.com
      paths:
        - path: /
          pathType: Prefix
  tls:
  - secretName: myapp-tls
    hosts:
    - api.myapp.com

resources:
  requests:
    memory: "512Mi"
    cpu: "250m"
  limits:
    memory: "1Gi"
    cpu: "500m

autoscaling:
  enabled: true
  minReplicas: 2
  maxReplicas: 10
  targetCPUUtilizationPercentage: 75
  targetMemoryUtilizationPercentage: 85

env:
  SPRING_PROFILES_ACTIVE: "k8s"
  DB_URL: "jdbc:postgresql://postgres:5432/mydb"

config:
  application.properties: |
    cache.enabled=true
    cache.ttl=300

secrets:
  db-password: "changeme"
  jwt-secret: "changeme"

nodeSelector: {}
tolerations: []
affinity: {}

postgresql:
  enabled: false
  auth:
    database: mydb
    username: myuser

redis:
  enabled: false
  auth:
    enabled: true
```

```yaml
# templates/_helpers.tpl
{{- define "myapp.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "myapp.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{- define "myapp.labels" -}}
helm.sh/chart: {{ include "myapp.name" . }}-{{ .Chart.Version }}
{{ include "myapp.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{- define "myapp.selectorLabels" -}}
app.kubernetes.io/name: {{ include "myapp.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}
```

### Helm Hooks

```yaml
# templates/migration-job.yaml — runs before install/upgrade
apiVersion: batch/v1
kind: Job
metadata:
  name: "{{ .Release.Name }}-db-migrate"
  annotations:
    helm.sh/hook: pre-install,pre-upgrade
    helm.sh/hook-weight: "-5"
    helm.sh/hook-delete-policy: before-hook-creation,hook-succeeded
spec:
  template:
    spec:
      restartPolicy: Never
      containers:
      - name: migration
        image: flyway/flyway
        command: ["flyway", "migrate"]
        env:
        - name: FLYWAY_URL
          value: {{ .Values.config.dbUrl }}
        - name: FLYWAY_USER
          value: {{ .Values.config.dbUser }}
        - name: FLYWAY_PASSWORD
          valueFrom:
            secretKeyRef:
              name: {{ .Release.Name }}-secrets
              key: db-password
```

```bash
# Helm workflow
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update

# Install with custom values
helm install my-release ./my-java-app \
  -f values.yaml \
  -f values-prod.yaml \
  --set replicaCount=5 \
  --set image.tag=2.5.0 \
  --namespace production \
  --create-namespace \
  --wait \
  --timeout 10m

# Upgrade with atomic rollback
helm upgrade my-release ./my-java-app \
  --atomic \
  --cleanup-on-fail \
  --install \
  --reset-values

# Test release
helm test my-release
# This runs any tests/ pods in the chart

# See rendered templates
helm template ./my-java-app --debug

# Get values for a release
helm get values my-release
helm get manifest my-release
helm get notes my-release

# Manage repos
helm repo list
helm search repo myapp
helm dependency update ./my-java-app
helm dependency build ./my-java-app
```

1. Create a Docker image for a Spring Boot app and push to Docker Hub.
2. Write Deployment, Service, and ConfigMap YAML for the app.
3. Deploy to Minikube and verify with `kubectl get pods`.
4. Perform a rolling update to a new image version.
5. Create a Helm chart for the app and install it.
