package com.angelozero.keycloak.rest.api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final String RESOURCE_ACCESS = "resource_access";
    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    private final JwtAuthProperties jwtAuthProperties;

    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {

        Collection<GrantedAuthority> authorities = Stream.concat(
                        jwtGrantedAuthoritiesConverter.convert(jwt).stream(),
                        extractResourceRoles(jwt).stream())
                .collect(Collectors.toSet());

        return new JwtAuthenticationToken(
                jwt,
                authorities,
                getPrincipleClaimName(jwt)
        );
    }

    private String getPrincipleClaimName(Jwt jwt) {
        var principleAttribute = jwtAuthProperties.getPrincipleAttribute();
        var claimName = principleAttribute != null ? principleAttribute : JwtClaimNames.SUB;

        return jwt.getClaim(claimName);
    }

    private Collection<? extends GrantedAuthority> extractResourceRoles(Jwt jwt) {

        Map<String, Object> resource;
        Map<String, Object> resourceAccess;
        Collection<String> resourceRoles;

        if (jwt.getClaim(RESOURCE_ACCESS) == null) {
            return Set.of();
        }

        resourceAccess = jwt.getClaim(RESOURCE_ACCESS);

        if (resourceAccess.get(jwtAuthProperties.getResourceId()) == null) {
            return Set.of();
        }

        resource = (Map<String, Object>) resourceAccess.get(jwtAuthProperties.getResourceId());

        resourceRoles = (Collection<String>) resource.get("roles");

        return resourceRoles
                .stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toSet());
    }
}
