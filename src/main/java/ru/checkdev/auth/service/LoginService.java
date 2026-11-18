package ru.checkdev.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;
import ru.checkdev.auth.dto.LoginRequest;
import ru.checkdev.auth.dto.TokenResponse;

import java.time.Instant;
import java.util.UUID;

@Service
public class LoginService {
    private static final long TOKEN_SECONDS = 900;
    private final AuthenticationManager authenticationManager;
    private final JwtEncoder encoder;
    private final String issuer;

    public LoginService(AuthenticationManager authenticationManager, JwtEncoder encoder,
                        @Value("${auth.public-url}") String issuer) {
        this.authenticationManager = authenticationManager;
        this.encoder = encoder;
        this.issuer = issuer;
    }

    public TokenResponse login(LoginRequest request) {
        var user = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.email(), request.password()));
        var now = Instant.now();
        var claims = JwtClaimsSet.builder().issuer(issuer).subject(user.getName())
                .issuedAt(now).notBefore(now).expiresAt(now.plusSeconds(TOKEN_SECONDS))
                .id(UUID.randomUUID().toString())
                .claim("roles", user.getAuthorities().stream()
                        .map(authority -> authority.getAuthority())
                        .filter(role -> role.startsWith("ROLE_")).toList()).build();
        var header = JwsHeader.with(SignatureAlgorithm.RS256).build();
        var token = encoder.encode(JwtEncoderParameters.from(header, claims));
        return new TokenResponse(token.getTokenValue(), "Bearer", TOKEN_SECONDS);
    }
}
