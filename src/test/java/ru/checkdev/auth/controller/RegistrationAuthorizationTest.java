package ru.checkdev.auth.controller;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.checkdev.auth.repository.PersonRepository;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RegistrationAuthorizationTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper json;
    @Autowired
    private PersonRepository persons;
    @Autowired
    private PasswordEncoder passwords;
    @Autowired
    private JwtEncoder encoder;

    @Test
    @Transactional
    void registrationAndJsonLoginWithoutSession() throws Exception {
        String email = UUID.randomUUID() + "@example.com";
        String password = UUID.randomUUID().toString();
        String username = "name-" + UUID.randomUUID();
        mvc.perform(post("/registration").contentType(MediaType.APPLICATION_JSON)
                        .content(registration(username, email, password, true)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.key").doesNotExist());
        var profile = persons.findByEmail(email);
        assertNotNull(profile);
        assertTrue(profile.isActive());
        assertTrue(passwords.matches(password, profile.getPassword()));
        assertNotEquals(password, profile.getPassword());
        assertTrue(profile.getRoles().isEmpty());
        mvc.perform(get("/user")).andExpect(status().isUnauthorized());
        var login = mvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(email.toUpperCase(Locale.ROOT), password)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(header().doesNotExist("Set-Cookie"))
                .andExpect(jsonPath("$.token_type").value("Bearer"))
                .andExpect(jsonPath("$.expires_in").value(900))
                .andExpect(jsonPath("$.refresh_token").doesNotExist()).andReturn();
        assertNull(login.getRequest().getSession(false));
        String token = json.readTree(login.getResponse().getContentAsString()).get("access_token").asText();
        assertEquals(3, token.split("\\.").length);
        mvc.perform(get("/user").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(profile.getId()))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.password").doesNotExist());
        mvc.perform(get("/person/list/10/0/noSearch").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        mvc.perform(get("/user")).andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    void incorrectUnknownAndInactiveCredentialsHaveSameError() throws Exception {
        String email = UUID.randomUUID() + "@example.com";
        String password = UUID.randomUUID().toString();
        String username = "name-" + UUID.randomUUID();
        mvc.perform(post("/registration").contentType(MediaType.APPLICATION_JSON)
                .content(registration(username, email, password, true))).andExpect(status().isCreated());
        for (var entry : Map.of(email, "wrong-" + password, UUID.randomUUID() + "@example.com", password).entrySet()) {
            mvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON)
                            .content(credentials(entry.getKey(), entry.getValue())))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.detail").value("Неверный email или пароль"))
                    .andExpect(jsonPath("$.access_token").doesNotExist())
                    .andExpect(header().doesNotExist("Location"));
        }
        var profile = persons.findByEmail(email);
        profile.setActive(false);
        persons.save(profile);
        mvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(credentials(email, password)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Неверный email или пароль"));
    }

    @Test
    void malformedLoginReturnsBadRequest() throws Exception {
        String email = UUID.randomUUID() + "@example.com";
        String password = UUID.randomUUID().toString();
        for (var body : new String[]{credentials("not-email", password), credentials(email, ""), "{}"}) {
            mvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.access_token").doesNotExist());
        }
    }

    @Test
    void invalidExpiredAndWrongIssuerTokensAreRejected() throws Exception {
        mvc.perform(get("/user").header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
        var now = Instant.now();
        String email = UUID.randomUUID() + "@example.com";
        for (var claims : new JwtClaimsSet[]{
                JwtClaimsSet.builder().subject(email).issuer("http://localhost:9900")
                        .issuedAt(now.minusSeconds(1800)).expiresAt(now.minusSeconds(600)).build(),
                JwtClaimsSet.builder().subject(email).issuer("https://other-issuer.example")
                        .issuedAt(now).expiresAt(now.plusSeconds(900)).build()}) {
            String token = encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
            mvc.perform(get("/user").header("Authorization", "Bearer " + token))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    void noHtmlLoginOrOauthRedirectsAndNoSession() throws Exception {
        for (String path : new String[]{"/login", "/user", "/oauth2/authorize", "/oauth2/token", "/oauth/token"}) {
            var result = mvc.perform(get(path).accept(MediaType.TEXT_HTML))
                    .andExpect(status().isUnauthorized())
                    .andExpect(header().doesNotExist("Location"))
                    .andExpect(header().doesNotExist("Set-Cookie")).andReturn();
            assertNull(result.getRequest().getSession(false));
        }
    }

    @Test
    @Transactional
    void duplicateEmailAndInvalidRegistration() throws Exception {
        String email = UUID.randomUUID() + "@example.com";
        String password = UUID.randomUUID().toString();
        String username = "name-" + UUID.randomUUID();
        mvc.perform(post("/registration").contentType(MediaType.APPLICATION_JSON)
                        .content(registration(username, email, password, true))).andExpect(status().isCreated());
        mvc.perform(post("/registration").contentType(MediaType.APPLICATION_JSON)
                        .content(registration(username, email.toUpperCase(Locale.ROOT), password, true)))
                .andExpect(status().isConflict());
        mvc.perform(post("/registration").contentType(MediaType.APPLICATION_JSON)
                        .content(registration(username, "not-an-email", "short", true)))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/registration").contentType(MediaType.APPLICATION_JSON)
                        .content(registration(username, UUID.randomUUID() + "@example.com", password, false)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void swaggerDocumentsJsonLoginAndBearerAuthorization() throws Exception {
        mvc.perform(get("/")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/swagger-ui/index.html"));
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/registration'].post.responses['201']").exists())
                .andExpect(jsonPath("$.paths['/login'].post.requestBody.content['application/json']").exists())
                .andExpect(jsonPath("$.paths['/login'].post.responses['200']").exists())
                .andExpect(jsonPath("$.paths['/user'].get.security[0].bearerAuth").isArray())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.type").value("http"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.components.securitySchemes.oauth2").doesNotExist());
    }

    private String credentials(String email, String password) throws Exception {
        return json.writeValueAsString(Map.of("email", email, "password", password));
    }

    private String registration(String username, String email, String password, boolean privacy) throws Exception {
        return json.writeValueAsString(Map.of("username", username, "email", email,
                "password", password, "privacy", privacy));
    }
}
