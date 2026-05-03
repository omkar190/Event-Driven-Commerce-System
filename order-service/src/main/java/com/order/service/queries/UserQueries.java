package com.order.service.queries;

public class UserQueries {

    public static final String INSERT = """
            INSERT INTO users (id, email, password, role, is_active, created_at)
            VALUES (:id, :email, :password, :role, :isActive, :createdAt)
            """;

    public static final String FIND_BY_EMAIL = """
            SELECT * FROM users WHERE email = :email
            """;

    public static final String EXISTS_BY_EMAIL = """
            SELECT COUNT(*) FROM users WHERE email = :email
            """;
}