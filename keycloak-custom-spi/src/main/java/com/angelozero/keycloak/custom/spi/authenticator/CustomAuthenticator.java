package com.angelozero.keycloak.custom.spi.authenticator;

import com.angelozero.keycloak.custom.spi.authenticator.dto.User;
import com.angelozero.keycloak.custom.spi.authenticator.exception.CustomAuthenticatorException;
import com.angelozero.keycloak.custom.spi.authenticator.repository.UserPostgresRepository;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomAuthenticator implements Authenticator {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomAuthenticator.class);

    public static final String PROVIDER_ID = "angelo-zero-custom-authenticator-id";

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        LOGGER.info("Initializing customized SPI");

        var userName = context.getHttpRequest().getDecodedFormParameters().getFirst("username");
        var password = context.getHttpRequest().getDecodedFormParameters().getFirst("password");

        LOGGER.info("USERNAME ---> {}", userName);
        LOGGER.info("PASSWORD ---> {}", password);

        if (userName.equals("admin") && password.equals("admin")) {
            context.success();
            return;
        }

        var userModel = findUserModel(context, userName);
        saveUser(userModel, userModel.getEmail(), password);

        LOGGER.info("User saved and authenticated with success");
        context.success();
    }

    @Override
    public void action(AuthenticationFlowContext context) {
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

    private void saveUser(UserModel userModel, String email, String password) {
        var repository = UserPostgresRepository.getInstance();
        var userFound = repository.findByEmailAndPassword(email, password);

        if (userFound == null) {
            var user = new User(null, userModel.getFirstName(), userModel.getLastName(), email, password);
            repository.save(user);
        }
    }

    private UserModel findUserModel(AuthenticationFlowContext context, String username) {
        UserModel userModel = null;

        var realm = context.getSession().getContext().getRealm();

        userModel = context.getSession().users().getUserByUsername(realm, username);

        if (userModel == null) {
            userModel = context.getSession().users().getUserByEmail(realm, username);
        }

        if (userModel == null) {
            LOGGER.error("User Model by info \"{}\" was not found", username);
            throw new CustomAuthenticatorException("User Model by info \"" + username + "\" was not found");
        }

        return userModel;
    }
}
