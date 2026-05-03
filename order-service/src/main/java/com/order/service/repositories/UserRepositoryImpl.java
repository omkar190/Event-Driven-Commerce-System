package com.order.service.repositories;

import com.order.service.entities.User;
import com.order.service.mapper.UserRowMapper;
import com.order.service.queries.UserQueries;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository("userRepositoryImpl")
public class UserRepositoryImpl implements UserRepository {

    private final DatabaseClient databaseClient;
    private final UserRowMapper mapper = new UserRowMapper();

    public UserRepositoryImpl(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<User> save(User user) {
        return databaseClient.sql(UserQueries.INSERT)
                .bind("id", user.getId())
                .bind("email", user.getEmail())
                .bind("password", user.getPassword())
                .bind("role", user.getRole())
                .bind("isActive", user.isActive())
                .bind("createdAt", user.getCreatedAt())
                .then()
                .thenReturn(user);
    }

    @Override
    public Mono<User> findByEmail(String email) {
        return databaseClient.sql(UserQueries.FIND_BY_EMAIL)
                .bind("email", email)
                .map(mapper)
                .one();
    }

    @Override
    public Mono<Boolean> existsByEmail(String email) {
        return databaseClient.sql(UserQueries.EXISTS_BY_EMAIL)
                .bind("email", email)
                .map(row -> row.get(0, Long.class))
                .one()
                .map(count -> count > 0);
    }
}