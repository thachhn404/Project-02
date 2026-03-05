package com.team2.Crowdsourced_Waste_Collection_Recycling_System.config;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.authentication.InvalidatedTokenRepository;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

public class JtiDenylistValidator implements OAuth2TokenValidator<Jwt> {
    private final InvalidatedTokenRepository invalidatedTokenRepository;

    public JtiDenylistValidator(InvalidatedTokenRepository invalidatedTokenRepository) {
        this.invalidatedTokenRepository = invalidatedTokenRepository;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        String jti = token.getId();
        if (jti != null && invalidatedTokenRepository.existsById(jti)) {
            return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "revoked", null));
        }
        return OAuth2TokenValidatorResult.success();
    }
}

