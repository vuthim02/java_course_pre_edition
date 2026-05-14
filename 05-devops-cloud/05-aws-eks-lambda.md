# DevOps & Cloud — Lesson 5: AWS (EKS, Lambda, Core Services)

## Why AWS?

AWS is the most widely used cloud platform. As a Java engineer, you'll deploy, run, and manage Java applications on AWS daily.

```
┌─────────────────────────────────────────────────────────────┐
│                        AWS ECOSYSTEM                         │
│                                                               │
│  Compute        Storage        Database      Networking      │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐    │
│  │ EC2      │  │ S3       │  │ RDS      │  │ VPC      │    │
│  │ ECS      │  │ EBS      │  │ DynamoDB │  │ Route 53 │    │
│  │ EKS      │  │ EFS      │  │ Elasti-  │  │ Cloud-   │    │
│  │ Lambda   │  │ Glacier  │  │ cache    │  │ Front    │    │
│  │ Fargate  │  │          │  │ Neptune  │  │ ALB/NLB  │    │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘    │
│                                                               │
│  DevOps                       Security      Monitoring       │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐    │
│  │ Cloud-   │  │ Code-    │  │ IAM      │  │ Cloud-   │    │
│  │ Formation│  │ Deploy   │  │ KMS      │  │ Watch    │    │
│  │ CDK      │  │ Code-    │  │ Secrets  │  │ X-Ray    │    │
│  │ Terraform│  │ Pipeline │  │ Manager  │  │          │    │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘    │
└─────────────────────────────────────────────────────────────┘
```

## Core AWS Services for Java Developers

| Service | What It Does | Java Use Case |
|---------|-------------|---------------|
| **EC2** | Virtual machines in the cloud | Run any Java app (manual) |
| **ECS** | Docker container orchestration | Run Dockerized Java apps |
| **EKS** | Managed Kubernetes | Run K8s-based Java microservices |
| **Lambda** | Serverless functions | Run small Java functions without servers |
| **S3** | Object storage | Store files, images, backups |
| **RDS** | Managed relational databases | PostgreSQL, MySQL, Oracle for JPA |
| **DynamoDB** | NoSQL database | Key-value store for high-scale apps |
| **ElastiCache** | Managed Redis/Memcached | Caching layer for Spring Boot |
| **SQS** | Message queue | Async task processing |
| **SNS** | Pub/sub notifications | Event notifications |
| **CloudWatch** | Monitoring and logs | Log aggregation, metrics, alerts |

## EKS — Elastic Kubernetes Service

EKS is managed Kubernetes on AWS. You get a control plane for free; you pay only for worker nodes.

### eksctl — The CLI for EKS

```bash
# Create cluster
eksctl create cluster \
  --name my-java-cluster \
  --region us-east-1 \
  --nodegroup-name java-workers \
  --node-type t3.medium \
  --nodes 3 \
  --nodes-min 2 \
  --nodes-max 10 \
  --managed

# Update kubeconfig
aws eks update-kubeconfig --region us-east-1 --name my-java-cluster

# Verify
kubectl get nodes
```

### Deploying Java App to EKS

```bash
# Build and push to ECR (Elastic Container Registry)
aws ecr create-repository --repository-name user-service --region us-east-1

# Tag and push
docker tag user-service:1.0 \
  AWS_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/user-service:1.0
aws ecr get-login-password | docker login --password-stdin \
  -u AWS https://AWS_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com
docker push AWS_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/user-service:1.0

# Apply K8s manifests
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/ingress.yaml
```

### ALB Ingress Controller

```yaml
# ingress.yaml — Uses AWS ALB (Load Balancer)
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: myapp-ingress
  annotations:
    kubernetes.io/ingress.class: alb
    alb.ingress.kubernetes.io/scheme: internet-facing
    alb.ingress.kubernetes.io/target-type: ip
    alb.ingress.kubernetes.io/listen-ports: '[{"HTTP": 80}, {"HTTPS": 443}]'
    alb.ingress.kubernetes.io/certificate-arn: arn:aws:acm:us-east-1:...
spec:
  rules:
  - host: api.myapp.com
    http:
      paths:
      - path: /api/users
        pathType: Prefix
        backend:
          service:
            name: user-service
            port:
              number: 80
      - path: /api/orders
        pathType: Prefix
        backend:
          service:
            name: order-service
            port:
              number: 80
```

## Lambda — Serverless Java

Lambda runs your code without provisioning servers. You upload a JAR, AWS handles scaling.

```
Traditional:                          Lambda:
┌──────────┐  ┌──────────┐           ┌──────────────────────┐
│ Server   │  │ Server   │           │  Event → Function →  │
│ (24/7)   │  │ (24/7)   │           │  PAY PER INVOCATION  │
│ $50/mo   │  │ $50/mo   │           │  $0.000002 per call  │
└──────────┘  └──────────┘           └──────────────────────┘
  Idle 90% of time!                     Scalable to millions
```

### Java Lambda Function

```java
// Handler class
public class OrderHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final OrderService orderService = new OrderService();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent request, Context context) {

        try {
            String orderId = request.getPathParameters().get("id");
            Order order = orderService.findById(orderId);

            String json = new ObjectMapper().writeValueAsString(order);

            return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withBody(json)
                .withHeaders(Map.of("Content-Type", "application/json"));
        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                .withStatusCode(500)
                .withBody("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}
```

### Deployment

```xml
<!-- Maven plugin for Lambda -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>3.5.0</version>
    <executions>
        <execution>
            <phase>package</phase>
            <goals><goal>shade</goal></goals>
        </execution>
    </executions>
</plugin>
```

```bash
# Build fat JAR
mvn package

# Deploy via AWS CLI
aws lambda create-function \
  --function-name order-processor \
  --runtime java21 \
  --role arn:aws:iam::ACCOUNT:role/lambda-execution-role \
  --handler com.example.OrderHandler::handleRequest \
  --zip-file fileb://target/function.jar \
  --memory-size 512 \
  --timeout 30
```

### Lambda + API Gateway

```
API Gateway ──▶ Lambda ──▶ DynamoDB
     │              │           │
  HTTP Req       Java Code    NoSQL DB
```

```yaml
# serverless.yml (Serverless Framework)
service: order-service

provider:
  name: aws
  runtime: java21
  memorySize: 512
  timeout: 30

functions:
  createOrder:
    handler: com.example.CreateOrderHandler
    events:
      - http:
          path: /orders
          method: POST
  getOrder:
    handler: com.example.GetOrderHandler
    events:
      - http:
          path: /orders/{id}
          method: GET
```

## Spring Boot on ECS Fargate

Fargate runs containers without managing EC2 instances.

```json
// task-definition.json
{
  "family": "user-service",
  "networkMode": "awsvpc",
  "executionRoleArn": "arn:aws:iam::ACCOUNT:role/ecsTaskExecutionRole",
  "containerDefinitions": [{
    "name": "user-service",
    "image": "AWS_ACCOUNT.dkr.ecr.us-east-1.amazonaws.com/user-service:latest",
    "portMappings": [{
      "containerPort": 8080,
      "protocol": "tcp"
    }],
    "environment": [{
      "name": "SPRING_PROFILES_ACTIVE",
      "value": "prod"
    }],
    "logConfiguration": {
      "logDriver": "awslogs",
      "options": {
        "awslogs-group": "/ecs/user-service",
        "awslogs-region": "us-east-1",
        "awslogs-stream-prefix": "ecs"
      }
    }
  }]
}
```

## IAM — Identity and Access Management

Every AWS service needs permissions. Never hardcode credentials.

```java
// Use AWS SDK with IAM role (NOT hardcoded keys)
@Configuration
public class AwsConfig {

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
            .region(Region.US_EAST_1)
            .credentialsProvider(DefaultCredentialsProvider.create())  // Auto from IAM role
            .build();
    }

    @Bean
    public SqsClient sqsClient() {
        return SqsClient.builder()
            .region(Region.US_EAST_1)
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();
    }
}
```

## IAM Roles, Policies, and Trust Relationships Deep Dive

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:GetObject",
        "s3:PutObject",
        "s3:ListBucket"
      ],
      "Resource": [
        "arn:aws:s3:::myapp-data/*",
        "arn:aws:s3:::myapp-data"
      ]
    },
    {
      "Effect": "Deny",
      "Action": "s3:DeleteBucket",
      "Resource": "*"
    }
  ]
}
```

### Trust Policy (who can assume the role)

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": "ec2.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    },
    {
      "Effect": "Allow",
      "Principal": {
        "AWS": "arn:aws:iam::123456789012:role/deploy-role"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
```

### IAM Best Practices

```java
// AWS SDK with IAM role (no hardcoded keys)
@Configuration
public class AwsConfig {
    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
            .region(Region.US_EAST_1)
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();
    }

    @Bean
    public DynamoDbClient dynamoClient() {
        return DynamoDbClient.builder()
            .region(Region.US_EAST_1)
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();
    }
}
```

### IAM Policy Types

| Type | Description | Use Case |
|------|-------------|----------|
| **Identity-based** | Attached to users/groups/roles | Most common |
| **Resource-based** | Attached to resource (S3 bucket policy, Lambda resource policy) | Cross-account access |
| **Permission boundaries** | Max permissions a role can have | Delegating admin |
| **Service control policies** | Org-level max permissions | Organization guardrails |

## VPC Design

```hcl
# VPC with public/private subnets, NAT gateways, flow logs
resource "aws_vpc" "main" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_hostnames = true
  enable_dns_support   = true
}

# Public subnets (load balancers, NAT gateways)
resource "aws_subnet" "public" {
  count             = 2
  vpc_id            = aws_vpc.main.id
  cidr_block        = "10.0.${count.index}.0/24"
  availability_zone = data.aws_availability_zones.available.names[count.index]
  map_public_ip_on_launch = true
}

# Private subnets (application, database)
resource "aws_subnet" "private_app" {
  count             = 2
  vpc_id            = aws_vpc.main.id
  cidr_block        = "10.0.${count.index + 10}.0/24"
  availability_zone = data.aws_availability_zones.available.names[count.index]
}

resource "aws_subnet" "private_db" {
  count             = 2
  vpc_id            = aws_vpc.main.id
  cidr_block        = "10.0.${count.index + 20}.0/24"
  availability_zone = data.aws_availability_zones.available.names[count.index]
}

# NAT Gateway for private subnet internet access
resource "aws_eip" "nat" {
  domain = "vpc"
}

resource "aws_nat_gateway" "main" {
  allocation_id = aws_eip.nat.id
  subnet_id     = aws_subnet.public[0].id
}

resource "aws_route_table" "private" {
  vpc_id = aws_vpc.main.id
  route {
    cidr_block     = "0.0.0.0/0"
    nat_gateway_id = aws_nat_gateway.main.id
  }
}
```

### Security Groups vs NACLs

| Feature | Security Group (SG) | Network ACL (NACL) |
|---------|--------------------|--------------------|
| Scope | Instance-level | Subnet-level |
| Rules | Allow only | Allow + Deny |
| State | Stateful (return traffic auto-allowed) | Stateless (return traffic must be explicit) |
| Order | All rules evaluated | Rule number order (lowest first) |
| Use case | Fine-grained security | Subnet-level guardrails |

```hcl
# Security group — stateful, allow-only
resource "aws_security_group" "app" {
  name   = "app-sg"
  vpc_id = aws_vpc.main.id

  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["10.0.0.0/8"]  # Internal traffic only
  }

  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# NACL — stateless, deny-list bad actors
resource "aws_network_acl" "public" {
  vpc_id     = aws_vpc.main.id
  subnet_ids = aws_subnet.public[*].id

  ingress {
    rule_no    = 100
    from_port  = 80
    to_port    = 80
    protocol   = "tcp"
    cidr_block = "0.0.0.0/0"
    action     = "allow"
  }

  ingress {
    rule_no    = 200
    from_port  = 443
    to_port    = 443
    protocol   = "tcp"
    cidr_block = "0.0.0.0/0"
    action     = "allow"
  }

  egress {
    rule_no    = 100
    from_port  = 0
    to_port    = 0
    protocol   = "-1"
    cidr_block = "0.0.0.0/0"
    action     = "allow"
  }
}
```

## S3 Storage Classes and Lifecycle Policies

```bash
# S3 storage classes (cost vs access frequency)
# STANDARD — frequently accessed, millisecond access
# INTELLIGENT_TIERING — auto-move between tiers based on access
# STANDARD_IA — infrequent, but quick access when needed
# ONEZONE_IA — lower cost, single AZ (not for critical data)
# GLACIER — archival, minutes to hours retrieval
# DEEP_ARCHIVE — cheapest, 12-hour retrieval

# Lifecycle policy — transition to cheaper storage over time
aws s3api put-bucket-lifecycle-configuration \
  --bucket myapp-logs \
  --lifecycle-configuration '{
    "Rules": [
      {
        "Id": "transition-rule",
        "Status": "Enabled",
        "Filter": {"Prefix": "logs/"},
        "Transitions": [
          {"Days": 30, "StorageClass": "STANDARD_IA"},
          {"Days": 90, "StorageClass": "GLACIER"},
          {"Days": 365, "StorageClass": "DEEP_ARCHIVE"}
        ],
        "Expiration": {"Days": 730}
      }
    ]
  }'

# Presigned URLs — temporary access without credentials
# Java SDK
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import java.time.Duration;

S3Presigner presigner = S3Presigner.create();
GetObjectRequest getObject = GetObjectRequest.builder()
    .bucket("myapp-files")
    .key("reports/2026-05-12.pdf")
    .build();
GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
    .signatureDuration(Duration.ofHours(1))
    .getObjectRequest(getObject)
    .build();
String url = presigner.presignGetObject(presignRequest).url().toString();
// Share this URL — expires in 1 hour

# CLI
aws s3 presign s3://myapp-files/reports/2026-05-12.pdf --expires-in 3600
```

## EKS Cluster Creation with eksctl

```bash
# Production-grade EKS cluster with managed node groups and Fargate
eksctl create cluster \
  --name prod-java-cluster \
  --region us-east-1 \
  --version 1.29 \
  --vpc-cidr 10.0.0.0/16 \
  --vpc-private-subnets "10.0.1.0/24,10.0.2.0/24,10.0.3.0/24" \
  --vpc-public-subnets "10.0.101.0/24,10.0.102.0/24,10.0.103.0/24" \
  --nodegroup-name standard-workers \
  --node-type t3.large \
  --nodes 3 \
  --nodes-min 2 \
  --nodes-max 10 \
  --managed \
  --node-volume-size 50 \
  --node-volume-type gp3 \
  --ssh-access=false \
  --enable-ssm \
  --alb-ingress-controller \
  --asg-access \
  --full-ecr-access \
  --appmesh-access \
  --timeout 60m

# Add Fargate profile for serverless pods
eksctl create fargateprofile \
  --cluster prod-java-cluster \
  --name fargate-profile \
  --namespace fargate \
  --labels '{"usage":"serverless"}'

# Add node group with spot instances (save ~60%)
eksctl create nodegroup \
  --cluster prod-java-cluster \
  --name spot-workers \
  --node-type t3.large \
  --nodes 3 \
  --nodes-min 1 \
  --nodes-max 15 \
  --spot \
  --managed

# Update kubeconfig
aws eks update-kubeconfig --region us-east-1 --name prod-java-cluster

# Verify
kubectl get nodes -o wide
kubectl get pods -n kube-system
```

```yaml
# nodegroup.yaml — eksctl config file
apiVersion: eksctl.io/v1alpha5
kind: ClusterConfig

metadata:
  name: prod-java-cluster
  region: us-east-1
  version: "1.29"

vpc:
  cidr: "10.0.0.0/16"
  subnets:
    private:
      us-east-1a: { cidr: "10.0.1.0/24" }
      us-east-1b: { cidr: "10.0.2.0/24" }
      us-east-1c: { cidr: "10.0.3.0/24" }
    public:
      us-east-1a: { cidr: "10.0.101.0/24" }
      us-east-1b: { cidr: "10.0.102.0/24" }
      us-east-1c: { cidr: "10.0.103.0/24" }

managedNodeGroups:
  - name: standard-workers
    instanceType: t3.large
    minSize: 2
    maxSize: 10
    desiredCapacity: 3
    volumeSize: 50
    volumeType: gp3
    labels:
      role: worker
    tags:
      Environment: production

fargateProfiles:
  - name: fargate-default
    selectors:
      - namespace: fargate
      - namespace: default
        labels:
          schedule: fargate

cloudWatch:
  clusterLogging:
    enableTypes: ["api", "audit", "authenticator", "controllerManager", "scheduler"]
```

## Lambda Function Handlers and Layers

```java
// Lambda handler with AWS SDK, environment variables, VPC access
public class OrderProcessor implements RequestHandler<SQSEvent, Void> {

    private final DynamoDbClient dynamoDb;
    private final SnsClient snsClient;
    private static final Logger log = LoggerFactory.getLogger(OrderProcessor.class);

    public OrderProcessor() {
        // Initialize SDK clients (reused across invocations)
        dynamoDb = DynamoDbClient.builder()
            .region(Region.of(System.getenv("AWS_REGION")))
            .build();
        snsClient = SnsClient.builder()
            .region(Region.of(System.getenv("AWS_REGION")))
            .build();
    }

    @Override
    public Void handleRequest(SQSEvent event, Context context) {
        String orderTable = System.getenv("ORDERS_TABLE");
        String topicArn = System.getenv("ORDER_TOPIC_ARN");

        for (SQSEvent.SQSMessage msg : event.getRecords()) {
            try {
                Order order = new ObjectMapper().readValue(msg.getBody(), Order.class);

                // Process order
                dynamoDb.putItem(PutItemRequest.builder()
                    .tableName(orderTable)
                    .item(OrderMapper.toItem(order))
                    .build());

                // Publish notification
                snsClient.publish(PublishRequest.builder()
                    .topicArn(topicArn)
                    .message("Order processed: " + order.getId())
                    .build());

                log.info("Processed order: {}", order.getId());
            } catch (Exception e) {
                log.error("Failed to process order: {}", msg.getBody(), e);
                // Will cause SQS to retry (depending on redrive policy)
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}
```

### Lambda Layers (shared dependencies)

```xml
<!-- pom.xml — build layer ZIP -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-assembly-plugin</artifactId>
    <configuration>
        <descriptorRefs>
            <descriptorRef>jar-with-dependencies</descriptorRef>
        </descriptorRefs>
    </configuration>
</plugin>
```

```bash
# Create a Lambda layer
mkdir -p java/lib
cp target/function-deps.jar java/lib/
zip -r layer.zip java/

# Publish layer
aws lambda publish-layer-version \
  --layer-name java-common-deps \
  --zip-file fileb://layer.zip \
  --compatible-runtimes java21 \
  --compatible-architectures x86_64 arm64

# Attach to function
aws lambda update-function-configuration \
  --function-name order-processor \
  --layers arn:aws:lambda:us-east-1:ACCOUNT:layer:java-common-deps:1
```

### Lambda Environment Variables and VPC Access

```bash
# Create function with VPC access
aws lambda create-function \
  --function-name order-processor \
  --runtime java21 \
  --role arn:aws:iam::ACCOUNT:role/lambda-execution-role \
  --handler com.example.OrderProcessor::handleRequest \
  --zip-file fileb://target/function.zip \
  --memory-size 1024 \
  --timeout 60 \
  --vpc-config SubnetIds=subnet-xxx,subnet-yyy,SecurityGroupIds=sg-zzz \
  --environment Variables={ORDERS_TABLE=prod-orders,ORDER_TOPIC_ARN=arn:aws:sns:us-east-1:ACCOUNT:order-events} \
  --tracing-config Mode=Active

# Lambda + RDS Proxy for connection pooling with RDS in VPC
# Use RDS Proxy endpoint instead of direct RDS endpoint
# spring.datasource.url=jdbc:postgresql://rds-proxy.proxy-xxx.us-east-1.rds.amazonaws.com:5432/mydb
```

## Step Functions — Workflow Orchestration

```json
{
  "Comment": "Order processing workflow",
  "StartAt": "ValidateOrder",
  "States": {
    "ValidateOrder": {
      "Type": "Task",
      "Resource": "arn:aws:lambda:us-east-1:ACCOUNT:function:validate-order",
      "Next": "ProcessPayment"
    },
    "ProcessPayment": {
      "Type": "Task",
      "Resource": "arn:aws:lambda:us-east-1:ACCOUNT:function:process-payment",
      "Next": "CheckFraud",
      "Retry": [
        {
          "ErrorEquals": ["Lambda.ServiceException", "Lambda.AWSLambdaException"],
          "IntervalSeconds": 2,
          "MaxAttempts": 3,
          "BackoffRate": 2
        }
      ],
      "Catch": [
        {
          "ErrorEquals": ["PaymentFailed"],
          "Next": "NotifyFailure"
        }
      ]
    },
    "CheckFraud": {
      "Type": "Task",
      "Resource": "arn:aws:lambda:us-east-1:ACCOUNT:function:fraud-detection",
      "Next": "UpdateInventory",
      "Choices": [
        {
          "Variable": "$.fraudScore",
          "NumericGreaterThan": 80,
          "Next": "FlagForReview"
        }
      ]
    },
    "UpdateInventory": {
      "Type": "Task",
      "Resource": "arn:aws:lambda:us-east-1:ACCOUNT:function:update-inventory",
      "Next": "SendConfirmation"
    },
    "SendConfirmation": {
      "Type": "Task",
      "Resource": "arn:aws:lambda:us-east-1:ACCOUNT:function:send-email",
      "End": true
    },
    "FlagForReview": {
      "Type": "Task",
      "Resource": "arn:aws:lambda:us-east-1:ACCOUNT:function:flag-review",
      "Next": "NotifyFailure"
    },
    "NotifyFailure": {
      "Type": "Task",
      "Resource": "arn:aws:lambda:us-east-1:ACCOUNT:function:notify-failure",
      "End": true
    }
  }
}
```

```bash
# Create and execute state machine
aws stepfunctions create-state-machine \
  --name order-workflow \
  --definition file://workflow.json \
  --role-arn arn:aws:iam::ACCOUNT:role/step-functions-role

aws stepfunctions start-execution \
  --state-machine-arn arn:aws:states:us-east-1:ACCOUNT:stateMachine:order-workflow \
  --input '{"orderId": "123", "amount": 150.00, "userId": "user-456"}'
```

## Exercises

1. Create an S3 bucket and upload a file using the AWS Java SDK.
2. Deploy a Spring Boot app as a Lambda function behind API Gateway.
3. Create an EKS cluster with eksctl, deploy a Java app with 3 replicas.
4. Set up CloudWatch logging and create a metric filter for ERROR logs.
5. Configure IAM roles with least-privilege for a Lambda function.
