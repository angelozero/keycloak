package com.angelozero.keycloak.custom.spi.authenticator;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;

public class CustomAuthenticator implements Authenticator {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomAuthenticator.class);

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        var username = context.getHttpRequest().getDecodedFormParameters().getFirst("username");
        LOGGER.info("\nUSER_NAME ---> {}", username);

        var password = context.getHttpRequest().getDecodedFormParameters().getFirst("password");
        LOGGER.info("\nPASSWORD ---> {}\n", password);

        try {
            LOGGER.info("\nRealizando consulta Postgres\n");
            Connection connection = DriverManager.getConnection(
                    "jdbc:postgresql://postgres_container:5432/postgres", "admin", "admin");

            var sqlQuery = "SELECT * FROM public.user WHERE email = ? AND password = ?";

            var statement = connection.prepareStatement(sqlQuery);

            statement.setString(1, username);
            statement.setString(2, password);

            var resultSet = statement.executeQuery();

            LOGGER.info("\nConsulta Postgres realizada com sucesso!\n");

            if (resultSet.next()) {

                LOGGER.info("\nUsuário encontrado com sucesso!\n");
                LOGGER.info("ID -------- {}", resultSet.getInt("id"));
                LOGGER.info("NOME ------ {}", resultSet.getString("name"));
                LOGGER.info("EMAIL ----- {}", resultSet.getString("email"));
                LOGGER.info("PASSWORD -- {}\n", resultSet.getString("password"));

//                var user = context.getSession().users().addUser(context.getRealm(), username);
//
//                user.setEnabled(true);
//                context.setUser(user);
                context.success();

            } else {
                LOGGER.error("Usuário não foi encontrado - Acesso Negado");
                context.failure(AuthenticationFlowError.ACCESS_DENIED);
            }

        } catch (Exception ex) {
            LOGGER.error("Falha ao se autenticar: {}", ex.getMessage());
            context.failure(AuthenticationFlowError.ACCESS_DENIED);
        }
    }

    @Override
    public void action(AuthenticationFlowContext authenticationFlowContext) {

    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {
        return false;
    }

    @Override
    public void setRequiredActions(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {

    }

    @Override
    public void close() {

    }
}
