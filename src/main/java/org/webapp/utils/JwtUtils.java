package org.webapp.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;

import java.time.Instant;

public class JwtUtils {
    private static final String SECRET = "2*jsI0?{;w8`A-bKP4g:m$^L|17)Fe*39&4Y]?>n";
    private static final String ISSUER = "auth_system";

    public static String generateToken(String userId, String username) {
        Instant instant = Instant.now();
        return JWT.create()
                .withClaim("user_id", userId)
                .withClaim("username", username)
                .withIssuer(ISSUER)
                .withIssuedAt(instant)
                .withExpiresAt(instant.plusSeconds(86400 * 15))
                .sign(Algorithm.HMAC256(SECRET));
    }

    public static String getUserId(String token) {
        try {
            return JWT.require(Algorithm.HMAC256(SECRET)).withIssuer(ISSUER).build().verify(token).getClaim("user_id").asString();
        } catch (JWTVerificationException e) {
            return null;
        }
    }
}