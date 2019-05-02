package com.company.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

public class TokenUtils {
    public static String keyToString(Key key) {
        return Encoders.BASE64.encode(key.getEncoded());
    }

    public static Key createKeyFromString(String key) {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(key));
    }

    public static Key createKey() {
        return Keys.secretKeyFor(SignatureAlgorithm.HS256);
    }

    public static String createToken(String username, Key key) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 10000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public static String getUsername(String token, Key key) {
        try {
            return Jwts.parser()
                    .setSigningKey(key)
                    .parseClaimsJws(token)
                    .getBody().getSubject();
        } catch (Exception e) {
            return "";
        }
    }

    public static boolean isTokenValid(String token, Key key) {
        try {
            return Jwts.parser()
                    .setSigningKey(key)
                    .parseClaimsJws(token)
                    .getBody().getSubject() != null;
        } catch (Exception e) {
            return false;
        }
    }
}
