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
- https://www.forumsys.com/2022/05/10/online-ldap-test-server/
- ldap://ldap.forumsys.com:389
- cn=read-only-admin,dc=example,dc=com
- password
- dc=example,dc=com
---
- [Keycloak UserFederation e Mappers - by Marco Seabra](https://www.youtube.com/watch?v=PHbxodkWlxg)
- [How to create a Keycloak client with an audience mapper - by Alex Ellis](https://www.youtube.com/watch?v=G2QVhUAEylc)
- [Custom Protocol Mapper - by Niko Köbler](https://www.youtube.com/watch?v=5WBb176YqKg)
- [Baeldung - Custom Protocol Mapper with Keycloak](https://www.baeldung.com/keycloak-custom-protocol-mapper)
- [GitHub - Keycloak - ScriptBasedOIDCProtocolMapper](https://github.com/keycloak/keycloak/blob/main/services/src/main/java/org/keycloak/protocol/oidc/mappers/ScriptBasedOIDCProtocolMapper.java)