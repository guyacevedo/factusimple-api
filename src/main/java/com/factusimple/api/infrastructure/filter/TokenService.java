package com.factusimple.api.infrastructure.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.factusimple.api.infrastructure.factus.config.FactusProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenService {

    private final FactusProperties factusProperties;

    /**
     * Validación híbrida:
     * - estructura JWT
     * - algoritmo esperado
     * - expiration
     * - not before
     * - audience
     * NO se valida firma porque Factus
     * no expone public key ni JWKS endpoint.
     */
    public boolean isTokenValid(String token) {

        try {

            DecodedJWT jwt = JWT.decode(token);

            return validateAlgorithm(jwt)
                    && validateExpiration(jwt)
                    && validateNotBefore(jwt)
                    && validateAudience(jwt);

        } catch (Exception e) {

            log.warn("Token inválido: {}", e.getMessage());
            return false;
        }
    }

    private boolean validateAlgorithm(DecodedJWT jwt) {

        String alg = jwt.getAlgorithm();

        return "RS256".equals(alg);
    }

    private boolean validateExpiration(DecodedJWT jwt) {

        Date expiresAt = jwt.getExpiresAt();

        return expiresAt != null
                && expiresAt.after(new Date());
    }

    private boolean validateNotBefore(DecodedJWT jwt) {

        Date nbf = jwt.getNotBefore();

        return nbf == null
                || !nbf.after(new Date());
    }

    private boolean validateAudience(DecodedJWT jwt) {

        return jwt.getAudience() != null
                && jwt.getAudience().contains(factusProperties.expectedAudience());
    }

}
