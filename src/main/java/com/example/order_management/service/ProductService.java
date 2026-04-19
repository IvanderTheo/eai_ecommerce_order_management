package com.example.order_management.service;

import com.example.order_management.dto.ProductRequest;
import com.example.order_management.entity.Category;
import com.example.order_management.entity.Product;
import com.example.order_management.repository.CategoryRepository;
import com.example.order_management.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product createProduct(ProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setSku(request.getSku());
        
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Kategori tidak ditemukan"));
            product.setCategory(category);
        }
        
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, ProductRequest request) {
        return productRepository.findById(id)
                .map(product -> {
                    product.setName(request.getName());
                    product.setPrice(request.getPrice());
                    product.setStock(request.getStock());
                    product.setSku(request.getSku());
                    
                    if (request.getCategoryId() != null) {
                        Category category = categoryRepository.findById(request.getCategoryId())
                                .orElseThrow(() -> new RuntimeException("Kategori tidak ditemukan"));
                        product.setCategory(category);
                    }
                    
                    return productRepository.save(product);
                })
                .orElseThrow(() -> new RuntimeException("Produk tidak ditemukan"));
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Produk tidak ditemukan");
        }
        productRepository.deleteById(id);
    }
}
