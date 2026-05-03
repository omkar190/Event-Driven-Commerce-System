package com.order.service.repositories;

import com.order.service.entities.Product;
import com.order.service.mapper.ProductRowMapper;
import com.order.service.queries.ProductQueries;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public class ProductRepositoryImpl implements ProductRepository {

    private final DatabaseClient databaseClient;
    private final ProductRowMapper mapper = new ProductRowMapper();

    public ProductRepositoryImpl(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Flux<Product> findAll() {
        return databaseClient.sql(ProductQueries.FIND_ALL)
                .map(mapper)
                .all();
    }

    @Override
    public Mono<Product> findById(UUID id) {
        return databaseClient.sql(ProductQueries.FIND_BY_ID)
                .bind("id", id)
                .map(mapper)
                .one();
    }

    @Override
    public Mono<Long> reduceStock(UUID id, int quantity) {
        return databaseClient.sql(ProductQueries.REDUCE_STOCK)
                .bind("id", id)
                .bind("quantity", quantity)
                .fetch()
                .rowsUpdated();
    }
}