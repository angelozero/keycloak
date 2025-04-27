package com.angelozero.keycloak.custom.spi.repository;

import com.angelozero.keycloak.custom.spi.dto.User;
import com.angelozero.keycloak.custom.spi.exception.UserRepositoryException;
import com.angelozero.keycloak.custom.spi.service.PasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

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
                var interests = Arrays.asList((String[]) resultSet.getArray("interests").getArray());
                var userEmail = resultSet.getString("email");
                var userPassword = resultSet.getString("password");

                LOGGER.info("[UserPostgresRepository] - User found with success");
                LOGGER.info("[UserPostgresRepository] - ID -------------- {}", id);
                LOGGER.info("[UserPostgresRepository] - FIRST NAME ------ {}", firstName);
                LOGGER.info("[UserPostgresRepository] - EMAIL ----------- {}", userEmail);
                LOGGER.info("[UserPostgresRepository] - INTERESTS ------- {}", interests);

                return new User(id, firstName, lastName, interests, userEmail, userPassword);
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

            var interestsArray = connection.createArrayOf("text", user.interests().toArray(new String[0]));

            var statement = connection.prepareStatement("""
                INSERT INTO public."USER"
                (id, first_name, last_name, interests, email, "password")
                VALUES(nextval('"USER_id_seq"'::regclass), ?, ?, ?, ?, ?);
                """);

            statement.setString(1, user.firstName());
            statement.setString(2, user.lastName());
            statement.setArray(3, interestsArray);
            statement.setString(4, user.email());
            statement.setString(5, PasswordService.generateHash(user.password()));

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
