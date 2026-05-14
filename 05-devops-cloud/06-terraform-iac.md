# DevOps & Cloud — Lesson 6: Terraform & Infrastructure as Code

## Why Infrastructure as Code?

```
Manual (ClickOps):                       Infrastructure as Code:
┌──────────────────────────────┐        ┌──────────────────────────────┐
│ AWS Console                  │        │ main.tf                     │
│                              │        │                              │
│ 😊 Create VPC... click       │        │ resource "aws_vpc" "main" {   │
│ 😊 Create subnet... click    │        │   cidr_block = "10.0.0.0/16" │
│ 😊 Create security group...  │        │ }                            │
│ 😊 Create RDS... click       │        │                              │
│                              │        │ terraform apply              │
│ ❌ Can't reproduce           │        │ ✅ Repeatable, versioned     │
│ ❌ Can't review              │        │ ✅ Code review via PR        │
│ ❌ Drift over time           │        │ ✅ Always known state        │
│ ❌ "Works on my machine"     │        │ ✅ Works everywhere          │
└──────────────────────────────┘        └──────────────────────────────┘
```

## What is Terraform?

Terraform defines infrastructure as **declarative configuration**. You describe WHAT you want, Terraform figures out HOW.

```
┌─────────────────────────────────────────────────────────────┐
│                    TERRAFORM WORKFLOW                         │
│                                                               │
│  Write (.tf) ──▶ terraform init ──▶ terraform plan ──▶       │
│  HCL config       Initialize        Preview changes          │
│                   providers                                  │
│                                                               │
│  terraform apply ──▶ State file (terraform.tfstate)          │
│  Create/update      Tracks what exists                       │
│  infrastructure                                              │
└─────────────────────────────────────────────────────────────┘
```

## Terraform Basics

### Installation

```bash
# Install Terraform
wget -O- https://apt.releases.hashicorp.com/gpg | sudo gpg --dearmor -o /usr/share/keyrings/hashicorp-archive-keyring.gpg
echo "deb [signed-by=/usr/share/keyrings/hashicorp-archive-keyring.gpg] https://apt.releases.hashicorp.com $(lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/hashicorp.list
sudo apt update && sudo apt install terraform
```

### Main Configuration

```hcl
# main.tf — Define providers
terraform {
  required_version = ">= 1.5"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = "us-east-1"
}
```

## AWS Infrastructure with Terraform

### VPC and Networking

```hcl
# vpc.tf
resource "aws_vpc" "main" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = {
    Name = "java-app-vpc"
  }
}

resource "aws_subnet" "public" {
  count                   = 2
  vpc_id                  = aws_vpc.main.id
  cidr_block              = "10.0.${count.index}.0/24"
  availability_zone       = data.aws_availability_zones.available.names[count.index]
  map_public_ip_on_launch = true

  tags = {
    Name = "public-subnet-${count.index}"
  }
}

resource "aws_subnet" "private" {
  count             = 2
  vpc_id            = aws_vpc.main.id
  cidr_block        = "10.0.${count.index + 10}.0/24"
  availability_zone = data.aws_availability_zones.available.names[count.index]

  tags = {
    Name = "private-subnet-${count.index}"
  }
}

resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id

  tags = {
    Name = "main-igw"
  }
}
```

### RDS Database

```hcl
# database.tf
resource "aws_db_instance" "postgres" {
  identifier        = "java-app-db"
  engine            = "postgres"
  engine_version    = "16.3"
  instance_class    = "db.t3.medium"
  allocated_storage = 20

  db_name  = "myapp"
  username = "postgres"
  password = random_password.db_password.result

  vpc_security_group_ids = [aws_security_group.database.id]
  db_subnet_group_name   = aws_db_subnet_group.main.name

  backup_retention_period = 7
  backup_window           = "03:00-04:00"
  maintenance_window      = "sun:04:00-05:00"

  skip_final_snapshot = false
  final_snapshot_identifier = "java-app-db-final"

  tags = {
    Name = "java-app-postgres"
  }
}

resource "random_password" "db_password" {
  length  = 24
  special = false
}

# Store password in Secrets Manager
resource "aws_secretsmanager_secret" "db_password" {
  name = "db-password"
}

resource "aws_secretsmanager_secret_version" "db_password" {
  secret_id     = aws_secretsmanager_secret.db_password.id
  secret_string = random_password.db_password.result
}
```

### ECS Fargate (Docker)

```hcl
# ecs.tf
resource "aws_ecs_cluster" "main" {
  name = "java-app-cluster"
}

resource "aws_ecs_task_definition" "app" {
  family                   = "user-service"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "512"
  memory                   = "1024"
  execution_role_arn       = aws_iam_role.ecs_execution.arn

  container_definitions = jsonencode([
    {
      name  = "user-service"
      image = "${aws_ecr_repository.app.repository_url}:latest"
      portMappings = [
        {
          containerPort = 8080
          protocol      = "tcp"
        }
      ]
      environment = [
        {
          name  = "SPRING_PROFILES_ACTIVE"
          value = "prod"
        },
        {
          name  = "DB_HOST"
          value = aws_db_instance.postgres.address
        }
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = "/ecs/user-service"
          "awslogs-region"        = "us-east-1"
          "awslogs-stream-prefix" = "ecs"
        }
      }
    }
  ])
}

resource "aws_ecs_service" "app" {
  name            = "user-service"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.app.arn
  desired_count   = 2
  launch_type     = "FARGATE"

  network_configuration {
    subnets         = aws_subnet.private[*].id
    security_groups = [aws_security_group.app.id]
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.app.arn
    container_name   = "user-service"
    container_port   = 8080
  }
}
```

## Terraform Workflow

```bash
# 1. Initialize (download providers)
terraform init

# 2. Format and validate
terraform fmt
terraform validate

# 3. Preview changes
terraform plan -out=tfplan

# 4. Apply
terraform apply tfplan

# 5. Destroy when done
terraform destroy
```

## Remote State (S3 + DynamoDB)

Store state in a shared location so your team can collaborate:

```hcl
# backend.tf
terraform {
  backend "s3" {
    bucket         = "myapp-terraform-state"
    key            = "prod/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "terraform-state-lock"
    encrypt        = true
  }
}
```

```bash
# Create the backend first (manually)
aws s3 mb s3://myapp-terraform-state
aws dynamodb create-table \
  --table-name terraform-state-lock \
  --attribute-definitions AttributeName=LockID,AttributeType=S \
  --key-schema AttributeName=LockID,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST
```

## Terraform Workflow Expanded

```bash
# 1. Init — download providers and modules, init backend
terraform init                              # Basic init
terraform init -upgrade                     # Upgrade to latest provider versions
terraform init -backend-config=backend-prod.hcl  # Different backend config
terraform init -reconfigure                 # Reconfigure backend (discard cached)

# 2. Validate — check syntax and internal consistency
terraform fmt                               # Format all .tf files
terraform fmt -recursive                    # Recursive formatting
terraform validate                          # Validate configuration

# 3. Plan — preview changes
terraform plan                              # Show what will change
terraform plan -out=tfplan                  # Save plan to file
terraform plan -var="region=eu-west-1"      # Override variables
terraform plan -var-file=prod.tfvars        # Variable file
terraform plan -target=aws_instance.web     # Plan only specific resource
terraform plan -destroy                     # Plan what destroy would do

# 4. Apply — execute changes
terraform apply                             # Interactive (with prompt)
terraform apply tfplan                      # Apply saved plan (non-interactive)
terraform apply -auto-approve               # Non-interactive (CI/CD)
terraform apply -refresh-only               # Update state without changes

# 5. Destroy — tear everything down
terraform destroy                           # Destroy all resources
terraform destroy -target=aws_instance.web  # Destroy specific resource
terraform destroy -auto-approve             # Non-interactive

# State management
terraform state list                        # List all resources in state
terraform state show aws_instance.web       # Show resource in state
terraform state rm aws_instance.web         # Remove from state (not destroy)
terraform state mv module.old module.new    # Move resource in state
terraform state pull                        # Pull state to stdout
terraform state push                        # Push state (careful!)
terraform refresh                           # Sync state with real resources
terraform output                            # Show output values
terraform output -json                      # Outputs as JSON

# Debugging
TF_LOG=DEBUG terraform apply               # Debug logging
TF_LOG=TRACE terraform plan                # Trace-level logging
terraform console                           # Interactive console for expressions
```

## State Management — Remote State with Locking

```hcl
# backend.tf — S3 backend with DynamoDB locking
terraform {
  backend "s3" {
    bucket         = "myapp-terraform-state"
    key            = "prod/terraform.tfstate"
    region         = "us-east-1"
    encrypt        = true
    dynamodb_table = "terraform-state-lock"
  }
}
```

```bash
# Create the backend infrastructure
aws s3 mb s3://myapp-terraform-state --region us-east-1

# Enable versioning on the state bucket
aws s3api put-bucket-versioning \
  --bucket myapp-terraform-state \
  --versioning-configuration Status=Enabled

# Enable encryption
aws s3api put-bucket-encryption \
  --bucket myapp-terraform-state \
  --server-side-encryption-configuration '{
    "Rules": [{"ApplyServerSideEncryptionByDefault": {"SSEAlgorithm": "AES256"}}]
  }'

# Block public access
aws s3api put-public-access-block \
  --bucket myapp-terraform-state \
  --public-access-block-configuration \
  BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true

# Create DynamoDB table for locking
aws dynamodb create-table \
  --table-name terraform-state-lock \
  --attribute-definitions AttributeName=LockID,AttributeType=S \
  --key-schema AttributeName=LockID,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST

# Force unlock (if lock is stuck)
terraform force-unlock LOCK_ID
```

### Multiple Environment Backend Configs

```hcl
# backend-dev.hcl
bucket  = "myapp-terraform-state-dev"
key     = "dev/terraform.tfstate"
region  = "us-east-1"
encrypt = true

# backend-prod.hcl
bucket  = "myapp-terraform-state-prod"
key     = "prod/terraform.tfstate"
region  = "us-east-1"
encrypt = true
dynamodb_table = "terraform-state-lock-prod"
```

```bash
terraform init -backend-config=backend-dev.hcl -reconfigure
terraform workspace new dev
terraform plan -var-file=dev.tfvars
```

## Modules — Reusable Infrastructure Components

```hcl
# modules/vpc/main.tf
variable "vpc_cidr" {
  description = "CIDR block for VPC"
  type        = string
}

variable "environment" {
  description = "Environment name"
  type        = string
}

variable "public_subnet_cidrs" {
  description = "CIDR blocks for public subnets"
  type        = list(string)
}

variable "private_subnet_cidrs" {
  description = "CIDR blocks for private subnets"
  type        = list(string)
}

resource "aws_vpc" "main" {
  cidr_block           = var.vpc_cidr
  enable_dns_hostnames = true
  enable_dns_support   = true
  tags = {
    Name        = "${var.environment}-vpc"
    Environment = var.environment
  }
}

resource "aws_subnet" "public" {
  count                   = length(var.public_subnet_cidrs)
  vpc_id                  = aws_vpc.main.id
  cidr_block              = var.public_subnet_cidrs[count.index]
  availability_zone       = data.aws_availability_zones.available.names[count.index]
  map_public_ip_on_launch = true
  tags = {
    Name        = "${var.environment}-public-${count.index}"
    Environment = var.environment
  }
}

resource "aws_subnet" "private" {
  count             = length(var.private_subnet_cidrs)
  vpc_id            = aws_vpc.main.id
  cidr_block        = var.private_subnet_cidrs[count.index]
  availability_zone = data.aws_availability_zones.available.names[count.index]
  tags = {
    Name        = "${var.environment}-private-${count.index}"
    Environment = var.environment
  }
}

output "vpc_id" {
  value = aws_vpc.main.id
}

output "public_subnet_ids" {
  value = aws_subnet.public[*].id
}

output "private_subnet_ids" {
  value = aws_subnet.private[*].id
}
```

```hcl
# environments/prod/main.tf — using the VPC module
module "vpc" {
  source = "../../modules/vpc"

  vpc_cidr             = "10.0.0.0/16"
  environment          = "prod"
  public_subnet_cidrs  = ["10.0.1.0/24", "10.0.2.0/24", "10.0.3.0/24"]
  private_subnet_cidrs = ["10.0.10.0/24", "10.0.11.0/24", "10.0.12.0/24"]
}

module "rds" {
  source = "../../modules/rds"

  engine         = "postgres"
  engine_version = "16.3"
  instance_class = "db.t3.medium"
  allocated_storage = 20
  database_name  = "myapp"
  username       = "appuser"
  password       = var.db_password
  subnet_ids     = module.vpc.private_subnet_ids
  vpc_id         = module.vpc.vpc_id
  environment    = "prod"
}

# Module from the Terraform Registry
module "alb" {
  source  = "terraform-aws-modules/alb/aws"
  version = "~> 9.0"

  name    = "myapp-alb"
  vpc_id  = module.vpc.vpc_id
  subnets = module.vpc.public_subnet_ids

  security_group_ingress_rules = {
    "http" = {
      from_port   = 80
      to_port     = 80
      ip_protocol = "tcp"
      cidr_ipv4   = "0.0.0.0/0"
    }
    "https" = {
      from_port   = 443
      to_port     = 443
      ip_protocol = "tcp"
      cidr_ipv4   = "0.0.0.0/0"
    }
  }

  listeners = {
    http = {
      port     = 80
      protocol = "HTTP"
      forward = {
        target_group_key = "myapp"
      }
    }
  }

  target_groups = {
    myapp = {
      name              = "myapp-tg"
      backend_protocol  = "HTTP"
      backend_port      = 8080
      target_type       = "ip"
      health_check = {
        enabled             = true
        path                = "/actuator/health"
        healthy_threshold   = 2
        unhealthy_threshold = 3
        interval            = 30
      }
    }
  }
}
```

### Input/Output Variables

```hcl
# variables.tf — all input variables
variable "region" {
  description = "AWS region"
  type        = string
  default     = "us-east-1"
  validation {
    condition     = can(regex("^[a-z]{2}-[a-z]+-[0-9]+$", var.region))
    error_message = "Region must be a valid AWS region format."
  }
}

variable "instance_type" {
  description = "EC2 instance type"
  type        = string
  default     = "t3.medium"
}

variable "environment" {
  description = "Deployment environment"
  type        = string
  validation {
    condition     = contains(["dev", "staging", "prod"], var.environment)
    error_message = "Environment must be dev, staging, or prod."
  }
}

variable "tags" {
  description = "Common tags for all resources"
  type        = map(string)
  default = {
    ManagedBy = "Terraform"
    Project   = "MyApp"
  }
}

# outputs.tf
output "vpc_id" {
  description = "VPC ID"
  value       = module.vpc.vpc_id
}

output "database_endpoint" {
  description = "RDS endpoint"
  value       = module.rds.endpoint
  sensitive   = true
}

output "alb_dns_name" {
  description = "ALB DNS name"
  value       = module.alb.dns_name
}

output "connection_string" {
  value     = "jdbc:postgresql://${module.rds.endpoint}/myapp"
  sensitive = true
}
```

## Workspaces — Environment Separation

```bash
# Create and switch workspaces
terraform workspace new dev
terraform workspace new staging
terraform workspace new prod

# List and switch
terraform workspace list
terraform workspace show
terraform workspace select prod

# Workspace-based configuration
```

```hcl
# Use current workspace name in config
locals {
  environment = terraform.workspace
}

resource "aws_instance" "web" {
  instance_type = terraform.workspace == "prod" ? "t3.large" : "t3.micro"
  tags = {
    Name        = "web-${local.environment}"
    Environment = local.environment
  }
}
```

```bash
# Variable files per workspace
terraform workspace select dev
terraform apply -var-file=dev.tfvars

terraform workspace select prod
terraform apply -var-file=prod.tfvars
```

## AWS Provider with Complete Examples

```hcl
# Complete AWS example: EC2 + RDS + Load Balancer + Auto Scaling

# EC2 instance with user data
data "aws_ami" "amazon_linux" {
  most_recent = true
  owners      = ["amazon"]
  filter {
    name   = "name"
    values = ["amzn2-ami-hvm-*-x86_64-gp2"]
  }
}

resource "aws_instance" "web" {
  ami                    = data.aws_ami.amazon_linux.id
  instance_type          = "t3.medium"
  subnet_id              = module.vpc.public_subnet_ids[0]
  vpc_security_group_ids = [aws_security_group.web.id]
  key_name               = aws_key_pair.deployer.key_name

  user_data = <<-EOF
    #!/bin/bash
    yum update -y
    yum install -y docker
    systemctl start docker
    docker pull ${var.docker_image}
    docker run -d -p 8080:8080 ${var.docker_image}
  EOF

  root_block_device {
    volume_type = "gp3"
    volume_size = 30
    encrypted   = true
  }

  tags = {
    Name = "web-${local.environment}"
  }
}

# S3 bucket with versioning and encryption
resource "aws_s3_bucket" "app_data" {
  bucket = "myapp-data-${local.environment}-${data.aws_caller_identity.current.account_id}"
}

resource "aws_s3_bucket_versioning" "app_data" {
  bucket = aws_s3_bucket.app_data.id
  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "app_data" {
  bucket = aws_s3_bucket.app_data.id
  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

resource "aws_s3_bucket_public_access_block" "app_data" {
  bucket = aws_s3_bucket.app_data.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# RDS with read replica
resource "aws_db_instance" "primary" {
  identifier        = "myapp-db-${local.environment}"
  engine            = "postgres"
  engine_version    = "16.3"
  instance_class    = "db.t3.medium"
  allocated_storage = 20
  db_name           = "myapp"
  username          = "appuser"
  password          = random_password.db.result
  skip_final_snapshot = local.environment != "prod"
  backup_retention_period = local.environment == "prod" ? 30 : 7
  storage_encrypted = true
  deletion_protection = local.environment == "prod"
}

resource "aws_db_instance" "read_replica" {
  count                   = local.environment == "prod" ? 1 : 0
  identifier              = "myapp-db-read-${local.environment}"
  engine                  = "postgres"
  engine_version          = "16.3"
  instance_class          = "db.t3.medium"
  allocated_storage       = 20
  replicate_source_db     = aws_db_instance.primary.identifier
  skip_final_snapshot     = true
  storage_encrypted       = true
}
```

## Terraform Cloud / Enterprise

```hcl
# Backend for Terraform Cloud
terraform {
  cloud {
    organization = "mycompany"
    workspaces {
      name = "myapp-production"
      # Or use tags: tags = ["production"]
    }
  }
}

# VCS-driven workflow
# Connected to GitHub — automatically runs on PR:
# - terraform plan on PR creation
# - terraform apply on merge to main

# API-driven workflow with CLI
terraform login                            # Authenticate with TFC
terraform workspace show                   # Check current workspace
terraform plan                             # Remote execution by default

# Run tasks for policy enforcement (Sentinel)
# Sentinel policies can enforce:
# - Required tags on all resources
# - Allowed instance types only
# - Encryption must be enabled on all resources
# - Maximum cost per environment

# TFC Variables
terraform cloud variables set \
  --workspace myapp-production \
  --name "db_password" \
  --value "..." \
  --sensitive true
```

## Terraform Best Practices

| Practice | Why |
|----------|-----|
| Use modules | Reuse infrastructure components |
| Pin provider versions | Prevent unexpected changes |
| Use `terraform plan` always | Review changes before applying |
| Store state remotely | Collaboration and safety |
| Use workspaces | Manage multiple environments (dev/staging/prod) |
| Tag everything | Cost tracking, resource identification |
| Never edit state manually | Use `terraform state` commands instead |
| Use `prevent_destroy` for critical resources | Prevent accidental deletion |
| Use `precondition` and `postcondition` | Validate assumptions |
| Enable versioning on state bucket | Recover from corruption |
| Use `terraform_remote_state` data source | Share outputs across projects |
| Generate `terraform-docs` documentation | Auto-generate README from variables/outputs |

## Exercises

1. Install Terraform and configure the AWS provider.
2. Write Terraform to create a VPC with public and private subnets.
3. Add an RDS PostgreSQL instance with security group.
4. Deploy a Spring Boot app on ECS Fargate using Terraform.
5. Set up remote state in S3 with DynamoDB locking.
