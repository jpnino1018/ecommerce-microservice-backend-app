package com.selimhorri.app.service;

import com.selimhorri.app.domain.Category;
import com.selimhorri.app.domain.Product;
import com.selimhorri.app.dto.CategoryDto;
import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.exception.wrapper.ProductNotFoundException;
import com.selimhorri.app.repository.CategoryRepository;
import com.selimhorri.app.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ProductServiceIntegrationTests {
    
    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    private ProductDto productDto;
    private Product product;
    private Category category;
    private CategoryDto categoryDto;

    private static final String DEFAULT_PRODUCT_TITLE = "Gaming Laptop";
    private static final String DEFAULT_IMAGE_URL = "http://example.com/laptop.jpg";
    private static final String DEFAULT_SKU = "LAP-2023-001";
    private static final Double DEFAULT_PRICE = 1299.99;
    private static final Integer DEFAULT_QUANTITY = 50;
    private static final String DEFAULT_CATEGORY_TITLE = "Electronics";

    private static final String UPDATED_PRODUCT_TITLE = "Gaming Laptop Pro";
    private static final Double UPDATED_PRICE = 1499.99;
    private static final Integer UPDATED_QUANTITY = 45;    @Autowired
    private CategoryRepository categoryRepository;
    
    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        // First save the category
        category = Category.builder()
                .categoryTitle(DEFAULT_CATEGORY_TITLE)
                .imageUrl(DEFAULT_IMAGE_URL)
                .build();
        category = categoryRepository.save(category);

        // Then create the DTOs with the saved category's ID
        categoryDto = CategoryDto.builder()
                .categoryId(category.getCategoryId())
                .categoryTitle(DEFAULT_CATEGORY_TITLE)
                .imageUrl(DEFAULT_IMAGE_URL)
                .build();

        productDto = ProductDto.builder()
                .productTitle(DEFAULT_PRODUCT_TITLE)
                .imageUrl(DEFAULT_IMAGE_URL)
                .sku(DEFAULT_SKU)
                .priceUnit(DEFAULT_PRICE)
                .quantity(DEFAULT_QUANTITY)
                .categoryDto(categoryDto)
                .build();

        // Create product entity with saved category
        product = Product.builder()
                .productTitle(DEFAULT_PRODUCT_TITLE)
                .imageUrl(DEFAULT_IMAGE_URL)
                .sku(DEFAULT_SKU)
                .priceUnit(DEFAULT_PRICE)
                .quantity(DEFAULT_QUANTITY)
                .category(category)
                .build();
    }

    @Test
    void saveProduct_shouldPersistProduct() {
        ProductDto savedProductDto = productService.save(productDto);

        assertThat(savedProductDto).isNotNull();
        assertThat(savedProductDto.getProductId()).isNotNull();
        assertThat(savedProductDto.getProductTitle()).isEqualTo(DEFAULT_PRODUCT_TITLE);
        assertThat(savedProductDto.getSku()).isEqualTo(DEFAULT_SKU);
        assertThat(savedProductDto.getPriceUnit()).isEqualTo(DEFAULT_PRICE);
        assertThat(savedProductDto.getQuantity()).isEqualTo(DEFAULT_QUANTITY);
        assertThat(savedProductDto.getCategoryDto().getCategoryTitle()).isEqualTo(DEFAULT_CATEGORY_TITLE);

        Optional<Product> repoProduct = productRepository.findById(savedProductDto.getProductId());
        assertThat(repoProduct).isPresent();
        assertThat(repoProduct.get().getSku()).isEqualTo(DEFAULT_SKU);
    }

    @Test
    void updateProduct_shouldModifyExistingProduct() {
        Product savedEntity = productRepository.saveAndFlush(product);

        ProductDto dtoToUpdate = ProductDto.builder()
                .productId(savedEntity.getProductId())
                .productTitle(UPDATED_PRODUCT_TITLE)
                .imageUrl(savedEntity.getImageUrl())
                .sku(savedEntity.getSku())
                .priceUnit(UPDATED_PRICE)
                .quantity(UPDATED_QUANTITY)
                .categoryDto(CategoryDto.builder()
                        .categoryId(savedEntity.getCategory().getCategoryId())
                        .categoryTitle(savedEntity.getCategory().getCategoryTitle())
                        .imageUrl(savedEntity.getCategory().getImageUrl())
                        .build())
                .build();

        ProductDto updatedDto = productService.update(dtoToUpdate);

        assertThat(updatedDto.getProductTitle()).isEqualTo(UPDATED_PRODUCT_TITLE);
        assertThat(updatedDto.getPriceUnit()).isEqualTo(UPDATED_PRICE);
        assertThat(updatedDto.getQuantity()).isEqualTo(UPDATED_QUANTITY);
    }

    @Test
    void deleteProductById_shouldRemoveProductFromDatabase() {
        Product savedEntity = productRepository.saveAndFlush(product);
        Integer productIdToDelete = savedEntity.getProductId();

        assertThat(productRepository.findById(productIdToDelete)).isPresent();

        productService.deleteById(productIdToDelete);

        assertThat(productRepository.findById(productIdToDelete)).isNotPresent();
    }

    @Test
    void findProductById_whenExists_shouldReturnProduct() {
        Product savedEntity = productRepository.saveAndFlush(product);
        Integer productIdToFind = savedEntity.getProductId();

        ProductDto foundDto = productService.findById(productIdToFind);

        assertThat(foundDto).isNotNull();
        assertThat(foundDto.getProductId()).isEqualTo(productIdToFind);
        assertThat(foundDto.getProductTitle()).isEqualTo(DEFAULT_PRODUCT_TITLE);
        assertThat(foundDto.getSku()).isEqualTo(DEFAULT_SKU);
    }

    @Test
    void findProductById_whenNotExists_shouldThrowProductNotFoundException() {
        Integer nonExistentId = -999;
        Exception exception = assertThrows(ProductNotFoundException.class, () -> {
            productService.findById(nonExistentId);
        });

        assertThat(exception.getMessage()).contains("Product with id: " + nonExistentId + " not found");
    }

    @Test
    void findAll_shouldReturnAllProducts() {
        productRepository.saveAndFlush(product); // Product 1        
        Category category2 = Category.builder()
                .categoryTitle("Accessories")
                .imageUrl("http://example.com/accessories.jpg")
                .build();
        category2 = categoryRepository.save(category2);

        Product product2 = Product.builder()
                .productTitle("Gaming Mouse")
                .imageUrl("http://example.com/mouse.jpg")
                .sku("MOU-2023-001")
                .priceUnit(79.99)
                .quantity(100)
                .category(category2)
                .build();

        productRepository.saveAndFlush(product2); // Product 2

        List<ProductDto> products = productService.findAll();

        assertThat(products).isNotNull();
        assertThat(products.size()).isEqualTo(2);
        assertThat(products.stream().anyMatch(p -> p.getProductTitle().equals(DEFAULT_PRODUCT_TITLE))).isTrue();
        assertThat(products.stream().anyMatch(p -> p.getProductTitle().equals("Gaming Mouse"))).isTrue();
    }
}
