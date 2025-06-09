package com.ecommerce.productservice.service;

import com.ecommerce.productservice.dto.ProductDTO;
import com.ecommerce.productservice.model.Product;
import com.ecommerce.productservice.repository.ProductRepository;
import com.ecommerce.productservice.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.ArgumentMatchers.*;
        import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private ProductDTO productDTO;

    @BeforeEach
    void setUp() {
        product = new Product(
                1L,
                "Test Product",
                "Test Description",
                new BigDecimal("99.99"),
                10,
                "Electronics",
                "http://example.com/image.jpg"
        );

        productDTO = new ProductDTO(
                1L,
                "Test Product",
                "Test Description",
                new BigDecimal("99.99"),
                10,
                "Electronics",
                "http://example.com/image.jpg"
        );
    }

    @Test
    void createProduct_ShouldReturnCreatedProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductDTO result = productService.createProduct(productDTO);

        assertNotNull(result);
        assertEquals(productDTO.getName(), result.getName());
        assertEquals(productDTO.getPrice(), result.getPrice());
        verify(productRepository, times(1)).save(any(Product.class));
        verify(kafkaTemplate, times(1)).send(eq("product.created"), eq("1"), any(Product.class));
    }

    @Test
    void getProductById_ShouldReturnProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductDTO result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals(product.getId(), result.getId());
        assertEquals(product.getName(), result.getName());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void getProductById_ShouldThrowException_WhenProductNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> productService.getProductById(1L));

        assertEquals("Product not found with ID: 1", exception.getMessage());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void getAllProducts_ShouldReturnProductList() {
        List<Product> products = Arrays.asList(product);
        when(productRepository.findAll()).thenReturn(products);

        List<ProductDTO> result = productService.getAllProducts();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(product.getName(), result.get(0).getName());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void updateProduct_ShouldReturnUpdatedProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductDTO result = productService.updateProduct(1L, productDTO);

        assertNotNull(result);
        assertEquals(productDTO.getName(), result.getName());
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
        verify(kafkaTemplate, times(1)).send(eq("product.updated"), eq("1"), any(Product.class));
    }

    @Test
    void deleteProduct_ShouldDeleteProduct() {
        when(productRepository.existsById(1L)).thenReturn(true);

        assertDoesNotThrow(() -> productService.deleteProduct(1L));

        verify(productRepository, times(1)).existsById(1L);
        verify(productRepository, times(1)).deleteById(1L);
        verify(kafkaTemplate, times(1)).send(eq("product.deleted"), eq("1"), eq(1L));
    }

    @Test
    void deleteProduct_ShouldThrowException_WhenProductNotFound() {
        when(productRepository.existsById(1L)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> productService.deleteProduct(1L));

        assertEquals("Product not found with ID: 1", exception.getMessage());
        verify(productRepository, times(1)).existsById(1L);
        verify(productRepository, never()).deleteById(1L);
    }

    @Test
    void getProductsByCategory_ShouldReturnFilteredProducts() {
        List<Product> products = Arrays.asList(product);
        when(productRepository.findByCategory("Electronics")).thenReturn(products);

        List<ProductDTO> result = productService.getProductsByCategory("Electronics");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productRepository, times(1)).findByCategory("Electronics");
    }

    @Test
    void searchProducts_ShouldReturnMatchingProducts() {
        List<Product> products = Arrays.asList(product);
        when(productRepository.findByNameContaining("Test")).thenReturn(products);

        List<ProductDTO> result = productService.searchProducts("Test");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productRepository, times(1)).findByNameContaining("Test");
    }
}
