package com.order.service.controllers;

import com.order.service.entities.Product;
import com.order.service.services.ProductCacheService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductCacheService productCacheService;

    public ProductController(ProductCacheService productCacheService) {
        this.productCacheService = productCacheService;
    }

    @GetMapping
    public Flux<Product> getAllProducts() {
        return productCacheService.getAllProducts();
    }
}