package com.daruda.darudaserver.global.auth.jwt.provider;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token-expire-time}")
    private long accessTokenExpireTime;

    @Value("${jwt.refresh-token-expire-time}")
    private long refreshTokenExpireTime;

    private static final String USER_ID  = "userId";

    @PostConstruct
    protected void init(){
        jwtSecret = Base64.getEncoder().encodeToString(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(final Authentication authentication){
        return generateToken(authentication, accessTokenExpireTime);
    }

    public String generateRefreshToken(final Authentication authentication){
        return generateToken(authentication, refreshTokenExpireTime);
    }

    public Long getUserIdFromJwt(String token){
        Claims claims = getBody(token);
        Long userId = Long.valueOf(claims.get(USER_ID).toString());

        return userId;
    }

    private Claims getBody(final String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private SecretKey getSigningKey(){
        String encodedKey = Base64.getEncoder().encodeToString(jwtSecret.getBytes());
        return Keys.hmacShaKeyFor(encodedKey.getBytes());
    }

    private String generateToken(final Authentication authentication, final long expiredTime){
        final Date now = new Date();

        //claim 생성 및 토큰 만료 시간 설정(현재 시간 + yml 파일에 설정한 expire time)
        final Claims claims = Jwts.claims().setIssuedAt(now).setExpiration(new Date(now.getTime()+expiredTime));

        //userId claim에 저장
        claims.put(USER_ID, authentication.getPrincipal());

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setClaims(claims)
                .signWith(getSigningKey())
                .compact();
    }

    public JwtValidationType validateToken(String token){
        try{
            Claims claims = getBody(token);
            return JwtValidationType.VALID_JWT;
        } catch (MalformedJwtException ex){
            return JwtValidationType.INVALID_JWT_TOKEN;
        } catch (ExpiredJwtException ex){
            return JwtValidationType.EXPIRED_JWT_TOKEN;
        } catch (UnsupportedJwtException ex){
            return JwtValidationType.UNSUPPORTED_JWT_TOKEN;
        } catch (IllegalArgumentException ex){
            return JwtValidationType.EMPTY_JWT;
        } catch (SignatureException ex){
            return JwtValidationType.INVALID_JWT_TOKEN;
        }
    }


}
