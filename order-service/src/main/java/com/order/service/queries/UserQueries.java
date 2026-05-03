package com.order.service.queries;

public class UserQueries {

    public static final String INSERT = """
            INSERT INTO order_schema.users (id, email, password, country_code, mobile_number, role, is_active, created_at)
            VALUES (:id, :email, :password, :countryCode, :mobileNumber, :role, :isActive, :createdAt)
            """;

    public static final String FIND_BY_EMAIL = """
            SELECT * FROM order_schema.users WHERE email = :email
            """;

    public static final String EXISTS_BY_EMAIL = """
            SELECT COUNT(*) FROM order_schema.users WHERE email = :email
            """;
}