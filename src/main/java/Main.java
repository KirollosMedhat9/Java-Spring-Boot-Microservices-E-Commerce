
public class Main {
    public static void main(String[] args) {
        System.out.println("E-commerce Microservices Platform");
        System.out.println("This is the parent project. Run individual microservices:");
        System.out.println("- Service Registry: mvn spring-boot:run -pl service-registry");
        System.out.println("- API Gateway: mvn spring-boot:run -pl api-gateway");
        System.out.println("- Product Service: mvn spring-boot:run -pl product-service");
        System.out.println("- Order Service: mvn spring-boot:run -pl order-service");
        System.out.println("");
        System.out.println("Or use docker-compose up to start infrastructure");
    }
}
