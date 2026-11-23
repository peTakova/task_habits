package task_habit.api.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    /** Platnosť tokenu aj cookie – 10 hodín. */
    public static final long JWT_EXPIRATION_MS = 1000L * 60 * 60 * 10;

    /** Claim s ID používateľa – token je viazaný na konkrétny účet, nie len na email. */
    public static final String USER_ID_CLAIM = "userId";

    /** Base64 kľúč z premennej prostredia JWT_SECRET (pozri application.properties). */
    @Value("${jwt.secret:}")
    private String configuredSecret;

    private Key signingKey;

    @PostConstruct
    void initSigningKey() {
        if (this.configuredSecret == null || this.configuredSecret.isBlank()) {
            // Bez nastaveného kľúča sa vygeneruje náhodný – vhodné na lokálny vývoj,
            // tokeny však po reštarte servera prestanú platiť.
            this.signingKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
            log.warn("JWT_SECRET nie je nastavený – používa sa náhodný kľúč, prihlásenia po reštarte servera vypršia.");
            return;
        }
        byte[] keyBytes = Decoders.BASE64.decode(this.configuredSecret.trim());
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET musí mať aspoň 256 bitov (32 bajtov) v Base64.");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /** Overí podpis aj expiráciu a vráti obsah tokenu – pri neplatnom tokene vyhodí JwtException. */
    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(this.signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String generateToken(Long userId, String email) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(Map.of(USER_ID_CLAIM, userId))
                .setSubject(email)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + JWT_EXPIRATION_MS))
                .signWith(this.signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /** ID používateľa z tokenu alebo null, ak ho token neobsahuje. */
    public static Long userIdOf(Claims claims) {
        Object value = claims.get(USER_ID_CLAIM);
        return value instanceof Number number ? number.longValue() : null;
    }
}
