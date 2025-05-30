package com.selimhorri.app.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.selimhorri.app.domain.Product;
import com.selimhorri.app.domain.Category;
import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.dto.CategoryDto;
import com.selimhorri.app.exception.wrapper.ProductNotFoundException;
import com.selimhorri.app.repository.ProductRepository;
import com.selimhorri.app.service.impl.ProductServiceImpl;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTests {
    
    @Mock
    private ProductRepository productRepository;
    
    @InjectMocks
    private ProductServiceImpl productService;
    
    private Product product;
    private ProductDto productDto;
    private Category category;
    private CategoryDto categoryDto;
    
    private static final String DEFAULT_PRODUCT_TITLE = "Test Product";
    private static final String DEFAULT_IMAGE_URL = "http://example.com/image.jpg";
    private static final String DEFAULT_SKU = "TEST-SKU-123";
    private static final Double DEFAULT_PRICE = 99.99;
    private static final Integer DEFAULT_QUANTITY = 100;
    private static final String DEFAULT_CATEGORY_TITLE = "Test Category";
    
    @BeforeEach
    void setup() {
        Instant now = Instant.now();
        
        categoryDto = CategoryDto.builder()
            .categoryId(1)
            .categoryTitle(DEFAULT_CATEGORY_TITLE)
            .imageUrl(DEFAULT_IMAGE_URL)
            .build();
            
        category = Category.builder()
            .categoryId(1)
            .categoryTitle(DEFAULT_CATEGORY_TITLE)
            .imageUrl(DEFAULT_IMAGE_URL)
            .build();

        productDto = ProductDto.builder()
            .productId(1)
            .productTitle(DEFAULT_PRODUCT_TITLE)
            .imageUrl(DEFAULT_IMAGE_URL)
            .sku(DEFAULT_SKU)
            .priceUnit(DEFAULT_PRICE)
            .quantity(DEFAULT_QUANTITY)
            .categoryDto(categoryDto)
            .build();
            
        product = Product.builder()
            .productId(1)
            .productTitle(DEFAULT_PRODUCT_TITLE)
            .imageUrl(DEFAULT_IMAGE_URL)
            .sku(DEFAULT_SKU)
            .priceUnit(DEFAULT_PRICE)
            .quantity(DEFAULT_QUANTITY)
            .category(category)
            .build();
            
        // Set audit fields
        product.setCreatedAt(now);
        product.setUpdatedAt(now);
        category.setCreatedAt(now);
        category.setUpdatedAt(now);
    }
    
    @Test
    void findAll_shouldReturnListOfProducts() {
        // Arrange
        when(productRepository.findAll()).thenReturn(Arrays.asList(product));
        
        // Act
        List<ProductDto> result = productService.findAll();
        
        // Assert
        assertNotNull(result, "Result list should not be null");
        assertEquals(1, result.size(), "Should return one product");
        assertEquals(DEFAULT_PRODUCT_TITLE, result.get(0).getProductTitle(), "Product title should match");
        assertEquals(DEFAULT_SKU, result.get(0).getSku(), "SKU should match");
        assertEquals(DEFAULT_PRICE, result.get(0).getPriceUnit(), "Price should match");
        
        // Verify category mapping
        assertNotNull(result.get(0).getCategoryDto(), "Category should not be null");
        assertEquals(DEFAULT_CATEGORY_TITLE, result.get(0).getCategoryDto().getCategoryTitle(), "Category title should match");
        
        verify(productRepository, times(1)).findAll();
    }
    
    @Test
    void findById_whenProductExists_shouldReturnProduct() {
        // Arrange
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        
        // Act
        ProductDto result = productService.findById(1);
        
        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.getProductId(), "Product ID should match");
        assertEquals(DEFAULT_PRODUCT_TITLE, result.getProductTitle(), "Product title should match");
        assertEquals(DEFAULT_SKU, result.getSku(), "SKU should match");
        assertEquals(DEFAULT_PRICE, result.getPriceUnit(), "Price should match");
        
        // Verify category mapping
        assertNotNull(result.getCategoryDto(), "Category should not be null");
        assertEquals(DEFAULT_CATEGORY_TITLE, result.getCategoryDto().getCategoryTitle(), "Category title should match");
        
        verify(productRepository, times(1)).findById(1);
    }
    
    @Test
    void findById_whenProductDoesNotExist_shouldThrowException() {
        // Arrange
        when(productRepository.findById(999)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ProductNotFoundException.class, () -> 
            productService.findById(999),
            "Should throw ProductNotFoundException"
        );
        verify(productRepository, times(1)).findById(999);
    }
    
    @Test
    void save_shouldReturnSavedProduct() {
        // Arrange
        when(productRepository.save(any(Product.class))).thenReturn(product);
        
        // Act
        ProductDto result = productService.save(productDto);
        
        // Assert
        assertNotNull(result, "Saved product should not be null");
        assertEquals(productDto.getProductId(), result.getProductId(), "Product ID should match");
        assertEquals(productDto.getProductTitle(), result.getProductTitle(), "Product title should match");
        assertEquals(productDto.getSku(), result.getSku(), "SKU should match");
        assertEquals(productDto.getPriceUnit(), result.getPriceUnit(), "Price should match");
        
        // Verify category mapping
        assertNotNull(result.getCategoryDto(), "Category should not be null");
        assertEquals(DEFAULT_CATEGORY_TITLE, result.getCategoryDto().getCategoryTitle(), "Category title should match");
        
        verify(productRepository, times(1)).save(any(Product.class));
    }
    
    @Test
    void update_shouldReturnUpdatedProduct() {
        // Arrange
        Product updatedProduct = Product.builder()
            .productId(1)
            .productTitle("Updated Product")
            .imageUrl(DEFAULT_IMAGE_URL)
            .sku(DEFAULT_SKU)
            .priceUnit(149.99)
            .quantity(DEFAULT_QUANTITY)
            .category(category)
            .build();
        updatedProduct.setCreatedAt(product.getCreatedAt());
        updatedProduct.setUpdatedAt(product.getUpdatedAt());
        
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);
        
        // Update the DTO
        productDto.setProductTitle("Updated Product");
        productDto.setPriceUnit(149.99);
        
        // Act
        ProductDto result = productService.update(productDto);
        
        // Assert
        assertNotNull(result, "Updated product should not be null");
        assertEquals("Updated Product", result.getProductTitle(), "Product title should be updated");
        assertEquals(149.99, result.getPriceUnit(), "Price should be updated");
        assertEquals(productDto.getSku(), result.getSku(), "SKU should remain unchanged");
        
        verify(productRepository, times(1)).save(any(Product.class));
    }
    
    @Test
    void deleteById_shouldCallRepositoryDeleteById() {
        // Arrange
        Integer productId = 1;
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        
        // Act
        productService.deleteById(productId);
        
        // Assert
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).delete(any(Product.class));
    }
}






