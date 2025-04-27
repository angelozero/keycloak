package com.angelozero.keycloak.custom.spi;

import com.angelozero.keycloak.custom.spi.dto.User;
import com.angelozero.keycloak.custom.spi.dto.UserDataInterests;
import com.angelozero.keycloak.custom.spi.dto.UserInterests;
import com.angelozero.keycloak.custom.spi.repository.UserPostgresRepository;
import org.keycloak.models.*;
import org.keycloak.protocol.oidc.mappers.*;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.AccessToken;
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

        var infoResponse = getCustomAuthenticationConfigValue(clientSessionCtx);

        if (infoResponse.equalsIgnoreCase("ACTIVE")) {
            var email = clientSessionCtx.getClientSession().getUserSession().getUser().getEmail();
            var userDataInterests = findUserDataInterests(email);
            OIDCAttributeMapperHelper.mapClaim(token, mappingModel, userDataInterests);
            LOGGER.info("[CustomAccessTokenMapper] - Token updated with success");

        } else {
            OIDCAttributeMapperHelper.mapClaim(token, mappingModel, "no_info_was_found");
            LOGGER.info("[CustomAccessTokenMapper] - Token was not updated");
        }
    }

    @Override
    public AccessToken transformAccessToken(AccessToken token, ProtocolMapperModel mappingModel, KeycloakSession session, UserSessionModel userSession, ClientSessionContext clientSessionCtx) {
        LOGGER.info("[CustomAccessTokenMapper] - Custom Transform Access Token");
        var isEnable = isCustomAuthenticationConfigEnable(clientSessionCtx);

        if (isEnable) {
            var email = clientSessionCtx.getClientSession().getUserSession().getUser().getEmail();
            var user = findUser(email);
            token.getOtherClaims().put("interests_string_list", user.interests());
            setClaim(token, mappingModel, userSession, session, clientSessionCtx);
            LOGGER.info("[CustomAccessTokenMapper] - Token updated value to interests_string_list with success");

        } else {
            token.getOtherClaims().put("interests_string_list", "not_enable");
            setClaim(token, mappingModel, userSession, session, clientSessionCtx);
            LOGGER.info("[CustomAccessTokenMapper] - Token updated value to interests_string_list was not enable");
        }

        return token;
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


    private UserDataInterests findUserDataInterests(String email) {
        var user = findUser(email);

        return user != null ?
                new UserDataInterests(user.interests().stream().map(UserInterests::new).toList())
                : null;
    }

    private User findUser(String email) {
        var repository = UserPostgresRepository.getInstance();
        return repository.findByEmail(email);
    }

    private String getCustomAuthenticationConfigValue(ClientSessionContext clientSessionCtx) {

        var realm = clientSessionCtx.getClientSession().getRealm();
        var authenticatorConfigByList = realm.getAuthenticatorConfigsStream().toList();

        return authenticatorConfigByList.stream()
                .filter(Objects::nonNull)
                .map(AuthenticatorConfigModel::getConfig)
                .filter(auth -> auth.get(CustomAuthenticator.CUSTOM_AUTH_CLIENT_CONFIG_VALUE) != null)
                .map(auth ->
                {
                    LOGGER.info("[CustomAccessTokenMapper] - Config value -------> {}", auth);
                    return auth.get(CustomAuthenticator.CUSTOM_AUTH_CLIENT_CONFIG_VALUE);
                })
                .findFirst()
                .orElse("");
    }

    private Boolean isCustomAuthenticationConfigEnable(ClientSessionContext clientSessionCtx) {

        var realm = clientSessionCtx.getClientSession().getRealm();
        var authenticatorConfigByList = realm.getAuthenticatorConfigsStream().toList();

        return authenticatorConfigByList.stream()
                .filter(Objects::nonNull)
                .map(AuthenticatorConfigModel::getConfig)
                .filter(auth -> Boolean.parseBoolean(auth.get(CustomAuthenticator.CUSTOM_AUTH_CLIENT_CONFIG_ENABLE)))
                .map(auth ->
                {
                    LOGGER.info("[CustomAccessTokenMapper] - Is enable ? --------> {}", auth);
                    return Boolean.parseBoolean(auth.get(CustomAuthenticator.CUSTOM_AUTH_CLIENT_CONFIG_ENABLE));
                })
                .findFirst()
                .orElse(Boolean.FALSE);
    }
}
