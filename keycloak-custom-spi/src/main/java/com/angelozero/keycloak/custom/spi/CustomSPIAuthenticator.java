package com.angelozero.keycloak.custom.spi;

import com.angelozero.keycloak.custom.spi.database.MongoDBConnection;
import com.angelozero.keycloak.custom.spi.dto.User;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import jakarta.ws.rs.core.MultivaluedMap;
import org.bson.Document;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.mongodb.client.model.Filters.eq;

public class CustomSPIAuthenticator implements Authenticator {

    private static final Logger log = LoggerFactory.getLogger(CustomSPIAuthenticator.class);
    private final MongoDBConnection mongoDBConnection;

    public CustomSPIAuthenticator() {
        this.mongoDBConnection = new MongoDBConnection();
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {

        var email = context.getHttpRequest().getDecodedFormParameters().getFirst("email");
        var password = context.getHttpRequest().getDecodedFormParameters().getFirst("password");

        var database = mongoDBConnection.getDatabase();
        var collection = database.getCollection("users");

        var userDoc = collection.find(eq("email", email)).first();

        if (userDoc != null) {
            var user = User.fromDocument(userDoc);
            //if (BCrypt.checkpw(password, user.passwordHash())) {
            if (password.equals(user.passwordHash())) {
                var keycloakUser = context.getSession().users().addUser(context.getRealm(), user.email());
                keycloakUser.setFirstName(user.name());
                keycloakUser.setEmail(user.email());
                context.setUser(keycloakUser);
                context.success();
                log.info("\n\nUser is authenticated :)\n\n");
                return;
            }
        }

        log.info("\n\nUser is not authenticated :(\n\n");
        context.failure(AuthenticationFlowError.ACCESS_DENIED);
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
