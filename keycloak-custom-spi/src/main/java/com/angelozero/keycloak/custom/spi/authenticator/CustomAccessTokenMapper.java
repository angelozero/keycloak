package com.angelozero.keycloak.custom.spi.authenticator;

import com.angelozero.keycloak.custom.spi.authenticator.repository.UserPostgresRepository;
import org.keycloak.models.*;
import org.keycloak.protocol.oidc.mappers.*;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.IDToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CustomAccessTokenMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomAccessTokenMapper.class);

    public static final String CUSTOM_ACCESS_TOKEN_MAPPER_ID = "custom-access-token-map-id";


    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    static {
        OIDCAttributeMapperHelper.addTokenClaimNameConfig(configProperties);
        OIDCAttributeMapperHelper.addIncludeInTokensConfig(configProperties, CustomAccessTokenMapper.class);
    }

    @Override
    protected void setClaim(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession, KeycloakSession keycloakSession, ClientSessionContext clientSessionCtx) {
        LOGGER.info("[CustomAccessTokenMapper] - Custom Access Token Mapper SPI");

        var infoRespone = "";

        var realm = clientSessionCtx.getClientSession().getRealm();
        var authenticatorConfigByList = realm.getAuthenticatorConfigsStream().toList();

        if (!authenticatorConfigByList.isEmpty()) {
            authenticatorConfigByList.stream()
                    .filter(Objects::nonNull)
                    .map(AuthenticatorConfigModel::getConfig)
                    .forEach(data -> LOGGER.info("[CustomAccessTokenMapper] - Config value -------> {}", data));

            infoRespone = authenticatorConfigByList.stream()
                    .filter(Objects::nonNull)
                    .map(AuthenticatorConfigModel::getConfig)
                    .filter(auth -> auth.get(CustomAuthenticator.CUSTOM_AUTH_CLIENT_CONFIG_VALUE) != null)
                    .map(auth -> auth.get(CustomAuthenticator.CUSTOM_AUTH_CLIENT_CONFIG_VALUE))
                    .findFirst()
                    .orElse("");
        }


        if (infoRespone.equalsIgnoreCase("ACTIVE")) {
            var postgresUserId = findUserId(clientSessionCtx.getClientSession().getUserSession().getUser().getEmail());
            OIDCAttributeMapperHelper.mapClaim(token, mappingModel, postgresUserId);
            LOGGER.info("[CustomAccessTokenMapper] - Token updated with success");

        } else {
            OIDCAttributeMapperHelper.mapClaim(token, mappingModel, "no_info_was_found");
            LOGGER.info("[CustomAccessTokenMapper] - Token was not updated");
        }
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


    private Integer findUserId(String email) {
        var repository = UserPostgresRepository.getInstance();
        var userFound = repository.findByEmail(email);

        if (userFound == null) {
            return 0;
        }

        return userFound.id();
    }
}
