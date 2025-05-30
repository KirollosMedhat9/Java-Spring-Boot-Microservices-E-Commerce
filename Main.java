@ExtendWith(MockitoExtension.class)
        class ProductServiceTest {
            @Mock
            private ProductRepository productRepository;
            @Mock
            private KafkaTemplate<String, Object> kafkaTemplate;
            @InjectMocks
            private ProductServiceImpl productService;

            @Test
            void testCreateProduct() {
                // Given
                ProductDTO productDTO = new ProductDTO("Laptop", "High-end laptop", BigDecimal.valueOf(1200.00), 10);
                Product product = new Product(); // map DTO to entity
                when(productRepository.save(any(Product.class))).thenReturn(product);

                // When
                ProductDTO createdProduct = productService.createProduct(productDTO);

                // Then
                assertNotNull(createdProduct);
                // Add more assertions
                verify(productRepository, times(1)).save(any(Product.class));
                // verify(kafkaTemplate, times(1)).send(eq("product.created"), any(ProductEvent.class));
            }
        }

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
        @AutoConfigureMockMvc
        @ActiveProfiles("test") // Use a test application.yml
        class ProductControllerIntegrationTest {
            @Autowired
            private MockMvc mockMvc;

            @MockBean // Mocks the actual repository to avoid DB dependency for this controller test
            private ProductRepository productRepository;

            // Or if you want to test with a real (test) DB:
            // @Autowired private ProductRepository productRepository;
            // @Sql("/test-data.sql") // to load initial data

            @Test
            void shouldGetProductById() throws Exception {
                Product product = new Product(1L, "Test Product", "Description", BigDecimal.TEN, 5);
                when(productRepository.findById(1L)).thenReturn(Optional.of(product));

                mockMvc.perform(get("/api/v1/products/1"))
                       .andExpect(status().isOk())
                       .andExpect(jsonPath("$.name").value("Test Product"));
            }
        }

// Sources:
// 1. https://www.stackextend.com/angular/websocket-with-spring-boot-and-angular/
// 2. https://blog.karthisoftek.com/a?ID=01600-6bdfc9c6-75a2-4ff7-9973-5fb1761d9c8b
// 3. https://github.com/daramahesh/MICROSERVICES-TUTORIAL
// 4. https://statusneo.com/kafka-with-spring-boot-using-docker-compose/
// 5. https://runebook.dev/en/articles/spring_boot/application-properties/application-properties.actuator.management.prometheus.metrics.export.descriptions
// 6. https://stackoverflow.com/questions/35217603/kafka-python-consumer-not-receiving-messages
// 7. https://forum.confluent.io/t/unknown-topic-or-partition-error-even-though-the-topic-should-be-created-automatically/2049
// 8. https://anyflip.com/ulhe/vvwd/basic
// 9. https://forums.docker.com/t/cant-connect-to-postgres-from-another-python-container-all-started-from-same-docker-compose/138146
// 10. https://seonjun0906.tistory.com/entry/SpringBootTest-vs-Mock
// 11. https://github.com/j-e-0/scaffold-hexagonal-pattern-java
// 12. https://github.com/alecthegeek/gitlab-cicd-crash-course-glab subject to MIT
// 13. https://www.blackcoffeerobotics.com/blog/our-devops-pipeline-for-a-heterogeneous-fleet-of-robots
// 14. https://cylab.be/blog/331/gitlab-fix-cgroups-cgroup-mountpoint-does-not-exist-unknown