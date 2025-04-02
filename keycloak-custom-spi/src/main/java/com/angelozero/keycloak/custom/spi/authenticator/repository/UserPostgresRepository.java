package com.angelozero.keycloak.custom.spi.authenticator.repository;

import com.angelozero.keycloak.custom.spi.authenticator.dto.User;
import com.angelozero.keycloak.custom.spi.authenticator.exception.UserRepositoryException;
import com.angelozero.keycloak.custom.spi.authenticator.service.PasswordService;
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
            LOGGER.info("Connecting to the Postgres database - find by email and password method");
            var connection = DriverManager.getConnection(JDBC_POSTGRESQL_URL, POSTGRESQL_USER, POSTGRESQL_PASSWORD);

            var statement = connection.prepareStatement("SELECT * FROM public.\"USER\" WHERE email = ?");
            statement.setString(1, email);

            var resultSet = statement.executeQuery();

            LOGGER.info("Postgres database query executed with success");
            if (resultSet.next()) {

                var id = resultSet.getInt("id");
                var firstName = resultSet.getString("first_name");
                var lastName = resultSet.getString("last_name");
                var userEmail = resultSet.getString("email");
                var userPassword = resultSet.getString("password");

                var hashPassword = PasswordService.generateHash(password);

                if (!userPassword.equals(hashPassword)) {
                    throw new UserRepositoryException("The provided password does not match the stored password.");
                }

                LOGGER.info("User found with success");
                LOGGER.info("ID -------------- {}", id);
                LOGGER.info("FIRST NAME ------ {}", firstName);
                LOGGER.info("LAST NAME ------- {}", lastName);
                LOGGER.info("EMAIL ----------- {}", userEmail);
                LOGGER.info("PASSWORD -------- {}", password);
                LOGGER.info("HASH PASSWORD --- {}", hashPassword);

                return new User(id, firstName, lastName, userEmail, userPassword);
            }

            LOGGER.info("User in Postgres DataBase was not found");
            return null;

        } catch (Exception ex) {
            LOGGER.error("Failed to find User into the Postgres database - Error: {}", ex.getMessage());
            throw new RuntimeException("Failed to connect to the Postgres database - Error: " + ex.getMessage());
        }
    }

    public void save(User user) {
        try {
            LOGGER.info("Connecting to the Postgres database - save user method");
            var connection = DriverManager.getConnection(JDBC_POSTGRESQL_URL, POSTGRESQL_USER, POSTGRESQL_PASSWORD);

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
                LOGGER.info("User saved with success");
            }

        } catch (Exception ex) {
            LOGGER.error("Failed to connect to the Postgres database - Error: {}", ex.getMessage());
            throw new RuntimeException("Failed to connect to the Postgres database - Error: " + ex.getMessage());
        }
    }

    public static UserPostgresRepository getInstance() {
        return new UserPostgresRepository();
    }

}
