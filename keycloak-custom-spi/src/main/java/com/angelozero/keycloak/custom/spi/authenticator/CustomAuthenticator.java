package com.angelozero.keycloak.custom.spi.authenticator;

import com.angelozero.keycloak.custom.spi.authenticator.repository.UserPostgresRepository;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.events.Details;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomAuthenticator implements Authenticator {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomAuthenticator.class);

    public static final String PROVIDER_ID = "angelo-zero-custom-authenticator-id";
    public static final String VERIFY_USERNAME_FORM = "verify-username.ftl";
    public static final String EMAIL_NOT_FOUND = "emailNotFoundMessage";

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        var challenge = context.form().createForm(VERIFY_USERNAME_FORM);
        context.challenge(challenge);
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

                var hashPassword = UserCredentialModel.password(user.getHashPassword(), Boolean.FALSE);

                // todo: userCredentialManager() v 19.x.x is deprecated / v 2x.x.x was removed
                session.userCredentialManager().updateCredential(realm, sessionUser, hashPassword);
                sessionUser.addRequiredAction(UserModel.RequiredAction.UPDATE_PASSWORD);

                LOGGER.info("\nUser authenticated with success");
                context.success();

            } else {
                LOGGER.error("\nUser not found - Invalid User");
                var response = context.form()
                        .addError(new FormMessage(EMAIL_NOT_FOUND))
                        .createForm(VERIFY_USERNAME_FORM);

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
}
