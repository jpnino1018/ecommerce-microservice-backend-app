# Ecommerce Microservice Backend App

This project is a microservices-based backend for an ecommerce platform. It leverages containerization, orchestration, and cloud infrastructure for scalable and maintainable deployments. Below is an overview of the main directories and files in the repository:

## Root-Level Files
- **azure-pipelines.yml**: Azure DevOps pipeline configuration.
- **compose.yml**: Docker Compose file for orchestrating services locally.
- **Jenkinsfile**: Jenkins pipeline configuration.
- **mvnw, mvnw.cmd**: Maven wrapper scripts for building Java projects.
- **pom.xml**: Parent Maven configuration for the multi-module project.
- **system.properties**: System properties for Java runtime.

## Microservice Directories
Each service follows a similar structure:
- **api-gateway/**: API Gateway service.
- **cloud-config/**: Centralized configuration server.
- **favourite-service/**: Manages user favourites.
- **order-service/**: Handles order processing.
- **payment-service/**: Manages payment transactions.
- **product-service/**: Manages product catalog.
- **proxy-client/**: Proxy client service.
- **service-discovery/**: Service registry/discovery (e.g., Eureka).
- **shipping-service/**: Handles shipping logistics.
- **user-service/**: Manages user accounts and authentication.

Each service contains:
  - `compose.yml`, `Dockerfile`: Containerization and orchestration configs.
  - `mvnw`, `mvnw.cmd`, `pom.xml`: Maven build files.
  - `system.properties`: Java system properties.
  - `src/`: Source code (`main/` for app code, `test/` for tests).
  - `target/`: Build output.

## Other Directories
- **e2e-tests/**: End-to-end testing resources, including Postman/Newman collections and Docker setup.
- **k8s/**: Kubernetes manifests for different environments:
  - `core/`, `dev/`, `prod/`, `stage/`: Environment-specific deployment files.
- **terraform/**: Infrastructure as Code (IaC) for cloud resources.
  - `main.tf`, `providers.tf`, `variables.tf`: Main Terraform configs.
  - `modules/`: Reusable Terraform modules.
- **src/**: Base code

## Hidden and CI/CD Directories
- **.github/**: GitHub Actions workflows for CI/CD.
- **.mvn/**: Maven wrapper support files.

## Notes
- Each microservice is independently deployable and testable.
- The project supports both local development (Docker Compose) and cloud-native deployments (Kubernetes, Terraform).

For more details, refer to the documentation within each service or configuration directory.
