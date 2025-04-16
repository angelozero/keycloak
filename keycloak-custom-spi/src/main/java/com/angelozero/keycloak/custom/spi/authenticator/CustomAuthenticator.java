package com.angelozero.keycloak.custom.spi.authenticator;

import com.angelozero.keycloak.custom.spi.authenticator.dto.User;
import com.angelozero.keycloak.custom.spi.authenticator.exception.CustomAuthenticatorException;
import com.angelozero.keycloak.custom.spi.authenticator.repository.UserPostgresRepository;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.events.Details;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
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
        var clientMasterId = context.getAuthenticatorConfig().getConfig().get(CustomAuthenticationFactory.CLIENT_MASTER_ID);
        var clientMasterEnable = context.getAuthenticatorConfig().getConfig().get(CustomAuthenticationFactory.CLIENT_MASTER_ENABLE);

        LOGGER.info("USERNAME ---------------> {}", userName);
        LOGGER.info("PASSWORD ---------------> {}", password);
        LOGGER.info("CLIENT_MASTER_ID -------> {}", clientMasterId);
        LOGGER.info("CLIENT_MASTER_ENABLE ---> {}", clientMasterEnable);

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
        var email = context.getHttpRequest().getDecodedFormParameters().getFirst("username");
        LOGGER.info("\nUSER_NAME ---> {}", email);

        var password = context.getHttpRequest().getDecodedFormParameters().getFirst("password");
        LOGGER.info("\nPASSWORD ---> {}", password);

        var user = UserPostgresRepository.getInstance().findByEmailAndPassword(email, password);

        try {
            if (user != null) {
                var session = context.getSession();
                var realm = context.getRealm();
                var sessionUser = session.users().addUser(realm, email);

                sessionUser.setEnabled(true);
                sessionUser.setEmail(user.email());
                sessionUser.setFirstName(user.firstName());
                sessionUser.setLastName(user.lastName());

                context.setUser(sessionUser);

                context.getEvent().user(sessionUser);
                context.getEvent().success();
                context.newEvent().event(EventType.LOGIN);

                context.getEvent().client(context.getAuthenticationSession().getClient().getClientId())
                        .detail(Details.REDIRECT_URI, context.getAuthenticationSession().getRedirectUri())
                        .detail(Details.AUTH_METHOD, context.getAuthenticationSession().getProtocol());

                var authType = context.getAuthenticationSession().getAuthNote(Details.AUTH_TYPE);

                if (authType != null) {
                    context.getEvent().detail(Details.AUTH_TYPE, authType);
                }

                //var hashPassword = UserCredentialModel.password(user.getHashPassword(), Boolean.FALSE);

                // todo: userCredentialManager() v 19.x.x is deprecated / v 2x.x.x was removed
                //session.userCredentialManager().updateCredential(realm, sessionUser, hashPassword);
                sessionUser.addRequiredAction(UserModel.RequiredAction.UPDATE_PASSWORD);

                LOGGER.info("\nUser authenticated with success");
                context.success();

            } else {
                LOGGER.error("\nUser not found - Invalid User");
                var response = context.form()
                        .addError(new FormMessage("emailNotFound"))
                        .createForm("verifyUserNameForm");

                context.failureChallenge(AuthenticationFlowError.INVALID_USER, response);
            }

        } catch (Exception ex) {
            LOGGER.error("User authentication failed - Access Denied - Error: {}", ex.getMessage());
            context.failure(AuthenticationFlowError.ACCESS_DENIED);
        }
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
