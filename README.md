Keycloak Features
- Single Sign-On (SSO) and Single Logout
- Identity Brokering and Social Login
- User Federation ( LDAP / Active Directory )
- Fine-Grained Authorization Services 
- Centralized Management and Admin Console
- Client Adapters ( Java / Javascript / Node JS )

Keycloack Advantages
- Open Source
- Versatility
- Scalability
- Security
- Customizability
- Ease of Use ( desinged to be ease to use)

Keycloak Terms
- Realm
- Clients
- Client Scopes
- Users
-  Groups


Site
- [Keycloack](https://www.keycloak.org/)
- [Keycloack with Docker](https://www.keycloak.org/getting-started/getting-started-docker)
    - to run on a MacOs Sonoma 14.2.1 Apple M1 add "--platform=linux/amd64"
    - docker run --platform=linux/amd64 -p 8080:8080 -e KC_BOOTSTRAP_ADMIN_USERNAME=admin -e KC_BOOTSTRAP_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:26.1.4 start-dev
        - solution found by GabaGabaDev on question [Stackoverflow - M1 mac cannot run jboss/keycloak docker image](https://stackoverflow.com/questions/67044893/m1-mac-cannot-run-jboss-keycloak-docker-image)
