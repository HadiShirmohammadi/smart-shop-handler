package assistant.main.javaServer.Services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {
    // Create key
    private final Key key;
    public JwtService(@Value("${jwt.secret}") String secretString){
        this.key = Keys.hmacShaKeyFor(secretString.getBytes());
    }
    //Generate token
    public String generateToken(String username,boolean isAdmin){
        Map<String,Object> claims = new HashMap<>();
        claims.put("isAdmin",isAdmin);
        long now  = System.currentTimeMillis();
        long expirationTime = 1000 * 60 * 60 * 12;

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationTime))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    // Get username from key
    public String extractUsername(String token){
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
    // Get admin user
    public boolean extractIsAdmin(String token){
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("isAdmin",Boolean.class);
    }
    // Understanding whether a key is correct or incorrect
    public boolean isTokenValid(String token){
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e){
            return false;
        }

    }

}