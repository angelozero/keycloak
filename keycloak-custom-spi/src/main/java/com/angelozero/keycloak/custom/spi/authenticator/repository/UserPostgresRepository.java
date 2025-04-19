package com.angelozero.keycloak.custom.spi.authenticator.repository;

import com.angelozero.keycloak.custom.spi.authenticator.dto.User;
import com.angelozero.keycloak.custom.spi.authenticator.exception.UserRepositoryException;
import com.angelozero.keycloak.custom.spi.authenticator.service.PasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserPostgresRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserPostgresRepository.class);

    private static final String JDBC_POSTGRESQL_URL = "jdbc:postgresql://postgres_container:5432/postgres";
    private static final String POSTGRESQL_USER = "admin";
    private static final String POSTGRESQL_PASSWORD = "admin";

    public static UserPostgresRepository getInstance() {
        return new UserPostgresRepository();
    }

    public User findByEmail(String email) {
        LOGGER.info("[UserPostgresRepository] - Find user by email: {}", email);

        try (var resultSet = findBy(email)) {

            if (resultSet.next()) {
                var id = resultSet.getInt("id");
                var firstName = resultSet.getString("first_name");
                var lastName = resultSet.getString("last_name");
                var userEmail = resultSet.getString("email");
                var userPassword = resultSet.getString("password");

                LOGGER.info("[UserPostgresRepository] - User found with success");
                LOGGER.info("ID -------------- {}", id);
                LOGGER.info("FIRST NAME ------ {}", firstName);
                LOGGER.info("EMAIL ----------- {}", userEmail);

                return new User(id, firstName, lastName, userEmail, userPassword);
            }

            LOGGER.info("[UserPostgresRepository] - No User was found with email {}", email);
            return null;

        } catch (SQLException ex) {
            LOGGER.error("[UserPostgresRepository] - Failed to find User - Error: {}", ex.getMessage());
            throw new UserRepositoryException("[UserPostgresRepository] - Failed to find User - Error: " + ex.getMessage());
        }
    }

    public void save(User user) {
        try {
            var connection = getConnection();

            var statement = connection.prepareStatement("INSERT INTO public.\"USER\" " +
                    "(id, first_name, last_name, email, \"password\") " +
                    "VALUES(nextval('\"User_id_seq\"'::regclass), " +
                    "?, ?, ?, ?)");

            statement.setString(1, user.firstName());
            statement.setString(2, user.lastName());
            statement.setString(3, user.email());
            statement.setString(4, PasswordService.generateHash(user.password()));

            int rowsInserted = statement.executeUpdate();

            if (rowsInserted > 0) {
                LOGGER.info("[UserPostgresRepository] - User saved with success");
            }

        } catch (Exception ex) {
            LOGGER.error("[UserPostgresRepository] - Failed to save user - Error: {}", ex.getMessage());
            throw new RuntimeException("[UserPostgresRepository] - Failed to save user - Error: " + ex.getMessage());
        }
    }

    private ResultSet findBy(String email) {
        var connection = getConnection();

        try {
            var statement = connection.prepareStatement("SELECT * FROM public.\"USER\" WHERE email = ?");
            statement.setString(1, email);

            return statement.executeQuery();

        } catch (Exception ex) {
            LOGGER.error("[UserPostgresRepository] - Failed to execute select query to find user by email {} - Error: {}", email, ex.getMessage());
            throw new UserRepositoryException("[UserPostgresRepository] - " +
                    "Failed to execute select query to find user by email" + email + " - Error: " + ex.getMessage());
        }
    }

    private Connection getConnection() {
        try {
            LOGGER.info("[UserPostgresRepository] - Getting connection into PostgresSQl database");
            return DriverManager.getConnection(JDBC_POSTGRESQL_URL, POSTGRESQL_USER, POSTGRESQL_PASSWORD);

        } catch (Exception ex) {
            LOGGER.error("[[UserPostgresRepository] - Failed to connect into the database - Error: {}", ex.getMessage());
            throw new UserRepositoryException("[UserPostgresRepository] - Failed to connect into the database - Error: " + ex.getMessage());
        }
    }
}
