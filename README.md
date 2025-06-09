
# E-commerce Microservices Platform

A comprehensive microservices-based e-commerce system built with Spring Boot, featuring service discovery, API gateway, and asynchronous communication with Kafka.

## Architecture

- **Service Registry**: Netflix Eureka for service discovery
- **API Gateway**: Spring Cloud Gateway for request routing
- **Product Service**: Manages product catalog (MySQL)
- **Order Service**: Handles order processing (PostgreSQL) 
- **Inventory Service**: Manages stock levels

[//]: # (- **Notification Service**: Handles notifications via Kafka)

[//]: # (- **Auth Server**: OAuth2 authentication and authorization)

## Technologies

- Java 17
- Spring Boot 3.1.5
- Spring Cloud
- Apache Kafka
- MySQL & PostgreSQL
- Docker & Kubernetes
- Prometheus & Grafana
- JUnit & Mockito

## Quick Start

### Prerequisites
- Java 17
- Maven 3.6+
- Docker & Docker Compose

### Local Development

1. Start infrastructure services:
```bash
docker-compose up -d
```
This will start all dependencies needed 
And start the service-registry server and the api-gatewat

[//]: # (2. Build all services:)

[//]: # (```bash)

[//]: # (mvn clean install)

[//]: # (```)

2. Start services in order:

[//]: # (## Service Registry &#40;port 8761&#41;)

[//]: # (#)

[//]: # (# mvn spring-boot:run -pl service-registry)

[//]: # (#)

[//]: # (#)

[//]: # (## API Gateway &#40;port 8080&#41;)

[//]: # (#)
[//]: # (#mvn spring-boot:run -pl api-gateway)
```bash
# Product Service (port 8081)
mvn spring-boot:run -pl product-service

# Order Service (port 8082)
mvn spring-boot:run -pl order-service

# Inventory Service (port 8085)
mvn spring-boot:run -pl order-service
```

### Testing

Run unit tests:
```bash
mvn test
```

Run integration tests:
```bash
mvn verify
```
For quick test for: 
Adding Product ->
Checking Inventory -> 
Creating Order ->  
Detecting the inventory deducting
```bash
./test-communication.sh
```



### API Endpoints

#### Product Service (via API Gateway) Port 8081
- GET `/api/products` - Get all products
- GET `/api/products/{id}` - Get product by ID
- POST `/api/products` - Create product
- PUT `/api/products/{id}` - Update product
- DELETE `/api/products/{id}` - Delete product

#### Order Service (via API Gateway) Port 8082
- GET `/api/orders` - Get all orders
- GET `/api/orders/{id}` - Get order by ID
- POST `/api/orders` - Create order

### Monitoring
- Eureka Dashboard: http://localhost:8761
- API Gateway: http://localhost:8080
- Prometheus metrics: http://localhost:3000/actuator/prometheus

## Deployment

[//]: # (### Kubernetes)

[//]: # (Deploy to Kubernetes using the manifests in the `kubernetes/` directory:)

[//]: # (```bash)

[//]: # (kubectl apply -f kubernetes/)

[//]: # (```)

### GitLab CI/CD
The project includes `.gitlab-ci.yml` for automated CI/CD pipeline.

