package sg.edu.nus.iss.edgp.notification.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;

import sg.edu.nus.iss.edgp.notification.configuration.JwtConfig;
import sg.edu.nus.iss.edgp.notification.pojo.User;
import sg.edu.nus.iss.edgp.notification.service.impl.JwtService;
import sg.edu.nus.iss.edgp.notification.utility.JSONReader;

public class JwtServiceTest {

    private JwtService jwtService;
    private JwtConfig jwtConfig;
    private JSONReader jsonReader;

    private RSAPublicKey publicKey;
    private Claims claims;

    @BeforeEach
    void setUp() throws Exception {
        jwtConfig = mock(JwtConfig.class);
        jsonReader = mock(JSONReader.class);
        jwtService = new JwtService(jwtConfig, jsonReader);

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        publicKey = (RSAPublicKey) keyPair.getPublic();
        when(jwtConfig.loadPublicKey()).thenReturn(publicKey);

        claims = mock(Claims.class);
    }

    @Test
    void extractSubject_returnsCorrectSubject() throws Exception {
        String token = "dummy.jwt.token";
        String expectedSubject = "user123";

        JwtService spyService = Mockito.spy(jwtService);
        doReturn(claims).when(spyService).extractAllClaims(token);
        when(claims.getSubject()).thenReturn(expectedSubject);

        String result = spyService.extractSubject(token);
        assertEquals(expectedSubject, result);
    }

    @Test
    void isTokenExpired_falseWhenFutureExpiry() throws Exception {
        String token = "valid.token";
        JwtService spyService = Mockito.spy(jwtService);
        Date future = new Date(System.currentTimeMillis() + 60_000);
        doReturn(future).when(spyService).extractExpiration(token);

        assertFalse(spyService.isTokenExpired(token));
    }

    @Test
    void isTokenExpired_trueWhenPastExpiry() throws Exception {
        String token = "expired.token";
        JwtService spyService = Mockito.spy(jwtService);
        Date past = new Date(System.currentTimeMillis() - 60_000);
        doReturn(past).when(spyService).extractExpiration(token);

        assertTrue(spyService.isTokenExpired(token));
    }

    @Test
    void extractUserNameFromToken_valid_returnsName() throws Exception {
        String token = "valid.token";
        String expectedName = "John Doe";

        JwtService spyService = Mockito.spy(jwtService);
        doReturn(claims).when(spyService).extractAllClaims(token);
        when(claims.get(JwtService.USER_NAME, String.class)).thenReturn(expectedName);

        assertEquals(expectedName, spyService.extractUserNameFromToken(token));
    }

    @Test
    void extractUserNameFromToken_expired_usesClaimsFromException() throws Exception {
        String token = "expired.token";
        String expectedName = "Jane Roe";

        Claims expiredClaims = mock(Claims.class);
        when(expiredClaims.get(JwtService.USER_NAME, String.class)).thenReturn(expectedName);

        JwtService spyService = Mockito.spy(jwtService);
        doThrow(new ExpiredJwtException(null, expiredClaims, "expired")).when(spyService).extractAllClaims(token);

        assertEquals(expectedName, spyService.extractUserNameFromToken(token));
    }

    @Test
    void extractUserNameFromToken_otherException_returnsInvalidUsername() throws Exception {
        String token = "bad.token";
        JwtService spyService = Mockito.spy(jwtService);
        doThrow(new RuntimeException("boom")).when(spyService).extractAllClaims(token);

        assertEquals("Invalid Username", spyService.extractUserNameFromToken(token));
    }

    @Test
    void extractUserIdFromToken_valid_returnsSubject() throws Exception {
        String token = "valid.token";
        JwtService spyService = Mockito.spy(jwtService);
        doReturn(claims).when(spyService).extractAllClaims(token);
        when(claims.getSubject()).thenReturn("user-abc");

        assertEquals("user-abc", spyService.extractUserIdFromToken(token));
    }

    @Test
    void extractUserIdFromToken_expired_usesClaimsFromException() throws Exception {
        String token = "expired.token";
        Claims expiredClaims = mock(Claims.class);
        when(expiredClaims.getSubject()).thenReturn("expired-user");

        JwtService spyService = Mockito.spy(jwtService);
        doThrow(new ExpiredJwtException(null, expiredClaims, "expired")).when(spyService).extractAllClaims(token);

        assertEquals("expired-user", spyService.extractUserIdFromToken(token));
    }

    @Test
    void extractUserIdFromToken_otherException_returnsInvalidUserId() throws Exception {
        String token = "bad.token";
        JwtService spyService = Mockito.spy(jwtService);
        doThrow(new RuntimeException("nope")).when(spyService).extractAllClaims(token);

        assertEquals("Invalid UserID", spyService.extractUserIdFromToken(token));
    }

    @Test
    void validateToken_success_whenEmailMatchesAndNotExpired() throws Exception {
        String token = "valid.token";
        String email = "test@example.com";

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(email).password("pass").roles("USER").build();

        JwtService spyService = Mockito.spy(jwtService);
        doReturn(claims).when(spyService).extractAllClaims(token);
        when(claims.get(JwtService.USER_EMAIL, String.class)).thenReturn(email);
        doReturn(false).when(spyService).isTokenExpired(token);

        assertTrue(spyService.validateToken(token, userDetails));
    }

    @Test
    void validateToken_false_whenEmailMismatch() throws Exception {
        String token = "valid.token";
        String email = "test@example.com";

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(email).password("pass").roles("USER").build();

        JwtService spyService = Mockito.spy(jwtService);
        doReturn(claims).when(spyService).extractAllClaims(token);
        when(claims.get(JwtService.USER_EMAIL, String.class)).thenReturn("other@example.com"); // mismatch
        doReturn(false).when(spyService).isTokenExpired(token);

        assertFalse(spyService.validateToken(token, userDetails));
    }

    @Test
    void validateToken_false_whenExpired() throws Exception {
        String token = "expired.token";
        String email = "test@example.com";

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(email).password("pass").roles("USER").build();

        JwtService spyService = Mockito.spy(jwtService);
        doReturn(claims).when(spyService).extractAllClaims(token);
        when(claims.get(JwtService.USER_EMAIL, String.class)).thenReturn(email);
        doReturn(true).when(spyService).isTokenExpired(token); // expired

        assertFalse(spyService.validateToken(token, userDetails));
    }

    @Test
    void getUserDetail_success_buildsSpringUser() throws Exception {
        String token = "token";
        String authHeader = "Bearer token";
        String userId = "user123";

        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("securepass");
       
        user.setRole("Customer");

        JSONObject response = new JSONObject();
        response.put("success", true);

        JwtService spyService = Mockito.spy(jwtService);
        doReturn(userId).when(spyService).extractSubject(token);

        when(jsonReader.getActiveUserInfo(userId, authHeader)).thenReturn(response);
        when(jsonReader.getSuccessFromResponse(response)).thenReturn(true);
        when(jsonReader.getUserObject(response)).thenReturn(user);

        UserDetails ud = spyService.getUserDetail(authHeader, token);
        assertEquals(user.getEmail(), ud.getUsername());
    }

    @Test
    void getUserDetail_failure_throwsWithMessage() throws Exception {
        String token = "token";
        String authHeader = "Bearer token";
        String userId = "user123";

        JSONObject response = new JSONObject();
        response.put("success", false);

        JwtService spyService = Mockito.spy(jwtService);
        doReturn(userId).when(spyService).extractSubject(token);

        when(jsonReader.getActiveUserInfo(userId, authHeader)).thenReturn(response);
        when(jsonReader.getSuccessFromResponse(response)).thenReturn(false);
        when(jsonReader.getMessageFromResponse(response)).thenReturn("User not found");

        Exception ex = assertThrows(Exception.class, () -> spyService.getUserDetail(authHeader, token));
        assertEquals("User not found", ex.getMessage());
    }
}
