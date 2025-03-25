package com.angelozero.keycloak.custom.spi;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.Collections;
import java.util.List;

import static org.keycloak.provider.ProviderConfigProperty.STRING_TYPE;

public class CustomSPIAuthenticatorFactory implements AuthenticatorFactory {
    private static final CustomSPIAuthenticator CUSTOM_SPI_AUTHENTICATOR_INSTANCE = new CustomSPIAuthenticator();
    private static final String CUSTOM_SPI_AUTHENTICATOR_ID = "Custom SPI Authenticator ID";
    private static final String CUSTOM_SPI_AUTHENTICATOR_NAME = "AngeloZero Custom SPI Auth";


    @Override
    public Authenticator create(KeycloakSession keycloakSession) {
        return CUSTOM_SPI_AUTHENTICATOR_INSTANCE;
    }

    @Override
    public String getDisplayType() {
        return "Custom IP Authenticator";
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public String getReferenceCategory() {
        return "";
    }


    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return new AuthenticationExecutionModel.Requirement[]{AuthenticationExecutionModel.Requirement.REQUIRED};
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public String getHelpText() {
        return "This is a custom message to inform: Allows access only to approved IP addresses.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        ProviderConfigProperty name = new ProviderConfigProperty();

        name.setType(STRING_TYPE);
        name.setName(CUSTOM_SPI_AUTHENTICATOR_NAME);
        name.setLabel("A simple custom auth spi");
        name.setHelpText("This is a help text!!!.");

        return Collections.singletonList(name);
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
        return CUSTOM_SPI_AUTHENTICATOR_ID;
    }
}
