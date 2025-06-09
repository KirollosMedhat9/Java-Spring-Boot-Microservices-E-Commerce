package com.ecommerce.productservice.service;



import com.ecommerce.productservice.controller.ProductController;
import com.ecommerce.productservice.dto.ProductDTO;
import com.ecommerce.productservice.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductDTO productDTO;

    @BeforeEach
    void setUp() {
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
    void createProduct_ShouldReturnCreatedProduct() throws Exception {
        when(productService.createProduct(any(ProductDTO.class))).thenReturn(productDTO);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.price").value(99.99));

        verify(productService, times(1)).createProduct(any(ProductDTO.class));
    }

    @Test
    void getProductById_ShouldReturnProduct() throws Exception {
        when(productService.getProductById(1L)).thenReturn(productDTO);

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Product"));

        verify(productService, times(1)).getProductById(1L);
    }

    @Test
    void getAllProducts_ShouldReturnProductList() throws Exception {
        List<ProductDTO> products = Arrays.asList(productDTO);
        when(productService.getAllProducts()).thenReturn(products);

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(productService, times(1)).getAllProducts();
    }

    @Test
    void updateProduct_ShouldReturnUpdatedProduct() throws Exception {
        when(productService.updateProduct(eq(1L), any(ProductDTO.class))).thenReturn(productDTO);

        mockMvc.perform(put("/api/v1/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(productService, times(1)).updateProduct(eq(1L), any(ProductDTO.class));
    }

    @Test
    void deleteProduct_ShouldReturnNoContent() throws Exception {
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/v1/products/1"))
                .andExpect(status().isNoContent());

        verify(productService, times(1)).deleteProduct(1L);
    }

    @Test
    void getProductsByCategory_ShouldReturnFilteredProducts() throws Exception {
        List<ProductDTO> products = Arrays.asList(productDTO);
        when(productService.getProductsByCategory("Electronics")).thenReturn(products);

        mockMvc.perform(get("/api/v1/products/category/Electronics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(productService, times(1)).getProductsByCategory("Electronics");
    }

    @Test
    void searchProducts_ShouldReturnMatchingProducts() throws Exception {
        List<ProductDTO> products = Arrays.asList(productDTO);
        when(productService.searchProducts("Test")).thenReturn(products);

        mockMvc.perform(get("/api/v1/products/search?name=Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(productService, times(1)).searchProducts("Test");
    }
}

