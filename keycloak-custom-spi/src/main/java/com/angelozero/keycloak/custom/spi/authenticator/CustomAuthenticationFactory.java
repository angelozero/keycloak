package com.angelozero.keycloak.custom.spi.authenticator;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.ArrayList;
import java.util.List;

public class CustomAuthenticationFactory implements AuthenticatorFactory {

    private static final CustomAuthenticator CUSTOM_AUTHENTICATOR_INSTANCE = new CustomAuthenticator();

    @Override
    public Authenticator create(KeycloakSession keycloakSession) {
        return CUSTOM_AUTHENTICATOR_INSTANCE;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return new AuthenticationExecutionModel.Requirement[]{
                AuthenticationExecutionModel.Requirement.REQUIRED,
                AuthenticationExecutionModel.Requirement.DISABLED
        };
    }

    @Override
    public String getDisplayType() {
        return "AngeloZero - Custom Authentication";
    }

    @Override
    public String getReferenceCategory() {
        return "";
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public String getHelpText() {
        return "Simple custom authentication by email and password trough a Postgres SQL database";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        final List<ProviderConfigProperty> configProperties = new ArrayList<>();

        ProviderConfigProperty clientMasterConfig = new ProviderConfigProperty();
        clientMasterConfig.setName(CustomAuthenticator.CUSTOM_AUTH_CLIENT_CONFIG_VALUE);
        clientMasterConfig.setLabel("Config value");
        clientMasterConfig.setType(ProviderConfigProperty.STRING_TYPE);
        clientMasterConfig.setHelpText("This is an information to set a token property");

        ProviderConfigProperty clientMasterEnableConfig = new ProviderConfigProperty();
        clientMasterEnableConfig.setName(CustomAuthenticator.CUSTOM_AUTH_CLIENT_CONFIG_ENABLE);
        clientMasterEnableConfig.setLabel("Config enable");
        clientMasterEnableConfig.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        clientMasterEnableConfig.setHelpText("This is an information to set a token property but it's inactive...");

        configProperties.add(clientMasterConfig);
        configProperties.add(clientMasterEnableConfig);

        return configProperties;
    }


    @Override
    public void init(Config.Scope scope) {

    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return CustomAuthenticator.CUSTOM_AUTHENTICATOR_PROVIDER_ID;
    }
}
