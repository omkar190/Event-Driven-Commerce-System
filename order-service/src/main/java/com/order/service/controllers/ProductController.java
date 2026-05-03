package com.order.service.controllers;

import com.order.service.entities.Product;
import com.order.service.repositories.ProductRepository;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    public Flux<Product> getAllProducts() {
        return productRepository.findAll();
    }
}