package com.angelozero.keycloak.custom.spi.authenticator;

import com.angelozero.keycloak.custom.spi.authenticator.repository.UserPostgresRepository;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.mappers.*;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.IDToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CustomAccessTokenMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomAccessTokenMapper.class);

    public static final String CUSTOM_ACCESS_TOKEN_MAPPER_ID = "custom-access-token-map-id";


    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    static {
        OIDCAttributeMapperHelper.addTokenClaimNameConfig(configProperties);
        OIDCAttributeMapperHelper.addIncludeInTokensConfig(configProperties, CustomAccessTokenMapper.class);
    }

    @Override
    public String getDisplayCategory() {
        return "AngeloZero - Custom Access Token Mapper";
    }

    @Override
    public String getDisplayType() {
        return "AngeloZero - Custom Access Token Mapper";
    }

    @Override
    public String getHelpText() {
        return "AngeloZero - This is a custom access token mapper";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    @Override
    public String getId() {
        return CUSTOM_ACCESS_TOKEN_MAPPER_ID;
    }

    @Override
    protected void setClaim(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession, KeycloakSession keycloakSession, ClientSessionContext clientSessionCtx) {
        LOGGER.info("[CustomAccessTokenMapper] - Custom Access Token Mapper SPI");

        var postgresUserId = findUserId(clientSessionCtx.getClientSession().getUserSession().getUser().getEmail());
        OIDCAttributeMapperHelper.mapClaim(token, mappingModel, postgresUserId);

        LOGGER.info("[CustomAccessTokenMapper] - Token updated with success if \"postgres user id\": \"{}\"", postgresUserId);
    }

    private Integer findUserId(String email) {
        var repository = UserPostgresRepository.getInstance();
        var userFound = repository.findByEmail(email);

        if (userFound == null) {
            return 0;
        }

        return userFound.id();
    }
}
