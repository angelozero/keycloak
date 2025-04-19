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

    public static final String CUSTOM_AUTHENTICATOR_PROVIDER_ID = "angelo-zero-custom-authenticator-id";
    public static final String CUSTOM_CLIENT_MASTER_ID = "CUSTOM_CLIENT_MASTER_ID";
    public static final String CUSTOM_CLIENT_MASTER_ENABLE = "CUSTOM_CLIENT_MASTER_ENABLE";

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        LOGGER.info("[CustomAuthenticator] - Custom Authenticator SPI");

        var userName = context.getHttpRequest().getDecodedFormParameters().getFirst("username");
        var password = context.getHttpRequest().getDecodedFormParameters().getFirst("password");
        var clientMasterId = context.getAuthenticatorConfig() != null ?
                context.getAuthenticatorConfig().getConfig().get(CUSTOM_CLIENT_MASTER_ID) : 0;
        var clientMasterEnable = context.getAuthenticatorConfig() != null ?
                context.getAuthenticatorConfig().getConfig().get(CUSTOM_CLIENT_MASTER_ENABLE) : true;

        LOGGER.info("[CustomAuthenticator] - User request dada info:");
        LOGGER.info("USERNAME ---------------> {}", userName);
        LOGGER.info("PASSWORD ---------------> {}", password);
        LOGGER.info("CLIENT_MASTER_ID -------> {}", clientMasterId);
        LOGGER.info("CLIENT_MASTER_ENABLE ---> {}", clientMasterEnable);

        if (userName.equals("admin") && password.equals("admin")) {
            LOGGER.info("[CustomAuthenticator] - User ADMIN authenticated with success");
            context.success();
            return;
        }

        var userModel = findUserModel(context, userName);
        saveUser(userModel, userModel.getEmail(), password);

        LOGGER.info("[CustomAuthenticator] - User \"{}\" authenticated with success", userName);
        context.success();
    }

    private void saveUser(UserModel userModel, String email, String password) {
        var repository = UserPostgresRepository.getInstance();
        var userFound = repository.findByEmail(email);

        if (userFound == null) {
            var user = new User(null, userModel.getFirstName(), userModel.getLastName(), email, password);
            repository.save(user);
        }
    }

    private UserModel findUserModel(AuthenticationFlowContext context, String username) {
        UserModel userModel;

        var realm = context.getSession().getContext().getRealm();

        userModel = context.getSession().users().getUserByUsername(realm, username);

        if (userModel == null) {
            userModel = context.getSession().users().getUserByEmail(realm, username);
        }

        if (userModel == null) {
            LOGGER.error("[CustomAuthenticator] - User Model by info \"{}\" was not found", username);
            throw new CustomAuthenticatorException("User Model by info \"" + username + "\" was not found");
        }

        return userModel;
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
}
