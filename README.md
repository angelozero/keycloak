# SPI Customizada para autenticação com SpringBoot, Keycloak, Docker e PostgreSQL

![logo](./images/logo-keycloak-spi.jpeg)

- O que é um SPI (Service Provider Interface) ? 
  - Uma SPI é um mecanismo que permite a extensão e personalização do comportamento do Keycloak. As SPIs fornecem um conjunto de interfaces que os desenvolvedores podem implementar para adicionar novas funcionalidades ou modificar as existentes.

- Neste artigo irei demonstrar como criar uma simples SPI customizada para interceptar uma autenticação. A idéia é exclusivamente para estudo e não uma solução para um "problema real".
- O que vamos fazer no keycloack ?
    - Criar um realm
    - Criar um client
    - Criar roles 
    - Se conectar em uma base Ldap
    - Associar os usuário da base Ldap a nossas roles
    - Interceptar sua autenticação para registrar seus dados em uma base de dados postgres
    - Invocar uma api rest e atraves do token gerado pelo keycloak liberar o acesso respectivo a esse usuário de acordo com seu papel (role)

- Vamos interceptar uma autenticação, recuperar os dados deste usuário e tentar encontra-lo em uma base de dados postgres. Se ele existir apenas seguir com a autenticação caso contrário vamos registrar ele na nossa base de dados e assim liberar sua autenticação.
- Após a geração do token, vamos utiliza-lo para chamar um serviço rest e tentar acessar alguns serviços sendo validados pelas roles associdadas a este usuário do LDAP.





---
Keycloak Features
- Single Sign-On (SSO) and Single Logout
- Identity Brokering and Social Login
- User Federation ( LDAP / Active Directory )
- Fine-Grained Authorization Services 
- Centralized Management and Admin Console
- Client Adapters ( Java / Javascript / Node JS )

Keycloak Advantages
- Open Source
- Versatility
- Scalability
- Security
- Customizability
- Ease of Use ( designed to be ease to use)

Keycloak Terms
- Realm
- Clients
- Client Scopes
- Users
-  Groups


- Subir postgresql via docker
  - docker pull postgres --platform=linux/amd64 
  - docker run --platform=linux/amd64 -p 5432:5432 -e POSTGRES_USER=admin -e POSTGRES_PASSWORD=admin postgres

- Para copiar jar e temas
    - docker ps e copiar o nome do container Ex.: keycloak_container
    - (dentro de target) docker cp keycloak-custom-spi.jar  keycloak_container:/opt/keycloak/providers
    - (dentro de jar) docker cp postgresql-42.7.2.jar keycloak_container:/opt/keycloak/providers
    - (terminal) docker exec -it keycloak_container /opt/keycloak/bin/kc.sh build
    - (dentro de themes) docker cp ./login keycloak_container:/opt/keycloak/themes

- Iniciar keycloak e postgres na mesma rede
  - docker network create keycloak-network
  - docker run --platform=linux/amd64 --name postgres_container --network keycloak-network -e POSTGRES_USER=admin -e POSTGRES_PASSWORD=admin -e POSTGRES_DB=postgres -p 5432:5432 -d postgres
  - docker run --platform=linux/amd64 --name keycloak_container --network keycloak-network -e DB_VENDOR=postgres -e DB_ADDR=intelligent_euclid -e DB_PORT=5432 -e DB_DATABASE=postgres -e DB_USER=admin -e DB_PASSWORD=admin -p 8080:8080 -e KC_BOOTSTRAP_ADMIN_USERNAME=admin -e KC_BOOTSTRAP_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:26.1.4 start-dev

Site
- [Keycloak](https://www.keycloak.org/)
- [Keycloak with Docker](https://www.keycloak.org/getting-started/getting-started-docker)
    - to run on a MacOs Sonoma 14.2.1 Apple M1 add "--platform=linux/amd64"
    - docker run --platform=linux/amd64 -p 8080:8080 -e KC_BOOTSTRAP_ADMIN_USERNAME=admin -e KC_BOOTSTRAP_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:26.1.4 start-dev
        - solution found by GabaGabaDev on question [Stackoverflow - M1 mac cannot run jboss/keycloak docker image](https://stackoverflow.com/questions/67044893/m1-mac-cannot-run-jboss-keycloak-docker-image)

- [Installation Guide for Keycloak (macOS)](https://blog.devops.dev/installation-guide-for-keycloak-macos-c17a111bfdff)
- [Stackoverflow - Implement custom SPI in Keycloak by Azdy](https://stackoverflow.com/questions/62672377/implement-custom-spi-in-keycloak)
- [Service Provider Interfaces (SPI)](https://www.keycloak.org/docs/latest/server_development/#_providers)


- Next Steps
1 - create a rest application
2 - integration test with custom spi

- Postgres
  - CREATE TABLE "USER" (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL
    );
  - 

- LDAP
= https://www.forumsys.com/2022/05/10/online-ldap-test-server/
- ldap://ldap.forumsys.com:389
- cn=read-only-admin,dc=example,dc=com
- password
- dc=example,dc=com

- [Youtube - Keycloak UserFederation e Mappers](https://www.youtube.com/watch?v=PHbxodkWlxg)

- backupt
```java
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
```

i need create an article about this

...

work in progress I swear
