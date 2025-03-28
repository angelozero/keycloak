package com.angelozero.keycloak.custom.spi.authenticator.repository;

import com.angelozero.keycloak.custom.spi.authenticator.dto.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.DriverManager;

public class UserPostgresRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserPostgresRepository.class);

    private static final String JDBC_POSTGRESQL_URL = "jdbc:postgresql://postgres_container:5432/postgres";
    private static final String POSTGRESQL_USER = "admin";
    private static final String POSTGRESQL_PASSWORD = "admin";

    public User findByEmailAndPassword(String email, String password) {

        try {
            LOGGER.info("\nConnecting to the Postgres database");
            var connection = DriverManager.getConnection(JDBC_POSTGRESQL_URL, POSTGRESQL_USER, POSTGRESQL_PASSWORD);

            var statement = connection.prepareStatement("SELECT * FROM public.USER WHERE email = ? AND password = ?");
            statement.setString(1, email);
            statement.setString(2, password);

            var resultSet = statement.executeQuery();

            LOGGER.info("\nPostgres database query executed with success");
            if (resultSet.next()) {

                var id = resultSet.getInt("id");
                var firstName = resultSet.getString("first_name");
                var lastName = resultSet.getString("last_name");
                var userEmail = resultSet.getString("email");
                var userPassword = resultSet.getString("password");

                LOGGER.info("\nUser found with success");
                LOGGER.info("\nID -------------- {}", id);
                LOGGER.info("FIRST NAME ------ {}", firstName);
                LOGGER.info("LAST NAME ------- {}", lastName);
                LOGGER.info("EMAIL ----------- {}", userEmail);
                LOGGER.info("PASSWORD -------- {}", userPassword);

                return new User(id, firstName, lastName, userEmail, userPassword);
            }

            LOGGER.info("\nUser not found");
            return null;

        } catch (Exception ex) {
            LOGGER.error("\nFailed to connect to the Postgres database - Error: {}", ex.getMessage());
            throw new RuntimeException("Failed to connect to the Postgres database - Error: " + ex.getMessage());
        }
    }

    public static UserPostgresRepository getInstance() {
        return new UserPostgresRepository();
    }
}
