
# E-commerce Microservices Platform

A comprehensive microservices-based e-commerce system built with Spring Boot, featuring service discovery, API gateway, and asynchronous communication with Kafka.

## Architecture

- **Service Registry**: Netflix Eureka for service discovery
- **API Gateway**: Spring Cloud Gateway for request routing
- **Product Service**: Manages product catalog (MySQL)
- **Order Service**: Handles order processing (PostgreSQL) 
- **Inventory Service**: Manages stock levels
- **Notification Service**: Handles notifications via Kafka
- **Auth Server**: OAuth2 authentication and authorization

## Technologies

- Java 17
- Spring Boot 3.1.5
- Spring Cloud 2022.0.4
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

2. Build all services:
```bash
mvn clean install
```

3. Start services in order:
```bash
# Service Registry (port 8761)
mvn spring-boot:run -pl service-registry

# API Gateway (port 8080)
mvn spring-boot:run -pl api-gateway

# Product Service (port 8081)
mvn spring-boot:run -pl product-service

# Order Service (port 8082)
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

### API Endpoints

#### Product Service (via API Gateway)
- GET `/api/products` - Get all products
- GET `/api/products/{id}` - Get product by ID
- POST `/api/products` - Create product
- PUT `/api/products/{id}` - Update product
- DELETE `/api/products/{id}` - Delete product

#### Order Service (via API Gateway)
- GET `/api/orders` - Get all orders
- GET `/api/orders/{id}` - Get order by ID
- POST `/api/orders` - Create order

### Monitoring
- Eureka Dashboard: http://localhost:8761
- API Gateway: http://localhost:8080
- Prometheus metrics: http://localhost:{port}/actuator/prometheus

## Deployment

### Kubernetes
Deploy to Kubernetes using the manifests in the `kubernetes/` directory:

```bash
kubectl apply -f kubernetes/
```

### GitLab CI/CD
The project includes `.gitlab-ci.yml` for automated CI/CD pipeline.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License.
